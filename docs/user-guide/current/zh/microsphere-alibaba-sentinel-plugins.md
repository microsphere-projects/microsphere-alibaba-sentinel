# microsphere-alibaba-sentinel-plugins

[English](../microsphere-alibaba-sentinel-plugins.md)

## 概述

`microsphere-alibaba-sentinel-plugins` 是一个 Maven **聚合 POM**（`<packaging>pom</packaging>`），用于构建本仓库随附的
六个技术专属 Sentinel 插件。每个子模块都把某一个框架的扩展点适配到
[microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) 中定义的插件抽象上：框架自身的 SPI
（Druid `Filter`、MyBatis `ExecutorFilter`、Hibernate `EntityCallback`、P6Spy `JdbcEventListener`、Redis 拦截器、
Spring Web 拦截器）由一个同时实现 `SentinelPlugin` 的类来完成，因此经过该扩展点的每一次调用都会被包装进一个
Sentinel 上下文与 entry 之中。

| 子模块 | 插件名称 | 默认上下文名 | Sentinel origin | 资源类型 | 适配的扩展点 |
| --- | --- | --- | --- | --- | --- |
| [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) | `alibaba-druid` | `microsphere_sentinel_alibaba_druid_context` | `Filter` | `COMMON_DB_SQL` | `com.alibaba.druid.filter.Filter` |
| [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) | `mybatis` | `microsphere_sentinel_mybatis_context` | `Executor` | `COMMON_DB_SQL` | `io.microsphere.mybatis.executor.ExecutorFilter` |
| [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md) | `hibernate` | `microsphere_sentinel_hibernate_context` | `SessionFactory` | `COMMON_DB_SQL` | `io.microsphere.hibernate.entity.EntityCallback` |
| [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md) | `p6spy` | `microsphere_sentinel_p6spy_context` | `Statement` | `COMMON_DB_SQL` | `com.p6spy.engine.event.JdbcEventListener` |
| [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md) | `redis` | `microsphere_sentinel_redis_context` | `RedisConnection` | `COMMON`（默认） | `io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor` |
| [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md) | `spring-web` | `microsphere_sentinel_spring_web_context` | `HandlerMethod` | `COMMON_WEB` | `io.microsphere.spring.web.method.support.HandlerMethodInterceptor` |

每个子模块都依赖 `microsphere-alibaba-sentinel-commons`（compile 作用域），并把所适配的框架声明为 `optional`，
因此插件绝不会把它所依附宿主技术栈拖入一个并不使用它的应用中。

## 版本信息

