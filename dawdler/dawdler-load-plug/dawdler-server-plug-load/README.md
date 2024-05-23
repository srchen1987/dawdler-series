# dawdler-server-plug-load

## 模块介绍

 dawdler-server-plug-load 服务端用于提供加载远程web组件.

 支持以下三种组件做远程加载

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-HandlerInterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)


### 1. pom中引入依赖

```xml
 <groupId>io.github.dawdler-series</groupId>
 <artifactId>dawdler-server-plug-load</artifactId>
```

### 2. 配置需要被加载的组件

注意: 只有提供远程加载服务时才需要配置

dawdler提供远程加载组件的服务,在项目中的resources下的services-conf.xml中有remote-load节点,用于指定配置文件.

```xml
<!--${classpath}是当前项目的class节点-->
<remote-load package="${classpath}/load-config.xml"></remote-load>
```

load-config.xml文件是用来配置本服务中哪些包是可以被远程加载的,支持的组件有controller,listener,interceptor.

load-config.xml示例：


```xml
<?xml version="1.0" encoding="UTF-8"?>
<hosts>
 <host name="user">
  <package>com.anywide.yyg.user.controller</package>
  <package>com.anywide.yyg.user.interceptor</package>
 </host>
</hosts>
```

load-config.xml是为client-conf.xml中的loads-on节点提供服务的,client-conf.xml是在调用端配置的,此处只是引用做说明.

client-conf.xml示例：

```xml
<!-- web启动时动态加载配置,dawdler-client-plug需要此配置 -->
    <loads-on>
        <item sleep="15000" channel-group-id="user-api" mode="run">user</item><!-- 配置加载user模块  sleep 检查更新间隔 毫秒单位,channel-group-id指定组,mode=run 为运行模式 不检查更新-->
    </loads-on>
```

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

