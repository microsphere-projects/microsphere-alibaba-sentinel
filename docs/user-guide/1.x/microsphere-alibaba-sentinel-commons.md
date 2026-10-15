# microsphere-alibaba-sentinel-commons

[中文版](zh/microsphere-alibaba-sentinel-commons.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-commons.md](../current/microsphere-alibaba-sentinel-commons.md).

## Overview

`microsphere-alibaba-sentinel-commons` is the framework-neutral core of the Microsphere Alibaba Sentinel integration. It replaces the low-level `SphU.entry(...)` / `ContextUtil.enter(...)` protocol with a callback model, and defines the plugin abstraction that every technology-specific plugin (Druid, MyBatis, Hibernate, P6Spy, Redis, Spring Web) implements.

- **Execution wrapping** — `SentinelOperations`, implemented by `SentinelTemplate`, runs business code inside a Sentinel context and entry, records result and failure, traces exceptions, and always releases the entry.
- **Plugin model** — `SentinelPlugin` describes a source of traffic (name, context name, origin, resource type, traffic type, enabled flag) and installs itself into a `SentinelPluginRepository`, a `java.util.ServiceLoader` SPI provided by `JMXSentinelPluginRepository`, so every installed plugin is also a JMX MBean whose enabled state can be flipped at runtime.
- **Observability hooks** — `DefaultNodeEntryCallback`, `ProcessorSlotCallbackUtils`, `SentinelMetricsRepository` and `SentinelNodeEventPublisher` attach to Sentinel's statistic-slot callback registry to expose node events and metric files.
- **No forced dependencies** — `AbstractSentinelPlugin` is the base of every plugin and `JMXSentinelPluginRepository` delegates to `SimpleSentinelPluginRepository`; `sentinel-core`, `sentinel-transport-simple-http`, `microsphere-java-core` and `microsphere-annotation-processor` are all declared `optional`, so the module adds no transitive weight downstream.

## Version Information

| Item | Value |
| --- | --- |
| Branch | `release-1.x` — docs under `docs/user-guide/1.x/` |
| Coordinates | `io.github.microsphere-projects:microsphere-alibaba-sentinel-commons:<version>` |
| Branch / released versions | `0.1.6-SNAPSHOT` (`${revision}`) / `0.1.0` – `0.1.5` |
| Packaging / Java / Since | `jar` / 8 / `1.0.0` for all public types |
| POM name | `Microsphere :: Alibaba Sentinel :: Commons` |
| Packages | `io.microsphere.alibaba.sentinel.common` plus `.callback`, `.constants`, `.init`, `.reposistory`, `.util`, and `io.microsphere.alibaba.sentinel.event`, `.jmx` |

## Compatibility

| Component | Version managed here | Required at runtime |
| --- | --- | --- |
| Java | 8 | Yes |
| `com.alibaba.csp:sentinel-core`, `com.alibaba.csp:sentinel-transport-simple-http` | 1.8.10 | Core yes, transport optional (dashboard) |
| `microsphere-java-core`, `microsphere-annotation-processor` | via `microsphere-spring-cloud-dependencies` 0.1.26 | Yes (both declared optional) |
| `com.alibaba:fastjson` | 2.0.64 managed, not a direct dependency | No |
| Spring Cloud / Spring Boot | Hoxton.SR12 – 2021.0.x / 2.x | No — the module is framework-neutral |

This `0.1.x` line is built against Java 8 / Spring Boot 2.x / Spring Cloud Hoxton.SR12 - 2021.0.x and is maintained for applications that cannot move to Java 17. The current `0.2.x` line requires Java 17 / Spring Boot 3.x and exposes the same API plus everything that lands after `0.1.5`.

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
        <artifactId>microsphere-alibaba-sentinel-commons</artifactId>
    </dependency>
    <dependency><!-- optional upstream, declare it explicitly -->
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.10</version>
    </dependency>
