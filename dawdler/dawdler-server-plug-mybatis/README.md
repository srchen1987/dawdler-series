# dawdler-server-plug-mybatis

## 模块介绍

通过mybatis3.5.6进行改造,session变更为单例模式,支持读写分离,去除cache功能.

### 1. pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-server-plug-mybatis</artifactId>
```

### 2. mybatis-config.xml配置文件说明

mybatis-config.xml是mybatis官方支持的配置文件，其他配置参考官方说明，以下为示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
  PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
 <typeAliases>
  <!-- 包扫描 以此法被扫描的实体类，别名为类名的首字母小写形式(类似于Bean -> bean) -->
  <!-- <package name="com.anywide.shop.order.entity"/> -->

  <!-- 逐个声明别名 -->
  <typeAlias alias="Order" type="com.anywide.shop.order.entity.Order" />
 </typeAliases>
 <mappers>
  <!-- 添加mapper -->
  <mapper resource="com/anywide/shop/order/mapper/xml/Order.xml" />
 </mappers>
</configuration>
```

### 3. services-config.xml中mybatis的配置文件说明

services-config.xml是服务端核心配置文件，包含了数据源定义，指定目标包定义数据源，读写分离配置，服务端配置.

本模块中涉及mybatis的配置在mybatis的子节点mapper的值中，支持antPath语法进行配置.

示例：

```xml
<mybatis>
 <mappers>
  <mapper>classpath*:com/anywide/shop/*/mapper/xml/*.xml</mapper>
 </mappers>
 </mybatis>
```

### 4. 注入mapper

在service层通过@Repository注入mapper，即可使用mapper.

示例：

```java
public class OrderServiceImpl implements OrderService{

 @Repository
 OrderMapper orderMapper;
 
 @Override
 @DBTransaction
 public boolean createOrder(Integer userId,Integer productId, BigDecimal amount) {
  Order order = new Order();
  order.setAddtime((int)(System.currentTimeMillis()/1000));
  order.setAmount(amount);
  order.setProductId(productId);
  order.setUserId(userId);
  orderMapper.insert(order);
  return true;
 }
```
