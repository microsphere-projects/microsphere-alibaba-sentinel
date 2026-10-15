# microsphere-alibaba-sentinel-plugins

[English](../microsphere-alibaba-sentinel-plugins.md)

> **已归档的 `1.x` 分支线。** 本页文档描述 `0.1.x` 制品（分支 `release-1.x`、Java 8、Spring Boot 2.x、Spring Cloud Hoxton - 2021.0.x）。主干源码与当前分支线一致，仅基线版本不同（javax Servlet API、`org.hibernate:hibernate-core`、通过 `META-INF/spring.factories` 注册自动配置）。当前分支线对应的文档是 [../current/zh/microsphere-alibaba-sentinel-plugins.md](../../current/zh/microsphere-alibaba-sentinel-plugins.md)。

## 概述

`microsphere-alibaba-sentinel-plugins` 是一个 Maven **聚合 POM**（`<packaging>pom</packaging>`），用于构建本仓库随附的
六个技术专用 Sentinel 插件。每个子模块把某一个框架的扩展点适配到
[microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) 中定义的插件抽象：框架自身的 SPI
（Druid `Filter`、MyBatis `ExecutorFilter`、Hibernate `EntityCallback`、P6Spy `JdbcEventListener`、一个 Redis
拦截器、一个 Spring Web 拦截器）由一个同时实现 `SentinelPlugin` 的类来完成，因此经过该扩展点的每一次调用都会被
包装进一个 Sentinel 上下文与 entry。

| 子模块 | 插件名称 | 默认上下文名称 | Sentinel 来源 | 资源类型 | 适配的扩展点 |
| --- | --- | --- | --- | --- | --- |
| [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) | `alibaba-druid` | `microsphere_sentinel_alibaba_druid_context` | `Filter` | `COMMON_DB_SQL` | `com.alibaba.druid.filter.Filter` |
| [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) | `mybatis` | `microsphere_sentinel_mybatis_context` | `Executor` | `COMMON_DB_SQL` | `io.microsphere.mybatis.executor.ExecutorFilter` |
| [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md) | `hibernate` | `microsphere_sentinel_hibernate_context` | `SessionFactory` | `COMMON_DB_SQL` | `io.microsphere.hibernate.entity.EntityCallback` |
| [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md) | `p6spy` | `microsphere_sentinel_p6spy_context` | `Statement` | `COMMON_DB_SQL` | `com.p6spy.engine.event.JdbcEventListener` |
| [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md) | `redis` | `microsphere_sentinel_redis_context` | `RedisConnection` | `COMMON`（默认） | `io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor` |
| [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md) | `spring-web` | `microsphere_sentinel_spring_web_context` | `HandlerMethod` | `COMMON_WEB` | `io.microsphere.spring.web.method.support.HandlerMethodInterceptor` |

每个子模块都依赖 `microsphere-alibaba-sentinel-commons`（compile 作用域），并把被适配的框架声明为 `optional`，
因此插件绝不会把宿主技术强行拖进一个并不使用它的应用中。

## 版本信息

| 项目 | 值 |
| --- | --- |
| 分支 | `release-1.x` —— 文档位于 `docs/user-guide/1.x/` |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-alibaba-sentinel-plugins:<version>` |
| 分支版本 | `0.1.6-SNAPSHOT`（`${revision}`），由全部六个子模块继承 |
| 已发布版本 | `0.1.0`、`0.1.1`、`0.1.2`、`0.1.3`、`0.1.4`、`0.1.5` |
| 打包方式 | `pom`（仅聚合） |
| POM 名称 | `Microsphere :: Alibaba Sentinel :: Plugins` |
| Java | 8 |

## 兼容性

聚合器自身不依赖任何内容；兼容性按子模块继承，由 `microsphere-alibaba-sentinel-parent` 中导入的
Microsphere BOM 驱动：

| Microsphere BOM | 版本 | 对齐的第三方 |
| --- | --- | --- |
| `microsphere-spring-cloud-dependencies` | 0.1.26 | Spring Cloud Hoxton.SR12 – 2021.0.x、Spring Boot 2.x、Java 8 |
| `microsphere-alibaba-druid-dependencies` | 0.1.20 | Alibaba Druid（druid 插件） |
| `microsphere-mybatis-dependencies` | 0.1.15 | MyBatis（mybatis 插件） |
| `microsphere-hibernate-dependencies` | 0.1.9 | Hibernate（hibernate-core 插件） |
| `microsphere-redis-dependencies` | 0.1.13 | Spring Data Redis（redis 插件） |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | 全部六个插件 |

Java 8 / Spring Boot 2.x 的应用请使用这条 `0.1.x` 分支线；它是已归档的一代，其父 POM 与
`microsphere-spring-cloud-dependencies` 0.1.26 对齐。基于 Java 17 / Spring Boot 3.x 的应用应当使用当前的
`0.2.x` 分支线。不要把一条线的插件与另一条线的 commons 模块混用（反之亦然）：两条线之间框架基线和
`SentinelPlugin` 契约都不相同。

## 安装

应用不会依赖这个聚合器，而是依赖它们各自需要的单个插件制品。请导入 BOM，使所有坐标都处于版本管理之下：

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
    <!-- select one or more plugins -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-alibaba-druid</artifactId>
    </dependency>
    <!-- the adapted framework and sentinel-core are optional, declare them yourself -->
    <dependency>
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.10</version>
    </dependency>
</dependencies>
```