| 项目 | 值 |
| --- | --- |
| 分支 | `release` —— 文档位于 `docs/user-guide/current/` |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-alibaba-sentinel-plugins:<version>` |
| 分支版本 | `0.2.6-SNAPSHOT` (`${revision}`)，六个子模块全部继承 |
| 已发布版本 | `0.2.0`、`0.2.1`、`0.2.2`、`0.2.3`、`0.2.4`、`0.2.5` |
| 打包方式 | `pom`（仅作为聚合器） |
| POM 名称 | `Microsphere :: Alibaba Sentinel :: Plugins` |
| Java | 17 |

## 兼容性

聚合器自身不依赖任何东西；兼容性按子模块继承，由 `microsphere-alibaba-sentinel-parent` 中导入的 Microsphere BOM
决定：

| Microsphere BOM | 版本 | 它所对齐的第三方 |
| --- | --- | --- |
| `microsphere-spring-cloud-dependencies` | 0.2.26 | Spring Cloud 2022.0.x – 2025.0.x、Spring Boot 3.x、Java 17 |
| `microsphere-alibaba-druid-dependencies` | 0.2.20 | Alibaba Druid（druid 插件） |
| `microsphere-mybatis-dependencies` | 0.2.16 | MyBatis（mybatis 插件） |
| `microsphere-hibernate-dependencies` | 0.2.10 | Hibernate（hibernate-core 插件） |
| `microsphere-redis-dependencies` | 0.2.14 | Spring Data Redis（redis 插件） |
| `com.alibaba.csp:sentinel-core` | 1.8.10 | 全部六个插件 |

面向 Java 17 / Spring Boot 3.x 的应用请选择 `0.2.x` 版本线；`0.1.x` 版本线是已归档的 Java 8 /
Spring Boot 2.x 一代，其 parent 对齐的是更早的 Druid、MyBatis、Hibernate 与 Redis 版本。不要把 `0.1.x`
的插件与 `0.2.x` 的 commons 模块混用（反之亦然）：两个版本线之间的 optional 框架版本和
`SentinelPlugin` 契约都不相同。

## 安装

应用并不依赖这个聚合器，而是依赖各自需要的具体插件构件。请导入 BOM，使所有坐标都处于版本管理之下：

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

从源码一起构建全部六个插件，正是聚合器存在的意义：

```bash
./mvnw -pl microsphere-alibaba-sentinel-plugins -amd clean install
```

## API 参考

本模块不暴露任何属于自己的 Java API。它只包含一个 `pom.xml`，其中没有 `<dependencies>`，没有源码目录，
也不打包任何内容——构建只产出一个 POM 构件。你实际编程所面对的 API 位于各子模块和 commons 之中：

- `io.microsphere.alibaba.sentinel.common.SentinelPlugin` —— 每个插件都实现的契约（`getName()`、
  `getContextName()`、`getOrigin()`、`getResourceType()`、`getTrafficType()`、`isEnabled()`、`setEnabled(boolean)`）。
- `io.microsphere.alibaba.sentinel.common.SentinelOperations` / `SentinelTemplate` —— 插件内部通过
  `begin(...)` / `end(...)` 或 `call(...)` 所调用的包装逻辑。
- 每个子模块自己的常量接口（例如 `SentinelAlibabaDruidConstants`、`SentinelMyBatisConstants`），
  对外发布 `PLUGIN_NAME`、`DEFAULT_CONTEXT_NAME`、`DEFAULT_ORIGIN` 和 `ENABLED_PROPERTY_NAME`。

构造器签名与扩展点行为请参见各子模块的文档。

## 配置属性

本模块不引入任何配置属性。每个子模块各自声明一个
`microsphere.sentinel.<pluginName>.enabled` 开关，它基于 commons 中的前缀
`microsphere.sentinel.` 解析，默认启用；确切的属性名请查阅子模块页面。

## 用法示例

聚合器不会在应用代码中被使用。与之等价的“使用”方式就是挑选插件，这是一个构建期的事情：

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

插件一旦进入 classpath，就会通过它自己的 SPI 文件（`META-INF/services/...`）被发现，或被注册为
一个 Spring bean，你可以借助共享仓库来查看或切换它：

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

同样的这些实例也会以 JMX MBean 的形式注册在
`io.microsphere.sentinel:type=SentinelPlugin,name=alibaba-druid` 和
`io.microsphere.sentinel:type=SentinelPlugin,name=mybatis` 之下，因为 commons 的 SPI 提供者是
`JMXSentinelPluginRepository`；使用任意 JMX 客户端即可在不重启的前提下切换 `Enabled`。

## 相关模块

- [microsphere-alibaba-sentinel-commons](./microsphere-alibaba-sentinel-commons.md) —— 所有插件共同构建其上的 API
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md)
- [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md)
- [microsphere-alibaba-sentinel-hibernate-core](./microsphere-alibaba-sentinel-hibernate-core.md)
- [microsphere-alibaba-sentinel-p6spy](./microsphere-alibaba-sentinel-p6spy.md)
- [microsphere-alibaba-sentinel-redis](./microsphere-alibaba-sentinel-redis.md)
- [microsphere-alibaba-sentinel-spring-web](./microsphere-alibaba-sentinel-spring-web.md)
- `microsphere-alibaba-sentinel-dependencies` —— 上文“安装”一节使用的 BOM
- [分支索引](./README.md) · [仓库根目录](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-plugins.md)
