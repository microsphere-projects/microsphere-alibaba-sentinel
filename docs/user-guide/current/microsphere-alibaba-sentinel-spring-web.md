# microsphere-alibaba-sentinel-spring-web

[中文版](zh/microsphere-alibaba-sentinel-spring-web.md)

## Overview

`microsphere-alibaba-sentinel-spring-web` protects Spring WebMVC handler methods with Alibaba Sentinel. It contributes
`io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`, a `HandlerMethodInterceptor` of
**microsphere-spring-web** (`io.microsphere.spring.web.method.support.HandlerMethodInterceptor`) that opens a Sentinel
entry before the handler method runs and closes it afterwards, using the endpoint mapping expression as the resource
name.

Key facts verified from the source:

- Plugin name (`SentinelSpringWebConstants.PLUGIN_NAME`): `spring-web`.
- Context name: `microsphere_sentinel_spring_web_context` (`SentinelSpringWebConstants.DEFAULT_CONTEXT_NAME`, the
  `microsphere_sentinel_{}_context` pattern of `SentinelUtils.getDefaultContextName(String)`).
- Origin: `HandlerMethod` (`SentinelSpringWebConstants.DEFAULT_ORIGIN`).
- Resource type: `ResourceTypeConstants.COMMON_WEB` (`1`); traffic type `EntryType.IN` - the constructor calls the four
  argument `AbstractSentinelPlugin(String, String, String, int)`, which defaults the entry type to `IN`.
- Auto-installation: as a direct `AbstractSentinelPlugin` subclass, the constructor installs the plugin into the
  `SentinelPluginRepository` service, which also registers the MBean
  `io.microsphere.sentinel:type=SentinelPlugin,name=spring-web`.
- Spring bean name constant: `SentinelHandlerMethodInterceptor.BEAN_NAME = "sentinelHandlerMethodInterceptor"`.
- Registration with Spring: `META-INF/spring.factories` declares
  `io.microsphere.spring.web.method.support.HandlerMethodInterceptor=io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`,
  which microsphere-spring-web's `WebExtensionBeanDefinitionRegistrar` turns into beans when the web extension is enabled.
- Order: `getOrder()` returns `SentinelConstants.DEFAULT_PRIORITY` (`9`) by default and `setOrder(int)` is public.

## Version Information

| Item | Value |
|------|-------|
| Documented branch | `release` (this file lives in `docs/user-guide/current`) |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-spring-web` |
| Branch version | `0.2.6-SNAPSHOT` |
| Released versions | `0.2.0` - `0.2.5` |
| Java version | 17 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| Spring Cloud / Spring Boot | 2022.0.x - 2025.0.x / 3.x (default build profile: 2025.0.3 / 3.5.15) |

## Compatibility

| Technology | Status | Evidence |
|------------|--------|----------|
| Spring WebMVC 6.x (`org.springframework:spring-webmvc`) | required, declared `optional` | imports `HandlerMethod`, `NativeWebRequest` from the Jakarta-era packages |
| `jakarta.servlet:jakarta.servlet-api` | `provided` | dependency in the module POM |
| microsphere-spring-web (`io.github.microsphere-projects:microsphere-spring-web`) | required, declared `optional` | implements `HandlerMethodInterceptor`, listens for `WebEndpointMappingsReadyEvent`, uses `WebEndpointMapping` and `WebScope` |
| Spring Boot | auto-configured for servlet and reactive apps | `SentinelSpringWebAutoConfiguration` is annotated `@ConditionalOnWebApplication(type = ANY)` |
| Spring Cloud Alibaba Sentinel | coexists | the auto-configuration runs `@AutoConfigureAfter` `com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration` |

The parent POM also carries a `spring-cloud-2025.1` profile (Spring Boot 4.x); the release branch documentation covers
the Spring Boot 3.x / Spring Cloud 2022-2025 line.

## Installation

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
        <artifactId>microsphere-alibaba-sentinel-spring-web</artifactId>
    </dependency>
    <!-- Optional in the plugin POM; the application must provide them -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-spring-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>
</dependencies>
```

Under Spring Boot nothing else is required. In a plain WebMVC context, add `@EnableWebMvcExtension(registerHandlerInterceptors =
true)` (or `@EnableWebExtension`) so the `HandlerMethodInterceptor` declared in `spring.factories` is registered.

## API Reference

```java
public class SentinelHandlerMethodInterceptor
        extends io.microsphere.alibaba.sentinel.common.AbstractSentinelPlugin
        implements io.microsphere.spring.web.method.support.HandlerMethodInterceptor,
                   org.springframework.context.ApplicationListener<WebEndpointMappingsReadyEvent>,
                   org.springframework.core.Ordered {

    public static final String BEAN_NAME = "sentinelHandlerMethodInterceptor";

    public SentinelHandlerMethodInterceptor();
    public SentinelHandlerMethodInterceptor(String contextName, String origin);

    // HandlerMethodInterceptor
    public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception;
    public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error,
                             NativeWebRequest request);

    // ApplicationListener
    public void onApplicationEvent(WebEndpointMappingsReadyEvent event);

    // Ordered
    public int getOrder();
    public void setOrder(int order);

    // Resource naming
    public String getResourceName(HandlerMethod handlerMethod);
    protected String buildResourceName(HandlerMethod handlerMethod, WebEndpointMapping webEndpointMapping);
    protected void initEntryCache(WebEndpointMappingsReadyEvent event);

    // Request-scoped context bridging
    public static void setSentinelContext(@Nonnull SentinelContext context, @Nonnull NativeWebRequest request);
    @Nonnull public static SentinelContext getSentinelContext(NativeWebRequest request);
}
```

