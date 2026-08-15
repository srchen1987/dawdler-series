# dawdler-discovery-center-zookeeper-core

## 模块介绍

zookeeper实现注册中心的核心模块,提供zookeeper操作的单例类和健康检测的实现类.

### 1. 配置文件

支持本地配置文件或统一配置中心

#### 1.1 本地配置文件

zookeeper.properties文件位于项目classpath下. 如果没有账号密码则无需配置user与password.

```properties
connectString=192.168.43.137:2181
#user=zkUser
#password=zkPassword
#baseSleepTimeMs=1000
#maxRetries=3
```

#### 1.2 统一配置中心

统一配置中心请参考 [统一配置中心模块](../../../dawdler-config-center/README.md)

consul里面的path为 /zookeeper

```yml
connectString:192.168.43.137:2181
```

#### 1.3 配置参数说明

connectString zookeeper的连接地址,多个地址用逗号隔开

user zookeeper的认证用户名,如果没有配置则无需设置

password zookeeper的认证密码,如果没有配置则无需设置

baseSleepTimeMs 初始重试等待时间(毫秒),默认1000

maxRetries 最大重试次数,默认3

### 2. 本地缓存兜底

`ZkDiscoveryCenter` 维护了一份本地服务列表缓存,`getServiceList` 在 ZK 读取成功时刷新缓存,在 ZK 不可用时自动降级返回上次缓存的服务列表,避免 ZK 抖动导致服务发现直接失败.缓存由 `CuratorCache` 监听器在节点增删时实时同步.
