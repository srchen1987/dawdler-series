# dawdler-client-plug-velocity

## 模块介绍

dawdler-client-plug-velocity velocity模板的一个插件，目前升到最新版本2.3。(之前经历过1.6，1.7，2.2三个版本)

### 1. web端的pom中引入依赖

```xml
 <groupId>dawdler</groupId>
 <artifactId>dawdler-client-plug-velocity</artifactId>
```

### 2. properties文件说明

具体配置请参考官方文档

```properties
velocimacro.library.autoreload=true
resource.loader.cache=true
resource.default_encoding=utf-8
output.encoding=utf-8
#resource.loaders=jar
resource.loader.jar.class=org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
resource.loader.jar.cache=true
runtime.custom_directives=com.anywide.dawdler.clientplug.velocity.direct.PageDirect,com.anywide.dawdler.clientplug.velocity.direct.ControlDirect #自定义指令，需要扩展请参考velocity官方文档
```

### 3. dawdler内置常用的指令

#### 3.1 分页指令 #pages

\#page指令提供一行代码分页的功能，应用如下：

示例1：

```html
//其中~p 是一个标识 固定写法，用来传递动态页的标识
//生成 首页 上一页 1 2 3 4 5 6 7 8 9 下一页 尾页
#page("user/list.html?page=~p")

```

示例2：

```html
//生成 首页 上一页 1 2 3 4 5 6 7 8 9 下一页 尾页 增加额外参数visible
#page("user/list.html?page=~p&visible=1")

```

示例3：

```html
//adminStyle是自定义的一种样式，示例1、示例2中未传入样式则采用系统默认。
#page("user/list.html?page=~p&visible=1","adminStyle")

```

自定义样式扩展

参考PageStyle源代码中的export方法：

```java
//相关注释可以看源代码
 public static void export(String prefix, String first, String up, String pages, String pageon, String last,
   String end, String steppage)
```

举例如下：

```java
//adminStyle扩展实现

export("adminStyle", "<span><a href=\"" + CONTENTMARK + "\">首页</a></span>",
    "<a class=\"prev\" href=\"" + CONTENTMARK + "\"></a>",
    "<a href=\"" + CONTENTMARK + "\">" + PMARK + "</a>",
    "<strong><font color=\"red\">" + PMARK + "</font></strong>",
    "<a class=\"nxt\" href=\"" + CONTENTMARK + "\"></a>",
    "<span><a href=\"" + CONTENTMARK + "\">尾页</a></span>", null);

```

#### 3.2 XSS过滤指令 #XSSFilter

用于过滤xss脚本攻击，使用方式如下：

```html
//直接过滤字符串
#XSSFilter("<script>alert(1);</scropt>")

//举例输出实体对象中的属性
#XSSFilter($thread.content)

```

#### 3.3 其他指令

\#tree指令废弃了 不在此介绍，这种树形建议采用前端组件实现。

\#control指令是动态表单实现的一组指令，如果有需要可以联系我。不在此介绍。

### 4. 关于模板路径的设置

参考VelocityDisplayPlug类中的init方法，在init方法中定义了模板的路径。
具体代码：

```java
String templatePath = servletContext.getInitParameter("template-path");
  VelocityTemplateManager tm = VelocityTemplateManager.getInstance();
  String path;
  if (templatePath != null && !templatePath.trim().equals(""))
   path = servletContext.getRealPath("WEB-INF/" + templatePath);
  else
   path = servletContext.getRealPath("WEB-INF/template");
  
```

如果在servletContext指定了初始化参数则按指定的来设置，如果没设置默认则为WEB-INF/template。
