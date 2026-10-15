# microsphere-alibaba-sentinel-spring-boot

[中文版](zh/microsphere-alibaba-sentinel-spring-boot.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-spring-boot.md](../current/microsphere-alibaba-sentinel-spring-boot.md).

## Overview

`microsphere-alibaba-sentinel-spring-boot` is the Spring Boot distribution of the Microsphere Alibaba
Sentinel project. It contains **auto-configuration only** — no business API — and turns the individual
Sentinel plugins into beans that are registered when, and only when, the matching technology is
present on the classpath and Sentinel is not explicitly disabled.

The module registers four auto-configuration classes through the Spring Boot 2.x
`spring.factories` mechanism:

| Auto-configuration class                | Plugin bean it registers            | Technology protected |
|-----------------------------------------|-------------------------------------|----------------------|
| `SentinelAlibabaDruidAutoConfiguration` | `SentinelAlibabaDruidFilter`        | Alibaba Druid `DataSource` / statements |
| `SentinelMyBatisAutoConfiguration`      | `SentinelMyBatisExecutorFilter`     | MyBatis `Executor` |
| `SentinelRedisAutoConfiguration`        | `SentinelRedisCommandInterceptor`   | Spring Data Redis `RedisConnection` |
| `SentinelSpringWebAutoConfiguration`    | `SentinelHandlerMethodInterceptor`  | Spring Web MVC and WebFlux handler methods |

All classes live in the `io.microsphere.alibaba.sentinel.spring.boot.autoconfigure` package and each
bean method is annotated `@ConditionalOnMissingBean`, so a manually declared bean always wins.

## Version Information

| Item                     | Value                                                    |
|--------------------------|----------------------------------------------------------|
| Branch                   | `release-1.x` (documentation directory: `1.x`)           |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring-boot`               |
| Packaging                | `jar`                                                    |
| Version on this branch   | `0.1.6-SNAPSHOT`                                         |
| Released versions        | `0.1.0` - `0.1.5` (latest release: `0.1.5`)              |
| Java                     | 8                                                        |
| Spring Boot              | 2.x (`spring-boot-autoconfigure`, `spring-boot-actuator`)|
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |

## Compatibility

This module **requires Spring Boot 2.x**, because its auto-configurations are registered in
`META-INF/spring.factories` under the `EnableAutoConfiguration` key — the mechanism Spring Boot 3.x dropped in
favour of `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. A Boot 3 application
that puts this jar on its classpath loads no Sentinel auto-configuration at all.

| Consumed technology  | This line (`0.1.x`)         | Current line (`0.2.x`)          |
|----------------------|-----------------------------|---------------------------------|
| Java                 | 8                           | 17                              |
| Spring Boot          | 2.x                         | 3.x                             |
| Spring Cloud         | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| Registration file    | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel     | 1.8.10                      | 1.8.10                          |

