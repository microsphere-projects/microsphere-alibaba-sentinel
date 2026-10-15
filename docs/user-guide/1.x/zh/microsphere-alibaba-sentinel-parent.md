# microsphere-alibaba-sentinel-parent

[English](../microsphere-alibaba-sentinel-parent.md)

> **已归档的 `1.x` 分支。** 本页记录 `0.1.x` 构件（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支一致，仅基线不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。本页在主干分支上的对应文档是 [../current/zh/microsphere-alibaba-sentinel-parent.md](../../current/zh/microsphere-alibaba-sentinel-parent.md)。

## 概述

`microsphere-alibaba-sentinel-parent` 是一个以 `pom` 打包、由 Microsphere Alibaba Sentinel 项目
全部模块共用的 Maven 父 POM。它不携带代码也不携带资源；它的存在是为了：

- 固定项目构建所依赖的第三方与 Microsphere BOM 版本，
- 导入上游 Microsphere 依赖 BOM，使各模块 POM 可以声明不带版本的依赖，
- 作为基于 `${revision}` 的唯一父 POM，服务于
  `microsphere-alibaba-sentinel-commons`、六个插件模块、
  `microsphere-alibaba-sentinel-spring`、`microsphere-alibaba-sentinel-spring-boot`、
  `microsphere-alibaba-sentinel-spring-cloud` 与
  `microsphere-alibaba-sentinel-dependencies`。

它自身的父 POM 是仓库聚合器 `microsphere-alibaba-sentinel`（`../pom.xml`），后者又继承
`io.github.microsphere-projects:microsphere-spring-cloud-parent:0.1.26` —— Java 8 编译配置与构建插件配置
即来源于此。

## 版本信息

| 项目                     | 值                                                          |
|--------------------------|-------------------------------------------------------------|
| 分支                     | `release-1.x`（文档目录：`1.x`）                             |
| Group ID                 | `io.github.microsphere-projects`                            |
| Artifact ID              | `microsphere-alibaba-sentinel-parent`                       |
| 打包方式                 | `pom`                                                       |
| 本分支版本               | `0.1.6-SNAPSHOT`（`${revision}`）                            |
| 已发布版本               | `0.1.0` - `0.1.5`（最新发布版：`0.1.5`）                     |
| Java                     | 8                                                           |
| Maven 父 POM             | `microsphere-alibaba-sentinel` `${revision}` -> `microsphere-spring-cloud-parent` 0.1.26 |

## 兼容性

| 所依赖的技术                 | 本分支（`0.1.x`）           | 当前分支（`0.2.x`）             |
|----------------------------|-----------------------------|---------------------------------|
| Java                       | 8（由父 POM 强制约束）        | 17                              |
| Spring Boot                | 2.x                         | 3.x                             |
| Spring Cloud               | Hoxton.SR12 - 2021.0.x      | 2022.0.x - 2025.0.x             |
| 注册文件                     | `META-INF/spring.factories` | `AutoConfiguration.imports`     |
| Alibaba Sentinel           | 1.8.10                      | 1.8.10                          |

本分支仍通过 `META-INF/spring.factories` 注册自动配置；当前分支把它们迁移到了
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`，该文件由 Spring Boot 3.x 读取，
而 Spring Boot 2.x 会忽略它。这就是经由该父 POM 构建的 `spring-boot` 与 `spring-cloud` 模块要求
Spring Boot 2.x 的原因。POM 本身没有运行时：这个父 POM 是惰性的，真正约束你 classpath 的是下面列出的版本。

## 安装

该构件是作为父 POM 被使用的，而不是作为依赖：

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-parent</artifactId>
    <version>0.1.5</version>
</parent>
```

只需要版本对齐（这是常见情形）的应用应当改为导入
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md)。

### 依赖管理

| `<properties>` 条目                    | 被管理的依赖                                                                | 版本     | 类别          |
|------------------------------------|--------------------------------------------------------------------------|----------|---------------|
| `microsphere-spring-cloud.version` | `io.github.microsphere-projects:microsphere-spring-cloud-dependencies`   | `0.1.26` | `pom`, import |
| `microsphere-alibaba-druid.version`| `io.github.microsphere-projects:microsphere-alibaba-druid-dependencies`  | `0.1.20` | `pom`, import |
| `microsphere-mybatis.version`      | `io.github.microsphere-projects:microsphere-mybatis-dependencies`        | `0.1.15` | `pom`, import |
| `microsphere-hibernate.version`    | `io.github.microsphere-projects:microsphere-hibernate-dependencies`      | `0.1.9` | `pom`, import |
| `microsphere-redis.version`        | `io.github.microsphere-projects:microsphere-redis-dependencies`          | `0.1.13` | `pom`, import |
| `sentinel.version`                 | `com.alibaba.csp:sentinel-core`                                          | `1.8.10` | jar           |
| `sentinel.version`                 | `com.alibaba.csp:sentinel-transport-simple-http`                         | `1.8.10` | jar           |
| `fastjson.version`                 | `com.alibaba:fastjson`                                                   | `2.0.64` | jar           |

这五个导入项都是普通 BOM，因此 `microsphere-redis-spring`、`microsphere-mybatis-spring-boot`
等伙伴构件的实际版本由它们解析，而不是由本 POM 的 `<properties>` 解析。项目自身的功能构件并不在此管理 ——
那是 BOM 的职责。

## API 参考

无。本模块仅有 POM（`<packaging>pom</packaging>`），不发布 Java 类、不发布资源，
也不发布任何 `META-INF` 注册文件。

## 配置属性

本模块不引入任何配置属性。

## 使用示例

### 基于该父 POM 构建自定义的 Sentinel 扩展模块

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-parent</artifactId>
    <version>0.1.5</version>
    <relativePath/>
</parent>

<dependencies>
    <!-- versionless: sentinel.version resolves to 1.8.10 -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>

    <!-- versionless: managed by microsphere-redis-dependencies 0.1.13 -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>
</dependencies>
```

### 覆盖某个受管版本

在你自己的 POM 中声明 `<dependencyManagement>` 条目，或覆盖版本属性：你的条目先于继承而来的条目求值，
因此 `com.alibaba.csp` 可以切换到其他发布版本。

## 相关模块

- [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) —— 面向
  使用方的 BOM；它本身继承本父 POM。
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md)、
  [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md)、
  [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md)、
  [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) —— 全部是本
  父 POM 的子模块。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-parent.md)
