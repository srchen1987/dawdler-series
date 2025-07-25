# dawdler-client-plug-web

## 模块介绍

webmvc模块,使用上基本与spring mvc一致.提供远程加载组件的客户端,远程加载组件通知器,web监听器,web拦截器,aop实现.

### 1. pom中引入依赖

```xml
<groupId>club.dawdler</groupId>
<artifactId>dawdler-client-plug-web</artifactId>
```

### 2. webmvc框架使用方法

#### 2.1 创建Controller

编写一个Controller类上加入注解@Controller.

#### 2.2 创建API

通过RequestMapping定义webApi,RequestMapping可以用在类上和方法上,也可以用在类上,用在某个类上那么所有webApi的开头都必须以用在类上定义的RequestMapping开头.(RequestMapping用在类上只有value有效,其余无效)

### 3. Controller注解

#### 3.1 标识注解介绍

| 注解 | 作用域 | 描述 |
| :-: | :-: | :-: |
| @Controller | 类 | 标识一个类为Controller |
| @RequestMapping  | 类或方法 | 标识一个api |
| @ResponseBody  | 方法 | 将api的method返回值以json类型输出 |

#### 3.2 方法参数注解介绍

| 注解 | 作用域 | 描述 | 支持转换 |
| :-: | :-: | :-: | :-: |
| @RequestParam | 参数 | 获取request参数,其中value为参数名 | 支持类型转换 |
| @PathVariable | 参数 | 获取antPath的参数,其中value为antPath变量名 | 支持类型转换 |
| @RequestAttribute | 参数 | 获取request作用域下的属性值,其中value为属性名 | 不支持 |
| @SessionAttribute | 参数 | 获取session作用域下的属性值,其中value为属性名 | 不支持 |
| @RequestHeader | 参数 | 获取http请求头值,其中value为请求头名 | 只支持String或String[] |
| @CookieValue | 参数 | 获取cookie值,其中value为cookie名 | 只支持String |
| @RequestBody | 参数 | String接收json请求体或自定义的对象通过json方式进行映射(前端提交必须以body中传递json体的方式提交) | 只支持String |

#### 3.3 RequestMapping源码注释

```java
public @interface RequestMapping {
 String[] value() default {};//path 支持antPath 只有value可以用到类上,以下其他只在方法上生效

 RequestMethod[] method() default {};//请求方法 POST GET以及其他

 ViewType viewType() default ViewType.json;//响应的视图类型 支持json,jsp,velocity

 boolean generateValidator() default false;//生成验证规则,根据后台的验证框架生成前端的表达式

 String input() default "";//配置验证框架之后验证未通过的跳转路径,默认为空,返回json类型的错误提醒,如果配置会在request域下设置属性validate_error并forward到指定的路径

 long uploadSizeMax() default 0l;//上传文件最大的限制,单位byte

 long uploadPerSizeMax() default 0l;//上传单个文件最大的限制,单位byte

 String exceptionHandler() default "";//异常处理者,系统内提供三种处理者json, jsp, velocity,会根据ViewType自动选择,如果有需要可以扩展,参考HttpExceptionHolder的register方法,可以在监听器启动时扩展,一般不会考虑扩展所以没采用SPI方式配置

 enum ViewType {
  json, jsp, velocity
 }
}
```

### 4. Controller方法参数类型说明

支持方法参数类型如下：

#### 4.1 基础类型

| long | int | short | byte | double | float | boolean | char |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |

| long[] | int[] | short[] | byte[] | double[] | float[] | boolean[] | char[] |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |

#### 4.2 基础类型包装类

| Long | Integer | Short | Byte | Double | Float | Boolean | Character |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |

| Long[] | Integer[] | Short[] | Byte[] | Double[] | Float[] | Boolean[] | Character[] |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |

#### 4.3 大数值对象

| BigDecimal | BigDecimal[] |
| :-: | :-: |

#### 4.4 文件上传对象

