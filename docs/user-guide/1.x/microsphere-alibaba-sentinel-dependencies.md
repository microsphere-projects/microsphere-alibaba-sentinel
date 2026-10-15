# microsphere-alibaba-sentinel-dependencies

[中文版](zh/microsphere-alibaba-sentinel-dependencies.md)

> **Archived `1.x` line.** This page documents the `0.1.x` artifacts (branch `release-1.x`, Java 8, Spring Boot 2.x, Spring Cloud Hoxton - 2021.0.x). The main sources are the same as the current line; only the baselines differ (javax Servlet API, `org.hibernate:hibernate-core`, `META-INF/spring.factories` auto-configuration registration). The current-line equivalent of this page is [../current/microsphere-alibaba-sentinel-dependencies.md](../current/microsphere-alibaba-sentinel-dependencies.md).

## Overview

`microsphere-alibaba-sentinel-dependencies` is the Bill of Materials (BOM) of the Microsphere Alibaba
Sentinel project. Packaged as `pom`, it declares a single `<dependencyManagement>` section that pins
every published artifact of this repository to `${revision}`, so that consumers can add Sentinel
integration modules without repeating versions.

It is the artifact meant for applications: the sibling
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) is a Maven *parent* for
building the project itself, while this BOM is what an application imports. It manages the ten non-POM
artifacts of the repository — the commons module, the six plugin modules, and the three Spring-facing
modules.

## Version Information

| Item                     | Value                                                    |
|--------------------------|----------------------------------------------------------|
| Branch                   | `release-1.x` (documentation directory: `1.x`)           |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-dependencies`              |
| Packaging                | `pom`                                                    |
| Version on this branch   | `0.1.6-SNAPSHOT` (`${revision}`)                         |
| Released versions        | `0.1.0` - `0.1.5` (latest release: `0.1.5`)              |
| Java                     | 8                                                        |
| Maven parent             | `microsphere-alibaba-sentinel-parent` `${revision}`      |

## Compatibility

| Consumed technology  | This line (`0.1.x`)         | Current line (`0.2.x`)          |
|----------------------|-----------------------------|---------------------------------|
| Java                 | 8                           | 17                              |
| Spring Boot          | 2.x                         | 3.x                             |
| Spring Cloud         | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| Registration file    | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel     | 1.8.10                      | 1.8.10                          |

Import `0.1.x` of this BOM only with Spring Boot 2.x: the `spring-boot` and `spring-cloud` artifacts it
manages register their auto-configurations in `META-INF/spring.factories`, which Spring Boot 3.x no longer
reads for `EnableAutoConfiguration`. Applications on Spring Cloud Hoxton.SR12 through 2021.0.x must stay on this
line; applications on 2022.0.x and later import the current `0.2.x` BOM instead.

## Installation

Import the BOM in your `<dependencyManagement>` section:

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

`<type>pom</type>` and `<scope>import</scope>` are mandatory — without them Maven treats the BOM as a
plain dependency and no versions are managed. Afterwards declare any module without a version.

### Managed Artifacts

| Artifact ID                                     | Contents                                                  |
|-------------------------------------------------|-----------------------------------------------------------|
| `microsphere-alibaba-sentinel-commons`          | Sentinel plugin model, template, repository, utilities    |
| `microsphere-alibaba-sentinel-alibaba-druid`    | Druid `Filter` plugin                                     |
| `microsphere-alibaba-sentinel-mybatis`          | MyBatis `ExecutorFilter` plugin                           |
| `microsphere-alibaba-sentinel-hibernate-core`   | Hibernate `EntityCallback` plugin                         |
| `microsphere-alibaba-sentinel-p6spy`            | P6Spy `JdbcEventListener` plugin                          |
| `microsphere-alibaba-sentinel-redis`            | Spring Data Redis connection interceptor plugin           |
| `microsphere-alibaba-sentinel-spring-web`       | Spring Web MVC / WebFlux handler-method plugin            |
| `microsphere-alibaba-sentinel-spring`           | Plain-Spring dependency aggregation                       |
| `microsphere-alibaba-sentinel-spring-boot`      | Spring Boot auto-configurations and conditions            |
| `microsphere-alibaba-sentinel-spring-cloud`     | Spring Cloud feature reporting                            |

All entries use `${revision}`, so a managed version always equals the BOM version itself. Since the BOM
inherits [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md), its effective
dependency management additionally carries `sentinel-core` and `sentinel-transport-simple-http` at
`1.8.10`, `com.alibaba:fastjson` at `2.0.64`, and the imported Microsphere BOMs.

## API Reference

None. This module is POM-only (`<packaging>pom</packaging>`) and publishes no Java classes or
resources; it only pins versions.

## Configuration Properties

This module introduces no configuration properties.

## Usage Examples

### Declaring versionless dependencies

```xml
<dependencies>
    <!-- auto-configuration, for Spring Boot 2.x applications -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-spring-boot</artifactId>
    </dependency>
    <!-- or pick individual plugins and wire them yourself -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-redis</artifactId>
    </dependency>
    <!-- also managed, at 1.8.10, through the parent of this BOM -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>
</dependencies>
```

A `<dependencyManagement>` entry declared in your own POM wins over the imported BOM, so pinning
`sentinel-core` explicitly overrides the version inherited from
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md).

## Related Modules

- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) — the Maven parent
  this BOM inherits, with the version properties and imported BOMs.
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md),
  [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md),
  [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) — the
  entry points most applications import.
- Branch index: [README](./README.md) — repository overview:
  [README](../../../README.md)

---

## 中文

[中文版](zh/microsphere-alibaba-sentinel-dependencies.md)
