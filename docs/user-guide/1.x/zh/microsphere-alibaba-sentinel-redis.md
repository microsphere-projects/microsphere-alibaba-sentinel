# microsphere-alibaba-sentinel-redis

[English](../microsphere-alibaba-sentinel-redis.md)

> **已归档的 `1.x` 分支线。** 本页文档描述 `0.1.x` 制品（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支线一致，仅基线版本不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。当前分支线对应的文档是 [../current/zh/microsphere-alibaba-sentinel-redis.md](../../current/zh/microsphere-alibaba-sentinel-redis.md)。

## 概述

`microsphere-alibaba-sentinel-redis` 使用 Alibaba Sentinel 保护 Spring Data Redis 命令。它贡献
`io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor`，这是
**microsphere-redis** 的一个 `RedisConnectionInterceptor`
（`io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor`），
它把每一次被拦截的 `RedisConnection` 方法调用包装进一个 Sentinel entry。microsphere-redis 通过包装
`RedisTemplate` / `StringRedisTemplate`（在测试配置中默认由 Lettuce 支撑）来实现拦截，
因此应用代码保持不变。

从源码核实的关键事实：

- 插件名称（`SentinelRedisConstants.PLUGIN_NAME`）：`redis`。
- 上下文名称：`microsphere_sentinel_redis_context`（`SentinelRedisConstants.DEFAULT_CONTEXT_NAME`，
  即 `SentinelUtils.getDefaultContextName(String)` 的 `microsphere_sentinel_{}_context` 模式）。
- 来源：`RedisConnection`（`SentinelRedisConstants.DEFAULT_ORIGIN`）。
- 资源类型：`ResourceTypeConstants.COMMON`（`0`），流量类型 `EntryType.IN`。该类调用三参数的
  `AbstractSentinelPlugin(String, String, String)` 构造器，由它应用这些默认值；Sentinel 1.8.10
  没有定义专用的 Redis 类型（只有 `COMMON`、`COMMON_WEB`、`COMMON_RPC`、`COMMON_API_GATEWAY`、`COMMON_DB_SQL`）。
- 资源名：`SentinelUtils.buildResourceName(Method)` —— `<简单声明类名>.<方法名>(<简单参数类型>)`，
  例如 `RedisStringCommands.set(byte[],byte[])`。
- 自动安装：该类继承 `AbstractSentinelPlugin`，其构造器会调用 `SentinelPlugin.install(this)`
  （插件被注册进 `SentinelPluginRepository` 服务，并暴露为 MBean
  `io.microsphere.sentinel:type=SentinelPlugin,name=redis`）。
- 与 Spring 的注册：本 jar 中的 `META-INF/spring.factories` 声明了
  `io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor=io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor`。
- 拦截器顺序：`getOrder()` 返回 `SentinelConstants.DEFAULT_PRIORITY`（`9`）。

## 版本信息

| 项目 | 值 |
|------|-------|
| 文档所描述的分支 | `release-1.x`（本文件位于 `docs/user-guide/1.x`） |
| Group ID | `io.github.microsphere-projects` |
| Artifact ID | `microsphere-alibaba-sentinel-redis` |
| 分支版本 | `0.1.6-SNAPSHOT` |
| 已发布版本 | `0.1.0` - `0.1.5` |
| Java 版本 | 8 |
| `com.alibaba.csp:sentinel-core` | 1.8.10 |
| `microsphere-redis` BOM | 0.1.13 |
| Spring Boot / Spring Cloud | 2.x / Hoxton.SR12 - 2021.0.x |

## 兼容性

| 技术 | 状态 | 证据 |
|------------|--------|----------|
| Spring Data Redis（`org.springframework.data:spring-data-redis`） | 必需，声明为 `optional` | import 了 `RedisConnection`、`RedisClusterConnection` |
| microsphere-redis（`io.github.microsphere-projects:microsphere-redis-spring`） | 必需，声明为 `optional` | 实现 `RedisConnectionInterceptor`，使用 `RedisMethodContext` |
| microsphere-spring-context | 声明为 `optional` | 模块 POM 中的依赖 |
| Redis 客户端 | Lettuce | 测试作用域中的 `io.lettuce:lettuce-core`；拦截器位于客户端之上 |
| Spring Boot | 已自动配置 | `SentinelRedisAutoConfiguration` 注册 `SentinelRedisCommandInterceptor` bean |
| 命名空间 | javax / Spring 5 基线 | Java 8，加上父 POM 的 Spring Cloud Hoxton-2021 profiles |

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

