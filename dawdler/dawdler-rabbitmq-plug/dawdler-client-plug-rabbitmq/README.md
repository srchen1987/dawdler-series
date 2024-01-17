# dawdler-client-plug-rabbitmq

## 模块介绍

实现dawdler-client端注入RabbitProvider与RabbitListener注解的功能.

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

#### 2.1 web端支持注入的三种组件

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-HandlerInterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)
