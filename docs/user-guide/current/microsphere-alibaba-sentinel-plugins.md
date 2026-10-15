# microsphere-alibaba-sentinel-plugins

[中文版](zh/microsphere-alibaba-sentinel-plugins.md)

## Overview

`microsphere-alibaba-sentinel-plugins` is a Maven **aggregator POM** (`<packaging>pom</packaging>`) that builds the
six technology-specific Sentinel plugins shipped with this repository. Each child module adapts one framework's
extension point to the plugin abstraction defined in
[microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md): the framework's own SPI
(Druid `Filter`, MyBatis `ExecutorFilter`, Hibernate `EntityCallback`, P6Spy `JdbcEventListener`, a Redis
interceptor, a Spring Web interceptor) is implemented by a class that also implements `SentinelPlugin`, so every
call passing through that extension point is wrapped in a Sentinel context and entry.

| Child module | Plugin name | Default context name | Sentinel origin | Resource type | Extension point adapted |
| --- | --- | --- | --- | --- | --- |
| [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) | `alibaba-druid` | `microsphere_sentinel_alibaba_druid_context` | `Filter` | `COMMON_DB_SQL` | `com.alibaba.druid.filter.Filter` |
| [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) | `mybatis` | `microsphere_sentinel_mybatis_context` | `Executor` | `COMMON_DB_SQL` | `io.microsphere.mybatis.executor.ExecutorFilter` |
| [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md) | `hibernate` | `microsphere_sentinel_hibernate_context` | `SessionFactory` | `COMMON_DB_SQL` | `io.microsphere.hibernate.entity.EntityCallback` |
| [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md) | `p6spy` | `microsphere_sentinel_p6spy_context` | `Statement` | `COMMON_DB_SQL` | `com.p6spy.engine.event.JdbcEventListener` |
| [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md) | `redis` | `microsphere_sentinel_redis_context` | `RedisConnection` | `COMMON` (default) | `io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor` |
| [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md) | `spring-web` | `microsphere_sentinel_spring_web_context` | `HandlerMethod` | `COMMON_WEB` | `io.microsphere.spring.web.method.support.HandlerMethodInterceptor` |

Every child depends on `microsphere-alibaba-sentinel-commons` (compile scope) and declares the adapted framework
as `optional`, so a plugin never drags its host technology into an application that does not use it.

## Version Information

| Item | Value |
| --- | --- |
| Branch | `release` — documentation under `docs/user-guide/current/` |
| Maven coordinates | `io.github.microsphere-projects:microsphere-alibaba-sentinel-plugins:<version>` |
| Branch version | `0.2.6-SNAPSHOT` (`${revision}`), inherited by all six children |
| Released versions | `0.2.0`, `0.2.1`, `0.2.2`, `0.2.3`, `0.2.4`, `0.2.5` |
| Packaging | `pom` (aggregator only) |
| POM name | `Microsphere :: Alibaba Sentinel :: Plugins` |
| Java | 17 |

## Compatibility

The aggregator itself depends on nothing; compatibility is inherited per child, driven by the Microsphere BOMs
imported in `microsphere-alibaba-sentinel-parent`:

| Microsphere BOM | Version | Third party it aligns |
| --- | --- | --- |
| `microsphere-spring-cloud-dependencies` | 0.2.26 | Spring Cloud 2022.0.x – 2025.0.x, Spring Boot 3.x, Java 17 |
| `microsphere-alibaba-druid-dependencies` | 0.2.20 | Alibaba Druid (druid plugin) |
| `microsphere-mybatis-dependencies` | 0.2.16 | MyBatis (mybatis plugin) |
| `microsphere-hibernate-dependencies` | 0.2.10 | Hibernate (hibernate-core plugin) |
| `microsphere-redis-dependencies` | 0.2.14 | Spring Data Redis (redis plugin) |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | all six plugins |

