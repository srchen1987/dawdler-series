# dawdler-jedis-core

## 模块介绍

jedis实现redis的插件,支持单机模式与哨兵模式.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-jedis-core</artifactId>
```

### 2. properties文件说明

```properties
#######################
#哨兵模式下的masterName (注意：哨兵与单机只能用一种,用单机就不能配置此项)
masterName=masterName
#哨兵列表(注意：哨兵与单机只能用一种,用单机就不能配置此项)
sentinels=192.168.0.2:26379,192.168.0.3:26379,192.168.0.4:26379
#sentinel用户名
sentinelUser=sentinelUser
#sentinel密码
sentinelPassword=sentinelPassword
#######################
#单机ip
host=127.0.0.1
#单机端口
port=6379
#######################

#redis6之后支持设置用户名,如果不需要注释掉此项
user=redis_user
#密码
password=password
#最大连接数
pool.maxTotal=20
#最小空闲数
pool.minIdle=2
#最大空闲数
pool.maxIdle=8
#最大等待时长(单位毫秒)
pool.maxWaitMillis=10000
#获取连接时是否验证连接有效 默认为false
pool.testOnBorrow=false
#创建连接时是否验证连接有效 默认为false
pool.testOnCreate=false
#返还连接时是否验证连接有效 默认为false
pool.testOnReturn=false
#超时时间(单位毫秒)
timeout=10000
#使用指定数据槽
database=0
```

### 3. JedisInjector注解

用于注入JedisOperator,JedisOperator的方法参考Jedis类官方文档.

JedisInjector注解中的value传入fileName为配置文件名(不包含.properties后缀).

具体参考:

[dawdler-server-plug-redis 实现dawdler-server端注入功能.](../dawdler-server-plug-jedis/README.md)

[dawdler-client-plug-redis 实现web端注入功能.](../dawdler-client-plug-jedis/README.md)

### 4. 在非dawdler架构下的使用方式

```java
// //通过调用JedisPoolFactory的getJedisPool方法

public static Pool<Jedis> getJedisPool(String fileName); 

//通过调用此方法来获取Pool<Jedis>,fileName是不包含后缀.properties.

//例如：传入fileName为myRedis,则需要在项目的classPath中创建配置文件myRedis.properties.

```

注意：Pool```<Jedis>```在客户端和服务器端中运行无需手动关闭,dawdler会自动进行关闭相关资源.

在非dawdler架构下使用需要调用 JedisPoolFactory.shutdownAll(); 释放资源.

### 5. 分布式锁的使用方式

分布式锁基于redis的lua脚本实现.

JedisLockInjector注解用于注入JedisDistributedLockHolder.

```java
/**
 * @author jackson.song
 * @version V1.0
 * 标注一个成员变量 注入JedisDistributedLock
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JedisLockInjector {
	/**
	 * 配置文件名
	 */
	String fileName();

	/**
	 * 下一次重试等待，单位毫秒
	 */
	int intervalInMillis() default JedisDistributedLockHolder.DEFAULT_INTERVAL_IN_MILLIS;

	/**
	 * 锁的过期时长，单位毫秒
	 */
	long lockExpiryInMillis() default JedisDistributedLockHolder.DEFAULT_LOCK_EXPIRY_IN_MILLIS;

	/**
	 * 是否启用看门狗,用于延时未处理完的操作,默认开启
	 */
	boolean useWatchDog() default true;

}
```

具体参考:

[dawdler-server-plug-redis 实现dawdler-server端注入功能.](../dawdler-server-plug-jedis/README.md)

[dawdler-client-plug-redis 实现web端注入功能.](../dawdler-client-plug-jedis/README.md)
