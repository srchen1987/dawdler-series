# dawdler-client-plug-redis

## 模块介绍

dawdler-client-plug-rabbitmq 实现dawdler-client端注入功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-rabbitmq</artifactId>
```

### 2. 使用方式

发送端者: 通过@RabbitInjector注解标识全局变量为RabbitProvider类型的变量即可.

消费者: 通过@RabbitListener标识消费者方法,方法参数为Message类型.

```java
 @Controller
 public class UserController{

    @RabbitInjector("myRabbitmq")//myRabbitmq为配置文件的名称,不包含后缀properties
    RabbitProvider rabbitProvider;

    @RequestMapping(value = "/pushMessage", method = RequestMethod.POST)
    public void pushMssage(String message) {
        rabbitProvider.publish("", "queueName", null, message.getBytes());//使用rabbitProvider对象
        return null;
    }

    @RabbitListener(fileName = "myRabbitmq",queueName = "test") //监听test队列
    public void consumer(Message message) {
        System.out.println(new String(message.getBody()));
    }
 
 }

```

可注入的范围:

1、 [Controller](../../dawdler-client-plug/README.md#2-1-创建Controller)

2、 [HandlerInterceptor拦截器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [WebContextListener监听器](../../dawdler-core/README.md#2-RemoteService注解)
