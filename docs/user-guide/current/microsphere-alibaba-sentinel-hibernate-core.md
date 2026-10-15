# microsphere-alibaba-sentinel-hibernate-core

[中文版](zh/microsphere-alibaba-sentinel-hibernate-core.md)

## Overview

`microsphere-alibaba-sentinel-hibernate-core` protects Hibernate ORM entity operations with Alibaba Sentinel. It is a
plugin module that implements the `EntityCallback` SPI of `microsphere-hibernate-core`
(`io.microsphere.hibernate.entity.EntityCallback`) and registers itself through the Java Service Loader file
`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`. Hibernate event listeners installed by
`microsphere-hibernate` (`EntittyCallbackIntegrator`, itself registered through
`META-INF/services/org.hibernate.integrator.spi.Integrator`) then invoke the callback, so every `INSERT`, `UPDATE`,
`LOAD` and `DELETE` on a managed entity opens and closes a Sentinel entry without any application code change.

Key facts verified from the source:

- Entry point class: `io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`.
- Plugin name (`SentinelHibernateConstants.PLUGIN_NAME`): `hibernate`.
- Sentinel context name: `microsphere_sentinel_hibernate_context` (`SentinelHibernateConstants.DEFAULT_CONTEXT_NAME`,
  matching the `microsphere_sentinel_{}_context` pattern of `SentinelUtils.getDefaultContextName(String)`).
- Origin: `SessionFactory` (`SentinelHibernateConstants.DEFAULT_ORIGIN`).
- Resource type: `ResourceTypeConstants.COMMON_DB_SQL` (`4`); traffic type: `EntryType.IN`.
- Resource name pattern: `Entity:<ACTION>:<fully qualified entity class name>`, produced by
  `getSentinelResourceName(Object, String)`. Example: `Entity:INSERT:com.acme.domain.User`.
- Two-phase instrumentation: `onPreXxx` calls `SentinelOperations.begin(...)` and stores the returned
  `SentinelContext` in a `ThreadLocal` (`SentinelContext.withinContext()`); `onPostXxx` calls
  `SentinelContext.doInContext(consumer, true)` to end and remove it.
- Unlike the Redis and Spring Web plugins, this class does **not** extend `AbstractSentinelPlugin`; it implements
  `SentinelPlugin` and delegates the plugin metadata to a `SimpleSentinelPlugin` created with `autoInstalled = false`,
  then installs itself explicitly with `SentinelPlugin.install(this)`. Installation registers the plugin in the
  `SentinelPluginRepository` and, because the repository service is `JMXSentinelPluginRepository`, exposes it as the
  MBean `io.microsphere.sentinel:type=SentinelPlugin,name=hibernate`.

## Version Information

| Item | Value |
|------|-------|
| Documented branch | `release` (this file lives in `docs/user-guide/current`) |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-hibernate-core` |
| Branch version | `0.2.6-SNAPSHOT` |
| Released versions | `0.2.0` - `0.2.5` |
| Java version | 17 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `microsphere-hibernate` BOM | 0.2.10 |

## Compatibility

Verified against the module POM (all third-party dependencies are `optional`) and the imports in
`SentinelHibernateEntityCallback`:

| Technology | Status | Evidence |
|------------|--------|----------|
| Hibernate ORM `org.hibernate.orm:hibernate-core` | 6.x and 7.x (Jakarta namespace) | imports `org.hibernate.type.Type`; test `META-INF/persistence.xml` uses `jakarta.persistence.jdbc.*` properties |
| `microsphere-hibernate-core` (0.2.10) | required at runtime for the `EntityCallback` SPI | declared `optional` in the module POM, provided by the application |
| `sentinel-core` | required at runtime | declared `optional` in the module POM |
| Spring Boot | works, but this module is **not** auto-configured | `microsphere-alibaba-sentinel-spring-boot` auto-configures only Druid, MyBatis, Redis and Spring Web |

Build note: the `microsphere-hibernate` parent BOM activates the `hibernate-7.3` profile (Hibernate `7.3.4.Final`) by
default and keeps profiles for `6.2.52.Final` up to `6.6.50.Final`; the `EntityCallback` methods used here are unchanged.

## Installation

Add the Microsphere Alibaba Sentinel BOM and the module:

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

No further registration is needed: the `META-INF/services/io.microsphere.hibernate.entity.EntityCallback` file inside
the jar contains a single line, `io.microsphere.alibaba.sentinel.hibernate.entity.SentinelHibernateEntityCallback`.

## API Reference

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

Behaviour details that matter when writing rules:

- Every `onPreXxx` method is a no-op when `isEnabled()` is `false`; `onPostXxx` likewise, so a mid-flight `setEnabled(false)`
  leaves the `ThreadLocal` context untouched rather than throwing.
- `getSentinelResourceName` uses `entity.getClass().getName()`, i.e. the concrete (possibly Hibernate-proxied) entity
  class; override it if you need the unmapped name for proxies.
- `begin(Object, String)` returns `Optional.empty()` when disabled and otherwise wraps the checked
  `SentinelOperations.begin(...)` call, so a Sentinel `BlockException` propagates from the Hibernate event to the caller.

## Configuration Properties

| Property | Default | Where it is read | Effect |
|----------|---------|------------------|--------|
| `microsphere.sentinel.enabled` | `true` | Spring Boot module (`@ConditionalOnSentinelEnabled`) | Gates auto-configuration of other plugins only; this module never reads it |
| `microsphere.sentinel.hibernate.enabled` | `true` | `SentinelUtils.isPluginEnabled(String)` via `System.getProperty` | Read once when `SentinelHibernateEntityCallback` is constructed; the value is evaluated as `!"false".equalsIgnoreCase(value)`, so anything except `false` keeps interception on |
| `SentinelConstants.DEFAULT_CONTEXT_NAME` | `microsphere_sentinel_default_context` | `SentinelTemplate.begin` | Fallback context name when a blank context name is supplied; not used by the default constructor |

Because the enabled flag is captured at construction and cached by the `SimpleSentinelPlugin` delegate,
`-Dmicrosphere.sentinel.hibernate.enabled=false` must be present before the callback is instantiated. At runtime,
toggle the plugin through `setEnabled(boolean)` on the instance or on the registered `SentinelPlugin` MBean.

## Usage Examples

### Automatic registration with a JPA/Hibernate unit

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

### Disabling the plugin

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

### Custom context name and origin

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

### Runtime toggle and resource naming

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

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `SentinelPlugin`,
  `SentinelOperations`, `SentinelTemplate`, `SentinelContext` and `SentinelUtils`.
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) - auto-configuration for
  other plugins; note that this Hibernate plugin is registered by Java SPI only and is **not** auto-configured.
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) and
  [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) - sibling JDBC-layer plugins.
- [User guide index](./README.md) - all modules of the current branch.
- [Project root](../../../README.md) - repository overview.

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-hibernate-core.md)
