# microsphere-alibaba-sentinel-mybatis

[中文版](zh/microsphere-alibaba-sentinel-mybatis.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-mybatis.md](../current/microsphere-alibaba-sentinel-mybatis.md).

## Overview

`microsphere-alibaba-sentinel-mybatis` protects MyBatis statement execution with Alibaba Sentinel. Its runtime
class, `SentinelMyBatisExecutorFilter`, is an `io.microsphere.mybatis.executor.ExecutorFilter` that extends
`AbstractSentinelPlugin`, so it is simultaneously a MyBatis executor interception rule and a Sentinel plugin with a
name, a context, an origin, and a runtime-togglable enabled flag.

Every `update` and `query` routed through the intercepting executor is wrapped in a Sentinel entry whose resource
name is the `MappedStatement` id — the fully qualified mapper method, for example
`com.acme.mapper.UserMapper.selectById`. The mapper method therefore becomes the unit of flow control and circuit
breaking, independent of SQL text and of the JDBC driver below it.

The filter registers through Microsphere's MyBatis extension (`ExecutorFilter` SPI plus
`InterceptingExecutorInterceptor`) rather than through a MyBatis `Interceptor` of its own, so it composes with
other executor filters without replacing MyBatis internals.

## Version Information

| Item | Value |
| --- | --- |
| Branch | `release-1.x` — docs under `docs/user-guide/1.x/` |
| Coordinates | `io.github.microsphere-projects:microsphere-alibaba-sentinel-mybatis:<version>` |
| Branch version | `0.1.6-SNAPSHOT` (`${revision}`) |
| Released versions | `0.1.0` – `0.1.5` |
| Packaging | `jar` |
| POM name | `Microsphere :: Alibaba Sentinel :: Plugins :: MyBatis` |
| Java | 8 |
| Base packages | `io.microsphere.alibaba.sentinel.mybatis`, `io.microsphere.alibaba.sentinel.mybatis.executor` |
| Since | `1.0.0` |

## Compatibility

| Dependency | Version | Scope in this module |
| --- | --- | --- |
| MyBatis (`org.mybatis:mybatis`) | 3.5.19, managed by `microsphere-mybatis-dependencies` 0.1.15 | `optional` — provide it yourself |
| `microsphere-mybatis-core` | 0.1.15 (same BOM) | `optional` — supplies `ExecutorFilter`, `ExecutorFilterChain`, `InterceptingExecutorInterceptor` |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | `optional` — provide it yourself |
| `microsphere-alibaba-sentinel-commons` | `${revision}` | `compile` — the only mandatory project dependency |
| Java | 8 | Required |
| Spring Cloud / Spring Boot | Hoxton.SR12 – 2021.0.x / 2.x | Only through `microsphere-alibaba-sentinel-spring-boot` |

This `0.1.x` line requires Java 8 and runs on Spring Cloud Hoxton.SR12 through 2021.0.x with Spring Boot 2.x.
MyBatis itself is 3.5.19 on both lines (managed by `microsphere-mybatis-dependencies` 0.1.15), so the difference is
the framework baseline, not the MyBatis build. Keep an application wholly on one line, since the `ExecutorFilter`
signatures are resolved against either the Spring 5 or the Spring 6 generation.

## Installation

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

With Spring Boot, add `microsphere-alibaba-sentinel-spring-boot`: `SentinelMyBatisAutoConfiguration` contributes a
`SentinelMyBatisExecutorFilter` bean inside a `@EnableMyBatisExtension` configuration, ordered after
`SentinelAutoConfiguration`, `MybatisAutoConfiguration`, `MybatisPlusAutoConfiguration` and Microsphere's
`MyBatisAutoConfiguration`, and only when `@ConditionalOnMyBatisAvailable` and
`@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)` hold.

## API Reference

```java
public class SentinelMyBatisExecutorFilter extends AbstractSentinelPlugin implements ExecutorFilter

public SentinelMyBatisExecutorFilter();                                       // default context name and origin
public SentinelMyBatisExecutorFilter(String contextName, String origin);
```

The second constructor calls `super(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL)`. `AbstractSentinelPlugin`
then defaults traffic type to `EntryType.IN` and `autoInstalled` to `true`, so merely constructing the filter
installs it into the shared `SentinelPluginRepository` as `mybatis` — and publishes the MBean
`io.microsphere.sentinel:type=SentinelPlugin,name=mybatis`. A `SentinelTemplate` is created from the same resource
type and traffic type.

