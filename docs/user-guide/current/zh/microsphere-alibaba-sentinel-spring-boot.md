# microsphere-alibaba-sentinel-spring-boot

[English](../microsphere-alibaba-sentinel-spring-boot.md)

## 概述

`microsphere-alibaba-sentinel-spring-boot` 是 Microsphere Alibaba Sentinel 项目的 Spring Boot 分发模块。它**只包含自动配置** ——
不含业务 API —— 并把一个个 Sentinel 插件变成 bean，当且仅当对应技术存在于 classpath 上、并且 Sentinel 没有被显式关闭时才注册它们。

该模块通过 Spring Boot 3.x 的 `AutoConfiguration.imports` 机制注册四个自动配置类：

| 自动配置类                              | 所注册的插件 bean                        | 保护的技术                    |
|-----------------------------------------|-------------------------------------|----------------------|
| `SentinelAlibabaDruidAutoConfiguration` | `SentinelAlibabaDruidFilter`        | Alibaba Druid `DataSource` / 语句 |
| `SentinelMyBatisAutoConfiguration`      | `SentinelMyBatisExecutorFilter`     | MyBatis `Executor` |
| `SentinelRedisAutoConfiguration`        | `SentinelRedisCommandInterceptor`   | Spring Data Redis `RedisConnection` |
| `SentinelSpringWebAutoConfiguration`    | `SentinelHandlerMethodInterceptor`  | Spring Web MVC 与 WebFlux 的 handler 方法 |

这些类都位于 `io.microsphere.alibaba.sentinel.spring.boot.autoconfigure` 包中，并且每个 bean 方法都标注了
`@ConditionalOnMissingBean`，因此手工声明的bean总是优先。

## 版本信息

| 项目                     | 取值                                                     |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release`（文档目录：`current`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring-boot`               |
| 打包方式                 | `jar`                                                    |
| 本分支版本               | `0.2.6-SNAPSHOT`                                         |
| 已发布版本               | `0.2.0` - `0.2.5`（最新发布版本：`0.2.5`）                 |
| Java                     | 17                                                       |
| Spring Boot              | 3.x（`spring-boot-autoconfigure`、`spring-boot-actuator`）|
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |

## 兼容性

本模块**要求 Spring Boot 3.x**，因为它的自动配置只在
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中注册。Spring Boot 2.x
读取的是 `META-INF/spring.factories`，因而会完全忽略本模块。

| 所消费的技术           | 本分支（`0.2.x`）           | 历史版本线（`0.1.x`）     |
|----------------------|---------------------------|-----------------------------|
| Java                 | 17 及以上                  | 更早的基线                   |
| Spring Boot          | 3.x                       | 2.x                         |
| Spring Cloud         | 2022.0.x - 2025.0.x       | Hoxton - 2021.0.x           |
| 注册文件              | `AutoConfiguration.imports` | `META-INF/spring.factories` |
| Alibaba Sentinel     | 1.8.10                    | 1.8.x                       |

如果你无法迁移到 Spring Boot 3.x 与 Java 17，请留在 `0.1.x` 版本线。

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring-boot</artifactId>
</dependency>
```

导入 [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md)
以管理该版本：

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
```

该构件会传递引入 `microsphere-alibaba-sentinel-spring`、各个 Sentinel 插件模块（`alibaba-druid`、`hibernate-core`、
`mybatis`、`redis`、`spring-web`）以及 `microsphere-spring-boot-core`。所有把插件与真实 starter 连接起来的依赖都是
`optional` 的，需要你自己添加：`microsphere-alibaba-druid-spring-boot`、`microsphere-mybatis-spring-boot`、
`microsphere-redis-spring-boot`、`microsphere-spring-boot-webmvc` 或 `microsphere-spring-boot-webflux`，
再加上 `sentinel-core` 以及你要保护的技术组件。

## API 参考

公共 API 仅限于四个自动配置类，以及 `io.microsphere.alibaba.sentinel.spring.boot.condition` 中的两个条件注解：

- **`@ConditionalOnSentinelEnabled`** —— 作为元注解使用
  `@ConditionalOnProperty(name = "microsphere.sentinel.enabled", matchIfMissing = true)`。该属性从 Spring
  `Environment` 解析；不存在即视为启用。
