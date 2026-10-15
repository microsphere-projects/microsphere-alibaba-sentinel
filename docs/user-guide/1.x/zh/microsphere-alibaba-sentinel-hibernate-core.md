# microsphere-alibaba-sentinel-hibernate-core

[English](../microsphere-alibaba-sentinel-hibernate-core.md)

> **已归档的 `1.x` 分支线。** 本页文档描述 `0.1.x` 制品（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支线一致，仅基线版本不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。当前分支线对应的文档是 [../current/zh/microsphere-alibaba-sentinel-hibernate-core.md](../../current/zh/microsphere-alibaba-sentinel-hibernate-core.md)。

## 概述

`microsphere-alibaba-sentinel-hibernate-core` 使用 Alibaba Sentinel 保护 Hibernate ORM 的实体操作。它是一个插件
模块，实现 `microsphere-hibernate-core` 的 `EntityCallback` SPI（`io.microsphere.hibernate.entity.EntityCallback`），
并通过 Java Service Loader 文件 `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 注册自身。
`microsphere-hibernate` 安装的 Hibernate 事件监听器（`EntittyCallbackIntegrator`，它自身通过
`META-INF/services/org.hibernate.integrator.spi.Integrator` 注册）随后会调用该回调，于是受管实体上的每一次
`INSERT`、`UPDATE`、`LOAD` 与 `DELETE` 都会打开并关闭一个 Sentinel entry，完全无需改动应用代码。

从源码核实的关键事实：

- 入口类：`io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`。
- 插件名称（`SentinelHibernateConstants.PLUGIN_NAME`）：`hibernate`。
- Sentinel 上下文名称：`microsphere_sentinel_hibernate_context`（`SentinelHibernateConstants.DEFAULT_CONTEXT_NAME`，
  符合 `SentinelUtils.getDefaultContextName(String)` 的 `microsphere_sentinel_{}_context` 模式）。
- 来源：`SessionFactory`（`SentinelHibernateConstants.DEFAULT_ORIGIN`）。
- 资源类型：`ResourceTypeConstants.COMMON_DB_SQL`（`4`）；流量类型：`EntryType.IN`。
- 资源名模式：`Entity:<ACTION>:<fully qualified entity class name>`，由
  `getSentinelResourceName(Object, String)` 生成。例如 `Entity:INSERT:com.acme.domain.User`。
- 两阶段埋点：`onPreXxx` 调用 `SentinelOperations.begin(...)`，并把返回的
  `SentinelContext` 存入 `ThreadLocal`（`SentinelContext.withinContext()`）；`onPostXxx` 调用
  `SentinelContext.doInContext(consumer, true)` 来结束并移除它。
- 与 Redis 和 Spring Web 插件不同，这个类**不**继承 `AbstractSentinelPlugin`；它实现
  `SentinelPlugin`，并把插件元数据委托给一个以 `autoInstalled = false` 创建的 `SimpleSentinelPlugin`，
  然后显式调用 `SentinelPlugin.install(this)` 安装自身。安装会把插件注册进
  `SentinelPluginRepository`，并且由于该仓库服务是 `JMXSentinelPluginRepository`，它同时被暴露为 MBean
  `io.microsphere.sentinel:type=SentinelPlugin,name=hibernate`。

## 版本信息

| 项目 | 值 |
|------|-------|
| 文档所描述的分支 | `release-1.x`（本文件位于 `docs/user-guide/1.x`） |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-hibernate-core` |
| 分支版本 | `0.1.6-SNAPSHOT` |
| 已发布版本 | `0.1.0` - `0.1.5` |
| Java 版本 | 8 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `microsphere-hibernate` BOM | 0.1.9 |

## 兼容性

依据模块 POM（所有第三方依赖都是 `optional`）以及 `SentinelHibernateEntityCallback` 中的 import 核实：

