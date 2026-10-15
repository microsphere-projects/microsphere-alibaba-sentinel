# Developer Guide

[中文版](zh/README.md) · [Documentation home](../README.md)

The Developer Guide explains, module by module, how Microsphere Alibaba Sentinel is built: internal architecture,
class-level source analysis, the design patterns in use, and Mermaid flow / sequence diagrams for every non-trivial
runtime or startup flow. Module dependency relationships are shown as diagrams.

## Choose your branch line

| Directory                                     | Source branch | Artifact line | Implementation notes                            |
|-----------------------------------------------|---------------|---------------|-------------------------------------------------|
| [current/](current/README.md) | `release`     | `0.2.x`       | Java 17; Boot 3 auto-config registration; jakarta Servlet API |
| [1.x/](1.x/README.md)         | `release-1.x` | `0.1.x`       | Java 8; Boot 2 `spring.factories` registration; javax Servlet API |

## Structure of every module document

Each document in a branch directory follows the same outline:

1. Purpose and Scope
2. Architecture — Mermaid diagram of the module internals and its framework hook points
3. Source Analysis — class-by-class walkthrough with exact signatures
4. Design Patterns — only the patterns actually present, with implementing classes
5. Core Flows — Mermaid `sequenceDiagram` / `flowchart` for the complex paths
6. Dependencies — Mermaid graph plus the direct Maven dependency table
7. Extension Points — how to add a new plugin or behaviour
8. Related Modules

## Cross-cutting concepts to learn first

The [commons developer guide](current/microsphere-alibaba-sentinel-commons.md) documents the mechanisms every plugin
relies on:

- `SentinelPlugin` install / uninstall lifecycle (registry + JVM shutdown hook)
- `SentinelOperations` one-time (`execute` / `call`) and two-phase (`begin` / `end`) operations on top of `SphU.entry`
- `SentinelContext` as a `ThreadLocal`-carried execution context
- SPI loading of `SentinelPluginRepository` and the Sentinel `InitFunc`
- Enable/disable property resolution (`microsphere.sentinel.enabled`, `microsphere.sentinel.<plugin>.enabled`)

## Modules covered

Both directories contain the same 13 documents, one per Maven module:

- `microsphere-alibaba-sentinel-parent.md`
- `microsphere-alibaba-sentinel-dependencies.md`
- `microsphere-alibaba-sentinel-commons.md`
- `microsphere-alibaba-sentinel-plugins.md`
- `microsphere-alibaba-sentinel-alibaba-druid.md`
- `microsphere-alibaba-sentinel-hibernate-core.md`
- `microsphere-alibaba-sentinel-mybatis.md`
- `microsphere-alibaba-sentinel-p6spy.md`
- `microsphere-alibaba-sentinel-redis.md`
- `microsphere-alibaba-sentinel-spring-web.md`
- `microsphere-alibaba-sentinel-spring.md`
- `microsphere-alibaba-sentinel-spring-boot.md`
- `microsphere-alibaba-sentinel-spring-cloud.md`

---

## 中文

[开发者指南（中文版）](zh/README.md)
