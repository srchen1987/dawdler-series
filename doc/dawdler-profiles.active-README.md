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

本地配置文件优先与统一配置中心的配置.

## 3. 已支持的组件配置文件列表

| 配置文件 | 所属模块 | 所在位置 | 支持统一配置中心 | 支持加密 | 备注 |
| :-: | :-: | :-: | :-: | :-: | :-: |
| client-conf.xml | dawdler-client | web端{classPath}/client/ | 否 | 否 | web端请求配置 |
| dawdler-config.yml | dawdler-config-center | web端与服务端{classPath}/ | 否 |  否 | 统一配置中心配置 |
| services-conf.yml | dawdler-server | 服务端{classPath}/ | 否 |  否 | 服务相关配置 |
| zookeeper.properties | dawdler-discovery-center-zookeeper-core | web端与服务端{classPath}/ | 是 |  是 | 基于zk实现注册中心配置 |
| consul.properties | dawdler-discovery-center-consul-core | web端与服务端{classPath}/ | 是 |  是 | 基于consul实现注册中心配置 |
| identityConfig.properties | dawdler-client-plug-session | web端{classPath}/ | 是 |  是 | session相关配置 |
| distributed-transaction | dawdler-distributed-transaction-core | 分布式事务的web端与补偿器{classPath}/ | 是 |  是 | 分布式事务配置 |
| distributed-transaction-redis | dawdler-distributed-transaction-core | 分布式事务的web端与补偿器{classPath}/ | 是 |  是 | 分布式事务redis配置 |
| distributed-transaction-rabbitmq | dawdler-distributed-transaction-core | 分布式事务的web端与补偿器{classPath}/ | 是 |  是 | 分布式事务rabbtimq配置 |

## 4. 其他组件的配置

系统中实现的组件配置全部支持多环境配置、统一配置中心、加密功能.

如redis、es、rabbitmq.

```java
public class UserServiceImpl implements UserService {

@EsInjector("myEs")
EsOperator esOperator;

@RabbitInjector("myRabbit")
RabbitProvider rabbitProvider;

@JedisInjector("myJedis")
JedisOperator jedisOperator;

}
```
