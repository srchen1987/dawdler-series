# dawdler-cache-core

## 模块介绍

缓存核心模块提供注解(Cacheable、CacheEvict、CachePut),CacheManager缓存管理器,CacheConfig缓存配置.

### 1. 核心模块提供注解(Cacheable、CacheEvict、CachePut)

#### 1.1 Cacheable

@Cacheable用于方法组件的方法上,存在缓存则返回缓存信息,不存在则调用方法将结果设置缓存后返回信息.

1、如果符合入参的条件表达式(condition)并获取到缓存则直接返回.

2、如果符合入参的条件表达式(condition)并获取不到缓存则执行方法,如果不符合方法结果表达式(unless)放入缓存后并返回结果,否则直接返回结果.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
 
 /**
  * 指定cacheManager 如:caffeine或jedis
  */
 String cacheManager();

 /**
  * 缓存的名字
  */
 String cacheName();

 /**
  * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
  */
 String key() default "";

 /**
  * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名
  * 只有入参符合条件的才会放入缓存
  */
 String condition() default "";

 /**
  * 方法结果的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为result,result为方法执行结果的key
  * 只有结果集不符合这个条件才会放入缓存
  */
 String unless() default "";
 
}
```

#### 1.2 CacheEvict

@CacheEvict 用于删除缓存信息

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {
 
 /**
  * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
  */
 String key() default "";

 /**
  * 缓存的名字的数组
  */
 String[] cacheNames();

 /**
  * 指定cacheManager 如:caffeine或jedis
  */
 String cacheManager() default "";

 /**
  * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名
  * 只有入参符合条件的才会删除缓存
  */
 String condition() default "";

 /**
  * 是否清空全部缓存
  */
 boolean allEntries() default false;

 /**
  * 是否在方法执行前就清空
  */
 boolean beforeInvocation() default false;

}
```

#### 1.3 CachePut

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CachePut {

 /**
  * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
  */
 String key();

 /**
  * 缓存的名字
  */
 String cacheName();

 /**
  * 指定cacheManager 如:caffeine或jedis
  */
 String cacheManager() default "";

 /**
  * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名
  * 只有入参符合条件的才会放入缓存
  */
 String condition() default "";

 /**
  * 方法结果的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为result,result为方法执行结果的key
  * 只有结果集不符合这个条件才会放入缓存
  */
 String unless() default "";

}
```

### 2. CacheConfig缓存配置接口

用于配置缓存的接口,需要在web端或service端定义此接口的实现类(组件 web需要load,服务端需要扫描路径).

```java

public interface CacheConfig {

 /**
  * 缓存名称
  */
 String getName();

 /**
  * 最大个数
  */
 Long getMaxSize();

 /**
  * 访问后过期时间
  */
 Duration getExpireAfterAccessDuration();

 /**
  * 缓存有效期
  */
 Duration getExpireAfterWriteDuration();

 /**
  * 序列化方式默认为 KRYO
  */
 default SerializeType getSerializeType() {
  return SerializeType.KRYO;
 }

 /**
  * 配置文件名（如用到dawdler-cache-jedis配置redis时需要）
  */
 default String getFileName() {
  return null;
 }

}

```

### 3. 缓存模块扩展方式

目前实现了caffeine与redis两种方式.如果需要扩展其他缓存组件按以下步骤实现.

1、创建项目并创建类继承AbstractCacheManager抽象类,实现 createCacheNative抽象方法.

2、通过SPI配置com.anywide.dawdler.cache.CacheManager具体实现.

具体实现可以参考CaffeineCacheManager或JedisCacheManager.

# dawdler-cache-core

## 模块介绍

缓存核心模块提供注解(Cacheable、CacheEvict、CachePut),CacheManager缓存管理器,CacheConfig缓存配置.

### 1. 核心模块提供注解(Cacheable、CacheEvict、CachePut)

#### 1.1 Cacheable

@Cacheable用于方法组件的方法上,存在缓存则返回缓存信息,不存在则调用方法将结果设置缓存后返回信息.

1、如果符合入参的条件表达式(condition)并获取到缓存则直接返回.

2、如果符合入参的条件表达式(condition)并获取不到缓存则执行方法,如果不符合方法结果表达式(unless)放入缓存后并返回结果,否则直接返回结果.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable {
 
 /**
  * 指定cacheManager 如:caffeine或jedis
  */
 String cacheManager();

 /**
  * 缓存的名字
  */
 String cacheName();

 /**
  * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
  */
 String key() default "";

 /**
  * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名
  * 只有入参符合条件的才会放入缓存
  */
 String condition() default "";

 /**
  * 方法结果的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为result,result为方法执行结果的key
  * 只有结果集不符合这个条件才会放入缓存
  */
 String unless() default "";
 
}
```

#### 1.2 CacheEvict

@CacheEvict 用于删除缓存信息

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEvict {
 
 /**
  * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
  */
 String key() default "";

 /**
  * 缓存的名字的数组
  */
 String[] cacheNames();

 /**
  * 指定cacheManager 如:caffeine或jedis
  */
 String cacheManager() default "";

 /**
  * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名
  * 只有入参符合条件的才会删除缓存
  */
 String condition() default "";

 /**
  * 是否清空全部缓存
  */
 boolean allEntries() default false;

 /**
  * 是否在方法执行前就清空
  */
 boolean beforeInvocation() default false;

}
```

#### 1.3 CachePut

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CachePut {

 /**
  * 缓存的key 支持JEXL表达式 内置变量为方法中变量名
  */
 String key();

 /**
  * 缓存的名字
  */
 String cacheName();

 /**
  * 指定cacheManager 如:caffeine或jedis
  */
 String cacheManager() default "";

 /**
  * 执行入参的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为方法中变量名
  * 只有入参符合条件的才会放入缓存
  */
 String condition() default "";

 /**
  * 方法结果的条件表达式,支持JEXL表达式,表达式的结果必须是true或false 内置变量为result,result为方法执行结果的key
  * 只有结果集不符合这个条件才会放入缓存
  */
 String unless() default "";

}
```

### 2. CacheConfig缓存配置接口

用于配置缓存的接口,需要在web端或service端定义此接口的实现类(组件 web需要load,服务端需要扫描路径).

```java

public interface CacheConfig {

 /**
  * 缓存名称
  */
 String getName();

 /**
  * 最大个数
  */
 Long getMaxSize();

 /**
  * 访问后过期时间
  */
 Duration getExpireAfterAccessDuration();

 /**
  * 缓存有效期
  */
 Duration getExpireAfterWriteDuration();

 /**
  * 序列化方式默认为 KRYO
  */
 default SerializeType getSerializeType() {
  return SerializeType.KRYO;
 }

 /**
  * 配置文件名（如用到dawdler-cache-jedis配置redis时需要）
  */
 default String getFileName() {
  return null;
 }

}

```

### 3. 缓存模块扩展方式

目前实现了caffeine与redis两种方式.如果需要扩展其他缓存组件按以下步骤实现.

1、创建项目并创建类继承AbstractCacheManager抽象类,实现 createCacheNative抽象方法.

2、通过SPI配置com.anywide.dawdler.cache.CacheManager具体实现.

具体实现可以参考CaffeineCacheManager或JedisCacheManager.

### 4. 使用范围

适用于controller层,service层使用缓存注解.