</dependencies>
```

## API Reference

### `SentinelOperations`

`resourceName` is `@Nonnull`, `contextName` and `origin` are `@Nullable`. The one-time `execute` family takes `Runnable`, `Consumer<SentinelContext>` or `Function<SentinelContext, R>`; the `call` family takes `ThrowableAction`, `ThrowableConsumer<SentinelContext>` or `ThrowableFunction<SentinelContext, R>` and throws `Throwable`. Both exist in the shapes `(resourceName, callback)` and `(resourceName, contextName, origin, callback)`, next to the throwable-narrowing form and the two-phase pair:

```java
<R, TR extends Throwable> R call(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin,
    @Nonnull ThrowableFunction<SentinelContext, R> callback, Class<TR> throwableClass) throws TR;
@Nonnull SentinelContext begin(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin) throws Exception;
void end(SentinelContext context);
```

`execute(resourceName, contextName, origin, callback)` is exactly `call(resourceName, contextName, origin, callback::apply, RuntimeException.class)` — `execute` is the unchecked facade, `call` the checked one. `call` opens the context, applies the callback and in `finally` calls `end(context)`; a failure that is not a block exception (`BlockException.isBlockException(e)`) is reported with `Tracer.trace(e)` and logged at `error` before rethrowing. `begin` may raise `BlockException` when traffic is limited.

### `SentinelTemplate implements SentinelOperations`

`SentinelTemplate()`, `SentinelTemplate(int resourceType)` and `SentinelTemplate(int resourceType, EntryType trafficType)` default to `ResourceTypeConstants.COMMON` and `EntryType.IN`. `begin` substitutes `SentinelConstants.DEFAULT_CONTEXT_NAME` for a blank context name and `SentinelConstants.DEFAULT_ORIGIN` for a blank origin, runs `ContextUtil.enter(...)` then `SphU.entry(resourceName, resourceType, trafficType)`, and returns a new `SentinelContext`. `end` calls `Tracer.traceEntry(failure, entry)` only when `context.getFailure() != null`, then `entry.exit()` and `ContextUtil.exit()`.

### `SentinelContext`

A `ThreadLocal`-backed holder of `resourceName`, `contextName`, `origin` and the Sentinel `Entry`; its `protected` constructor rejects empty `resourceName` / `contextName` and null `origin` / `entry` with `IllegalArgumentException`. Fluent methods returning `this`: `setResult(Object)`, `setFailure(Throwable)`, `setAttribute(String, Object)`, `removeAttributes()`, `withinContext()`.

Documented nulls: `getResult()` is `null` when the callback returns no value or fails, `getFailure()` is `null` when execution succeeded, `getAttribute(name)` is `null` when absent while `getAttribute(name, defaultValue)` yields the default, `getAttributes()` is unmodifiable (an empty map until something is set), and `removeAttribute(name)` returns the removed value or `null`. Thread-bound API: `getContext()` / `removeContext()` (both `@Nullable`), `setContext(@Nullable SentinelContext)`, and `doInContext(Consumer<SentinelContext>)` / `doInContext(Consumer<SentinelContext>, boolean forRemoval)`, which do nothing when no context is bound and unbind it first when `forRemoval` is `true`.

### `SentinelPlugin`, `AbstractSentinelPlugin`, `SimpleSentinelPlugin`

```java
boolean isAutoInstalled();   void setEnabled(boolean enabled);   @Nonnull String getName();
default boolean isEnabled() { return isPluginEnabled(getName()); }      // reads a JVM system property
@Nonnull default String getContextName() { return getDefaultContextName(getName()); }
@Nonnull String getOrigin();   int getResourceType();   @Nonnull EntryType getTrafficType();
static void install(SentinelPlugin plugin) throws NullPointerException;
```

`install` adds the plugin to `SentinelPluginRepository.INSTANCE` and registers a shutdown hook that uninstalls it. `AbstractSentinelPlugin` caches the enabled flag in a `volatile boolean` seeded from `isEnabled()` at construction, adds `enable()` / `disable()`, and self-installs when `autoInstalled` is `true` — its 3-, 4- and 5-argument constructors default it to `true`, resource type `COMMON`, traffic type `IN`. `SimpleSentinelPlugin` adds a one-name constructor yielding context `microsphere_sentinel_default_context`, origin `""`, `COMMON`, `IN`, auto-installed.

### `SentinelPluginRepository` and implementations

```java
SentinelPluginRepository INSTANCE = loadFirstService(SentinelPluginRepository.class, true);
boolean install(SentinelPlugin plugin) throws NullPointerException;   // true only on first install
boolean isInstalled(String pluginName);   void clear();
@Nullable SentinelPlugin get(String pluginName);   @Nonnull Collection<SentinelPlugin> getAll();
default boolean uninstall(SentinelPlugin plugin) throws NullPointerException;
boolean uninstall(String pluginName) throws NullPointerException;
```

`SimpleSentinelPluginRepository` stores plugins in a `ConcurrentHashMap` keyed by name, using `putIfAbsent` and logging a `warn` on a duplicate name. `JMXSentinelPluginRepository` is the provider listed in `META-INF/services/io.microsphere.alibaba.sentinel.common.SentinelPluginRepository`, hence what `INSTANCE` loads: it delegates storage to a `SimpleSentinelPluginRepository` and registers a `StandardMBean` exposing `SentinelPlugin` under domain `io.microsphere.sentinel`, pattern `io.microsphere.sentinel:type=SentinelPlugin,name={}`. `get` self-heals (an unregistered MBean evicts the delegate entry), `clear` unregisters every MBean of the domain, and its `Prioritized` priority is `DEFAULT_PRIORITY` (`9`).

### Statistic-slot callbacks, metrics and events

- `DefaultNodeEntryCallback extends ProcessorSlotEntryCallback<DefaultNode>` is the marker interface for entry callbacks; `ProcessorSlotCallbackUtils` registers and looks them up (`addEntryCallback(s)`, `addExitCallback(s)`, `getEntryCallback(Class)`, `getExitCallback(Class)`, `removeEntryCallback(Class)`, `removeExitCallback(Class)`) over `StatisticSlotCallbackRegistry`, keyed by the callback class type name.
- `SentinelMetricsRepository` (`@Spi`, a `DefaultNodeEntryCallback`) records the resource-to-context mapping on pass and on block and reads metric logs through `MetricSearcher`: `findMetricNodes(long, int)`, `findMetricNodes(long, long)`, `findMetricNodes(long, long, String resource)`, `findContextMetricNodesMap(long, long)`, `getResource(DefaultNode)`, `getContext(String)` (falls back to Sentinel `CONTEXT_DEFAULT_NAME`) and the `@Nullable` static `getSentinelMetricsRepository()`. Search failures are logged and yield empty lists or maps, never exceptions.
- `SentinelNodeEventPublisher` (`@Spi`, a `DefaultNodeEntryCallback`) dispatches one `ClusterNodeAddedEvent` (exposing `getClusterNode()`, `getContextName()`, `getResourceName()`) per resource through the fluent `addEventListener` / `removeEventListener` pair (`ClusterNodeAddedEventListener` is `@FunctionalInterface`) and an optional `Executor`; `getSentinelNodeEventPublisher()` is `@Nullable`.
- `StatisticSlotCallbackSpiLoaderInitFunc implements InitFunc`, listed in `META-INF/services/com.alibaba.csp.sentinel.init.InitFunc`, loads the sorted `DefaultNodeEntryCallback` and `ProcessorSlotExitCallback` SPI instances at Sentinel startup and registers them, which is how the two classes above become active without a DI container.

### `SentinelUtils` and `SentinelConstants`

`buildResourceName(Method)` renders `SimpleClassName.method(SimpleParam1,SimpleParam2)`, e.g. `SentinelUtils.buildResourceName(Method)`. Also: `getFlowDataId(String)` (pattern `{}-flow-rules`), `getPluginEnabledPropertyName(String)`, `isPluginEnabled(String)`, `getDefaultContextName(String)` (pattern `microsphere_sentinel_{}_context`), `getResourceTypeAsString(int)` (`UNKNOWN` when unmapped), `findSentinelMetricsTaskExecutor()`, `getSentinelMetricsTaskExecutor()`, `resetContextMap()` (also run from a shutdown hook), `getMetricSearcher()` and `newMetricSearcher()`.

`SentinelConstants` holds `FLOW_DATA_ID_PATTERN`, `DEFAULT_CONTEXT_NAME_PATTERN`, `DEFAULT_CONTEXT_NAME` (`microsphere_sentinel_default_context`), `DEFAULT_ORIGIN` (`""`), `PROPERTY_NAME_PREFIX` (`microsphere.sentinel.`), `ENABLED_PROPERTY_VALUE` (`"true"`), `ENABLED_PROPERTY_NAME` (`microsphere.sentinel.enabled`), `SENTINEL_CONTEXT_ATTRIBUTE_NAME` (`sentinel-context`) and `DEFAULT_PRIORITY` (`9`).

## Configuration Properties

| Property | Type | Default | Where it is consumed | Meaning |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.enabled` | `boolean` | `true` | `SentinelConstants.ENABLED_PROPERTY_NAME`; the Spring Boot `ConditionalOnSentinelEnabled` condition | Global switch for all Sentinel auto-configuration |
| `microsphere.sentinel.<pluginName>.enabled` | `boolean` | `true` | `SentinelUtils.isPluginEnabled(String)`, JVM **system property** only | Per-plugin switch seeding `SentinelPlugin.isEnabled()` |