在纯 Spring（非 Boot）上下文中，请引入拦截器 bean，并对需要保护的模板启用 microsphere-redis 拦截
（`@EnableRedisInterceptor`）。在 Spring Boot 下，当 Sentinel、Redis 与拦截器模块三者齐备时，
`SentinelRedisAutoConfiguration` 会创建该 bean。

## API 参考

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

从 `AbstractSentinelPlugin` / `SentinelPlugin` 继承而来：
`getName()`、`getContextName()`、`getOrigin()`、`getResourceType()`、`getTrafficType()`、`isAutoInstalled()`、
`isEnabled()`、`setEnabled(boolean)`、`enable()`、`disable()`。

行为细节：

- `afterPropertiesSet()` 会遍历 `ClassUtils.getAllInterfacesForClass(RedisClusterConnection.class, classLoader)`，
  并为每个接口方法缓存 `buildResourceName(method)`（通过 `MapUtils.newFixedHashMap(512)` 预先按 512 个条目
  分配容量的 map）。
- `beforeExecute` 在该缓存中查找被调用的方法。如果方法未知 —— 例如来自某个非 Redis 接口的方法 ——
  它会记录一条 trace 级消息并直接返回，不打开 entry。
- `SentinelContext` 在两个阶段之间通过 `RedisMethodContext` 的属性 `sentinel-context`
  （`SentinelConstants.SENTINEL_CONTEXT_ATTRIBUTE_NAME`）传递，而不是放在 `ThreadLocal` 中，
  因此嵌套命令保持一致。
- `afterExecute` 会在 `SentinelTemplate.end(...)` 之前把 `result` 与 `failure` 记录到上下文；非限流类失败会上报给
  `Tracer` 并馈入基于异常的规则。
- 如果 `afterPropertiesSet()` 从未被调用，缓存将保持为空，也就没有任何命令被拦截；由 `begin` 抛出的
  `BlockException` 会从 Redis 调用中传播出来。

## 配置属性

| 属性 | 默认值 | 来源 | 效果 |
|----------|---------|--------|--------|
| `microsphere.sentinel.enabled` | `true` | 系统属性、应用配置 | 通过 `@ConditionalOnSentinelEnabled` 约束 `SentinelRedisAutoConfiguration` |
| `microsphere.sentinel.redis.enabled` | `true` | 系统属性、应用配置 | 约束自动配置（`@ConditionalOnProperty(matchIfMissing = true)`）以及运行时的 `isEnabled()` 判断 |
| `microsphere.redis.enabled` | - | 应用配置 | microsphere-redis 开关；必须为 `true` 拦截才会发生 |
| `microsphere.redis.interceptor.enabled` | - | 应用配置 | microsphere-redis 对自动配置内部 `Config` 类的约束 |

运行时判断 `SentinelUtils.isPluginEnabled("redis")` 只读取 JVM 系统属性
（`!"false".equalsIgnoreCase(value)`），并且在拦截器构造时求值一次；此后请使用 `setEnabled(false)`、
`disable()` 或 JMX MBean 来变更。

## 使用示例

### 在纯 Spring 上下文中注册拦截器

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

### 保护某条命令并断言 Sentinel 指标

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

### 关闭插件

```java
// adapted from SentinelRedisCommandInterceptorTest#testDisabled
interceptor.setEnabled(false);
assertFalse(interceptor.isEnabled());
test();                     // commands still succeed, no Sentinel entry recorded
interceptor.setEnabled(true);
assertTrue(interceptor.isEnabled());
```

### 不被拦截的方法

```java
// adapted from SentinelRedisCommandInterceptorTest#testResourceNotFound
// A method that is not part of the RedisClusterConnection interface hierarchy is skipped
Method method = getClass().getMethods()[0];
RedisMethodContext context =
        new RedisMethodContext(stringRedisTemplate, method, ofArray(), redisContext);
assertDoesNotThrow(() -> interceptor.beforeExecute(context));
assertDoesNotThrow(() -> interceptor.afterExecute(context, null, null));
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) - `AbstractSentinelPlugin`、
  `SentinelOperations`、`SentinelTemplate`、`SentinelContext`、`SentinelUtils`。
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) -
  `SentinelRedisAutoConfiguration` 及其条件。
- [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md) - 使用
  `COMMON_WEB` 资源类型保护 Web 入口的兄弟插件。
- [用户指南索引](./README.md) - 本分支的全部模块。
- [项目根目录](../../../../README.md) - 仓库总览。

---

## English

[English 版本](../microsphere-alibaba-sentinel-redis.md)