Stay on this `0.1.x` line while the application runs Java 8 with Spring Boot 2.x; move to the current `0.2.x` line
as soon as it runs Java 17 with Spring Boot 3.x.

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring-boot</artifactId>
</dependency>
```

Import [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) to
manage that version:

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

This artifact transitively brings in `microsphere-alibaba-sentinel-spring`, the Sentinel plugin
modules (`alibaba-druid`, `hibernate-core`, `mybatis`, `redis`, `spring-web`) and
`microsphere-spring-boot-core`. Everything connecting a plugin to a real starter is `optional` and is
yours to add: `microsphere-alibaba-druid-spring-boot`, `microsphere-mybatis-spring-boot`,
`microsphere-redis-spring-boot`, `microsphere-spring-boot-webmvc` or
`microsphere-spring-boot-webflux`, plus `sentinel-core` and the technology you protect.

## API Reference

The public API is limited to four auto-configuration classes and two condition annotations in
`io.microsphere.alibaba.sentinel.spring.boot.condition`:

- **`@ConditionalOnSentinelEnabled`** — meta-annotated
  `@ConditionalOnProperty(name = "microsphere.sentinel.enabled", matchIfMissing = true)`. The property
  is resolved from the Spring `Environment`; missing means enabled.
- **`@ConditionalOnSentinelAvailable`** — combines `@ConditionalOnSentinelEnabled` with
  `@ConditionalOnClass(name = {"com.alibaba.csp.sentinel.SphU", "io.microsphere.alibaba.sentinel.common.SentinelPlugin"})`.
  A component is registered only when Sentinel is enabled **and** both `sentinel-core` and the
  Microsphere commons API are on the classpath.
- **`SentinelAlibabaDruidAutoConfiguration`** — class-level
  `@ConditionalOnSentinelAvailable`, `@ConditionalOnProperty(name = SentinelAlibabaDruidConstants.ENABLED_PROPERTY_NAME, matchIfMissing = true)`
  and `@ConditionalOnClass` on the Druid Microsphere condition type plus `SentinelAlibabaDruidFilter`.
  The nested `Config` class is guarded by `@ConditionalOnAlibabaDruidAvailable`. It runs after Spring
  Cloud Alibaba `SentinelAutoConfiguration` and `AlibabaDruidAutoConfiguration`.
- **`SentinelMyBatisAutoConfiguration`** — additionally guarded by `@ConditionalOnMyBatisAvailable`;
  the nested `Config` class is annotated `@EnableMyBatisExtension`, which installs the executor filter
  into MyBatis. It runs after `MybatisAutoConfiguration`, `MybatisPlusAutoConfiguration` and
  Microsphere `MyBatisAutoConfiguration`.
- **`SentinelRedisAutoConfiguration`** — guarded by `@ConditionalOnRedisAvailable`; the nested `Config`
  class is guarded by `@ConditionalOnRedisInterceptorEnabled`. It runs after Spring Boot
  `RedisAutoConfiguration`.
- **`SentinelSpringWebAutoConfiguration`** — guarded by `@ConditionalOnWebApplication(type = ANY)` in
  addition to the Sentinel conditions and `@ConditionalOnClass` on `HandlerMethod`,
  `HandlerMethodInterceptor` and `SentinelHandlerMethodInterceptor`. It declares its bean directly on
  the auto-configuration class (no nested `Config`), and runs after the Microsphere WebMvc and WebFlux
  auto-configurations.

Two plugins are intentionally **not** auto-configured here:

- **Hibernate** — `SentinelHibernateEntityCallback` is registered as a Java `ServiceLoader` provider
  (`META-INF/services/io.microsphere.hibernate.entity.EntityCallback`) inside
  `microsphere-alibaba-sentinel-hibernate-core` and is activated by the
  `microsphere-hibernate` Spring Boot module.
- **P6Spy** — `SentinelJdbcEventListener` is registered through
  `META-INF/services/com.p6spy.engine.event.JdbcEventListener` and is configured in `spy.properties`;
  this module has no P6Spy auto-configuration at all.

## Configuration Properties

| Property                                        | Type    | Default | Effect                                                                      |
|-------------------------------------------------|---------|---------|-----------------------------------------------------------------------------|
| `microsphere.sentinel.enabled`                  | boolean | `true`  | Master switch for every Sentinel auto-configuration (`SentinelConstants.ENABLED_PROPERTY_NAME`). |
| `microsphere.sentinel.alibaba-druid.enabled`    | boolean | `true`  | Switches the Druid plugin bean.                                             |
| `microsphere.sentinel.mybatis.enabled`          | boolean | `true`  | Switches the MyBatis plugin bean.                                           |
| `microsphere.sentinel.redis.enabled`            | boolean | `true`  | Switches the Redis plugin bean.                                             |
| `microsphere.sentinel.spring-web.enabled`       | boolean | `true`  | Switches the Spring Web plugin bean.                                        |
| `microsphere.sentinel.hibernate.enabled`        | boolean | `true`  | Read by the Hibernate SPI callback, not by an auto-configuration here.      |
| `microsphere.sentinel.p6spy.enabled`            | boolean | `true`  | Read by the P6Spy event listener, not by an auto-configuration here.        |

The prefix `microsphere.sentinel.` comes from `SentinelConstants.PROPERTY_NAME_PREFIX`; each per-plugin
key is assembled by `SentinelUtils.getPluginEnabledPropertyName(pluginName)` (prefix + plugin name +
`.enabled`) and published as `<Plugin>Constants.ENABLED_PROPERTY_NAME`. At run time
`SentinelPlugin#isEnabled()` reads the same key as a **Java system property** (`false`, ignoring case,
disables the plugin), so a plugin reacts both to the Spring `Environment` and to `-D...` properties.

## Usage Examples

### Asserting a plugin bean is auto-configured (and can be switched off)

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

### The web plugin only applies to a web application

```java
// adapted from SentinelSpringWebAutoConfigurationTest
new ApplicationContextRunner()
        .withConfiguration(of(SentinelSpringWebAutoConfiguration.class))
        .run(context -> assertThat(context).doesNotHaveBean(SentinelHandlerMethodInterceptor.class));

new WebApplicationContextRunner()
        .withConfiguration(of(SentinelSpringWebAutoConfiguration.class))
        .run(context -> assertThat(context).hasSingleBean(SentinelHandlerMethodInterceptor.class));
```

### Conditions evaluated when removing the Druid plugin

```java
// adapted from SentinelAlibabaDruidAutoConfigurationTest
// missing class -> no bean (FilteredClassLoader("com.alibaba.druid.pool.DruidDataSource"))
// microsphere.sentinel.alibaba-druid.enabled=false -> no bean
// microsphere.sentinel.enabled=false -> no bean
// microsphere.sentinel.alibaba-druid.enabled=true (default) + DruidDataSource present -> bean
```

### End-to-end boot test

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

## Related Modules

- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md) — compile
  dependency, provides the plugin API.
- [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) —
  depends on this module and adds Spring Cloud feature reporting.
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) — where the
  property constants are defined.
- Plugin modules: [alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md),
  [mybatis](./microsphere-alibaba-sentinel-mybatis.md),
  [hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md),
  [p6spy](./microsphere-alibaba-sentinel-p6spy.md), [redis](./microsphere-alibaba-sentinel-redis.md),
  [spring-web](./microsphere-alibaba-sentinel-spring-web.md) — the beans created above.
- Branch index: [README](./README.md) — repository overview:
  [README](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-spring-boot.md)
