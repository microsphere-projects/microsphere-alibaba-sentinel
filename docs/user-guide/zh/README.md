# 用户指南

[English](../README.md) · [文档总目录](../../zh/README.md)

用户指南逐模块说明如何使用 Microsphere Alibaba Sentinel：公开 API、安装方式、配置属性、
改编自项目自身测试用例的可运行代码示例、版本信息，以及各版本线的兼容范围。

## 选择分支版本线

| 目录                                        | 源码分支 | 版本线 | 适用场景                                                            |
|-----------------------------------------------|---------------|---------------|-------------------------------------------------------------------|
| [current/](../current/zh/README.md) | `release`     | `0.2.x`       | Java 17、Spring Boot 3.x、Spring Cloud 2022.0.x – 2025.0.x        |
| [1.x/](../1.x/zh/README.md)         | `release-1.x` | `0.1.x`       | Java 8、Spring Boot 2.x、Spring Cloud Hoxton – 2021.0.x           |

## 每篇模块文档的结构

分支目录中的每篇文档都遵循相同的大纲：

1. 概述 —— 该模块做什么、面向谁
2. 版本信息 —— 该分支上的坐标与已发布版本
3. 兼容性 —— 支持的框架范围，以及何时选择该版本线
4. 安装 —— BOM 导入与依赖片段
5. API 参考 —— 公开类型与方法及其契约
6. 配置属性 —— 与该模块相关的 `microsphere.sentinel.*` 键
7. 使用示例 —— 改编自该模块测试用例的代码
8. 相关模块 —— 交叉引用

## 覆盖的模块

两个目录都包含相同的 13 篇文档，每个 Maven 模块一篇：

- `microsphere-alibaba-sentinel-parent.md` —— 父 POM 与版本基线
- `microsphere-alibaba-sentinel-dependencies.md` —— 项目 BOM
- `microsphere-alibaba-sentinel-commons.md` —— Sentinel 核心 API
- `microsphere-alibaba-sentinel-plugins.md` —— 插件聚合模块
- `microsphere-alibaba-sentinel-alibaba-druid.md` —— Druid 保护插件
- `microsphere-alibaba-sentinel-hibernate-core.md` —— Hibernate 保护插件
- `microsphere-alibaba-sentinel-mybatis.md` —— MyBatis 保护插件
- `microsphere-alibaba-sentinel-p6spy.md` —— P6Spy 保护插件
- `microsphere-alibaba-sentinel-redis.md` —— Redis 保护插件
- `microsphere-alibaba-sentinel-spring-web.md` —— Spring Web 保护插件
- `microsphere-alibaba-sentinel-spring.md` —— 原生 Spring 聚合模块
- `microsphere-alibaba-sentinel-spring-boot.md` —— Spring Boot 自动配置
- `microsphere-alibaba-sentinel-spring-cloud.md` —— Spring Cloud 集成

## 建议阅读顺序

1. [BOM / dependencies](../current/zh/microsphere-alibaba-sentinel-dependencies.md) —— 装配版本
2. [commons](../current/zh/microsphere-alibaba-sentinel-commons.md) —— 核心 API
3. 你所需要的插件，例如 [spring-web](../current/zh/microsphere-alibaba-sentinel-spring-web.md)
4. [spring-boot](../current/zh/microsphere-alibaba-sentinel-spring-boot.md) —— 零代码启用

---

## English

[English 版本](../README.md)
