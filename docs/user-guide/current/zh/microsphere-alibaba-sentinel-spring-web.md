# microsphere-alibaba-sentinel-spring-web

[English](../microsphere-alibaba-sentinel-spring-web.md)

## 概述

`microsphere-alibaba-sentinel-spring-web` 使用 Alibaba Sentinel 保护 Spring WebMVC 的 handler 方法。它贡献了
`io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`，这是 **microsphere-spring-web** 的
`HandlerMethodInterceptor`（`io.microsphere.spring.web.method.support.HandlerMethodInterceptor`）：它在 handler 方法执行之前开启一个
Sentinel entry，并在执行结束后关闭它，使用端点映射表达式作为资源名。

以下是从源码验证过的关键事实：

- 插件名（`SentinelSpringWebConstants.PLUGIN_NAME`）：`spring-web`。
- 上下文名：`microsphere_sentinel_spring_web_context`（`SentinelSpringWebConstants.DEFAULT_CONTEXT_NAME`，即
  `SentinelUtils.getDefaultContextName(String)` 的 `microsphere_sentinel_{}_context` 模式）。
- 来源（origin）：`HandlerMethod`（`SentinelSpringWebConstants.DEFAULT_ORIGIN`）。
- 资源类型：`ResourceTypeConstants.COMMON_WEB`（`1`）；流量类型 `EntryType.IN` —— 构造函数调用的是四参数的
  `AbstractSentinelPlugin(String, String, String, int)`，该构造方式会把 entry 类型默认设为 `IN`。
- 自动安装：作为 `AbstractSentinelPlugin` 的直接子类，其构造函数会把插件安装到 `SentinelPluginRepository` 服务中，同时注册 MBean
  `io.microsphere.sentinel:type=SentinelPlugin,name=spring-web`。
- Spring bean 名称常量：`SentinelHandlerMethodInterceptor.BEAN_NAME = "sentinelHandlerMethodInterceptor"`。
- 与 Spring 的注册方式：`META-INF/spring.factories` 声明了
  `io.microsphere.spring.web.method.support.HandlerMethodInterceptor=io.microsphere.alibaba.sentinel.spring.web.SentinelHandlerMethodInterceptor`，
  当启用 web 扩展时，microsphere-spring-web 的 `WebExtensionBeanDefinitionRegistrar` 会把它转换为 bean。
- 顺序：`getOrder()` 默认返回 `SentinelConstants.DEFAULT_PRIORITY`（`9`），并且 `setOrder(int)` 是 public 方法。

## 版本信息

| 项目 | 取值 |
|------|-------|
| 文档对应分支 | `release`（本文件位于 `docs/user-guide/current`） |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-spring-web` |
| 分支版本 | `0.2.6-SNAPSHOT` |
| 已发布版本 | `0.2.0` - `0.2.5` |
| Java 版本 | 17 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| Spring Cloud / Spring Boot | 2022.0.x - 2025.0.x / 3.x（默认构建 profile：2025.0.3 / 3.5.15） |

## 兼容性

| 技术 | 状态 | 依据 |
|------------|--------|----------|
| Spring WebMVC 6.x（`org.springframework:spring-webmvc`） | 必需，声明为 `optional` | 从 Jakarta 时代的包中导入 `HandlerMethod`、`NativeWebRequest` |
| `jakarta.servlet:jakarta.servlet-api` | `provided` | 模块 POM 中的依赖 |
| microsphere-spring-web（`io.github.microsphere-projects:microsphere-spring-web`） | 必需，声明为 `optional` | 实现 `HandlerMethodInterceptor`，监听 `WebEndpointMappingsReadyEvent`，使用 `WebEndpointMapping` 与 `WebScope` |
| Spring Boot | 为 servlet 与 reactive 应用提供自动配置 | `SentinelSpringWebAutoConfiguration` 标注了 `@ConditionalOnWebApplication(type = ANY)` |
| Spring Cloud Alibaba Sentinel | 可共存 | 该自动配置通过 `@AutoConfigureAfter` 排在 `com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration` 之后 |

父 POM 还带有 `spring-cloud-2025.1` profile（Spring Boot 4.x）；release 分支的文档覆盖 Spring Boot 3.x / Spring Cloud 2022-2025
这一版本线。

## 安装

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

在 Spring Boot 下不需要任何额外配置。在普通的 WebMVC 上下文中，需要添加 `@EnableWebMvcExtension(registerHandlerInterceptors =
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

- 资源名在 microsphere-spring-web 发布 `WebEndpointMappingsReadyEvent` 时准备好：`initEntryCache` 会遍历
  `WebEndpointMapping` 集合，为每个 `HandlerMethod` 保存形如
  `webEndpointMapping.toExpression() + "#" + handlerMethod` 的名称，例如
  `GET "/test/greeting/{message}"#GET io.microsphere...TestController.greeting(String)`。
- 对于不属于已发布端点映射的 handler 方法，`getResourceName(HandlerMethod)` 会退回到 `spring-web:<handlerMethod>`
  （即 `computeIfAbsent` 的默认值）。
- `SentinelContext` 通过 `WebScope.REQUEST` 以请求作用域属性 `sentinel-context`
  （`SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME`）的形式保存；`afterExecute` 先向其写入返回值与异常，然后由
  `SentinelTemplate.end(...)` 把异常上报给 `Tracer` 并退出 entry。
- 当请求中不存在 `SentinelContext` 时，`afterExecute` 会静默返回（打印 trace 日志），这就是为什么在没有前置
  `beforeExecute` 的情况下调用它也是安全的。`getSentinelContext` 虽然标注了 `@Nonnull`，但此时确实会返回 `null`；该注解只起文档说明作用。
- 当 `isEnabled()` 为 `false` 时，两个钩子都会被跳过；由 `begin` 抛出的 `BlockException` 会向上传播到 DispatcherServlet。

## 配置属性

| 属性 | 默认值 | 来源 | 作用 |
|----------|---------|--------|--------|
| `microsphere.sentinel.enabled` | `true` | 系统属性、应用配置 | 通过 `@ConditionalOnSentinelEnabled` 决定 `SentinelSpringWebAutoConfiguration` 是否生效 |
| `microsphere.sentinel.spring-web.enabled` | `true` | 系统属性、应用配置 | 决定自动配置是否生效（`@ConditionalOnProperty(matchIfMissing = true)`），同时决定运行时的 `isEnabled()` 取值 |

注意插件属性名中的连字符：它派生自插件名 `spring-web`，而 Sentinel 上下文名使用下划线形式
`microsphere_sentinel_spring_web_context`。运行时检查只在构造时读取一次 JVM 系统属性
（`SentinelUtils.isPluginEnabled("spring-web")`）；此后请使用 `setEnabled(boolean)`、`enable()`、`disable()`
或 `SentinelPlugin` MBean。

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

### 验证受保护端点的行为

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
- [用户指南索引](./README.md) - 当前分支的全部模块。
- [项目根目录](../../../../README.md) - 仓库总览。

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring-web.md)
