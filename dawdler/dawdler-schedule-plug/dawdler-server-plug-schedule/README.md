# dawdler-server-plug-schedule

## 模块介绍

实现dawdler-server端注入schedule的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-server-plug-schedule</artifactId>
```

### 2. 使用方式

通过@Schedule标识方法,方法参数必须为无参.

```java
 public class UserServiceImpl implements UserService {
    /**
    *每5秒执行一次
    **/
    @Schedule(cron="*/5 * * * * ?")
    public void schedule() {
        System.out.println(new Date());
    }
 
 }
```

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@Service注解的接口实现类](../../dawdler-service-plug/dawdler-service-core/README.md#2-service说明)
