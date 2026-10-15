# microsphere-alibaba-sentinel-redis

[中文版](zh/microsphere-alibaba-sentinel-redis.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-redis.md](../current/microsphere-alibaba-sentinel-redis.md).

## Overview

`microsphere-alibaba-sentinel-redis` protects Spring Data Redis commands with Alibaba Sentinel. It contributes
`io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor`, a
`RedisConnectionInterceptor` of **microsphere-redis** (`io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor`)
that wraps every intercepted `RedisConnection` method call in a Sentinel entry. microsphere-redis achieves the
interception by wrapping `RedisTemplate` / `StringRedisTemplate` (Lettuce-backed by default in the test configuration),
so application code stays unchanged.

Key facts verified from the source:

- Plugin name (`SentinelRedisConstants.PLUGIN_NAME`): `redis`.
- Context name: `microsphere_sentinel_redis_context` (`SentinelRedisConstants.DEFAULT_CONTEXT_NAME`, the
  `microsphere_sentinel_{}_context` pattern of `SentinelUtils.getDefaultContextName(String)`).
- Origin: `RedisConnection` (`SentinelRedisConstants.DEFAULT_ORIGIN`).
- Resource type: `ResourceTypeConstants.COMMON` (`0`) and traffic type `EntryType.IN`. The class calls the three
  argument `AbstractSentinelPlugin(String, String, String)` constructor, which applies those defaults; Sentinel 1.8.10
  defines no dedicated Redis type (`COMMON`, `COMMON_WEB`, `COMMON_RPC`, `COMMON_API_GATEWAY`, `COMMON_DB_SQL` only).
- Resource name: `SentinelUtils.buildResourceName(Method)` - `<simple declaring class>.<method>(<simple parameter types>)`,
  for example `RedisStringCommands.set(byte[],byte[])`.
- Auto-installation: the class extends `AbstractSentinelPlugin`, whose constructor calls `SentinelPlugin.install(this)`
  (the plugin is registered in the `SentinelPluginRepository` service and exposed as the MBean
  `io.microsphere.sentinel:type=SentinelPlugin,name=redis`).
- Registration with Spring: `META-INF/spring.factories` in this jar declares
  `io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor=io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor`.
- Interceptor order: `getOrder()` returns `SentinelConstants.DEFAULT_PRIORITY` (`9`).

## Version Information

| Item | Value |
|------|-------|
| Documented branch | `release-1.x` (this file lives in `docs/user-guide/1.x`) |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-redis` |
| Branch version | `0.1.6-SNAPSHOT` |
| Released versions | `0.1.0` - `0.1.5` |
| Java version | 8 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `microsphere-redis` BOM | 0.1.13 |
| Spring Boot / Spring Cloud | 2.x / Hoxton.SR12 - 2021.0.x |

## Compatibility

| Technology | Status | Evidence |
|------------|--------|----------|
| Spring Data Redis (`org.springframework.data:spring-data-redis`) | required, declared `optional` | imports `RedisConnection`, `RedisClusterConnection` |
| microsphere-redis (`io.github.microsphere-projects:microsphere-redis-spring`) | required, declared `optional` | implements `RedisConnectionInterceptor`, uses `RedisMethodContext` |
| microsphere-spring-context | declared `optional` | dependency in the module POM |
| Redis client | Lettuce | `io.lettuce:lettuce-core` in test scope; the interceptor sits above the client |
| Spring Boot | auto-configured | `SentinelRedisAutoConfiguration` registers the `SentinelRedisCommandInterceptor` bean |
| Namespace | javax / Spring 5 baseline | Java 8 plus the Spring Cloud Hoxton-2021 profiles of the parent POM |

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
        <artifactId>microsphere-alibaba-sentinel-redis</artifactId>
    </dependency>
    <!-- Optional in the plugin POM; the application must provide them -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>
</dependencies>
```

In a plain Spring (non-Boot) context, import the interceptor bean and enable microsphere-redis interception on the
templates you want to protect (`@EnableRedisInterceptor`). Under Spring Boot, `SentinelRedisAutoConfiguration` creates
the bean when Sentinel, Redis and the interceptor module are all present.

## API Reference

```java
public class SentinelRedisCommandInterceptor
        extends io.microsphere.alibaba.sentinel.common.AbstractSentinelPlugin
        implements io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor,
                   org.springframework.beans.factory.InitializingBean,
                   org.springframework.beans.factory.BeanClassLoaderAware {

    public SentinelRedisCommandInterceptor();
    public SentinelRedisCommandInterceptor(String contextName, String origin);

    // RedisConnectionInterceptor (RedisMethodInterceptor<RedisConnection>)
    public void beforeExecute(RedisMethodContext<RedisConnection> redisMethodContext) throws Throwable;
    public void afterExecute(RedisMethodContext<RedisConnection> redisMethodContext, Object result, Throwable failure);
    public int getOrder();   // SentinelConstants.DEFAULT_PRIORITY = 9

    // Spring lifecycle
    public void setBeanClassLoader(ClassLoader classLoader);
    public void afterPropertiesSet();   // builds the method -> resource name cache

    // Context bridging helpers
    public static void setContext(RedisMethodContext context, SentinelContext sentinelContext);
    public static SentinelContext getSentinelContext(RedisMethodContext context);
}
```