| 技术 | 状态 | 证据 |
|------------|--------|----------|
| Hibernate ORM `org.hibernate:hibernate-core` | 5.x，即 javax 命名空间那一代 | import 了 `org.hibernate.type.Type`；测试的 `META-INF/persistence.xml` 声明 JPA 2.1 schema 并使用 `jakarta.persistence.jdbc.*` 属性键，Hibernate 5.6 可以接受 |
| `microsphere-hibernate-core`（0.1.9） | 运行时必需，用于 `EntityCallback` SPI | 在模块 POM 中声明为 `optional`，由应用提供 |
| `sentinel-core` | 运行时必需 | 在模块 POM 中声明为 `optional` |
| Spring Boot | 可用，但本模块**不会**被自动配置 | `microsphere-alibaba-sentinel-spring-boot` 只自动配置 Druid、MyBatis、Redis 与 Spring Web |

构建说明：`microsphere-hibernate` 父 BOM 默认激活 `hibernate-5.6` profile（Hibernate `5.6.15.Final`），
并保留了从 `hibernate-5.5` 到 `hibernate-5.0`（`5.0.9.Final`）的 profile；这里用到的 `EntityCallback` 方法
在该区间内没有变化。

## 安装

加入 Microsphere Alibaba Sentinel BOM 与本模块：

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-sentinel-dependencies</artifactId>
            <version>0.1.5</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-hibernate-core</artifactId>
    </dependency>
    <!-- Provided by the application because the module declares them optional -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-hibernate-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>
</dependencies>
```

无需再做任何注册：jar 内的
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 文件只有一行，即
`io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`。

## API 参考

```java
public class SentinelHibernateEntityCallback
        implements io.microsphere.hibernate.entity.EntityCallback,
                   io.microsphere.alibaba.sentinel.common.SentinelPlugin {

    public SentinelHibernateEntityCallback();

    public SentinelHibernateEntityCallback(@Nonnull String contextName, @Nonnull String origin);

    // EntityCallback lifecycle methods
    public void onPreInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes);
    public void onPostInsert(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes);
    public void onPreUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes);
    public void onPostUpdate(Object entity, Object id, Object[] state, String[] propertyNames, Type[] propertyTypes);
    public void onPreLoad(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types);
    public void onPostLoad(Object entity, Object id, String[] propertyNames, Type[] types);   // no state parameter
    public void onPreDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types);
    public void onPostDelete(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types);

    // SentinelPlugin contract, delegated to SimpleSentinelPlugin
    public boolean isAutoInstalled();
    public boolean isEnabled();
    public void setEnabled(boolean enabled);
    @Nonnull public String getName();           // "hibernate"
    @Nonnull public String getContextName();    // "microsphere_sentinel_hibernate_context"
    @Nonnull public String getOrigin();         // "SessionFactory"
    public int getResourceType();               // ResourceTypeConstants.COMMON_DB_SQL (4)
    @Nonnull public EntryType getTrafficType(); // EntryType.IN

    // Extension points
    protected Optional<SentinelContext> begin(Object entity, String action);
    protected void end();
    protected String getSentinelResourceName(Object entity, String action);
}
```

编写规则时需要留意的行为细节：

- 当 `isEnabled()` 为 `false` 时，每个 `onPreXxx` 方法都是空操作；`onPostXxx` 同样如此，因此在操作进行中途调用
  `setEnabled(false)` 不会去动 `ThreadLocal` 中的上下文，也不会抛异常。
- `getSentinelResourceName` 使用 `entity.getClass().getName()`，也就是具体（可能是 Hibernate 代理后的）实体类；
  如果你需要代理对象的非映射类名，请覆盖它。
- `begin(Object, String)` 在禁用时返回 `Optional.empty()`，其余情况下包装那个受检的
  `SentinelOperations.begin(...)` 调用，因此 Sentinel 的 `BlockException` 会从 Hibernate 事件传播给调用方。

## 配置属性

| 属性 | 默认值 | 读取位置 | 效果 |
|----------|---------|------------------|--------|
| `microsphere.sentinel.enabled` | `true` | Spring Boot 模块（`@ConditionalOnSentinelEnabled`） | 只约束其他插件的自动配置；本模块从不读取它 |
| `microsphere.sentinel.hibernate.enabled` | `true` | 经 `System.getProperty` 调用 `SentinelUtils.isPluginEnabled(String)` | 在 `SentinelHibernateEntityCallback` 构造时读取一次；取值按 `!"false".equalsIgnoreCase(value)` 判定，因此除 `false` 之外的任何值都会保持拦截开启 |
| `SentinelConstants.DEFAULT_CONTEXT_NAME` | `microsphere_sentinel_default_context` | `SentinelTemplate.begin` | 当传入空白上下文名时的回退上下文名；默认构造器不会用到 |

由于启用标志在构造时被捕获并由 `SimpleSentinelPlugin` 委托对象缓存，
`-Dmicrosphere.sentinel.hibernate.enabled=false` 必须在该回调实例化之前就存在。运行期请通过实例上的
`setEnabled(boolean)`，或注册后的 `SentinelPlugin` MBean 来切换插件。

## 使用示例

### 在 JPA/Hibernate 单元中自动注册

```java
// adapted from SentinelHibernateEntityCallbackTest
// Any CRUD through the SessionFactory triggers the callback; the test base class
// (AbstractHibernateH2Test) persists a User entity, which produces:
//   Entity:INSERT:io.microsphere.sentinel.hibernate.test.entity.User
// The assertion below proves the entrance node of the plugin context is not empty.
@AfterEach
void postTest() {
    Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    DefaultNode entranceNode = context.getEntranceNode();
    Set<Node> childList = entranceNode.getChildList();
    assertFalse(childList.isEmpty());
    exit();
}
```

### 关闭插件

```java
// adapted from SentinelHibernateEntityCallbackDisabledTest
// The property is a JVM system property, matching
// SentinelHibernateConstants.ENABLED_PROPERTY_NAME = "microsphere.sentinel.hibernate.enabled"
@BeforeAll
static void beforeAll() {
    setProperty(ENABLED_PROPERTY_NAME, "false");
}

