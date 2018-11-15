/**
 * date : 2007-2-25 11:05 author colorless glasses QQ 121616325
 * 
 * 
 * notempty&englishWords&maxsize:25&minsize:3
 * 
 * 空值 notempty 手机号码 cellPhone email email 电话 phone 整数 number 英文字母 englishWords 汉字 chineseWords 网址 webSite 实数
 * realNumber 自然数 natureNumber 日期 date 汉字、字母或数字 regularCharacter 小写英文字母
 * lowercaseLetters 大写英文字母 capitalLetters 负整数 negativeNumber 正整数 positiveNumber
 * 身份证 IDCard chineseWords只能是汉字 englishWords只能是英文字母
 * 
 * 以下是包括数值的表达式,包括数值的必须在:号后追加你想加入的数字 如 maxsize:25 字符串长度不能超过25 长度 大 maxsize 字符串的长度
 * 长度 小 minsize 选择数量 大 maxselect 只针对下拉列表框 复选框 单选框有作用 选择数量 小 minselect 数字值 大
 * maxnumber 数字值 小 minnumber
 * 
 * 
 * 
 * 
 * 请注意 如果需要组合应用请用&连接起来,如 number&maxnumber:25&minnumber:3 表示含义为 必须是一个大于3并小于25的数
 */
var isNullNoSkip=new Array("notEmpty","maxselect","minselect");
var rulesdata = new Array([ 'notEmpty', 2 ], [ 'cellPhone', 4 ],
		[ 'email', 8 ], [ 'phone', 16 ], [ 'number', 32 ], [ 'length', 64 ], [
				'englishWords', 128 ], [ 'chineseWords', 256 ], [
				'webSite', 512 ], [ 'realNumber', 1024 ], [ 'natureNumber',
				2048 ], [ 'date', 4096 ], [ 'regularCharacter', 8192 ], [
				'lowercaseLetters', 16384 ], [ 'capitalLetters', 32768 ], [
				'IDCard', 65536 ]);
var debug = false;
var success_prefix="";
var success_suffix="";
var error_prefix="";
var error_suffix="";
function print(text){
	if(debug)alert(text);
}
var regs = new Array(/maxsize:([0-9]+)/, /minsize:([0-9]+)/,
		/maxnumber:([0-9]+)/, /minnumber:([0-9]+)/, /maxselect:([0-9]+)/,
		/minselect:([0-9]+)/);

