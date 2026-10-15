# microsphere-alibaba-sentinel-spring

[English](../microsphere-alibaba-sentinel-spring.md)

## 概述

`microsphere-alibaba-sentinel-spring` 是 Microsphere Alibaba Sentinel 项目的纯 Spring 分发模块。

在 `release` 分支上，该模块以 `jar` 方式打包，但它**完全不含主 Java 源码**：模块目录下只有 `pom.xml`。因此它是一个*依赖聚合*模块。
它的目的是让非 Boot 的 Spring 应用只需一个坐标，即可引入 Sentinel 扩展模型，以及 Sentinel 插件所拦截的 Spring 侧基础设施：

- Sentinel 插件核心 API 与运行时（`microsphere-alibaba-sentinel-commons`），
- Microsphere 的 Redis Spring 支持（`microsphere-redis-spring`），Redis Sentinel 插件构建在其之上，
- 可选的第三方集成库（`sentinel-core`、`spring-data-redis`、`spring-orm`、`hibernate-core`、`druid`、`mybatis`、
  `mybatis-spring`），它们被聚合的代码所引用，从而免去使用方手工对齐这些版本。

该模块同时也是
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) 的基础，后者依赖本模块并在其上叠加自动配置。

## 版本信息

| 项目                     | 取值                                                     |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release`（文档目录：`current`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring`                    |
| 打包方式                 | `jar`（不含自有 class，仅做聚合）                          |
| 本分支版本               | `0.2.6-SNAPSHOT`                                         |
| 已发布版本               | `0.2.0` - `0.2.5`（最新发布版本：`0.2.5`）                 |
| Java                     | 17                                                       |
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |
| Maven parent             | `microsphere-alibaba-sentinel-parent` `${revision}`      |

## 兼容性

| 所消费的技术             | 本分支（`0.2.x`）                            | 历史版本线（`0.1.x`）                        |
|--------------------------|----------------------------------------------|----------------------------------------------|
| Java                     | 17 及以上                                     | 更早的基线（参见 1.x 文档）                    |
| Spring Framework         | 6.x（Jakarta 命名空间）                        | 5.x                                          |
| Spring Boot              | 3.x                                          | 2.x                                          |
| Spring Cloud             | 2022.0.x - 2025.0.x                          | Hoxton - 2021.0.x                            |
| Alibaba Sentinel         | 1.8.10                                       | 1.8.x                                        |

说明：

- 本模块**不**注册任何自动配置，因此它自身并不依赖 Spring Boot 3.x 的
  `AutoConfiguration.imports` 机制；它可以在任意 Spring 应用上下文中工作。
- Spring Boot 3.x 的要求适用于叠加在其上的模块，即 `microsphere-alibaba-sentinel-spring-boot` 与
  `microsphere-alibaba-sentinel-spring-cloud`，它们通过
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册自动配置。在 `0.1.x`
  版本线上，同样的自动配置是通过 `META-INF/spring.factories` 注册的。
- 可选依赖使用 Jakarta 时代的坐标（`org.hibernate.orm:hibernate-core`），这也是本分支面向 Spring Boot 3.x 与 Java 17 的另一个原因。

## 安装

直接添加坐标（版本由项目 BOM 统一管理，或显式声明）：

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring</artifactId>
    <version>0.2.5</version>
</dependency>
```

推荐做法是导入
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) 并省略版本：

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

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-spring</artifactId>
    </dependency>
</dependencies>
```

由于本模块中的每一个第三方依赖都声明为 `optional`，你仍然需要自己添加真正要保护的库（例如 `sentinel-core` 与
`spring-data-redis`）；本模块只保证这些版本与聚合代码所期望的版本保持一致。

## API 参考

本模块**不暴露自有 API** —— 它没有声明任何类、任何资源，也没有任何 `META-INF` 服务或自动配置注册文件。从该构件可访问到的一切都是传递而来的：

| 被引入的依赖（scope）    | 提供的内容                                                          |
|--------------------------|----------------------------------------------------------------------------|
| `microsphere-alibaba-sentinel-commons`（compile） | Sentinel 扩展模型：`SentinelPlugin`、`AbstractSentinelPlugin`、`SimpleSentinelPlugin`、`SentinelPluginRepository`、`SentinelOperations`/`SentinelTemplate`、`SentinelContext`、`SentinelConstants`、`SentinelUtils`、`SentinelMetricsRepository` 以及节点事件类。详见 [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md)。 |
| `microsphere-redis-spring`（compile，版本受管）    | Redis 插件所实现的 Redis 拦截契约（`RedisConnectionInterceptor` 及相关类型）。 |
| `com.alibaba.csp:sentinel-core`（optional）        | Sentinel 运行时（`SphU`、`ContextUtil`、资源类型常量）。                        |
| `org.springframework.data:spring-data-redis`（optional） | `RedisConnection` 以及 Redis 模板基础设施。                             |
| `org.springframework:spring-orm`（optional）       | Hibernate/JPA 的 Spring 集成类。                                             |
| `org.hibernate.orm:hibernate-core`（optional）     | Hibernate 的 `SessionFactory` 与实体生命周期 API。                            |
| `com.alibaba:druid`（optional）                    | Druid 插件所使用的 Druid `Filter`/`StatementProxy` API。                      |
| `org.mybatis:mybatis`、`org.mybatis:mybatis-spring`（optional） | MyBatis 插件所使用的 MyBatis `Executor` API。                    |

如果要在自动配置的帮助下使用 Sentinel 插件 API，请改为依赖
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md)，而不是本模块。

## 配置属性

本模块不引入任何自有配置属性。`microsphere.sentinel.*` 这些键由它所聚合的
`microsphere-alibaba-sentinel-commons` 代码定义并读取 —— 完整清单见
[Spring Boot 模块的配置属性](./microsphere-alibaba-sentinel-spring-boot.md)。

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

### 校验插件上下文是否存在

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
  依赖本模块并添加自动配置。
- [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) ——
  在 Boot 模块之上提供 Spring Cloud 特性上报。
- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) —— 持有上文所用受管版本的
  Maven parent。
- [microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) ——
  管理本构件的 BOM。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring.md)
