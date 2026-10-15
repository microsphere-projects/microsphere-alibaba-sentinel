# microsphere-alibaba-sentinel-parent

[English](../microsphere-alibaba-sentinel-parent.md)

## 概述

`microsphere-alibaba-sentinel-parent` 是一个以 `pom` 打包、由 Microsphere Alibaba Sentinel 项目全部模块共用的 Maven
parent。它不含代码也不含资源；它的存在是为了：

- 固定项目构建所依赖的第三方与 Microsphere BOM 版本，
- 导入上游 Microsphere 依赖 BOM，使各模块 POM 可以声明不带版本的依赖，
- 作为唯一的基于 `${revision}` 的 parent，服务于 `microsphere-alibaba-sentinel-commons`、六个插件模块、
  `microsphere-alibaba-sentinel-spring`、`microsphere-alibaba-sentinel-spring-boot`、
  `microsphere-alibaba-sentinel-spring-cloud` 以及 `microsphere-alibaba-sentinel-dependencies`。

它自身的 parent 是仓库聚合工程 `microsphere-alibaba-sentinel`（`../pom.xml`），后者又继承
`io.github.microsphere-projects:microsphere-spring-cloud-parent:0.2.26` —— Java 17 编译设置与构建插件配置正是来源于它。

## 版本信息

| 项目                     | 取值                                                         |
|--------------------------|-------------------------------------------------------------|
| 分支                     | `release`（文档目录：`current`）                              |
| Group ID                 | `io.github.microsphere-projects`                            |
| Artifact ID              | `microsphere-alibaba-sentinel-parent`                       |
| 打包方式                 | `pom`                                                       |
| 本分支版本               | `0.2.6-SNAPSHOT`（`${revision}`）                            |
| 已发布版本               | `0.2.0` - `0.2.5`（最新发布版本：`0.2.5`）                     |
| Java                     | 17                                                          |
| Maven parent             | `microsphere-alibaba-sentinel` `${revision}` -> `microsphere-spring-cloud-parent` 0.2.26 |

## 兼容性

| 所消费的技术                 | 本分支（`0.2.x`）              | 历史版本线（`0.1.x`）   |
|----------------------------|--------------------------------|-------------------------|
| Java                       | 17（由 parent 强制约束）         | 更早的基线               |
| Spring Boot                | 3.x                            | 2.x                     |
| Spring Cloud               | 2022.0.x - 2025.0.x            | Hoxton - 2021.0.x       |
| Alibaba Sentinel           | 1.8.10                         | 1.8.x                   |

`0.2.x` 版本线是注册方式从 `META-INF/spring.factories` 迁移到
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 的分界点，这正是经由本 parent
构建的 `spring-boot` 与 `spring-cloud` 模块要求 Spring Boot 3.x 的原因。POM 没有自己的运行时：本 parent 是惰性的，下面列出的版本才是
约束你 classpath 的东西。

## 安装

该构件是作为 parent 使用，而不是作为依赖：

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-parent</artifactId>
    <version>0.2.5</version>
</parent>
```

只想要版本对齐效果的应用（这是常见情形）应当改为导入
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md)。

### 依赖管理

| `<properties>` 条目                    | 受管依赖                                                                  | 版本     | 类型          |
|------------------------------------|--------------------------------------------------------------------------|----------|---------------|
| `microsphere-spring-cloud.version` | `io.github.microsphere-projects:microsphere-spring-cloud-dependencies`   | `0.2.26` | `pom`，import |
| `microsphere-alibaba-druid.version`| `io.github.microsphere-projects:microsphere-alibaba-druid-dependencies`  | `0.2.20` | `pom`，import |
| `microsphere-mybatis.version`      | `io.github.microsphere-projects:microsphere-mybatis-dependencies`        | `0.2.16` | `pom`，import |
| `microsphere-hibernate.version`    | `io.github.microsphere-projects:microsphere-hibernate-dependencies`      | `0.2.10` | `pom`，import |
| `microsphere-redis.version`        | `io.github.microsphere-projects:microsphere-redis-dependencies`          | `0.2.14` | `pom`，import |
| `sentinel.version`                 | `com.alibaba.csp:sentinel-core`                                          | `1.8.10` | jar           |
| `sentinel.version`                 | `com.alibaba.csp:sentinel-transport-simple-http`                         | `1.8.10` | jar           |
| `fastjson.version`                 | `com.alibaba:fastjson`                                                   | `2.0.65` | jar           |

这五个导入都是普通的 BOM，因此 `microsphere-redis-spring`、`microsphere-mybatis-spring-boot` 这类配套构件的实际版本由它们解析，
而不是由本 POM 的 `<properties>` 决定。项目自身的特性构件不在这里管理 —— 那是 BOM 的职责。

## API 参考

无。该模块仅有 POM（`<packaging>pom</packaging>`），不发布任何 Java 类、任何资源，也不发布任何 `META-INF` 注册文件。

## 配置属性

该模块不引入任何配置属性。

## 使用示例

### 基于本 parent 构建自定义 Sentinel 扩展模块

```xml
<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-parent</artifactId>
    <version>0.2.5</version>
    <relativePath/>
</parent>

<dependencies>
    <!-- versionless: sentinel.version resolves to 1.8.10 -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
    </dependency>

    <!-- versionless: managed by microsphere-redis-dependencies 0.2.14 -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>
</dependencies>
```

### 覆盖受管版本

在你自己的 POM 中声明 `<dependencyManagement>` 条目或覆盖版本属性：你的条目先于继承而来的条目被评估，因此 `com.alibaba.csp`
可以切换到其他发布版本。

## 相关模块

- [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) ——
  面向使用方的 BOM；它自身继承本 parent。
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md)、
  [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md)、
  [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md)、
  [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) —— 均为本
  parent 的子模块。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-parent.md)
