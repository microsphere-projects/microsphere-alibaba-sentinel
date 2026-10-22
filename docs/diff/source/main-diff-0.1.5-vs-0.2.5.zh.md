# 源码对比：`src/main` —— tag 0.1.5 vs tag 0.2.5

[English](main-diff-0.1.5-vs-0.2.5.md) · [测试对比（中文）](test-diff-0.1.5-vs-0.2.5.zh.md) · [测试对比（英文）](test-diff-0.1.5-vs-0.2.5.md)

按 Maven 模块逐一比较最新两个 tag 的 `src/main` 内容：

- Tag `0.1.5` → Maven 版本 `0.1.5`（`revision=0.1.5-SNAPSHOT`，Java 8 / Spring Boot 2.x 发布线）
- Tag `0.2.5` → Maven 版本 `0.2.5`（`revision=0.2.5-SNAPSHOT`，`java.version=17`，Java 17 / Spring Boot 3.x 发布线）

对比范围规则：

- 仅对比两个 tag 中同名模块；无法匹配的模块忽略（本次对比中所有模块均能匹配）。
- 排除纯 Java 8 与 Java 17 的语法/API 差异（如 `switch` 箭头标签、`instanceof` 模式匹配）。
- 排除纯 Java EE 与 Jakarta EE API 差异（如 `javax.servlet` → `jakarta.servlet` 依赖与导入）。
- 其余为真正的实现差异，并解释差异原因。

## 模块总览（`src/main`）

| Maven 模块 | 0.1.5 文件数 | 0.2.5 文件数 | 结果 |
|---|---|---|---|
| `microsphere-alibaba-sentinel-commons` | 20 | 20 | 完全一致 |
| `microsphere-alibaba-sentinel-plugins/*`（6 个插件模块） | 各 3 | 各 3 | 完全一致 |
| `microsphere-alibaba-sentinel-spring` | 0（聚合模块，无源码） | 0 | 完全一致 |
| `microsphere-alibaba-sentinel-spring-boot` | 8 | 7 | 存在差异（见下） |
| `microsphere-alibaba-sentinel-spring-cloud` | 3 | 2 | 存在差异（见下） |
| `microsphere-alibaba-sentinel-parent` / `-dependencies` | 仅 POM | 仅 POM | 无 `src` 可对比 |

## 详细差异

### `microsphere-alibaba-sentinel-spring-boot` —— `src/main`

| 文件 | 0.1.5 | 0.2.5 | 原因 |
|---|---|---|---|
| `META-INF/spring.factories` | 存在；以 `EnableAutoConfiguration` 键注册 4 个自动配置类 | 已删除 | **注册机制升级。** Spring Boot 2.7 起 `AutoConfiguration.imports` 成为标准机制，Boot 3.x 不再读取 `spring.factories` 中的 `EnableAutoConfiguration`。0.2.5 仅保留 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`（0.1.5 中同样存在且列出相同的 4 个类），Java 自动配置代码本身未变——属于 API 兼容清理，而非功能变化。 |
| `SentinelSpringWebAutoConfiguration.java` | Javadoc 引用 `org.springframework.boot.autoconfigure.web.*` 下的 `WebMvcAutoConfiguration` / `WebFluxAutoConfiguration` | 新增 `@see` 引用 `org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` 与 `org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration` | **上游包位置迁移（仅文档）。** Spring Boot 4.x 将相关自动配置迁移到独立模块/包；0.2.x 线补充新位置引用，同时保留旧引用以便追溯。无行为变化。 |

### `microsphere-alibaba-sentinel-spring-cloud` —— `src/main`

| 文件 | 0.1.5 | 0.2.5 | 原因 |
|---|---|---|---|
| `META-INF/spring.factories` | 存在；注册 `SentinelCloudAutoConfiguration` | 已删除 | 与 Boot 模块相同的注册机制升级；`AutoConfiguration.imports`（两个 tag 中内容一致）成为唯一注册来源。 |

### `src/main` 完全一致的模块

`commons`、`alibaba-druid`、`hibernate-core`、`mybatis`、`p6spy`、`redis`、`spring-web`、`spring` —— 两个 tag 之间逐字节一致（包括 `META-INF/services` 下的 SPI 文件，以及 `redis` 模块的 `spring.factories`：它使用的是 microsphere-redis 的拦截器扩展点而非 Boot 自动配置注册，因此两条发布线均保留）。

## 已排除的差异类别（备查）

以下差异存在于两个 tag 之间，但按范围规则被排除：

- **Java 17 语法/API**：`switch` 箭头标签、`instanceof` 模式匹配（主要出现在 `src/test`，见测试对比文档）。
- **Java EE → Jakarta EE**：`spring-web` 模块 POM 依赖 `javax.servlet:javax.servlet-api` → `jakarta.servlet:jakarta.servlet-api`。
- **POM 中上游坐标/版本升级**（非 `src/main`，此处仅列出以便理解）：父 POM 中 `microsphere-*` BOM 版本属性 `0.1.x` → `0.2.x`；Hibernate 坐标 `org.hibernate` → `org.hibernate.orm`（基于 Jakarta 的 Hibernate 6+）；`hibernate-test` 依赖 scope 收紧为 `test`；`spring-web` 测试依赖移除 JUnit 4 / `junit-vintage-engine`。

## 结论

0.1.5 与 0.2.5 之间，所有可匹配模块的生产代码（`src/main`）在功能上完全一致。真正差异仅有：`spring-boot`、`spring-cloud` 两模块删除了遗留的 `spring.factories` 自动配置注册（Spring Boot 3/4 API 兼容清理），以及两处跟踪 Spring Boot 包迁移的 Javadoc `@see` 补充。
