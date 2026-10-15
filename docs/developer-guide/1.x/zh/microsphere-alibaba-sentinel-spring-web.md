# microsphere-alibaba-sentinel-spring-web — 开发者指南

[English](../microsphere-alibaba-sentinel-spring-web.md)

> **已归档的 `1.x` 分支。** 本页记录 `0.1.x` 构件的内部设计（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。源码树与当前分支完全一致，仅 POM 基线不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。本页在主干文档中的对应页面是 [../current/microsphere-alibaba-sentinel-spring-web.md](../../current/zh/microsphere-alibaba-sentinel-spring-web.md)。

## 目的与范围

本模块将 Alibaba Sentinel 流控应用于 Spring WebMVC 处理方法。
`SentinelHandlerMethodInterceptor` 实现了来自兄弟项目 `microsphere-spring-web` 的
`io.microsphere.spring.web.method.support.HandlerMethodInterceptor` 扩展点，在每次分发
`HandlerMethod` 的前后包裹一个 Sentinel entry。资源名以请求映射表达式加上处理方法作为键，
并在端点映射就绪时预先计算。

本指南涵盖：

- 针对 `HandlerMethod` 分发的 `beforeExecute`/`afterExecute` 这一对拦截钩子。
- 请求作用域的 `SentinelContext` 经由 `NativeWebRequest` 属性的传递。
- 构建资源名缓存的 `WebEndpointMappingsReadyEvent` 监听器。
- `META-INF/spring.factories` 注册了什么，以及本分支与 javax.servlet 的对齐。

发布线事实：0.1.x、Java 8、Spring Boot 2.x 世代（`sentinel-core` 1.8.10）；
pom 将 Servlet API 固定为 `javax.servlet:javax.servlet-api` 且作用域为 `provided`，
与 Spring Framework 5 / Spring Boot 2.x 对齐（当前 `0.2.x` 线在 Spring Framework 6 上使用 `jakarta.servlet`）。

## 架构

```mermaid
graph TB
    subgraph Spring WebMVC
        DD[DispatcherServlet]
        EAH[HandlerAdapter invocation]
        HM[HandlerMethod]
    end
    subgraph microsphere-spring-web
        HMI[HandlerMethodInterceptor extension point]
        WEM[WebEndpointMapping registry]
        EE[WebEndpointMappingsReadyEvent]
        SF[META-INF spring.factories lookup]
    end
    subgraph This Module
        IN[SentinelHandlerMethodInterceptor]
        KC[SentinelSpringWebConstants]
    end
    subgraph Commons
        ASP[AbstractSentinelPlugin]
        SOP[SentinelOperations]
        ST[SentinelTemplate]
        SC[SentinelContext]
    end
    SENT[Sentinel Core SphU Tracer ContextUtil]
    DD --> EAH --> HM
    SF -. lookup by HandlerMethodInterceptor key .-> HMI
    EAH --> HMI --> IN
    WEM --> EE --> IN
    IN --> KC
    IN --> ASP
    IN --> SOP
    SOP --> ST
    ST --> SENT
    IN --> SC
```

## 源码分析

### `io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`

声明为
`public class SentinelHandlerMethodInterceptor extends AbstractSentinelPlugin implements HandlerMethodInterceptor, ApplicationListener<WebEndpointMappingsReadyEvent>, Ordered`。

字段与常量：

- `public static final String BEAN_NAME = "sentinelHandlerMethodInterceptor"` — 由测试断言。
- `private final SentinelOperations sentinelOperations;`
- `private int order;` 以及 `private Map<Method, String> methodResourceNamesCache;`

构造器：

- `public SentinelHandlerMethodInterceptor()` — `this(DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN)`。
- `public SentinelHandlerMethodInterceptor(String contextName, String origin)` —
  `super(PLUGIN_NAME, contextName, origin, COMMON_WEB)`；四参数的 `AbstractSentinelPlugin`
  形式会设置 `resourceType = ResourceTypeConstants.COMMON_WEB`、`trafficType = EntryType.IN`、
  `autoInstalled = true`（自安装进 `SentinelPluginRepository.INSTANCE`，并注册关停钩子以卸载）。随后
  `new SentinelTemplate(getResourceType(), getTrafficType())` 和
  `this.setOrder(DEFAULT_PRIORITY)`（9）。

拦截钩子：

- `public void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception` —
  当 `isEnabled()` 时：`resourceName = getResourceName(handlerMethod)`；
  `sentinelOperations.begin(resourceName, getContextName(), getOrigin())` 先执行
  `ContextUtil.enter`，再执行 `SphU.entry(resourceName, COMMON_WEB, IN)`；`BlockException` 会从
  该钩子向外传播并中止处理方法调用。随后上下文通过
  `setSentinelContext(context, request)` 存入请求。
- `public void afterExecute(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request)` —
  当 `isEnabled()` 时：`getSentinelContext(request)`；若不存在，则记录 trace 日志并返回。否则
  `context.setResult(returnValue).setFailure(error)` 与 `sentinelOperations.end(context)`，后者
  在记录了错误时调用 `Tracer.traceEntry(error, entry)`，然后 `entry.exit()` 和
  `ContextUtil.exit()`。

