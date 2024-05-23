# dawdler-server-plug-jedis

## 模块介绍

实现dawdler-server端注入JedisOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>io.github.dawdler-series</groupId>
 <artifactId>dawdler-server-plug-jedis</artifactId>
```

### 2. 使用方式

通过@JedisInjector注解标识全局变量为JedisOperator类型的变量即可.

```java
 
 public class UserServiceImpl implements UserService {

    @JedisInjector("myRedis")//myRedis为配置文件的名称,不包含后缀properties
    JedisOperator jedisOperator;

    public User getUser(String userId) {
        jedisOperator.set("userId", userId);//使用jedisOperator对象
        return null;
    }
 
 }

```

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@Service注解的接口实现类](../../../dawdler-service-plug/dawdler-service-core/README.md#2-service说明)

