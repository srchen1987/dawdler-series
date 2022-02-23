# dawdler-distributed-transaction

## 模块介绍

dawdler-distributed-transaction 分布式事务模块的根模块.

分布式事务架构原理

分布式事务有三种状态: trying(尝试中),confirm(已确认),cancel(取消).

发起者通过以下4个步骤完成分布式事务的处理:

1、通过@DistributedTransaction标识为分布式事务的发起者与参与者.

2、通过aop拦截@DistributedTransaction,发起者生成全局事务id及上下文.

3、发起者调用参与者,通过aop拦截到@DistributedTransaction标识的参与者,生成分支事务id及trying状态信息并序列化到redis中.调用具体的参与者服务,如果执行出现异常则标识事务回滚并抛出异常.

4、执行过程中发起者调用参与者出现异常或自定义的TransactionInterceptInvoker标识事务为cancel状态,将redis中存储的事务标识为cancel状态并进行事务回滚(发送mq消息).如果参与者全部执行完成(没有被标识为回滚状态)则将redis中存储设为confirm状态并进行事务提交(发送mq消息).

补偿器模块

1、消费到发起者发送过来的消息后执行对应的业务方法(注意:需要实现幂等),执行成功后会删除redis存储的对应的分支事务信息.

2、定时补偿器会定期执行指定时间范围的事务,防止mq消费后执行失败的事务不再执行(一般为服务宕机或网络不可用).

### 1. web端的pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-distributed-transaction-client</artifactId>
```

### 2. dawdler服务端的pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-distributed-transaction-server</artifactId>
```

### 3. web端补偿器模块的pom中引入依赖

```xml
<groupId>dawdler</groupId>
<artifactId>dawdler-distributed-transaction-compensator</artifactId>
```

### 4. 使用方式

#### 4.1 配置分布式事务

将需要加入分布式事务管理的服务进行配置,分布式事务分为发起者与参与者.

根据业务定义服务,将web接口设置为事务发起者,服务的接口设为参与者.

举例 下订单的一个业务中,有以下3个服务(具体可以参考示例)：

##### 4.1.1 订单服务

api层定义接口

```java
//订单服务的接口定义
@RemoteService("distributed-transaction-order-service")
public interface OrderService {

 @DistributedTransaction(action = "order",sponsor = false)//标识为分布式事务的参与者
 public boolean createOrder(Integer userId, Integer productId, BigDecimal amount);

 public boolean updateStatusOrder(DistributedTransactionContext context, String status);
}

.
```

##### 4.1.2 用户服务

api层定义接口

```java
//用户服务的接口定义
@RemoteService("distributed-transaction-user-service")
public interface UserService {
 @DistributedTransaction(action = "user",sponsor = false)//标识为分布式事务的参与者
 public Map<String, Object> tryPayment(Integer userId,BigDecimal amount);
 
 
 public boolean doPayment(DistributedTransactionContext context, String status);
}


```

##### 4.1.3 商品库存服务

api层定义接口

```java
//商品库存服务的接口定义
@RemoteService("distributed-transaction-product-service")
public interface ProductService {

 @DistributedTransaction(action = "product",sponsor = false)//标识为分布式事务的参与者
 public Map<String, Object> tryDeductStock(Integer productId, Integer stock);

 public boolean doDeductStock(DistributedTransactionContext context,String status);
}
```

##### 4.1.4 下单的web接口

web端将发起者声明在web接口中(分布式事务框架也支持将发起者放在服务端做,服务端再调用多个服务,一般不建议这么做).

```java

@RequestMapping(value = "/order")
public class OrderController extends TransactionController {
 @Service
 UserService userService;//注入用户服务

 @Service
 OrderService orderService;//注入订单服务

 @Service
 ProductService productService;//注入商品库存服务
 
 
 @DistributedTransaction(action = "createOrder",sponsor = true)//标识为分布式事务的发起者
 @RequestMapping(value = "/createOrder.do", viewType = ViewType.json)
 public void createOrder(@RequestParam Integer productId, @RequestParam Integer stock,
   @RequestParam BigDecimal amount) throws Exception {
  int userId = 1;//定义一个用户id
  Map<String, Object> result;
//  RpcContext.getContext().setAttachment("testdata", "test");
  boolean success = orderService.createOrder(userId, productId, amount);//调用创建订单服务
  if(!success) {
   result = new HashMap<>();
   result.put("success", false);
   result.put("msg", "订单创建失败!");
   setData(result);
   return;
  }
  result = userService.tryPayment(userId, amount);//扣减用户金额
  success = (boolean) result.get("success");
  if(!success) {
   setData(result);
   return;
  }

  result = productService.tryDeductStock(productId, stock);//扣减库存
  setData(result);
 }
```

#### 4.2 配置redis

redis用于存储事务状态,防止服务意外崩溃或停机造成事务状态不可溯的情况.

如果发起者在web端,则需要在web端的classpath下定义distributed-transaction-redis.properties.

如果在服务端(分布式事务框架也支持将发起者放在服务端做,服务端再调用多个服务,一般不建议这么做)则需要在dawdler服务端的classpath下定义distributed-transaction-redis.properties.

