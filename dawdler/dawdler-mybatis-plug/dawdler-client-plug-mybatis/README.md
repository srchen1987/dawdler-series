# dawdler-client-plug-mybatis

## 模块介绍

实现web端的服务层注入mybaits中Mapper的功能.

### 1. pom中引入依赖

```xml
<groupId>io.github.dawdler-series</groupId>
<artifactId>dawdler-client-plug-mybatis</artifactId>
```

### 2. web-conf.xml中mybatis的配置文件说明

web-conf.xml是web端核心配置文件. 包含组件扫描,配置据源定义,指定目标包定义数据源,读写分离配置.

本模块中涉及mybatis的配置在mybatis的子节点mapper的值中，支持antPath语法进行配置.

示例：

```xml
<mybatis>
 <mappers>
  <mapper>classpath*:com/anywide/shop/*/mapper/xml/*.xml</mapper>
 </mappers>
</mybatis>
```

### 3. 注入Mapper

参考 [注入Mapper](../dawdler-mybatis-core/README.md#2-注入mapper).