Inherited from `AbstractSentinelPlugin` / `SentinelPlugin`:
`getName()`, `getContextName()`, `getOrigin()`, `getResourceType()`, `getTrafficType()`, `isAutoInstalled()`,
`isEnabled()`, `setEnabled(boolean)`, `enable()`, `disable()`.

Behaviour details:

- `afterPropertiesSet()` walks `ClassUtils.getAllInterfacesForClass(RedisClusterConnection.class, classLoader)` and
  caches `buildResourceName(method)` for every interface method (a map pre-sized for 512 entries via
  `MapUtils.newFixedHashMap(512)`).
- `beforeExecute` looks the invoked method up in that cache. If the method is unknown - for example a method of a
  non-Redis interface - it logs a trace message and returns without opening an entry.
- The `SentinelContext` is carried between the two phases in the `RedisMethodContext` attribute `sentinel-context`
  (`SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME`), not in a `ThreadLocal`, so nested commands stay consistent.
- `afterExecute` records `result` and `failure` on the context before `SentinelTemplate.end(...)`; non-block failures are
  reported to `Tracer` and feed exception-based rules.
- If `afterPropertiesSet()` is never called, the cache stays empty and no command is intercepted; a `BlockException`
  raised by `begin` propagates out of the Redis call.

## Configuration Properties

| Property | Default | Source | Effect |
|----------|---------|--------|--------|
| `microsphere.sentinel.enabled` | `true` | system properties, application config | Gates `SentinelRedisAutoConfiguration` through `@ConditionalOnSentinelEnabled` |
| `microsphere.sentinel.redis.enabled` | `true` | system properties, application config | Gates the auto-configuration (`@ConditionalOnProperty(matchIfMissing = true)`) and the runtime `isEnabled()` check |
| `microsphere.redis.enabled` | - | application config | microsphere-redis switch; must be `true` for interception to happen |
| `microsphere.redis.interceptor.enabled` | - | application config | microsphere-redis gate on the inner `Config` class of the auto-configuration |

The runtime check `SentinelUtils.isPluginEnabled("redis")` reads the JVM system property only
(`!"false".equalsIgnoreCase(value)`), evaluated once when the interceptor is constructed; use `setEnabled(false)`,
`disable()` or the JMX MBean to change it afterwards.

## Usage Examples

### Registering the interceptor in a plain Spring context

```java
// adapted from SentinelRedisCommandInterceptorTest
@SpringJUnitConfig(classes = {
        RedisConfig.class,
        SentinelRedisCommandInterceptorTest.class
})
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true",
        "spring.application.name=test-app"
})
@EnableRedisInterceptor(wrapRedisTemplates = "stringRedisTemplate")
class SentinelRedisCommandInterceptorTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SentinelRedisCommandInterceptor interceptor;
}
```

### Protecting a command and asserting Sentinel metrics

```java
// adapted from SentinelRedisCommandInterceptorTest#test
String key = "key";
String value = "value";
ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
valueOperations.set(key, value);          // resource: RedisStringCommands.set(byte[],byte[])
assertEquals(value, valueOperations.get(key));
assertEquals(DEFAULT_PRIORITY, interceptor.getOrder());   // 9

// the entrance node of microsphere_sentinel_redis_context now holds child nodes
Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);
assertFalse(context.getEntranceNode().getChildList().isEmpty());
exit();
```

### Turning the plugin off

```java
// adapted from SentinelRedisCommandInterceptorTest#testDisabled
interceptor.setEnabled(false);
assertFalse(interceptor.isEnabled());
test();                     // commands still succeed, no Sentinel entry recorded
interceptor.setEnabled(true);
assertTrue(interceptor.isEnabled());
```

### Non-intercepted methods

```java
// adapted from SentinelRedisCommandInterceptorTest#testResourceNotFound
// A method that is not part of the RedisClusterConnection interface hierarchy is skipped
Method method = getClass().getMethods()[0];
RedisMethodContext context =
        new RedisMethodContext(stringRedisTemplate, method, ofArray(), redisContext);
assertDoesNotThrow(() -> interceptor.beforeExecute(context));
assertDoesNotThrow(() -> interceptor.afterExecute(context, null, null));
```

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `AbstractSentinelPlugin`,
  `SentinelOperations`, `SentinelTemplate`, `SentinelContext`, `SentinelUtils`.
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) -
  `SentinelRedisAutoConfiguration` and its conditions.
- [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md) - the sibling plugin that
  protects the web entry point with resource type `COMMON_WEB`.
- [User guide index](./README.md) - all modules of this branch.
- [Project root](../../../README.md) - repository overview.

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-redis.md)
