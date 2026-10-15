# microsphere-alibaba-sentinel-hibernate-core — 开发者指南

[English](../microsphere-alibaba-sentinel-hibernate-core.md)

## 目的与范围

本模块用 Alibaba Sentinel 流控来保护 Hibernate 的实体持久化操作。
它提供 `SentinelHibernateEntityCallback`，即同级项目 `microsphere-hibernate-core` 发布的
`io.microsphere.hibernate.entity.EntityCallback` SPI 的一个实现。该 callback 通过
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 被发现，因此只要把 jar
放到 classpath 上，就会启用对实体 `INSERT`、`UPDATE`、`LOAD`、`DELETE` 操作的拦截。

本指南的范围：

- 前置/后置实体 callback 如何开启与关闭一个 Sentinel entry（`begin` / `end`）。
- 资源名以及 Sentinel 上下文名/origin 如何构建。
- 由 `microsphere.sentinel.hibernate.enabled` 驱动的启用/禁用语义。
- 在本发布线（0.2.x、Java 17、`sentinel-core` 1.8.10）上，本模块运行在 Hibernate ORM 6 的
  `jakarta` 命名空间之上，测试用的 `src/test/resources/META-INF/persistence.xml` 可以佐证。

## 架构

```mermaid
graph TB
    subgraph Hibernate Stack
        SES[Session and EntityManager]
        EVD[Entity event dispatch]
        MH[microsphere-hibernate-core EntityCallback SPI]
    end
    subgraph This Module
        CB[SentinelHibernateEntityCallback]
        KC[SentinelHibernateConstants]
        SVC[META-INF services file]
    end
    subgraph Commons
        SP[SentinelPlugin]
        SSP[SimpleSentinelPlugin]
        SOP[SentinelOperations]
        ST[SentinelTemplate]
        SC[SentinelContext]
    end
    SENT[Sentinel Core SphU Tracer ContextUtil]
    SES --> EVD --> MH
    SVC -. ServiceLoader .-> MH
    MH --> CB
    CB --> KC
    CB --> SP
    SP --> SSP
    CB --> SOP
    SOP --> ST
    ST --> SENT
    CB --> SC
```

## 源码分析

### `io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`

声明为 `public class SentinelHibernateEntityCallback implements EntityCallback, SentinelPlugin`。
注意它**不**继承 `AbstractSentinelPlugin`，而是持有一个插件委托。

字段与构造器：

- `private final SentinelPlugin delegate;`
- `private final SentinelOperations sentinelOperations;`
- `public SentinelHibernateEntityCallback()` —— 以 `SentinelHibernateConstants` 的
  `DEFAULT_CONTEXT_NAME` 和 `DEFAULT_ORIGIN` 委托给两参数构造器。
- `public SentinelHibernateEntityCallback(@Nonnull String contextName, @Nonnull String origin)` ——
  创建 `new SimpleSentinelPlugin(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL, IN, false)`
  （这里的 `false` 抑制了 `AbstractSentinelPlugin` 的自我安装），随后
  `new SentinelTemplate(getResourceType(), getTrafficType())`，再调用静态方法
  `SentinelPlugin.install(this)`，它把该 callback 注册进 `SentinelPluginRepository.INSTANCE`，
  并添加一个在 JVM 关闭时卸载它的回调。

实体生命周期钩子 —— `EntityCallback` SPI 为每种动作提供成对的前置/后置通知：

- `public void onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)`
  —— `begin(entity, "INSERT").ifPresent(SentinelContext::withinContext)`。
- `public void onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` —— `end()`。
- `public void onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)`
  —— `begin(entity, "UPDATE").ifPresent(SentinelContext::withinContext)`。
- `public void onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes)` —— `end()`。
- `public void onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)`
  —— `begin(entity, "LOAD").ifPresent(SentinelContext::withinContext)`。
- `public void onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types)` —— `end()`。
- `public void onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)`
  —— `begin(entity, "DELETE").ifPresent(SentinelContext::withinContext)`。
- `public void onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types)` —— `end()`。

受保护的核心操作：

- `protected Optional<SentinelContext> begin(Object entity, String action)` —— 仅当 `isEnabled()`：
  构建 `resourceName = getSentinelResourceName(entity, action)`，从委托读取 `contextName` 和 `origin`，
  并返回 `Optional.of(execute(() -> sentinelOperations.begin(resourceName, contextName, origin)))`，
  其中 `execute` 是 `io.microsphere.lang.function.ThrowableSupplier.execute`，它会重新抛出任何
  `Throwable` —— 包括 `SphU.entry` 抛出的 Sentinel `BlockException`。禁用时返回 `Optional.empty()`。
