# 开发者指南

[English](../README.md) · [文档总目录](../../zh/README.md)

开发者指南逐模块说明 Microsphere Alibaba Sentinel 是如何构建的：内部架构、类级源码分析、
所使用的设计模式，以及覆盖每个非平凡运行时或启动流程的 Mermaid 流程图 / 时序图。
模块之间的依赖关系以图示方式呈现。

## 选择分支版本线

| 目录                                        | 源码分支 | 版本线 | 实现要点                             |
|-----------------------------------------------|---------------|---------------|-------------------------------------------------|
| [current/](../current/zh/README.md) | `release`     | `0.2.x`       | Java 17；Boot 3 自动配置注册；jakarta Servlet API |
| [1.x/](../1.x/zh/README.md)         | `release-1.x` | `0.1.x`       | Java 8；Boot 2 `spring.factories` 注册；javax Servlet API |

## 每篇模块文档的结构

分支目录中的每篇文档都遵循相同的大纲：

1. 目的与范围
2. 架构 —— 模块内部结构及其框架钩子点的 Mermaid 图
3. 源码分析 —— 逐类讲解，附带精确签名
4. 设计模式 —— 只列出确实存在的模式，并给出实现类
5. 核心流程 —— 针对复杂路径的 Mermaid `sequenceDiagram` / `flowchart`
6. 依赖 —— Mermaid 图加上直接 Maven 依赖表
7. 扩展点 —— 如何新增插件或行为
8. 相关模块

## 建议先掌握的横切概念

[commons 开发者指南](../current/zh/microsphere-alibaba-sentinel-commons.md)记录了每个插件都依赖的机制：

- `SentinelPlugin` 的安装 / 卸载生命周期（注册表 + JVM 关闭钩子）
- `SentinelOperations` 基于 `SphU.entry` 的一次性（`execute` / `call`）与两阶段（`begin` / `end`）操作
- `SentinelContext`：由 `ThreadLocal` 承载的执行上下文
- `SentinelPluginRepository` 与 Sentinel `InitFunc` 的 SPI 加载
- 启用/禁用属性解析（`microsphere.sentinel.enabled`、`microsphere.sentinel.<plugin>.enabled`）

## 覆盖的模块

两个目录都包含相同的 13 篇文档，每个 Maven 模块一篇：

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

## English

[English 版本](../README.md)
