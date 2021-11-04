# dawdler-core

## 模块介绍

dawdler-core 是dawdler-server与dawdler-client公用的核心模块.包含网络,服务发现实现,线程池,注解,压缩算法等(系统内部使用,用户无须关注).相关注解在不同的模块有说明.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-core</artifactId>
```

### 2. RemoteService注解

@RemoteService可以用于服务端,也可以用于调用端.

RemoteService参数介绍：

```java
 String value() default "";//服务调用

 String group() default "";//在调用端有效,指定服务端部署的服务名.

 boolean single() default true;//在服务端有效,标识一个服务的实现类是否为单例.默认为单例.

 boolean remote() default false;//在服务端有效,标识是否是一个远程服务,一般不建议在服务端再次调用另一个服务,默认为否,调用本服务中的服务(适用事务传播).

 int timeout() default 120;//在调用端有效,调用远程服务的超时事件,单位为秒,默认120秒.

 boolean fuzzy() default true;//在调用端有效,是否模糊匹配方法,默认为true,模糊匹配根据方法名与参数个数进行匹配,非模糊匹配会根据方法名与参数类型进行精确匹配.模糊匹配效率高,如果一个服务实现类中存在相同方法相同参数个数时需要设置此参数为true.

 String loadBalance() default "roundRobin";//调用端有效,负载方式

```