| UploadFile | UploadFile[] |
| :-: | :-: |

说明：

UploadFile类中的一些方法

getInputStream() //输入流

getBytes() //字节数组

getFileName() //获取文件名

getSize() //获取文件大小

delete() //删除文件,此方法架构会自动调用无需开发者调用

#### 4.5 其他内置对象

| HttpServletRequest | HttpServletResponse | HttpSession | InputStream | Reader | PrintWriter | Locale | Map | ViewForward |
| :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: | :-: |

说明:

Map 为 request.getParameterMap();

InputStream 为 request.getInputStream();

Reader 为 request.getReader();

PrintWriter 为 response.getWriter();

Locale 为 request.getLocale();

ViewForward 提供了非常丰富的api 可以设置数据集,可以设置模板路径

#### 4.6 自定义对象

使用@RequestBody注解标识

#### 4.7 部分示例

示例1：
演示@RequestParam使用方式
如果配置@RequestParam 并指定value按value获取,未配置按参数名获取,如：

```java
createOrder(@RequestParam("pid") Integer productId, @RequestParam Integer stock, @RequestParam BigDecimal amount) throws Exception {

```

其中productId被重定义为pid,表单提交需要传入pid.

stock参数表单提交需要传入stock

 示例2：@Controller注解

```java
@Controller
@RequestMapping(value = "/order")
public class OrderController { 
 
 @RequestMapping(value = "/createOrder.do", viewType = ViewType.json)
 public void createOrder(@RequestParam Integer productId, @RequestParam Integer stock,
   @RequestParam BigDecimal amount, ViewForward viewForward) throws Exception {
  Map<String, Object> result = new HashMap<>();
  result.put("success", true);
  viewForward.setData(result);
 }
 
 
 @RequestMapping(value = "/order.html", viewType = ViewType.velocity)
 public void order(ViewForward viewForward) throws Exception {
  viewForward.setTemplatePath("order/add.html");//注入ViewForward 来设置templatePath
 }

}
```

 示例3：@ResponseBody注解

```java
@Controller
//@RequestMapping(value = "/order") 可以注释掉 这样就无需/order
public class OrderController { 
 
 @RequestMapping(value = "/createOrder.do")
 @ResponseBody
 public Map createOrder(@RequestParam Integer productId, @RequestParam Integer stock,
   @RequestParam BigDecimal amount, ViewForward viewForward) throws Exception {
  Map<String, Object> result = new HashMap<>();
  result.put("success", true);
  return result;
 }
 
 }

 ```

### 5. HandlerInterceptor 拦截器

拦截器的作用与spring mvc的一样,实现接口HandlerInterceptor,如果有多个拦截器,支持@Order注解进行升序排序,拦截请求api之前,之后,渲染模板之后的方法.

示例：

```java

@Order(1)//可以有多个拦截器 支持排序 排序为升序
public class UserWebInterceptor implements HandlerInterceptor {

 @Override
 public boolean preHandle(Object controller, ViewForward viewForward, RequestMapping requestMapping)
   throws Exception {
  String uri = viewForward.getRequest().getRequestURI();
  System.out.println(this.getClass().getSimpleName() + " preHandle " + uri);
  return true;
 }

 @Override
 public void postHandle(Object controller, ViewForward viewForward, RequestMapping requestMapping, Throwable ex)
   throws Exception {
  System.out.println(this.getClass().getSimpleName() + " postHandle ");
 }

 @Override
 public void afterCompletion(Object controller, ViewForward viewForward, RequestMapping requestMapping,
   Throwable ex) {
  System.out.println(this.getClass().getSimpleName() + " afterCompletion ");
 }

}


```

### 6. WebContextListener 监听器

监听器的作用与Servlet提供的ServletContextListener完全一致,如果有多个监听器,支持@Order注解进行升序排序.目前只提供容器启动与销毁的监听器(HttpSessionListener,ServletRequestListener,HttpSessionActivationListener 不提供,如果有需要采用servlet提供的即可).

