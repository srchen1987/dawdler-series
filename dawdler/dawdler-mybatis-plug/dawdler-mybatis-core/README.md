# dawdler-mybatis-core

## 模块介绍

mybatis核心模块.

### 1. mybatis-config.xml配置文件说明(非必须)

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

### 2. 注入Mapper

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
  return orderMapper.insert(order) > 0;
 }

}
```