# microsphere-alibaba-sentinel-spring-web

[English](../microsphere-alibaba-sentinel-spring-web.md)

> **已归档的 `1.x` 分支。** 本页记录 `0.1.x` 构件（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支一致，仅基线不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。本页在主干分支上的对应文档是 [../current/zh/microsphere-alibaba-sentinel-spring-web.md](../../current/zh/microsphere-alibaba-sentinel-spring-web.md)。

## 概述

`microsphere-alibaba-sentinel-spring-web` 使用 Alibaba Sentinel 保护 Spring WebMVC 的处理器方法。它提供了
`io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`，这是 **microsphere-spring-web**
（`io.microsphere.spring.web.method.support.HandlerMethodInterceptor`）的一个 `HandlerMethodInterceptor` 实现，
它在处理器方法执行之前打开一个 Sentinel entry，并在执行之后将其关闭，资源名使用端点映射表达式。

从源码核实的关键事实：

- 插件名（`SentinelSpringWebConstants.PLUGIN_NAME`）：`spring-web`。
- 上下文名：`microsphere_sentinel_spring_web_context`（`SentinelSpringWebConstants.DEFAULT_CONTEXT_NAME`，即
  `SentinelUtils.getDefaultContextName(String)` 的 `microsphere_sentinel_{}_context` 命名模式）。
- 来源（origin）：`HandlerMethod`（`SentinelSpringWebConstants.DEFAULT_ORIGIN`）。
- 资源类型：`ResourceTypeConstants.COMMON_WEB`（`1`）；流量类型 `EntryType.IN`——构造函数调用四参数的
  `AbstractSentinelPlugin(String, String, String, int)`，该构造器将入口类型默认置为 `IN`。
- 自动安装：作为 `AbstractSentinelPlugin` 的直接子类，其构造函数会把插件安装进 `SentinelPluginRepository` 服务，
  同时注册 MBean `io.microsphere.sentinel:type=SentinelPlugin,name=spring-web`。
- Spring bean 名常量：`SentinelHandlerMethodInterceptor.BEAN_NAME = "sentinelHandlerMethodInterceptor"`。
- 与 Spring 的集成方式：`META-INF/spring.factories` 声明了
  `io.microsphere.spring.web.method.support.HandlerMethodInterceptor=io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`，
  当 web 扩展被启用时，microsphere-spring-web 的 `WebExtensionBeanDefinitionRegistrar` 会把它转换为 bean。
- 顺序：`getOrder()` 默认返回 `SentinelConstants.DEFAULT_PRIORITY`（`9`），`setOrder(int)` 是 public 方法。

## 版本信息

| 项目 | 值 |
|------|-------|
| 文档所属分支 | `release-1.x`（本文件位于 `docs/user-guide/1.x`） |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-spring-web` |
| 分支版本 | `0.1.6-SNAPSHOT` |
| 已发布版本 | `0.1.0` - `0.1.5` |
| Java 版本 | 8 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| Spring Cloud / Spring Boot | Hoxton.SR12 - 2021.0.x / 2.x（默认构建 profile：2021.0.9 / 2.6.15） |

## 兼容性

| 技术 | 状态 | 依据 |
|------------|--------|----------|
| Spring WebMVC 5.x（`org.springframework:spring-webmvc`） | 必需，声明为 `optional` | 从 javax 时代的包中导入 `HandlerMethod`、`NativeWebRequest` |
| `javax.servlet:javax.servlet-api` | `provided` | 模块 POM 中的依赖声明 |
| microsphere-spring-web（`io.github.microsphere-projects:microsphere-spring-web`） | 必需，声明为 `optional` | 实现 `HandlerMethodInterceptor`，监听 `WebEndpointMappingsReadyEvent`，使用 `WebEndpointMapping` 与 `WebScope` |
| Spring Boot | 为 servlet 与 reactive 应用提供自动配置 | `SentinelSpringWebAutoConfiguration` 标注了 `@ConditionalOnWebApplication(type = ANY)` |
| Spring Cloud Alibaba Sentinel | 可共存 | 自动配置通过 `@AutoConfigureAfter` 排在 `com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration` 之后 |

`0.1.x` 的父 POM 链提供三个构建 profile：`spring-cloud-hoxton`（Spring Boot 2.3.12.RELEASE、Spring
Cloud Hoxton.SR12）、`spring-cloud-2020`（Spring Boot 2.4.13、Spring Cloud 2020.0.5）与 `spring-cloud-2021`
（Spring Boot 2.6.15、Spring Cloud 2021.0.9），其中后者默认激活。

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

在 Spring Boot 下无需任何其他配置。在纯 WebMVC 上下文中，请添加 `@EnableWebMvcExtension(registerHandlerInterceptors =
true)`（或 `@EnableWebExtension`），这样 `spring.factories` 中声明的 `HandlerMethodInterceptor` 才会被注册。

## API 参考

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

行为细节：

- 资源名在 microsphere-spring-web 发布 `WebEndpointMappingsReadyEvent` 时准备就绪：
  `initEntryCache` 会遍历 `WebEndpointMapping` 集合，为每个 `HandlerMethod` 存储形如
  `webEndpointMapping.toExpression() + "#" + handlerMethod` 的名称，例如
  `GET "/test/greeting/{message}"#GET io.microsphere...TestController.greeting(String)`。