Pick the `0.2.x` line for Java 17 / Spring Boot 3.x applications; the `0.1.x` line is the archived Java 8 /
Spring Boot 2.x generation, whose parent aligns with older Druid, MyBatis, Hibernate and Redis releases. Do not
mix a `0.1.x` plugin with a `0.2.x` commons module (or the reverse): the optional framework versions and the
`SentinelPlugin` contract both differ between the lines.

## Installation

Applications do not depend on this aggregator; they depend on the individual plugin artifact(s) they need. Import
the BOM so that all coordinates are version-managed:

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
    <!-- select one or more plugins -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-alibaba-druid</artifactId>
    </dependency>
    <!-- the adapted framework and sentinel-core are optional, declare them yourself -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.10</version>
    </dependency>
</dependencies>
```

Building all six plugins from source is what the aggregator is for:

```bash
./mvnw -pl microsphere-alibaba-sentinel-plugins -amd clean install
```

## API Reference

This module exposes no Java API of its own. It contains a single `pom.xml` with no `<dependencies>`, no source
directory, and nothing packaged — the build produces a POM artifact only. The API you actually program against
lives in the child modules and in commons:

- `io.microsphere.alibaba.sentinel.common.SentinelPlugin` — the contract every plugin implements (`getName()`,
  `getContextName()`, `getOrigin()`, `getResourceType()`, `getTrafficType()`, `isEnabled()`, `setEnabled(boolean)`).
- `io.microsphere.alibaba.sentinel.common.SentinelOperations` / `SentinelTemplate` — the wrapping the plugins call
  internally through `begin(...)` / `end(...)` or `call(...)`.
- Each child's own constants interface (for example `SentinelAlibabaDruidConstants`, `SentinelMyBatisConstants`)
  publishing `PLUGIN_NAME`, `DEFAULT_CONTEXT_NAME`, `DEFAULT_ORIGIN` and `ENABLED_PROPERTY_NAME`.

See the child documentation for constructor signatures and extension-point behavior.

## Configuration Properties

This module introduces no configuration properties. Each child declares its own
`microsphere.sentinel.<pluginName>.enabled` switch, resolved against the commons prefix
`microsphere.sentinel.` and enabled by default; consult the child pages for the exact property names.

## Usage Examples

The aggregator is not used from application code. The equivalent of "using" it is selecting plugins, which is a
build-time concern:

```xml
<!-- adapted from the <modules> list of microsphere-alibaba-sentinel-plugins/pom.xml -->
<!-- Enable SQL-layer protection only: druid + mybatis -->
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-alibaba-druid</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-mybatis</artifactId>
    </dependency>
</dependencies>
```

Once on the classpath, a plugin is discovered through its own SPI file (`META-INF/services/...`) or registered as
a Spring bean, and you can inspect or toggle it through the shared repository:

```java
// adapted from SentinelPluginRepositoryTest — the two plugins installed above are visible here
SentinelPluginRepository repository = SentinelPluginRepository.INSTANCE;

assertTrue(repository.isInstalled("alibaba-druid"));
SentinelPlugin myBatisPlugin = repository.get("mybatis");
if (myBatisPlugin != null) {
    myBatisPlugin.setEnabled(false);          // stop protecting MyBatis statements at runtime
}

repository.getAll().forEach(plugin -> System.out.println(
        plugin.getName() + " -> " + plugin.getContextName() + " [" + plugin.getOrigin() + "]"));
```

The same instances are registered as JMX MBeans under
`io.microsphere.sentinel:type=SentinelPlugin,name=alibaba-druid` and
`io.microsphere.sentinel:type=SentinelPlugin,name=mybatis`, because the commons SPI provider is
`JMXSentinelPluginRepository`; use any JMX client to flip `Enabled` without restarting.

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) — the API all plugins build on
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md)
- [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md)
- [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md)
- [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md)
- [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md)
- [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md)
- `microsphere-alibaba-sentinel-dependencies` — the BOM used in Installation
- [Branch index](./README.md) · [Repository root](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-plugins.md)