- **`@ConditionalOnSentinelAvailable`** —— 组合了 `@ConditionalOnSentinelEnabled` 与
  `@ConditionalOnClass(name = {"com.alibaba.csp.sentinel.SphU", "io.microsphere.alibaba.sentinel.common.SentinelPlugin"})`。
  只有当 Sentinel 启用**并且** `sentinel-core` 与 Microsphere commons API 都在 classpath 上时，组件才会被注册。
- **`SentinelAlibabaDruidAutoConfiguration`** —— 类级别标注了
  `@ConditionalOnSentinelAvailable`、`@ConditionalOnProperty(name = SentinelAlibabaDruidConstants.ENABLED_PROPERTY_NAME, matchIfMissing = true)`，
  以及针对 Druid 的 Microsphere 条件类型加 `SentinelAlibabaDruidFilter` 的 `@ConditionalOnClass`。
  内部嵌套的 `Config` 类由 `@ConditionalOnAlibabaDruidAvailable` 守护。它在 Spring Cloud Alibaba 的
  `SentinelAutoConfiguration` 与 `AlibabaDruidAutoConfiguration` 之后运行。
- **`SentinelMyBatisAutoConfiguration`** —— 额外由 `@ConditionalOnMyBatisAvailable` 守护；内部嵌套的 `Config`
  类标注了 `@EnableMyBatisExtension`，该注解会把 executor 过滤器安装进 MyBatis。它在
  `MybatisAutoConfiguration`、`MybatisPlusAutoConfiguration` 以及 Microsphere 的 `MyBatisAutoConfiguration` 之后运行。
- **`SentinelRedisAutoConfiguration`** —— 由 `@ConditionalOnRedisAvailable` 守护；内部嵌套的 `Config` 类由
  `@ConditionalOnRedisInterceptorEnabled` 守护。它在 Spring Boot 的 `RedisAutoConfiguration` 之后运行。
- **`SentinelSpringWebAutoConfiguration`** —— 除 Sentinel 相关条件以及针对 `HandlerMethod`、
  `HandlerMethodInterceptor` 和 `SentinelHandlerMethodInterceptor` 的 `@ConditionalOnClass` 之外，还由
  `@ConditionalOnWebApplication(type = ANY)` 守护。它直接在自动配置类上声明自己的 bean（没有嵌套 `Config`），并在
  Microsphere 的 WebMvc 与 WebFlux 自动配置之后运行。

有两个插件是**有意**不在这里做自动配置的：

- **Hibernate** —— `SentinelHibernateEntityCallback` 在 `microsphere-alibaba-sentinel-hibernate-core` 内以 Java
  `ServiceLoader` 提供者的方式注册（`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`），并由
  `microsphere-hibernate` Spring Boot 模块激活。
- **P6Spy** —— `SentinelJdbcEventListener` 通过
  `META-INF/services/com.p6spy.engine.event.JdbcEventListener` 注册，并在 `spy.properties` 中配置；本模块完全没有
  P6Spy 自动配置。

## 配置属性

| 属性                                            | 类型    | 默认值 | 作用                                                                       |
|-------------------------------------------------|---------|---------|-----------------------------------------------------------------------------|
| `microsphere.sentinel.enabled`                  | boolean | `true`  | 所有 Sentinel 自动配置的总开关（`SentinelConstants.ENABLED_PROPERTY_NAME`）。 |
| `microsphere.sentinel.alibaba-druid.enabled`    | boolean | `true`  | 控制 Druid 插件 bean。                                                      |
| `microsphere.sentinel.mybatis.enabled`          | boolean | `true`  | 控制 MyBatis 插件 bean。                                                    |
| `microsphere.sentinel.redis.enabled`            | boolean | `true`  | 控制 Redis 插件 bean。                                                      |
| `microsphere.sentinel.spring-web.enabled`       | boolean | `true`  | 控制 Spring Web 插件 bean。                                                 |
| `microsphere.sentinel.hibernate.enabled`        | boolean | `true`  | 由 Hibernate SPI 回调读取，而非由这里的自动配置读取。                         |
| `microsphere.sentinel.p6spy.enabled`            | boolean | `true`  | 由 P6Spy 事件监听器读取，而非由这里的自动配置读取。                            |

