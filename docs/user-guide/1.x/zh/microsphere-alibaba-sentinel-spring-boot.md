# microsphere-alibaba-sentinel-spring-boot

[English](../microsphere-alibaba-sentinel-spring-boot.md)

> **已归档的 `1.x` 分支。** 本页记录 `0.1.x` 构件（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支一致，仅基线不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。本页在主干分支上的对应文档是 [../current/zh/microsphere-alibaba-sentinel-spring-boot.md](../../current/zh/microsphere-alibaba-sentinel-spring-boot.md)。

## 概述

`microsphere-alibaba-sentinel-spring-boot` 是 Microsphere Alibaba Sentinel 项目的 Spring Boot 分发模块。
它**只包含自动配置** —— 不含业务 API —— 并把各个 Sentinel 插件转变为 bean，仅在
相称的技术已存在于 classpath 上且 Sentinel 未被显式关闭时才注册它们。

本模块通过 Spring Boot 2.x 的 `spring.factories` 机制注册四个自动配置类：

| 自动配置类                              | 注册的插件 bean                        | 所保护的技术               |
|-----------------------------------------|-------------------------------------|----------------------|
| `SentinelAlibabaDruidAutoConfiguration` | `SentinelAlibabaDruidFilter`        | Alibaba Druid `DataSource` / 语句 |
| `SentinelMyBatisAutoConfiguration`      | `SentinelMyBatisExecutorFilter`     | MyBatis `Executor` |
| `SentinelRedisAutoConfiguration`        | `SentinelRedisCommandInterceptor`   | Spring Data Redis `RedisConnection` |
| `SentinelSpringWebAutoConfiguration`    | `SentinelHandlerMethodInterceptor`  | Spring Web MVC 与 WebFlux 处理器方法 |

这些类都位于 `io.microsphere.alibaba.sentinel.spring.boot.autoconfigure` 包中，并且每个
bean 方法都标注了 `@ConditionalOnMissingBean`，因此手工声明的 bean 总是优先生效。

## 版本信息

| 项目                     | 值                                                       |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release-1.x`（文档目录：`1.x`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring-boot`               |
| 打包方式                 | `jar`                                                    |
| 本分支版本               | `0.1.6-SNAPSHOT`                                         |
| 已发布版本               | `0.1.0` - `0.1.5`（最新发布版：`0.1.5`）                  |
| Java                     | 8                                                        |
| Spring Boot              | 2.x（`spring-boot-autoconfigure`、`spring-boot-actuator`）|
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |

## 兼容性

本模块**要求 Spring Boot 2.x**，因为它的自动配置注册在
`META-INF/spring.factories` 的 `EnableAutoConfiguration` 键之下 —— 而 Spring Boot 3.x 已放弃该机制，
改用 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。
如果把该 jar 放到 Boot 3 应用的 classpath 上，它不会加载任何 Sentinel 自动配置。

| 所依赖的技术         | 本分支（`0.1.x`）           | 当前分支（`0.2.x`）             |
|----------------------|-----------------------------|---------------------------------|
| Java                 | 8                           | 17                              |
| Spring Boot          | 2.x                         | 3.x                             |
| Spring Cloud         | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| 注册文件             | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel     | 1.8.10                      | 1.8.10                          |

当应用运行在 Java 8 + Spring Boot 2.x 时请停留在本 `0.1.x` 分支；一旦应用升级到
Java 17 + Spring Boot 3.x，就应迁移到当前的 `0.2.x` 分支。

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring-boot</artifactId>
</dependency>
```

导入 [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) 以
统一管理该版本：

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
```

该构件会传递引入 `microsphere-alibaba-sentinel-spring`、各个 Sentinel 插件模块
（`alibaba-druid`、`hibernate-core`、`mybatis`、`redis`、`spring-web`）以及
`microsphere-spring-boot-core`。所有把插件与真实 starter 关联起来的依赖都是 `optional` 的，
需要你自己添加：`microsphere-alibaba-druid-spring-boot`、`microsphere-mybatis-spring-boot`、
`microsphere-redis-spring-boot`、`microsphere-spring-boot-webmvc` 或
`microsphere-spring-boot-webflux`，再加上 `sentinel-core` 以及你要保护的技术组件。

## API 参考

公开 API 仅限于四个自动配置类，以及 `io.microsphere.alibaba.sentinel.spring.boot.condition` 中的
两个条件注解：

- **`@ConditionalOnSentinelEnabled`** —— 元标注了
  `@ConditionalOnProperty(name = "microsphere.sentinel.enabled", matchIfMissing = true)`。该属性
  从 Spring `Environment` 解析；未配置即表示启用。
- **`@ConditionalOnSentinelAvailable`** —— 将 `@ConditionalOnSentinelEnabled` 与
  `@ConditionalOnClass(name = {"com.alibaba.csp.sentinel.SphU", "io.microsphere.alibaba.sentinel.common.SentinelPlugin"})`
  组合起来。只有当 Sentinel 已启用**并且** `sentinel-core` 与 Microsphere commons API 都在 classpath 上时，
  组件才会被注册。