redis的配置参考[dawdler-redis-plug](../dawdler-redis-plug/README.md#2-properties文件说明)

#### 4.3 配置rabbitmq

mq用于实时消息通知事务参与者去执行对应的服务.

rabbitmq的配置参考[dawdler-rabbitmq-plug](../dawdler-rabbitmq-plug/README.md#2-properties文件说明)

#### 4.4 配置事务补偿器

创建一个项目在maven中依赖补偿器模块,补偿器在web端启动,补偿器中包含消费rabbitmq消息调用对应服务的功能,同时也支持定时读取redis中未处理完的事务进行补偿处理.

配置redis与rabbitmq在事务管理器的classpath下.具体配置参考上面的redis配置与rabbitmq的配置(注意:补偿器中的redis配置和rabbitmq的配置要与发起端的是一致的).

##### 4.4.1 配置Processor

Processor是分布式事务参与者的处理器.

定义参与者的Processor需要继承DistributedTransactionCustomProcessor类.

用户服务的处理器:

```java
public class UserCompensator extends DistributedTransactionCustomProcessor {
 @Service //注入用户服务
 UserService userService;
 public UserCompensator() {
  super("user");//定义Processor的别名,DistributedTransaction注解中的action与其对应
 }

 @Override
 public boolean process(DistributedTransactionContext context, String status) {
  return userService.doPayment(context, status);//调用对应的服务
 }
}
```

商品库存服务的处理器:

```java
public class ProductCompensator extends DistributedTransactionCustomProcessor {
 @Service
 ProductService productService;
 public ProductCompensator() {
  super("product");
 }

 @Override
 public boolean process(DistributedTransactionContext context, String status) {
  return productService.doDeductStock(context, status);
 }
}
```

订单服务的处理器:

```java
public class OrderCompensator extends DistributedTransactionCustomProcessor {
 @Service
 OrderService orderService;
 public OrderCompensator() {
  super("order");
 }

 @Override
 public boolean process(DistributedTransactionContext context, String status) {
  return orderService.updateStatusOrder(context, status);
 }
}
```

将以上的处理器通过SPI方式进行注入,META-INF/services/com.pttl.distributed.transaction.compensate.process.DistributedTransactionCustomProcessor文件中内容如下:

```spi
com.anywide.shop.compensator.OrderCompensator
com.anywide.shop.compensator.ProductCompensator
com.anywide.shop.compensator.UserCompensator
```

#### 4.5 分布式事务执行失败留存时间与延迟处理时间配置

留存时间配置项:

在发起者模块中的classpath下可以通过配置distributed-transaction.properties来设定留存时间.

```properties
expireTime=259200 #存留在redis中的时间为259200秒,如果不配置默认为259200秒=3天.
```

在补偿器模块中的classpath下可以通过配置distributed-transaction.properties来设定延迟处理时间.

如果有参与者的服务不可用,补偿器定时器每15秒执行一次,此参数设置为只查询60秒以外的事务进行补偿,因为不可用的服务还尚未启动,没有必要进行补偿.

```properties
compensateLater=60 #存留在redis中的时间为60秒,如果不配置默认为60秒.
```

#### 4.6 自定义响应结果回滚设置

分布式事务框架会在调用参与者的服务出现异常时回滚整个事务,如果有特殊需求需要使用TransactionInterceptInvokerHolder来扩展,例如下订单需要以下步骤:

创建订单->扣减金额->扣减库存

如果扣减金额或扣减库存服务出现异常则回滚整个事务,如果库存不足(也可以通过抛出异常,触发回滚),需要回退用户的金额,同时将信息提醒给用户.

通过TransactionInterceptInvokerHolder设置TransactionInterceptInvoker来拦截分布式事务的执行结果并针对业务自定义响应结果做回滚设置.

自定义的TransactionInterceptInvoker代码如下:

```java
 @Override
 public void contextInitialized(ServletContext arg0) {
  TransactionInterceptInvokerHolder.setTransactionInterceptInvoker(new TransactionInterceptInvoker() {
   @Override
   public Object invoke(ProceedingJoinPoint invocation, DistributedTransactionContext tc) throws Throwable {
    Object result = invocation.proceed();//获取调用服务的返回结果
    if(result instanceof Map) {
     Map resultMap = (Map) result;
     Boolean success = (Boolean)resultMap.get("success");
     if(success==null || !success) {//如果失败则设置事务回滚
      tc.setCancel(true); 
     }
    }
    return result;
   }
  });
 }

```

调用的商品库存服务的具体实现:

```java
 @Override
 @DBTransaction
 public Map<String, Object> tryDeductStock(Integer productId, Integer stock) {
  Map<String, Object> resultMap = new HashMap<>();
  Product product = new Product();
  product.setProductId(productId);
  product.setStock(stock);
  int result = productMapper.deductStockByPrimaryKey(product);
  if(result == 0) {
   resultMap.put("success", false);//此处设置为调用失败
   resultMap.put("msg", "库存不足!");//设置具体原因
   return resultMap;
  }
  //...省略以下入库明细记录的代码 如有需要参考具体分布式事务的例子
}
```
