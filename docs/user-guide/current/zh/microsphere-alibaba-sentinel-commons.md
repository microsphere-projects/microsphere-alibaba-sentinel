# microsphere-alibaba-sentinel-commons

[English](../microsphere-alibaba-sentinel-commons.md)

## 概述

`microsphere-alibaba-sentinel-commons` 是 Microsphere Alibaba Sentinel 集成方案中与框架无关的核心模块。它用回调模型取代了底层的 `SphU.entry(...)` / `ContextUtil.enter(...)` 协议，并定义了每一个技术专属插件（Druid、MyBatis、Hibernate、P6Spy、Redis、Spring Web）都要实现的插件抽象。

- **执行包装** —— `SentinelOperations` 由 `SentinelTemplate` 实现，它在 Sentinel 上下文与 entry 之内运行业务代码，记录结果与失败，追踪异常，并且始终释放 entry。
- **插件模型** —— `SentinelPlugin` 描述一个流量来源（名称、上下文名、origin、资源类型、流量类型、启用开关），并把自身安装进 `SentinelPluginRepository`；该仓库是一个由 `JMXSentinelPluginRepository` 提供的 `java.util.ServiceLoader` SPI，因此每个已安装的插件同时也是一个 JMX MBean，其启用状态可以在运行时切换。
- **可观测性钩子** —— `DefaultNodeEntryCallback`、`ProcessorSlotCallbackUtils`、`SentinelMetricsRepository` 与 `SentinelNodeEventPublisher` 挂接到 Sentinel 的统计槽回调注册表上，用来暴露节点事件与指标文件。
- **不强制引入依赖** —— `AbstractSentinelPlugin` 是所有插件的基类，`JMXSentinelPluginRepository` 将存储委托给 `SimpleSentinelPluginRepository`；`sentinel-core`、`sentinel-transport-simple-http`、`microsphere-java-core` 与 `microsphere-annotation-processor` 全部声明为 `optional`，因此本模块不会给下游增加传递依赖的负担。

## 版本信息

| 项目 | 值 |
| --- | --- |
| 分支 | `release` —— 文档位于 `docs/user-guide/current/` |
| 坐标 | `io.github.microsphere-projects:microsphere-alibaba-sentinel-commons:<version>` |
| 分支版本 / 已发布版本 | `0.2.6-SNAPSHOT` (`${revision}`) / `0.2.0` – `0.2.5` |
| 打包方式 / Java / 起始版本 | `jar` / 17 / 全部公开类型自 `1.0.0` 起 |
| POM 名称 | `Microsphere :: Alibaba Sentinel :: Commons` |
| 包 | `io.microsphere.alibaba.sentinel.common` 以及 `.callback`、`.constants`、`.init`、`.reposistory`、`.util`，还有 `io.microsphere.alibaba.sentinel.event`、`.jmx` |

## 兼容性

| 组件 | 此处管理的版本 | 运行时是否必需 |
| --- | --- | --- |
| Java | 17 | 是 |
| `com.alibaba.csp:sentinel-core`、`com.alibaba.csp:sentinel-transport-simple-http` | 1.8.10 | core 必需，transport 可选（控制台） |
| `microsphere-java-core`、`microsphere-annotation-processor` | 经 `microsphere-spring-cloud-dependencies` 0.2.26 管理 | 是（两者均声明为 optional） |
| `com.alibaba:fastjson` | 2.0.65 受管版本，但不是直接依赖 | 否 |
| Spring Cloud / Spring Boot | 2022.0.x – 2025.0.x / 3.x | 否 —— 本模块与框架无关 |

面向 Java 17 / Spring Boot 3.x / Spring Cloud 2022.0.x 及更高版本，请使用 `0.2.x` 版本线；已归档的 `0.1.x` 版本线面向 Java 8 / Spring Boot 2.x，只暴露本 API 的一个子集。

## 安装

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
        <artifactId>microsphere-alibaba-sentinel-commons</artifactId>
    </dependency>
    <dependency><!-- optional upstream, declare it explicitly -->
        <groupId>com.alibaba.csp</groupId>
        <artifactId>sentinel-core</artifactId>
        <version>1.8.10</version>
    </dependency>
</dependencies>
```

## API 参考

### `SentinelOperations`

`resourceName` 为 `@Nonnull`，`contextName` 与 `origin` 为 `@Nullable`。一次性的 `execute` 系列方法接受 `Runnable`、`Consumer<SentinelContext>` 或 `Function<SentinelContext, R>`；`call` 系列方法接受 `ThrowableAction`、`ThrowableConsumer<SentinelContext>` 或 `ThrowableFunction<SentinelContext, R>`，并抛出 `Throwable`。两者都提供 `(resourceName, callback)` 与 `(resourceName, contextName, origin, callback)` 两种形态，除此之外还有收窄受检异常类型的形式，以及两阶段的一对方法：

```java
<R, TR extends Throwable> R call(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin,
    @Nonnull ThrowableFunction<SentinelContext, R> callback, Class<TR> throwableClass) throws TR;