@AfterAll
static void afterAll() {
    setProperty(ENABLED_PROPERTY_NAME, "true");
}
```

### 自定义上下文名称与来源

```java
// adapted from SentinelHibernateEntityCallback (public constructor) and SentinelHibernateConstantsTest
// assertEquals("microsphere_sentinel_hibernate_context", DEFAULT_CONTEXT_NAME);
// assertEquals("SessionFactory", DEFAULT_ORIGIN);
SentinelHibernateEntityCallback callback =
        new SentinelHibernateEntityCallback("my_hibernate_context", "EntityManager");

assertEquals("hibernate", callback.getName());
assertEquals("my_hibernate_context", callback.getContextName());
assertEquals("EntityManager", callback.getOrigin());
assertTrue(callback.isEnabled());
```

### 运行时切换与资源命名

```java
// adapted from SentinelHibernateEntityCallback.getSentinelResourceName and AbstractSentinelPlugin
callback.setEnabled(false); // interception skipped; onPreXxx returns without opening an entry
callback.setEnabled(true);

// Resource names observed in Sentinel metrics:
// Entity:INSERT:com.acme.domain.User
// Entity:UPDATE:com.acme.domain.User
// Entity:LOAD:com.acme.domain.User
// Entity:DELETE:com.acme.domain.User
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `SentinelPlugin`、
  `SentinelOperations`、`SentinelTemplate`、`SentinelContext` 与 `SentinelUtils`。
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) - 其他插件的自动配置；
  请注意本 Hibernate 插件只通过 Java SPI 注册，**不会**被自动配置。
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) 与
  [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) - 同为 JDBC 层的兄弟插件。
- [用户指南索引](./README.md) - 本分支的全部模块。
- [项目根目录](../../../../README.md) - 仓库总览。

---

## English

[English 版本](../microsphere-alibaba-sentinel-hibernate-core.md)
