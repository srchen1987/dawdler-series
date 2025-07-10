# dawdler-client-plug-validator

## 模块介绍

是一个强大的前后端通用校验器,支持js和java后端通用表达式校验,支持扩展,支持后端校验规则生成前端表达式,java后端支持分组,继承,排除等特性,js支持校验扩展,各种事件扩展.

### 1. pom中引入依赖

```xml
 <groupId>club.dawdler</groupId>
 <artifactId>dawdler-client-plug-validator</artifactId>
```

### 2. 接入方式

#### 2.1 查看系统支持的验证规则

通过调用RuleOperatorProvider的静态方法help可以查看目前系统内支持的规则(包含自定义扩展).

以下是系统自带的一些校验规则,能够满足绝大部分验证需求.

```text
#stringRule list 字符类验证规则
状态码:[ date ] 日期验证
状态码:[ chineseWords ] 中文字母验证
状态码:[ realNumber ] 实数验证
状态码:[ IDCard ] 身份证验证
状态码:[ lowercaseLetters ] 小写字母验证
状态码:[ negativeNumber ] 负整数验证
状态码:[ positiveNumber ] 正整数验证
状态码:[ webSite ] 网址验证
状态码:[ number ] 整数验证
状态码:[ regularCharacter ] 汉字字母或数字验证
状态码:[ natureNumber ] 自然数验证
状态码:[ capitalLetters ] 大写字母验证
状态码:[ phone ] 座机验证
状态码:[ notEmpty ] 不能为空验证
状态码:[ englishWords ] 英文字母验证
状态码:[ cellPhone ] 手机号验证
状态码:[ email ] 邮箱验证
#regexRule list 正则类验证规则
状态码:[ ^contain:\[(.+)\]$ ] 规定性范围内包含验证,如：contain:[China,1] ,表单中必须出现China或1 !
状态码:[ ^minNumber:([-+]?\d+(\.\d+)?$) ] 最小数值不能小于指定数字如:minNumber:25或minNumber:25.32!
状态码:[ ^maxSize:([1-9]{1}\d*$) ] 字符串或数组中的字符串的长度不能大于指5定长度,如：maxSize:32!
状态码:[ ^maxSelect:([1-9]{1}\d*$) ] 最大选择数或最大参数个数或List或数组的长度不能大于指定数字如:maxSelect:3!
状态码:[ ^minSelect:([1-9]{1}\d*$) ] 最大选择数或最小参数个数或List或数组的长度不能小于指定数字如:minSelect:3!
状态码:[ ^noContain:\[(.+)\]$ ] 规定性范围内不包含验证,如：noContain:[China,1] ,表单中不能出现China或1 !
状态码:[ ^maxNumber:([-+]?\d+(\.\d+)?$) ] 最大数值不能大于指定数字如:maxNumber:25或maxNumber:25.32!
状态码:[ ^minSize:([1-9]{1}\d*$) ] 字符串或数组中的字符串的长度不能小于指定长度,如：minSize:3!
```

#### 2.2 配置验证器

在controller同级目录创建一个\{controllerName\}-validator.xml结尾的文件.

验证器支持对 application/x-www-form-urlencoded、multipart/form-data、application/json(@RequestBody)、http-header(@RequestHeader)、antPath中的@PathVariable变量进行校验.

示例 ：

定义UserController.java,UserController-validator.xml.

