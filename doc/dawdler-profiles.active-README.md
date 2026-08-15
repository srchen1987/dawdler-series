# 多环境配置

## 1. 使用方式

通过jvm参数传入dawdler.profiles.active
例如:

```shell
-Ddawdler.profiles.active=uat
```

以上指定了uat环境的配置文件.

## 2. 加载顺序

如果部署的项目中存在有dawdler.profiles.active的配置会优先加载.

例如:

项目中存在session-redis.properties与session-redis-uat.properties两个配置文件,如果启动参数中有设置 ```-Ddawdler.profiles.active=uat``` 则读取到的是session-redis-uat.properties.

本地配置文件优先于统一配置中心的配置.

## 3. 已支持的组件配置文件列表

| 配置文件 | 所属模块 | 所在位置 | 支持统一配置中心 | 支持加密 | 备注 |
| :-: | :-: | :-: | :-: | :-: | :-: |
| client-conf.xml | dawdler-client | web端\{classPath\}/ | 否 | 否 | web端请求配置 |
| dawdler-config.yml | dawdler-config-center | web端与服务端\{classPath\}/ | 否 | 否 | 统一配置中心配置 |
| services-conf.yml | dawdler-server | 服务端\{classPath\}/ | 否 | 否 | 服务相关配置 |
| zookeeper.properties | dawdler-discovery-center-zookeeper-core | web端与服务端\{classPath\}/ | 是 | 是 | 基于zk实现注册中心配置 |
| consul.properties | dawdler-discovery-center-consul-core | web端与服务端\{classPath\}/ | 是 | 是 | 基于consul实现注册中心配置 |
| identityConfig.properties | dawdler-client-plug-session | web端\{classPath\}/ | 是 | 是 | session相关配置 |
| distributed-transaction | dawdler-distributed-transaction-core | 分布式事务的web端与补偿器\{classPath\}/ | 是 | 是 | 分布式事务配置 |
| distributed-transaction-redis | dawdler-distributed-transaction-core | 分布式事务的web端与补偿器\{classPath\}/ | 是 | 是 | 分布式事务redis配置 |
| distributed-transaction-rabbitmq | dawdler-distributed-transaction-core | 分布式事务的web端与补偿器\{classPath\}/ | 是 | 是 | 分布式事务rabbitmq配置 |

## 4. 其他组件的配置

系统中实现的组件配置全部支持多环境配置、统一配置中心、加密、变量替换功能.

如redis、es、rabbitmq、kafka.

```java
public class UserServiceImpl implements UserService {

@EsInjector("myEs")
EsOperator esOperator;

@RabbitInjector("myRabbit")
RabbitProvider rabbitProvider;

@JedisInjector("myJedis")
JedisOperator jedisOperator;

@KafkaInjector("myKafka")
KafkaProvider kafkaProvider;
}
```

## 5. 变量替换

在所有的xml、properties、yml、统一配置中心中都支持变量替换

用yml来举例:

```yml
name: "${app.name}"
port: 9090
url: "${API_URL:http://localhost:3000}"
description: "This is ${app.name:DefaultApp} running on port 9090"
```

变量替换的优先级 System.getProperty > System.getenv

例子中的`${app.name}` 先从System.getProperty("app.name")中获取 如果获取不到则通过System.getenv("app.name")获取 如果获取不到则为`${app.name}`.

例子中的`${API_URL:http://localhost:3000}` 先从System.getProperty("API_URL")中获取 如果获取不到则通过System.getenv("API_URL")获取 如果获取不到则为`http://localhost:3000`.