// 空值
function notEmpty(none) {
	if (none == "" || none.trim() == "") {
		return "不能为空!";
	}
	return true;
}
/**
function ni(ni){
	if(ni!='你')return "必须输入 你 ";
		return true;

}
**/
// 手机号码
function cellPhone(cellPhone) {
	//var reg = /(^[1][3,5,8][0-9]{9}$)|(^0[1][3,5][0-9]{9}$)/;
	var reg = /(^(16[0-9]|17[0-9]|13[0-9]|15[0-9]|18[0-9]|14[7,5])\d{8}$)|(^0(13[0-9]|15[0-9]|18[0-9]|14[7,5])\d{8}$)/;
	return cellPhone.match(reg) ? true : "请输入合法的手机号码！";
}
// email
function email(email) {
	//var reg = /^([-_A-Za-z0-9\.]+)@([_A-Za-z0-9\-]+\.)[A-Za-z0-9]{2,3}$/;
	var reg = /^(\w)+(\.\w+)*@(\w)+((\.\w{2,4}){1,3})$/;
	return email.match(reg) ? true : "请输入合法E-Mail地址！";
}
// 固定电话
function phone(phone) {
	var reg = /(^([0][1-9]{2,3}[-])?\d{3,8}(-\d{1,6})?$)|(^\([0][1-9]{2,3}\)\d{3,8}(\(\d{1,6}\))?$)|(^\d{3,8}$)/;
	return phone.match(reg) ? true : "请输入合法的电话号码！";
}
// 数字
function number(number) {
	var reg = /(^\d+$)|(^-\d+$)/;
	return number.match(reg) ? true : "请输入数字！";
}
// 长度大小
function maxsize(value, size) {
	return value.trim().length > size[1] ? ("不能大于" + size[1] + "个字符!") : true;
}
function minsize(value, size) {
	return value.trim().length < size[1] ? ("不能小于" + size[1] + "个字符!") : true;
}
function minselect(selectsize,size) {
	return selectsize < size[1] ? ("至少要选择或选中" + size[1] + "项!") : true;
}
function maxselect(selectsize, size) {
	return selectsize > size[1] ? ("最多只能选择或选中" + size[1] + "项!") : true;
}
function maxnumber(value, size) {
	return value > Number(size[1]) ? ("数值不能大于" + size[1]) : true;
}
function minnumber(value, size) {
	return value <  Number(size[1]) ? ("数值不能小于" + size[1]) : true;
}
function englishWords(englishWords) {
	var reg = /^[A-Z|a-z]+$/;
	return englishWords.match(reg) ? true : "请输入合法的英文字母！";
}
function chineseWords(chineseWords) {
	var reg = /^[\u4e00-\u9fa5]+$/;
	return chineseWords.match(reg) ? true : "请输入汉字！";
}
function webSite(webSite) {
	var reg = /^(http(s)?:\/\/)?([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/;
	return webSite.match(reg) ? true : "请输入合法网址！";
}
function realNumber(realNumber) {
	var reg = /^[-+]?\d+(\.\d+)?$/;
	return realNumber.match(reg) ? true : "请输入实数！";
}
function natureNumber(natureNumber) {
	var reg = /^[0-9]+$/;
	return natureNumber.match(reg) ? true : "请输入自然数！";
}

function date(date) {
	var reg = /^((?!0000)[0-9]{4}-((0[1-9]|1[0-2])-(0[1-9]|1[0-9]|2[0-8])|(0[13-9]|1[0-2])-(29|30)|(0[13578]|1[02])-31)|([0-9]{2}(0[48]|[2468][048]|[13579][26])|(0[48]|[2468][048]|[13579][26])00)-02-29)$/;
	return date.match(reg) ? true : "请正确输入日期";
}
function regularCharacter(regularCharacter) {
	var reg = /^[0-9a-zA-Z\u4e00-\u9fa5]+$/;
	return regularCharacter.match(reg) ? true : "请正确输入汉字字母或数字";
}
function lowercaseLetters(lowercaseLetters) {
	var reg = /^[a-z]+$/;
	return lowercaseLetters.match(reg) ? true : "请正确输入小写字母";
}
function capitalLetters(capitalLetters) {
	var reg = /^[a-z]+$/;
	return capitalLetters.match(reg) ? true : "请正确输入大写字母";
}

// 负整数
function negativeNumber(negativeNumber) {
	var reg = /^-[1-9]{1}\d*$/;
	return negativeNumber.match(reg) ? true : "请正确输入负整数";
}

// 正整数
function positiveNumber(positiveNumber) {
	var reg = /(^[1-9]{1}\d*$)/;
	return positiveNumber.match(reg) ? true : "请正确输入正整数！";
}
function IDCard(IDCard) {
	num = IDCard.toUpperCase();
	// 身份证号码为15位或者18位，15位时全为数字，18位前17位为数字，最后一位是校验位，可能为数字或字符X。
	if (!(/(^\d{15}$)|(^\d{17}([0-9]|X)$)/.test(num))) {
		return '输入的身份证号长度不对，或者号码不符合规定！\n15位号码应全为数字，18位号码末位可以为数字或X。';
		return false;
	}
	// 校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
	// 下面分别分析出生日期和校验位
	var len, re;
	len = num.length;
	if (len == 15) {
		re = new RegExp(/^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/);
		var arrSplit = num.match(re);
		// 检查生日日期是否正确
		var dtmBirth = new Date('19' + arrSplit[2] + '/' + arrSplit[3] + '/'
				+ arrSplit[4]);
		var bGoodDay;
		bGoodDay = (dtmBirth.getYear() == Number(arrSplit[2]))
				&& ((dtmBirth.getMonth() + 1) == Number(arrSplit[3]))
				&& (dtmBirth.getDate() == Number(arrSplit[4]));
		if (!bGoodDay) {
			return '输入的身份证号里出生日期不对！';
		} else {
			// 将15位身份证转成18位
			// 校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
			var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
					8, 4, 2);
			var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4',
					'3', '2');
			var nTemp = 0, i;
			num = num.substr(0, 6) + '19' + num.substr(6, num.length - 6);
			for (i = 0; i < 17; i++) {
				nTemp += num.substr(i, 1) * arrInt[i];
			}
			num += arrCh[nTemp % 11];
			return num;
		}
	}
	if (len == 18) {
		re = new RegExp(/^(\d{6})(\d{4})(\d{2})(\d{2})(\d{3})([0-9]|X)$/);
		var arrSplit = num.match(re);
		// 检查生日日期是否正确
		var dtmBirth = new Date(arrSplit[2] + "/" + arrSplit[3] + "/"
				+ arrSplit[4]);
		var bGoodDay;
		bGoodDay = (dtmBirth.getFullYear() == Number(arrSplit[2]))
				&& ((dtmBirth.getMonth() + 1) == Number(arrSplit[3]))
				&& (dtmBirth.getDate() == Number(arrSplit[4]));
		if (!bGoodDay) {
			return '输入的身份证号里出生日期不对！';
		} else {
			// 检验18位身份证的校验码是否正确。
			// 校验位按照ISO 7064:1983.MOD 11-2的规定生成，X可以认为是数字10。
			var valnum;
			var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
					8, 4, 2);
			var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4',
					'3', '2');
			var nTemp = 0, i;
			for (i = 0; i < 17; i++) {
				nTemp += num.substr(i, 1) * arrInt[i];
			}
			valnum = arrCh[nTemp % 11];
			if (valnum != num.substr(17, 1)) {
				return '18位身份证的校验码不正确！应该为：' + valnum;
				return false;
			}
			return true;
		}
	}
	return false;
}
String.prototype.trim = function() {
	return this.replace(/(^\s*)|(\s*$)/g, "");
};