示例：

```java
package com.anywide.yyg.user.web.listener;

import javax.servlet.ServletContext;

import club.dawdler.clientplug.web.listener.WebContextListener;
import club.dawdler.core.annotation.Order;

@Order(2)//可以有多个监听器 支持排序 排序为升序
public class UserWebContextListener implements WebContextListener {

 @Override
 public void contextInitialized(ServletContext servletContext) {
  System.out.println(this.getClass().getSimpleName() + "  contextInitialized " + servletContext.getRealPath(""));
 }

 @Override
 public void contextDestroyed(ServletContext servletContext) {
  System.out.println(this.getClass().getSimpleName() + " contextDestroyed " + servletContext.getRealPath(""));
 }

}

```

### 7. ViewForward介绍

ViewForward是一个传递request,response,设置模板路径,指定响应状态等等的一个类.

ViewForward可以注入到方法参数列表中.

常用方法：

```java
String getUriShort() //获取请求uri

void setInvokeException(Throwable invokeException) //设置执行异常,设置异常后会记录错误日志,如果是jsp或velocity响应视图会通过response进行响应http状态码为500,具体实现：response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error!");

//如果是json响应视图会直接响应http状态码500,内容体 {"msg","Internal Server Error!"}

public void putData(String key, Object value); //放置数据,如：request.setAttribute("name","jackson"); 如果是json视图输出{"name", "jackson"},如果是velocity视图在context中会设置context.put("name", "jackson"); 模板中即可通过$name获取.

public void setAddRequestAttribute(boolean addRequestAttribute); //默认为false,设置自动将request作用域下的属性添加到context中,等同于调用putData(String key, Object value);

public void setData(Map<String, Object> data); //直接将参数data覆盖到context中.

public String getHeader(String headerName); //获取请求Header,等同于request.getHeader(String name);

public void setTemplatePath(String templatePath); //设置模板路径,velocity或jsp的路径,velocity模板和jsp根路径默认为 WEB-INF/template/.
```

param*系列方法如下：

源码示例1：

```java
public short paramShort(String paramName) {
  try {
   return Short.parseShort(getRequest().getParameter(paramName));
  } catch (Exception e) {
   return 0;
  }
 }
```

源码示例2：

```java
public long paramLong(String paramName, long value) {
  try {
   return Long.parseLong(getRequest().getParameter(paramName));
  } catch (Exception e) {
   return value;
  }
 }
```

### 8. RemoteClassLoaderFire 加载类通知器

需要获取加载类触发一些操作可以实现RemoteClassLoaderFire接口,通过SPI方式扩展,支持@Order注解进行升序排序,参考[WebComponentClassLoaderFire](src/main/java/club/dawdler/clientplug/web/fire/WebComponentClassLoaderFire.java),用于实现自动注入Service到Controller,WebContextListener,HandlerInterceptor.(普通开发人员一般无须扩展)

### 9. DisplayPlug 视图插件扩展

dawdler内部提供[JsonDisplayPlug](src/main/java/club/dawdler/clientplug/web/plugs/impl/JsonDisplayPlug.java),[JspDisplayPlug](src/main/java/club/dawdler/clientplug/web/plugs/impl/JspDisplayPlug.java),[VelocityDisplayPlug](../dawdler-client-plug-velocity/src/main/java/club/dawdler/clientplug/web/plugs/impl/VelocityDisplayPlug.java)三种视图插件,如果有其他需要,比如freemarker的需求可以实现DisplayPlug接口,通过SPI方式来进行扩展.可以参考系统内的三个插件.(普通开发人员一般无须扩展)

注意: 如果方法标记了@ResponseBody 返回类型是基本数据类型或String类型或BigDecimal类型则直接输出类型为text/html;charset=UTF-8,其他类型会转换为json类型为application/json;charset=UTF-8.

### 10. 扫描组件包配置

