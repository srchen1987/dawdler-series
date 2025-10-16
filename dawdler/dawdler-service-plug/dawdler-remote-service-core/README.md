# dawdler-remote-service-core

## 模块介绍

RemoteService核心模块,用于标注一个被注入的服务是远程服务.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-remote-service-core</artifactId>
```

### 2. @RemoteService说明

@RemoteService 用于标注一个被注入的服务是远程服务.

```java
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface RemoteService {
    //服务的类名,默认为空,则为注解所在类或接口的全称(类优先). 与@Service中serviceName对应
    String serviceName() default "";
}
```

### 3. @RemoteServiceAssistant说明

@RemoteServiceAssistant  应用在服务接口的方法上. 为服务的方法做特定的异步,超时时间,模糊匹配,负载均衡方式的设置.

一般不会使用,有特殊需要时配置即可.

```java
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RemoteServiceAssistant {
    boolean async() default false;// 在客户端有效,是否为异步执行.

    int timeout() default 120;// 在调用端有效,调用远程服务的超时事件,单位为秒,默认120秒.

    boolean fuzzy() default true;// 在调用端有效,是否模糊匹配方法,默认为true,模糊匹配根据方法名与参数个数进行匹配,非模糊匹配会根据方法名与参数类型进行精确匹配.模糊匹配效率高,如果一个服务实现类中存在相同方法相同参数个数时需要设置此参数为true.

    String loadBalance() default "roundRobin";// 调用端有效,负载方式
}
```

### 4. 使用示例

#### 4.1 创建Controller

```java
@Controller
@RequestMapping("/order")
public class OrderController {

    @RemoteService
    private OrderService orderService;

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public PageResult<List<Order>> list(Integer pageOn, Order order) {
        int row = 10;
        return orderService.selectPageList(order, pageOn, row);
    }

}
```

#### 4.2 创建服务接口

```java
@Service("order-service")
public interface OrderService {
    @RemoteServiceAssistant(timeout = 30) //设置超时时间为30秒
    BaseResult<Order> selectByPrimaryKey(Integer orderId);

}
```

#### 4.3 编写服务实现层(以mybatis举例)

```java
public class OrderServiceImpl implements OrderService {
    @Repository
    private OrderMapper orderMapper;

    @Override
    @DBTransaction(mode = MODE.readOnly)
    public BaseResult<Order> selectByPrimaryKey(Integer orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        BaseResult<Order> baseResult = new BaseResult<>(order);
        return baseResult;
    }
}
```
