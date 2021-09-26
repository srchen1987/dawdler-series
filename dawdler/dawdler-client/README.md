# dawdler-client

## 模块介绍

dawdler-client 客户端核心代码，过滤器，服务发现，连接池，动态代理，aop实现，负载均衡等。

### 1. web端的pom中引入依赖

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
    <server-channel-group channel-group-id="defaultgroup"
                          service-path="services" connection-num="2"
                          session-num="4" serializer="2"
                          user="jackson.song" password="srchen">
    <!-- channel-group-id 标识id 一般用于@RemoteService(标识id)  zookeeper里面也配置相同的结构即可
 service-path dawdler下deploys下部署的项目名称，
 connection-num 连接数
 session-num 会话数
 serializer 序列化方式（1，jdk默认，2 kroy，支持扩展）
 user与password是帐号密码
  -->
    </server-channel-group>
    
<!-- 以下是动态加载配置 -->
    <loads-on>
        <item sleep="15000" channel-group-id="defaultgroup" mode="run">core</item><!-- 配置加载core模块  sleep 检查更新间隔 毫秒单位，channel-group-id指定组，mode=run 为运行模式 不在检查更新-->
        <item sleep="15000" channel-group-id="defaultgroup" mode="run">bbs</item><!-- 配置加载bbs模块 -->
    </loads-on>

</config>
```

### 3. api 调用方式

```java
public static void main(String[] args) throws Exception {
  Transaction tr = TransactionProvider.getTransaction("defaultgroup");
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
 HelloService hs = ServiceFactory.getService(HelloService.class, "defaultgroup");
 String response = hs.say("jackson");
```

### 5.  调用端过滤器 DawdlerClientFilter

实现DawdlerClientFilter接口，同时通过SPI方式扩展，具体可参考dawdler-circuit-breaker模块下的CircuitBreakerFilter。

### 6.  调用端负载均衡扩展

 继承AbstractLoadBalance抽象类，构造方法中传入的name即为RemoteService注解中的loadBalance(默认为roundRobin)，也为ServiceFactory中的getService方法参数loadBalance。

自行扩展请参考本模块下com.anywide.dawdler.client.cluster.impl.RoundRobinLoadBalance
 目前提供随机负载与轮询负载，如果需要其他实现可以自行扩展。
