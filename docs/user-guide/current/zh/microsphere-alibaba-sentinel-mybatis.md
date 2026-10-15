# microsphere-alibaba-sentinel-mybatis

[English](../microsphere-alibaba-sentinel-mybatis.md)

## 概述

`microsphere-alibaba-sentinel-mybatis` 使用 Alibaba Sentinel 保护 MyBatis 语句的执行。它的运行时类
`SentinelMyBatisExecutorFilter` 是一个 `io.microsphere.mybatis.executor.ExecutorFilter`，继承
`AbstractSentinelPlugin`，因此它同时是一个 MyBatis executor 拦截规则和一个带有名称、上下文、origin 以及可在运行时
切换的启用开关的 Sentinel 插件。

凡是经过被拦截 executor 路由的 `update` 和 `query`，都会被包装进一个 Sentinel entry，其资源名就是
`MappedStatement` 的 id——即 Mapper 方法的全限定名，例如
`com.acme.mapper.UserMapper.selectById`。因此 Mapper 方法成为流控与熔断的基本单位，与 SQL 文本以及其下方的
JDBC 驱动都无关。

该过滤器通过 Microsphere 的 MyBatis 扩展（`ExecutorFilter` SPI 加上
`InterceptingExecutorInterceptor`）注册，而不是自带一个 MyBatis `Interceptor`，所以它可以与其他 executor 过滤器
共存组合，而不会替换 MyBatis 的内部机制。

## 版本信息

| 项目 | 值 |
| --- | --- |
| 分支 | `release` —— 文档位于 `docs/user-guide/current/` |
| 坐标 | `io.github.microsphere-projects:microsphere-alibaba-sentinel-mybatis:<version>` |
| 分支版本 | `0.2.6-SNAPSHOT` (`${revision}`) |
| 已发布版本 | `0.2.0` – `0.2.5` |
| 打包方式 | `jar` |
| POM 名称 | `Microsphere :: Alibaba Sentinel :: Plugins :: MyBatis` |
| Java | 17 |
| 基础包 | `io.microsphere.alibaba.sentinel.mybatis`、`io.microsphere.alibaba.sentinel.mybatis.executor` |
| 起始版本 | `1.0.0` |

## 兼容性

| 依赖 | 版本 | 在本模块中的作用域 |
| --- | --- | --- |
| MyBatis（`org.mybatis:mybatis`） | 3.5.19，由 `microsphere-mybatis-dependencies` 0.2.16 管理 | `optional` —— 需自行提供 |
| `microsphere-mybatis-core` | 0.2.16（同一个 BOM） | `optional` —— 提供 `ExecutorFilter`、`ExecutorFilterChain`、`InterceptingExecutorInterceptor` |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | `optional` —— 需自行提供 |
| `microsphere-alibaba-sentinel-commons` | `${revision}` | `compile` —— 唯一必需的项目内依赖 |
| Java | 17 | 必需 |
| Spring Cloud / Spring Boot | 2022.0.x – 2025.0.x / 3.x | 仅通过 `microsphere-alibaba-sentinel-spring-boot` 引入 |

`0.2.x` 版本线要求 Java 17、MyBatis 3.5.x 和 Spring Boot 3.x，适用于 Spring Cloud 2022.0.x 到 2025.0.x。
已归档的 `0.1.x` 版本线（对齐 `microsphere-mybatis` 0.1.15）面向 Java 8 /
Spring Boot 2.x 一代以及更早的 MyBatis 构建；请让一个应用完整地停留在同一条版本线上，因为两条线之间的
optional MyBatis 基线版本和 `ExecutorFilter` 方法签名都有差异。

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
        <artifactId>microsphere-alibaba-sentinel-mybatis</artifactId>
    </dependency>
    <!-- both are optional upstream: declare them explicitly -->
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.5.19</version>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.10</version>
    </dependency>
</dependencies>
```

在 Spring Boot 下，请加入 `microsphere-alibaba-sentinel-spring-boot`：`SentinelMyBatisAutoConfiguration`
会在一个 `@EnableMyBatisExtension` 配置内贡献一个 `SentinelMyBatisExecutorFilter` bean，其顺序排在
`SentinelAutoConfiguration`、`MybatisAutoConfiguration`、`MybatisPlusAutoConfiguration` 以及 Microsphere 的
`MyBatisAutoConfiguration` 之后，并且只在 `@ConditionalOnMyBatisAvailable` 与
`@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)` 成立时生效。

## API 参考

```java
public class SentinelMyBatisExecutorFilter extends AbstractSentinelPlugin implements ExecutorFilter

public SentinelMyBatisExecutorFilter();                                       // default context name and origin
public SentinelMyBatisExecutorFilter(String contextName, String origin);
```

第二个构造器调用 `super(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL)`。`AbstractSentinelPlugin`
随后把流量类型默认设为 `EntryType.IN`、`autoInstalled` 默认设为 `true`，因此仅仅构造这个过滤器就会以 `mybatis`
为名把它安装进共享的 `SentinelPluginRepository`——并发布出 MBean
`io.microsphere.sentinel:type=SentinelPlugin,name=mybatis`。一个 `SentinelTemplate` 会用同样的资源类型和
流量类型创建出来。

被覆盖的 executor 方法，每一个都包装了接口的默认实现：

```java
public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException;
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                         CacheKey cacheKey, BoundSql boundSql, ExecutorFilterChain chain) throws SQLException;
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                         ExecutorFilterChain chain) throws SQLException;
```

包装逻辑集中在一处：

```java
protected <T> T doInSentinel(MappedStatement ms, Callable<T> callable) throws SQLException {
    if (isEnabled()) {
        String resourceName = getSentinelResourceName(ms);
        return sentinelOperations.call(resourceName, getContextName(), getOrigin(),
                context -> callable.call(), SQLException.class);
    } else {
        try {
            return callable.call();
        } catch (Exception e) {
            throw throwTarget(e, SQLException.class);
        }
    }
}

