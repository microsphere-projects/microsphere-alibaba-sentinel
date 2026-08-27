# Microsphere Alibaba Sentinel

> Microsphere Projects for [Alibaba Sentinel](https://github.com/alibaba/Sentinel)

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-alibaba-sentinel)
[![Maven Build](https://github.com/microsphere-projects/microsphere-alibaba-sentinel/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-alibaba-sentinel/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-alibaba-sentinel/branch/main/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-alibaba-sentinel)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-alibaba-sentinel.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-alibaba-sentinel.svg)

Microsphere Sentinel is a multi-module Maven project that provides seamless integration between Alibaba Sentinel flow
control framework and commonly used Java technologies. The project acts as an abstraction layer that enables transparent
application of Sentinel's flow control, circuit breaking, and system protection capabilities to database operations,
caching, and other infrastructure components.

The project follows a layered approach, offering both low-level integration APIs and high-level auto-configuration for
Spring Boot applications. This design allows developers to adopt Sentinel protection incrementally, from core
infrastructure components to complete application-level integration:

- Web
    - Spring Web
        - Spring WebMVC
        - Spring WebFlux
- Database access
    - Alibaba Druid
    - Hibernate
    - MyBatis
    - P6Spy
- Redis
    - Spring Data Redis

## Modules

| **Module**                                    | **Purpose**                                                            |
|-----------------------------------------------|------------------------------------------------------------------------|
| **microsphere-alibaba-sentinel-parent**       | Defines the parent POM with dependency management and version profiles |
| **microsphere-alibaba-sentinel-dependencies** | Centralizes dependency management for all project modules              |
| **microsphere-alibaba-sentinel-commons**      | Common featurues of Alibaba Sentinel extension                         |
| **microsphere-alibaba-sentinel-plugins**      | The plugins of Alibaba Sentinel                                        |
| **microsphere-alibaba-sentinel-spring**       | Integration for Alibaba Sentinel Spring                                |

## Getting Started

The easiest way to get started is by adding the Microsphere Alibaba Sentinel BOM (Bill of Materials) to your project's
pom.xml:

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Alibaba Sentinel Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-sentinel-dependencies</artifactId>
            <version>${microsphere-sentinel.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

`${microsphere-sentinel.version}` has two branches:

| **Branches** | **Purpose**                                      | **Latest Version** |
|--------------|--------------------------------------------------|--------------------|
| **main**     | Compatible with Spring Cloud 2022.0.x - 2025.0.x | `0.2.4`            |
| **1.x**      | Compatible with Spring Cloud Hoxton - 2021.0.x   | `0.1.4`            |

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-alibaba-sentinel.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw package
```

- Windows:

```powershell
mvnw.cmd package
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search
  the [issues](https://github.com/microsphere-projects/microsphere-alibaba-sentinel/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-alibaba-sentinel/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-alibaba-sentinel)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-alibaba-sentinel/wiki)

### JavaDoc

- [microsphere-alibaba-sentinel-commons](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-commons)
- [microsphere-alibaba-sentinel-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-spring)
- [microsphere-alibaba-sentinel-alibaba-druid](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-alibaba-druid)
- [microsphere-alibaba-sentinel-hibernate](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-hibernate)
- [microsphere-alibaba-sentinel-mybatis](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-mybatis)
- [microsphere-alibaba-sentinel-p6spy](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-p6spy)
- [microsphere-alibaba-sentinel-redis](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-redis)
- [microsphere-alibaba-sentinel-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-spring)
- [microsphere-alibaba-sentinel-spring-web](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-alibaba-sentinel-spring-web)

## License

The Microsphere Spring is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
