# microsphere-alibaba-sentinel-spring

[English](../microsphere-alibaba-sentinel-spring.md)

> **已归档的 `1.x` 分支。** 本页记录 `0.1.x` 构件（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支一致，仅基线不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。本页在主干分支上的对应文档是 [../current/zh/microsphere-alibaba-sentinel-spring.md](../../current/zh/microsphere-alibaba-sentinel-spring.md)。

## 概述

`microsphere-alibaba-sentinel-spring` 是 Microsphere Alibaba Sentinel 项目的纯 Spring 分发模块。

在 `release-1.x` 分支上该模块打包为 `jar`，但它**完全不含主干 Java 源码**：
模块目录下只有 `pom.xml`。因此它是一个*依赖聚合*模块。
它的目的是让非 Boot 的 Spring 应用只需一个坐标，就能引入 Sentinel
扩展模型，以及 Sentinel 插件所要拦截的 Spring 侧基础设施：

- Sentinel 插件核心 API 与运行时（`microsphere-alibaba-sentinel-commons`），
- Microsphere Redis 的 Spring 支持（`microsphere-redis-spring`），Redis Sentinel 插件即构建在其之上，
- 被聚合代码所引用的可选第三方集成库（`sentinel-core`、`spring-data-redis`、
  `spring-orm`、`hibernate-core`、`druid`、`mybatis`、`mybatis-spring`），使使用方不必手工对齐这些版本。

该模块同时也是
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) 的基础，
后者依赖本模块并在其上追加自动配置。

## 版本信息

| 项目                     | 值                                                       |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release-1.x`（文档目录：`1.x`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring`                    |
| 打包方式                 | `jar`（不含自身类；仅做聚合）                              |
| 本分支版本               | `0.1.6-SNAPSHOT`                                         |
| 已发布版本               | `0.1.0` - `0.1.5`（最新发布版：`0.1.5`）                  |
| Java                     | 8                                                        |
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |
| Maven 父 POM             | `microsphere-alibaba-sentinel-parent` `${revision}`      |

## 兼容性

| 所依赖的技术             | 本分支（`0.1.x`）                     | 当前分支（`0.2.x`）                    |
|--------------------------|----------------------------------------|----------------------------------------|
| Java                     | 8                                      | 17                                     |
| Spring Framework         | 5.x（javax 命名空间）                   | 6.x                                    |
| Spring Boot              | 2.x                                    | 3.x                                    |
| Spring Cloud             | Hoxton.SR12 - 2021.0.x                 | 2022.0.x - 2025.0.x                    |
| Alibaba Sentinel         | 1.8.10                                 | 1.8.10                                 |

说明：

- 本模块**不**注册任何自动配置，因此它本身并不依赖 Spring Boot 2.x 的
  `spring.factories` 机制；它可以在任意 Spring 应用上下文中工作。
- Spring Boot 2.x 的要求适用于叠加在其上的模块，即
  `microsphere-alibaba-sentinel-spring-boot` 与 `microsphere-alibaba-sentinel-spring-cloud`，它们通过
  `META-INF/spring.factories` 注册自己的自动配置。在当前 `0.2.x` 分支上，同样的自动配置改为通过
  `AutoConfiguration.imports` 注册。
- 可选依赖使用 javax 时代的坐标（`org.hibernate:hibernate-core` 5.x、
  `javax.servlet:javax.servlet-api`），这也是本分支面向 Spring Boot 2.x 与 Java 8 的另一个原因。

## 安装

直接添加坐标（版本由项目 BOM 统一管理，或显式声明）：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring</artifactId>
    <version>0.1.5</version>
</dependency>
```

推荐的做法是导入
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) 并省略
版本号：

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

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-spring</artifactId>
    </dependency>
</dependencies>
```

由于本模块中的每一个第三方依赖都被声明为 `optional`，你仍然需要自行添加真正要保护的库
（例如 `sentinel-core` 与 `spring-data-redis`）；该模块只保证这些依赖的版本与聚合代码的预期一致。

## API 参考

本模块**不暴露自身的 API** —— 它不声明任何类、任何资源，也不声明任何
`META-INF` 服务或自动配置注册文件。从该构件可访问到的一切都是传递而来的：

