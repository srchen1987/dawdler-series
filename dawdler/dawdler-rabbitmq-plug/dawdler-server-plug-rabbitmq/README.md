# dawdler-server-plug-rabbitmq

## 模块介绍

实现dawdler-server端注入RabbitProvider与RabbitListener注解的功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-server-plug-rabbitmq</artifactId>
```

### 2. 使用方式

发送端者: 通过@RabbitInjector注解标识全局变量为RabbitProvider类型的变量即可.

消费者: 通过@RabbitListener标识消费者方法,方法参数为Message类型.

```java
 
 public class UserServiceImpl implements UserService {

    @RabbitInjector("myRabbitmq")//myRabbitmq为配置文件的名称,不包含后缀properties
    RabbitProvider rabbitProvider;

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

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@RemoteService注解的接口实现类](../../dawdler-core/README.md#2-RemoteService注解)
