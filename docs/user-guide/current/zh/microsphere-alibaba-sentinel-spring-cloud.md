# microsphere-alibaba-sentinel-spring-cloud

[English](../microsphere-alibaba-sentinel-spring-cloud.md)

## 概述

`microsphere-alibaba-sentinel-spring-cloud` 把 Sentinel 的 Spring Boot 自动配置适配到 Spring Cloud 应用。它只提供一样东西：
一个 Spring Cloud `HasFeatures` bean，把当前 JVM 中已安装的 Sentinel 插件发布到 `/actuator/features` 端点上。

该模块只包含一个自动配置类
`io.microsphere.alibaba.sentinel.spring.cloud.SentinelCloudAutoConfiguration`，通过
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册。其内部嵌套的
`FeaturesConfig` 类会遍历 Sentinel 插件仓库，把每个处于启用状态的插件以一个 `NamedFeature` 上报，而已安装但被禁用的插件则作为抽象特性上报。

它位于分层结构的最顶层：`commons` -> `spring` -> `spring-boot` -> `spring-cloud`。

## 版本信息

| 项目                     | 取值                                                     |
|--------------------------|----------------------------------------------------------|
| 分支                     | `release`（文档目录：`current`）                          |
| Group ID                 | `io.github.microsphere-projects`                         |
| Artifact ID              | `microsphere-alibaba-sentinel-spring-cloud`              |
| 打包方式                 | `jar`                                                    |
| 本分支版本               | `0.2.6-SNAPSHOT`                                         |
| 已发布版本               | `0.2.0` - `0.2.5`（最新发布版本：`0.2.5`）                 |
| Java                     | 17                                                       |
| Spring Boot              | 3.x                                                      |
| Spring Cloud             | 2022.0.x - 2025.0.x（`spring-cloud-commons`、`spring-cloud-context`） |
| Microsphere Spring Cloud | BOM 0.2.26                                               |
| Alibaba Sentinel         | `sentinel-core` 1.8.10                                   |

## 兼容性

同时要求 Spring Boot 3.x **以及**相匹配的 Spring Cloud 版本线：

| 所消费的技术           | 本分支（`0.2.x`）           | 历史版本线（`0.1.x`）   |
|----------------------|-----------------------------|-------------------------|
| Java                 | 17 及以上                  | 更早的基线               |
| Spring Boot          | 3.x                         | 2.x                     |
| Spring Cloud         | 2022.0.x - 2025.0.x         | Hoxton - 2021.0.x       |
| 注册文件              | `AutoConfiguration.imports` | `META-INF/spring.factories` |
| Alibaba Sentinel     | 1.8.10                      | 1.8.x                   |

