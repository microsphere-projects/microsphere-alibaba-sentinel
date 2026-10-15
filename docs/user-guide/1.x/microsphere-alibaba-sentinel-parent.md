# microsphere-alibaba-sentinel-parent

[中文版](zh/microsphere-alibaba-sentinel-parent.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-parent.md](../current/microsphere-alibaba-sentinel-parent.md).

## Overview

`microsphere-alibaba-sentinel-parent` is a `pom`-packaged Maven parent shared by every module of the
Microsphere Alibaba Sentinel project. It carries no code and no resources; it exists to:

- fix the third-party and Microsphere BOM versions that the project builds against,
- import the upstream Microsphere dependency BOMs so that module POMs can declare dependencies without
  versions,
- act as the single `${revision}`-based parent for
  `microsphere-alibaba-sentinel-commons`, the six plugin modules,
  `microsphere-alibaba-sentinel-spring`, `microsphere-alibaba-sentinel-spring-boot`,
  `microsphere-alibaba-sentinel-spring-cloud` and
  `microsphere-alibaba-sentinel-dependencies`.

Its own parent is the repository aggregator `microsphere-alibaba-sentinel` (`../pom.xml`), which in
turn inherits `io.github.microsphere-projects:microsphere-spring-cloud-parent:0.1.26` — the source of
the Java 8 compiler settings and the build plugin configuration.

## Version Information

| Item                     | Value                                                       |
|--------------------------|-------------------------------------------------------------|
| Branch                   | `release-1.x` (documentation directory: `1.x`)              |
| Group ID                 | `io.github.microsphere-projects`                            |
| Artifact ID              | `microsphere-alibaba-sentinel-parent`                       |
| Packaging                | `pom`                                                       |
| Version on this branch   | `0.1.6-SNAPSHOT` (`${revision}`)                            |
| Released versions        | `0.1.0` - `0.1.5` (latest release: `0.1.5`)                 |
| Java                     | 8                                                           |
| Maven parent             | `microsphere-alibaba-sentinel` `${revision}` -> `microsphere-spring-cloud-parent` 0.1.26 |

## Compatibility

| Consumed technology        | This line (`0.1.x`)         | Current line (`0.2.x`)          |
|----------------------------|-----------------------------|---------------------------------|
| Java                       | 8 (enforced by the parent)  | 17                              |
| Spring Boot                | 2.x                         | 3.x                             |
| Spring Cloud               | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| Registration file          | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel           | 1.8.10                      | 1.8.10                          |

This line still registers auto-configurations through `META-INF/spring.factories`; the current line moved them to
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, which Spring Boot 3.x reads and
Spring Boot 2.x ignores. That is why the `spring-boot` and `spring-cloud` modules built through this parent require
Spring Boot 2.x. A POM has no runtime of its own: this parent is inert, and the versions below are what constrain
your classpath.

## Installation

This artifact is consumed as a parent, not as a dependency:

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-parent</artifactId>
    <version>0.1.5</version>
</parent>
```

Applications that only want the version alignment (the usual case) should import
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) instead.

### Dependency Management

| `<properties>` entry               | Managed dependency                                                       | Version  | Kind          |
|------------------------------------|--------------------------------------------------------------------------|----------|---------------|
| `microsphere-spring-cloud.version` | `io.github.microsphere-projects:microsphere-spring-cloud-dependencies`   | `0.1.26` | `pom`, import |
| `microsphere-alibaba-druid.version`| `io.github.microsphere-projects:microsphere-alibaba-druid-dependencies`  | `0.1.20` | `pom`, import |
| `microsphere-mybatis.version`      | `io.github.microsphere-projects:microsphere-mybatis-dependencies`        | `0.1.15` | `pom`, import |
| `microsphere-hibernate.version`    | `io.github.microsphere-projects:microsphere-hibernate-dependencies`      | `0.1.9` | `pom`, import |
| `microsphere-redis.version`        | `io.github.microsphere-projects:microsphere-redis-dependencies`          | `0.1.13` | `pom`, import |
| `sentinel.version`                 | `com.alibaba.csp:sentinel-core`                                          | `1.8.10` | jar           |
| `sentinel.version`                 | `com.alibaba.csp:sentinel-transport-simple-http`                         | `1.8.10` | jar           |
| `fastjson.version`                 | `com.alibaba:fastjson`                                                   | `2.0.64` | jar           |

The five imports are ordinary BOMs, so the effective versions of partner artifacts such as
`microsphere-redis-spring` and `microsphere-mybatis-spring-boot` are resolved from them rather than
from this POM's `<properties>`. The project's own feature artifacts are not managed here — that is the
BOM's job.

## API Reference

None. This module is POM-only (`<packaging>pom</packaging>`) and publishes no Java classes, no
resources, and no `META-INF` registration files.

## Configuration Properties

This module introduces no configuration properties.

## Usage Examples

### Building a custom Sentinel extension module on this parent

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-parent</artifactId>
    <version>0.1.5</version>
    <relativePath/>
</parent>

<dependencies>
    <!-- versionless: sentinel.version resolves to 1.8.10 -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>

    <!-- versionless: managed by microsphere-redis-dependencies 0.1.13 -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>
</dependencies>
```

### Overriding a managed version

Declare your own `<dependencyManagement>` entry or override the version property in your POM: your
entries are evaluated before the inherited ones, so `com.alibaba.csp` can move to another release.

## Related Modules

- [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) — the
  BOM for consumers; itself inherits this parent.
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md),
  [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md),
  [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md),
  [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) — all
  children of this parent.
- Branch index: [README](./README.md) — repository overview:
  [README](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-parent.md)