- **`SentinelAlibabaDruidAutoConfiguration`** —— 类级别上有
  `@ConditionalOnSentinelAvailable`、`@ConditionalOnProperty(name = SentinelAlibabaDruidConstants.ENABLED_PROPERTY_NAME, matchIfMissing = true)`，
  并对 Druid 的 Microsphere 条件类型加上 `SentinelAlibabaDruidFilter` 做了 `@ConditionalOnClass`。
  其嵌套的 `Config` 类由 `@ConditionalOnAlibabaDruidAvailable` 把关。它排在 Spring
  Cloud Alibaba 的 `SentinelAutoConfiguration` 与 `AlibabaDruidAutoConfiguration` 之后。
- **`SentinelMyBatisAutoConfiguration`** —— 额外由 `@ConditionalOnMyBatisAvailable` 把关；
  嵌套的 `Config` 类标注了 `@EnableMyBatisExtension`，它负责把 executor 过滤器装配进 MyBatis。
  它排在 `MybatisAutoConfiguration`、`MybatisPlusAutoConfiguration` 以及 Microsphere 的
  `MyBatisAutoConfiguration` 之后。
- **`SentinelRedisAutoConfiguration`** —— 由 `@ConditionalOnRedisAvailable` 把关；嵌套的 `Config`
  类由 `@ConditionalOnRedisInterceptorEnabled` 把关。它排在 Spring Boot 的
  `RedisAutoConfiguration` 之后。
- **`SentinelSpringWebAutoConfiguration`** —— 除 Sentinel 相关条件以及针对 `HandlerMethod`、
  `HandlerMethodInterceptor` 与 `SentinelHandlerMethodInterceptor` 的 `@ConditionalOnClass` 之外，
  还由 `@ConditionalOnWebApplication(type = ANY)` 把关。它把 bean 直接声明在自动配置类上
  （没有嵌套的 `Config`），并排在 Microsphere 的 WebMvc 与 WebFlux 自动配置之后。

有两个插件是**有意**不做自动配置的：

- **Hibernate** —— `SentinelHibernateEntityCallback` 在
  `microsphere-alibaba-sentinel-hibernate-core` 中作为 Java `ServiceLoader` 提供者注册
  （`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`），并由
  `microsphere-hibernate` Spring Boot 模块激活。
- **P6Spy** —— `SentinelJdbcEventListener` 通过
  `META-INF/services/com.p6spy.engine.event.JdbcEventListener` 注册，并在 `spy.properties` 中配置；
  本模块完全不包含 P6Spy 的自动配置。

## 配置属性

| 属性                                            | 类型    | 默认值 | 作用                                                                       |
|-------------------------------------------------|---------|---------|-----------------------------------------------------------------------------|
| `microsphere.sentinel.enabled`                  | boolean | `true`  | 所有 Sentinel 自动配置的总开关（`SentinelConstants.ENABLED_PROPERTY_NAME`）。 |
| `microsphere.sentinel.alibaba-druid.enabled`    | boolean | `true`  | 开关 Druid 插件 bean。                                                      |
| `microsphere.sentinel.mybatis.enabled`          | boolean | `true`  | 开关 MyBatis 插件 bean。                                                    |
| `microsphere.sentinel.redis.enabled`            | boolean | `true`  | 开关 Redis 插件 bean。                                                      |
| `microsphere.sentinel.spring-web.enabled`       | boolean | `true`  | 开关 Spring Web 插件 bean。                                                 |
| `microsphere.sentinel.hibernate.enabled`        | boolean | `true`  | 由 Hibernate SPI 回调读取，本模块的自动配置并不读取它。                       |
| `microsphere.sentinel.p6spy.enabled`            | boolean | `true`  | 由 P6Spy 事件监听器读取，本模块的自动配置并不读取它。                         |

前缀 `microsphere.sentinel.` 来自 `SentinelConstants.PROPERTY_NAME_PREFIX`；每个按插件区分的
键由 `SentinelUtils.getPluginEnabledPropertyName(pluginName)`（前缀 + 插件名 +
`.enabled`）拼装而成，并作为 `<Plugin>Constants.ENABLED_PROPERTY_NAME` 对外暴露。在运行期
`SentinelPlugin#isEnabled()` 会把同一个键当作 **Java 系统属性**读取（值为 `false` 时忽略大小写，
即关闭插件），因此插件既响应 Spring `Environment`，也响应 `-D...` 属性。

## 使用示例

### 断言插件 bean 被自动配置（且可以被关闭）

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

### Web 插件仅对 web 应用生效

```java
// adapted from SentinelSpringWebAutoConfigurationTest
new ApplicationContextRunner()
        .withConfiguration(of(SentinelSpringWebAutoConfiguration.class))
        .run(context -> assertThat(context).doesNotHaveBean(SentinelHandlerMethodInterceptor.class));

new WebApplicationContextRunner()
        .withConfiguration(of(SentinelSpringWebAutoConfiguration.class))
        .run(context -> assertThat(context).hasSingleBean(SentinelHandlerMethodInterceptor.class));
```

### 移除 Druid 插件时被求值的条件

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
  依赖本模块并追加 Spring Cloud 特性上报。
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— 属性常量
  的定义之处。
- 插件模块：[alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md)、
  [mybatis](./microsphere-alibaba-sentinel-mybatis.md)、
  [hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md)、
  [p6spy](./microsphere-alibaba-sentinel-p6spy.md)、[redis](./microsphere-alibaba-sentinel-redis.md)、
  [spring-web](./microsphere-alibaba-sentinel-spring-web.md) —— 上文所创建的 bean。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring-boot.md)
