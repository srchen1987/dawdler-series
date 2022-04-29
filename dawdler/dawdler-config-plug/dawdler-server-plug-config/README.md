# dawdler-server-plug-config

## 模块介绍

服务端统一配置中心

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-server-plug-config</artifactId>
```

### 2. server端使用方式

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@RemoteService注解的接口实现类](../../dawdler-core/README.md#2-RemoteService注解)

#### 2.2 FieldConfig注解

参考[FieldConfig注解使用](../dawdler-config-core/README.md#4-FieldConfig注解)
