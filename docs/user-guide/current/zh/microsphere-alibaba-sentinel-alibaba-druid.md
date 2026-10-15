# microsphere-alibaba-sentinel-alibaba-druid

[English](../microsphere-alibaba-sentinel-alibaba-druid.md)

## 概述

`microsphere-alibaba-sentinel-alibaba-druid` 保护流经 Alibaba **Druid** 连接池的 JDBC 流量。它的唯一运行时类
`SentinelAlibabaDruidFilter` 是一个 Druid `Filter`，同时实现了 `SentinelPlugin`：在被代理的 `DruidDataSource`
上执行的每一条语句，都会在执行前打开一个 Sentinel 上下文和 entry，并在执行后关闭它，从而使流控、线程并发和熔断
规则按 SQL 资源逐条生效。

该过滤器继承 `io.microsphere.alibaba.druid.filter.AbstractStatementFilter`（来自 `microsphere-alibaba-druid-core`），
后者从被执行的语句解析出 Sentinel 资源名，并回调两个 protected 钩子方法：
`beforeExecute(StatementProxy, String resourceName)` 和
`afterExecute(StatementProxy, String resourceName, Object result, Throwable failure)`。

由于该过滤器是一个 `SentinelPlugin`，它会以 `alibaba-druid` 为名把自己安装进共享的 `SentinelPluginRepository`，
并暴露为一个 JMX MBean，其启用状态可以在运行时切换。保护是对称的：两个钩子都由同一个 `isEnabled()` 判断把关，
因此禁用过滤器不会留下悬挂的 thread-local 上下文。

## 版本信息

| 项目 | 值 |
| --- | --- |
| 分支 | `release` —— 文档位于 `docs/user-guide/current/` |
| 坐标 | `io.github.microsphere-projects:microsphere-alibaba-sentinel-alibaba-druid:<version>` |
| 分支版本 | `0.2.6-SNAPSHOT` (`${revision}`) |
| 已发布版本 | `0.2.0` – `0.2.5` |
| 打包方式 | `jar` |
| POM 名称 | `Microsphere :: Alibaba Sentinel :: Plugins :: Alibaba Druid` |
| Java | 17 |
| 基础包 | `io.microsphere.alibaba.sentinel.alibaba.druid` |
| 起始版本 | `1.0.0` |

## 兼容性

| 依赖 | 版本 | 在本模块中的作用域 |
| --- | --- | --- |
| Alibaba Druid（`com.alibaba:druid`） | 1.2.28，由 `microsphere-alibaba-druid-dependencies` 0.2.20 管理 | `optional` —— 需自行提供 |
| `microsphere-alibaba-druid-core` | 0.2.20（同一个 BOM） | `optional` —— 提供 `AbstractStatementFilter` |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | `optional` —— 需自行提供 |
| `microsphere-alibaba-sentinel-commons` | `${revision}` | `compile` —— 唯一必需的项目内依赖 |
| Java | 17 | 必需 |
| Spring Cloud / Spring Boot | 2022.0.x – 2025.0.x / 3.x | 仅通过 `microsphere-alibaba-sentinel-spring-boot` 引入 |

在 Java 17 / Spring Boot 3.x 搭配 Druid 1.2.x 时选择 `0.2.x` 版本线。已归档的 `0.1.x` 版本线是 Java 8 /
Spring Boot 2.x 分支，对齐的是更早的 Druid 版本；只有当应用无法迁移到 Java 17 时才留在该分支上，并且绝不与
`0.2.x` 的 commons 混用，因为 optional 的 Druid 基线版本和插件契约在两条线之间都不一样。

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
        <artifactId>microsphere-alibaba-sentinel-alibaba-druid</artifactId>
    </dependency>
    <!-- both are optional upstream: declare them explicitly -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.2.28</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.10</version>
    </dependency>
</dependencies>
```

在 Spring Boot 中，更推荐额外引入 `microsphere-alibaba-sentinel-spring-boot`：`SentinelAlibabaDruidAutoConfiguration`
会在 `SentinelAutoConfiguration` 和 Microsphere 的 `AlibabaDruidAutoConfiguration` 之后贡献一个
`SentinelAlibabaDruidFilter` bean，并由 `@ConditionalOnAlibabaDruidAvailable` 与
`@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)` 把关。

## API 参考

```java
public class SentinelAlibabaDruidFilter extends AbstractStatementFilter implements SentinelPlugin

public SentinelAlibabaDruidFilter();                                        // default context name and origin
public SentinelAlibabaDruidFilter(String contextName, String origin);
```

第二个构造器用
`new SimpleSentinelPlugin(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL, IN, false)` 构建插件描述符——资源类型
`ResourceTypeConstants.COMMON_DB_SQL`，流量类型 `EntryType.IN`，`autoInstalled = false`——再用同一对参数创建
一个 `SentinelTemplate`，最后调用 `SentinelPlugin.install(this)`。安装过滤器自身而不是那个委托对象，正是
`alibaba-druid` MBean 得以发布的原因；`autoInstalled = false` 则避免委托对象再注册出一个重复条目。
`isAutoInstalled()`、`isEnabled()`、`setEnabled(boolean)`、`getName()`、
`getContextName()`、`getOrigin()`、`getResourceType()` 和 `getTrafficType()` 全部委托给这个内部插件。

两个钩子方法，与实现完全一致：

```java
protected void beforeExecute(StatementProxy statement, String resourceName) throws Throwable {
    if (isEnabled()) {
        SentinelContext context = this.sentinelOperations.begin(resourceName, getContextName(), getOrigin());
        context.withinContext();
    }
}

