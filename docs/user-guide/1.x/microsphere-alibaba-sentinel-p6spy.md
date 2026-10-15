# microsphere-alibaba-sentinel-p6spy

[中文版](zh/microsphere-alibaba-sentinel-p6spy.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-p6spy.md](../current/microsphere-alibaba-sentinel-p6spy.md).

## Overview

`microsphere-alibaba-sentinel-p6spy` turns every SQL statement that passes through [P6Spy](https://github.com/p6spy/p6spy)
into a Sentinel resource. The module ships `io.microsphere.alibaba.sentinel.p6spy.SentinelJdbcEventListener`, which
extends P6Spy's `com.p6spy.engine.event.SimpleJdbcEventListener` and is registered through the Java Service Loader file
`META-INF/services/com.p6spy.engine.event.JdbcEventListener`. P6Spy discovers and instantiates the listener on its own,
so no Spring, no configuration class and no application code is required - only a `jdbc:p6spy:` URL.

Key facts verified from the source:

- Plugin name (`SentinelP6SpyConstants.PLUGIN_NAME`): `p6spy`.
- Context name: `microsphere_sentinel_p6spy_context` (`SentinelP6SpyConstants.DEFAULT_CONTEXT_NAME`, the
  `microsphere_sentinel_{}_context` pattern applied by `SentinelUtils.getDefaultContextName(String)`).
- Origin: `Statement` (`SentinelP6SpyConstants.DEFAULT_ORIGIN`).
- Resource type: `ResourceTypeConstants.COMMON_DB_SQL` (`4`); traffic type: `EntryType.IN`. Both are returned by
  hard-coded overrides in `getResourceType()` and `getTrafficType()`.
- Resource name: the SQL string reported by P6Spy, `statementInformation.getSql()` (see `getResourceName`).
- Only prepared statements are tracked: `isEligibleStatement(StatementInformation)` returns
  `statementInformation instanceof PreparedStatementInformation`, so `Statement.execute("...")` calls are ignored.
- Two-phase entry: `onBeforeAnyExecute` opens the entry and stores the `SentinelContext` in a `ThreadLocal`
  (`SentinelContext.withinContext()`); `onAfterAnyExecute` retrieves and removes it
  (`SentinelContext.doInContext(consumer, true)`), records the `SQLException` with `context.setFailure(e)` and lets
  `SentinelTemplate.end(...)` report it via `Tracer.traceEntry` before exiting.
- Like the Hibernate plugin, the class does **not** extend `AbstractSentinelPlugin`; it implements `SentinelPlugin` and
  delegates metadata to a `SimpleSentinelPlugin` built with `autoInstalled = false`, calling `SentinelPlugin.install(this)`
  explicitly. Installation registers the MBean `io.microsphere.sentinel:type=SentinelPlugin,name=p6spy`.

## Version Information

| Item | Value |
|------|-------|
| Documented branch | `release-1.x` (this file lives in `docs/user-guide/1.x`) |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-p6spy` |
| Branch version | `0.1.6-SNAPSHOT` |
| Released versions | `0.1.0` - `0.1.5` |
| Java version | 8 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `p6spy:p6spy` | 3.9.1 (3.x line, declared `optional`) |

## Compatibility

| Technology | Status | Evidence |
|------------|--------|----------|
| P6Spy 3.x (`p6spy:p6spy:3.9.1`) | required | `com.p6spy.engine.event.SimpleJdbcEventListener`, `PreparedStatementInformation`, `StatementInformation` imports; the published module POM resolves `p6spy` to `3.9.1` |
| Any JDBC 4.x driver + `P6SpyDriver` | required | tests use `com.p6spy.engine.spy.P6SpyDriver` with `jdbc:p6spy:h2:mem:test_mem` |
| Connection pools (Druid, Hikari, ...) | neutral | the pool sees only the P6Spy driver; tests wrap the Druid `DruidDataSource` |
| `sentinel-core` | required at runtime | declared `optional` in the module POM |
| Spring Boot | works, but **not auto-configured** | `microsphere-alibaba-sentinel-spring-boot` has no `SentinelP6SpyAutoConfiguration`; the `spring.factories` file lists only Druid, MyBatis, Redis and Spring Web |

P6Spy 3.9.1 is the newest 3.x release used by the Microsphere parent chain; the listener API used here
(`onBeforeAnyExecute` / `onAfterAnyExecute`) is stable across the 3.x line.

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

Then point the datasource at the P6Spy driver, keep `spy.properties` on the classpath, and the listener is loaded
automatically from the plugin jar's service file:

```properties
# adapted from src/test/resources/spy.properties
driverlist=org.h2.Driver
```

## API Reference

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

Notes:

- `getResourceName` is `protected`, so overriding it is the supported way to change granularity, for example returning
  `statementInformation.getSqlWithValues()` or a normalised digest of the SQL.
- `begin`/`end` of the underlying `SentinelOperations` (`SentinelTemplate`) may throw `BlockException`; it propagates to
  the JDBC caller as a runtime failure of the statement execution.
- When the plugin is disabled, both lifecycle methods return immediately and no entry is opened, so no Sentinel metrics
  are produced for that datasource.

## Configuration Properties

| Property | Default | Where it is read | Effect |
|----------|---------|------------------|--------|
| `microsphere.sentinel.enabled` | `true` | Spring Boot module (`@ConditionalOnSentinelEnabled`) | Auto-configuration gate only; this module never reads it |
| `microsphere.sentinel.p6spy.enabled` | `true` | `SentinelUtils.isPluginEnabled(String)` via `System.getProperty` | Captured when the listener instance is created by P6Spy; evaluated as `!"false".equalsIgnoreCase(value)` |
| `SentinelConstants.DEFAULT_CONTEXT_NAME` | `microsphere_sentinel_default_context` | `SentinelTemplate.begin` | Fallback when a blank context name is passed to the two-argument constructor |

Set the plugin property before startup, for example `-Dmicrosphere.sentinel.p6spy.enabled=false`, or toggle the live
instance through `setEnabled(boolean)` (also reachable from the `SentinelPlugin` JMX MBean).

## Usage Examples

### Registering the datasource with P6Spy

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

### Verifying the defaults of the listener

```java
// adapted from SentinelJdbcEventListenerTest#testDefaults
SentinelJdbcEventListener listener = new SentinelJdbcEventListener();
assertTrue(listener.isEnabled());
assertEquals(PLUGIN_NAME, listener.getName());                    // "p6spy"
assertEquals(DEFAULT_CONTEXT_NAME, listener.getContextName());    // microsphere_sentinel_p6spy_context
assertEquals(DEFAULT_ORIGIN, listener.getOrigin());               // "Statement"
```

### Disabling interception at runtime

```java
// adapted from SentinelJdbcEventListenerTest#testDisabled
SentinelJdbcEventListener listener = new SentinelJdbcEventListener();
listener.setEnabled(false);
listener.onBeforeAnyExecute(null);                 // no-op: isEnabled() is false
listener.onAfterAnyExecute(null, 0L, null);        // no-op as well
assertFalse(listener.isEnabled());
```

### Asserting that a Sentinel node was created

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

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `SentinelPlugin`,
  `SentinelOperations`, `SentinelTemplate`, `SentinelContext`, `SentinelUtils`.
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) - auto-configuration for
  Druid, MyBatis, Redis and Spring Web; P6Spy is intentionally not auto-configured because registration happens via
  P6Spy's own service loader.
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) - Druid `Filter` based
  SQL protection, an alternative to P6Spy at the connection-pool layer.
- [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md) - entity-level SQL
  protection with the same `COMMON_DB_SQL` resource type.
- [User guide index](./README.md) - all modules of this branch.
- [Project root](../../../README.md) - repository overview.

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-p6spy.md)
