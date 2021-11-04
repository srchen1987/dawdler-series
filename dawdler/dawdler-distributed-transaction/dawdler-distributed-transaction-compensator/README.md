# dawdler-distributed-transaction-compensator

## 模块介绍

dawdler-distributed-transaction-compensator 分布式事务web端补偿器模块,提供实时消费执行分布式事务参与者的子事务,定时执行补偿机制,释放redis与rabbitmq的资源.

### 1. web端的pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-distributed-transaction-compensator</artifactId>
```