- `protected void end()` —— 仅当 `isEnabled()`：`doInContext(this.sentinelOperations::end, true)`，
  它把当前 `SentinelContext` 从 `ThreadLocal` 移除并关闭。
- `protected String getSentinelResourceName(Object entity, String action)` ——
  返回 `"Entity:" + action + ":" + entity.getClass().getName()`；覆盖它即可自定义命名。

资源名/上下文构建小结：资源名完全由实体类名和操作推导；上下文名与 origin 来自委托插件
（`microsphere_sentinel_hibernate_context` / `SessionFactory`）。在 `SentinelTemplate.begin` 中，
空白的上下文名或 origin 会先回退到 `SentinelConstants.DEFAULT_CONTEXT_NAME` / `DEFAULT_ORIGIN`，
然后再执行 `ContextUtil.enter` 与 `SphU.entry(resourceName, resourceType, trafficType)`。

插件接口方法（`isAutoInstalled`、`isEnabled`、`setEnabled`、`getName`、`getContextName`、
`getOrigin`、`getResourceType`、`getTrafficType`）全部转发给 `delegate`。

### `io.microsphere.alibaba.sentinel.hibernate.SentinelHibernateConstants`

一个常量 `interface`，由 `SentinelHibernateConstantsTest` 断言校验：

- `String PLUGIN_NAME = "hibernate"`。
- `String DEFAULT_CONTEXT_NAME = "microsphere_sentinel_hibernate_context"` —— 符合 commons 的
  模板 `microsphere_sentinel_{}_context`。
- `String DEFAULT_ORIGIN = "SessionFactory"`。
- `String ENABLED_PROPERTY_NAME = "microsphere.sentinel.hibernate.enabled"` —— 由
  `PROPERTY_NAME_PREFIX + PLUGIN_NAME + DOT + enabled` 拼成，标注 `@ConfigurationProperty(type = boolean.class,
  defaultValue = "true", source = SYSTEM_PROPERTIES_SOURCE)`。

### SPI 注册

`src/main/resources/META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 只包含一行：
`io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`。

### 测试

- `SentinelHibernateEntityCallbackTest` 继承 `AbstractHibernateH2Test`（microsphere-hibernate-test，
  H2 配置位于 `src/test/resources/META-INF/persistence.xml`，单元为 `CRM`，实体为
  `io.microsphere.sentinel.hibernate.test.entity.User`）。其 `@AfterEach postTest()` 以 origin
  `SessionFactory` 进入上下文 `microsphere_sentinel_hibernate_context`，并断言 entrance node 的子节点列表非空 ——
  这证明基类测试所执行的 CRUD 已经创建了 Sentinel 节点。
- `SentinelHibernateEntityCallbackDisabledTest` 在 `@BeforeAll` 中设置系统属性
  `microsphere.sentinel.hibernate.enabled=false`（在 `@AfterAll` 中恢复），从而让同样的生命周期在
  所有 `begin`/`end` 工作被短路的情况下运行。

## 设计模式

- **SPI（服务提供者注册）**：`SentinelHibernateEntityCallback` 通过 `META-INF/services`
  注册为 `io.microsphere.hibernate.entity.EntityCallback` 实现，由宿主项目通过 `ServiceLoader` 加载。
- **委托/适配器**：该类实现 `SentinelPlugin`，但把所有插件元数据与启用状态委托给私有的
  `SimpleSentinelPlugin`（它自身继承 `AbstractSentinelPlugin`），使 `EntityCallback` 类不携带插件状态字段。
- **模板方法（操作执行）**：全部 Sentinel 机制集中在
  `SentinelOperations`/`SentinelTemplate` 中；callback 只提供资源名、上下文名与 origin，并选择两阶段调用。
- **观察者（前置/后置事件）**：Hibernate 的实体动作通知成对驱动 `begin`/`end`。
- **线程局部上下文耦合**：`SentinelContext.withinContext()` 与
  `SentinelContext.doInContext(consumer, true)` 在前置/后置钩子之间绑定并弹出上下文，
  因此它们必须运行在同一线程上。

## 核心流程

### 实体操作保护流程

```mermaid
sequenceDiagram
    participant HV as Hibernate ORM
    participant MH as microsphere-hibernate dispatcher
    participant CB as SentinelHibernateEntityCallback
    participant ST as SentinelTemplate
    participant SL as Sentinel Core
    HV->>MH: entity pre-insert event
    MH->>CB: onPreInsert entity id state propertyNames types
    CB->>CB: isEnabled check via delegate
    CB->>ST: begin resourceName contextName origin
    ST->>SL: ContextUtil.enter contextName origin
    ST->>SL: SphU.entry resourceName COMMON_DB_SQL IN
    SL-->>ST: Entry
    ST-->>CB: SentinelContext
    CB->>CB: withinContext binds ThreadLocal
    MH->>HV: real INSERT executes
    HV->>MH: entity post-insert event
    MH->>CB: onPostInsert
    CB->>CB: doInContext with removal
    CB->>ST: end context
    ST->>SL: entry.exit then ContextUtil.exit
