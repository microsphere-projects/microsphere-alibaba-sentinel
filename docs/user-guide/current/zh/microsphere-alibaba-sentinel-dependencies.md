# microsphere-alibaba-sentinel-dependencies

[English](../microsphere-alibaba-sentinel-dependencies.md)

## 概述

`microsphere-alibaba-sentinel-dependencies` 是 Microsphere Alibaba Sentinel 项目的物料清单（BOM）。它以 `pom`
打包，只声明了一个 `<dependencyManagement>` 段落，把本仓库发布的所有构件固定到 `${revision}`，这样使用方添加 Sentinel 集成模块时就无需重复写版本。

它是面向应用的那个构件：同级的
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) 是用于构建项目自身的 Maven
*parent*，而本 BOM 才是应用要导入的东西。它管理仓库中十个非 POM 构件 —— commons 模块、六个插件模块，以及三个面向 Spring 的模块。

## 版本信息

| 项目                     | 取值                                                     |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release`（文档目录：`current`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-dependencies`              |
| 打包方式                 | `pom`                                                    |
| 本分支版本               | `0.2.6-SNAPSHOT`（`${revision}`）                         |
| 已发布版本               | `0.2.0` - `0.2.5`（最新发布版本：`0.2.5`）                 |
| Java                     | 17                                                       |
| Maven parent             | `microsphere-alibaba-sentinel-parent` `${revision}`      |

## 兼容性

| 所消费的技术           | 本分支（`0.2.x`）           | 历史版本线（`0.1.x`）   |
|----------------------|-----------------------------|-------------------------|
| Java                 | 17 及以上                  | 更早的基线               |
| Spring Boot          | 3.x                         | 2.x                     |
| Spring Cloud         | 2022.0.x - 2025.0.x         | Hoxton - 2021.0.x       |
| 注册文件              | `AutoConfiguration.imports` | `META-INF/spring.factories` |
| Alibaba Sentinel     | 1.8.10                      | 1.8.x                   |

本 BOM 的 `0.2.x` 只能在 Spring Boot 3.x 下导入：它管理的 `spring-boot` 与 `spring-cloud` 条目只通过
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置，而 Spring Boot
2.x 从不读取该文件。仍在使用 Spring Cloud Hoxton - 2021.0.x 的应用必须留在 `0.1.x` 版本线，那一线上同样的自动配置是通过
`spring.factories` 发布的。

## 安装

在你的 `<dependencyManagement>` 段落中导入该 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-alibaba-sentinel-dependencies</artifactId>
            <version>0.2.5</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`<type>pom</type>` 与 `<scope>import</scope>` 是必须的 —— 缺少它们，Maven 会把 BOM 当成普通依赖，因而不会管理任何版本。之后你就可以
不带版本地声明任意模块。

### 受管构件

| Artifact ID                                       | 内容                                                       |
|-------------------------------------------------|-----------------------------------------------------------|
| `microsphere-alibaba-sentinel-commons`          | Sentinel 插件模型、模板、仓库、工具类                          |
| `microsphere-alibaba-sentinel-alibaba-druid`    | Druid `Filter` 插件                                       |
| `microsphere-alibaba-sentinel-mybatis`          | MyBatis `ExecutorFilter` 插件                             |
| `microsphere-alibaba-sentinel-hibernate-core`   | Hibernate `EntityCallback` 插件                           |
| `microsphere-alibaba-sentinel-p6spy`            | P6Spy `JdbcEventListener` 插件                            |
| `microsphere-alibaba-sentinel-redis`            | Spring Data Redis 连接拦截器插件                             |
| `microsphere-alibaba-sentinel-spring-web`       | Spring Web MVC / WebFlux 的 handler 方法插件                |
| `microsphere-alibaba-sentinel-spring`           | 纯 Spring 依赖聚合                                         |
| `microsphere-alibaba-sentinel-spring-boot`      | Spring Boot 自动配置与条件注解                               |
| `microsphere-alibaba-sentinel-spring-cloud`     | Spring Cloud 特性上报                                      |

所有条目都使用 `${revision}`，因此受管版本总是等于 BOM 自身的版本。由于该 BOM 继承
[microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md)，其生效的依赖管理还额外带有
`1.8.10` 版本的 `sentinel-core` 与 `sentinel-transport-simple-http`、`2.0.65` 版本的 `com.alibaba:fastjson`，
以及导入的各个 Microsphere BOM。

## API 参考

无。该模块仅有 POM（`<packaging>pom</packaging>`），不发布任何 Java 类或资源；它只是固定版本。

## 配置属性

该模块不引入任何配置属性。

## 使用示例

### 声明不带版本的依赖

```xml
<dependencies>
    <!-- auto-configuration, for Spring Boot 3.x applications -->
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

在你自己的 POM 中声明的 `<dependencyManagement>` 条目优先于导入的 BOM，因此显式固定 `sentinel-core`
可以覆盖从 [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) 继承而来的版本。

## 相关模块

- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) —— 本 BOM 所继承的
  Maven parent，含有版本属性与导入的 BOM。
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md)、
  [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md)、
  [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) ——
  大多数应用会导入的入口模块。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-dependencies.md)
