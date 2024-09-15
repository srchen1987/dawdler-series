# dawdler-es-core

## 模块介绍

es插件,通过pool2对elasticsearch-java进行封装实现的一套连接池.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-es-core</artifactId>
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

### 3. EsInjector注解

用于注入EsOperator,EsOperator的方法参考ElasticsearchClient类官方文档.

 EsInjector注解中的value传入fileName为配置文件名(不包含.properties后缀).

具体参考:

[dawdler-server-plug-es 实现dawdler-server端注入功能.](../dawdler-server-plug-es/README.md)

[dawdler-client-plug-es 实现web端注入功能.](../dawdler-client-plug-es/README.md)

### 4. 非dawdler架构下的使用方式

```java
// //通过调用ElasticSearchClientFactory的getInstance方法

public ElasticSearchClientFactory(String fileName);

//通过调用此方法来获取ElasticSearchClientFactory ,fileName是不包含后缀.properties.

//ElasticSearchClientFactory.getElasticSearchClient()可获取ElasticSearchClient对象,通过ElasticSearchClient对象调用getRestHighLevelClient可获取getElasticsearchClient对象对es进行操作,当调用结束时需调用ElasticSearchClient的close方法进行资源回收.

//例如：传入fileName为myEs,则需要在项目的classPath中创建配置文件myEs.properties.

```

注意：ElasticSearchClientFactory在客户端和服务器端中运行无需手动关闭,dawder会自动进行关闭相关资源.

在非dawdler架构下使用需要调用 ElasticSearchClientFactory.shutdownAll(); 来释放资源.

### 5. 吐槽ES客户端的设计

1、7.15之后不推荐使用RestHighLevelClient 吐槽RestHighLevelClient类的设计

RestHighLevelClient类很多方法被设置成了final,特别是close这种方法,导致开发者无法应用cglib这种动态代理的模式来拦截close方法(因为没有实现接口更没办法应用jdk的动态代理来实现).如果想重写close方法就必须用wrapper这种方式,又不能继承这个RestHighLevelClient,不伦不类这个词比较适合这种情况.

2、7.15之后推荐使用elasticsearch-java

ElasticsearchClient类没有实现接口 无法通过jdk来做动态代理.
