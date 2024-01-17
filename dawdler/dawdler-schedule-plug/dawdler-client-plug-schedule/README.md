# dawdler-client-plug-schedule

## 模块介绍

实现dawdler-client端注入schedule的功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-schedule</artifactId>
```

### 2. 使用方式

通过@Schedule标识方法,方法参数必须为无参.

```java
 @Controller
 public class UserController{

   /**
    *每5秒执行一次
    **/
    @Schedule(cron="*/5 * * * * ?")
    public void schedule() {
        System.out.println(new Date());
    }
 
 }
```

#### 2.1 web端支持注入的三种组件

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-HandlerInterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)
