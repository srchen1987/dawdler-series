# dawdler-discovery-center-core

## 模块介绍

注册中心核心模块,提供DiscoveryCenter接口.需要扩展其他配置中心可引用此模块并实现DiscoveryCenter接口即可.

### 1. pom中引入依赖

```xml
<groupId>io.github.dawdler-series</groupId>
<artifactId>dawdler-discovery-center-core</artifactId>
```

### 2. DiscoveryCenter接口

```java
public interface DiscoveryCenter {
 
 /**
  * 初始化接口
  */
 void init() throws Exception;

 /**
  * 销毁接口
  */
 void destroy() throws Exception;
 
 /**
  * 获取服务列表
  */
 List<String> getServiceList(String path) throws Exception;

 /**
  * 添加服务提供者
  */
 boolean addProvider(String path, String value) throws Exception;

 /**
  * 更新服务提供者
  */
 default boolean updateProvider(String path, String value) throws Exception {
  return true;
 }


 /**
  * 删除服务提供者
  */
 default boolean deleteProvider(String path, String value) throws Exception {
  return true;
 }

 /**
  * 判断是否存在
  */
 boolean isExist(String path) throws Exception;

```
