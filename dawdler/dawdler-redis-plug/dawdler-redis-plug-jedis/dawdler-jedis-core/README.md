# dawdler-jedis-core

## 模块介绍

jedis实现redis的插件,支持单机模式与哨兵模式.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-jedis-core</artifactId>
```

### 2. properties文件说明

```properties
#######################
masterName=masterName #哨兵模式下的masterName (注意：哨兵与单机只能用一种,用单机就不能配置此项)
sentinels=192.168.0.2:26379,192.168.0.3:26379,192.168.0.4:26379 #哨兵列表(注意：哨兵与单机只能用一种,用单机就不能配置此项)
#######################
addr=127.0.0.1 #单机ip
port=6379 #单机端口
######################

userName=redis_user #redis6之后支持设置用户名,如果不需要注释掉此项
auth=password #密码
max_active=20 #最大连接数
max_idle=8 #最大空闲数
max_wait=10000 #最大等待时长(单位毫秒)
timeout=10000 #超时时间(单位毫秒)
test_on_borrow=false #获取连接时是否验证连接有效
database=0 #使用指定数据槽
```

### 3. JedisInjector注解

用于注入JedisOperator,JedisOperator的方法参考Jedis类官方文档.

JedisInjector注解中的value传入fileName为配置文件名(不包含.properties后缀).

具体参考:

[dawdler-server-plug-redis 实现dawdler-server端注入功能.](../dawdler-server-plug-redis/README.md)

[dawdler-client-plug-redis 实现dawdler-client端注入功能.](../dawdler-client-plug-redis/README.md)

### 4. 在非dawdler架构下的使用方式

```java
// //通过调用JedisPoolFactory的getJedisPool方法

public static Pool<Jedis> getJedisPool(String fileName); 

//通过调用此方法来获取Pool<Jedis>,fileName是不包含后缀.properties.

//例如：传入fileName为myRedis,则需要在项目的classPath中创建配置文件myRedis.properties.

```

注意：Pool<Jedis>在客户端和服务器端中运行无需手动关闭,dawder会自动进行关闭相关资源.

在非dawdler架构下使用需要调用 JedisPoolFactory.shutdownAll(); 释放资源.
