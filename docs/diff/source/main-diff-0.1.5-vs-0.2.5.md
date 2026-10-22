# Source Diff: `src/main` — tag 0.1.5 vs tag 0.2.5

[中文版](main-diff-0.1.5-vs-0.2.5.zh.md) · [Test diff (EN)](test-diff-0.1.5-vs-0.2.5.md) · [Test diff (ZH)](test-diff-0.1.5-vs-0.2.5.zh.md)

Compare the `src/main` content of the two latest tags, module by module:

- Tag `0.1.5` → Maven version `0.1.5` (`revision=0.1.5-SNAPSHOT`, Java 8 / Spring Boot 2.x release line)
- Tag `0.2.5` → Maven version `0.2.5` (`revision=0.2.5-SNAPSHOT`, `java.version=17`, Java 17 / Spring Boot 3.x release line)

Scope rules:

- Only Maven modules with the same name in both tags are compared; unmatched modules are ignored (in this comparison all modules match).
- Differences that are purely Java 8 vs Java 17 syntax/API (e.g. `switch` arrow labels, pattern-matching `instanceof`) are excluded from the tables.
- Differences that are purely Java EE vs Jakarta EE API (e.g. `javax.servlet` → `jakarta.servlet` dependencies and imports) are excluded.
- What remains are genuine implementation differences, with the reason explained.

## Module overview (`src/main`)

| Maven module | Files in 0.1.5 | Files in 0.2.5 | Result |
|---|---|---|---|
| `microsphere-alibaba-sentinel-commons` | 20 | 20 | Identical |
| `microsphere-alibaba-sentinel-plugins/*` (6 plugin modules) | 3 each | 3 each | Identical |
| `microsphere-alibaba-sentinel-spring` | 0 (aggregator, no sources) | 0 | Identical |
| `microsphere-alibaba-sentinel-spring-boot` | 8 | 7 | Differences (see below) |
| `microsphere-alibaba-sentinel-spring-cloud` | 3 | 2 | Differences (see below) |
| `microsphere-alibaba-sentinel-parent` / `-dependencies` | POM only | POM only | No `src` to compare |

## Detailed differences

### `microsphere-alibaba-sentinel-spring-boot` — `src/main`

| File | 0.1.5 | 0.2.5 | Reason |
|---|---|---|---|
| `META-INF/spring.factories` | Present; registers the 4 auto-configuration classes under `EnableAutoConfiguration` | Deleted | **Registration-mechanism upgrade.** Since Spring Boot 2.7, `AutoConfiguration.imports` is the canonical mechanism and Boot 3.x no longer reads `EnableAutoConfiguration` from `spring.factories`. The 0.2.5 module keeps only `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (which already existed in 0.1.5 with the same 4 classes), so the Java auto-configuration code itself is unchanged — this is API compatibility, not a functional change. |
| `SentinelSpringWebAutoConfiguration.java` | Javadoc references `WebMvcAutoConfiguration` / `WebFluxAutoConfiguration` under `org.springframework.boot.autoconfigure.web.*` | Adds `@see` references to `org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration` and `org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration` | **Upstream package relocation (doc-only).** Spring Boot 4.x moved these auto-configurations to dedicated modules/packages; the 0.2.x line references the new locations while retaining the old ones for traceability. No behavioral change. |

### `microsphere-alibaba-sentinel-spring-cloud` — `src/main`

| File | 0.1.5 | 0.2.5 | Reason |
|---|---|---|---|
| `META-INF/spring.factories` | Present; registers `SentinelCloudAutoConfiguration` | Deleted | Same registration-mechanism upgrade as in the Boot module; `AutoConfiguration.imports` (identical in both tags) is now the single source of truth. |

### Modules with identical `src/main`

`commons`, `alibaba-druid`, `hibernate-core`, `mybatis`, `p6spy`, `redis`, `spring-web`, `spring` — byte-identical between the two tags (including SPI files under `META-INF/services` and the `redis` module's `spring.factories`, which uses a microsphere-redis interceptor extension point rather than Boot auto-configuration and is therefore kept in both lines).

## Excluded difference categories (for reference)

The following changes exist between the tags but were excluded per the scope rules:

- **Java 17 syntax/API**: `switch` arrow labels, pattern-matching `instanceof` (mostly in `src/test`, see the test diff).
- **Java EE → Jakarta EE**: `spring-web` module POM dependency `javax.servlet:javax.servlet-api` → `jakarta.servlet:jakarta.servlet-api`.
- **Upstream coordinate/version upgrades in POMs** (not `src/main`, listed here for context): `microsphere-*` BOM properties `0.1.x` → `0.2.x` in the parent POM; `org.hibernate` → `org.hibernate.orm` Hibernate coordinates (Jakarta-based Hibernate 6+); `hibernate-test` scope tightened to `test`; JUnit 4 / `junit-vintage-engine` dropped from `spring-web` test deps.

## Conclusion

Between 0.1.5 and 0.2.5 the production code (`src/main`) of every matched module is functionally identical. The only real differences are the removal of the legacy `spring.factories` auto-configuration registrations in the `spring-boot` and `spring-cloud` modules (API-compatibility cleanup for Spring Boot 3/4) and two Javadoc `@see` additions tracking Spring Boot's package relocation.
