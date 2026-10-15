# microsphere-alibaba-sentinel-spring-cloud

[中文版](zh/microsphere-alibaba-sentinel-spring-cloud.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-spring-cloud.md](../current/microsphere-alibaba-sentinel-spring-cloud.md).

## Overview

`microsphere-alibaba-sentinel-spring-cloud` adapts the Sentinel Spring Boot auto-configurations to a
Spring Cloud application. It contributes exactly one thing: a Spring Cloud `HasFeatures` bean that
publishes the Sentinel plugins installed in the current JVM on the `/actuator/features` endpoint.

The module contains a single auto-configuration class,
`io.microsphere.alibaba.sentinel.spring.cloud.SentinelCloudAutoConfiguration`, registered through
`META-INF/spring.factories`. Its nested
`FeaturesConfig` class walks the Sentinel plugin repository and reports each plugin that is enabled as
a `NamedFeature`, while plugins that are installed but disabled are reported as abstract features.

It is the top of the layering: `commons` -> `spring` -> `spring-boot` -> `spring-cloud`.

## Version Information

| Item                     | Value                                                    |
|--------------------------|----------------------------------------------------------|
| Branch                   | `release-1.x` (documentation directory: `1.x`)           |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring-cloud`              |
| Packaging                | `jar`                                                    |
| Version on this branch   | `0.1.6-SNAPSHOT`                                         |
| Released versions        | `0.1.0` - `0.1.5` (latest release: `0.1.5`)              |
| Java                     | 8                                                        |
| Spring Boot              | 2.x                                                      |
| Spring Cloud             | Hoxton.SR12 - 2021.0.x (`spring-cloud-commons`, `spring-cloud-context`) |
| Microsphere Spring Cloud | BOM 0.1.26                                               |
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |

## Compatibility

Both Spring Boot 2.x **and** a matching Spring Cloud release train are required:

| Consumed technology  | This line (`0.1.x`)         | Current line (`0.2.x`)          |
|----------------------|-----------------------------|---------------------------------|
| Java                 | 8                           | 17                              |
| Spring Boot          | 2.x                         | 3.x                             |
| Spring Cloud         | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| Registration file    | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel     | 1.8.10                      | 1.8.10                          |

The Spring Boot 2.x requirement is not incidental: this module ships a single
`META-INF/spring.factories` file holding the `EnableAutoConfiguration` key, which Spring Boot 3.x no longer reads.
On the current `0.2.x` line the same `SentinelCloudAutoConfiguration` is registered through
`AutoConfiguration.imports` and pairs with Spring Cloud 2022.0.x through 2025.0.x.

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring-cloud</artifactId>
</dependency>
```

With the project BOM imported (see
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md)) no
version is needed. The artifact transitively provides
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) and
`microsphere-spring-cloud-commons`; the Spring Cloud and technology-specific partners
(`spring-cloud-commons`, `spring-cloud-context`, `spring-boot-actuator`, `sentinel-core`,
`microsphere-alibaba-druid-spring-cloud`, `microsphere-mybatis-spring-cloud`,
`microsphere-redis-spring-cloud`, `microsphere-hibernate-core`, the WebMvc/WebFlux starters) are all
declared `optional` and come from your own Spring Cloud BOM.

## API Reference

`SentinelCloudAutoConfiguration` (since `1.0.0`):

- **Class conditions** — `@ConditionalOnSentinelAvailable`, i.e. `microsphere.sentinel.enabled` must
  not resolve to `false` in the Spring `Environment` **and** both `com.alibaba.csp.sentinel.SphU` and
  `io.microsphere.alibaba.sentinel.common.SentinelPlugin` must be loadable.
- **Ordering** — `@AutoConfigureAfter` (by name, so absent classes are simply skipped) places it after
  Spring Cloud Alibaba `SentinelAutoConfiguration`, the Microsphere Druid/MyBatis/Redis Spring Cloud
  auto-configurations, `ConfigurationPropertyHasFeaturesAutoConfiguration`, and all four Sentinel
  Spring Boot auto-configurations. This guarantees the plugin beans exist before features are
  collected.
