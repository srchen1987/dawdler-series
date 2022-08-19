# dawdler-server-plug-es

## 模块介绍

实现dawdler-server端注入EsOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-server-plug-es</artifactId>
```

### 2. 使用方式

通过@EsInjector注解标识全局变量为EsOperator类型的变量即可.

```java
 
 public class UserServiceImpl implements UserService {

    @EsInjector("myEs")//myEs为配置文件的名称,不包含后缀properties
    EsOperator esOperator;

    public User getUser(String userId) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name", "电冰箱");
        searchSourceBuilder.query(matchQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = esOperator.search(searchRequest, Product.class);//使用esOperator对象
        System.out.println(response);
        return null;
    }
 
 }

```

#### 2.1 dawdler服务端支持注入的三种组件

1、 [DawdlerFilter服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [DawdlerServiceListener监听器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [@RemoteService注解的接口实现类](../../dawdler-core/README.md#2-RemoteService注解)
