# dawdler-circuit-breaker

## 模块介绍

dawdler-circuit-breaker 是基于时间滑动窗口方式实现的熔断器，支持熔断配置，降级。

### 1. web端的pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-circuit-breaker</artifactId>
```

### 2.  在service接口层的方法上定义熔断器

使用@CircuitBreaker标注的方法会被开启熔断器

例:

```java
package com.anywide.yyg.user.service;

import java.util.Map;

import com.anywide.dawdler.core.annotation.CircuitBreaker;
import com.anywide.dawdler.core.annotation.RemoteService;
import com.anywide.yyg.user.entity.User;

@RemoteService
public interface UserService {

 int addUser(User user)throws Exception;
 
 @CircuitBreaker
 Map<String, Object> selectUserList(int pageon,int row)throws Exception;
 
}
```

### 3.  服务降级

@CircuitBreaker 中有fallbackMethod的属性，fallbackMethod是接口中方法的实现。

例：

```java
@CircuitBreaker(fallbackMethod = "selectUserListFallback")
Map<String, Object> selectUserList(int pageon,int row)throws Exception;
 
default Map<String, Object> selectUserListFallback(int pageon,int row)throws Exception{
  Map<String, Object> result = new HashMap<>();
  return result;
}
```

### 4. CircuitBreaker属性说明

```java
public @interface CircuitBreaker {

 /**
  * @return String
  * @Description 标识key，默认为"" 则为servicePath+serviceName+serviceMethod组合
  * @date 2018年3月10日
  */
 String breakerKey() default "";

 /**
  * @return int
  * @Description 统计时长 intervalInMs/windowsCount 建议为整数，默认3000，单位为毫秒。
  * @date 2018年3月10日
  */
 int intervalInMs() default 3000;

 /**
  * @return int
  * @Description 窗口大小
  * @date 2018年3月10日
  */
 int windowsCount() default 2;

 /**
  * @return int
  * @Title sleepWindowInMilliseconds
  * @Description 熔断器打开后，所有的请求都会直接失败，熔断器打开时会在经过一段时间后就放行一条请求成功则关闭熔断器，此配置就为指定的这段时间，默认值是
  *              5000，单位为毫秒。
  * @date 2018年3月10日
  */
 int sleepWindowInMilliseconds() default 5000;

 /**
  * @return int
  * @Description 启用熔断器功能窗口时间内的最小请求数，默认为5。
  * @date 2018年3月10日
  */

 int requestVolumeThreshold() default 5;

 /**
  * @return double
  * @Description 错误百分比，默认为40% 达到40%的错误率会触发熔断（大于requestVolumeThreshold）
  * @date 2018年3月10日
  */
 double errorThresholdPercentage() default 0.4;

 /**
  * @return String
  * @Description 熔断后执行的方法 参数与返回值与执行的方法相同
  * @date 2018年3月10日
  */
 String fallbackMethod() default "";

}
```
