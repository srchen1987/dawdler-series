# dawdler-config-serverside

## 模块介绍

服务端统一配置中心

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-config-serverside</artifactId>
```

### 2. server端使用方式

#### 2.1 server端支持注入的三种组件

在server端只有这两种组件可以使用配置中心

[dawdler服务器启动销毁监听器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

[dawdler服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

#### 2.2 FieldConfig注解使用

参考[FieldConfig注解使用](../dawdler-config-core/README.md#4-FieldConfig注解使用)
