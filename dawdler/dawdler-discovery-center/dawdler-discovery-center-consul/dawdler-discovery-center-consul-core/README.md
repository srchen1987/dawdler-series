# dawdler-discovery-center-consul-core

## 模块介绍

consul实现注册中心的核心模块,提供consul操作的单例类和健康检测的实现类.

### 1. 配置文件

支持本地配置文件或统一配置中心

#### 1.1 本地配置文件

consul.properties文件位于项目classpath下.

```properties
host=127.0.0.1
port=8500
#healthCheckType=tcp
#checkTime=90s
#keyStoreInstanceType=JKS
#certificatePath=/home/srchen/certificatePath/xxx
#certificatePassword=password
#keyStorePath=/home/srchen/keyStorePath/xxx
#keyStorePassword=password
```

#### 1.2 统一配置中心

统一配置中心请参考 [统一配置中心模块](../../../dawdler-config-center/README.md)

consul里面的path为 /consul

```yml
host: 127.0.0.1
port: 8500
#healthCheckType=tcp
#checkTime: 90s
#keyStoreInstanceType: JKS
#certificatePath: /home/srchen/certificatePath/xxx
#certificatePassword: password
#keyStorePath: /home/srchen/keyStorePath/xxx
#keyStorePassword: password
```

#### 1.3 配置参数说明

host consul的ip地址

port consul的端口号

healthCheckType 服务端的健康检测方式,默认为tcp. 另外支持http,使用http需要配置 [服务端健康检测](../../../dawdler-server/README.md#7-健康检测)

checkTime 健康检测时间(默认为90秒).

keyStoreInstanceType 证书类型 目前支持JKS, JCEKS, PKCS12, PKCS11, DKS

certificatePath 证书所在路径

certificatePassword 证书密码

keyStorePath 私钥所在路径

keyStorePassword 私钥密码
