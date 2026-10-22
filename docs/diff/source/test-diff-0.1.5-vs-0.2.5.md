# Source Diff: `src/test` — tag 0.1.5 vs tag 0.2.5

[中文版](test-diff-0.1.5-vs-0.2.5.zh.md) · [Main diff (EN)](main-diff-0.1.5-vs-0.2.5.md) · [Main diff (ZH)](main-diff-0.1.5-vs-0.2.5.zh.md)

Compare the `src/test` content of the two latest tags, module by module:

- Tag `0.1.5` → Maven version `0.1.5` (Java 8 / Spring Boot 2.x release line)
- Tag `0.2.5` → Maven version `0.2.5` (Java 17 / Spring Boot 3.x release line)

Scope rules (same as the main diff): only same-named modules are compared; pure Java 8 vs Java 17 syntax/API differences and Java EE vs Jakarta EE differences are excluded from the tables and listed separately at the end.

## Module overview (`src/test`)

| Maven module | Test files 0.1.5 | Test files 0.2.5 | Result |
|---|---|---|---|
| `microsphere-alibaba-sentinel-commons` | 15 | 15 | Same file set; 1 file changed |
| `microsphere-alibaba-sentinel-alibaba-druid` | 2 | 2 | Same file set; 1 file changed |
| `microsphere-alibaba-sentinel-hibernate-core` | 3 + `persistence.xml` | 3 + `persistence.xml` | Same file set; 1 file changed |
| `microsphere-alibaba-sentinel-spring-web` | 2 | 2 | Same file set; 1 file changed |
| `microsphere-alibaba-sentinel-mybatis` | 2 | 2 | Identical |
| `microsphere-alibaba-sentinel-p6spy` | 2 + `spy.properties` | 2 + `spy.properties` | Identical |
| `microsphere-alibaba-sentinel-redis` | 2 | 2 | Identical |
| `microsphere-alibaba-sentinel-spring-boot` | 6 | 6 | Identical |
| `microsphere-alibaba-sentinel-spring-cloud` | 1 | 1 | Identical |
| `microsphere-alibaba-sentinel-spring` | 0 | 0 | Identical (no tests) |

No test class was added, removed or renamed in any matched module — the two release lines keep identical test inventories.

## Detailed differences (after excluding syntax-only changes)

### `microsphere-alibaba-sentinel-commons`

| File | Difference in 0.2.5 (vs 0.1.5) | Reason |
|---|---|---|
| `SimpleSentinelPluginTest.java` | In the `test()` switch over repetition cases, case 1 gains `assertTrue(this.plugin.isAutoInstalled())`; `plugin` references normalized to `this.plugin` | **Test-strength fix.** In 0.1.5 case 1 (the all-defaults constructor) never verified `isAutoInstalled()` although every other case did; 0.2.5 closes that coverage gap. The `this.` normalization is stylistic. (The rest of the file's diff is the Java 17 `switch` arrow syntax and is excluded.) |

### `microsphere-alibaba-sentinel-plugins/microsphere-alibaba-sentinel-hibernate-core`

| File | Difference in 0.2.5 (vs 0.1.5) | Reason |
|---|---|---|
| `SentinelHibernateEntityCallbackTest.java` | `postTest()` after entering the Sentinel context: 0.1.5 asserted `assertNotNull(context.getEntranceNode())`; 0.2.5 asserts `assertFalse(entranceNode.getChildList().isEmpty())` | **Stronger, meaningful assertion.** `Context.getEntranceNode()` is contractually non-null, so the old check was near-vacuous. The new assertion verifies that executing protected entity operations actually created child `Node`s (cluster/resource nodes) under the context entrance node — i.e. it tests the plugin's real effect on the Sentinel metric tree, not just object existence. |

### `microsphere-alibaba-sentinel-plugins/microsphere-alibaba-sentinel-spring-web`

| File | Difference in 0.2.5 (vs 0.1.5) | Reason |
|---|---|---|
| `SentinelHandlerMethodInterceptorTest.java` | Removed the `@BeforeEach setUp()` override that only delegated to `super.setUp()` | **Redundant boilerplate removal.** JUnit 5 already runs the `@BeforeEach` lifecycle method inherited from the test base class, so the explicit override that only delegated to `super.setUp()` never had any effect; 0.2.5 drops it. No behavior change. |

### `microsphere-alibaba-sentinel-plugins/microsphere-alibaba-sentinel-alibaba-druid`

| File | Difference in 0.2.5 (vs 0.1.5) | Reason |
|---|---|---|
| `SentinelAlibabaDruidFilterTest.java` | Cast replaced by `instanceof` pattern variable | **Syntax only — excluded.** After removing the Java 17 pattern-matching difference, the test logic is identical in both tags. |

## Excluded difference categories (for reference)

- **Java 17 syntax**: `switch` arrow labels in `SimpleSentinelPluginTest` (0.1.5 must use `case …: break;` for Java 8); pattern-matching `instanceof` in `SentinelAlibabaDruidFilterTest`.
- **Test dependency changes in POMs** (context, not `src/test` itself): `spring-web` module dropped JUnit 4 and `junit-vintage-engine` on the 0.2.x line (Java 17 / Spring Boot 3 tests run on the JUnit 5 platform only); `microsphere-hibernate-test` scope tightened to `test` in the `hibernate-core` module.

## Conclusion

Test inventories are identical across all matched modules. After stripping Java-version syntax differences, only three substantive test changes exist on the 0.2.5 line, all quality improvements rather than functional changes: a closed coverage gap for `isAutoInstalled()`, a stronger assertion on the Sentinel entrance-node child tree in the Hibernate callback test, and removal of a redundant `@BeforeEach` in the Spring Web interceptor test.
