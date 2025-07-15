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
package club.dawdler.clientplug.dynamicform.control;

import club.dawdler.clientplug.velocity.ControlTag;

/**
 * @author jackson.song
 * @version V1.0
 * 下拉列表框的实现
 */
public class SelectControl extends Control {
	protected SelectControl(ControlTag tag) {
		super(tag);
	}

	@Override
	protected String replaceContent() {
		String controlName = tag.getControlName();
		String controlType = tag.getControlType();
		String css = tag.getCss();
		String viewName = tag.getViewName();
		String validateRule = tag.getValidateRule();
		String showItems = tag.getShowItems();
		String value = tag.getValue();
		String additional = tag.getAdditional();
		if (showItems == null) {
			throw new NullPointerException("show item can't null!");
		}
		StringBuffer sbt = new StringBuffer(32);
		sbt.append(ControlContent.SELECT_START.replace(ControlContent.CONTROL_NAME_REPLACE, controlName)
				.replace(ControlContent.CONTROL_TYPE_REPLACE, controlType)
				.replace(ControlContent.VIEW_NAME_REPLACE, viewName));
		if (css != null && !css.trim().equals("")) {
			sbt.append(ControlContent.TAG_CSS.replace(ControlContent.CSS_REPLACE, css));
		}
		if (validateRule != null && !validateRule.trim().equals("")) {
			sbt.append(ControlContent.TAG_VALIDATE.replace(ControlContent.VALIDATE_RULE_REPLACE, validateRule));
		}
		if (additional != null) {
			sbt.append(" " + additional);
		}
		sbt.append(">");
		String[] showItem = showItems.split(",");
		StringBuffer sb = new StringBuffer(150);
		sb.append(ControlContent.OPTION_START.replace(ControlContent.VALUE_REPLACE, "")
				.replace(ControlContent.CHECKED_REPLACE, value == null ? ControlContent.SELECTED : ""));
		sb.append("请选择");
		sb.append(ControlContent.OPTION_OVER);
		for (int i = 0; i < showItem.length; i++) {
			String optionStart = ControlContent.OPTION_START.replace(ControlContent.VALUE_REPLACE, "" + i);
			if (value == null) {
				optionStart = optionStart.replace(ControlContent.CHECKED_REPLACE, "");
			} else {
				String[] values = value.split(",");
				for (String v : values) {
					if (v.equals("" + i)) {
						optionStart = optionStart.replace(ControlContent.CHECKED_REPLACE, ControlContent.SELECTED);
					} else {
						optionStart = optionStart.replace(ControlContent.CHECKED_REPLACE, "");
					}
				}
			}
			sb.append(optionStart);
			sb.append(showItem[i]);
			sb.append(ControlContent.OPTION_OVER);
		}
		sbt.append(sb.toString() + ControlContent.SELECT_OVER);
		return sbt.toString();
	}
}