function contains(a, e) {
	var j=0;
	for (j = 0; j < a.length; j++) {
		if (getParamType(a[j]) == 'array') {
			for ( var rg in regs) {
				var reg = regs[rg];
				if (e.match(reg)) {
					//var de = RegExp.$+;
					//var vs =reg.exec(e);
					var str = e.substr(0,e.indexOf(":"));
					if (a[j][0] == str)
						return true;
				}
			}

		} else {
			if (a[j] == e)
				return true;
		}
	}
	return false;
}
function uniqueArr(a) {
	var temp = new Array();
	for (var i = 0; i < a.length; i++) {
		if(a[i]!='')
		if (!contains(temp, a[i])) {
			var rmk = false;
			for ( var rg in regs) {
				var reg = regs[rg];
				var fs = a[i].match(reg);
				if (fs) {
					rmk = true;
					var vs =reg.exec(a[i]);
					//var de = RegExp.$+;
					var str = a[i].substr(0,a[i].indexOf(":"));
					temp[temp.length] = new Array(str, vs);
					break;
				}
			}
			if (!rmk)
				temp[temp.length] = a[i];

		}
	}
	return temp;
}
function getParamType(param) {
	return ((_t = typeof (param)) == "object" ? Object.prototype.toString.call(
			param).slice(8, -1) : _t).toLowerCase();
}
function executeFunction(fname,value){
	var execute=false;
	for(var i = 0;i<isNullNoSkip.length;i++){
		if(isNullNoSkip[i]==fname){
			execute=true;
			break;
		}
	}
	if(execute||(value!=null&&value!='')){
		if(typeof arguments[2]!='undefined'){
			return eval(fname + "('" + value + "',arguments[2]);");
		}else{
			return eval(fname + "('" + value + "');");
		}
			
	}
	return true;
}
function parse(parserule, obj) {
	if (parserule == null||parserule=='')
		return true;
	parserule = uniqueArr(parserule.split("&"));
	var type = arguments[2];
	if (typeof type == 'boolean') {
		try {
			if(!type){
			var objs = document.getElementsByName(obj.name);
			var error = null;
			for (var ri = 0; ri < objs.length; ri++) {
					value = objs[ri].value;
					for ( var rle in parserule) {
						var rlre = parserule[rle];
						if (getParamType(rlre) == 'array') {
							if ((rlre[0] != 'maxselect' && rlre[0] != 'minselect')) {
								error =	executeFunction(rlre[0],value,rlre[1]);
								if (typeof error != 'boolean')
									return new Array(objs[ri],error.toString());
							}
						} else {
								error =	executeFunction(rlre,value);
							if (typeof error != 'boolean')
								return new Array(objs[ri],error.toString());
						}
					}
			}
			}else{
				var value = obj.value.trim();
				for ( var rle in parserule) {
					var rlre = parserule[rle];
					var error = null;
					if (getParamType(rlre) == 'array') {
						if ((rlre[0] != 'maxselect' && rlre[0] != 'minselect')) {
							error =	executeFunction(rlre[0],value,rlre[1]);
						}
					} else {
						error =	executeFunction(rlre,value);
					}
					if (typeof error != 'boolean') {
						return new Array(obj,error.toString());
					}
				}
			}
		
		} catch (e) {
			if (debug)
				return new Array(obj,"debug\t"+e.toString());
		}
	} else {
		if (type == 'radio') {
			var objs = document.getElementsByName(obj.name);
			var objvar = null;
			var value = null;
			var size = 0;
			for ( var ri = 0; ri < objs.length; ri++) {
				if (objs[ri].checked) {
					value = objs[ri].value;
					var objvar=objs[ri];
					size++;
					break;
				}
			}
			for ( var rle in parserule) {
				var rlre = parserule[rle];
				var error = null;
				if (getParamType(rlre) == 'array') {
					if (rlre[0] == 'maxselect' || rlre[0] == 'minselect')
						error =	executeFunction(rlre[0],size,rlre[1]);
					else
						error =	executeFunction(rlre[0],value,rlre[1]);
				} else {
					error =	executeFunction(rlre,value);
				}
				if (typeof error != 'boolean') {
					return new Array(objvar==null?objs[0]:objvar,error.toString());
				}
			}
		} else if (type == 'checkbox') {
			var objs = document.getElementsByName(obj.name);
			var size = 0;
			var error = null;
			var cobjvar = null;
			for (var ri = 0; ri < objs.length; ri++) {
				if (objs[ri].checked) {
					value = objs[ri].value;
					cobjvar=objs[ri];
					size++;
					for ( var rle in parserule) {
						var rlre = parserule[rle];
						if (getParamType(rlre) == 'array') {
							if ((rlre[0] != 'maxselect' && rlre[0] != 'minselect')) {
								error =	executeFunction(rlre[0],value,rlre[1]);
								if (typeof error != 'boolean')
									return new Array(objs[ri],error.toString());
							}
						} else {
								error =	executeFunction(rlre,value);
							if (typeof error != 'boolean')
								return new Array(objs[ri],error.toString());
						}
					}
				}
			}
			for ( var rle in parserule) {
				var rlre = parserule[rle];
				if ((rlre[0] == 'maxselect' || rlre[0] == 'minselect')) {
					error =	executeFunction(rlre[0],size,rlre[1]);
					if (typeof error != 'boolean')
						return new Array(cobjvar==null?objs[0]:cobjvar,error.toString());
				}
			}
		} else if (type == 'SELECT') {
			var size = 0;
			for ( var i = 0; i < obj.options.length; i++) {
				if (obj.options[i].selected) {
					var value = obj.options[i].value;
					size++;
					for ( var rle in parserule) {
						var rlre = parserule[rle];
						if (getParamType(rlre) == 'array') {
							if ((rlre[0] != 'maxselect' && rlre[0] != 'minselect')) {
									error =	executeFunction(rlre[0],value,rlre[1]);
								if (typeof error != 'boolean')
									return error.toString();
							}
						} else {
							error =	executeFunction(rlre,value);
							if (typeof error != 'boolean')
								return new Array(obj,error.toString());
						}
					}
				}
			}
			for ( var rle in parserule) {
				var rlre = parserule[rle];
				if ((rlre[0] == 'maxselect' || rlre[0] == 'minselect')) {
					error =	executeFunction(rlre[0],size,rlre[1]);
					if (typeof error != 'boolean')
						return new Array(obj,error.toString());
				}
			}
		}
	}
	return true;
}

