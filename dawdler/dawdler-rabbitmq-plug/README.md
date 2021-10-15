# dawdler-rabbitmq-plug

## 模块介绍

dawdler-rabbitmq-plug 提供通过pool2实现的连接池.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-rabbitmq-plug</artifactId>
```

### 2. properties文件说明

```properties
host=localhost # rabbitmq服务器的ip地址,如果采用高可用的集群模式放置vip地址即可
port=5672 #端口号
virtualHost=/ #虚拟host
username=mq_user #用户名
password=mq_user #密码
pool.maxTotal=32 #最大连接
pool.maxWaitMillis=5000 #最大等待时长(单位毫秒)
pool.minIdle=0 #最小空闲数
pool.maxIdle=4 #最大空闲数

channel.size=16 #每个connection中的channel数量
channel.getTimeout=15000 #获取channel的超时事件(单位毫秒)

testOnBorrow=true #获取之前校验连接
testOnCreate=false #创建后校验连接
testOnReturn=true #返回到池之前校验连接
```

### 3. 使用方式

```java
//通过调用AMQPConnectionFactory的getInstance方法

public static AMQPConnectionFactory getInstance(String fileName);

//通过调用此方法来获取AMQPConnectionFactory,fileName是不包含后缀.properties.

//例如：传入fileName为myRabbitMQ,则需要在项目的classPath中创建配置文件myRabbitMQ.properties.

```

注意：AMQPConnectionFactory在容器停止时需要配置Listener自行关闭.

参考[dawdler-distributed-transaction-client下的WebListener2ReleaseResources](../dawdler-distributed-transaction/dawdler-distributed-transaction-client/src/main/java/com/anywide/dawdler/distributed/transaction/client/listener/WebListener2ReleaseResources.java)在web容器停止时释放资源.

参考[dawdler-distributed-transaction-server下的DawdlerListener2ReleaseResources](../dawdler-distributed-transaction/dawdler-distributed-transaction-server/src/main/java/com/anywide/dawdler/distributed/transaction/server/listener/DawdlerListener2ReleaseResources.java)在dawdler容器停止时释放资源.
