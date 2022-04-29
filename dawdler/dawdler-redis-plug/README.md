# dawdler-redis-plug

## 模块介绍

dawdler-redis-plug redis模块的支持,包含客户端,服务器端,redis核心模块.

### 1. 子模块介绍

[dawdler-redis-core redis插件,针对jedis进行封装,支持单机模式与哨兵模式.](./dawdler-redis-core/README.md)

[dawdler-server-plug-redis 实现dawdler-server端注入功能.](./dawdler-server-plug-redis/README.md)

<<<<<<< HEAD
```properties
#######################
masterName=masterName #哨兵模式下的masterName (注意：哨兵与单机只能用一种,用单机就不能配置此项)
sentinels=192.168.0.2:26379,192.168.0.3:26379,192.168.0.4:26379 #哨兵列表(注意：哨兵与单机只能用一种,用单机就不能配置此项)
#######################
addr=127.0.0.1 #单机ip
port=6379 #单机端口
######################
userName=redis_user #redis6之后支持设置用户名,如果不需要注释掉此项
auth=password #密码
max_active=20 #最大连接数
max_idle=8 #最大空闲数
max_wait=10000 #最大等待时长(单位毫秒)
timeout=10000 #超时时间(单位毫秒)
test_on_borrow=false #获取连接时是否验证连接有效
database=0 #使用指定数据槽
```
=======
[dawdler-client-plug-redis 实现dawdler-client端注入功能.](./dawdler-client-plug-redis/README.md)

>>>>>>> 0.0.2-RELEASES


