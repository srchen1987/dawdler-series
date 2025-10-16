/**
 * date : 2007-2-25 11:05 author jackson.song
 */
const isNullNoSkipRules = ["notEmpty", "maxItems", "minItems"];
let debug = true;
let successPrefix = "";
let successSuffix = "";
let errorPrefix = "";
let errorSuffix = "";

function print(text) {
	if (debug) alert(text);
}

const validationRegExps = [
	/maxLength:([0-9]+)/, /minLength:([0-9]+)/,
	/maximum:([0-9.eE+-]+)/, /minimum:([0-9.eE+-]+)/, /maxItems:([0-9]+)/,
	/minItems:([0-9]+)/
];

function notEmpty(none) {
	if (none == null || none == "" || none.trim() == "") {
		return "不能为空!";
	}
	return true;
}

function cellPhone(cellPhone) {
	const reg = /(^(16[0-9]|17[0-9]|13[0-9]|15[0-9]|18[0-9]|14[7,5])\d{8}$)|(^0(13[0-9]|15[0-9]|18[0-9]|14[7,5])\d{8}$)/;
	return cellPhone.match(reg) ? true : "请输入手机号码!";
}

function email(email) {
	const reg = /^\b[\w.%+-]+@[\w.-]+\.[a-zA-Z]{2,6}\b$/;
	return email.match(reg) ? true : "请输入E-Mail地址!";
}

function number(number) {
	const reg = /(^\d+$)|(^-\d+$)/;
	return number.match(reg) ? true : "请输入数字!";
}

function maxLength(value, size) {
	return value.trim().length > size[1] ? ("不能大于" + size[1] + "个字符!") : true;
}

function minLength(value, size) {
	return value.trim().length < size[1] ? ("不能小于" + size[1] + "个字符!") : true;
}

function minItems(selectSize, size) {
	return selectSize < size[1] ? ("至少要选择或选中" + size[1] + "项!") : true;
}

function maxItems(selectSize, size) {
	return selectSize > size[1] ? ("最多只能选择或选中" + size[1] + "项!") : true;
}

function uniqueItems(value) {
	if (Array.isArray(value)) {
		const uniqueValues = new Set(value);
		return uniqueValues.size === value.length ? true : "不允许重复!";
	}
	return true;
}

function parseScientificToBigInt(str) {
	if (!str.includes('e') && !str.includes('E')) {
		return BigInt(str);
	}
	const num = Number(str);
	return BigInt(Math.floor(num));
}

function maximum(value, size) {
	try {
		const valueNum = parseScientificToBigInt(value);
		const sizeNum = parseScientificToBigInt(size[1]);
		return valueNum > sizeNum ? ("数值不能大于" + size[1]) : true;
	} catch (e) {
		return Number(value) > Number(size[1]) ? ("数值不能大于" + size[1]) : true;
	}
}

function minimum(value, size) {
	try {
		const valueNum = parseScientificToBigInt(value);
		const sizeNum = parseScientificToBigInt(size[1]);
		return valueNum < sizeNum ? ("数值不能小于" + size[1]) : true;
	} catch (e) {
		return Number(value) < Number(size[1]) ? ("数值不能小于" + size[1]) : true;
	}
}
function englishWords(englishWords) {
	var reg = /^[A-Z|a-z]+$/;
	return englishWords.match(reg) ? true : "请输入英文字母!";
}

function chineseWords(chineseWords) {
	var reg = /^[\u4e00-\u9fa5]+$/;
	return chineseWords.match(reg) ? true : "请输入汉字!";
}

function webSite(webSite) {
	var reg = /^(http(s)?:\/\/)?([\w-]+\.)+[\w-]+(\/[\w- .\/?%&=]*)?/;
	return webSite.match(reg) ? true : "请输入网址!";
}

function realNumber(realNumber) {
	var reg = /^[-+]?\d+(\.\d+)?$/;
	return realNumber.match(reg) ? true : "请输入实数!";
}

function natureNumber(natureNumber) {
	var reg = /^[0-9]+$/;
	return natureNumber.match(reg) ? true : "请输入自然数!";
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
	var reg = /^[A-Z]+$/;
	return capitalLetters.match(reg) ? true : "请正确输入大写字母";
}