Enable semantics are literal: `isPluginEnabled` resolves `System.getProperty("microsphere.sentinel." + pluginName + ".enabled", "true")` and returns `!"false".equalsIgnoreCase(value)` — only the exact string `false` (any case) disables, any other value, even a malformed one, leaves the plugin enabled. The property is read once per plugin construction; afterwards use `setEnabled(boolean)`, `enable()` / `disable()`, or the JMX MBean.

## Usage Examples

```java
// adapted from SentinelTemplateTest
SentinelTemplate template = new SentinelTemplate();
template.execute("test-resource", () -> { /* business logic */ });
String value = template.execute("test-resource", "test-context", "test-origin",
        context -> context.getResourceName());              // "test-resource"
List<String> list = template.call("test-resource", "test-context", "test-origin",
        context -> Collections.singletonList(context.getOrigin()), SQLException.class);

// two-phase form, as the plugins use it: begin in beforeExecute, end in afterExecute
SentinelContext context = template.begin("test-resource", "test-context", "test-origin").withinContext();
context.setResult(result);
context.setFailure(failure);          // traced by Tracer.traceEntry inside end()
template.end(context);
SentinelContext.doInContext(c -> c.setAttribute("sql", "select 1"), true);   // consumes, then unbinds
assertNull(SentinelContext.getContext());
```

