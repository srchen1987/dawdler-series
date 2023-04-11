# dawdler-discovery-center-consul-core

## 模块介绍

consul实现注册中心的核心模块,提供consul操作的单例类和健康检测的实现类.

### 1. 配置文件

支持本地配置文件或统一配置中心

#### 1.1 本地配置文件

consul.properties文件位于项目classpath下.checkTime为健康检测时间.

```properties
host=127.0.0.1
port=8500
#checkTime=3s
```

#### 1.2 统一配置中心

统一配置中心请参考 [统一配置中心模块](../../../dawdler-config-plug/README.md)

consul里面的path为 /consul

```yml
host: 127.0.0.1
port: 8500
#checkTime: 3s
```
