# dawdler-serialization

## 模块介绍

序列化模块

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-serialization</artifactId>
```

### 2. 通过SPI方式扩展

实现com.anywide.dawdler.core.serializer.Serializer接口
注意：key方法返回一个byte值,不可以重复,目前系统提供jdk,kryo两种方式,jdk的标识为1,kryo的标识为2.

标识用于client-conf.xml中的server-channel-group节点下的serializer属性.

在resources/META-INF/services下创建文件
com.anywide.dawdler.core.serializer.Serializer

参考dawdler-serialization源码中的resources/META-INF/services/com.anywide.dawdler.core.serializer.Serializer

另外需要将新增类打入dawdler-serialization.jar中或将新扩展的jar放置到dawdler-server/bin下,client端也需要放置.

### 3. 通过listener方式扩展

另一种扩增方法SerializeDecider中有register方法,可以通过监听器在启动时进行扩展.这种方式客户端和服务器端都需要进行设置,不建议使用这种方式.
