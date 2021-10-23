# dawdler-config-clientside

## 模块介绍

web端统一配置中心

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-config-clientside</artifactId>
```

### 2. web端使用方式

#### 2.1 web端支持注入的三种组件

在web端只有这三种组件可以使用配置中心

[web端controller](../../dawdler-client-plug/README.md#3-controller注解说明)

[web端拦截器HandlerInterceptor](../../dawdler-client-plug/README.md#5-HandlerInterceptor-拦截器)

[web端监听器WebContextListener](../../dawdler-client-plug/README.md#6-webcontextlistener-监听器)

#### 2.2 FieldConfig注解使用

参考[FieldConfig注解使用](../dawdler-config-core/README.md#4-FieldConfig注解使用)
