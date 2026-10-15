# User Guide

[中文版](zh/README.md) · [Documentation home](../README.md)

The User Guide explains, module by module, how to use Microsphere Alibaba Sentinel: public APIs, installation,
configuration properties, runnable code examples adapted from the project's own test cases, version information and
the compatibility range of each artifact line.

## Choose your branch line

| Directory                                     | Source branch | Artifact line | Use when                                                          |
|-----------------------------------------------|---------------|---------------|-------------------------------------------------------------------|
| [current/](current/README.md) | `release`     | `0.2.x`       | Java 17, Spring Boot 3.x, Spring Cloud 2022.0.x – 2025.0.x        |
| [1.x/](1.x/README.md)         | `release-1.x` | `0.1.x`       | Java 8, Spring Boot 2.x, Spring Cloud Hoxton – 2021.0.x           |

## Structure of every module document

Each document in a branch directory follows the same outline:

1. Overview — what the module does and for whom
2. Version Information — coordinates and released versions on that branch
3. Compatibility — supported framework ranges and when to pick this line
4. Installation — BOM import plus dependency snippet
5. API Reference — public types and methods with their contracts
6. Configuration Properties — `microsphere.sentinel.*` keys relevant to the module
7. Usage Examples — code adapted from the module's tests
8. Related Modules — cross-references

## Modules covered

Both directories contain the same 13 documents, one per Maven module:

- `microsphere-alibaba-sentinel-parent.md` — parent POM and version baselines
- `microsphere-alibaba-sentinel-dependencies.md` — the project BOM
- `microsphere-alibaba-sentinel-commons.md` — core Sentinel API
- `microsphere-alibaba-sentinel-plugins.md` — plugin aggregator
- `microsphere-alibaba-sentinel-alibaba-druid.md` — Druid protection plugin
- `microsphere-alibaba-sentinel-hibernate-core.md` — Hibernate protection plugin
- `microsphere-alibaba-sentinel-mybatis.md` — MyBatis protection plugin
- `microsphere-alibaba-sentinel-p6spy.md` — P6Spy protection plugin
- `microsphere-alibaba-sentinel-redis.md` — Redis protection plugin
- `microsphere-alibaba-sentinel-spring-web.md` — Spring Web protection plugin
- `microsphere-alibaba-sentinel-spring.md` — plain-Spring aggregation module
- `microsphere-alibaba-sentinel-spring-boot.md` — Spring Boot auto-configuration
- `microsphere-alibaba-sentinel-spring-cloud.md` — Spring Cloud integration

## Suggested reading order

1. [BOM / dependencies](current/microsphere-alibaba-sentinel-dependencies.md) — wire up versions
2. [commons](current/microsphere-alibaba-sentinel-commons.md) — the core API
3. The plugin(s) you need, e.g. [spring-web](current/microsphere-alibaba-sentinel-spring-web.md)
4. [spring-boot](current/microsphere-alibaba-sentinel-spring-boot.md) — zero-code enablement

---

## 中文

[用户指南（中文版）](zh/README.md)