Overridden executor methods, each wrapping the default interface implementation:

```java
public int update(MappedStatement ms, Object parameter, ExecutorFilterChain chain) throws SQLException;
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                         CacheKey cacheKey, BoundSql boundSql, ExecutorFilterChain chain) throws SQLException;
public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler,
                         ExecutorFilterChain chain) throws SQLException;
```

The wrapping is centralized:

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

- When enabled, the mapper call runs inside `SentinelOperations.call`, which enters the context, opens the entry and
  in `finally` ends it. Any throwable from the callback is rethrown; a failure that is not a block exception is
  first reported with `Tracer.trace(e)` and logged at `error` level, so exception-ratio rules react to real mapper
  failures while `BlockException` is not counted as one.
- The `SQLException.class` argument only narrows the checked-exception declaration, letting these methods keep the
  `throws SQLException` contract of `ExecutorFilter`.
- When disabled, the callback runs unwrapped: no entry is created, so the filter records no resource and no metrics.
- Override `getSentinelResourceName(MappedStatement)` to coarsen resources, for example by mapper namespace instead
  of by statement id.

`SentinelMyBatisConstants` publishes the wiring values:

| Constant | Value |
| --- | --- |
| `PLUGIN_NAME` | `"mybatis"` |
| `DEFAULT_CONTEXT_NAME` | `"microsphere_sentinel_mybatis_context"` |
| `DEFAULT_ORIGIN` | `"Executor"` |
| `ENABLED_PROPERTY_NAME` | `"microsphere.sentinel.mybatis.enabled"` |

Discovery: `META-INF/services/io.microsphere.mybatis.executor.ExecutorFilter` names
`io.microsphere.alibaba.sentinel.mybatis.executor.SentinelMyBatisExecutorFilter`, so Microsphere's MyBatis
extension collects the filter automatically once the JAR is on the classpath.

## Configuration Properties

| Property | Type | Default | Source read | Effect |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.mybatis.enabled` | `boolean` | `true` | Java system property (`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`) | Seeds `isEnabled()`; `false` runs mappers without Sentinel entries |
| `microsphere.sentinel.enabled` | `boolean` | `true` | System property and application configuration (`SentinelConstants.ENABLED_PROPERTY_NAME`) | Global switch for the Spring Boot auto-configuration layer |

`SentinelUtils.isPluginEnabled("mybatis")` returns `!"false".equalsIgnoreCase(value)`: only the exact literal
`false` disables, and only the JVM system property is visible at this layer. In a Spring environment rely on the
auto-configuration condition instead, or change state with `setEnabled(boolean)` / `enable()` / `disable()`, or
through the `mybatis` MBean.

## Usage Examples

Attaching the filter to a MyBatis `Configuration`, as the module test does:

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

Running the mapper suite and confirming the resources landed in the Sentinel context tree:

```java
// adapted from SentinelMyBatisExecutorFilterTest#postTest
super.testMapper();                                             // each MappedStatement id becomes a resource

Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
DefaultNode entranceNode = context.getEntranceNode();
assertFalse(entranceNode.getChildList().isEmpty());
exit();
```

Bypassing protection at runtime:

```java
// adapted from SentinelMyBatisExecutorFilterTest#testDisabled
this.filter.setEnabled(false);
super.testMapper();                                             // statements execute, nothing is recorded
```

Checking plugin identity and the generated property name:

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

A flow rule keyed on a mapper method id limits exactly that statement:

```java
// rule shape adapted from AbstractSentinelTemplateTest
FlowRule rule = new FlowRule();
rule.setResource("com.acme.mapper.UserMapper.selectById");      // the MappedStatement id
rule.setCount(1);
rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
rule.setLimitApp("default");
FlowRuleManager.loadRules(Collections.singletonList(rule));
```

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) — `AbstractSentinelPlugin`, `SentinelOperations`, `SentinelTemplate`, `SentinelPluginRepository`
- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) — the plugins aggregator
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) — JDBC-level alternative for the same SQL traffic; pick one to avoid double counting
- `microsphere-alibaba-sentinel-spring-boot` — `SentinelMyBatisAutoConfiguration`
- [Branch index](./README.md) · [Repository root](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-mybatis.md)