@Nonnull SentinelContext begin(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin) throws Exception;
void end(SentinelContext context);
```

`execute(resourceName, contextName, origin, callback)` 完全等价于 `call(resourceName, contextName, origin, callback::apply, RuntimeException.class)`——`execute` 是非受检异常的门面，`call` 是受检异常的门面。`call` 打开上下文、应用回调，并在 `finally` 中调用 `end(context)`；如果失败并不是阻塞异常（`BlockException.isBlockException(e)`），则在重新抛出之前先用 `Tracer.trace(e)` 上报，并以 `error` 级别记录日志。当流量被限制时，`begin` 可能抛出 `BlockException`。

### `SentinelTemplate implements SentinelOperations`

`SentinelTemplate()`、`SentinelTemplate(int resourceType)` 与 `SentinelTemplate(int resourceType, EntryType trafficType)` 的默认值是 `ResourceTypeConstants.COMMON` 和 `EntryType.IN`。`begin` 会在上下文名为空白时替换为 `SentinelConstants.DEFAULT_CONTEXT_NAME`，在 origin 为空白时替换为 `SentinelConstants.DEFAULT_ORIGIN`，随后执行 `ContextUtil.enter(...)`，再执行 `SphU.entry(resourceName, resourceType, trafficType)`，并返回一个新的 `SentinelContext`。`end` 仅在 `context.getFailure() != null` 时才调用 `Tracer.traceEntry(failure, entry)`，然后调用 `entry.exit()` 与 `ContextUtil.exit()`。

### `SentinelContext`

这是一个由 `ThreadLocal` 支撑的持有者，保存 `resourceName`、`contextName`、`origin` 以及 Sentinel 的 `Entry`；它的 `protected` 构造器会以 `IllegalArgumentException` 拒绝空的 `resourceName` / `contextName` 和为 null 的 `origin` / `entry`。返回 `this` 的链式方法有：`setResult(Object)`、`setFailure(Throwable)`、`setAttribute(String, Object)`、`removeAttributes()`、`withinContext()`。

文档明确说明的 null 语义：当回调没有返回值或执行失败时 `getResult()` 为 `null`；当执行成功时 `getFailure()` 为 `null`；属性不存在时 `getAttribute(name)` 为 `null`，而 `getAttribute(name, defaultValue)` 会返回默认值；`getAttributes()` 不可修改（在写入任何属性之前是一个空 Map）；`removeAttribute(name)` 返回被移除的值或 `null`。与线程绑定的 API 包括：`getContext()` / `removeContext()`（两者都是 `@Nullable`）、`setContext(@Nullable SentinelContext)`，以及 `doInContext(Consumer<SentinelContext>)` / `doInContext(Consumer<SentinelContext>, boolean forRemoval)`；当没有上下文绑定时它们什么都不做，而当 `forRemoval` 为 `true` 时会先解绑。

### `SentinelPlugin`、`AbstractSentinelPlugin`、`SimpleSentinelPlugin`

```java
boolean isAutoInstalled();   void setEnabled(boolean enabled);   @Nonnull String getName();
default boolean isEnabled() { return isPluginEnabled(getName()); }      // reads a JVM system property
@Nonnull default String getContextName() { return getDefaultContextName(getName()); }
@Nonnull String getOrigin();   int getResourceType();   @Nonnull EntryType getTrafficType();
static void install(SentinelPlugin plugin) throws NullPointerException;
```

`install` 会把插件加入 `SentinelPluginRepository.INSTANCE`，并注册一个在停机时卸载该插件的 shutdown hook。`AbstractSentinelPlugin` 用一个 `volatile boolean` 缓存启用标志，该标志在构造时由 `isEnabled()` 播种；它额外提供 `enable()` / `disable()`，并在 `autoInstalled` 为 `true` 时自行安装——其三参、四参和五参构造器将该默认值设为 `true`，资源类型为 `COMMON`，流量类型为 `IN`。`SimpleSentinelPlugin` 增加了一个只带名称的构造器，产出上下文 `microsphere_sentinel_default_context`、origin `""`、`COMMON`、`IN`、自动安装。

### `SentinelPluginRepository` 及其实现

```java
SentinelPluginRepository INSTANCE = loadFirstService(SentinelPluginRepository.class, true);
boolean install(SentinelPlugin plugin) throws NullPointerException;   // true only on first install
boolean isInstalled(String pluginName);   void clear();
@Nullable SentinelPlugin get(String pluginName);   @Nonnull Collection<SentinelPlugin> getAll();
default boolean uninstall(SentinelPlugin plugin) throws NullPointerException;
boolean uninstall(String pluginName) throws NullPointerException;
```

`SimpleSentinelPluginRepository` 把插件存放在以名称为键的 `ConcurrentHashMap` 中，使用 `putIfAbsent`，并在遇到重名时记录一条 `warn`。`JMXSentinelPluginRepository` 是列在 `META-INF/services/io.microsphere.alibaba.sentinel.common.SentinelPluginRepository` 中的提供者，因此也就是 `INSTANCE` 实际加载的实现：它把存储委托给一个 `SimpleSentinelPluginRepository`，并为每个 `SentinelPlugin` 注册一个 `StandardMBean`，域为 `io.microsphere.sentinel`，模式为 `io.microsphere.sentinel:type=SentinelPlugin,name={}`。`get` 具备自愈能力（一个已被注销的 MBean 会驱逐委托存储中的对应条目），`clear` 会注销该域下的所有 MBean，它的 `Prioritized` 优先级为 `DEFAULT_PRIORITY`（`9`）。

### 统计槽回调、指标与事件

- `DefaultNodeEntryCallback extends ProcessorSlotEntryCallback<DefaultNode>` 是入口回调的标记接口；`ProcessorSlotCallbackUtils` 基于 `StatisticSlotCallbackRegistry` 完成注册与查找（`addEntryCallback(s)`、`addExitCallback(s)`、`getEntryCallback(Class)`、`getExitCallback(Class)`、`removeEntryCallback(Class)`、`removeExitCallback(Class)`），键为回调类的全限定类型名。
- `SentinelMetricsRepository`（`@Spi`，是一个 `DefaultNodeEntryCallback`）在通过和阻塞时记录资源到上下文的映射，并通过 `MetricSearcher` 读取指标日志：`findMetricNodes(long, int)`、`findMetricNodes(long, long)`、`findMetricNodes(long, long, String resource)`、`findContextMetricNodesMap(long, long)`、`getResource(DefaultNode)`、`getContext(String)`（回退到 Sentinel 的 `CONTEXT_DEFAULT_NAME`），以及 `@Nullable` 的静态方法 `getSentinelMetricsRepository()`。检索失败会被记录日志并返回空列表或空 Map，绝不抛出异常。
- `SentinelNodeEventPublisher`（`@Spi`，是一个 `DefaultNodeEntryCallback`）为每个资源分发一个 `ClusterNodeAddedEvent`（暴露 `getClusterNode()`、`getContextName()`、`getResourceName()`），监听器通过链式的 `addEventListener` / `removeEventListener` 这一对方法管理（`ClusterNodeAddedEventListener` 是 `@FunctionalInterface`），并可选地传入一个 `Executor`；`getSentinelNodeEventPublisher()` 是 `@Nullable` 的。
- `StatisticSlotCallbackSpiLoaderInitFunc implements InitFunc` 列在 `META-INF/services/com.alibaba.csp.sentinel.init.InitFunc` 中，它在 Sentinel 启动时加载排好序的 `DefaultNodeEntryCallback` 与 `ProcessorSlotExitCallback` SPI 实例并注册它们，上述两个类正是借此在没有 DI 容器的情况下生效。

### `SentinelUtils` 与 `SentinelConstants`

`buildResourceName(Method)` 渲染出 `SimpleClassName.method(SimpleParam1,SimpleParam2)`，例如 `SentinelUtils.buildResourceName(Method)`。其余方法还有：`getFlowDataId(String)`（模式 `{}-flow-rules`）、`getPluginEnabledPropertyName(String)`、`isPluginEnabled(String)`、`getDefaultContextName(String)`（模式 `microsphere_sentinel_{}_context`）、`getResourceTypeAsString(int)`（无法映射时返回 `UNKNOWN`）、`findSentinelMetricsTaskExecutor()`、`getSentinelMetricsTaskExecutor()`、`resetContextMap()`（同时也会从 shutdown hook 执行一次）、`getMetricSearcher()` 与 `newMetricSearcher()`。

`SentinelConstants` 持有 `FLOW_DATA_ID_PATTERN`、`DEFAULT_CONTEXT_NAME_PATTERN`、`DEFAULT_CONTEXT_NAME`（`microsphere_sentinel_default_context`）、`DEFAULT_ORIGIN`（`""`）、`PROPERTY_NAME_PREFIX`（`microsphere.sentinel.`）、`ENABLED_PROPERTY_VALUE`（`"true"`）、`ENABLED_PROPERTY_NAME`（`microsphere.sentinel.enabled`）、`SENTINEL_CONTEXT_ATTRIBUTE_NAME`（`sentinel-context`）以及 `DEFAULT_PRIORITY`（`9`）。

## 配置属性

| 属性 | 类型 | 默认值 | 读取位置 | 含义 |
| --- | --- | --- | --- | --- |
| `microsphere.sentinel.enabled` | `boolean` | `true` | `SentinelConstants.ENABLED_PROPERTY_NAME`；Spring Boot 的 `ConditionalOnSentinelEnabled` 条件 | 全部 Sentinel 自动配置的全局开关 |
| `microsphere.sentinel.<pluginName>.enabled` | `boolean` | `true` | `SentinelUtils.isPluginEnabled(String)`，仅读取 JVM **系统属性** | 插件级开关，为 `SentinelPlugin.isEnabled()` 提供初始值 |

启用的判定语义是字面量的：`isPluginEnabled` 解析 `System.getProperty("microsphere.sentinel." + pluginName + ".enabled", "true")` 并返回 `!"false".equalsIgnoreCase(value)`——只有恰好为 `false`（忽略大小写）这一个字符串才会禁用，任何其他值，即使格式非法，插件都保持启用。该属性只在每个插件构造时读取一次；此后应使用 `setEnabled(boolean)`、`enable()` / `disable()`，或改用 JMX MBean。

## 用法示例

```java
// adapted from SentinelTemplateTest
SentinelTemplate template = new SentinelTemplate();
template.execute("test-resource", () -> { /* business logic */ });
String value = template.execute("test-resource", "test-context", "test-origin",
        context -> context.getResourceName());              // "test-resource"
