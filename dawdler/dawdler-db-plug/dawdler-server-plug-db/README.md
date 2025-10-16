# dawdler-server-plug-db

## 模块介绍

服务端事务模块,提供事务管理器,提供与spring基本一致的事务应用,支持读写分离(一般不需要单独引用,由mybaits或dao模块依赖引用).

### 1. pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-server-plug-db</artifactId>
```

### 2. services-conf.xml 事务的配置文件说明

services-conf.xml是服务端核心配置文件，包含了数据源定义，指定目标包定义数据源，读写分离配置，服务端配置.

本模块中涉及事务的配置如下:

```xml
    <datasource-expressions>
        <datasource-expression id="order-datasource"
            latent-expression="write=[user_writeDataSource],read=[user_readDataSource|user_readDataSource1]" /><!-- 
            数据源表达式配置 id为标识 latent_expression为读写配置 其中write为写连接 read为读连接 读连接可以配置多个用|分开 
            轮询方式调用 -->
    </datasource-expressions>
    <decisions>
        <!-- mapping 需要注入数据源的service包 latent-expression 为数据源表达式配置中的id -->
        <decision mapping="com.dawdler.order.service.impl"
            latent-expression-id="order-datasource" />
    </decisions>
```