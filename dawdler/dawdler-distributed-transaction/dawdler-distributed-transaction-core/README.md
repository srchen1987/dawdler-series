# dawdler-distributed-transaction-core

## 模块介绍

分布式事务核心模块,提供事务注解和具体的实现.

### 1. DistributedTransaction注解

@DistributedTransaction应用在事务发起者与事务参与者的方法上.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DistributedTransaction {

 String action();// 具体的Processor的别名 参考DistributedTransactionCustomProcessor构造函数传入的参数即可,用于对应事务处理器.

 boolean sponsor() default false;// 是否为调用者,默认为协调者.
}
```
