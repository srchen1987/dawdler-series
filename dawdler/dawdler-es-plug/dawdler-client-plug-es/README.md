# dawdler-client-plug-es

## 模块介绍

dawdler-client-plug-es 实现dawdler-client端注入功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-es</artifactId>
```

### 2. 使用方式

通过@EsRestHighLevelInjector注解标识全局变量为EsRestHighLevelOperator类型的变量即可.

```java
 @Controller
 public class UserController{

    @EsRestHighLevelInjector("myEs")//myEs为配置文件的名称,不包含后缀properties
    EsRestHighLevelOperator esRestHighLevelOperator;

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public User getUser(String userId) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name", "电冰箱");
        searchSourceBuilder.query(matchQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = esRestHighLevelOperator.search(searchRequest, RequestOptions.DEFAULT);//使用esRestHighLevelOperator对象
        SearchHits hits = response.getHits();
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            System.out.println("输出数据:" + iterator.next().getSourceAsString());
        }
        return null;
    }
 
 }

```

可注入的范围:

1、 [Controller](../../dawdler-client-plug/README.md#2-1-创建Controller)

2、 [HandlerInterceptor拦截器](../../dawdler-server/README.md#3-dawdler服务器启动销毁监听器)

3、 [WebContextListener监听器](../../dawdler-core/README.md#2-RemoteService注解)