- **`FeaturesConfig`** — nested class, guarded by
  `io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesAvailable`, imported by the
  auto-configuration. It declares one bean, named by the constant
  `SentinelCloudAutoConfiguration.FeaturesConfig.BEAN_NAME` = `alibaba-sentinel.features`:

```java
// the feature collection performed by FeaturesConfig#features()
SentinelPluginRepository repository = SentinelPluginRepository.INSTANCE;
for (SentinelPlugin plugin : repository.getAll()) {
    if (plugin.isEnabled()) {
        namedFeatures.add(new NamedFeature(plugin.getName(), plugin.getClass()));
    } else {
        abstractFeatures.add(plugin.getClass());
    }
}
HasFeatures hasFeatures = new HasFeatures(abstractFeatures, namedFeatures);
```

There is **no** bootstrap-context participation, no Spring Cloud Config integration, and no
refresh-related behaviour in this module: it declares no bootstrap registration, no `RefreshScope`
bean, and no `RefreshEvent`/`EnvironmentChangeEvent` listener. A context refresh simply re-evaluates
the ordinary auto-configuration conditions.

## Configuration Properties

| Property                          | Type    | Default | Effect                                                                       |
|-----------------------------------|---------|---------|------------------------------------------------------------------------------|
| `microsphere.sentinel.enabled`    | boolean | `true`  | Removes the auto-configuration (and therefore the features bean) when `false`. |
| `spring.application.name`         | string  | -       | Not read by this module; required by the Spring Cloud actuator setup used in practice. |
| `management.endpoints.web.exposure.include` | string | - | Must include `features` to expose `/actuator/features`. |

Per-plugin keys (`microsphere.sentinel.<plugin>.enabled`) are documented in
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md); they
control whether a plugin bean is created, which in turn decides whether the plugin appears as a named
feature.

## Usage Examples

### Verifying the features bean with `ApplicationContextRunner`

```java
import org.springframework.cloud.client.actuator.HasFeatures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

// adapted from SentinelCloudAutoConfigurationTest
new ApplicationContextRunner()
        .withConfiguration(of(SentinelCloudAutoConfiguration.class))
        .withPropertyValues("spring.application.name=test-app")
        .run(context -> assertThat(context)
                .hasSingleBean(SentinelCloudAutoConfiguration.class)
                .hasBean("alibaba-sentinel.features")
                .hasSingleBean(HasFeatures.class));
```

### Boot test reading the Spring Cloud `FeaturesEndpoint`

```java
@SpringBootTest(classes = SentinelCloudAutoConfigurationTest.class,
        webEnvironment = NONE,
        properties = {
                "spring.application.name=test-app",
                "management.endpoints.web.exposure.include=features"
        })
@EnableAutoConfiguration
class SentinelCloudAutoConfigurationTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    // adapted from SentinelCloudAutoConfigurationTest
    @Test
    void test() {
        assertEquals("alibaba-sentinel.features", SentinelCloudAutoConfiguration.FeaturesConfig.BEAN_NAME);
        assertNotNull(hasFeaturesMap.get("alibaba-sentinel.features"));
        assertNotNull(featuresEndpoint.features());
    }
}
```

### Exposing the endpoint over HTTP

With `spring-boot-starter-actuator` and a Spring Cloud context on the classpath, start the
application and call:

```bash
curl http://localhost:8080/actuator/features
```

The `alibaba-sentinel` entries list one named feature per enabled Sentinel plugin — for example
`alibaba-druid`, `mybatis`, `redis`, `spring-web` — as reported by the plugin repository.

## Related Modules

- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) — the
  auto-configurations this module orders itself after.
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md) — aggregation layer
  beneath the Boot module.
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —
  `SentinelPlugin` and `SentinelPluginRepository` used to collect features.
- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) — manages the
  Microsphere Spring Cloud BOM version.
- Branch index: [README](./README.md) — repository overview:
  [README](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-spring-cloud.md)