function negativeNumber(negativeNumber) {
	var reg = /^-[1-9]{1}\d*$/;
	return negativeNumber.match(reg) ? true : "请正确输入负整数";
}

function positiveNumber(positiveNumber) {
	var reg = /(^[1-9]{1}\d*$)/;
	return positiveNumber.match(reg) ? true : "请正确输入正整数!";
}

function IDCard(IDCard) {
	let num = IDCard.toUpperCase(); zzzzzzzzz
	if (!(/(^\d{15}$)|(^\d{17}([0-9]|X)$)/.test(num))) {
		return '输入的身份证号长度不对，或者号码不符合规定! 15位号码应全为数字,18位号码末位可以为数字或X!';
	}
	let len, re;
	len = num.length;
	if (len == 15) {
		re = new RegExp(/^(\d{6})(\d{2})(\d{2})(\d{2})(\d{3})$/);
		var arrSplit = num.match(re);
		var dtmBirth = new Date('19' + arrSplit[2] + '/' + arrSplit[3] + '/' +
			arrSplit[4]);
		var bGoodDay;
		bGoodDay = (dtmBirth.getYear() == Number(arrSplit[2])) &&
			((dtmBirth.getMonth() + 1) == Number(arrSplit[3])) &&
			(dtmBirth.getDate() == Number(arrSplit[4]));
		if (!bGoodDay) {
			return '输入的身份证号里出生日期不正确!';
		} else {
			var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
				8, 4, 2);
			var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4',
				'3', '2');
			var nTemp = 0,
				i;
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
		var dtmBirth = new Date(arrSplit[2] + "/" + arrSplit[3] + "/" +
			arrSplit[4]);
		var bGoodDay;
		bGoodDay = (dtmBirth.getFullYear() == Number(arrSplit[2])) &&
			((dtmBirth.getMonth() + 1) == Number(arrSplit[3])) &&
			(dtmBirth.getDate() == Number(arrSplit[4]));
		if (!bGoodDay) {
			return '输入的身份证号里出生日期不对!';
		} else {
			var valNum;
			var arrInt = new Array(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5,
				8, 4, 2);
			var arrCh = new Array('1', '0', 'X', '9', '8', '7', '6', '5', '4',
				'3', '2');
			var nTemp = 0,
				i;
			for (i = 0; i < 17; i++) {
				nTemp += num.substr(i, 1) * arrInt[i];
			}
			valNum = arrCh[nTemp % 11];
			if (valNum != num.substr(17, 1)) {
				return '18位身份证的校验码不正确!应该为：' + valNum;
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
	let j = 0;
	for (j = 0; j < a.length; j++) {
		if (getParamType(a[j]) == 'array') {
			for (const rg in validationRegExps) {
				const reg = validationRegExps[rg];
				if (e.match(reg)) {
					const str = e.substr(0, e.indexOf(":"));
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
	const temp = [];
	for (let i = 0; i < a.length; i++) {
		if (a[i] !== '') {
			if (!contains(temp, a[i])) {
				let rmk = false;
				for (let j = 0; j < validationRegExps.length; j++) {
					const reg = validationRegExps[j];
					const fs = a[i].match(reg);
					if (fs) {
						rmk = true;
						const vs = reg.exec(a[i]);
						const str = a[i].substr(0, a[i].indexOf(":"));
						temp.push([str, vs]);
						break;
					}
				}
				if (!rmk) {
					temp.push(a[i]);
				}
			}
		}
	}
	return temp;
}

function getParamType(param) {
	return ((_t = typeof (param)) == "object" ? Object.prototype.toString.call(
		param).slice(8, -1) : _t).toLowerCase();
}

function executeFunction(fname, value) {
	let execute = false;
	for (let i = 0; i < isNullNoSkipRules.length; i++) {
		if (isNullNoSkipRules[i] === fname) {
			execute = true;
			break;
		}
	}
	if (execute || (value !== null && value !== '')) {
		try {
			if (typeof window[fname] !== 'function') {
				print(`验证函数${fname}不存在`);
				return true;
			}
			if (typeof arguments[2] !== 'undefined') {
				return window[fname].apply(null, [value, arguments[2]]);
			} else {
				return window[fname].call(null, value);
			}
		} catch (e) {
			print(`执行函数${fname}出错：${e.message}`);
			return true;
		}
	}
	return true;
}

function parse(parseRule, obj) {
	if (parseRule == null || parseRule == '')
		return true;
	parseRule = uniqueArr(parseRule.split("&"));
	var type = arguments[2];
	if (typeof type == 'boolean') {
		try {
			if (!type) {
				var objs = document.getElementsByName(obj.name);
				var error = null;
				for (var ri = 0; ri < objs.length; ri++) {
					value = objs[ri].value;
					for (var rle in parseRule) {
						var rlre = parseRule[rle];
						if (getParamType(rlre) == 'array') {
							if ((rlre[0] != 'maxItems' && rlre[0] != 'minItems')) {
								error = executeFunction(rlre[0], value, rlre[1]);
								if (typeof error != 'boolean')
									return new Array(objs[ri], error.toString());
							}
						} else {
							if (rlre != 'uniqueItems') {
								error = executeFunction(rlre, value);
								if (typeof error != 'boolean')
									return new Array(objs[ri], error.toString());
							}
						}
					}
				}
			} else {
				var value = obj.value.trim();
				for (var rle in parseRule) {
					var rlre = parseRule[rle];
					var error = null;
					if (getParamType(rlre) == 'array') {
						if ((rlre[0] != 'maxItems' && rlre[0] != 'minItems')) {
							error = executeFunction(rlre[0], value, rlre[1]);
						}
					} else {
						if (rlre != 'uniqueItems') {
							error = executeFunction(rlre, value);
						}
					}
					if (typeof error != 'boolean') {
						return new Array(obj, error.toString());
					}
				}
			}

		} catch (e) {
			if (debug)
				return new Array(obj, "debug\t" + e.toString());
		}
	} else {
		if (type == 'radio') {
			var objs = document.getElementsByName(obj.name);
			var objVar = null;
			var value = null;
			var valueArray = new Array();
			var size = 0;
			for (var ri = 0; ri < objs.length; ri++) {
				if (objs[ri].checked) {
					value = objs[ri].value;
					var objVar = objs[ri];
					valueArray.push(value);
					size++;
					break;
				}
			}
			for (var rle in parseRule) {
				var rlre = parseRule[rle];
				var error = null;
				if (getParamType(rlre) == 'array') {
					if (rlre[0] == 'maxItems' || rlre[0] == 'minItems') {
						error = executeFunction(rlre[0], size, rlre[1]);
					} else
						error = executeFunction(rlre[0], value, rlre[1]);
				} else {
					if (rlre == 'uniqueItems') {
						error = executeFunction(rlre, valueArray);
					} else {
						error = executeFunction(rlre, value);
					}
				}
				if (typeof error != 'boolean') {
					return new Array(objVar == null ? objs[0] : objVar, error.toString());
				}
			}
		} else if (type == 'checkbox') {
			var objs = document.getElementsByName(obj.name);
			var size = 0;
			var error = null;
			var cObjVar = null;
			var value = null;
			var valueArray = new Array();
			for (var ri = 0; ri < objs.length; ri++) {
				if (objs[ri].checked) {
					value = objs[ri].value;
					cObjVar = objs[ri];
					valueArray.push(value);
					size++;
					for (var rle in parseRule) {
						var rlre = parseRule[rle];
						if (getParamType(rlre) == 'array') {
							if ((rlre[0] != 'maxItems' && rlre[0] != 'minItems')) {
								error = executeFunction(rlre[0], value, rlre[1]);
								if (typeof error != 'boolean')
									return new Array(objs[ri], error.toString());
							}
						} else {
							if (rlre != 'uniqueItems') {
								error = executeFunction(rlre, value);
								if (typeof error != 'boolean')
									return new Array(objs[ri], error.toString());
							}
						}
					}
				}
			}
			for (var rle in parseRule) {
				var rlre = parseRule[rle];
				if (getParamType(rlre) == 'array') {
					if ((rlre[0] == 'maxItems' || rlre[0] == 'minItems')) {
						error = executeFunction(rlre[0], size, rlre[1]);
						if (typeof error != 'boolean')
							return new Array(cObjVar == null ? objs[0] : cObjVar, error.toString());
					}
				} else if (rlre == 'uniqueItems') {
					error = executeFunction(rlre, valueArray);
					if (typeof error != 'boolean')
						return new Array(obj, error.toString());
				}
			}
		} else if (type == 'SELECT') {
			var size = 0;
			for (var i = 0; i < obj.options.length; i++) {
				if (obj.options[i].selected) {
					var value = obj.options[i].value;
					size++;
					for (var rle in parseRule) {
						var rlre = parseRule[rle];
						if (getParamType(rlre) == 'array') {
							if ((rlre[0] != 'maxItems' && rlre[0] != 'minItems')) {
								error = executeFunction(rlre[0], value, rlre[1]);
								if (typeof error != 'boolean')
									return new Array(obj, error.toString());
							}
						} else {
							if (rlre != 'uniqueItems') {
								error = executeFunction(rlre, value);
								if (typeof error != 'boolean')
									return new Array(obj, error.toString());
							}
						}
					}
				}
			}
			for (var rle in parseRule) {
				var rlre = parseRule[rle];
				if (getParamType(rlre) == 'array') {
					if ((rlre[0] == 'maxItems' || rlre[0] == 'minItems')) {
						error = executeFunction(rlre[0], size, rlre[1]);
						if (typeof error != 'boolean')
							return new Array(obj, error.toString());
					}
				} else if (rlre == 'uniqueItems') {
					error = executeFunction(rlre, valueArray);
					if (typeof error != 'boolean')
						return new Array(obj, error.toString());
				}
			}
		}
	}
	return true;
}

function invoke(obj, type, validateRule, tagName, fromId) {
	if (type == 'text' || tagName == 'TEXTAREA' || type == 'password' ||
		type == 'hidden' || type == 'email' || type == 'number' || type == 'tel' || type == 'url' || type == 'date' || type == 'datetime-local' || type == 'month' || type == 'week' || type == 'time' || type == 'color') {
		return parse(validateRule, obj, fromId);
	} else if (type == 'radio' || type == 'checkbox') {
		return parse(validateRule, obj, type);
	} else if (tagName == "SELECT") {
		return parse(validateRule, obj, tagName);
	} else {
		return new Array(obj, "can't define " + type);
	}
}

function validate(param) {
	var obj = document.getElementById(param.id);
	var fromId = true;
	if (typeof obj == 'undefined' || obj == null) {
		fromId = false;
		obj = document.getElementsByName(param.id);
		if (typeof obj == 'undefined' || obj == null || obj.length == 0) {
			print("can't find id or name : " + param.id);
			return true;
		}
		obj = obj[0];
	}

	var validateRule = param.validateRule;
	if (validateRule == null)
		return true;
	var tagName = obj.tagName;
	var type = obj.getAttribute("type");
	var viewName = param.viewName;
	if (viewName == null) {
		print("viewName can't null!");
		return false;
	}
	if (tagName != 'INPUT' && tagName != 'TEXTAREA' && tagName != 'SELECT') {
		print(viewName + "\t" + tagName + "不属于表单对象!");
		return true;
	}
	var invokeVar = invoke(obj, type, validateRule, tagName, fromId);
	var error = true;
	var invokeObj;
	if (typeof invokeVar != 'boolean') {
		error = invokeVar[1];
		invokeObj = invokeVar[0];
	}
	if (typeof error != 'undefined' && typeof error != 'boolean') {
		if (typeof param.alertFunction == 'string') {
			var af = document.getElementById(param.alertFunction);
			if (typeof af == 'undefined' || af == null) {
				print("can't find id: " + param.alertFunction);
				return false;
			}
			af.innerHTML = errorPrefix + viewName + error + errorSuffix;
			return false;
		} else if (typeof param.alertFunction == 'function') {
			param.alertFunction(invokeObj, viewName + error);
			return false;
		} else {
			if (typeof globalAlertFunction == 'function') {
				globalAlertFunction(invokeObj, viewName + error);
				return false;
			} else if (typeof globalAlertFunction == 'string') {
				var af = document.getElementById(param.id + globalAlertFunction);
				if (typeof af == 'undefined' || af == null) {
					print("can't find id: " + param.id + globalAlertFunction);
					return false;
				}
				af.innerHTML = errorPrefix + viewName + error + errorSuffix;
				return false;
			} else {
				alert(viewName + error);
				return false;
			}
		}
	} else {
		if (typeof param.alertFunction == 'string') {
			var af = document.getElementById(param.alertFunction);
			if (typeof af == 'undefined' || af == null) {
				print("can't find id: " + param.alertFunction);
				return false;
			}
			af.innerHTML = successPrefix + viewName + "通过验证" + successSuffix;
		} else if (typeof globalAlertFunction == 'string') {
			var af = document.getElementById(param.id + globalAlertFunction);
			if (typeof af == 'undefined' || af == null) {
				print("can't find id: " + param.id + globalAlertFunction);
				return false;
			}
			af.innerHTML = successPrefix + viewName + "通过验证!" + successSuffix;
		}
		return true;
	}
}

function buildOn(funObj, funName, funValue) {
	try {
		funObj.addEventListener(funName, function () {
			return validate(funValue);
		}, true);
	} catch (e) {
		funObj.attachEvent("on" + funName, function () {
			return validate(funValue);
		});
	}
}

//globalAlertFunction = function|string

function Validator() {
	return {
		version: "v0.1",
		list: new Array(),
		getValidateObjById: function (id) {
			var list = this.list;
			for (var i = 0; i < list.length; i++) {
				var obj = list[i];
				if (obj.id == id) return obj;
			}
			return null;
		},
		addRegExp: function (exp) {
			validationRegExps.push(exp);
		},
		addNoSkip: function (fname) {
			isNullNoSkipRules.push(fname);
		},
		appendRule: function (id, rule) {
			var obj = this.getValidateObjById(id);
			if (obj != null) {
				var ary = null;
				if (obj.validateRule == null) ary = new Array();
				else ary = uniqueArr(obj.validateRule.split("&"));
				for (var i = 0; i < ary.length; i++) {
					if (getParamType(ary[i]) == 'array') {
						if (ary[i][1][0] == rule) return;
					} else {
						if (ary[i] == rule) return;
					}
				}
				if (ary.length > 0) obj.validateRule = obj.validateRule + "&" + rule;
				else obj.validateRule = rule;
			} else
				print("can't find rule id:" + id + "!");
		},
		removeRule: function (id, rule) {
			var obj = getValidateObjById(id);
			if (obj != null) {
				var ary = null;
				if (obj.validateRule == null) ary = new Array();
				else ary = uniqueArr(obj.validateRule.split("&"));
				for (var i = 0; i < ary.length; i++) {
					var rem = false;
					if (getParamType(ary[i]) == 'array')
						rem = ary[i][1][0] == rule;
					else
						rem = ary[i] == rule;
					if (rem) {
						ary.splice(i, 1);
						break;
					}
				}
				if (ary.length > 0) {
					var strBuf = "";
					for (var i = 0; i < ary.length; i++) {
						strBuf += ary[i];
						if (i < ary.length - 1)
							strBuf += "&";
					}
					obj.validateRule = strBuf;
				} else {
					obj.validateRule = null;
				}
			}
		},
		removeAllRule: function (id) {
			var obj = getValidateObjById(id);
			if (obj != null) {
				obj.validateRule = null;
			}
		},
		setRule: function (id, rule) {
			var obj = getValidateObjById(id);
			if (obj != null) {
				obj.validateRule = rule;
			}
		},
		setViewName: function (id, viewName) {
			var obj = getValidateObjById(id);
			if (obj != null) {
				obj.viewName = viewName;
			}
		},
		push: function (eData) {
			var isHtml = goodNodeHTML(eData);
			if (isHtml) {
				eData.id = eData.name;
				eData.viewName = eData.getAttribute("viewName");
				eData.validateRule = eData.getAttribute("validateRule");
				eData.buildFunction = eData.getAttribute("buildFunction");
				if (eData.getAttribute("alertFunction") != null) {
					try {
						if (typeof eval(eData.getAttribute("alertFunction")) == 'function') {
							eData.alertFunction = eval(eData.getAttribute("alertFunction"));
						}
					} catch (e) {
						eData.alertFunction = eData.getAttribute("alertFunction");
					}
				}
			}
			if (eData.viewName) eData.viewName = removeHTMLTag(eData.viewName);
			if (eData.id == null || eData.id == "") {
				print("id can't null!");
				return;
			}
			for (var i = 0; i < this.list.length; i++) {
				if (eData.id == this.list[i].id) return;
			}
			if (eData != null && eData.buildFunction != null) {
				var af = document.getElementById(eData.id);
				var bfs = eData.buildFunction.split(",");
				if (typeof af == 'undefined' || af == null) {
					var objs = document.getElementsByName(eData.id);
					if (typeof objs == 'undefined' || objs == null || objs.length == 0) {
						print("can't find id: " + eData.id);
						return;
					} else {
						for (var ri = 0; ri < objs.length; ri++) {
							for (var index in bfs) {
								buildOn(objs[ri], bfs[index], eData);
							}
						}
					}
				} else {
					if (af.type == 'checkbox' || af.type == 'radio') {
						var objs = document.getElementsByName(eData.id);
						if (typeof objs == 'undefined' || objs == null || objs.length == 0) {
							for (var index in bfs) {
								buildOn(af, bfs[index], eData);
							}
						} else {
							for (var ri = 0; ri < objs.length; ri++) {
								for (var index in bfs)
									buildOn(objs[ri], bfs[index], eData);
							}
						}
					} else
						for (var index in bfs) {
							buildOn(af, bfs[index], eData);
						}
				}
			}
			this.list.push(eData);
		},
		validate_rule: function (id, viewName, validateRule, buildFunction, alertFunction) {
			this.id = id;
			this.viewName = viewName;
			this.validateRule = validateRule;
			this.alertFunction = alertFunction;
			this.buildFunction = buildFunction;
		},
		addRule: function (validate_rule) {
			if (getParamType(validate_rule) == 'array') {
				for (var rle in validate_rule) {
					this.push(validate_rule[rle]);
				}
			} else if (getParamType(validate_rule) == 'object') {
				this.push(validate_rule);
			} else {
				print("unknown this addRule" + getParamType(validate_rule) + ",rule must in array or object!");
			}
		},
		validateAll: function () {
			for (var index in this.list) {
				var param = this.list[index];
				var rm = validate(param);
				if (!rm) return false;
			}
			return true;
		},
		buildFormValidateAutoRule: function (formId, build) {
			var form = document.getElementById(formId);
			if (typeof form == 'undefined') {
				print("can't build on " + formId);
				return;
			}
			var vData = new Array();
			loadNeedElement(form, vData);
			this.addRule(vData);
			if (build)
				form.onsubmit = this.validateAll;
		},
		buildFormValidate: function (formId) {
			var form = document.getElementById(formId);
			var validator = this;
			if (typeof form == 'undefined') {
				print("can't build on " + formId);
				return;
			}
			form.onsubmit = function () {
				return validator.validateAll();
			}
		}
	}
};


function loadNeedElement(ele, vData) {
	var elements;
	if (ele.hasChildNodes()) {
		elements = ele.childNodes;
		for (var i = 0; i < elements.length; i++) {
			loadNeedElement(elements[i], vData);
		}
	}
	var tagName = ele.tagName;
	if (tagName != 'INPUT' && tagName != 'TEXTAREA' && tagName != 'SELECT') return;
	var type = ele.type;
	vData.push(ele);
}

function goodNodeHTML(objHtml) {
	var de = document.createElement("div");
	try {
		de.appendChild(objHtml.cloneNode(true));
		return objHtml.nodeType == 1 ? true : false;
	} catch (e) {
		return false;
	}
}

function removeHTMLTag(str) {
	str = str.replace(/<\/?[^>]*>/g, '');
	str = str.replace(/[ | ]*\n/g, '\n');
	str = str.replace(/\n[\s| | ]*\r/g, '\n');
	str = str.replace(/&nbsp;/ig, '');
	return str;
}

var sir_validator = new Validator();
