# dawdler-config-center-core

## 模块介绍

统一配置中心核心模块.

### 1. FieldConfig注解

FieldConfig用于支持配置的类中的全局变量上.

FieldConfig 源码:

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

FieldConfig的使用的示例:

consul中的path为:orderConfig

yml文件内容

```yml
order:
 payOutTime: 3000
 queueName: orderChangeStatusQueue
```

controller中配置:

```java
@RequestMapping(value="/order")
public class OrderController{
 
 @RemoteService
 OrderService orderService;
 
 @FieldConfig(path="orderConfig",value = "order.queueName")
 private String queueName
```

另一个示例:

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

### 2. 其他配置中心扩展

如需要扩展其他配置中心,如zookeeper,apollo等等,需要实现ConfigClient接口,并通过SPI方式进行接入.

```java

public interface ConfigClient {

 void init(Map<String, Object> conf);

 void start();

 void stop();

 String type();

}

```

具体参考ConsulConfigClient来实现即可,如果在扩展其他配置中心的时遇到困难可以提ISSUES.