List<String> list = template.call("test-resource", "test-context", "test-origin",
        context -> List.of(context.getOrigin()), SQLException.class);

// two-phase form, as the plugins use it: begin in beforeExecute, end in afterExecute
SentinelContext context = template.begin("test-resource", "test-context", "test-origin").withinContext();
context.setResult(result);
context.setFailure(failure);          // traced by Tracer.traceEntry inside end()
template.end(context);
SentinelContext.doInContext(c -> c.setAttribute("sql", "select 1"), true);   // consumes, then unbinds
assertNull(SentinelContext.getContext());
```

```java
// adapted from SimpleSentinelPluginTest and SentinelPluginRepositoryTest
SentinelPlugin plugin = new SimpleSentinelPlugin("my-plugin", DEFAULT_CONTEXT_NAME, DEFAULT_ORIGIN,
        ResourceTypeConstants.COMMON, EntryType.IN, false);  // last argument: do not self-install
SentinelPluginRepository repository = SentinelPluginRepository.INSTANCE;
assertTrue(repository.install(plugin));                     // duplicate name returns false, logs a warn
assertTrue(repository.isInstalled("my-plugin"));
plugin.disable();                                          // the enabled flag is runtime-mutable
repository.uninstall(plugin);
```

```java
// adapted from SentinelMetricsRepositoryTest, SentinelNodeEventPublisherTest, AbstractSentinelTemplateTest
SentinelMetricsRepository metrics = new SentinelMetricsRepository();
ProcessorSlotCallbackUtils.addEntryCallback(metrics);
Map<String, List<MetricNode>> byContext = metrics.findContextMetricNodesMap(startTime, endTime);
SentinelNodeEventPublisher publisher = new SentinelNodeEventPublisher();
ProcessorSlotCallbackUtils.addEntryCallback(publisher);
publisher.addEventListener(event -> System.out.println(event.getResourceName()));

