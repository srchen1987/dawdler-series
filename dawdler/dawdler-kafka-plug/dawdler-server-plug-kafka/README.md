# dawdler-server-plug-kafka

## 模块介绍

实现dawdler-server端注入KafkaProducer与KafkaListener注解的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-server-plug-kafka</artifactId>
```

### 2. 使用方式

生产者: 通过@KafkaInjector注解标识全局变量为KafkaProducer类型的变量即可.

消费者: 通过@KafkaListener标识消费者方法,方法参数为Message类型.

```java
 
 public class UserServiceImpl implements UserService {

    @KafkaInjector("myKafka")//myKafka为配置文件的名称,不包含后缀properties
    KafkaProducer kafkaProducer;

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

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@Service注解的接口实现类](../../dawdler-service-plug/dawdler-service-core/README.md#2-service说明)