function invoke(obj, type, validaterule, viewname, tagname,fromid) {
	if (type == 'text' || tagname == 'TEXTAREA' || type == 'password'
			|| type == 'hidden') {
		return parse(validaterule,obj,fromid);
	} else if (type == 'radio' || type == 'checkbox') {
		return parse(validaterule,obj,type);
	} else if (tagname == "SELECT") {
		return parse(validaterule,obj,tagname);
	} else {
		return new Array(obj,"can't define " + type);
	}
}
function validate(param) {
/**	var obj;
	if(param.id==""){
		obj=param;
		param.viewname=param.getAttribute("viewname");
		param.validaterule=param.getAttribute("validaterule");
	}else{**/
		var obj = document.getElementById(param.id);
		var fromid=true;
		if(typeof obj=='undefined'||obj==null){
			fromid=false;
			obj = document.getElementsByName(param.id);
			if(typeof obj=='undefined'||obj==null||obj.length==0){
				print("can't find id or name : "+param.id);
				return true;
			}
			obj=obj[0];
		}
//	}
	
	var validaterule = param.validaterule;
	if (validaterule == null)
		return true;
	var tagname = obj.tagName;
	var type = obj.getAttribute("type");
	var viewname = param.viewname;
	if (viewname == null) {
		print("viewname can't null!");
		return false;
	}
	if (tagname != 'INPUT' && tagname != 'TEXTAREA' && tagname != 'SELECT'){
		print(viewname + "\t" + tagname + "不属于表单对象!");
		return true;
	}	
	var invokevar = invoke(obj, type, validaterule, viewname, tagname,fromid);
	var error = true;
	var invokeobj;
	if(typeof invokevar != 'boolean'){
		error=invokevar[1];
		invokeobj=invokevar[0];
	}
	//alertFunction only support user-defined  object not support html object 
	if (typeof error != 'undefined' && typeof error != 'boolean'){
		//invokeobj.style="border: 1px solid rgb(255, 0, 0);";
	//	invokeobj.focus();
		if(typeof param.alertFunction =='string'){
			var af = document.getElementById(param.alertFunction);
			if(typeof af=='undefined'||af==null){
				print("can't find id: "+param.alertFunction);
				return false;
			}
			af.innerHTML = error_prefix+viewname + error+error_suffix;
			return false;
		}
		else if(typeof param.alertFunction =='function') {
			param.alertFunction(invokeobj,viewname + error);
			return false;
		}
		else {
			if(typeof sir_validate.globalAlertFunction =='function'){
				sir_validate.globalAlertFunction(invokeobj,viewname + error);
				return false;
			}else if(typeof sir_validate.globalErrorDivPrefix =='string'){
				var af = document.getElementById(param.id+sir_validate.globalErrorDivPrefix);
				if(typeof af=='undefined'||af==null){
					print("can't find id: "+param.id+sir_validate.globalErrorDivPrefix);
					return false;
				}
				af.innerHTML = error_prefix+viewname + error+error_suffix;
				return false;
			}else{
				alert(viewname + error);
				return false;
			}
		}
	} else {
		if (typeof param.alertFunction == 'string'){
			var af = document.getElementById(param.alertFunction);
				if(typeof af=='undefined'||af==null){
				print("can't find id: "+param.alertFunction);
				return false;
			}
			af.innerHTML = success_prefix+viewname	+ "通过验证"+success_suffix;
		}else if(typeof sir_validate.globalErrorDivPrefix =='string'){
				var af = document.getElementById(param.id+sir_validate.globalErrorDivPrefix);
				if(typeof af=='undefined'||af==null){
					print("can't find id: "+param.id+sir_validate.globalErrorDivPrefix);
					return false;
				}
				af.innerHTML = success_prefix+viewname + "通过验证!"+success_suffix;
		}
		return true;
	}
}
function buildOn(funobj,funname,funvalue){
		try{
			funobj.addEventListener(funname,function(){return validate(funvalue);},true);
		}catch(e){
			funobj.attachEvent("on"+funname,function(){return validate(funvalue);});
		}
}
function getValidateObjById(id){
	var list = sir_validate.list;
	for(var i=0;i<list.length;i++){
		var obj = list[i];
		if(obj.id==id)return obj;
	}
	return null;
}
var sir_validate={
	//globalAlertFunction:""|functionName,
	//globalErrorDivPrefix:"_error",
	version:"beta0.1",
	list:new Array(),
	addRegExp:function(exp){
		regs.push(exp);
	},
	addNoSkip:function(fname){
		isNullNoSkip.push(fname);
	},
	appendRule:function(id,rule){
		var obj = getValidateObjById(id);
		if(obj!=null){
			var ary = null;
			if(obj.validaterule==null) ary=new Array();
			else ary = uniqueArr(obj.validaterule.split("&"));
			for(var i = 0;i<ary.length;i++){
					if (getParamType(ary[i]) == 'array'){
						if(ary[i][1][0]==rule)return;
					}else{
						if(ary[i]==rule)return;			
					}
			}
			if(ary.length>0)obj.validaterule=obj.validaterule+"&"+rule;
			else obj.validaterule=rule;
		}else
			print("can't find rule id:"+id+"!");
	},
	removeRule:function(id,rule){
		var obj = getValidateObjById(id);
		if(obj!=null){
				var ary = null;
				if(obj.validaterule==null) ary=new Array();
				else ary = uniqueArr(obj.validaterule.split("&"));
				for(var i = 0;i<ary.length;i++){
					var rem=false;
					if (getParamType(ary[i]) == 'array')
						rem=ary[i][1][0]==rule;
					else
						rem=ary[i]==rule;
					if(rem){
						ary.splice(i,1);
						break;
					}			
				}
				if(ary.length>0){
					var strbuf = "";
					for(var i = 0;i<ary.length;i++){
						strbuf+=ary[i];
						if(i<ary.length-1)
							strbuf+="&";
					}
					obj.validaterule=strbuf;
				}else{
					obj.validaterule=null;
				}
		}
	},	
	removeAllRule:function(id){
		var obj = getValidateObjById(id);
		if(obj!=null){
			obj.validaterule=null;
		}
	},
	setRule:function(id,rule){
		var obj = getValidateObjById(id);
		if(obj!=null){
			obj.validaterule=rule;
		}
	},
	setViewName:function(id,viewname){
		var obj = getValidateObjById(id);
		if(obj!=null){
			obj.viewname=viewname;
		}
	},
	datas:{
	push:function(edata){
	var isht = goodNodeHTML(edata);
	if(isht){
		edata.id=edata.name;
		edata.viewname=edata.getAttribute("viewname");
		edata.validaterule=edata.getAttribute("validaterule");
		edata.buildFunction=edata.getAttribute("buildFunction");
		if(edata.getAttribute("alertFunction")!=null){
			try{
			if(typeof eval(edata.getAttribute("alertFunction"))=='function'){
				edata.alertFunction=eval(edata.getAttribute("alertFunction"));
			}
			}catch(e){
				edata.alertFunction=edata.getAttribute("alertFunction");
			}
		}
	}
	if(edata.viewname)edata.viewname=removeHTMLTag(edata.viewname);
	if(edata.id==null||edata.id==""){
				print("id can't null!");
				return;
	}
	for(var i=0;i<sir_validate.list.length;i++){
		if(edata.id==sir_validate.list[i].id)return;
	}
	if(edata!=null&&edata.buildFunction!=null){
			var af = document.getElementById(edata.id);
			var bfs = edata.buildFunction.split(",");
			if(typeof af=='undefined'||af==null){
					var objs = document.getElementsByName(edata.id);
					if(typeof objs=='undefined'||objs==null||objs.length==0){
						print("can't find id: "+edata.id);
						return;
					}else{
						for (var ri = 0; ri < objs.length; ri++) {
							for (var index in bfs) {
								buildOn(objs[ri],bfs[index],edata);
							}
						}
					}
			}else{
				if(af.type=='checkbox'||af.type=='radio'){
					var objs = document.getElementsByName(edata.id);
					if(typeof objs=='undefined'||objs==null||objs.length==0){
						 for (var index in bfs) {
							buildOn(af,bfs[index],edata);
						 }
					}else{
						for (var ri = 0; ri < objs.length; ri++) {
							for (var index in bfs)
								buildOn(objs[ri],bfs[index],edata);
						}
					}
				}else
				for (var index in bfs) {
						buildOn(af,bfs[index],edata);
				}
			}
	}
	sir_validate.list.push(edata);
	}
 }
};
(function ($) {
	$.carete_validate_rule=function(id,viewname,validaterule,buildFunction,alertFunction){
		return new this.validate_rule(id,viewname,validaterule,buildFunction,alertFunction);
	},
	$.validate_rule=function(id,viewname,validaterule,buildFunction,alertFunction){
		this.id=id;
		this.viewname=viewname;
		this.validaterule=validaterule;
		this.alertFunction=alertFunction;
		this.buildFunction=buildFunction;
	},
	$.addRuleString=function(id,viewname,validaterule,buildFunction,alertFunction){
		this.datas.push(this.carete_validate_rule(id,viewname,validaterule,buildFunction,alertFunction));
	},
	$.addRuleString=function(id,viewname,validaterule,alertFunction){
		this.datas.push(this.carete_validate_rule(id,viewname,validaterule,null,alertFunction));
	},
	$.addRule=function(validate_rule){
		if(getParamType(validate_rule)=='array'){
				for ( var rle in validate_rule) {
					this.datas.push(validate_rule[rle]);
				}
		}else if(getParamType(validate_rule)=='object'){
			this.datas.push(validate_rule);
		}else{
			print("unknown this addRule"+getParamType(validate_rule)+",rule must in array or object!");
		}
	},
	$.validateAll=function(){
			for (var index in sir_validate.list) {
				var param =  sir_validate.list[index];
				var rm = validate(param);
				if(!rm)return false;
			}
			return true;
	},$.buildFormValidateAutoRule=function(formId,build){
		var form = document.getElementById(formId);
		if(typeof form=='undefined'){
			print("can't build on "+formId);
			return;
		}
		var vData = new Array();
  		loadNeedElement(form,vData);
  		 sir_validate.addRule(vData);
  		if(build)
			form.onsubmit = $.validateAll;
	},
	$.buildFormValidate=function(formId){
		var form = document.getElementById(formId);
		if(typeof form=='undefined'){
			print("can't build on "+formId);
			return;
		}
		form.onsubmit = $.validateAll;
	};
})(sir_validate);
function loadNeedElement(ele,vData){
	var eles;
	if(ele.hasChildNodes()){
	  	eles = ele.childNodes;
	  for(var i=0;i<eles.length;i++)
	  {
	             loadNeedElement(eles[i],vData);
	  }
	}
	var tagname = ele.tagName;
	if (tagname != 'INPUT' && tagname != 'TEXTAREA' && tagname != 'SELECT') return;
	var type=ele.type;
	if(tagname=='INPUT' && type!='text' && type!='password' && type!='radio' && type!='checkbox' && type!='hidden' )return;
	vData.push(ele);
}

function goodNodeHTML(objh){
    var de = document.createElement("div");
    try{
        de.appendChild(objh.cloneNode(true));
        return objh.nodeType==1?true:false;
    }catch(e){
        return false;
    }
}

function removeHTMLTag(str) {
            str = str.replace(/<\/?[^>]*>/g,''); //去除HTML tag
            str = str.replace(/[ | ]*\n/g,'\n'); //去除行尾空白
            str = str.replace(/\n[\s| | ]*\r/g,'\n'); //去除多余空行
            str=str.replace(/&nbsp;/ig,'');//去掉&nbsp;
            return str;
}


