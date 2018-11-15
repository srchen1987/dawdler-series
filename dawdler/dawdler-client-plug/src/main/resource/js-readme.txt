验证插件使用方法：

以下是目前自带的规则
 * 空值 notempty 手机号码 cellPhone email email 电话 phone 整数 number 英文字母 englishWords 汉字 chineseWords 网址 webSite 实数
 * realNumber 自然数 natureNumber 日期 date 汉字、字母或数字 regularCharacter 小写英文字母
 * lowercaseLetters 大写英文字母 capitalLetters 负整数 negativeNumber 正整数 positiveNumber
 * 身份证 IDCard chineseWords只能是汉字 englishWords只能是英文字母
 * 
 * 以下是包括数值的表达式,包括数值的必须在:号后追加你想加入的数字 如 maxsize:25 字符串长度不能超过25 长度 大 maxsize 字符串的长度
 * 长度 小 minsize 选择数量 大 maxselect 只针对下拉列表框 复选框 单选框有作用 选择数量 小 minselect 数字值 大
 * maxnumber 数字值 小 minnumber


1、第一步：页面中引入此js
2、通过 对象sir_validate调用方法，两种方法如下：
 2.1 sir_validate.addRule(验证对象);或sir_validate.addRule(验证对象数组)
 2.2 sir_validate.addRuleString();单独添加，这个不推荐使用了,如果使用可以参考源码对应位置 
         验证对象属性说明：
          id（控件的id）,必须有,如果控件没id则支持name查找
          viewname（提示错误前缀）,必须有
          validaterule（验证规则）,必须有
          buildFunction（绑定事件，支持多事件用,隔开，于原有事件重叠，不冲突）,
          alertFunction,提示错误的方法或提示错误的div的id，如果传入的是方法则调用方法，如果是div的id则将错误信息输出到div上，如果不传入默认调用alert
          //showstatus，显示成功状态的id 已废弃 现在已自动带入次状态
 2.3 sir_validate.globalAlertFunction 全局提示错误的方法 如sir_validate.globalAlertFunction=function(obj){alert(obj);}
 2.4 sir_validate.globalErrorDivPrefix 全局提示错误的层后缀，其格式为id（控件的id）+后缀 如sir_validate.globalErrorDivPrefix="_error";如有id="username"控件，提示错误div的id就为"username_error";
3、验证插件的扩展方法：
 3.1 普通方法直接写名字即可，如创建方法 
function test(text){
	if(text=='你好')return true;
	return "必须输入 你好 !";
}
方法如果返回true则成功，如果失败返回提示字符串！

 3.2 高级方法
  	sir_validate.addRegExp(验证表达式);

	将其扩展：如加入
sir_validate.addRegExp(/fanwei:([0-9]+)-([0-9]+)/);
 
  看到了定义规则是fanwei:([0-9]+)-([0-9]+)
  然后编写方法 方法名为:前的字符串，如下
function fanwei(value,args){
	if(value<args[1])return "必须大于"+args[1];
	if(value>args[2])return "必须小于"+args[2];
//
//value是控件的值，args是数组，分别对应位数，如第一个组下的值就为args[1]
  // 做一些验证处理后 同普通方法一样返回true则成功，字符串则失败

	return true;
}
引用此表达式如 fanwei:1-5 验证数必须大于1，必须小于5

4、绑定form验证通过方法 buildFormValidate来绑定 如 sir_validate.buildFormValidate(formid)，这样提交表单前就会验证。
5、验证已注入的验证规则，通过调用sir_validate.validateAll();方法来验证，返回false则为失败，true为成功.
6、动态添加/移除验证规则
 6.1 动态添加规则  sir_validate.appendRule("控件id或名字","规则");
 6.2 动态移除规则  sir_validate.removeRule("控件id或名字","规则");
7、为了支持动态表单所有支持了html元素里加入自定义属性 如 viewname、validaterule等等、通过调用buildFormValidateAutoRule方法来绑定验证 buildFormValidateAutoRule方法中第一个参数为form的id 如果传入第二个参数为true则绑定form提交事件
