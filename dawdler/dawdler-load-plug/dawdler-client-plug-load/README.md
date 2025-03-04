# dawdler-client-plug-load

## 模块介绍

远程加载模块,之前在dawdler-client-plug模块中,将dawdler-client-plug模块拆分成dawdler-client-plug-web与dawdler-client-plug-load模块.

此模块最常用的场景用于做动态网关. 

支持以下三种组件做远程加载

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-handlerinterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)


### 1. pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-client-plug-load</artifactId>
```

### 2. 配置需要加载的组件

参考以下示例,loads-on是配置加载项,其中channel-group-id对应上面server-channel-group中声明的server-channel-group.关于示例中其他配置请参考[client-conf.xml配置文件说明](../../dawdler-client/README.md#2-client-confxml配置文件说明).
示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <certificatePath>key/dawdler.cer</certificatePath>
    <server-channel-group channel-group-id="user-load-web"
                          connection-num="1"
                          sessionNum="4" serializer="2"
                          user="global_user" password="global_password">
    </server-channel-group>
  
    <!-- web启动时动态加载配置,dawdler-client-plug-load需要此配置 -->
    <loads-on>
        <item sleep="15000" channel-group-id="user-load-web" mode="run">user</item><!-- 配置加载user-load-web服务中的user模块,channel-group-id指定组,mode=run 为运行模式 不再检查更新,如果不填写mode 默认为debug模式 会触发sleep 检查更新间隔 毫秒单位 -->
    </loads-on>

</config>
``` 