请求作用域的载体（属性名 `sentinel-context` 来自 commons 的 `SentinelConstants`）：

- `public static void setSentinelContext(@Nonnull SentinelContext context, @Nonnull NativeWebRequest request)` —
  使用 `io.microsphere.spring.web.util.WebScope.REQUEST` 调用
  `REQUEST.setAttribute(request, SENTINEL_CONTEXT_ATTRIBUTE_NAME, context)`。
- `@Nonnull public static SentinelContext getSentinelContext(NativeWebRequest request)` —
  `REQUEST.getAttribute(request, SENTINEL_CONTEXT_ATTRIBUTE_NAME)`。

事件驱动的缓存构建：

- `public void onApplicationEvent(WebEndpointMappingsReadyEvent event)` — 调用 `initEntryCache(event)`。
- `protected void initEntryCache(WebEndpointMappingsReadyEvent event)` — 按 `event.getMappings()` 的大小
  重建一个全新的 `newFixedHashMap`；对每个 `getEndpoint()` 为 `HandlerMethod` 的
  `WebEndpointMapping`，将 `buildResourceName(handlerMethod, webEndpointMapping)`
  以 `handlerMethod.getMethod()` 为键缓存起来。
- `protected String buildResourceName(HandlerMethod handlerMethod, WebEndpointMapping webEndpointMapping)` —
  返回 `webEndpointMapping.toExpression() + "#" + handlerMethod`。
- `public String getResourceName(HandlerMethod handlerMethod)` —
  `methodResourceNamesCache.computeIfAbsent(method, m -> PLUGIN_NAME + ":" + handlerMethod)`；未知
  的方法仍会得到一个以 `spring-web:` 为前缀的非空回退名。
- `public int getOrder()` / `public void setOrder(int order)` — 可变的 `Ordered` 支持。

### `io.microsphere.alibaba.sentinel.spring.web.SentinelSpringWebConstants`

由 `SentinelSpringWebConstantsTest` 断言：

- `String PLUGIN_NAME = "spring-web"`。
- `String DEFAULT_CONTEXT_NAME = "microsphere_sentinel_spring_web_context"`。
- `String DEFAULT_ORIGIN = "HandlerMethod"`。
- `String ENABLED_PROPERTY_NAME = "microsphere.sentinel.spring-web.enabled"` — 带有 `SYSTEM_PROPERTIES_SOURCE`
  和 `APPLICATION_SOURCE` 的 `@ConfigurationProperty`；`SentinelUtils.isPluginEnabled` 读取该 JVM 系统属性，
  默认为 `true`，并在 `AbstractSentinelPlugin` 构造器中求值一次。

### `META-INF/spring.factories`

该文件仅包含一个映射——即 microsphere-spring-web 的拦截器查找键，**不是**
Spring Boot 自动配置键：

```
io.microsphere.spring.web.method.support.HandlerMethodInterceptor=\
io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor
```

### 测试

`SentinelHandlerMethodInterceptorTest` 继承 `AbstractWebMvcTest`，运行于
`@WebAppConfiguration @SpringJUnitConfig(classes = {SentinelHandlerMethodInterceptor.class, test})
@EnableWebMvc @EnableWebMvcExtension(registerHandlerInterceptors = true)` 之下——microsphere-spring-webmvc
扩展将拦截器链注册进 MVC 调用。

- `testConstants()` — 断言 `BEAN_NAME` 等于 `sentinelHandlerMethodInterceptor`。
- `test()` — 调用继承而来的 mock-MVC 方法 `testHelloWorld()` 和 `testGreeting()`，随后
  `postTest()` 进入 `microsphere_sentinel_spring_web_context`/`HandlerMethod`，并断言入口
  节点有子节点（基于映射的资源已被注册）。
- `testDisabled()` — 调用 `setEnabled(false)`，以无保护状态重新运行相同请求，再启用。
- `testAfterExecuteWithoutSentinelContext()` — 调用 `afterExecute(null, null, null, null,
  MockServletWebRequest)`；不抛出异常，且 `getSentinelContext(request)` 保持为 `null`。

## 设计模式

- **责任链 / 装饰器钩子**：`HandlerMethodInterceptor`（来自 microsphere-spring-web）
  在 Spring WebMVC 分发内部装饰 `HandlerMethod` 调用，并按 `Ordered`（9）排序。
- **观察者**：当端点映射就绪时，`ApplicationListener<WebEndpointMappingsReadyEvent>` 重建资源名缓存。
- **模板方法**：entry 机制集中于 `SentinelOperations`/`SentinelTemplate`；
  继承 `AbstractSentinelPlugin` 提供启用状态与插件元数据。
- **缓存/记忆化**：`methodResourceNamesCache` 以 `java.lang.reflect.Method` 为键，并使用
  `computeIfAbsent` 作为回退。
