# microsphere-alibaba-sentinel-alibaba-druid

[中文版](zh/microsphere-alibaba-sentinel-alibaba-druid.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-alibaba-druid.md](../current/microsphere-alibaba-sentinel-alibaba-druid.md).

## Overview

`microsphere-alibaba-sentinel-alibaba-druid` protects JDBC traffic flowing through an Alibaba **Druid** connection
pool. Its single runtime class, `SentinelAlibabaDruidFilter`, is a Druid `Filter` that also implements
`SentinelPlugin`: every statement executed against a proxied `DruidDataSource` opens a Sentinel context and entry
before execution and closes it afterwards, so flow, thread-concurrency and circuit-breaking rules apply per SQL
resource.

The filter extends `io.microsphere.alibaba.druid.filter.AbstractStatementFilter` (from `microsphere-alibaba-druid-core`),
which resolves the Sentinel resource name from the executed statement and calls back two protected hooks,
`beforeExecute(StatementProxy, String resourceName)` and
`afterExecute(StatementProxy, String resourceName, Object result, Throwable failure)`.

Because the filter is a `SentinelPlugin`, it installs itself into the shared `SentinelPluginRepository` under the
name `alibaba-druid` and is exposed as a JMX MBean whose enabled state is togglable at runtime. Protection is
symmetric: both hooks are gated by the same `isEnabled()` check, so disabling the filter leaves no dangling
thread-local context.

## Version Information

| Item | Value |
| --- | --- |
| Branch | `release-1.x` — docs under `docs/user-guide/1.x/` |
| Coordinates | `io.github.microsphere-projects:microsphere-alibaba-sentinel-alibaba-druid:<version>` |
| Branch version | `0.1.6-SNAPSHOT` (`${revision}`) |
| Released versions | `0.1.0` – `0.1.5` |
| Packaging | `jar` |
| POM name | `Microsphere :: Alibaba Sentinel :: Plugins :: Alibaba Druid` |
| Java | 8 |
| Base package | `io.microsphere.alibaba.sentinel.alibaba.druid` |
| Since | `1.0.0` |

## Compatibility

| Dependency | Version | Scope in this module |
| --- | --- | --- |
| Alibaba Druid (`com.alibaba:druid`) | 1.2.28, managed by `microsphere-alibaba-druid-dependencies` 0.1.20 | `optional` — provide it yourself |
| `microsphere-alibaba-druid-core` | 0.1.20 (same BOM) | `optional` — supplies `AbstractStatementFilter` |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | `optional` — provide it yourself |
| `microsphere-alibaba-sentinel-commons` | `${revision}` | `compile` — the only mandatory project dependency |
| Java | 8 | Required |
| Spring Cloud / Spring Boot | Hoxton.SR12 – 2021.0.x / 2.x | Only through `microsphere-alibaba-sentinel-spring-boot` |

Stay on this `0.1.x` line while the application runs Java 8 with Spring Boot 2.x (Druid itself is 1.2.28 on both
lines, managed by `microsphere-alibaba-druid-dependencies`); move to the current `0.2.x` line as soon as the
application runs Java 17 with Spring Boot 3.x. Never mix a `0.1.x` plugin with `0.2.x` commons (or the reverse):
the framework baseline and the `AbstractStatementFilter` contract both differ between the lines.

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

In Spring Boot, prefer adding `microsphere-alibaba-sentinel-spring-boot`: `SentinelAlibabaDruidAutoConfiguration`
contributes a `SentinelAlibabaDruidFilter` bean after `SentinelAutoConfiguration` and Microsphere's
`AlibabaDruidAutoConfiguration`, guarded by `@ConditionalOnAlibabaDruidAvailable` and
`@ConditionalOnProperty(name = ENABLED_PROPERTY_NAME, matchIfMissing = true)`.

## API Reference

```java
public class SentinelAlibabaDruidFilter extends AbstractStatementFilter implements SentinelPlugin

public SentinelAlibabaDruidFilter();                                        // default context name and origin
public SentinelAlibabaDruidFilter(String contextName, String origin);
```