前缀 `microsphere.sentinel.` 来自 `SentinelConstants.PROPERTY_NAME_PREFIX`；每个插件级键由
`SentinelUtils.getPluginEnabledPropertyName(pluginName)`（前缀 + 插件名 + `.enabled`）拼装而成，并作为
`<Plugin>Constants.ENABLED_PROPERTY_NAME` 对外暴露。在运行时，`SentinelPlugin#isEnabled()` 会把同一个键当作
**Java 系统属性**读取（值为 `false`，忽略大小写，即禁用该插件），因此插件既对 Spring `Environment` 作出响应，也对 `-D...` 属性作出响应。

## 使用示例

### 断言插件 bean 被自动配置（并且可以被关闭）

```java
import io.microsphere.alibaba.sentinel.redis.spring.SentinelRedisCommandInterceptor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

// adapted from SentinelRedisAutoConfigurationTest and the AutoConfigurationTest base class
ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(of(SentinelRedisAutoConfiguration.class));

// defaults: the plugin bean is present
runner.run(context -> assertThat(context).hasSingleBean(SentinelRedisCommandInterceptor.class));

// the per-plugin switch, the underlying Redis switch, or the master switch all remove the bean
for (String property : new String[] {"microsphere.sentinel.redis.enabled=false",
        "microsphere.redis.enabled=false", "microsphere.sentinel.enabled=false"}) {
    runner.withPropertyValues(property)
            .run(context -> assertThat(context).doesNotHaveBean(SentinelRedisCommandInterceptor.class));
}
```

### Web 插件仅适用于 Web 应用

```java
// adapted from SentinelSpringWebAutoConfigurationTest
new ApplicationContextRunner()
        .withConfiguration(of(SentinelSpringWebAutoConfiguration.class))
        .run(context -> assertThat(context).doesNotHaveBean(SentinelHandlerMethodInterceptor.class));

new WebApplicationContextRunner()
        .withConfiguration(of(SentinelSpringWebAutoConfiguration.class))
        .run(context -> assertThat(context).hasSingleBean(SentinelHandlerMethodInterceptor.class));
```

### 移除 Druid 插件时被评估的条件

```java
// adapted from SentinelAlibabaDruidAutoConfigurationTest
// missing class -> no bean (FilteredClassLoader("com.alibaba.druid.pool.DruidDataSource"))
// microsphere.sentinel.alibaba-druid.enabled=false -> no bean
// microsphere.sentinel.enabled=false -> no bean
// microsphere.sentinel.alibaba-druid.enabled=true (default) + DruidDataSource present -> bean
```

### 端到端启动测试

```java
@SpringBootTest(classes = {MyBatisDataBaseTestConfiguration.class, RedisConfig.class},
        webEnvironment = RANDOM_PORT,
        properties = {"microsphere.redis.enabled=true", "spring.application.name=test-app"})
@EnableAutoConfiguration
class SentinelAutoConfigurationTest {

    @Autowired
    private SentinelRedisCommandInterceptor interceptor;

    @Autowired
    private SqlSessionFactory sessionFactory;

    // adapted from SentinelAutoConfigurationTest
    @Test
    void test() {
        Configuration configuration = sessionFactory.getConfiguration();
        SqlSession sqlSession = sessionFactory.openSession();
        assertUserMapper(configuration.getMapper(UserMapper.class, sqlSession));
        assertNotNull(ContextUtil.enter("microsphere_sentinel_alibaba_druid_context", "Filter"));
        assertNotNull(ContextUtil.enter("microsphere_sentinel_mybatis_context", "Executor"));
        assertNotNull(ContextUtil.enter("microsphere_sentinel_redis_context", "RedisConnection"));
    }
}
```

## 相关模块

- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md) —— compile
  依赖，提供插件 API。
- [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) ——
  依赖本模块并添加 Spring Cloud 特性上报。
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— 属性常量
  的定义之处。
- 插件模块：[alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md)、
  [mybatis](./microsphere-alibaba-sentinel-mybatis.md)、
  [hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md)、
  [p6spy](./microsphere-alibaba-sentinel-p6spy.md)、[redis](./microsphere-alibaba-sentinel-redis.md)、
  [spring-web](./microsphere-alibaba-sentinel-spring-web.md) —— 即上文所创建的各个 bean。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring-boot.md)