- **属性黑板**：`SentinelContext` 通过 `NativeWebRequest` 的请求作用域（`WebScope.REQUEST`）
  从 before 钩子传递到 after 钩子，从而避免 `ThreadLocal` 耦合。

## 核心流程

### 处理方法保护流程

```mermaid
sequenceDiagram
    participant CL as HTTP client
    participant DS as DispatcherServlet
    participant IX as microsphere-spring-web chain
    participant IN as SentinelHandlerMethodInterceptor
    participant ST as SentinelTemplate
    participant SL as Sentinel Core
    CL->>DS: mapped request
    DS->>IX: invoke HandlerMethod with NativeWebRequest
    IX->>IN: beforeExecute handlerMethod args request
    IN->>IN: isEnabled then getResourceName via cache
    IN->>ST: begin resourceName contextName origin
    ST->>SL: ContextUtil.enter then SphU.entry resourceName COMMON_WEB IN
    SL-->>ST: Entry
    ST-->>IN: SentinelContext
    IN->>IX: context stored as request attribute sentinel-context
    IX->>IX: real handler method invocation and view or body return
    IX->>IN: afterExecute handlerMethod args returnValue error request
    IN->>ST: end context with result and error recorded
    ST->>SL: Tracer.traceEntry on error then entry.exit and ContextUtil.exit
```

### 映射就绪后的缓存构建与拦截路径

```mermaid
flowchart TD
    A[WebEndpointMappingsReadyEvent] --> B[initEntryCache rebuilds map]
    B --> C[resource name equals mapping expression hash HandlerMethod]
    D[request arrives] --> E{isEnabled}
    E -- false --> F[hooks no-op]
    E -- true --> G[SphU.entry]
    G -- BlockException --> H[beforeExecute throws, request rejected]
    G -- allowed --> I[context on request scope] --> J[afterExecute ends entry, traces handler error]
```

## 依赖

```mermaid
graph LR
    M[microsphere-alibaba-sentinel-spring-web] --> C1[microsphere-alibaba-sentinel-commons]
    M --> C2[microsphere-spring-web]
    M --> C3[spring-webmvc]
    M --> C4[sentinel-core]
    M --> C5[javax.servlet-api]
    M --> C6[microsphere-annotation-processor]
    M --> T1[microsphere-spring-webmvc]
    M --> T2[microsphere-spring-test spring-test]
    M --> T3[jsonassert hamcrest logback-classic junit-jupiter]
```

| 依赖 | 在 pom 中的作用域 |
|---|---|
| io.github.microsphere-projects:microsphere-annotation-processor | compile, optional |
| io.github.microsphere-projects:microsphere-alibaba-sentinel-commons | compile |
| io.github.microsphere-projects:microsphere-spring-web | compile, optional |
| com.alibaba.csp:sentinel-core | compile, optional |
| org.springframework:spring-webmvc | compile, optional |
| javax.servlet:javax.servlet-api | provided |
| org.junit.jupiter:junit-jupiter | test |
| ch.qos.logback:logback-classic | test |
| io.github.microsphere-projects:microsphere-spring-webmvc | test |
| io.github.microsphere-projects:microsphere-spring-test | test |
| org.springframework:spring-test | test |
| org.skyscreamer:jsonassert | test |
| org.hamcrest:hamcrest | test |

## 扩展点

- **自定义资源命名**：通过子类覆盖 `buildResourceName(HandlerMethod, WebEndpointMapping)`
  （例如去掉 `HandlerMethod` 后缀），或通过 `initEntryCache` 替换整个映射。
- **排序**：`setOrder(int)` 调整拦截器在处理方法拦截器链中的位置；默认
  为 `DEFAULT_PRIORITY` 9。
- **上下文/来源**：`(contextName, origin)` 构造器；在 Spring Boot 中，
  `SentinelSpringWebAutoConfiguration` 在 `@ConditionalOnWebApplication(type = ANY)`、
  `@ConditionalOnSentinelAvailable` 和 `@ConditionalOnProperty(ENABLED_PROPERTY_NAME, matchIfMissing = true)` 之下，
  用 `@Bean @ConditionalOnMissingBean` 声明 `sentinelHandlerMethodInterceptor()`——注册你自己的
  bean 即可接管。
- **开关**：运行时调用 `setEnabled(false)`，或 `microsphere.sentinel.spring-web.enabled=false`
  （构造时的 JVM 系统属性）。
- **注册**：额外的 `HandlerMethodInterceptor` 提供者可以在同一个
  `META-INF/spring.factories` 键下发布。

## 相关模块

- 开发者指南索引：[README](./README.md)
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) — `AbstractSentinelPlugin`、
  `SentinelOperations`、`SentinelTemplate`、`SentinelContext`、`SentinelConstants`。
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) —
  `SentinelSpringWebAutoConfiguration`。
- [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) — 基于 Spring Cloud Alibaba Sentinel
  构建的 Cloud 级协调。
- 用户指南对应页面：[../../user-guide/1.x/microsphere-alibaba-sentinel-spring-web.md](../../../user-guide/1.x/zh/microsphere-alibaba-sentinel-spring-web.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring-web.md)
