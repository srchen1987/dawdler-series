# dawdler-server-plug

## 模块介绍

dawdler-server-plug 用于提供服务端将服务注册到注册中心,加载远程entity,service接口(如果涉及远程调用服务可的情况下),注入mapper,dao,service到service层.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-server-plug</artifactId>
```

### 2. 配置需要被加载的组件

注意: 只有提供远程加载服务时才需要配置

dawdler提供远程加载组件的服务,在项目中的resources节点下的services-config配置文件中有remote-load节点,用于指定配置文件.

```xml
<!--${classpath}是当前项目的class节点-->
<remote-load package="${classpath}/load-config.xml"></remote-load>
```

load-config.xml文件是用来配置本服务中哪些包是可以被远程加载的,支持的组件有controller,listener,interceptor.

load-config.xml示例：

这是一种独立的api服务,只部署entity或dto和service接口.

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

client-config.xml示例：

```xml
<!-- web启动时动态加载配置,dawdler-client-plug需要此配置 -->
    <loads-on>
        <item sleep="15000" channel-group-id="user-api" mode="run">user</item><!-- 配置加载user模块  sleep 检查更新间隔 毫秒单位,channel-group-id指定组,mode=run 为运行模式 不检查更新-->
    </loads-on>
```

### 3. 配置需要扫描的包

services-config.xml中的扫描器

```xml
<scanner>
<!-- >
 <loads>
  <pre-load>com.anywide.shop.execute.AbstractOrderExecutor</pre-load>
 </loads>
  -->
 <package-paths>
  <package-path>com.anywide.shop.listener</package-path>
  <package-path>com.anywide.shop.**.service.impl</package-path>
 </package-paths><!-- 需要扫描的路径，支持antpath 如 com.anywide.shop.**.service.impl，被扫描的包中的组件会生效-->
</scanner>
```

pre-load为了解决先加载了子类而通过classpath加载父类未被aop织入的情况,没有特殊需求无需配置.

### 4. client端配置需要加载的组件

注意: 不建议使用服务调用其他服务的方式

当服务端需要调用远程服务时可以配置此项来进行远程加载组件,参考[dawdler-client-plug-load置需要加载的api与entity](../dawdler-client-plug-load/README.md#2配置需要加载的api与entity).