从源码构建全部六个插件正是这个聚合器的用途：

```bash
./mvnw -pl microsphere-alibaba-sentinel-plugins -amd clean install
```

## API 参考

本模块自身不暴露任何 Java API。它只包含一个没有 `<dependencies>` 的 `pom.xml`，没有源码目录，也不会打包任何
内容 —— 构建只产出一个 POM 制品。你真正编程对接的 API 位于各子模块以及 commons 模块中：

- `io.microsphere.alibaba.sentinel.common.SentinelPlugin` —— 每个插件都实现的契约（`getName()`、
  `getContextName()`、`getOrigin()`、`getResourceType()`、`getTrafficType()`、`isEnabled()`、`setEnabled(boolean)`）。
- `io.microsphere.alibaba.sentinel.common.SentinelOperations` / `SentinelTemplate` —— 插件内部通过
  `begin(...)` / `end(...)` 或 `call(...)` 调用的包装逻辑。
- 各子模块自己的常量接口（例如 `SentinelAlibabaDruidConstants`、`SentinelMyBatisConstants`），
  对外发布 `PLUGIN_NAME`、`DEFAULT_CONTEXT_NAME`、`DEFAULT_ORIGIN` 与 `ENABLED_PROPERTY_NAME`。

构造器签名与扩展点行为请参见各子模块文档。

## 配置属性

本模块不引入任何配置属性。每个子模块各自声明
`microsphere.sentinel.<pluginName>.enabled` 开关，基于 commons 的前缀 `microsphere.sentinel.` 解析，默认启用；
确切属性名请查阅子模块页面。

## 使用示例

聚合器不会在应用代码中使用。与“使用它”等价的动作是挑选插件，这属于构建期的事情：

```xml
<!-- adapted from the <modules> list of microsphere-alibaba-sentinel-plugins/pom.xml -->
<!-- Enable SQL-layer protection only: druid + mybatis -->
<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-alibaba-druid</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-alibaba-sentinel-mybatis</artifactId>
    </dependency>
</dependencies>
```

一旦进入 classpath，插件会通过它自己的 SPI 文件（`META-INF/services/...`）被发现，或者注册为 Spring bean，
你可以借助共享仓库来检查或切换它：

```java
// adapted from SentinelPluginRepositoryTest — the two plugins installed above are visible here
SentinelPluginRepository repository = SentinelPluginRepository.INSTANCE;

assertTrue(repository.isInstalled("alibaba-druid"));
SentinelPlugin myBatisPlugin = repository.get("mybatis");
if (myBatisPlugin != null) {
    myBatisPlugin.setEnabled(false);          // stop protecting MyBatis statements at runtime
}

repository.getAll().forEach(plugin -> System.out.println(
        plugin.getName() + " -> " + plugin.getContextName() + " [" + plugin.getOrigin() + "]"));
```

同样的实例会以 `io.microsphere.sentinel:type=SentinelPlugin,name=alibaba-druid` 和
`io.microsphere.sentinel:type=SentinelPlugin,name=mybatis` 注册为 JMX MBean，
因为 commons 的 SPI 提供者是 `JMXSentinelPluginRepository`；使用任意 JMX 客户端即可在不重启的情况下翻转 `Enabled`。

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— 所有插件所构建依赖的 API
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md)
- [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md)
- [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md)
- [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md)
- [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md)
- [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md)
- `microsphere-alibaba-sentinel-dependencies` —— 安装一节使用的 BOM
- [分支索引](./README.md) · [仓库根目录](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-plugins.md)
