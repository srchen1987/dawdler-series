# dawdler-cache-jedis

## 模块介绍

基于jedis实现的缓存模块.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-cache-jedis</artifactId>
```

### 2. 在web接口或service接口中的方法上使用注解

例:

```java
/**
 * 定义缓存,注意需要在web端或service扫描的包路径中定义.
 */
public class CacheConfigImpl implements CacheConfig {

 @Override
 public String getName() {
  return "userCache";
 }

 @Override
 public Long getMaxSize() {
  return 1024l;
 }

 @Override
 public Duration getExpireAfterAccessDuration() {
  return Duration.ofSeconds(30);
 }

 @Override
 public Duration getExpireAfterWriteDuration() {
  return null;
 }

 @Override
 public String getFileName() {
  return "jedis";
 }

}

```

```java
 @ResponseBody
 @RequestMapping(value = "/info/{userId}", method = RequestMethod.GET)
 @Cacheable(cacheManager = "jedis", cacheName = "userCache", key = "'userCache:'+userId", condition = "userId == 2", unless = "result.data == null")
 public BaseResult<User> info(@PathVariable("userId") Integer userId, @RequestHeader("site") String site) {
  return userService.selectByPrimaryKey(userId);
 }
```
