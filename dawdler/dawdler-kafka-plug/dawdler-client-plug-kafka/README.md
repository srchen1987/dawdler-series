# dawdler-client-plug-kafka

## 模块介绍

实现web端注入KafkaProducer与KafkaListener注解的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-plug-kafka</artifactId>
```

### 2. 使用方式

生产者: 通过@KafkaInjector注解标识全局变量为KafkaProducer类型的变量即可.

消费者: 通过@KafkaListener标识消费者方法,方法参数为Message类型.

```java
 @Controller
 public class UserController{

    @KafkaInjector("myKafka")//myKafka为配置文件的名称,不包含后缀properties
    KafkaProducer kafkaProducer;

    @RequestMapping(value = "/pushMessage", method = RequestMethod.POST)
    public void pushMessage(String message) {
        kafkaProducer.send("test-topic", message.getBytes());//使用kafkaProducer对象
        return null;
    }

    @KafkaListener(fileName = "myKafka", topic = "test-topic", groupId = "test-group") //监听test-topic主题
    public void consumer(Message message) {
        System.out.println(new String(message.getBody()));
    }
 
 }
 
```

#### 2.1 web端支持注入的三种组件

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-handlerinterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)
