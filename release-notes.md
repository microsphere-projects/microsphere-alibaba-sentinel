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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-alibaba-sentinel/compare/...0.2.0