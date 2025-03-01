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

### 3. 水平分表配置

dawdler支持水平分表的配置,由SubTable注解与@SubParam注解来配合进行配置.

注意: 需要创建分表规则对应的表,否则会导找不到对应的表. 如 t_user_0,t_user_1,t_user_2.

```java
/**
 * @author jackson.song
 * @version V1.0
 * 分表注解 用于标识mybatis接口方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubTable {

	String expression();

	String[] tables();

	String configPath();

	Class<? extends SubRule> subRuleType();

}
```

```java
/**
 * @author jackson.song
 * @version V1.0
 * 分表参数注解 用于标识mybatis接口中的方法参数
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubParam {

}
```

参数说明:

expression: 用于指定分表表达式,例如:分表字段id,则该参数名称为id,同时支持对象参数,例如:分表字段为对象user,则该表达式为user.id.

tables: 用于指定分表的表名,支持多个表名.

configPath: 用于指定分表规则配置文件的路径,该配置文件为yml格式(支持统一配置中心).

subRuleType: 用于指定分表规则的实现类,该类必须实现SubRule接口.


dawdler提供的分库分表规则实现类有: ConsistentHashSubRule,RemainderSubRule.有其他需求可以自行扩展.

ConsistentHashSubRule: 一致性hash算法实现的分库规则,该规则需要配置副本数量与节点列表.

示例: 

```yml
numberOfReplicas: 3
nodes: 
  - 0
  - 1
  - 2
```

RemainderSubRule: 取模算法实现的分库规则,该规则需要配置除数.

示例:

```yml
divisor: 3
```

以下演示通过用户id进行余3取模分表的例子.

userById.yml

```yml
divisor: 3
```

```java
  /** 
   * 查询[用户表] 用户id方式水平分表
   * @version 1.0
   * @param id	用户id
   */
  @SubTable(configPath = "userById", tables = {"t_user"}, subRuleType = RemainderSubRule.class, expression = "id")
  User selectByPrimaryKey(@SubParam @Param("id")Integer id);

  /** 
   * 查询[用户表] 表达式读取user对象获取id水平分表
   * @version 1.0
   * @param user user对象
   */
  @SubTable(configPath = "userById", tables = {"t_user"}, subRuleType = RemainderSubRule.class, expression = "user.id")
  User selectByPrimaryKey(@SubParam @Param("user") User user);
```

### 4. 打印SQL日志

在mybatis-config.xml中添加如下配置即可打印SQL日志：

```xml
<settings>
 <setting name="logImpl" value="STDOUT_LOGGING"/>
</settings>
```

在logback.xml中添加如下配置：

```xml
<logger name="org.apache.ibatis.logging.jdbc">
 <level value="DEBUG"/>
</logger>
```
