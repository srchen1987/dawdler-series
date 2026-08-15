# dawdler-client-plug-i18n

## 模块介绍

实现web端注入I18nOperator的功能.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-plug-i18n</artifactId>
```

### 2. 使用方式

#### 2.1 I18n的使用方式

在Web环境中，语言环境会按照以下优先级自动识别：

1. URL参数中的locale参数 (例如: ?locale=zh_CN)
2. HTTP请求头中的Accept-Language
3. 系统默认语言环境

通过@I18nInjector注解标识全局变量为I18nOperator类型的变量即可.

```java
@Controller
public class UserController{

    @I18nInjector("messages")//messages为资源文件的基础名称,不包含后缀properties
    I18nOperator i18nOperator;

    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public User getUser(String userId) {
        String message = i18nOperator.get("user.welcome");//使用i18nOperator对象
        System.out.println(message);
        return null;
    }

 }
```

#### 2.2 在拦截器中使用

```java
public class MyInterceptor implements HandlerInterceptor {

    @I18nInjector("messages")//messages为资源文件的基础名称,不包含后缀properties
    I18nOperator i18nOperator;

    @Override
    public boolean preHandle(Object controller, ViewForward viewForward, RequestMapping requestMapping) throws Exception {
        String message = i18nOperator.get("interceptor.prehandle");//使用i18nOperator对象
        System.out.println(message);
        return true;
    }

    @Override
    public void postHandle(Object controller, ViewForward viewForward, RequestMapping requestMapping, Throwable ex) throws Exception {
        String message = i18nOperator.get("interceptor.posthandle");//使用i18nOperator对象
        System.out.println(message);
    }

    @Override
    public void afterCompletion(Object controller, ViewForward viewForward, RequestMapping requestMapping, Throwable ex) {
        String message = i18nOperator.get("interceptor.aftercompletion");//使用i18nOperator对象
        System.out.println(message);
    }
}
```

#### 2.3 web端支持注入的两种组件

1、 [web端controller](../../dawdler-client-plug-web/README.md#3-controller注解)

2、 [web端拦截器HandlerInterceptor](../../dawdler-client-plug-web/README.md#5-handlerinterceptor-拦截器)