The second constructor builds the plugin descriptor with
`new SimpleSentinelPlugin(PLUGIN_NAME, contextName, origin, COMMON_DB_SQL, IN, false)` — resource type
`ResourceTypeConstants.COMMON_DB_SQL`, traffic type `EntryType.IN`, `autoInstalled = false` — creates a
`SentinelTemplate` from the same pair, then calls `SentinelPlugin.install(this)`. Installing the filter itself,
rather than the delegate, is what publishes the `alibaba-druid` MBean; `autoInstalled = false` keeps the delegate
from registering a duplicate entry. `isAutoInstalled()`, `isEnabled()`, `setEnabled(boolean)`, `getName()`,
`getContextName()`, `getOrigin()`, `getResourceType()` and `getTrafficType()` all delegate to that internal plugin.

The two hooks, verbatim from the implementation:

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

Consequences: `beforeExecute` declares `throws Throwable`, so a `BlockException` from `begin` aborts the statement —
that is the intended limiting behavior. `afterExecute` runs whatever the outcome, and a non-null `failure` makes
`SentinelTemplate.end` call `Tracer.traceEntry(failure, entry)` before `entry.exit()` and `ContextUtil.exit()`, so
exception-ratio rules count the SQL as a failure. The context is bound with `withinContext()` and consumed with
`doInContext(consumer, true)`, which removes it from the thread.

`SentinelAlibabaDruidConstants` publishes the wiring values:

| Constant | Value |
| --- | --- |
| `PLUGIN_NAME` | `"alibaba-druid"` |
| `DEFAULT_CONTEXT_NAME` | `"microsphere_sentinel_alibaba_druid_context"` |
| `DEFAULT_ORIGIN` | `"Filter"` |
| `ENABLED_PROPERTY_NAME` | `"microsphere.sentinel.alibaba-druid.enabled"` |

Discovery: the JAR ships `META-INF/services/com.alibaba.druid.filter.Filter` naming
`io.microsphere.alibaba.sentinel.alibaba.druid.SentinelAlibabaDruidFilter`, so Druid's filter service loading can
instantiate it automatically; explicit registration or the Spring Boot bean are the deterministic alternatives.

## Configuration Properties

| Property | Type | Default | Source read | Effect |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.alibaba-druid.enabled` | `boolean` | `true` | Java system property (`@ConfigurationProperty(source = SYSTEM_PROPERTIES_SOURCE)`) | Seeds `isEnabled()`; `false` turns both hooks into no-ops |
| `microsphere.sentinel.enabled` | `boolean` | `true` | System property and application configuration (`SentinelConstants.ENABLED_PROPERTY_NAME`) | Global switch for the Spring Boot auto-configuration layer |

`SentinelUtils.isPluginEnabled("alibaba-druid")` returns `!"false".equalsIgnoreCase(value)`, so only the exact
literal `false` disables, and only the JVM system property is visible at this layer — an `application.properties`
entry influences the filter through the auto-configuration condition, not through `isEnabled()`. The value is read
once, when the filter is constructed; afterwards use `setEnabled(boolean)` or the MBean.

## Usage Examples

Registering the filter on a `DruidDataSource`, as the module test does:

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

Toggling protection at runtime without rebuilding the pool:

```java
// adapted from SentinelAlibabaDruidFilterTest#testEnable / #testDisable
void setEnable(boolean enabled) {
    for (Filter proxyFilter : getDruidDataSource().getProxyFilters()) {
        if (proxyFilter instanceof SentinelAlibabaDruidFilter) {
            SentinelAlibabaDruidFilter filter = (SentinelAlibabaDruidFilter) proxyFilter;
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

Verifying the wiring constants and the generated property name:

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

Limiting a specific statement, using the plugin's context name and origin so rules written for `default` origins
match:

```java
// rule shape adapted from AbstractSentinelTemplateTest
FlowRule rule = new FlowRule();
rule.setResource(resourceName);                     // the Druid statement resource name from AbstractStatementFilter
rule.setCount(1);
rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
rule.setLimitApp("default");
FlowRuleManager.loadRules(Collections.singletonList(rule));
```

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) — `SentinelPlugin`, `SentinelOperations`, `SentinelContext`, `SentinelTemplate`
- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) — the plugins aggregator
- [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) — Mapper-level protection over the same SQL; enable only one layer to avoid double counting
- `microsphere-alibaba-sentinel-spring-boot` — `SentinelAlibabaDruidAutoConfiguration`
- [Branch index](./README.md) · [Repository root](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-alibaba-druid.md)