web-conf.xml是web端核心配置文件. 包含组件扫描,配置据源定义,指定目标包定义数据源,读写分离配置,健康检测.

package-path配置当前的web环境中的包扫描路径(部署在web容器中的lib或classes中的包路径并支持antpath).

示例：

```xml
 <scanner>
  <package-paths>
   <package-path>com.dawdler.order.controller</package-path>
   <package-path>com.dawdler.**.service.impl</package-path>
  </package-paths>
 </scanner>
```

### 11. aop使用方式

dawdler的aop支持采用aspjectJ来实现,没有采用Load-time weaving和cglib(spring的实现)方式.

适用范围：aop支持web端的Controller,HandlerInterceptor,WebContextListener,api接口(Service接口)这四种类型的切入.

示例(拦截Controller)：

注意: 以下两个文件都需要在web端创建

1、创建META-INF\aop.xml

```xml
<aspectj>
  <aspects>
    <aspect name="com.anywide.yyg.user.controller.UserControllerAspect"/>
  </aspects>
</aspectj>
```

2、创建com.anywide.yyg.user.controller.UserControllerAspect

```java
@Aspect
public class UserControllerAspect {

 @Pointcut("execution(*  com.anywide.yyg.user.controller..UserController.list(..))")
 public void list() {
 }

 @Around("list()")
 public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
  System.out.println(pjp.getSignature().getName() + ":" + pjp.getTarget());
  Object o = null;
  try {
   o = pjp.proceed();
  } catch (Throwable t) {
   throw t;
  }
  Object[] args = pjp.getArgs();
  System.out.println(o+":\t" + args);
  return o;
 }

}
```

或直接通过Around拦截无须Pointcut

```java
@Aspect
public class UserControllerAspect {
 
 @Around("execution(*  com.anywide.yyg.user.controller..UserController.list(..))")
 public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
  System.out.println(pjp.getSignature().getName() + ":" + pjp.getTarget());
  Object o = null;
  try {
   o = pjp.proceed();
  } catch (Throwable t) {
   throw t;
  }
  Object[] args = pjp.getArgs();
  System.out.println(o+":\t" + args);
  return o;
 }

}
```

拦截api接口(Service接口)的示例,请参考[分布式事务拦截注解的实现](../dawdler-distributed-transaction/dawdler-distributed-transaction-core/src/main/java/club/dawdler/distributed/transaction/aspect/DistributedTransactionAspect.java),拦截api定义的DistributedTransaction注解.

### 12. 健康检测

健康检测是指系统在启动时,检查系统的各个组件是否正常工作,可以为k8s的liveness,readiness提供该服务.

如果设有带(Basic Authentication)的认证,请通过head头加入Authorization头信息.

配置位于web-conf.xml的health-check节点,示例:

```xml
	<health-check check="on" uri="/health" username="jackson" password="jackson.song">
		<config check="on"/>
		<dataSource check="on" />
		<rabbit check="on" />
		<jedis check="on" />
		<elasticSearch check="on" />
	</health-check>
```

check="on" 为开启健康检测,off为关闭.关闭后不会开启http/https服务.

username="jackson" 用户名,未填写该属性则不开启认证模块.

password="jackson.song" 密码,未填写该属性则不开启认证模块.

uri="/health" 健康检测的uri,默认是/health.

以下为已支持健康检测的组件节点,check设为off或不填此节点则不会触发健康检测.

elasticSearch es检测

jedis redis检测

rabbit rabbitmq检测

dataSource 数据源检测

config 配置中心检测

通过wget验证:

```shell
wget http://localhost:8082/health
```

返回:

```json
{"data":{"status":"UP","dataSource":{"user_writeDataSource":{"status":"UP"},"user_readDataSource":{"status":"UP"},"status":"UP"},"config":{"consul":{"info":"srchen-dc1-1.20.1","status":"UP"},"status":"UP"}},"name":"","status":"UP"}
```
