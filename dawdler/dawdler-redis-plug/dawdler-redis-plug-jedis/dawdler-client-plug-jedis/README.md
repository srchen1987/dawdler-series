# dawdler-client-plug-jedis

## 模块介绍

实现web端注入JedisOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-plug-jedis</artifactId>
```

### 2. 使用方式

#### 2.1 Jedis的使用方式
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
#### 2.2 分布式锁的使用方式

通过@JedisLockInjector注解标识全局变量为 类型的变量即可.

```java
@Controller
public class UserController implements UserService {

    @JedisLockInjector(fileName = "myRedis")//myRedis为配置文件的名称,不包含后缀properties
    JedisDistributedLockHolder jedisDistributedLockHolder;

    @RequestMapping(value = "/synUser", method = RequestMethod.POST)
    public void synUser(String userId, Business business) {
        JedisDistributedLock lock = jedisDistributedLockHolder.createLock("lockKey:"+userId);
        try {
            lock.lock();
            // lock.lock(3000); //3秒等待锁
            // 模拟业务处理
             doSomething(business);
         } finally {
            lock.unlock();
         }
    }

    @RequestMapping(value = "/synUserNoWait", method = RequestMethod.POST)
    public void synUserNoWait(String userId, Business business) {
        JedisDistributedLock lock = jedisDistributedLockHolder.createLock("lockKey:"+userId);
        try {
            //获取不到锁 立即结束不等待
            if(lock.tryLock()){
                // 模拟业务处理
                 doSomething(business);
            }
         } finally {
            lock.unlock();
         }
    }
}
```

#### 2.3 web端支持注入的三种组件

1、 [web端controller](../../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../../dawdler-client-plug-web/README.md#5-handlerinterceptor-拦截器)

3、 [web端监听器WebContextListener](../../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)
