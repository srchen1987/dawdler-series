# dawdler-client-plug-session

## 模块介绍

dawdler-client-plug-session是一套高性能分布式session的实现模块.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-session</artifactId>
```

### 2. identityConfig.properties文件说明

用于配置分布式会话的核心参数

```properties
cookieName=_dawdler_key
domain= #cookie的域,一般无须填写默认即可
path=/ #cookie中的path 默认为/
secure=false #是否是https 如果是https此参数需要设置为true
maxInactiveInterval=1800 #session过期时间 单位为秒 默认为1800秒
maxSize=165525 #最大支持多少用户在线,在线是指登录使用session存储
defense=true #是否开启session防御模式(防止恶意创建session)
ipMaxInactiveInterval=1800 #统计IP的过期时间 单位为秒 默认为1800秒
ipMaxSize=165525  #最大支持统计ip个数
ipLimit=8 #最大一个ip可以创建的session个数
```

### 3. session-redis.properties文件说明

分布式session基于redis实现,redis客户端配置参考[dawdler-redis-plug](../dawdler-redis-plug/README.md#2-properties文件说明)
