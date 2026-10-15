# microsphere-alibaba-sentinel-dependencies

[English](../microsphere-alibaba-sentinel-dependencies.md)

> **已归档的 `1.x` 分支。** 本页记录 `0.1.x` 构件（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支一致，仅基线不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。本页在主干分支上的对应文档是 [../current/zh/microsphere-alibaba-sentinel-dependencies.md](../../current/zh/microsphere-alibaba-sentinel-dependencies.md)。

## 概述

`microsphere-alibaba-sentinel-dependencies` 是 Microsphere Alibaba Sentinel 项目的物料清单（BOM）。
它以 `pom` 打包，只声明一个 `<dependencyManagement>` 段，把本仓库发布的所有构件固定到 `${revision}`，
使使用方可以不再重复书写版本就能添加 Sentinel 集成模块。

它是面向应用的那个构件：同级
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) 是用于构建项目自身的
Maven *父 POM*，而本 BOM 才是应用要导入的东西。它管理仓库的十个非 POM 构件 —— commons 模块、
六个插件模块，以及三个面向 Spring 的模块。

## 版本信息

| 项目                     | 值                                                       |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release-1.x`（文档目录：`1.x`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-dependencies`              |
| 打包方式                 | `pom`                                                    |
| 本分支版本               | `0.1.6-SNAPSHOT`（`${revision}`）                         |
| 已发布版本               | `0.1.0` - `0.1.5`（最新发布版：`0.1.5`）                  |
| Java                     | 8                                                        |
| Maven 父 POM             | `microsphere-alibaba-sentinel-parent` `${revision}`      |

## 兼容性

| 所依赖的技术         | 本分支（`0.1.x`）           | 当前分支（`0.2.x`）             |
|----------------------|-----------------------------|---------------------------------|
| Java                 | 8                           | 17                              |
| Spring Boot          | 2.x                         | 3.x                             |
| Spring Cloud         | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| 注册文件             | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel     | 1.8.10                      | 1.8.10                          |

只在与 Spring Boot 2.x 搭配时导入本 BOM 的 `0.1.x`：它所管理的 `spring-boot` 与 `spring-cloud` 构件
把自动配置注册在 `META-INF/spring.factories` 中，而 Spring Boot 3.x 读取 `EnableAutoConfiguration` 时已不再
查看该文件。使用 Spring Cloud Hoxton.SR12 到 2021.0.x 的应用必须留在本分支；使用 2022.0.x 及之后版本的应用
应改为导入当前 `0.2.x` 的 BOM。

## 安装

在你的 `<dependencyManagement>` 段中导入该 BOM：

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

`<type>pom</type>` 与 `<scope>import</scope>` 是必需的 —— 缺少它们时 Maven 会把 BOM 当作
普通依赖处理，于是没有任何版本被管理。此后你就可以不带版本地声明任意模块。

### 被管理的构件

| Artifact ID                                       | 内容                                                      |
|-------------------------------------------------|-----------------------------------------------------------|
| `microsphere-alibaba-sentinel-commons`          | Sentinel 插件模型、模板、仓库、工具类                        |
| `microsphere-alibaba-sentinel-alibaba-druid`    | Druid `Filter` 插件                                       |
| `microsphere-alibaba-sentinel-mybatis`          | MyBatis `ExecutorFilter` 插件                              |
| `microsphere-alibaba-sentinel-hibernate-core`   | Hibernate `EntityCallback` 插件                            |
| `microsphere-alibaba-sentinel-p6spy`            | P6Spy `JdbcEventListener` 插件                             |
| `microsphere-alibaba-sentinel-redis`            | Spring Data Redis 连接拦截器插件                            |
| `microsphere-alibaba-sentinel-spring-web`       | Spring Web MVC / WebFlux 处理器方法插件                     |
| `microsphere-alibaba-sentinel-spring`           | 纯 Spring 依赖聚合                                         |
| `microsphere-alibaba-sentinel-spring-boot`      | Spring Boot 自动配置与条件注解                              |
| `microsphere-alibaba-sentinel-spring-cloud`     | Spring Cloud 特性上报                                      |

所有条目都使用 `${revision}`，因此受管版本总等于 BOM 自身的版本。由于该 BOM 继承
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md)，其生效的依赖管理还会额外带上
`1.8.10` 版本的 `sentinel-core` 与 `sentinel-transport-simple-http`、`2.0.64` 版本的
`com.alibaba:fastjson`，以及导入的各个 Microsphere BOM。

## API 参考

无。本模块仅有 POM（`<packaging>pom</packaging>`），不发布 Java 类也不发布资源；
它只固定版本。

## 配置属性

本模块不引入任何配置属性。

## 使用示例

### 声明不带版本的依赖

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

你自己 POM 中声明的 `<dependencyManagement>` 条目优先于导入的 BOM，因此显式固定
`sentinel-core` 会覆盖从
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) 继承而来的版本。

## 相关模块

- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) —— 本 BOM 所继承的
  Maven 父 POM，其中定义了版本属性与导入的 BOM。
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md)、
  [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md)、
  [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) —— 大多数应用
  会导入的入口。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-dependencies.md)
