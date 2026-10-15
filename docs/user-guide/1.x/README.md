# User Guide — 1.x (release-1.x branch, 0.1.x)

[中文版](zh/README.md) · [User Guide home](../README.md)

These documents describe the artifacts built from the `release-1.x` branch — the `0.1.x` line of Microsphere Alibaba
Sentinel, maintained for legacy Java 8 / Spring Boot 2.x applications.

## Version information

| Item                       | Value                                                        |
|----------------------------|--------------------------------------------------------------|
| Source branch              | `release-1.x`                                                |
| Released versions          | `0.1.0` – `0.1.5` (branch revision: `0.1.6-SNAPSHOT`)        |
| Java runtime               | 8                                                            |
| Spring Boot / Cloud        | 2.x / Hoxton – 2021.0.x                                      |
| Alibaba Sentinel           | `sentinel-core` 1.8.10                                       |
| Microsphere BOM baselines  | spring-cloud 0.1.26 · druid 0.1.20 · mybatis 0.1.15 · hibernate 0.1.9 · redis 0.1.13 |

## Module documents

| Module                                             | Read it for                                                       |
|----------------------------------------------------|-------------------------------------------------------------------|
| [parent](microsphere-alibaba-sentinel-parent.md)   | Version properties, imported third-party BOMs                     |
| [dependencies](microsphere-alibaba-sentinel-dependencies.md) | BOM import for consumer projects                          |
| [commons](microsphere-alibaba-sentinel-commons.md) | Core API: `SentinelPlugin`, `SentinelOperations`, `SentinelContext`, `SentinelTemplate`, plugin registry |
| [plugins](microsphere-alibaba-sentinel-plugins.md) | Overview of the six plugins                                       |
| [alibaba-druid](microsphere-alibaba-sentinel-alibaba-druid.md) | Druid `Filter` protection for JDBC statements       |
| [hibernate-core](microsphere-alibaba-sentinel-hibernate-core.md) | Hibernate entity-operation protection               |
| [mybatis](microsphere-alibaba-sentinel-mybatis.md) | MyBatis `Executor` protection                                     |
| [p6spy](microsphere-alibaba-sentinel-p6spy.md)    | JDBC statement protection through P6Spy                           |
| [redis](microsphere-alibaba-sentinel-redis.md)    | Spring Data Redis command protection                              |
| [spring-web](microsphere-alibaba-sentinel-spring-web.md) | Spring Web MVC handler-method protection                    |
| [spring](microsphere-alibaba-sentinel-spring.md)  | Dependency aggregation for plain Spring applications              |
| [spring-boot](microsphere-alibaba-sentinel-spring-boot.md) | Auto-configuration via `META-INF/spring.factories`, conditions, properties |
| [spring-cloud](microsphere-alibaba-sentinel-spring-cloud.md) | Spring Cloud integration                        |

## Line differences vs `current`

The Java sources of the two lines are nearly identical; the differences are environmental:

- Auto-configuration is registered through Spring Boot 2.x `META-INF/spring.factories`.
- The Servlet API baseline is `javax.servlet`, and Hibernate resolves to the pre-`org.hibernate.orm` coordinates.
- Microsphere companion BOMs and fastjson are pinned one minor version lower than the `0.2.x` line.

## Module dependencies

```mermaid
graph TD
    C[commons]
    DR[plugins alibaba-druid]
    HB[plugins hibernate-core]
    MB[plugins mybatis]
    P6[plugins p6spy]
    RD[plugins redis]
    SW[plugins spring-web]
    SP[spring]
    SB[spring-boot]
    SC[spring-cloud]
    C --> DR
    C --> HB
    C --> MB
    C --> P6
    C --> RD
    C --> SW
    C --> SP
    DR --> SB
    HB --> SB
    MB --> SB
    RD --> SB
    SW --> SB
    SP --> SB
    SB --> SC
```

## Companion set

The matching source-level analysis lives in the
[Developer Guide for the 1.x branch](../../developer-guide/1.x/README.md). Current-line users should read the
[0.2.x User Guide](../current/README.md) instead.

---

## 中文

[1.x 版本用户指南（中文版）](zh/README.md)
