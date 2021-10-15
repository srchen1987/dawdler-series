# dawdler-server-plug

## 模块介绍

dawdler-server-plug 用于提供服务端将服务注册到注册中心,加载远程entity,service接口（如果涉及远程调用服务可的情况下）,注入mapper,dao,service到service层.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-server-plug</artifactId>
```

### 2. 配置需要被加载的api与entity

现在的项目基本都是maven或gradle构建,在过去的开发中开发者需要将api与entity进行打包到具体应用中,api一般为接口,entity为数据库实体也有时被做为dto做传输,所以服务端与调用端都依赖这些类,dawdler提供远程加载服务将这些类暴露给调用端使用.

在项目中的resources节点下的services-config配置文件中有remote-load节点,用于指定配置文件.

```xml
<!--${classpath}是当前项目的class节点-->
<remote-load package="${classpath}/com/anywide/load/load-config.xml"></remote-load>
```

load-config.xml

```xml

```
