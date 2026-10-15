# microsphere-alibaba-sentinel-spring

[中文版](zh/microsphere-alibaba-sentinel-spring.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-spring.md](../current/microsphere-alibaba-sentinel-spring.md).

## Overview

`microsphere-alibaba-sentinel-spring` is the plain-Spring distribution module of the
Microsphere Alibaba Sentinel project.

On the `release-1.x` branch this module is packaged as a `jar`, yet it contains **no main Java sources at
all**: the module directory holds only `pom.xml`. It is therefore a *dependency-aggregation* module.
Its purpose is to give a non-Boot Spring application a single coordinate that pulls in the Sentinel
extension model together with the Spring-side infrastructure that the Sentinel plugins intercept:

- the Sentinel plugin core API and runtime (`microsphere-alibaba-sentinel-commons`),
- the Microsphere Redis Spring support (`microsphere-redis-spring`), which the Redis Sentinel plugin
  builds on,
- the optional third-party integration libraries (`sentinel-core`, `spring-data-redis`,
  `spring-orm`, `hibernate-core`, `druid`, `mybatis`, `mybatis-spring`) that the aggregated code
  references, so that consumers do not have to align those versions by hand.

The module also serves as the foundation for
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md), which
depends on it and adds auto-configuration on top.

## Version Information

| Item                     | Value                                                    |
|--------------------------|----------------------------------------------------------|
| Branch                   | `release-1.x` (documentation directory: `1.x`)           |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring`                    |
| Packaging                | `jar` (no classes of its own; aggregation only)          |
| Version on this branch   | `0.1.6-SNAPSHOT`                                         |
| Released versions        | `0.1.0` - `0.1.5` (latest release: `0.1.5`)              |
| Java                     | 8                                                        |
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |
| Maven parent             | `microsphere-alibaba-sentinel-parent` `${revision}`      |

## Compatibility

| Consumed technology      | This line (`0.1.x`)                    | Current line (`0.2.x`)                 |
|--------------------------|----------------------------------------|----------------------------------------|
| Java                     | 8                                      | 17                                     |
| Spring Framework         | 5.x (javax namespace)                  | 6.x                                    |
| Spring Boot              | 2.x                                    | 3.x                                    |
| Spring Cloud             | Hoxton.SR12 - 2021.0.x                 | 2022.0.x - 2025.0.x                    |
| Alibaba Sentinel         | 1.8.10                                 | 1.8.10                                 |

Notes:

- This module registers **no** auto-configuration, so it does not itself require the Spring Boot 2.x
  `spring.factories` mechanism; it works in any Spring application context.
- The Spring Boot 2.x requirement applies to the modules layered on top of it,
  `microsphere-alibaba-sentinel-spring-boot` and `microsphere-alibaba-sentinel-spring-cloud`, which
  register their auto-configurations through `META-INF/spring.factories`. On the current `0.2.x` line
  the same auto-configurations are registered through `AutoConfiguration.imports` instead.
- The optional dependencies use the javax-era coordinates (`org.hibernate:hibernate-core` 5.x,
  `javax.servlet:javax.servlet-api`), which is another reason this line targets Spring Boot 2.x and Java 8.

## Installation

Add the coordinate directly (versions are managed by the project BOM or declared explicitly):

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring</artifactId>
    <version>0.1.5</version>
</dependency>
```

The recommended approach is to import
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) and omit
the version:

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
        <artifactId>microsphere-alibaba-sentinel-spring</artifactId>
    </dependency>
</dependencies>
```

Because every third-party dependency in this module is declared `optional`, you still add the
libraries you actually protect (for example `sentinel-core` and `spring-data-redis`); the module only
guarantees that their versions line up with what the aggregated code expects.

## API Reference

This module exposes **no API of its own** — it declares no classes, no resources, and no
`META-INF` service or auto-configuration registration files. Everything reachable from this artifact
is transitive:

| Pulled in (scope)        | What it contributes                                                        |
|--------------------------|----------------------------------------------------------------------------|
| `microsphere-alibaba-sentinel-commons` (compile) | The Sentinel extension model: `SentinelPlugin`, `AbstractSentinelPlugin`, `SimpleSentinelPlugin`, `SentinelPluginRepository`, `SentinelOperations`/`SentinelTemplate`, `SentinelContext`, `SentinelConstants`, `SentinelUtils`, `SentinelMetricsRepository` and the node-event classes. Documented in [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md). |
| `microsphere-redis-spring` (compile, managed)    | The Redis interception contracts (`RedisConnectionInterceptor` and friends) that the Redis plugin implements. |
| `com.alibaba.csp:sentinel-core` (optional)       | Sentinel runtime (`SphU`, `ContextUtil`, resource type constants).          |
| `org.springframework.data:spring-data-redis` (optional) | `RedisConnection` and the Redis template infrastructure.            |
| `org.springframework:spring-orm` (optional)      | Hibernate/JPA Spring integration classes.                                   |
| `org.hibernate:hibernate-core` (optional)    | Hibernate `SessionFactory` and entity lifecycle APIs.                       |
| `com.alibaba:druid` (optional)                   | Druid `Filter`/`StatementProxy` APIs used by the Druid plugin.              |
| `org.mybatis:mybatis`, `org.mybatis:mybatis-spring` (optional) | MyBatis `Executor` APIs used by the MyBatis plugin.         |

To use the Sentinel plugin API with auto-configuration, depend on
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) instead of
this module.

## Configuration Properties

This module introduces no configuration properties of its own. The `microsphere.sentinel.*` keys are
defined and read by the `microsphere-alibaba-sentinel-commons` code it aggregates — see
[Configuration Properties in the Spring Boot module](./microsphere-alibaba-sentinel-spring-boot.md)
for the full list.

## Usage Examples

### Registering a Sentinel plugin in a plain Spring context

```java
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SimpleSentinelPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.microsphere.alibaba.sentinel.common.SentinelPluginRepository.INSTANCE;
// adapted from io.microsphere.alibaba.sentinel.common.SimpleSentinelPluginTest
@Configuration
public class SentinelPluginConfiguration {

    @Bean
    public SentinelPlugin orderPlugin() {
        // AbstractSentinelPlugin installs the plugin into SentinelPluginRepository.INSTANCE
        // and uninstalls it on JVM shutdown
        return new SimpleSentinelPlugin("order-service");
    }

    @Bean
    public String installedPluginNames() {
        // INSTANCE.isInstalled("order-service") is true once the bean above is created
        return INSTANCE.getAll().stream()
                .map(SentinelPlugin::getName)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
```

### Guarding business calls with the aggregated Sentinel template

```java
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_ORIGIN;

// adapted from io.microsphere.alibaba.sentinel.common.SentinelTemplateTest
SentinelOperations sentinelOperations = new SentinelTemplate(ResourceTypeConstants.COMMON);
sentinelOperations.execute("OrderService.create", DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN, () -> {
    // business logic flows through a Sentinel entry here
});
```

### Verifying that a plugin context exists

```java
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;

// adapted from io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAutoConfigurationTest
Context context = ContextUtil.enter("microsphere_sentinel_alibaba_druid_context", "Filter");
assert context != null;
```

## Related Modules

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) — the API this
  module distributes.
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) —
  depends on this module and adds auto-configuration.
- [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) —
  Spring Cloud feature reporting on top of the Boot module.
- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) — Maven parent with
  the managed versions used above.
- [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) — BOM
  that manages this artifact.
- Branch index: [README](./README.md) — repository overview:
  [README](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-spring.md)
