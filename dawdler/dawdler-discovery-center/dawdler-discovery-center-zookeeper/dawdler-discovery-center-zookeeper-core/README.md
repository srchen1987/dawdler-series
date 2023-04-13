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
```

#### 1.2 统一配置中心

统一配置中心请参考 [统一配置中心模块](../../../dawdler-config-center/README.md)

consul里面的path为 /zookeeper

```yml
connectString:192.168.43.137:2181
```