protected void afterExecute(StatementProxy statement, String resourceName, Object result, Throwable failure) {
    if (isEnabled()) {
        doInContext(context -> {
            context.setResult(result);
            context.setFailure(failure);
            this.sentinelOperations.end(context);
        }, true);
    }
}
```

由此带来的行为后果：`beforeExecute` 声明了 `throws Throwable`，因此 `begin` 抛出的 `BlockException` 会中止该条语句——
这正是预期的限流行为。`afterExecute` 无论结果如何都会执行，而一个非 null 的 `failure` 会让
`SentinelTemplate.end` 在 `entry.exit()` 和 `ContextUtil.exit()` 之前先调用 `Tracer.traceEntry(failure, entry)`，
于是异常比例规则会把这条 SQL 计为一次失败。上下文通过 `withinContext()` 绑定，通过
`doInContext(consumer, true)` 消费，后者会把它从线程上移除。

`SentinelAlibabaDruidConstants` 发布各装配取值：

| 常量 | 值 |
| --- | --- |
| `PLUGIN_NAME` | `"alibaba-druid"` |
| `DEFAULT_CONTEXT_NAME` | `"microsphere_sentinel_alibaba_druid_context"` |
| `DEFAULT_ORIGIN` | `"Filter"` |
| `ENABLED_PROPERTY_NAME` | `"microsphere.sentinel.alibaba-druid.enabled"` |

发现机制：JAR 中带有 `META-INF/services/com.alibaba.druid.filter.Filter`，其内容为
`io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidFilter`，因此 Druid 的过滤器服务加载可以自动
实例化它；显式注册或使用 Spring Boot bean 则是行为确定的替代方案。

## 配置属性

| 属性 | 类型 | 默认值 | 读取来源 | 作用 |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.alibaba-druid.enabled` | `boolean` | `true` | Java 系统属性（`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`） | 为 `isEnabled()` 提供初始值；`false` 会让两个钩子都变成空操作 |
| `microsphere.sentinel.enabled` | `boolean` | `true` | 系统属性与 application 配置（`SentinelConstants.ENABLED_PROPERTY_NAME`） | Spring Boot 自动配置层的全局开关 |

`SentinelUtils.isPluginEnabled("alibaba-druid")` 返回 `!"false".equalsIgnoreCase(value)`，所以只有字面量恰好为
`false` 才会禁用；而且在这一层只有 JVM 系统属性可见——写在 `application.properties` 里的条目是通过自动配置
条件影响过滤器的，并不是通过 `isEnabled()`。该值只在过滤器构造时读取一次；此后应使用 `setEnabled(boolean)` 或 MBean。

## 用法示例

在 `DruidDataSource` 上注册过滤器，与模块测试的做法一致：

```java
// adapted from SentinelAlibabaDruidFilterTest
class DruidSentinelSetup extends AbstractAlibabaDruidTest {

    private final SentinelAlibabaDruidFilter filter = new SentinelAlibabaDruidFilter();

    @Override
    protected void customize(DruidDataSource dataSource) {
        dataSource.getProxyFilters().add(filter);   // every statement becomes a Sentinel resource
    }

    @BeforeEach
    void setUp() throws Throwable {
        resetContextMap();                          // start each test with a clean Sentinel context map
    }
}
```

在不重建连接池的前提下于运行时切换保护：

```java
// adapted from SentinelAlibabaDruidFilterTest#testEnable / #testDisable
void setEnable(boolean enabled) {
    for (Filter proxyFilter : getDruidDataSource().getProxyFilters()) {
        if (proxyFilter instanceof SentinelAlibabaDruidFilter filter) {
            assertEquals(PLUGIN_NAME, filter.getName());
            filter.setEnabled(enabled);             // false -> statements run un-tracked, no exception raised
        }
    }
}

setEnable(true);
super.test();                                       // runs the CRUD statements against H2
Context context = enter(filter.getContextName(), filter.getOrigin());
assertEquals("microsphere_sentinel_alibaba_druid_context", context.getName());
```

校验装配常量与生成的属性名：

```java
// adapted from SentinelAlibabaDruidConstantsTest
assertEquals("alibaba-druid", PLUGIN_NAME);
assertEquals("microsphere_sentinel_alibaba_druid_context", DEFAULT_CONTEXT_NAME);
assertEquals("Filter", DEFAULT_ORIGIN);
assertEquals("microsphere.sentinel.alibaba-druid.enabled", ENABLED_PROPERTY_NAME);
assertEquals(getPluginEnabledPropertyName(PLUGIN_NAME), ENABLED_PROPERTY_NAME);

SentinelPlugin plugin = SentinelPluginRepository.INSTANCE.get("alibaba-druid");
assertEquals(ResourceTypeConstants.COMMON_DB_SQL, plugin.getResourceType());
assertEquals(EntryType.IN, plugin.getTrafficType());
```

对特定语句限流，使用插件的上下文名和 origin，以便针对 `default` origin 编写的规则能够命中：

```java
// rule shape adapted from AbstractSentinelTemplateTest
FlowRule rule = new FlowRule();
rule.setResource(resourceName);                     // the Druid statement resource name from AbstractStatementFilter
rule.setCount(1);
rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
rule.setLimitApp("default");
FlowRuleManager.loadRules(List.of(rule));
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— `SentinelPlugin`、`SentinelOperations`、`SentinelContext`、`SentinelTemplate`
- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) —— 插件聚合模块
- [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) —— 在同一段 SQL 上的 Mapper 级保护；只启用其中一层以避免重复计数
- `microsphere-alibaba-sentinel-spring-boot` —— `SentinelAlibabaDruidAutoConfiguration`
- [分支索引](./README.md) · [仓库根目录](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-alibaba-druid.md)
