# 用户指南 — current（release 分支，0.2.x）

[English](../README.md) · [用户指南总目录](../../zh/README.md)

这些文档介绍基于 `release` 分支构建的制品，即 Microsphere Alibaba Sentinel 的 `0.2.x` 版本线。

## 版本信息

| 项目                       | 值                                                           |
|----------------------------|--------------------------------------------------------------|
| 源码分支              | `release`                                                    |
| 已发布版本          | `0.2.0` – `0.2.5`（分支修订号：`0.2.6-SNAPSHOT`）        |
| Java 运行时               | 17                                                           |
| Spring Boot / Cloud        | 3.x / 2022.0.x – 2025.0.x                                    |
| Alibaba Sentinel           | `sentinel-core` 1.8.10                                       |
| Microsphere BOM 基线  | spring-cloud 0.2.26 · druid 0.2.20 · mybatis 0.2.16 · hibernate 0.2.10 · redis 0.2.14 |

## 模块文档

| 模块                                               | 适用于                                                          |
|----------------------------------------------------|-------------------------------------------------------------------|
| [parent](microsphere-alibaba-sentinel-parent.md)   | 版本属性、导入的第三方 BOM                     |
| [dependencies](microsphere-alibaba-sentinel-dependencies.md) | 消费方项目的 BOM 导入                          |
| [commons](microsphere-alibaba-sentinel-commons.md) | 核心 API：`SentinelPlugin`、`SentinelOperations`、`SentinelContext`、`SentinelTemplate`、插件注册表 |
| [plugins](microsphere-alibaba-sentinel-plugins.md) | 六个插件的概览                                       |
| [alibaba-druid](microsphere-alibaba-sentinel-alibaba-druid.md) | 针对 JDBC 语句的 Druid `Filter` 保护       |
| [hibernate-core](microsphere-alibaba-sentinel-hibernate-core.md) | Hibernate 实体操作保护               |
| [mybatis](microsphere-alibaba-sentinel-mybatis.md) | MyBatis `Executor` 保护                                     |
| [p6spy](microsphere-alibaba-sentinel-p6spy.md)    | 通过 P6Spy 实现的 JDBC 语句保护                           |
| [redis](microsphere-alibaba-sentinel-redis.md)    | Spring Data Redis 命令保护                              |
| [spring-web](microsphere-alibaba-sentinel-spring-web.md) | Spring Web MVC 处理器方法保护                    |
| [spring](microsphere-alibaba-sentinel-spring.md)  | 面向原生 Spring 应用的依赖聚合              |
| [spring-boot](microsphere-alibaba-sentinel-spring-boot.md) | 自动配置、`@ConditionalOnSentinel*` 注解、`microsphere.sentinel.*` 属性 |
| [spring-cloud](microsphere-alibaba-sentinel-spring-cloud.md) | Spring Cloud actuator 特性上报               |

## 模块依赖关系

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

## 配套文档集

同一分支的源码级分析位于
[开发者指南 · current 分支](../../../developer-guide/current/zh/README.md)。遗留版本线的文档见
[1.x 用户指南](../../1.x/zh/README.md)。

---

## English

[English 版本](../README.md)
