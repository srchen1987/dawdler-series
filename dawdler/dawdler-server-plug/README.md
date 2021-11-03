# dawdler-server-plug

## 模块介绍

dawdler-server-plug 用于提供服务端将服务注册到注册中心,加载远程entity,service接口(如果涉及远程调用服务可的情况下),注入mapper,dao,service到service层.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-server-plug</artifactId>
```

### 2. 配置需要被加载的api与entity

中,dawdler提供远程加载服务将这些类暴露给调用端使用,api一般为接口,entity为数据库实体也有时被做为dto做传输,所以服务端与调用端都依赖这些类.(现在的项目基本都是maven或gradle构建,在过去的开发中开发者需要将api与entity进行打包到具体应用)

在项目中的resources节点下的services-config配置文件中有remote-load节点,用于指定配置文件.

```xml
<!--${classpath}是当前项目的class节点-->
<remote-load package="${classpath}/load-config.xml"></remote-load>
```

load-config.xml文件是用来配置本服务中哪些包是可以被远程加载的,type类型为api代表本包是用于service接口或dto的,如果不填写type或type为component则为controller,listener,interceptor.

load-config.xml示例：

这是一种独立的api服务,只部署entity或dto和service接口.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<hosts>
 <host name="user">
  <package type="api">com.anywide.yyg.user.entity</package>
  <package type="api">com.anywide.yyg.user.service</package>
 </host>
</hosts>
```

load-config.xml是为client-conf.xml中的loads-on节点提供服务的,client-conf.xml是在调用端配置的,此处只是引用做说明.

client-config.xml示例：

```xml
<!-- web启动时动态加载配置,dawdler-client-plug需要此配置 -->
    <loads-on>
        <item sleep="15000" channel-group-id="user-api" mode="run">user</item><!-- 配置加载user模块  sleep 检查更新间隔 毫秒单位,channel-group-id指定组,mode=run 为运行模式 不检查更新-->
    </loads-on>
```

### 3. 配置需要加载的api与entity

当服务端需要调用远程服务时可以配置此项来进行远程加载api与entity,参考[dawdler-client-plug模块的配置需要加载的api与entity](../dawdler-client-plug/README.md#11-配置需要加载的api与entity).
