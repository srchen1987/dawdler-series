# dawdler-service-core

## 模块介绍

service核心模块.

### 1. pom中引入依赖

```xml
 <groupId>io.github.dawdler-series</groupId>
 <artifactId>dawdler-service-core</artifactId>
```

### 2. @Service说明

@Service  标注一个类为服务的注解,可以注释在实现类或接口上.

```java
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Service {

	String value() default "";// 在调用端有效,指定服务端部署的服务名.

	String serviceName() default "";// 服务的类名,默认为空,则为注解所在类或接口的全称(类优先).

	boolean single() default true;// 在服务端有效,标识一个服务的实现类是否为单例.默认为单例.

	int timeout() default 120;// 在调用端有效,调用远程服务的超时事件,单位为秒,默认120秒.

	boolean fuzzy() default true;// 在调用端有效,是否模糊匹配方法,默认为true,模糊匹配根据方法名与参数个数进行匹配,非模糊匹配会根据方法名与参数类型进行精确匹配.模糊匹配效率高,如果一个服务实现类中存在相同方法相同参数个数时需要设置此参数为true.

	String loadBalance() default "roundRobin";// 调用端有效,负载方式

}
```

示例:
```java
@Service("order-service")
public interface OrderService {

	/**
	 * 
	 * 根据订单号查询订单信息
	 * @author srchen
	 * @version 1.0
	 * @param orderId 订单ID
	 */
	BaseResult<Order> selectByPrimaryKey(Integer orderId);
}
```

