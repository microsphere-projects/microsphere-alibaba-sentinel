# 源码对比：`src/test` —— tag 0.1.5 vs tag 0.2.5

[English](test-diff-0.1.5-vs-0.2.5.md) · [主代码对比（中文）](main-diff-0.1.5-vs-0.2.5.zh.md) · [主代码对比（英文）](main-diff-0.1.5-vs-0.2.5.md)

按 Maven 模块逐一比较最新两个 tag 的 `src/test` 内容：

- Tag `0.1.5` → Maven 版本 `0.1.5`（Java 8 / Spring Boot 2.x 发布线）
- Tag `0.2.5` → Maven 版本 `0.2.5`（Java 17 / Spring Boot 3.x 发布线）

对比范围规则（与主代码对比一致）：仅对比同名模块；排除纯 Java 8 / Java 17 语法与 API 差异、Java EE / Jakarta EE API 差异，排除项在文末单独列出。

## 模块总览（`src/test`）

| Maven 模块 | 0.1.5 测试文件数 | 0.2.5 测试文件数 | 结果 |
|---|---|---|---|
| `microsphere-alibaba-sentinel-commons` | 15 | 15 | 文件集合一致；1 个文件有差异 |
| `microsphere-alibaba-sentinel-alibaba-druid` | 2 | 2 | 文件集合一致；1 个文件有差异 |
| `microsphere-alibaba-sentinel-hibernate-core` | 3 + `persistence.xml` | 3 + `persistence.xml` | 文件集合一致；1 个文件有差异 |
| `microsphere-alibaba-sentinel-spring-web` | 2 | 2 | 文件集合一致；1 个文件有差异 |
| `microsphere-alibaba-sentinel-mybatis` | 2 | 2 | 完全一致 |
| `microsphere-alibaba-sentinel-p6spy` | 2 + `spy.properties` | 2 + `spy.properties` | 完全一致 |
| `microsphere-alibaba-sentinel-redis` | 2 | 2 | 完全一致 |
| `microsphere-alibaba-sentinel-spring-boot` | 6 | 6 | 完全一致 |
| `microsphere-alibaba-sentinel-spring-cloud` | 1 | 1 | 完全一致 |
| `microsphere-alibaba-sentinel-spring` | 0 | 0 | 完全一致（无测试） |

所有可匹配模块中均没有新增、删除或重命名的测试类——两条发布线保持完全相同的测试清单。

## 详细差异（排除纯语法差异后）

### `microsphere-alibaba-sentinel-commons`

| 文件 | 0.2.5 相对 0.1.5 的差异 | 原因 |
|---|---|---|
| `SimpleSentinelPluginTest.java` | `test()` 中按调用次数分支的 switch：case 1 新增 `assertTrue(this.plugin.isAutoInstalled())`；`plugin` 引用统一为 `this.plugin` | **补齐断言覆盖。** 0.1.5 中 case 1（全默认构造器）是唯一未验证 `isAutoInstalled()` 的分支，其余分支均验证；0.2.5 闭合了这一覆盖缺口。`this.` 统一属于风格整理。（该文件其余差异为 Java 17 `switch` 箭头语法，已排除。） |

### `microsphere-alibaba-sentinel-plugins/microsphere-alibaba-sentinel-hibernate-core`

| 文件 | 0.2.5 相对 0.1.5 的差异 | 原因 |
|---|---|---|
| `SentinelHibernateEntityCallbackTest.java` | `postTest()` 进入 Sentinel 上下文后的断言：0.1.5 为 `assertNotNull(context.getEntranceNode())`；0.2.5 改为 `assertFalse(entranceNode.getChildList().isEmpty())` | **更有意义的强断言。** `Context.getEntranceNode()` 按契约永不为 null，旧断言近乎无效。新断言验证受保护的实体操作执行后确实在上下文入口节点下创建了子 `Node`（集群/资源节点）——即校验插件对 Sentinel 统计树的真实作用，而非对象存在性。 |

### `microsphere-alibaba-sentinel-plugins/microsphere-alibaba-sentinel-spring-web`

| 文件 | 0.2.5 相对 0.1.5 的差异 | 原因 |
|---|---|---|
| `SentinelHandlerMethodInterceptorTest.java` | 删除仅调用 `super.setUp()` 的 `@BeforeEach setUp()` 覆写 | **移除冗余样板。** JUnit 5 本身就会执行从测试基类继承来的 `@BeforeEach` 生命周期方法，这个仅委托给 `super.setUp()` 的显式覆写从来没有额外作用；0.2.5 将其删除。无行为变化。 |

### `microsphere-alibaba-sentinel-plugins/microsphere-alibaba-sentinel-alibaba-druid`

| 文件 | 0.2.5 相对 0.1.5 的差异 | 原因 |
|---|---|---|
| `SentinelAlibabaDruidFilterTest.java` | 强制类型转换改为 `instanceof` 模式变量 | **纯语法 —— 已排除。** 去掉 Java 17 模式匹配差异后，两个 tag 的测试逻辑完全一致。 |

## 已排除的差异类别（备查）

- **Java 17 语法**：`SimpleSentinelPluginTest` 中的 `switch` 箭头标签（0.1.5 因需支持 Java 8 只能使用 `case …: break;`）；`SentinelAlibabaDruidFilterTest` 中的 `instanceof` 模式匹配。
- **POM 测试依赖变化**（背景信息，非 `src/test` 内容）：`spring-web` 模块在 0.2.x 线移除 JUnit 4 与 `junit-vintage-engine`（Java 17 / Spring Boot 3 测试仅运行于 JUnit 5 平台）；`hibernate-core` 模块中 `microsphere-hibernate-test` 的 scope 收紧为 `test`。

## 结论

各可匹配模块的测试清单完全一致。剥离 Java 版本语法差异后，0.2.5 线仅有三处实质性测试变更，且全部是质量提升而非功能变化：补齐 `isAutoInstalled()` 断言覆盖、Hibernate 回调测试改用对 Sentinel 入口节点子树的强断言、移除 Spring Web 拦截器测试中冗余的 `@BeforeEach` 覆写。
