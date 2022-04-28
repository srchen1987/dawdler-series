# dawdler-client-plug-redis

## 模块介绍

dawdler-client-plug-redis 实现dawdler-client端注入功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-redis</artifactId>
```

### 2. 使用方式

通过@JedisInjector注解标识全局变量为JedisOperator类型的变量即可.

```java
 @Controller
 public class UserController{

    @JedisInjector("myRedis")//myRedis为配置文件的名称,不包含后缀properties
    JedisOperator jedisOperator;

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public User getUser(String userId) {
        jedisOperator.set("userId", userId);//使用jedisOperator对象
        return null;
    }
 
 }

```

可注入的范围:

1、 [Controller](../../dawdler-client-plug/README.md#2-1-创建Controller)

2、 [HandlerInterceptor拦截器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [WebContextListener监听器](../../dawdler-core/README.md#2-RemoteService注解)
