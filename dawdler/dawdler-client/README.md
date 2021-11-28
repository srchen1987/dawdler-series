# dawdler-client

## 模块介绍

dawdler-client 客户端核心代码,过滤器,服务发现,连接池,动态代理,aop实现,负载均衡等.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client</artifactId>
```

### 2. client/client-conf.xml配置文件说明

例:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <zk-host>localhost:2181</zk-host><!-- zookeeeper的地址 目前只支持zk-->
    <certificatePath>${CLASSPATH}key/dawdler.cer</certificatePath><!-- 身份验证 公钥路径 -->
    <server-channel-group channel-group-id="user"
                            connection-num="2"
                          session-num="4" serializer="2"
                          user="jackson.song" password="srchen">
    <!-- channel-group-id 标识id 一般用于@RemoteService(标识id),在服务器端是dawdler下deploys下部署的项目名称.
 connection-num 连接数
 session-num 会话数
 serializer 序列化方式(1,jdk默认,2 kroy,支持扩展)
 user 帐号
 password 密码
  -->
    </server-channel-group>
</config>
```

### 3. api调用方式

```java
public static void main(String[] args) throws Exception {
  Transaction tr = TransactionProvider.getTransaction("user");
  tr.setServiceName("com.anywide.dawdler.demo.service.HelloService");//接口全名
  tr.setMethod("say");//方法名
  tr.addString("jackson");//参数 String类型并传值 Transaction有一系列传参方法 具体查看Transaction
  Object obj = tr.executeResult();//执行
  
  System.out.println(obj);
  
  ConnectionPool.shutdown(); 
 }
```

### 4. interface proxy 调用方式

```java
 HelloService hs = ServiceFactory.getService(HelloService.class, "user");
 String response = hs.say("jackson");
```

### 5. 调用端过滤器 DawdlerClientFilter

实现DawdlerClientFilter接口,同时通过SPI方式扩展,支持@Order注解进行升序排序,具体可参考[dawdler-circuit-breaker模块下的CircuitBreakerFilter](../dawdler-circuit-breaker/src/main/java/com/anywide/dawdler/breaker/filter/CircuitBreakerFilter.java).

### 6. 调用端负载均衡SPI扩展

目前提供随机负载与轮询负载.

继承AbstractLoadBalance抽象类,参考[RoundRobinLoadBalance](src/main/java/com/anywide/dawdler/client/cluster/impl/RoundRobinLoadBalance.java).构造方法中传入的name对应RemoteService注解中的loadBalance(默认为roundRobin).通过SPI方式配置[LoadBalance文件中](src/main/resources/META-INF/services/com.anywide.dawdler.client.cluster.LoadBalance).

### 7. 异步调用

dawdler提供异步调用rpc的方式.

#### 7.1 api调用

```java
public static void main(String[] args) throws Exception {
  Transaction tr = TransactionProvider.getTransaction("user");
  tr.setServiceName("com.anywide.dawdler.demo.service.HelloService");//接口全名
  tr.setMethod("say");//方法名
  tr.addString("jackson");//参数 String类型并传值 Transaction有一系列传参方法 具体查看Transaction
  tr.setAsync(true);//设置为异步执行
  Object obj = tr.executeResult();//异步执行拿不到结果 返回的是null

  obj = AsyncInvokeFutureHolder.getContext().getInvokeFuture().getResult();//获取异步执行结果
  System.out.println(obj);
  
  ConnectionPool.shutdown(); 
 }
```

#### 7.2 api调用

```java
public static void main(String[] args) throws Exception {
  Transaction tr = TransactionProvider.getTransaction("user");
  tr.setServiceName("com.anywide.dawdler.demo.service.HelloService");//接口全名
  tr.setMethod("say");//方法名
  tr.addString("jackson");//参数 String类型并传值 Transaction有一系列传参方法 具体查看Transaction
  tr.setAsync(true);//设置为异步执行
  Object obj = tr.executeResult();//异步执行拿不到结果 返回的是null

  obj = AsyncInvokeFutureHolder.getContext().getInvokeFuture().getResult();//获取异步执行结果
  System.out.println(obj);
  
  ConnectionPool.shutdown(); 
 }
```
