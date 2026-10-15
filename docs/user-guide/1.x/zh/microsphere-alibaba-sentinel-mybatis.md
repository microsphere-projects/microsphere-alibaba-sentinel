# microsphere-alibaba-sentinel-mybatis

[English](../microsphere-alibaba-sentinel-mybatis.md)

> **已归档的 `1.x` 分支线。** 本页文档描述 `0.1.x` 制品（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支线一致，仅基线版本不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。当前分支线对应的文档是 [../current/zh/microsphere-alibaba-sentinel-mybatis.md](../../current/zh/microsphere-alibaba-sentinel-mybatis.md)。

## 概述

`microsphere-alibaba-sentinel-mybatis` 使用 Alibaba Sentinel 保护 MyBatis 的语句执行。它的运行时类
`SentinelMyBatisExecutorFilter` 是一个 `io.microsphere.mybatis.executor.ExecutorFilter`，继承
`AbstractSentinelPlugin`，因此它同时既是 MyBatis 执行器拦截规则，也是一个带有名称、上下文、来源以及运行时可切换
启用标志的 Sentinel 插件。

所有经由被拦截执行器路由的 `update` 与 `query` 都会被包装进一个 Sentinel entry，其资源名就是 `MappedStatement`
的 id —— 即全限定的 Mapper 方法名，例如
`com.acme.mapper.UserMapper.selectById`。于是 Mapper 方法本身成为流控与熔断的最小单位，与 SQL 文本以及下层
JDBC 驱动都无关。

该过滤器通过 Microsphere 的 MyBatis 扩展（`ExecutorFilter` SPI 加上
`InterceptingExecutorInterceptor`）注册，而不是自己实现一个 MyBatis `Interceptor`，因此它可以与其他执行器过滤器
叠加组合，而不会替换 MyBatis 的内部机制。

## 版本信息

| 项目 | 值 |
| --- | --- |
| 分支 | `release-1.x` —— 文档位于 `docs/user-guide/1.x/` |
| 坐标 | `io.github.microsphere-projects:microsphere-alibaba-sentinel-mybatis:<version>` |
| 分支版本 | `0.1.6-SNAPSHOT`（`${revision}`） |
| 已发布版本 | `0.1.0` – `0.1.5` |
| 打包方式 | `jar` |
| POM 名称 | `Microsphere :: Alibaba Sentinel :: Plugins :: MyBatis` |
| Java | 8 |
| 基础包 | `io.microsphere.alibaba.sentinel.mybatis`、`io.microsphere.alibaba.sentinel.mybatis.executor` |
| 起始版本 | `1.0.0` |

## 兼容性

| 依赖 | 版本 | 在本模块中的作用域 |
| --- | --- | --- |
| MyBatis（`org.mybatis:mybatis`） | 3.5.19，由 `microsphere-mybatis-dependencies` 0.1.15 管理 | `optional` —— 需自行提供 |
| `microsphere-mybatis-core` | 0.1.15（同一 BOM） | `optional` —— 提供 `ExecutorFilter`、`ExecutorFilterChain`、`InterceptingExecutorInterceptor` |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | `optional` —— 需自行提供 |
| `microsphere-alibaba-sentinel-commons` | `${revision}` | `compile` —— 唯一必需的项目内依赖 |
| Java | 8 | 必需 |
| Spring Cloud / Spring Boot | Hoxton.SR12 – 2021.0.x / 2.x | 仅通过 `microsphere-alibaba-sentinel-spring-boot` 涉及 |

这条 `0.1.x` 分支线要求 Java 8，运行在 Spring Cloud Hoxton.SR12 到 2021.0.x 与 Spring Boot 2.x 之上。
MyBatis 本身在两条线上都是 3.5.19（由 `microsphere-mybatis-dependencies` 0.1.15 管理），所以差异在于框架基线，
而不是 MyBatis 的构建版本。请让一个应用完整地留在同一条线上，因为 `ExecutorFilter` 的签名会分别针对
Spring 5 或 Spring 6 那一代来解析。

## 安装

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

配合 Spring Boot 时，请加入 `microsphere-alibaba-sentinel-spring-boot`：`SentinelMyBatisAutoConfiguration`
会在一个 `@EnableMyBatisExtension` 配置中贡献 `SentinelMyBatisExecutorFilter` bean，排在
`SentinelAutoConfiguration`、`MybatisAutoConfiguration`、`MybatisPlusAutoConfiguration` 以及 Microsphere 的
`MyBatisAutoConfiguration` 之后，并且只有在 `@ConditionalOnMyBatisAvailable` 与
`@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)` 成立时才会生效。