Behaviour details:

- Resource names are prepared when microsphere-spring-web publishes `WebEndpointMappingsReadyEvent`:
  `initEntryCache` iterates the `WebEndpointMapping` collection and stores, per `HandlerMethod`, the name
  `webEndpointMapping.toExpression() + "#" + handlerMethod`, for example
  `GET "/test/greeting/{message}"#GET io.microsphere...TestController.greeting(String)`.
- `getResourceName(HandlerMethod)` falls back to `spring-web:<handlerMethod>` (the `computeIfAbsent` default) for handler
  methods that were not part of the published endpoint mappings.
- The `SentinelContext` is stored as the request-scoped attribute `sentinel-context`
  (`SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME`) through `WebScope.REQUEST`; `afterExecute` sets result and error on
  it before `SentinelTemplate.end(...)` reports the error to `Tracer` and exits.
- `afterExecute` returns quietly (trace log) when no `SentinelContext` is present in the request, which is why calling it
  without a prior `beforeExecute` is safe. `getSentinelContext` is annotated `@Nonnull` but does return `null` in that case;
  the annotation is documentation only.
- Both hooks are skipped when `isEnabled()` is `false`; `BlockException` raised by `begin` propagates to the DispatcherServlet.

## Configuration Properties

| Property | Default | Source | Effect |
|----------|---------|--------|--------|
| `microsphere.sentinel.enabled` | `true` | system properties, application config | Gates `SentinelSpringWebAutoConfiguration` via `@ConditionalOnSentinelEnabled` |
| `microsphere.sentinel.spring-web.enabled` | `true` | system properties, application config | Gates the auto-configuration (`@ConditionalOnProperty(matchIfMissing = true)`) and the runtime `isEnabled()` value |

Note the hyphen in the plugin property: it is derived from the plugin name `spring-web`, while the Sentinel context name
uses the underscore form `microsphere_sentinel_spring_web_context`. The runtime check reads the JVM system property
(`SentinelUtils.isPluginEnabled("spring-web")`) once at construction; afterwards use `setEnabled(boolean)`, `enable()`,
`disable()` or the `SentinelPlugin` MBean.

## Usage Examples

### Registering the interceptor in a WebMVC test context

```java
// adapted from SentinelHandlerMethodInterceptorTest
@WebAppConfiguration
@SpringJUnitConfig(classes = {
        SentinelHandlerMethodInterceptor.class,
        SentinelHandlerMethodInterceptorTest.class
})
@EnableWebMvc
@EnableWebMvcExtension(registerHandlerInterceptors = true)
class SentinelHandlerMethodInterceptorTest extends AbstractWebMvcTest {

    @Autowired
    private SentinelHandlerMethodInterceptor interceptor;

    @Test
    void testConstants() {
        assertEquals("sentinelHandlerMethodInterceptor", BEAN_NAME);
    }
}
```

### Exercising protected endpoints

```java
// adapted from SentinelHandlerMethodInterceptorTest#test
// AbstractWebMvcTest performs MockMvc requests:
//   GET /test/helloworld     and  GET /test/greeting/{message}
this.testHelloWorld();
this.testGreeting();

// each request opens and closes a Sentinel entry whose resource name is the endpoint mapping
// expression plus the handler method, then the entrance node is populated:
Context context = enter(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN);   // microsphere_sentinel_spring_web_context
assertFalse(context.getEntranceNode().getChildList().isEmpty());
exit();
```

### Turning the plugin off

```java
// adapted from SentinelHandlerMethodInterceptorTest#testDisabled
interceptor.setEnabled(false);
assertFalse(interceptor.isEnabled());
test();                    // requests still succeed, no entries are recorded
interceptor.setEnabled(true);
assertTrue(interceptor.isEnabled());
```

### Calling the interceptor without a Sentinel context

```java
// adapted from SentinelHandlerMethodInterceptorTest#testAfterExecuteWithoutSentinelContext
MockServletWebRequest request = new MockServletWebRequest();
this.interceptor.afterExecute(null, null, null, null, request);   // trace log, no exception
assertNull(getSentinelContext(request));
```

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `AbstractSentinelPlugin`,
  `SentinelOperations`, `SentinelTemplate`, `SentinelContext`, `SentinelUtils`.
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) -
  `SentinelSpringWebAutoConfiguration` and its conditional annotations.
- [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md) - the sibling plugin covering Spring
  Data Redis commands.
- [User guide index](./README.md) - all modules of the current branch.
- [Project root](../../../README.md) - repository overview.

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-spring-web.md)