protected String getSentinelResourceName(MappedStatement ms) {
    return ms.getId();
}
```

- 启用时，Mapper 调用运行在 `SentinelOperations.call` 之内，它会进入上下文、打开 entry，并在 `finally`
  中结束它。回调抛出的任何 throwable 都会被重新抛出；若失败并非阻塞异常，则先用 `Tracer.trace(e)`
  上报，并以 `error` 级别记录日志，因此异常比例规则会对真实的 Mapper 失败作出反应，而 `BlockException`
  不会被计为失败。
- `SQLException.class` 参数只是收窄受检异常的声明，让这些方法得以保持 `ExecutorFilter` 的
  `throws SQLException` 契约。
- 禁用时，回调直接裸跑：不创建 entry，因此过滤器不记录任何资源，也不产生任何指标。
- 覆盖 `getSentinelResourceName(MappedStatement)` 可以让资源粒度变粗，例如按 Mapper 命名空间而非按
  statement id 归并。

`SentinelMyBatisConstants` 发布各装配取值：

| 常量 | 值 |
| --- | --- |
| `PLUGIN_NAME` | `"mybatis"` |
| `DEFAULT_CONTEXT_NAME` | `"microsphere_sentinel_mybatis_context"` |
| `DEFAULT_ORIGIN` | `"Executor"` |
| `ENABLED_PROPERTY_NAME` | `"microsphere.sentinel.mybatis.enabled"` |

发现机制：`META-INF/services/io.microsphere.mybatis.executor.ExecutorFilter` 中写着
`io.microsphere.alibaba.sentinel.mybatis.executor.SentinelMyBatisExecutorFilter`，因此 JAR 一旦进入 classpath，
Microsphere 的 MyBatis 扩展就会自动收集到这个过滤器。

## 配置属性

| 属性 | 类型 | 默认值 | 读取来源 | 作用 |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.mybatis.enabled` | `boolean` | `true` | Java 系统属性（`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`） | 为 `isEnabled()` 提供初始值；`false` 时 Mapper 在没有 Sentinel entry 的情况下执行 |
| `microsphere.sentinel.enabled` | `boolean` | `true` | 系统属性与 application 配置（`SentinelConstants.ENABLED_PROPERTY_NAME`） | Spring Boot 自动配置层的全局开关 |

`SentinelUtils.isPluginEnabled("mybatis")` 返回 `!"false".equalsIgnoreCase(value)`：只有字面量恰好为
`false` 才会禁用，并且在这一层只有 JVM 系统属性可见。在 Spring 环境中，请改为依赖自动配置条件，
或者用 `setEnabled(boolean)` / `enable()` / `disable()` 变更状态，也可以通过 `mybatis` MBean 变更。

## 用法示例

把过滤器挂到 MyBatis `Configuration` 上，与模块测试的做法一致：

```java
// adapted from SentinelMyBatisExecutorFilterTest
class MapperSentinelSetup extends AbstractMapperTest {

    private SentinelMyBatisExecutorFilter filter;

    @Override
    protected void customize(Configuration configuration) {
        this.filter = new SentinelMyBatisExecutorFilter();       // auto-installs as plugin "mybatis"
        InterceptingExecutorInterceptor interceptor =
                new InterceptingExecutorInterceptor(ofArray(this.filter));
        configuration.addInterceptor(interceptor);
    }
}
```

运行 Mapper 测试套件，并确认资源已经落到 Sentinel 上下文树中：

```java
// adapted from SentinelMyBatisExecutorFilterTest#postTest
super.testMapper();                                             // each MappedStatement id becomes a resource

Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
DefaultNode entranceNode = context.getEntranceNode();
assertFalse(entranceNode.getChildList().isEmpty());
exit();
```

在运行时绕过保护：

```java
// adapted from SentinelMyBatisExecutorFilterTest#testDisabled
this.filter.setEnabled(false);
super.testMapper();                                             // statements execute, nothing is recorded
```

检查插件身份与生成的属性名：

```java
// adapted from SentinelMyBatisConstantsTest
assertEquals("mybatis", PLUGIN_NAME);
assertEquals("microsphere_sentinel_mybatis_context", DEFAULT_CONTEXT_NAME);
assertEquals("Executor", DEFAULT_ORIGIN);
assertEquals("microsphere.sentinel.mybatis.enabled", ENABLED_PROPERTY_NAME);
assertEquals(getPluginEnabledPropertyName(PLUGIN_NAME), ENABLED_PROPERTY_NAME);

SentinelPlugin plugin = SentinelPluginRepository.INSTANCE.get("mybatis");
assertEquals(COMMON_DB_SQL, plugin.getResourceType());
assertEquals(EntryType.IN, plugin.getTrafficType());
```

以 Mapper 方法 id 为键的流控规则可以精确限制该条语句：

```java
// rule shape adapted from AbstractSentinelTemplateTest
FlowRule rule = new FlowRule();
rule.setResource("com.acme.mapper.UserMapper.selectById");      // the MappedStatement id
rule.setCount(1);
rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
rule.setLimitApp("default");
FlowRuleManager.loadRules(List.of(rule));
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— `AbstractSentinelPlugin`、`SentinelOperations`、`SentinelTemplate`、`SentinelPluginRepository`
- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) —— 插件聚合模块
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) —— 针对同一段 SQL 流量的 JDBC 层替代方案；二选一以避免重复计数
- `microsphere-alibaba-sentinel-spring-boot` —— `SentinelMyBatisAutoConfiguration`
- [分支索引](./README.md) · [仓库根目录](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-mybatis.md)