FlowRule rule = new FlowRule();
rule.setResource("test-resource-1"); rule.setCount(1); rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
FlowRuleManager.loadRules(List.of(rule));
assertThrows(BlockException.class, () -> template.call("test-resource-1",
        () -> { throw new FlowException("For testing..."); }));
```

## 相关模块

- [microsphere-alibaba-sentinel-plugins](./microsphere-alibaba-sentinel-plugins.md) —— 六个插件的聚合模块
- [microsphere-alibaba-sentinel-alibaba-druid](./microsphere-alibaba-sentinel-alibaba-druid.md) 与 [microsphere-alibaba-sentinel-mybatis](./microsphere-alibaba-sentinel-mybatis.md) —— 本 API 的参考使用方
- [microsphere-alibaba-sentinel-spring](./microsphere-alibaba-sentinel-spring.md)、[microsphere-alibaba-sentinel-spring-boot](./microsphere-alibaba-sentinel-spring-boot.md) 与 [microsphere-alibaba-sentinel-spring-cloud](./microsphere-alibaba-sentinel-spring-cloud.md) —— 上述属性在 Spring 中的装配；[microsphere-alibaba-sentinel-dependencies](./microsphere-alibaba-sentinel-dependencies.md) —— BOM
- [分支索引](./README.md) · [仓库根目录](../../../../README.md)

---

## English

[English 版本](../microsphere-alibaba-sentinel-commons.md)
