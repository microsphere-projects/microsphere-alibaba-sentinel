# microsphere-alibaba-sentinel-hibernate-core

[English](../microsphere-alibaba-sentinel-hibernate-core.md)

## 概述

`microsphere-alibaba-sentinel-hibernate-core` 使用 Alibaba Sentinel 保护 Hibernate ORM 的实体操作。它是一个插件
模块，实现了 `microsphere-hibernate-core` 的 `EntityCallback` SPI
（`io.microsphere.hibernate.entity.EntityCallback`），并通过 Java Service Loader 文件
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 完成注册。由
`microsphere-hibernate` 安装的 Hibernate 事件监听器（`EntittyCallbackIntegrator`，它自身通过
`META-INF/services/org.hibernate.integrator.spi.Integrator` 注册）随后会调用该回调，于是对受管实体的每一次
`INSERT`、`UPDATE`、`LOAD` 和 `DELETE` 都会打开并关闭一个 Sentinel entry，而无需改动任何应用代码。

从源码核实的关键事实：

- 入口类：`io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`。
- 插件名称（`SentinelHibernateConstants.PLUGIN_NAME`）：`hibernate`。
- Sentinel 上下文名：`microsphere_sentinel_hibernate_context`（`SentinelHibernateConstants.DEFAULT_CONTEXT_NAME`，
  符合 `SentinelUtils.getDefaultContextName(String)` 的 `microsphere_sentinel_{}_context` 模式）。
- origin：`SessionFactory`（`SentinelHibernateConstants.DEFAULT_ORIGIN`）。
- 资源类型：`ResourceTypeConstants.COMMON_DB_SQL`（`4`）；流量类型：`EntryType.IN`。
- 资源名模式：`Entity:<ACTION>:<fully qualified entity class name>`，由
  `getSentinelResourceName(Object, String)` 生成。例如：`Entity:INSERT:com.acme.domain.User`。
- 两阶段埋点：`onPreXxx` 调用 `SentinelOperations.begin(...)`，并把返回的
  `SentinelContext` 存入 `ThreadLocal`（`SentinelContext.withinContext()`）；`onPostXxx` 调用
  `SentinelContext.doInContext(consumer, true)` 来结束并移除它。
- 与 Redis 和 Spring Web 插件不同，这个类**并不**继承 `AbstractSentinelPlugin`；它实现
  `SentinelPlugin`，把插件元数据委托给一个以 `autoInstalled = false` 创建的 `SimpleSentinelPlugin`，
  随后用 `SentinelPlugin.install(this)` 显式安装自身。安装会把插件注册进
  `SentinelPluginRepository`，并且由于该仓库服务是 `JMXSentinelPluginRepository`，插件同时被暴露为
  MBean `io.microsphere.sentinel:type=SentinelPlugin,name=hibernate`。

## 版本信息

| 项目 | 值 |
|------|-------|
| 文档所描述的分支 | `release`（本文件位于 `docs/user-guide/current`） |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-hibernate-core` |
| 分支版本 | `0.2.6-SNAPSHOT` |
| 已发布版本 | `0.2.0` - `0.2.5` |
| Java 版本 | 17 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `microsphere-hibernate` BOM | 0.2.10 |

## 兼容性

以下结论对照模块 POM（所有第三方依赖均为 `optional`）以及 `SentinelHibernateEntityCallback` 中的 import 核实：

| 技术 | 状态 | 依据 |
|------------|--------|----------|
| Hibernate ORM `org.hibernate.orm:hibernate-core` | 6.x 与 7.x（Jakarta 命名空间） | import 了 `org.hibernate.type.Type`；测试的 `META-INF/persistence.xml` 使用 `jakarta.persistence.jdbc.*` 属性 |
| `microsphere-hibernate-core`（0.2.10） | 运行时必需，用于 `EntityCallback` SPI | 在模块 POM 中声明为 `optional`，由应用提供 |
| `sentinel-core` | 运行时必需 | 在模块 POM 中声明为 `optional` |
| Spring Boot | 可以工作，但本模块**不会**被自动配置 | `microsphere-alibaba-sentinel-spring-boot` 只自动配置 Druid、MyBatis、Redis 和 Spring Web |

构建说明：`microsphere-hibernate` 父 BOM 默认激活 `hibernate-7.3` profile（Hibernate `7.3.4.Final`），
并保留了从 `6.2.52.Final` 一直到 `6.6.50.Final` 的 profile；这里用到的 `EntityCallback` 方法在各版本间没有变化。

## 安装

加入 Microsphere Alibaba Sentinel BOM 和本模块：

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-sentinel-dependencies</artifactId>
            <version>0.2.5</version>
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

不需要再做任何注册：jar 内的 `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` 文件只有一行，
即 `io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`。

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

编写规则时需要注意的行为细节：

- 当 `isEnabled()` 为 `false` 时，每个 `onPreXxx` 方法都是空操作；`onPostXxx` 同样如此，因此在执行过程中
  `setEnabled(false)` 只会让 `ThreadLocal` 上下文保持不变，而不会抛异常。
- `getSentinelResourceName` 使用 `entity.getClass().getName()`，也就是具体（可能是 Hibernate 代理生成的）实体
  类；如果你需要为代理对象取未映射的类名，请覆盖它。
- `begin(Object, String)` 在禁用时返回 `Optional.empty()`，其余情况下它把受检的
  `SentinelOperations.begin(...)` 调用包装起来，因此 Sentinel 的 `BlockException` 会从 Hibernate 事件一路传播给调用方。

## 配置属性

| 属性 | 默认值 | 读取位置 | 作用 |
|----------|---------|------------------|--------|
| `microsphere.sentinel.enabled` | `true` | Spring Boot 模块（`@ConditionalOnSentinelEnabled`） | 只决定其他插件的自动配置是否生效；本模块从不读取它 |
| `microsphere.sentinel.hibernate.enabled` | `true` | `SentinelUtils.isPluginEnabled(String)`，经 `System.getProperty` | 在 `SentinelHibernateEntityCallback` 构造时读取一次；取值按 `!"false".equalsIgnoreCase(value)` 判断，因此除 `false` 之外的任何值都会保持拦截开启 |
| `SentinelConstants.DEFAULT_CONTEXT_NAME` | `microsphere_sentinel_default_context` | `SentinelTemplate.begin` | 当传入的上下文名为空白时使用的回退上下文名；默认构造器不会用到它 |

由于启用标志是在构造时被捕获并由 `SimpleSentinelPlugin` 委托对象缓存的，
`-Dmicrosphere.sentinel.hibernate.enabled=false` 必须在该回调被实例化之前就存在。在运行时，
请通过实例上的 `setEnabled(boolean)`，或注册出来的 `SentinelPlugin` MBean 来切换插件状态。

## 用法示例

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

### 禁用插件

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

### 自定义上下文名与 origin

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
  请注意这个 Hibernate 插件只通过 Java SPI 注册，**不会**被自动配置。
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) 与
  [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) - 同属 JDBC 层的兄弟插件。
- [用户手册索引](./README.md) - 当前分支的全部模块。
- [项目根目录](../../../../README.md) - 仓库总览。

---

## English

[English 版本](../microsphere-alibaba-sentinel-hibernate-core.md)