UserController-validator.xml 内容：

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<validator><!-- 根节点 -->
    <validator-fields><!--定义控件信息的根节点--> 
        <validator-field name="userid" explain="用户Id">
            <![CDATA[notEmpty&positiveNumber]]>
        </validator-field><!-- 控件信息节点,@name 控件名称  @explain 控件描述  @globalRules 放置的是全局别名配置文件中的key 如果填写则和本控件内的验证规则进行组合(@globalRules 需求很少不容易理,解所以废弃了 )  . <![CDATA[]]> 为验证规则,其中的内容为系统内支持的规则(包含自定义扩展) -->
        <validator-field name="username" explain="用户名">
            <![CDATA[notEmpty&maxSize:32]]>
        </validator-field>
        <validator-field name="password" explain="密码">
            <![CDATA[notEmpty&maxSize:32]]>
        </validator-field>
        <validator-field name="age" explain="年龄">
            <![CDATA[positiveNumber]]>
        </validator-field>
        <validator-field name="platform" explain="平台">
            <![CDATA[notEmpty&positiveNumber]]>
        </validator-field>
    </validator-fields>

    <validator-fields-groups><!-- 控件信息组 -->
    
    <!-- 控件信息组,如果同一个Controller里 有多个api都需要验证某些控件信息 则可以建立一个组,分别refgid这个组id即可.
    组之间也可以refgid引用,如果涉及相互依赖问题,系统会提醒错误！ -->
        <validator-fields-group id="add"><!-- 控件信息组节点 @id 组的标识id,供其他组或mapping验证器进行引用 -->
            <validator ref="userid"/><!-- 组内控件 @ref 引入validator-field中的定义.
            如果不存在则到全局global-validator.xml中引用,@refgid 引入其它组,@ref与@refgid可以并存. -->
            <validator refgid="edit"/>
        </validator-fields-group>

        <validator-fields-group id="edit">
                <validator ref="username"/>
                <validator ref="password"/>
                <validator ref="age"/>
        </validator-fields-group>
    </validator-fields-groups>
    <validator-mappings><!-- 验证器组 -->
        <validator-mapping name="/user/regist"><!-- 验证器 ＠name　请求的URI RequestMapping中定义的具体api地址.
        注意：如果类上有RequestMapping定义,需要将类上的RequestMapping中的value与方法上的RequestMapping中的value整合到一起.
        @skip为跳过某些验证,支持跳过多个规则,可以用 , 英文逗号隔开,里面写入的为validator-field的@name -->
            <validator refgid="add" skip="age,username"/> <!-- 跳过了age和username的验证 -->
        </validator-mapping>
        <validator-mapping name="/user/edit">
            <validator refgid="edit"/>
            <validator ref="platform" type="header"/><!-- type为类型 支持 param(默认不填时为param 标准的http请求参数)、header(@RequestHeader)、body(@RequestBody)、path(@PathVariable). -->
        </validator-mapping>
    </validator-mappings>
    
    <global-validator><!-- 全局验证器, 本Controller下的任何请求方法全部验证 一般不常用 -->
        <validator ref="userid"/><!-- 描述：全局验证的控件  @ref 引入validator-field中@name 就包含了此控件 @refgid 引入其它组（等于包含作用） -->
         <validator ref="platform" type="header"/><!-- type为类型 支持 param(默认不填时为param 标准的http请求参数)、header(@RequestHeader)、body(@RequestBody)、path(@PathVariable). -->
    </global-validator>
</validator>
```

### 3. 后台校验配置说明

#### 3.1 ~~validate-global-variable.properties 全局验证规则变量~~(需求很少不容易理,解所以废弃了.)

用于定义全局验证规则变量的配置文件

```properties
nm32=notEmpty&maxSize:32
```

以上定义了全局的规则名字为nm32,规则为notEmpty不允许为空,maxSize:32最大长度不能大于32位字符.

可以随意组装任何系统内提供的或扩展的规则,通过&符号连接即可.

使用globalRules属性来引用全局验证规则变量

#### 3.2 ~~global-validator.xml 说明~~(需求很少不容易理,解所以废弃了.)

用于定义全局验证规则的配置文件,全局验证规则可以通过validator-fields-group来引用,也可以通过validator-mapping来引用.

```xml

示例：

```xml
<global-validator>
    <validator-fields>
        <validator-field name="username" explain="用户名"  globalRules="nm32"></validator-field>
        <validator-field name="email" explain="邮箱" rules="email" globalRules="nm32"></validator-field>
        <validator-field name="phone" explain="电话" rules="phone"></validator-field>
    </validator-fields>
</global-validator>
```

以上示例定义了 username,email,phone三个控件的验证规则.

username控件名的规则,使用了globalRules,nm32为validate-global-variable.properties定义的nm32.

email控件名的规则,使用了globalRules,nm32为validate-global-variable.properties定义的nm32.

### 4. 后台自定义规则扩展

系统提供的验证规则不满足实际开发需求时,可以采用以下两种方式进行扩展.

#### 4.1 字符类验证规则扩展

继承StringRuleOperator类,实现validate与toString方法即可.

通过com.anywide.dawdler.clientplug.web.validator.operators.NumberRuleOperator 举例,NumberRuleOperator是系统内的number验证规则实现的类,用于验证一个字符串或数组中的值是否为整数.

示例:

```java
public class NumberRuleOperator extends StringRuleOperator {
 public static final String RULE_KEY = "number";//规则标识
 public static final String REGEX = "(^-\\d+$)|(^\\d+$)";//正则表达式
 public static final String EXPLAIN = "整数验证";//描述

 public NumberRuleOperator() {
  super(RULE_KEY, REGEX, EXPLAIN);
 }

 @Override
 public String validate(Object value) {
  return validate(value, "请输入整数!");
 }

 @Override
 public String toString() {
  return EXPLAIN;
 }
 
}
```

#### 4.2 正则类验证规则扩展

继承RegexRuleOperator类,实现validate与toString方法即可.

通过 com.anywide.dawdler.clientplug.web.validator.operators.MaxSizeRuleOperator 举例,MaxSizeRuleOperator是系统内提供验证字符个数不能大于指定范围的规则实现类,用法：maxSize:32,不能大于32个字符个数.

示例:

```java
public class MaxSizeRuleOperator extends RegexRuleOperator {
 public static final String RULE_KEY = "^maxSize:([1-9]{1}\\d*$)";//正则表达式

 public MaxSizeRuleOperator() {
  super(RULE_KEY);
 }
//以下是实现
 @Override
 public String validate(Object value, Matcher matcher) {
  boolean flag = true;
  int i = Integer.parseInt(matcher.group(1));
  String error = "不能大于" + i + "个字符!";
  if (value == null)
   return null;
  if (value instanceof String) {
   if (isEmpty(value.toString()))
    return null;
   if (((String) value).trim().length() > i)
    return error;
  }
  if (value instanceof String[]) {
   String[] values = (String[]) value;
   for (String v : values) {
    if (isEmpty(v)) {
     continue;
    }
    if (v.trim().length() > i) {
     flag = false;
     break;
    }
   }
  } else if (value instanceof List) {
   List values = (List) value;
   for (Object o : values) {
    if (isEmpty(o.toString())) {
     continue;
    }
    if (o.toString().trim().length() > i) {
     flag = false;
     break;
    }
   }
  }
  if (!flag)
   return error;
  return null;
 }

 @Override
 public String toString() {
  return "字符串或数组中的字符串的长度不能大于指定长度,如：maxSize:32!";
 }
}

```

### 5. 注入验证器

RuleOperatorProvider中提供registerRuleOperator与registerRuleOperatorScanPackage方法来添加扫描验证器.

registerRuleOperator接收参数为RuleOperator实体对象,只添加这个对象的实现.

registerRuleOperatorScanPackage接收参数为Class对象,添加这个Class所在包下所有RuleOperator的实现类.

#### 5.1 监听器中添加扫描验证器

