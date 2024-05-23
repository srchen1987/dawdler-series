# dawdler-schedule-core

## 模块介绍

schedule模块的支持,包含客户端,服务器端,schedule核心模块.

### 1. pom中引入依赖

```xml
 <groupId>io.github.dawdler-series</groupId>
 <artifactId>dawdler-schedule-core</artifactId>
```

### 2. Schedule注解

用于标识一个方法被定时器执行

```java
/**
 * @author jackson.song
 * @version V1.0
 * 定时任务注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Schedule {

 /**
  * 指定配置的schedule,未指定则采用默认配置(注意quartz默认为多线程),如果指定则会在classpath下寻找对应的properties文件,如 设为single则会采用single.properties文件中的配置.
  */
 String fileName() default "";
 
 /**
  * quartz的cron表达式
  */
 String cron();

 /**
  * 是否并发执行
  */
 boolean concurrent() default true;

}

```

### 3. quartz配置

请参考[quartz官方配置](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/configuration/)

建议：

如果使用单线程可以采用以下配置

```properties
org.quartz.threadPool.threadCount=1 #hreadCount 设置线程数为1 单线程
org.quartz.jobStore.misfireThreshold=10
```

关于misfireThreshold的说明

misfireThreshold只有当job任务被阻塞时才有效,如果线程池里线程很多,该参数没有意义.所以大部分时候只对有状态的job才有意义.

org.quartz.jobStore.misfireThreshold = 60000 #默认值60秒,如果不希望因为当前任务执行过长导致堆积后执行过期的任务,可以将此值设小.

如第一个任务执行为2022/08/17 08:10:30 任务每5秒执行一次,如果设为单线程并且为同一个任务管理器,则下一个任务在2022/08/17 08:10:35,如果第一个任务执行了10秒才结束,当下次执行时会连续执行两次该任务.

断定为misfire的指标, 两次任务执行之间的差大于misfireThreshold,则断定为misfire.
