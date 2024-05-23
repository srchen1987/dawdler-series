# dawdler-server-plug-config-center-consul

## 模块介绍

服务端统一配置中心

### 1. pom中引入依赖

```xml
<groupId>io.github.dawdler-series</groupId>
<artifactId>dawdler-server-plug-config-center-consul</artifactId>
```

### 2. server端使用方式

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@Service注解的接口实现类](../../../dawdler-service-plug/dawdler-service-core/README.md#2-service说明)

#### 2.2 FieldConfig注解

参考[FieldConfig注解使用](../../dawdler-config-center-core/README.md#1-FieldConfig注解)

