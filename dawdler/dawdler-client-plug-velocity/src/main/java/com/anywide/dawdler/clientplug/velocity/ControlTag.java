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
	private String controlName; // 前台控件名称
	private String viewName; // 前台显示名称
	private String controlType; // 控件的类型，如text,hiden,password,textarea,radio,checkbox,select等
	private String showItems;// 显示项目
	private String value;// 值
	private String validateRule; // 验证规则
	private String viewDescription; // 前台描述
	private String css;// CSS
	private String additional; // 追加信息
	private boolean radioDefault;// 为radio类型时默认选中第一位
	private boolean autoAddViewName;// 是否自动添加前面的viewName
	private boolean autoAddViewDescription;// 是否自动添加后面的viewdescription

	public String getControlName() {
		return controlName;
	}

	public void setControlName(String controlName) {
		this.controlName = controlName;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getControlType() {
		return controlType;
	}

	public void setControlType(String controlType) {
		this.controlType = controlType;
	}

	public String getShowItems() {
		return showItems;
	}

	public void setShowItems(String showItems) {
		this.showItems = showItems;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValidateRule() {
		return validateRule;
	}

	public void setValidateRule(String validateRule) {
		this.validateRule = validateRule;
	}

	public String getViewDescription() {
		return viewDescription;
	}

	public void setViewDescription(String viewDescription) {
		this.viewDescription = viewDescription;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAdditional() {
		return additional;
	}

	public void setAdditional(String additional) {
		this.additional = additional;
	}

	public boolean isRadioDefault() {
		return radioDefault;
	}

	public void setRadioDefault(boolean radioDefault) {
		this.radioDefault = radioDefault;
	}

	public boolean isAutoAddViewName() {
		return autoAddViewName;
	}

	public void setAutoAddViewName(boolean autoAddViewName) {
		this.autoAddViewName = autoAddViewName;
	}

	public boolean isAutoAddViewDescription() {
		return autoAddViewDescription;
	}

	public void setAutoAddViewDescription(boolean autoAddViewDescription) {
		this.autoAddViewDescription = autoAddViewDescription;
	}

}
