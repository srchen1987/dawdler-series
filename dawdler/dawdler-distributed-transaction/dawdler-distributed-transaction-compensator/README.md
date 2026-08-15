# dawdler-distributed-transaction-compensator

## 模块介绍

分布式事务web端补偿器模块,提供实时消费执行分布式事务参与者的子事务,定时执行补偿机制,释放redis与rabbitmq的资源.

### 1. web端的pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-distributed-transaction-compensator</artifactId>
```

### 2. 配置文件

补偿器通过 classpath 下的 `distributed-transaction-compensator.properties` 加载配置,支持统一配置中心覆盖.未配置时使用默认值.

```properties
# 定时补偿器首次执行延迟(秒),默认15
initialDelaySeconds=15
# 定时补偿器执行间隔(秒),默认15
delaySeconds=15
# 补偿最大重试次数,超过则放弃并告警,默认50
maxRetryTimes=50
```

| 配置项 | 说明 | 默认值 |
| :-: | :-: | :-: |
| initialDelaySeconds | 补偿定时器首次执行延迟(秒) | 15 |
| delaySeconds | 补偿定时器执行间隔(秒) | 15 |
| maxRetryTimes | 单个分支事务最大补偿重试次数,超过则告警并放弃 | 50 |

### 3. 补偿行为说明

- 补偿定时器按 `delaySeconds` 间隔扫描 Redis 中超过 `compensateLater`(见 `distributed-transaction.properties`,默认60秒)未完成的事务,重发 MQ 消息触发 confirm/cancel.
- **TRYING 超时强制回滚**:状态卡在 TRYING(如 sponsor 决策时 Redis 更新失败)的分支,超时后按 CANCEL 补偿,符合 TCC 规范防止资源悬挂.
- **重试上限**:单个分支补偿次数达到 `maxRetryTimes` 后不再重试并输出告警日志,避免永久重试.
- 补偿执行依赖 `distributed-transaction-redis.properties` 与 `distributed-transaction-rabbitmq.properties`,配置参考 [dawdler-redis-plug](../../dawdler-redis-plug/dawdler-redis-plug-jedis/dawdler-jedis-core/README.md) 与 [dawdler-rabbitmq-plug](../../dawdler-rabbitmq-plug/dawdler-rabbitmq-core/README.md).
