# dawdler-config-core

## 模块介绍

统一配置中心核心模块

### 1. pom中引入依赖

本模块是核心模块提供给clientside和serverside使用.

#### 1.1 web端的pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-config-clientside</artifactId>
```

### 1.2 dawdler服务端的pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-config-serverside</artifactId>
```

### 2. dawdler-config.yml配置文件

配置不同类型的配置中心与相关配置,目前只支持consul,支持自定义扩展.

```yml
consul:
 host: localhost
 port: 8500
 separator: 
 token: 
 wait-time: 10
 watch-keys: 
 - /orderConfig
 - /user
 #TLSConfig:
  #keyStoreInstanceType: JKS, JCEKS, PKCS12, PKCS11, DKS
  #certificatePath:
  #certificatePassword:
  #keyStorePath:
  #keyStorePassword:
```

host： consul服务器的ip

port： consul服务器暴露的端口

separator：分割符 一般无须配置,只在keys的场景有意义，如以下请求：
 设 目前已有目录 /config/config-uat /config/config-dev /config/config
访问 ```http://localhost:8500/v1/kv/config?keys&separator=-&wait=5s&index=2``` 返回 [
"config/config", "config/config-" ].

token：用于身份校验

wait-time：轮询超时长,单位秒数

 watch-keys: 监控的key,是一个list列表,只有被监控的key有变化才会刷新相关配置.

 TLSConfig：证书相关配置,如果有敏感数据方面建议采用此配置,具体参考：[consul encryption](https://www.consul.io/docs/security/encryption)

### 3. 安装consul

参考[consul downloads](https://www.consul.io/downloads) 即可完成安装.

consul在生产环境下一般使用集群模式,集群模式自行参考官方文档完成即可,建议通过nginx来负载多个集群节点.

单机配置启动如下：

```shell
consul agent -dev -data-dir=/data/consul/data -bind=192.168.43.128 -client=0.0.0.0 -ui &
```

dev：用于本地开发环境.

server： 以server身份启动,默认是client.

data-dir：data存放的目录.

bind：监听的ip地址,默认绑定0.0.0.0.

client: 客户端的ip地址，0.0.0.0对外公开任何ip都可以访问.

ui: 开启访问web管理界面.

启动完成 访问[consul管理界面](http://localhost:8500/) 验证是否成功.

### 4. FieldConfig注解使用

FieldConfig用于支持配置的类中的全局变量上.

FieldConfig 源码：

```java
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface FieldConfig {
 //配置中心的path 如 consul的path
 String path();
 //表达式 如 name 或 user.username 注意这里和SPEL不同,采用的Jexl来实现.
 String value() default "";

}
```

FieldConfig的使用的示例：

consul中的path为：orderConfig

yml文件内容

```yml
order:
 payOutTime: 3000
 queueName: orderChangeStatusQueue
```

controller中配置：

```java
@RequestMapping(value="/order")
public class OrderController{
 
 @RemoteService
 OrderService orderService;
 
 @FieldConfig(path="orderConfig",value = "order.queueName")
 private String queueName
```

另一个示例：

支持多层,同时也支持实体bean映射.

```yml
order:
 queueName: orderChangeStatusQueue
 payConfig:
  payOutTime: 3000
  payKey: UFFSA32FJJFJF
```

```java
 @FieldConfig(path="orderConfig",value = "order.payConfig.payKey")
 private String payKey
```

### 5. 其他配置中心扩展

如需要扩展其他配置中心,如zookeeper,apollo等等,需要实现ConfigClient接口,并通过SPI方式进行接入.

```java

public interface ConfigClient {

 void init(Map<String, Object> conf);

 void start();

 void stop();

 String type();

}

```

具体参考ConsulConfigClient来实现即可,如果觉得实现麻烦,有其他配置中心的需求可以提ISSUES.
