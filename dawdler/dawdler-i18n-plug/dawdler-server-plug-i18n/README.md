# dawdler-server-plug-i18n

## 模块介绍

实现dawdler-server端注入I18nOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-server-plug-i18n</artifactId>
```

### 2. 使用方式

#### 2.1 I18n的使用方式

通过@I18nInjector注解标识全局变量为I18nOperator类型的变量即可.

```java
@Service
public class UserServiceImpl implements UserService {

    @I18nInjector("messages")//messages为配置文件的基础名称,不包含后缀properties
    I18nOperator i18nOperator;

    public User getUser(String userId) {
        String message = i18nOperator.get("user.welcome");//使用i18nOperator对象
        System.out.println(message);
        return null;
    }
 
}
```

#### 2.2 在过滤器中使用

```java
public class MyFilter implements DawdlerFilter {

    @I18nInjector("messages")//messages为配置文件的基础名称,不包含后缀properties
    I18nOperator i18nOperator;

    @Override
    public void doFilter(DawdlerRequest request, DawdlerResponse response, FilterChain chain) throws IOException, ServletException {
        String message = i18nOperator.get("filter.processing");//使用i18nOperator对象
        System.out.println(message);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init() throws ServletException {
    }
}
```

#### 2.3 dawdler服务端支持注入的两种组件

1、 [DawdlerFilter服务过滤器](../../dawdler-server/README.md#4-dawdler服务过滤器)

2、 [@Service注解的接口实现类](../../dawdler-service-plug/dawdler-service-core/README.md#2-service说明)