- 对于未包含在已发布端点映射中的处理器方法，`getResourceName(HandlerMethod)` 会退回到
  `spring-web:<handlerMethod>`（即 `computeIfAbsent` 的默认值）。
- `SentinelContext` 通过 `WebScope.REQUEST` 作为请求级属性 `sentinel-context`
  （`SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME`）存储；`afterExecute` 会先把结果与异常设置到其中，
  再由 `SentinelTemplate.end(...)` 把异常上报给 `Tracer` 并退出。
- 当请求中不存在 `SentinelContext` 时，`afterExecute` 会静默返回（trace 日志），因此在不曾调用
  `beforeExecute` 的情况下调用它是安全的。`getSentinelContext` 标注了 `@Nonnull`，但此时确实会返回 `null`；
  该注解仅用于文档说明。
- 当 `isEnabled()` 为 `false` 时，两个钩子都会被跳过；`begin` 抛出的 `BlockException` 会向上传递到
  DispatcherServlet。

## 配置属性

| 属性 | 默认值 | 来源 | 作用 |
|----------|---------|--------|--------|
| `microsphere.sentinel.enabled` | `true` | 系统属性、应用配置 | 通过 `@ConditionalOnSentinelEnabled` 决定 `SentinelSpringWebAutoConfiguration` 是否生效 |
| `microsphere.sentinel.spring-web.enabled` | `true` | 系统属性、应用配置 | 决定自动配置是否生效（`@ConditionalOnProperty(matchIfMissing = true)`），并决定运行期的 `isEnabled()` 取值 |

注意插件属性名中的连字符：它派生自插件名 `spring-web`，而 Sentinel 上下文名使用下划线形式
`microsphere_sentinel_spring_web_context`。运行时检查只在构造时读取一次 JVM 系统属性
（`SentinelUtils.isPluginEnabled("spring-web")`）；此后请使用 `setEnabled(boolean)`、`enable()`、
`disable()` 或 `SentinelPlugin` MBean。

## 使用示例

### 在 WebMVC 测试上下文中注册拦截器

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

### 访问受保护的端点

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

### 关闭插件

```java
// adapted from SentinelHandlerMethodInterceptorTest#testDisabled
interceptor.setEnabled(false);
assertFalse(interceptor.isEnabled());
test();                    // requests still succeed, no entries are recorded
interceptor.setEnabled(true);
assertTrue(interceptor.isEnabled());
```

### 在没有 Sentinel 上下文的情况下调用拦截器

```java
// adapted from SentinelHandlerMethodInterceptorTest#testAfterExecuteWithoutSentinelContext
MockServletWebRequest request = new MockServletWebRequest();
this.interceptor.afterExecute(null, null, null, null, request);   // trace log, no exception
assertNull(getSentinelContext(request));
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `AbstractSentinelPlugin`、
  `SentinelOperations`、`SentinelTemplate`、`SentinelContext`、`SentinelUtils`。
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) -
  `SentinelSpringWebAutoConfiguration` 及其条件注解。
- [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md) - 覆盖 Spring
  Data Redis 命令的同级插件。
- [用户手册索引](./README.md) - 本分支的全部模块。
- [项目根目录](../../../../README.md) - 仓库总览。

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring-web.md)