```java
// adapted from SimpleSentinelPluginTest and SentinelPluginRepositoryTest
SentinelPlugin plugin = new SimpleSentinelPlugin("my-plugin", DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN,
        ResourceTypeConstants.COMMON, EntryType.IN, false);  // last argument: do not self-install
SentinelPluginRepository repository = SentinelPluginRepository.INSTANCE;
assertTrue(repository.install(plugin));                     // duplicate name returns false, logs a warn
assertTrue(repository.isInstalled("my-plugin"));
plugin.disable();                                          // the enabled flag is runtime-mutable
repository.uninstall(plugin);
```

```java
// adapted from SentinelMetricsRepositoryTest, SentinelNodeEventPublisherTest, AbstractSentinelTemplateTest
SentinelMetricsRepository metrics = new SentinelMetricsRepository();
ProcessorSlotCallbackUtils.addEntryCallback(metrics);
Map<String, List<MetricNode>> byContext = metrics.findContextMetricNodesMap(startTime, endTime);
SentinelNodeEventPublisher publisher = new SentinelNodeEventPublisher();
ProcessorSlotCallbackUtils.addEntryCallback(publisher);
publisher.addEventListener(event -> System.out.println(event.getResourceName()));

FlowRule rule = new FlowRule();
rule.setResource("test-resource-1"); rule.setCount(1); rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
FlowRuleManager.loadRules(Collections.singletonList(rule));
assertThrows(BlockException.class, () -> template.call("test-resource-1",
        () -> { throw new FlowException("For testing..."); }));
```

## Related Modules

- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) — the aggregator of the six plugins
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) and [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) — reference consumers of this API
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md), [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) and [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) — Spring wiring of the properties above; [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) — the BOM
- [Branch index](./README.md) · [Repository root](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-commons.md)
