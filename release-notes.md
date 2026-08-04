# Release Notes

## v0.2.0

# Release Notes - Version 0.2.0

## New Features
- **Sentinel Spring Cloud Integration**:  
  - Added Spring Cloud Sentinel module.  
  - Introduced Sentinel Spring Cloud auto-configuration for seamless integration.  
- **Improved Sentinel Context Handling**:  
  - Auto-install Sentinel DB plugins by default.  
  - Reset Sentinel context map on shutdown.  

## Bug Fixes
- Removed unused `SentinelPlugin` import.  
- Fixed Sentinel node assertion issues in callback, MyBatis, and web tests.  
- Strengthened context handling in Druid filter tests.  

## Documentations
- Refined Sentinel condition annotation documentation.  
- Formatted README to display branch versions as code for clarity.  

## Dependency Updates
- Bumped `microsphere-redis` to `0.2.12`.  
- Bumped `microsphere-hibernate` to `0.2.8`.  
- Bumped `microsphere-mybatis` to `0.2.14`.  
- Bumped `microsphere-alibaba-druid` to `0.2.19`.  
- Bumped Spring Cloud parent to `0.2.24`.  
- Updated Microsphere parent and BOM versions.  

## Test Improvements
- Reused shared Redis test configurations in interceptor tests.  
- Improved Sentinel context node assertions across MyBatis, web, and callback tests.  
- Added post-test Sentinel node assertions.  

## Build and Workflow Enhancements
- Refined Maven publish and release workflows to ensure test services start prior to publishing.  
- Reordered and refined dependencies in POM files.  
- Removed `spring-cloud` BOM from the parent POM.  

## Other Changes
- Updated several internal components such as installing Sentinel plugins explicitly and improving dependency management within the commons module.  

---

For the complete list of changes, please refer to the **Full Changelog**.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-sentinel/compare/...0.2.0## v0.2.1

_Release notes generation failed. Raw commits since 0.2.0:_

```
e8676e2 chore: merge main into release [skip ci]
c812550 Merge pull request #51 from microsphere-projects/dependabot/maven/com.alibaba-fastjson-2.0.63
24d8d90 chore: merge main into release [skip ci]
e2aa23a Use static field access in SentinelUtils
c78711d Fix Sentinel scheduler field lookup
8e1f85b Bump microsphere-redis to 0.2.13
ed01b43 Bump microsphere-hibernate to 0.2.9
172e00e Bump microsphere-mybatis to 0.2.15
23877b5 Bump microsphere-alibaba-druid to 0.2.20
b1300a6 chore: merge main into release [skip ci]
ac11ad7 Update repo links and parent version
ac6d25a Bump com.alibaba:fastjson from 2.0.62 to 2.0.63
53af98e chore: merge main into release [skip ci]
9d32f8f Relax flaky metric node count assertion
07c650a chore: merge main into release [skip ci]
8dcd00a Add Sentinel metrics repository and test base
dc9ea96 chore: merge main into release [skip ci]
c395f8a Add MetricSearcher helper to SentinelUtils
7290e41 chore: merge main into release [skip ci]
09d7b26 Use entry callbacks for node event publishing
6a84a34 chore: merge main into release [skip ci]
50b60d5 Use shared iteration count in event test
f701274 chore: merge main into release [skip ci]
f0e058b Clean up ClusterNodeAddedEventListener Javadoc
93645d5 Add Sentinel cluster node added event publisher
c1e758d Add contributor code of conduct
ac90fdc chore: merge release into main [skip ci]
bec41b9 chore: bump version to next patch after publishing 0.2.0
```

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-sentinel/compare/0.2.0...0.2.1