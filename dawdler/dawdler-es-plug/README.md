# dawdler-es-plug

## 模块介绍

dawdler-es-plug es插件,通过pool2对elasticsearch-rest-high-level-client进行封装实现的一套连接池.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-es-plug</artifactId>
```

### 2. properties文件说明

```properties
hosts=192.168.1.111:9200,192.168.1.112:9200,192.168.1.113:9200 #es主机列表用,隔开
username=myusername #用户名 如果没有设置,设空即可
password=mypassword #密码   如果没有设置,设空即可

connectionRequestTimeout=-1 #connetcion pool中获得一个connection的超时时间 默认-1 单位秒
connectTimeout=-1 #链接建立的超时时间 默认-1 单位秒
socketTimeout=-1 #响应超时时间 默认-1 单位秒

pool.maxTotal=20 #最大连接数
pool.minIdle=2 #最小空闲数
pool.maxIdle=8 #最大空闲数
pool.maxWaitMillis=10000 #最大等待时长(单位毫秒)
pool.testOnBorrow=false #获取连接时是否验证连接有效 默认为false
pool.testOnCreate=false #创建连接时是否验证连接有效 默认为false
pool.testOnReturn=false #反还连接时是否验证连接有效 默认为false


```

### 3. 使用方式

```java
// //通过调用ElasticSearchClientFactory的getInstance方法

public ElasticSearchClientFactory(String fileName);

//通过调用此方法来获取ElasticSearchClientFactory ,fileName是不包含后缀.properties.

//ElasticSearchClientFactory.getElasticSearchClient()可获取ElasticSearchClient对象,通过ElasticSearchClient对象调用getRestHighLevelClient可获取RestHighLevelClient对象对es进行操作,当调用结束时需调用ElasticSearchClient的close方法进行资源回收.

//例如：传入fileName为myes,则需要在项目的classPath中创建配置文件es.properties.

//注意：ElasticSearchClientFactory在容器停止时需要配置Listener自行关闭,关闭的方法为close();

//参考dawdler-distributed-transaction-client下的WebListener2ReleaseResources在web容器停止时释放资源.

//参考dawdler-distributed-transaction-server下的DawdlerListener2ReleaseResources在dawdler容器停止时释放资源.

```