## API 参考

```java
public class SentinelMyBatisExecutorFilter extends AbstractSentinelPlugin implements ExecutorFilter

public SentinelMyBatisExecutorFilter();                                       // default context name and origin
public SentinelMyBatisExecutorFilter(String contextName, String origin);
```

第二个构造器调用 `super(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL)`。随后 `AbstractSentinelPlugin`
把流量类型默认为 `EntryType.IN`、把 `autoInstalled` 默认为 `true`，因此仅仅构造出这个过滤器就会以 `mybatis`
之名把它安装进共享的 `SentinelPluginRepository` —— 并发布 MBean
`io.microsphere.sentinel:type=SentinelPlugin,name=mybatis`。同时会用相同的资源类型与流量类型创建一个
`SentinelTemplate`。

被覆盖的执行器方法，每一个都包装了接口的默认实现：

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

- 启用时，Mapper 调用运行在 `SentinelOperations.call` 内部，它会进入上下文、打开 entry，并在 `finally` 中结束。
  回调抛出的任何 throwable 都会被重新抛出；非限流类失败会先用 `Tracer.trace(e)` 上报并以 `error` 级别记录日志，
  于是异常比例规则能对真实的 Mapper 失败作出反应，而 `BlockException` 不会被计入失败。
- `SQLException.class` 参数只是收窄受检异常的声明，让这些方法保持 `ExecutorFilter` 的
  `throws SQLException` 契约。
- 禁用时，回调不被包装地直接执行：不创建 entry，因此过滤器不会记录任何资源，也不产生指标。
- 覆盖 `getSentinelResourceName(MappedStatement)` 可以让资源粒度变粗，例如按 Mapper 命名空间而不是按
  语句 id 归并。

`SentinelMyBatisConstants` 对外发布装配取值：

| 常量 | 值 |
| --- | --- |
| `PLUGIN_NAME` | `"mybatis"` |
| `DEFAULT_CONTEXT_NAME` | `"microsphere_sentinel_mybatis_context"` |
| `DEFAULT_ORIGIN` | `"Executor"` |
| `ENABLED_PROPERTY_NAME` | `"microsphere.sentinel.mybatis.enabled"` |

发现机制：`META-INF/services/io.microsphere.mybatis.executor.ExecutorFilter` 中列有
`io.microsphere.alibaba.sentinel.mybatis.executor.SentinelMyBatisExecutorFilter`，因此只要 JAR 进入 classpath，
Microsphere 的 MyBatis 扩展就会自动收集该过滤器。

## 配置属性

| 属性 | 类型 | 默认值 | 读取的来源 | 效果 |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.mybatis.enabled` | `boolean` | `true` | Java 系统属性（`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`） | 给 `isEnabled()` 播种；为 `false` 时 Mapper 在无 Sentinel entry 的情况下执行 |
| `microsphere.sentinel.enabled` | `boolean` | `true` | 系统属性与应用配置（`SentinelConstants.ENABLED_PROPERTY_NAME`） | Spring Boot 自动配置层的全局开关 |

`SentinelUtils.isPluginEnabled("mybatis")` 返回 `!"false".equalsIgnoreCase(value)`：只有精确的字面量
`false` 才会禁用，并且在这一层只有 JVM 系统属性可见。在 Spring 环境中请改为依赖自动配置条件，或者用
`setEnabled(boolean)` / `enable()` / `disable()` 变更状态，又或者通过 `mybatis` MBean 操作。

## 使用示例

像模块测试那样把过滤器挂到 MyBatis `Configuration` 上：

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

运行 Mapper 测试套件，并确认资源已进入 Sentinel 的上下文树：

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

校验插件身份与生成的属性名：

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

以 Mapper 方法 id 为键的流控规则可以精确限制该语句：

```java
// rule shape adapted from AbstractSentinelTemplateTest
FlowRule rule = new FlowRule();
rule.setResource("com.acme.mapper.UserMapper.selectById");      // the MappedStatement id
rule.setCount(1);
rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
rule.setLimitApp("default");
FlowRuleManager.loadRules(Collections.singletonList(rule));
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— `AbstractSentinelPlugin`、`SentinelOperations`、`SentinelTemplate`、`SentinelPluginRepository`
- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) —— 插件聚合器
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) —— 针对同一批 SQL 流量的 JDBC 层替代方案；二选一以避免重复计数
- `microsphere-alibaba-sentinel-spring-boot` —— `SentinelMyBatisAutoConfiguration`
- [分支索引](./README.md) · [仓库根目录](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-mybatis.md)