对 Spring Boot 3.x 的要求并非偶然：本模块只提供 `AutoConfiguration.imports` 文件，而 Spring Boot 2.x 不会读取它。在 `0.1.x`
版本线上，同一个自动配置是通过 `spring.factories` 注册的，并支持 Spring Cloud Hoxton 至 2021.0.x。

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-alibaba-sentinel-spring-cloud</artifactId>
</dependency>
```

如果已导入项目 BOM（参见
[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md)）则无需写版本。该构件会传递提供
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) 和
`microsphere-spring-cloud-commons`；而 Spring Cloud 及各技术专属的配套依赖
（`spring-cloud-commons`、`spring-cloud-context`、`spring-boot-actuator`、`sentinel-core`、
`microsphere-alibaba-druid-spring-cloud`、`microsphere-mybatis-spring-cloud`、
`microsphere-redis-spring-cloud`、`microsphere-hibernate-core`、WebMvc/WebFlux starter）都声明为 `optional`，
需要由你自己的 Spring Cloud BOM 提供。

## API 参考

`SentinelCloudAutoConfiguration`（自 `1.0.0` 起）：

- **类级别条件** —— `@ConditionalOnSentinelAvailable`，即 `microsphere.sentinel.enabled` 在 Spring
  `Environment` 中不能解析为 `false`，**并且** `com.alibaba.csp.sentinel.SphU` 与
  `io.microsphere.alibaba.sentinel.common.SentinelPlugin` 都必须可加载。
- **顺序** —— `@AutoConfigureAfter`（按类名指定，因此不存在的类会被直接跳过）把它排在 Spring Cloud Alibaba 的
  `SentinelAutoConfiguration`、Microsphere 的 Druid/MyBatis/Redis Spring Cloud 自动配置、
  `ConfigurationPropertyHasFeaturesAutoConfiguration` 以及全部四个 Sentinel Spring Boot 自动配置之后。这保证了在收集特性时插件
  bean 已经存在。
- **`FeaturesConfig`** —— 嵌套类，由 `io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesAvailable`
  守护，并由自动配置导入。它声明一个 bean，其名称由常量
  `SentinelCloudAutoConfiguration.FeaturesConfig.BEAN_NAME` = `alibaba-sentinel.features` 指定：

```java
// the feature collection performed by FeaturesConfig#features()
SentinelPluginRepository repository = SentinelPluginRepository.INSTANCE;
for (SentinelPlugin plugin : repository.getAll()) {
    if (plugin.isEnabled()) {
        namedFeatures.add(new NamedFeature(plugin.getName(), plugin.getClass()));
    } else {
        abstractFeatures.add(plugin.getClass());
    }
}
HasFeatures hasFeatures = new HasFeatures(abstractFeatures, namedFeatures);
```

本模块**没有** bootstrap 上下文的参与，没有 Spring Cloud Config 集成，也没有任何与刷新相关的行为：它没有声明 bootstrap 注册、
没有 `RefreshScope` bean，也没有 `RefreshEvent`/`EnvironmentChangeEvent` 监听器。上下文刷新只会重新评估常规的自动配置条件。

## 配置属性

| 属性                              | 类型    | 默认值 | 作用                                                                        |
|-----------------------------------|---------|---------|------------------------------------------------------------------------------|
| `microsphere.sentinel.enabled`    | boolean | `true`  | 为 `false` 时移除该自动配置（因而也移除 features bean）。                       |
| `spring.application.name`         | string  | -       | 本模块不读取它；但实践中使用的 Spring Cloud actuator 配置需要它。                 |
| `management.endpoints.web.exposure.include` | string | - | 必须包含 `features` 才能暴露 `/actuator/features`。 |

插件级键（`microsphere.sentinel.<plugin>.enabled`）在
[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) 中说明；它们决定插件 bean
是否被创建，进而决定该插件是否作为具名特性出现。

## 使用示例

### 用 `ApplicationContextRunner` 校验 features bean

```java
import org.springframework.cloud.client.actuator.HasFeatures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

// adapted from SentinelCloudAutoConfigurationTest
new ApplicationContextRunner()
        .withConfiguration(of(SentinelCloudAutoConfiguration.class))
        .withPropertyValues("spring.application.name=test-app")
        .run(context -> assertThat(context)
                .hasSingleBean(SentinelCloudAutoConfiguration.class)
                .hasBean("alibaba-sentinel.features")
                .hasSingleBean(HasFeatures.class));
```

### 读取 Spring Cloud `FeaturesEndpoint` 的启动测试

```java
@SpringBootTest(classes = SentinelCloudAutoConfigurationTest.class,
        webEnvironment = NONE,
        properties = {
                "spring.application.name=test-app",
                "management.endpoints.web.exposure.include=features"
        })
@EnableAutoConfiguration
class SentinelCloudAutoConfigurationTest {

    @Autowired
    private Map<String, HasFeatures> hasFeaturesMap;

    @Autowired
    private FeaturesEndpoint featuresEndpoint;

    // adapted from SentinelCloudAutoConfigurationTest
    @Test
    void test() {
        assertEquals("alibaba-sentinel.features", SentinelCloudAutoConfiguration.FeaturesConfig.BEAN_NAME);
        assertNotNull(hasFeaturesMap.get("alibaba-sentinel.features"));
        assertNotNull(featuresEndpoint.features());
    }
}
```

### 通过 HTTP 暴露端点

在 classpath 上具备 `spring-boot-starter-actuator` 与 Spring Cloud 上下文后，启动应用并调用：

```bash
curl http://localhost:8080/actuator/features
```

`alibaba-sentinel` 相关条目会为每个已启用的 Sentinel 插件列出一个具名特性 —— 例如
`alibaba-druid`、`mybatis`、`redis`、`spring-web` —— 由插件仓库上报。

## 相关模块

- [microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) —— 本模块
  排在其后的那些自动配置。
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md) —— Boot 模块之下的
  聚合层。
- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) ——
  用于收集特性的 `SentinelPlugin` 与 `SentinelPluginRepository`。
- [microsphere-alibaba-sentinel-parent](./microsphere-alibaba-sentinel-parent.md) —— 管理 Microsphere
  Spring Cloud BOM 版本。
- 分支索引：[README](./README.md) —— 仓库总览：
  [README](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-spring-cloud.md)
