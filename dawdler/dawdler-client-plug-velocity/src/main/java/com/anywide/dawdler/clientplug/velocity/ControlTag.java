/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.clientplug.velocity;

/**
 * @author jackson.song
 * @version V1.0
 * @Title CheckboxControl.java
 * @Description 控件标签 之前支持jsp标签库 目前放弃了jsp的支持
 * @date 2006年8月10日
 * @email suxuan696@gmail.com
 */
public class ControlTag /* extends TagSupport */ {
	private String controlname; // 前台控件名称
	private String viewName; // 前台显示名称
	private String controltype; // 控件的类型，如text,hiden,password,textarea,radio,checkbox,select等
	private String showitems;// 显示项目
	private String value;// 值
	private String validateRule; // 验证规则
	private String viewdescription; // 前台描述
	private String css;// CSS
	private String additional; // 追加信息
	private boolean radiodefault;// 为radio类型时默认选中第一位
	private boolean autoaddviewname;// 是否自动添加前面的viewname
	private boolean autoaddviewdescription;// 是否自动添加后面的viewdescription
	private String[] selectValue;// 这个属性只有显示页面时候才有用 在标签里不需要

	public boolean isAutoaddviewname() {
		return autoaddviewname;
	}

	public void setAutoaddviewname(boolean autoaddviewname) {
		this.autoaddviewname = autoaddviewname;
	}

	public boolean isAutoaddviewdescription() {
		return autoaddviewdescription;
	}

	public void setAutoaddviewdescription(boolean autoaddviewdescription) {
		this.autoaddviewdescription = autoaddviewdescription;
	}

	public boolean getRadiodefault() {
		return radiodefault;
	}

	public void setRadiodefault(boolean radiodefault) {
		this.radiodefault = radiodefault;
	}

	public String[] getSelectValue() {
		return selectValue;
	}

	public void setSelectValue(String... selectValue) {
		this.selectValue = selectValue;
	}

	public String getAdditional() {
		return additional;
	}

	public void setAdditional(String additional) {
		this.additional = additional;
	}

	public String getControlname() {
		return controlname;
	}

	public void setControlname(String controlname) {
		this.controlname = controlname;
	}

	public String getControltype() {
		return controltype;
	}

	public void setControltype(String controltype) {
		this.controltype = controltype;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getViewname() {
		return viewName;
	}

	public void setViewname(String viewName) {
		this.viewName = viewName;
	}

	public String getViewdescription() {
		return viewdescription;
	}

	public void setViewdescription(String viewdescription) {
		this.viewdescription = viewdescription;
	}

	public String getShowitems() {
		return showitems;
	}

	public void setShowitems(String showitems) {
		this.showitems = showitems;
	}

	public String getValidaterule() {
		return validateRule;
	}

	public void setValidaterule(String validateRule) {
		this.validateRule = validateRule;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
