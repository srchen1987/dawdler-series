# dawdler-core

## 模块介绍

dawdler-server与dawdler-client公用的核心模块.包含网络,服务发现实现,线程池,注解,压缩算法等(系统内部使用,用户无须关注).相关注解在不同的模块有说明.

### 1. pom中引入依赖

```xml
 <groupId>io.github.dawdler-series</groupId>
 <artifactId>dawdler-core</artifactId>
```

### 2. ComponentLifeCycle 容器生命周期接口

用于实现客户端/服务端容器,启动前/启动后/销毁的生命周期管理.

```java
public interface ComponentLifeCycle {

 default public void prepareInit() throws Throwable {
 };
 
 default public void init() throws Throwable {
 };

 default public void destroy() throws Throwable {
 };

}
```

在客户端(tomcat)中的顺序 prepareInit > WebContextListener > init

在服务端(dawdler)中的顺序 prepareInit > init > DawdlerServiceListener

具体实现可参考 redis、rabbitmq等组件相关实现该接口的类.

### 3. CustomComponentInjector 定制化组件注入器

通过实现此接口来实现注入功能,可以参考其他实现类.

```java

public interface CustomComponentInjector {

 /**
  * 注入方法
  */
 public void inject(Class<?> type, Object target) throws Throwable;

 /**
  * 匹配的类或接口
  */
 Class<?>[] getMatchTypes();

 /**
  * 匹配的注解
  */
 Set<? extends Class<? extends Annotation>> getMatchAnnotations();

}

```