关于监听器参考 [WebContextListener](../dawdler-client-plug-web/README.md#6-webcontextlistener-监听器)

```java
package com.anywide.yyg.user.web.listener;

import jakarta.servlet.ServletContext;

import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.clientplug.web.validator.RuleOperatorProvider;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.yyg.user.validator.operators.RegPasswordRuleOperator;

@Order(1)
public class UserWebContextListener2 implements WebContextListener{

 @Override
 public void contextInitialized(ServletContext servletContext) {
  
  RuleOperatorProvider.registerRuleOperatorScanPackage(RegPasswordRuleOperator.class);//添加扫描验证器,扫描并添加所有同包类的规则
  /* RegPasswordRuleOperator uo = new RegPasswordRuleOperator();
   RuleOperatorProvider.registerRuleOperator(uo);*/ //只加载指定的验证器
   RuleOperatorProvider.help();//生成文档给开发人员使用
 }

 @Override
 public void contextDestroyed(ServletContext servletContext) {
 }

}

```

#### 5.2 通过扫描组件包添加扫描验证器

通过配置扫描组件包的验证器会被注入,参考 [扫描组件包配置](../dawdler-client-plug-web/README.md#10-扫描组件包配置).

### 6. 前端js验证框架

dawdler-validator.js 是一套兼容后台验证表达式的前端框架.

#### 6.1 使用方式

引入dawdler-validator.js,内部提供了一个实例sir_validator(sir是为了纪念linuxsir,linuxsir当年就采用这个这个变量名).

可以重新声明一个对象

```javascript
<script language="javascript">
var validator = new Validator();
</script>
```

一般有这种重新声明一个对象的需求是因为同一个页面多个表单需要校验.

```javascript
<script language="javascript" src="dawdler-validator.js"></script>
```

#### 6.2 添加验证规则

通过调用addRule方法进行添加验证规则,addRule支持传入对象,同时也支持传入对象数组.

addRule支持传入对象的属性有以下五个：

id： 控件id或name,如果是复选框或单选框则为name属性,一般控件为id.

viewName： 控件的描述,例如：用户名.

validateRule： 验证规则,可以用多个&组合到一起.

alertFunction： 提示方法或提示组件的id. [参考alertFunction的例子](#622-alertfunction的示例)

buildFunction： 绑定触发验证事件.[参考buildFunction的例子](#623-buildfunction的示例)

##### 6.2.1 添加验证规则添加规则的示例

示例1：

```javascript
//添加单个控件的验证规则
 sir_validator.addRule({
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty"
    });
```

示例2：

```javascript
//添加多个控件的验证规则,数组方式
sir_validator.addRule([{
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty&maxSize:16"
    },{
        "id": "password",
        "viewName": "密码",
        "validateRule": "notEmpty&maxSize:16"
    }]);
```

------------------------------------------------------

##### 6.2.2 alertFunction的示例

传入提示方法的示例：

```javascript
//定义一个方法
 function password_alert_function(obj, error) {
    //其中obj为控件对象,error为错误信息.
    alert("password_alert_function:" + obj + ":" + error);
 } 

 sir_validator.addRule({
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty",
        "alertFunction": password_alert_function //直接传入方法即可,会调用上面定义的password_alert_function
 });

```

------------------------------------------------------

传入提示组件id示例：

```html
<!--定义一个html的控件-->
<span id="password_alert_span"></span>
```

```javascript
 sir_validator.addRule({
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty",
        "alertFunction": "password_alert_span" //直接传入上面定义的控件id,通过innerHTML方式将验证结果显示在上面定义的控件上.
 });

```

##### 6.2.3 buildFunction的示例

绑定了click事件与onblur事件.

```javascript
 sir_validator.addRule({
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty",
        "buildFunction": "click,onblur"
 });
```

#### 6.3 追加验证规则

通过调用appendRule方法进行追加验证规则,appendRule第一个参数为控件id,第二个参数为规则表达式,追加规则是在原有规则之上追加.

示例：

```javascript
//给username控件添加一个最小长度不能小于3个字符的验证规则.
sir_validator.appendRule("username","minSize:3");
```

#### 6.4 移除指定控件与验证规则

通过调用removeRule方法移除指定控件与验证规则,removeRule第一个参数为控件id,第二个参数为规则表达式,如果控件中有第二个参数传入的规则会被移除.

示例：

```javascript
//移除username控件最小长度不能小于3个字符的验证规则.
sir_validator.removeRule("username","minSize:3");
```

#### 6.5 移除指定控件的所有验证规则

通过调用removeAllRule方法移除指定控件的所有验证规则,removeAllRule参数为控件id.

示例：

```javascript
//移除username控件下所有的验证规则.
sir_validator.removeAllRule("username");
```

#### 6.6 设置指定控件的验证规则

通过调用setRule方法指定控件的验证规则,setRule第一个参数为控件id,第二个参数为规则表达式,如果指定的控件之前存在规则会被覆盖.

示例：

```javascript
//设置username控件的验证规则,不能为空.
sir_validator.setRule("username","notEmpty");
```

#### 6.7 添加不跳过空校验的表达式

通过调用addNoSkip来添加不跳过空验证的表达式,系统自带三个notEmpty、maxSelect、minSelect.这三个表达式都会在控件值为空的情况下执行的,因为当件值为空时默认情况下验证框架不会执行验证的表达式.

示例：

```javascript
//添加email表达式不跳过空校验,等同于email&notEmpty
addNoSkip("email");
```

#### 6.8 验证方法

通过调用validateAll方法进行验证,该方法返回boolean类型,如果返回true为验证通过,如果为false会触发提醒事件(如果配置了alertFunction或全局的事件或全局展现控件,则触发对应的配置,否则会默认调用alert进行提醒).

示例：

```javascript
//一般会用于ajax提交表单调用此方法

if(!sir_validator.validateAll())return;//验证不通过,直接return;

//验证通过,ajax调用后台
```

#### 6.9 绑定form的提交事件

通过调用buildFormValidate方法绑定form提交事件,buildFormValidate的参数为form的id.

示例：

```html
<!--定义一个form id为myform-->
<form id="myform" action="create.do" method="POST">
        <input type="text" name="username" value="" /><br/>
        <input type="text" name="password" /> <br/>
        <input type="submit" id="submit_onw" value="提交" /><br/>
</form>
```

```javascript
//  添加验证规则
sir_validator.addRule([{
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty&maxSize:16"
    },{
        "id": "password",
        "viewName": "密码",
        "validateRule": "notEmpty&maxSize:16"
}]);
// 绑定验证表单 id= myform
sir_validator.buildFormValidate("myform");
```

#### 6.10 加载通过html属性配置的验证规则(不常用)

通过调用buildFormValidateAutoRule加载通过dom属性配置的验证规则,第一个参数方法绑定form提交事件,buildFormValidate的参数为form的id,第二个参数为boolean类型,是否绑定form提交事件.

以下示例中在html的控件中定义了验证框架的相关属性,其属性与通过对象方式传入的方式完全相同.
一般不推荐这种方式,除非采用了动态表单(dawdler-client-plug-velocity 模块中实现的)会自动生成相关属性.

示例：

```html
<!--定义一个form id为myform, 并定义验证属性-->
<form id="myform" action="create.do" method="POST">
        <input type="text" name="username" viewName="用户名" validateRule="notEmpty&maxSize:16" value="" /><br/>
        <input type="text" name="password" viewName="密码"  validateRule="notEmpty&maxSize:16"/> <br/>
        <input type="submit" id="submit_onw" value="提交" /><br/>
</form>
```

```javascript
// 加载dom属性的验证配置,第二个参数true,绑定onsubmit事件.
sir_validator.buildFormValidateAutoRule("myform", true);
```

#### 6.11 全局提醒方法

定义一个方法或一个全局变量,来实现全局提醒方法,方法名或变量名为globalAlertFunction,方法的优先级优于变量的(如果验证控件规则中定义了alertFunction,则优先调用alertFunction).

如果定义方法会调用此方法,第一个参数为控件对象,第二个为错误信息.

定义方法的示例：

```javascript
//,可以通过这种方式重新定义提醒控件的样式
function global_validate_error_function(obj, error) {
        alert("validate_error:" + obj + ":" + error);
}
```

如果定义变量,则会将错误信息或通过验证信息通过innerHTML方式赋值到控件上,控件id为验证控件id+global_validate_error_function.下面的示例中出现错误会innerHTML到user_error和password_error的控件上.

定义变量的示例：

```html
<form id="myform" action="create.do" method="POST">
        <input type="text" name="username" viewName="用户名"/><span id="username_error"></span><br/>
        <input type="text" name="password" viewName="密码"/><span id="password_error"></span> <br/>
        <input type="submit" id="submit_onw" value="提交" /><br/>
</form>

```

```javascript
var global_validate_error_function = "_error";
sir_validator.addRule([{
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty&maxSize:16"
    },{
        "id": "password",
        "viewName": "密码",
        "validateRule": "notEmpty&maxSize:16"
}]);
// 绑定验证表单 id= myform
sir_validator.buildFormValidate("myform");

```

#### 6.12 前端验证框架扩展

前端支持两种类型的扩展方式,用于支持后台的扩展后的表达式.

##### 6.12.1 前端字符类验证规则扩展

定义一个方法,方法名为表达式,方法参数为控件值,返回值如果是true代表验证通过,返回字符串会触发提醒.

以下扩展一个验证表达式为hello,只能传入hello,否则会报错提醒,表达式可以单用也可以用&连接.

```javascript
function hello(text){
    if(text=='hello')return true;
    return "必须输入hello !";
}

sir_validator.addRule({
        "id": "username",
        "viewName": "用户名",
        "validateRule": "notEmpty&hello"//不允许为空并且必须为hello
 });

```

##### 6.12.2 前端正则类验证规则扩展

正则类验证规则与字符类不同,有这种需求的参考maxSize,minSelect系列的实现.

实现方式：

通过调用addRegExp方法来添加一个正则类验证规则,规则的组成由(表达式名:正则)组成.定义一个方法,方法名为表达式名.参数1为控件值,参数二为正则匹配到的数组.

示例：

以下是一个范围验证表达式的实现

```javascript

//添加表达式
sir_validator.addRegExp(/range:([0-9]+)-([0-9]+)/);
  
//定义验证方法名为:前的字符串,args 数组第一位为正则匹配到的第一个group,第二位为第二个group,支持N个.
function range(value,args){
    if(value<args[1])return "必须大于"+args[1];
    if(value>args[2])return "必须小于"+args[2];
}

sir_validator.addRule({
        "id": "age",
        "viewName": "年龄",
        "validateRule": "notEmpty&range:18-108"//不允许为空并且范围在18到108之间
 });
```
