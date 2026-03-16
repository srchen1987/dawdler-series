# dawdler-client-plug-velocity

## 模块介绍

velocity模板的一个插件,目前升到最新版本2.3.(之前经历过1.6,1.7,2.2三个版本)

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-plug-velocity</artifactId>
```

### 2. properties文件说明

具体配置请参考官方文档

```properties
velocimacro.library.autoreload=true
resource.loader.cache=false
resource.loader.class.cache=false
resource.default_encoding=utf-8
output.encoding=utf-8
template.path=/templates/
template.suffix=.vm
#自定义指令,需要扩展请参考velocity官方文档
runtime.custom_directives=club.dawdler.clientplug.velocity.direct.PageDirect,club.dawdler.clientplug.velocity.direct.ControlDirect
```

### 3. dawdler内置常用的指令

#### 3.1 分页指令 #pages

\#page指令提供一行代码分页的功能,应用如下：

示例1：

```html
//其中~p 是一个标识 固定写法,用来传递动态页的标识
//生成 首页 上一页 1 2 3 4 5 6 7 8 9 下一页 尾页
#pages("user/list.html?page=~p")

```

示例2：

```html
//生成 首页 上一页 1 2 3 4 5 6 7 8 9 下一页 尾页 增加额外参数visible
#pages("user/list.html?page=~p&visible=1")

```

示例3：

```html
//adminStyle是自定义的一种样式,示例1、示例2中未传入样式则采用系统默认.
#pages("user/list.html?page=~p&visible=1","adminStyle")

```

自定义样式扩展

参考[PageStyle](./src/main/java/club/dawdler/clientplug/velocity/PageStyle.java)源代码中的export方法：

```java
//相关注释可以看源代码
 public static void export(String prefix, String first, String up, String pages, String pageOn, String last,
   String end, String stepPage)
```

举例如下：

```java
//adminStyle扩展实现

export("adminStyle", "<span><a href=\"" + CONTENT_MARK + "\">首页</a></span>",
    "<a class=\"prev\" href=\"" + CONTENT_MARK + "\"></a>",
    "<a href=\"" + CONTENT_MARK + "\">" + P_MARK + "</a>",
    "<strong><font color=\"red\">" + P_MARK + "</font></strong>",
    "<a class=\"nxt\" href=\"" + CONTENT_MARK + "\"></a>",
    "<span><a href=\"" + CONTENT_MARK + "\">尾页</a></span>", null);

```

#### 3.2 XSS过滤指令 #XSSFilter

用于过滤xss脚本攻击,使用方式如下：

```html
//直接过滤字符串
#XSSFilter("<script>alert(1);</script>")

//举例输出实体对象中的属性,thread是后台返回的对象放置了velocity的上下文中.
#XSSFilter($thread.content)

```

#### 3.3 其他指令

\#tree指令废弃了 不在此介绍,这种树形建议采用前端组件实现.

\#control指令是动态表单实现的一组指令,如果有需要可以联系我.不在此介绍.

### 4. 关于模板路径的设置

velocity.properties 文件中配置模板路径和后缀

```properties
template.path=/templates/
template.suffix=.vm
```

### 5. VelocityToolBox的使用

自定义指令实现方式复杂,为了方便使用一些工具类的方法,提供了VelocityToolBox的扩展方法.

使用方式：

1、编写一个类继承VelocityToolBox,需要传入一个别名到构造函数中.

```java
public class MyTool extends VelocityToolBox{

 public MyTool(String name) {
  super(name);//别名 用于velocity
 }
 //定义一个转换大写的方法
 public String toUpperCase(String content) {
  if(content == null)
   return null;
  return content.toUpperCase();
 }
}
```

2、在resources下创建toolBoxes.properties文件并配置,properties中的key为别名,value为类名.

toolBoxes.properties 支持多环境配置 参考[统一配置中心与多环境支持](../../doc/dawdler-profiles.active-README.md).

```properties
myTool=com.anywide.yyg.user.velocity.tool.MyTool
```

3、在velocity中使用

```html
<html>
<head>
<title>velocity自定义工具类</title>
</head>

<body>
  $myTool.toUpperCase("hello")
</body>

</html> 
```

### 6. controller中使用velocity

```java
    @ResponseBody
    @RequestMapping(value = "/list.html", method = RequestMethod.GET, viewType = ViewType.velocity)
    public void list(Integer pageOn, Integer hostId, ViewForward viewForward) {
        if (pageOn == null) {
            pageOn = 1;
        }
        viewForward.setTemplatePath("/vm/list");
        PageResult<List<Vm>> pageResult = vmService.selectPageList(hostId, pageOn, 10);
        Map<String, Object> data = new HashMap<>();
        data.put("data", pageResult.getData());
        data.put("page", pageResult.getPage());
        viewForward.setData(data);
    }
  ```
