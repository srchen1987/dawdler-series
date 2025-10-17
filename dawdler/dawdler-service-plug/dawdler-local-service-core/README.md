# dawdler-local-service-core

## 模块介绍

LocalService核心模块,用于标注一个被注入的服务是本地服务.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-local-service-core</artifactId>
```

### 2. @LocalService说明

@LocalService 用于标注一个被注入的服务是本地服务.

```java
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface LocalService {
    //服务的类名,默认为空,则为注解所在类或接口的全称(类优先). 与@Service中serviceName对应
    String serviceName() default "";
}
```

### 3. 使用示例

#### 3.1 创建Controller

```java
@Controller
@RequestMapping("/order")
public class OrderController {

    @LocalService
    private OrderService orderService;

    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public PageResult<List<Order>> list(Integer pageOn, Order order) {
        int row = 10;
        return orderService.selectPageList(order, pageOn, row);
    }

}
```

#### 3.2 创建服务接口

```java
@Service("order-service")
public interface OrderService {
 
    BaseResult<Order> selectByPrimaryKey(Integer orderId);

}
```

#### 3.3 编写服务实现层(以mybatis举例)

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
