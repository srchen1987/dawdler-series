# dawdler-kafka-core

## 模块介绍

提供kafka消费者和生产者支持.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-kafka-core</artifactId>
```

### 2. properties文件说明

```properties
# Kafka服务器地址,支持多个用逗号分隔
bootstrap.servers=localhost:9092
# 消费者组ID
group.id=your-group-id
# 是否自动提交偏移量
enable.auto.commit=true
# 自动提交偏移量的间隔时间(单位毫秒)
auto.commit.interval.ms=1000
# 会话超时时间(单位毫秒)
session.timeout.ms=10000
# 心跳间隔时间(单位毫秒)
heartbeat.interval.ms=3000
# 每次poll的最大记录数
max.poll.records=500
# 消费者每次获取数据的最大字节数
fetch.max.bytes=52428800
# 消费者每个分区一次返回的最大字节数
max.partition.fetch.bytes=10485760
# 消费者key反序列化器
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
# 消费者value反序列化器
value.deserializer=org.apache.kafka.common.serialization.ByteArrayDeserializer
# 消费者offset重置策略 (可选: earliest, latest, none) 优先级低于注解配置 默认值为earliest
auto.offset.reset=earliest
# 生产者ack机制
acks=all
# 生产者重试次数
retries=3
# 生产者批量发送大小
batch.size=16384
# 生产者延迟发送时间(单位毫秒)
linger.ms=1
# 生产者缓冲区大小
buffer.memory=33554432
# 生产者key序列化器
key.serializer=org.apache.kafka.common.serialization.StringSerializer
# 生产者value序列化器
value.serializer=org.apache.kafka.common.serialization.ByteArraySerializer
# 安全协议 (可选)
security.protocol=SASL_PLAINTEXT # 或 SASL_SSL
# SASL 机制 (可选)
sasl.mechanism=PLAIN # 或 GSSAPI, SCRAM-SHA-256, SCRAM-SHA-512
# JAAS 配置 (可选)
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="your-username" password="your-password";
# Kerberos 服务名称 (当使用 GSSAPI 时)
sasl.kerberos.service.name=kafka
# Kerberos kinit命令 (可选，当使用 GSSAPI 时)
sasl.kerberos.kinit.cmd=/usr/bin/kinit
# Kerberos 票据续期窗口因子 (可选，当使用 GSSAPI 时)
sasl.kerberos.ticket.renew.window.factor=0.8
# Kerberos 票据续期抖动 (可选，当使用 GSSAPI 时)
sasl.kerberos.ticket.renew.jitter=0.05
# Kerberos 最小重新登录时间 (毫秒) (可选，当使用 GSSAPI 时)
sasl.kerberos.min.time.before.relogin=60000
# Kerberos keytab文件路径 (可选，当使用 GSSAPI 时，支持classpath路径)
# keytab文件会被自动复制到临时文件，临时文件路径会自动追加到JAAS配置中
kerberos.keytab=/etc/kafka/kafka.keytab
# Kerberos principal (可选，当使用 GSSAPI 时)
kerberos.principal=kafka/client@REALM.COM
# Kerberos krb5.conf文件路径 (JVM系统属性，可选，当使用 GSSAPI 时)
# 支持 classpath 路径（如：krb5.conf）或绝对文件系统路径（如：/etc/krb5.conf）
# 如果是 classpath 路径，文件会被自动复制到临时文件
kerberos.krb5.conf=/etc/krb5.conf
```

### 3. SASL/Kerberos (GSSAPI) 认证配置

#### 3.1 SASL/PLAIN 认证配置示例

```properties
# SASL/PLAIN 认证
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";
```

#### 3.2 SASL/SCRAM-SHA-256 认证配置示例

```properties
# SASL/SCRAM-SHA-256 认证
security.protocol=SASL_PLAINTEXT
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="admin" password="admin-secret";
```

#### 3.3 SASL/GSSAPI (Kerberos) 认证配置示例

##### 3.3.1 使用 keytab 文件

```properties
# SASL/GSSAPI (Kerberos) 认证 - 使用 classpath 中的 keytab 文件
security.protocol=SASL_PLAINTEXT
sasl.mechanism=GSSAPI
# JAAS 配置中不需要指定 keyTab，会自动从 kerberos.keytab 读取并追加
sasl.jaas.config=com.sun.security.auth.module.Krb5LoginModule required;
sasl.kerberos.service.name=kafka
sasl.kerberos.kinit.cmd=/usr/bin/kinit
sasl.kerberos.ticket.renew.window.factor=0.8
sasl.kerberos.ticket.renew.jitter=0.05
sasl.kerberos.min.time.before.relogin=60000
# keytab 文件路径，支持 classpath 路径（如：kafka.keytab 或 /etc/kafka/kafka.keytab）
kerberos.keytab=kafka.keytab
# Kerberos principal（可选，会自动追加到 JAAS 配置）
kerberos.principal=kafka/client@REALM.COM
kerberos.krb5.conf=/etc/krb5.conf
```

**说明：**

- `kerberos.keytab`和 `kerberos.keytab` 支持两种路径格式：

  - **classpath 路径**：如 `kafka.keytab`，文件需放在 `src/main/resources` 目录下
  - **绝对文件系统路径**：如 `/etc/kafka/kafka.keytab`，路径以 `/` 开头，直接使用原路径
- 最终生成的 JAAS 配置示例：

  ```properties
  com.sun.security.auth.module.Krb5LoginModule required useKeyTab=true keyTab="/tmp/kerberos-1234567890.keytab" principal="kafka/client@REALM.COM";
  ```

### 4. KafkaInjector注解

用于注入KafkaProvider

KafkaInjector注解中的value传入fileName为配置文件名(不包含.properties后缀).

具体参考:

[dawdler-server-plug-kafka 实现dawdler-server端注入功能.](../dawdler-server-plug-kafka/README.md)

[dawdler-client-plug-kafka 实现web端注入功能.](../dawdler-client-plug-kafka/README.md)

### 5. KafkaListener注解

用于标识一个方法监听指定topic的消息,dawdler的实现是一个类中可以有多个方法被KafkaListener注解标识.

```java
/**
 *
 * 标注一个方法是否是kafka的消费者 此方法格式固定为void methodName(Message message)
 * @author jackson.song
 * @version V1.0
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface KafkaListener {

     /**
       * 指定kafka的配置文件名
       */
      String fileName();

     /**
       * topic名
       */
      String topic();

     /**
       * 是否自动提交偏移量
       */
      boolean autoCommit() default true;

     /**
       * 会话超时时间(单位毫秒)
       */
      int sessionTimeoutMs() default 10000;

     /**
       * 心跳间隔时间(单位毫秒)
       */
      int heartbeatIntervalMs() default 3000;

     /**
       * 每次poll的最大记录数
       */
      int maxPollRecords() default 500;

     /**
       * 消费者线程数
       */
      int consumerThreads() default 1;

     /**
       * 关闭超时时间(毫秒)，用于优雅停机时等待任务完成
       * 不同消费者可以配置不同的超时时间
       */
      int shutdownTimeoutMs() default 5000;

     /**
       * 消费者offset重置策略 (可选: earliest, latest, none)
       * 不配置则使用Kafka官方默认值earliest
       * 优先级：注解 > 配置文件 > Kafka默认
       */
      String autoOffsetReset() default "";

}
```

#### 5.1 优雅停机超时配置

每个 `@KafkaListener` 可以独立配置 `shutdownTimeoutMs`，用于优雅停机时等待该消费者线程池完成当前任务：

```java
@KafkaListener(
    fileName = "orderKafka",
    topic = "order-topic",
    shutdownTimeoutMs = 10000  // 订单消费者等待10秒
)
public void handleOrder(Message message) {
    // 处理订单消息
}

@KafkaListener(
    fileName = "logKafka",
    topic = "log-topic",
    shutdownTimeoutMs = 3000   // 日志消费者等待3秒
)
public void handleLog(Message message) {
    // 处理日志消息
}
```

### 6. KafkaProvider类

```java
//推送一条消息到指定topic
public void publish(String topic, byte[] message) throws Exception

//推送一条消息到指定topic并传入key
public void publish(String topic, String key, byte[] message) throws Exception
```

### 7. 在非dawdler架构下的使用方式

```java
//通过调用KafkaProviderFactory的getKafkaProvider方法

public static KafkaProvider getKafkaProvider(String fileName);

//通过调用此方法来获取KafkaProvider,fileName是不包含后缀.properties.

//例如：传入fileName为myKafka,则需要在项目的classPath中创建配置文件myKafka.properties.

```

注意：KafkaProvider在客户端和服务器端中运行无需手动关闭,dawdler会自动进行关闭相关资源.

在非dawdler架构下使用需要调用 KafkaProviderFactory.shutdownAll(); 释放资源.