| 传递引入（作用域）        | 所提供的内容                                                     |
|--------------------------|----------------------------------------------------------------------------|
| `microsphere-alibaba-sentinel-commons`（compile） | Sentinel 扩展模型：`SentinelPlugin`、`AbstractSentinelPlugin`、`SimpleSentinelPlugin`、`SentinelPluginRepository`、`SentinelOperations`/`SentinelTemplate`、`SentinelContext`、`SentinelConstants`、`SentinelUtils`、`SentinelMetricsRepository` 以及节点事件类。详见 [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md)。 |
| `microsphere-redis-spring`（compile，版本受管）    | Redis 插件所实现的 Redis 拦截契约（`RedisConnectionInterceptor` 及相关类型）。 |
| `com.alibaba.csp:sentinel-core`（optional）        | Sentinel 运行时（`SphU`、`ContextUtil`、资源类型常量）。                       |
| `org.springframework.data:spring-data-redis`（optional） | `RedisConnection` 与 Redis 模板基础设施。                          |
| `org.springframework:spring-orm`（optional）       | Hibernate/JPA 的 Spring 集成类。                                             |
| `org.hibernate:hibernate-core`（optional）         | Hibernate 的 `SessionFactory` 与实体生命周期 API。                            |
| `com.alibaba:druid`（optional）                    | Druid 插件所使用的 Druid `Filter`/`StatementProxy` API。                      |
| `org.mybatis:mybatis`、`org.mybatis:mybatis-spring`（optional） | MyBatis 插件所使用的 MyBatis `Executor` API。                    |

若要在自动配置的配合下使用 Sentinel 插件 API，请改为依赖
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md)，
而不是本模块。

## 配置属性

本模块不引入任何自身的配置属性。`microsphere.sentinel.*` 键由它所聚合的
`microsphere-alibaba-sentinel-commons` 代码定义并读取 —— 完整清单参见
[Spring Boot 模块文档中的“配置属性”一节](./microsphere-alibaba-sentinel-spring-boot.md)。

## 使用示例

### 在纯 Spring 上下文中注册 Sentinel 插件

```java
import io.microsphere.alibaba.sentinel.common.SentinelPlugin;
import io.microsphere.alibaba.sentinel.common.SimpleSentinelPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.microsphere.alibaba.sentinel.common.SentinelPluginRepository.INSTANCE;
// adapted from io.microsphere.alibaba.sentinel.common.SimpleSentinelPluginTest
@Configuration
public class SentinelPluginConfiguration {

    @Bean
    public SentinelPlugin orderPlugin() {
        // AbstractSentinelPlugin installs the plugin into SentinelPluginRepository.INSTANCE
        // and uninstalls it on JVM shutdown
        return new SimpleSentinelPlugin("order-service");
    }

    @Bean
    public String installedPluginNames() {
        // INSTANCE.isInstalled("order-service") is true once the bean above is created
        return INSTANCE.getAll().stream()
                .map(SentinelPlugin::getName)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }
}
```

### 用聚合而来的 Sentinel 模板保护业务调用

```java
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import io.microsphere.alibaba.sentinel.common.SentinelOperations;
import io.microsphere.alibaba.sentinel.common.SentinelTemplate;

import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_CONTEXT_NAME;
import static io.microsphere.alibaba.sentinel.common.constants.SentinelConstants.DEFAULT_ORIGIN;

// adapted from io.microsphere.alibaba.sentinel.common.SentinelTemplateTest
SentinelOperations sentinelOperations = new SentinelTemplate(ResourceTypeConstants.COMMON);
sentinelOperations.execute("OrderService.create", DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN, () -> {
    // business logic flows through a Sentinel entry here
});
```

### 验证插件上下文是否存在

```java
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;

// adapted from io.microsphere.alibaba.sentinel.spring.boot.autoconfigure.SentinelAutoConfigurationTest
Context context = ContextUtil.enter("microsphere_sentinel_alibaba_druid_context", "Filter");
assert context != null;
```

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— 本模块所分发的 API。
- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) ——
  依赖本模块并追加自动配置。
- [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) ——
  在 Boot 模块之上提供 Spring Cloud 特性上报。
- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) —— Maven 父 POM，
  其中管理了上文使用的版本。
- [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) —— 管理本构件的
  BOM。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring.md)
