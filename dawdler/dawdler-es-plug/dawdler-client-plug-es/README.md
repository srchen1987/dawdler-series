# dawdler-client-plug-es

## 模块介绍

实现web端注入EsOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-plug-es</artifactId>
```

### 2. 使用方式

通过@EsInjector注解标识全局变量为EsOperator类型的变量即可.

```java
 @Controller
 public class ProductController{

    @EsInjector("myEs")//myEs为配置文件的名称,不包含后缀properties
    EsOperator esOperator;

    @RequestMapping(value = "/product/search", method = RequestMethod.GET)
    public List<Product> productSearch(String userId) {
        List<Product> list = termQuery("product", "name", "电冰箱", "addTime", 0, 10, false, Product.class);
        return list;
    }

    public <T> List<T> termQuery(
            String indexName,
            String searchField,
            String searchText,
            String sortedField,
            int fromIndex,
            int pageSize,
            boolean isDesc,
            Class<T> clazz)
            throws IOException {
        SearchResponse<T> response = esOperator.search(
                s -> s.index(indexName)
                        .query(q -> q.match(t -> t.field(searchField).query(searchText)))
                        // 分页查询，从第fromIndex页开始查询pageSize个document
                        .from(fromIndex)
                        .size(pageSize)
                        // 按要排序字段进行降序排序
                        .sort(f -> f.field(o -> o.field(sortedField).order(isDesc ? SortOrder.Desc : SortOrder.Asc))),
                clazz);

        return getSources(response);
    }

    private <T> List<T> getSources(SearchResponse<T> response) {
        List<T> result = new ArrayList<>();
        for (Hit<T> hit : getHitList(response)) {
            result.add(hit.source());
        }
        return result;
    }

    private <T> List<Hit<T>> getHitList(SearchResponse<T> response) {
        List<Hit<T>> hitList = response.hits().hits();
        if (hitList == null || hitList.isEmpty()) {
            return new ArrayList<>();
        }
        return hitList;
    }
 
 }

```

#### 2.1 web端支持注入的三种组件

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-handlerinterceptor-拦截器)

3、 [web端监听器WebContextListener](../../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)
