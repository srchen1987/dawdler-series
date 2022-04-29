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

#### 2.1 web端支持注入的三种组件

1、 [web端controller](../../dawdler-client-plug/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug/README.md#5-HandlerInterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug/README.md#6-webcontextlistener-监听器)
