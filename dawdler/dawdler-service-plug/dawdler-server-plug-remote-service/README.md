# dawdler-server-plug-remote-service

## 模块介绍

服务端远程服务模块.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-server-plug-remote-service</artifactId>
```

### 2. 使用说明

服务端调用远程服务时需要引入此jar包.

参考 [@RemoteService](../dawdler-remote-service-core/README.md#2-remoteservice说明)

### 3. 配置需要扫描的包

services-conf.xml中的扫描器

```xml
<scanner>
 <package-paths>
  <package-path>com.anywide.shop.listener</package-path>
  <package-path>com.anywide.shop.**.service.impl</package-path>
 </package-paths><!-- 需要扫描的路径，支持antpath 如 com.anywide.shop.**.service.impl，被扫描的包中的组件会生效-->
</scanner>
```
