# dawdler-client-plug-es

## 模块介绍

实现dawdler-client端注入EsOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-es</artifactId>
```

### 2. 使用方式

通过@EsInjector注解标识全局变量为EsOperator类型的变量即可.

```java
 @Controller
 public class UserController{

    @EsInjector("myEs")//myEs为配置文件的名称,不包含后缀properties
    EsOperator esOperator;

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
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

#### 2.1 web端支持注入的三种组件

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-HandlerInterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)
