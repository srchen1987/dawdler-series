验证框架使用文档

首先参考 validatedemo项目
访问 validatedemo/test.html
可以看到一些效果


此框架分为前后台两部分
先说后台的 
1、查看validatedemo下的 validate_global_variable.properties 文件 这个为全局别名配置文件和指定全局配置文件
2、global-validator.xml 全局配置文件 它由validate_global_variable.properties里的global_path=${classpath}/global-validator.xml指定
3、最后一个是TestController-validator.xml文件 它以每个Controller的类名开头加-validator.xml结尾构成

TestController-validator.xml中的结构如下（约定 @开头的为属性 Text为控件的Text值，--为xml节点的层次）：
   --<validator> //根标记
	--<validator-fields> 描述：控件信息的组
		--<validator-field>（描述：配置控件信息） @name 控件名称 @explain 控件中文  @globalrules 放置的是全局别名配置文件中的key 如果填写了它则和本控件内的验证规则进行组合   Text 里面写入验证规则 如：<![CDATA[ positiveNumber&maxselect:3&minselect:1 ]]>
	--<validator-fields-groups>描述：控件信息组的组 这个组很好用 如果同一个Controller里 有3个方法都需要验证某些控件信息 则可以建立一个组分别refgid这个组id即可，组之间也可以refgid相当于包含
		--<validator-fields-group>描述：控件信息组 @id 组的标识id 供其他组或mapping验证器进行引用
			--<validator> 描述：组内控件  @ref 引入validator-field中@name 就包含了此控件 @refgid 引入其它组（等于包含作用）
	
	--<validator-mappings> 描述：验证器组
		--<validator-mapping>描述：验证器　　＠name　填写请求的URI一般可以写为spring的Mapping的配置 @skip为跳过某些验证如果为多个可以用‘,’号隔开，里面写入的为validator-field的@name
			--<validator> 描述：验证的控件  @ref 引入validator-field中@name 就包含了此控件 @refgid 引入其它组（等于包含作用）

	--<global-validator> 全局验证器只要是此Controller下的任何请求方法全部验证 一般不常用
			--<validator> 描述：全局验证的控件  @ref 引入validator-field中@name 就包含了此控件 @refgid 引入其它组（等于包含作用）



global-validator.xml文件下的结构如下：
  --<global-validator> 根标记
	--<validator-fields>验证控件信息组
		--<validator-field>同以上的作用一样
global-validator.xml这里面配置的验证信息是为了防止重复控件配置的出现，如果多个Controller中用到了相同的组件就可以将其组件配置到这里,在单独的Controller验证配置中无需声明直接引用即可，如果声明了则优先选择Controller独立验证配置的




validate_global_variable.properties 结构如下：
global_path是为了指定全局配置文件位置如： global_path=${classpath}/global-validator.xml
其余的配置如下：
max8=maxsize:8
rule2=notEmpty&maxselect:4&minselect:2
这里的max8只是一个别名，rule2也是别名如果其他验证控件里需要用到可以用@globalrules 来引用




自定义与扩展：
参考 org.src.validatedemo.validate.ext包下提供的源代码实现自己想要的验证即可

org.src.validatedemo.listener.SystemListener类下为加载自定义扩展的过程
@Override
	public void contextInitialized(ServletContext arg0) {
		System.out.println("contextInitialized ...");
		RuleOperatorProvider.registerRuleOperatorScanPackage(UUIDRuleOperator.class);//加载指定验证器与其同包下的验证器,扫描功能
	/*	UUIDRuleOperator uo = new UUIDRuleOperator();
		RuleOperatorProvider.registerRuleOperator(uo);*/ //只加载指定的验证器
		RuleOperatorProvider.help();//生成文档给开发人员使用
	}
 
	
	
	
前台的js验证框架的规则与后台的一模一样 请参考 js-readme.txt

TODO:以后会通过后台的自动生成前台的规则 省去配置的时间(已经实现)