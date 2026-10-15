# microsphere-alibaba-sentinel-p6spy

[English](../microsphere-alibaba-sentinel-p6spy.md)

## 概述

`microsphere-alibaba-sentinel-p6spy` 把每一条经过 [P6Spy](https://github.com/p6spy/p6spy)
的 SQL 语句变成一个 Sentinel 资源。本模块提供 `io.microsphere.alibaba.sentinel.p6spy.SentinelJdbcEventListener`，它
继承 P6Spy 的 `com.p6spy.engine.event.SimpleJdbcEventListener`，并通过 Java Service Loader 文件
`META-INF/services/com.p6spy.engine.event.JdbcEventListener` 注册。P6Spy 会自行发现并实例化这个监听器，
因此不需要 Spring、不需要配置类、也不需要应用代码——只要一个 `jdbc:p6spy:` URL。

从源码核实的关键事实：

- 插件名称（`SentinelP6SpyConstants.PLUGIN_NAME`）：`p6spy`。
- 上下文名：`microsphere_sentinel_p6spy_context`（`SentinelP6SpyConstants.DEFAULT_CONTEXT_NAME`，即
  由 `SentinelUtils.getDefaultContextName(String)` 应用 `microsphere_sentinel_{}_context` 模式得到的结果）。
- origin：`Statement`（`SentinelP6SpyConstants.DEFAULT_ORIGIN`）。
- 资源类型：`ResourceTypeConstants.COMMON_DB_SQL`（`4`）；流量类型：`EntryType.IN`。两者都由
  `getResourceType()` 和 `getTrafficType()` 中硬编码的覆盖实现返回。
- 资源名：P6Spy 上报的 SQL 字符串，即 `statementInformation.getSql()`（见 `getResourceName`）。
- 只跟踪预编译语句：`isEligibleStatement(StatementInformation)` 返回
  `statementInformation instanceof PreparedStatementInformation`，因此 `Statement.execute("...")` 调用会被忽略。
- 两阶段 entry：`onBeforeAnyExecute` 打开 entry 并把 `SentinelContext` 存入 `ThreadLocal`
  （`SentinelContext.withinContext()`）；`onAfterAnyExecute` 取回并移除它
  （`SentinelContext.doInContext(consumer, true)`），用 `context.setFailure(e)` 记录 `SQLException`，并交由
  `SentinelTemplate.end(...)` 在退出之前通过 `Tracer.traceEntry` 上报。
- 与 Hibernate 插件一样，这个类**并不**继承 `AbstractSentinelPlugin`；它实现 `SentinelPlugin`，
  并把元数据委托给一个以 `autoInstalled = false` 构造的 `SimpleSentinelPlugin`，同时显式调用
  `SentinelPlugin.install(this)`。安装会注册 MBean `io.microsphere.sentinel:type=SentinelPlugin,name=p6spy`。

## 版本信息

| 项目 | 值 |
|------|-------|
| 文档所描述的分支 | `release`（本文件位于 `docs/user-guide/current`） |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-p6spy` |
| 分支版本 | `0.2.6-SNAPSHOT` |
| 已发布版本 | `0.2.0` - `0.2.5` |
| Java 版本 | 17 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `p6spy:p6spy` | 3.9.1（3.x 版本线，声明为 `optional`） |

## 兼容性

| 技术 | 状态 | 依据 |
|------------|--------|----------|
| P6Spy 3.x（`p6spy:p6spy:3.9.1`） | 必需 | `com.p6spy.engine.event.SimpleJdbcEventListener`、`PreparedStatementInformation`、`StatementInformation` 的 import；已发布的模块 POM 把 `p6spy` 解析到 `3.9.1` |
| 任意 JDBC 4.x 驱动 + `P6SpyDriver` | 必需 | 测试使用 `com.p6spy.engine.spy.P6SpyDriver` 配合 `jdbc:p6spy:h2:mem:test_mem` |
| 连接池（Druid、Hikari 等） | 中立 | 连接池只看到 P6Spy 驱动；测试包装的是 Druid `DruidDataSource` |
| `sentinel-core` | 运行时必需 | 在模块 POM 中声明为 `optional` |
| Spring Boot | 可以工作，但**不会被自动配置** | `microsphere-alibaba-sentinel-spring-boot` 中没有 `SentinelP6SpyAutoConfiguration`；imports 文件只列出 Druid、MyBatis、Redis 和 Spring Web |

P6Spy 3.9.1 是 Microsphere 父级依赖链所使用的最新 3.x 版本；这里用到的监听器 API
（`onBeforeAnyExecute` / `onAfterAnyExecute`）在整个 3.x 版本线上保持稳定。

## 安装

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
        <artifactId>microsphere-alibaba-sentinel-p6spy</artifactId>
    </dependency>
    <!-- Both dependencies below are optional in the plugin POM and must be declared by the application -->
    <dependency>
        <groupId>p6spy</groupId>
        <artifactId>p6spy</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>
</dependencies>
```

然后把数据源指向 P6Spy 驱动，并在 classpath 上保留 `spy.properties`，监听器就会自动地从插件 jar 的服务文件
中被加载：

```properties
# adapted from src/test/resources/spy.properties
driverlist=org.h2.Driver
```

## API 参考

```java
public class SentinelJdbcEventListener
        extends com.p6spy.engine.event.SimpleJdbcEventListener
        implements io.microsphere.alibaba.sentinel.common.SentinelPlugin {

    public SentinelJdbcEventListener();
    public SentinelJdbcEventListener(String contextName, String origin);

    // P6Spy lifecycle
    public void onBeforeAnyExecute(StatementInformation statementInformation);
    public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException e);

    // Extension points
    protected String getResourceName(StatementInformation statementInformation);
    protected boolean isEligibleStatement(StatementInformation statementInformation);

    // SentinelPlugin contract (name/context/origin delegate to SimpleSentinelPlugin;
    // resource type and traffic type are overridden constants)
    public boolean isAutoInstalled();
    public boolean isEnabled();
    public void setEnabled(boolean enabled);
    public String getName();            // "p6spy"
    public String getContextName();     // "microsphere_sentinel_p6spy_context"
    public String getOrigin();          // "Statement"
    public int getResourceType();       // ResourceTypeConstants.COMMON_DB_SQL (4)
    public EntryType getTrafficType();  // EntryType.IN
}
```

说明：

- `getResourceName` 是 `protected` 的，因此覆盖它就是调整粒度的受支持方式，例如返回
  `statementInformation.getSqlWithValues()`，或返回 SQL 的规范化摘要。
- 底层 `SentinelOperations`（`SentinelTemplate`）的
  `begin`/`end` 可能抛出 `BlockException`；它会作为语句执行期的一次运行时失败传播给 JDBC 调用方。
- 插件被禁用时，两个生命周期方法立即返回且不打开 entry，因此该数据源不会产生任何 Sentinel 指标。

## 配置属性

| 属性 | 默认值 | 读取位置 | 作用 |
|----------|---------|------------------|--------|
| `microsphere.sentinel.enabled` | `true` | Spring Boot 模块（`@ConditionalOnSentinelEnabled`） | 仅作为自动配置的开关；本模块从不读取它 |
| `microsphere.sentinel.p6spy.enabled` | `true` | `SentinelUtils.isPluginEnabled(String)`，经 `System.getProperty` | 在 P6Spy 创建监听器实例时捕获；按 `!"false".equalsIgnoreCase(value)` 判断 |
| `SentinelConstants.DEFAULT_CONTEXT_NAME` | `microsphere_sentinel_default_context` | `SentinelTemplate.begin` | 当双参构造器收到空白的上下文名时使用的回退值 |

请在启动之前设置插件属性，例如 `-Dmicrosphere.sentinel.p6spy.enabled=false`；或者通过 `setEnabled(boolean)`
切换运行中的实例（也可以经由 `SentinelPlugin` JMX MBean 完成）。

## 用法示例

### 在 P6Spy 中注册数据源

```java
// adapted from SentinelJdbcEventListenerTest#buildDruidDataSource
@Override
protected DruidDataSource buildDruidDataSource() {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setDriverClassName("com.p6spy.engine.spy.P6SpyDriver");
    dataSource.setUrl("jdbc:p6spy:h2:mem:test_mem");
    dataSource.setUsername("sa");
    return dataSource;
}
```

### 校验监听器的默认值

```java
// adapted from SentinelJdbcEventListenerTest#testDefaults
SentinelJdbcEventListener listener = new SentinelJdbcEventListener();
assertTrue(listener.isEnabled());
assertEquals(PLUGIN_NAME, listener.getName());                    // "p6spy"
assertEquals(DEFAULT_CONTEXT_NAME, listener.getContextName());    // microsphere_sentinel_p6spy_context
assertEquals(DEFAULT_ORIGIN, listener.getOrigin());               // "Statement"
```

### 在运行时关闭拦截

```java
// adapted from SentinelJdbcEventListenerTest#testDisabled
SentinelJdbcEventListener listener = new SentinelJdbcEventListener();
listener.setEnabled(false);
listener.onBeforeAnyExecute(null);                 // no-op: isEnabled() is false
listener.onAfterAnyExecute(null, 0L, null);        // no-op as well
assertFalse(listener.isEnabled());
```

### 断言 Sentinel 节点已被创建

```java
// adapted from SentinelJdbcEventListenerTest#postTest
// Any prepared statement executed through jdbc:p6spy:h2:mem:test_mem registers a child node
// whose name is the SQL text, e.g. "select * from user where id = ?"
@AfterEach
void postTest() {
    Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
    DefaultNode entranceNode = context.getEntranceNode();
    Set<Node> childList = entranceNode.getChildList();
    assertFalse(childList.isEmpty());
    exit();
}
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `SentinelPlugin`、
  `SentinelOperations`、`SentinelTemplate`、`SentinelContext`、`SentinelUtils`。
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) - Druid、MyBatis、
  Redis 和 Spring Web 的自动配置；P6Spy 有意不做自动配置，因为它的注册是通过 P6Spy 自己的服务加载器完成的。
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) - 基于 Druid
  `Filter` 的 SQL 保护，是在连接池层对 P6Spy 的一种替代。
- [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md) - 实体级的 SQL
  保护，使用同样的 `COMMON_DB_SQL` 资源类型。
- [用户手册索引](./README.md) - 当前分支的全部模块。
- [项目根目录](../../../../README.md) - 仓库总览。

---

## English

[English 版本](../microsphere-alibaba-sentinel-p6spy.md)
