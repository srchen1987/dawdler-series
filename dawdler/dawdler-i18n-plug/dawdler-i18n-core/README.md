# dawdler-i18n-core

## 模块介绍

国际化核心模块，提供注解和动态代理实现国际化资源的注入功能。

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-i18n-core</artifactId>
```

### 2. properties文件说明

国际化资源配置文件需要放置在classpath路径下，文件命名规则为`基础名称.properties`或`基础名称_语言代码.properties`。

例如：

- messages.properties (默认语言)
- messages_zh.properties (中文)
- messages_en.properties (英文)
- messages_zh_CN.properties (简体中文)
- messages_en_US.properties (美式英语)

配置文件内容示例(messages.properties)：

```properties
# 欢迎信息
welcome.message=欢迎使用系统
user.login.success=用户登录成功
error.code.404=页面未找到
```

配置文件内容示例(messages_en.properties)：

```properties
# Welcome message
welcome.message=Welcome to the system
user.login.success=User login successful
error.code.404=Page not found
```

### 3. I18nInjector注解

用于注入I18nOperator，提供国际化资源的获取功能。

I18nInjector注解中的value传入baseName为资源文件的基础名称(如messages)。

参数说明：

- value: 资源文件的基础名称，不包含语言代码和.properties后缀

具体参考:

[dawdler-server-plug-i18n 实现dawdler-server端注入功能.](../dawdler-server-plug-i18n/README.md)

[dawdler-client-plug-i18n 实现web端注入功能.](../dawdler-client-plug-i18n/README.md)

### 4. I18nOperator接口

提供获取国际化资源的方法:

```java
String get(String key); // 根据key获取当前语言环境下的资源
String get(String key, Locale locale); // 根据key和指定语言环境获取资源
```

方法说明：

- get(String key): 根据资源键获取当前语言环境下的资源值
- get(String key, Locale locale): 根据资源键和指定语言环境获取资源值

### 5. 使用示例

#### 5.1 基本使用方式

以下示例在dawdler托管的组件(如@Service、@Controller标注的类)中通过`@I18nInjector`注入I18nOperator.

```java
@Service("my-service")
public class MyService {
    @I18nInjector("messages") // messages为资源文件的基础名称
    private I18nOperator i18nOperator;
    
    public void doSomething() {
        // 获取当前语言环境下的资源
        String message = i18nOperator.get("welcome.message");
        // 获取指定语言环境下的资源
        String englishMessage = i18nOperator.get("welcome.message", Locale.ENGLISH);
    }
}
```

#### 5.2 在Web环境中的使用

在Web环境中，语言环境会自动从HTTP请求中获取，无需手动设置。

```java
@Controller
public class UserController {
    @I18nInjector("messages") // messages为资源文件的基础名称
    private I18nOperator i18nOperator;
    
    @RequestMapping(value = "/welcome", method = RequestMethod.GET)
    public String welcome() {
        // 自动根据请求中的Accept-Language头获取对应语言的资源
        String welcomeMessage = i18nOperator.get("welcome.message");
        return welcomeMessage;
    }
}
```

#### 5.3 在服务端环境中的使用

```java
@Service
public class UserServiceImpl implements UserService {
    @I18nInjector("messages") // messages为资源文件的基础名称
    private I18nOperator i18nOperator;
    
    public String getUserWelcomeMessage(String userId) {
        // 在服务端环境中，可以指定语言环境
        String message = i18nOperator.get("user.login.success");
        return message;
    }
}
```
