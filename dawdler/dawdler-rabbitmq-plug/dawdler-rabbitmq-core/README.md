# dawdler-rabbitmq-core

## 模块介绍

提供通过pool2实现的rabbitmq连接池.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-rabbitmq-core</artifactId>
```

### 2. properties文件说明

```properties
host=localhost # rabbitmq服务器的ip地址,如果采用高可用的集群模式放置vip地址即可
port=5672 #端口号
virtualHost=/ #虚拟host
username=mq_user #用户名
password=mq_user #密码
networkRecoveryInterval=3000 #网络中断自动重连频率 3000ms 单位毫秒
shutdownTimeout=30000 #关闭超时时间 30000ms 单位毫秒
confirmSelect=true #开启confirm模式
pool.maxTotal=32 #最大连接
pool.maxWaitMillis=5000 #最大等待时长(单位毫秒)
pool.minIdle=0 #最小空闲数
pool.maxIdle=4 #最大空闲数
confirmSelect=true #开启confirm模式 一般配合ConfirmListener使用,参考RabbitProvider中publishIfFaildRetry方法
channel.size=16 #每个connection中的channel数量
channel.getTimeout=15000 #获取channel的超时事件(单位毫秒)
ttlTime=5000 #消费者消费失败后重试的时间 单位ms,需要配合@RabbitListener来使用 
testOnBorrow=true #获取之前校验连接
testOnCreate=false #创建后校验连接
testOnReturn=true #返回到池之前校验连接
```

### 3. RabbitInjector注解

用于注入RabbitProvider

RabbitInjector注解中的value传入fileName为配置文件名(不包含.properties后缀).

具体参考:

[dawdler-server-plug-rabbitmq 实现dawdler-server端注入功能.](../dawdler-server-plug-rabbitmq/README.md)

[dawdler-client-plug-rabbitmq 实现web端注入功能.](../dawdler-client-plug-rabbitmq/README.md)

### 4. RabbitListener注解

用于标识一个方法监听指定队列的消息

```java
/**
*
* @Title RabbitListener.java
* @Description 标注一个方法是否是rabbitmq的消费者 此方法格式固定为void methodName(Message message)
* @author jackson.song
* @date 2022年4月14日
* @version V1.0
* @email suxuan696@gmail.com
*/
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RabbitListener {
 
 /**
  * 指定rabbitmq的配置文件名
  */
 String fileName();
 
 /**
  * 队列名
  */
 String queueName();
 
 /**
  * 是否自动ack
  */
 boolean autoAck() default true;
 
 /**
  * 是否重试
  */
 boolean retry() default false;
 
 /**
  * 重试次数
  */
 int retryCount() default 12;
 
 /**
  * 当前消费者个数 不能大于channel.size=16 #每个connection中的channel数量
  */
 int concurrentConsumers() default 1;
 
 /**
  * prefetchCount来限制服务器端每次发送给每个消费者的消息数.
  */
 int prefetchCount() default 1;
 
}

```

### 5. RabbitProvider类

```java
//推送一条消息到队列 如: publish("", "queueName", null, "hello world".getBytes());
public void publish(String exchange, String routingKey, BasicProperties props, byte[] body) throws Exception {
  Connection con = null;
  Channel channel = null;
  try {
   con = connectionFactory.getConnection();
   channel = con.createChannel();
   channel.basicPublish(exchange, routingKey, props, body);
  } finally {
   if (channel != null) {
    channel.close();
   }
   if (con != null) {
    con.close();
   }
  }
 }
//推送一条消息到队列传入listener自行处理confirm事件(注意要在配置文件中开启confirmSelect=true) 如: publish("", "queueName", null, "hello world".getBytes(),listener);
 public void publish(String exchange, String routingKey, BasicProperties props, byte[] body,
   ConfirmListener listener) throws Exception {
  Connection con = null;
  Channel channel = null;
  try {
   con = connectionFactory.getConnection();
   channel = con.createChannel();
   channel.basicPublish(exchange, routingKey, props, body);
   channel.addConfirmListener(listener);
  } finally {
   if (channel != null) {
    channel.close();
   }
   if (con != null) {
    con.close();
   }
  }
 }

 /**
  * 
  * @Title: publishIfFaildRetry
  * @author jackson.song
  * @date 2022年4月15日
  * @Description 推送支持失败重试(发送到mq后没有获取到ack而获取到了nack这种情况) (注意要在配置文件中开启confirmSelect=true)
  * @param exchange
  * @param routingKey
  * @param props
  * @param body
  * @throws Exception
  *
  * 
  */
 public void publishIfFaildRetry(String exchange, String routingKey, BasicProperties props, byte[] body)
   throws Exception {
  Connection con = null;
  Channel channel = null;
  try {
   con = connectionFactory.getConnection();
   channel = con.createChannel();
   long deliveryTag = channel.getNextPublishSeqNo();
   channel.basicPublish(exchange, routingKey, props, body);
   LocalCacheMessage message = new LocalCacheMessage(deliveryTag, exchange, routingKey, props, body);
   localCacheMessages.put(deliveryTag, message);
   ConfirmListener listener = new ConfirmListener() {

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
     LocalCacheMessage message = localCacheMessages.get(deliveryTag);
     if (message != null) {
      try {
       publishIfFaildRetry(message.getExchange(), message.getRoutingKey(), message.getProps(),
         message.getBody());
      } catch (Exception e) {
       logger.error("", e);
      }
      localCacheMessages.remove(deliveryTag);
     }
    }

    @Override
    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
     if (multiple) {
      localCacheMessages.clear();
     } else {
      localCacheMessages.remove(deliveryTag);
     }
    }
   };
   channel.addConfirmListener(listener);
  } finally {
   if (channel != null) {
    channel.close();
   }
   if (con != null) {
    con.close();
   }
  }
 }

```

### 6. 在非dawdler架构下的使用方式

```java
//通过调用AMQPConnectionFactory的getInstance方法

public static AMQPConnectionFactory getInstance(String fileName);

//通过调用此方法来获取AMQPConnectionFactory,fileName是不包含后缀.properties.

//例如：传入fileName为myRabbitMQ,则需要在项目的classPath中创建配置文件myRabbitMQ.properties.

```

注意：AMQPConnectionFactory在客户端和服务器端中运行无需手动关闭,dawder会自动进行关闭相关资源.

在非dawdler架构下使用需要调用 AMQPConnectionFactory.shutdownAll(); 释放资源.