```

### 禁用与限流路径

```mermaid
flowchart TD
    A[onPreInsert invoked] --> B{isEnabled}
    B -- false --> C[begin returns Optional empty, no entry, no context]
    B -- true --> D[sentinelOperations.begin]
    D --> E{SphU.entry outcome}
    E -- allowed --> F[SentinelContext bound to ThreadLocal]
    E -- BlockException --> G[exception rethrown through ThrowableSupplier execute, entity operation aborted]
    F --> H[onPostInsert]
    H --> I[end via doInContext forRemoval true]
    C --> J[onPostInsert end finds no ThreadLocal context]
```

关于错误统计的说明：与一次性的 `SentinelOperations.call`（它对非 `BlockException` 的 throwable 调用 `Tracer.trace`）
不同，也与 p6spy/redis/spring-web 插件（它们调用 `SentinelContext.setFailure`，从而让 `SentinelTemplate.end`
执行 `Tracer.traceEntry`）不同，`EntityCallback` 的后置钩子收不到异常参数，因此 `end()` 永远不会携带已记录的失败，
本插件会跳过 `traceEntry`。

## 依赖关系

```mermaid
graph LR
    M[microsphere-alibaba-sentinel-hibernate-core] --> C1[microsphere-alibaba-sentinel-commons]
    M --> C2[microsphere-hibernate-core]
    M --> C3[hibernate-core org.hibernate.orm]
    M --> C4[sentinel-core]
    M --> C5[microsphere-annotation-processor]
    M --> T1[microsphere-hibernate-test]
    M --> T2[junit-jupiter mockito-core mockito-junit-jupiter]
    M --> T3[h2 logback-classic]
```

| 依赖 | pom 中的作用域 |
|---|---|
| io.github.microsphere-projects:microsphere-annotation-processor | compile, optional |
| io.github.microsphere-projects:microsphere-alibaba-sentinel-commons | compile |
| io.github.microsphere-projects:microsphere-hibernate-core | compile, optional |
| com.alibaba.csp:sentinel-core | compile, optional |
| org.hibernate.orm:hibernate-core | compile, optional |
| org.junit.jupiter:junit-jupiter | test |
| org.mockito:mockito-core | test |
| org.mockito:mockito-junit-jupiter | test |
| io.github.microsphere-projects:microsphere-hibernate-test | test |
| ch.qos.logback:logback-classic | test |
| com.h2database:h2 | test |

## 扩展点

- **自定义资源命名**：继承 `SentinelHibernateEntityCallback` 并覆盖
  `getSentinelResourceName(Object entity, String action)`（例如改用简单类名或按 schema 命名）。
- **上下文/origin 调整**：使用 `SentinelHibernateEntityCallback(contextName, origin)`
  构造，而不是默认值。
- **运行时切换**：通过委托调用 `setEnabled(false)`/`enable()`/`disable()`，或在插件构造之前设置
  `microsphere.sentinel.hibernate.enabled` JVM 系统属性；`-Dmicrosphere.sentinel.hibernate.enabled=false`
  会整体关闭拦截。
- **替换 SPI 条目**：通过同样的 `META-INF/services` 机制注册你自己的 `EntityCallback`；
  `SentinelPlugin.install(SentinelPlugin)` 让任何自定义插件都能加入 `SentinelPluginRepository`
  以被监控/卸载。

## 相关模块

- 开发者指南索引：[README](./README.md)
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— `SentinelPlugin`、
  `AbstractSentinelPlugin`、`SimpleSentinelPlugin`、`SentinelOperations`、`SentinelTemplate`、
  `SentinelContext`、`SentinelUtils`。
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) 与
  [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md) —— 同级的 JDBC 层插件。
- 对应用户指南：[../../user-guide/current/zh/microsphere-alibaba-sentinel-hibernate-core.md](../../../user-guide/current/zh/microsphere-alibaba-sentinel-hibernate-core.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-hibernate-core.md)
