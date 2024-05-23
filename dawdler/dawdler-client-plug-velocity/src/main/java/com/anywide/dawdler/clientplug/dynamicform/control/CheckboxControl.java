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
package com.anywide.dawdler.clientplug.dynamicform.control;

import com.anywide.dawdler.clientplug.velocity.ControlTag;

/**
 * @author jackson.song
 * @version V1.0
 * 复选框控件实现
 */
public class CheckboxControl extends Control {
	public CheckboxControl(ControlTag tag) {
		super(tag);
	}

	public String replaceContent() {
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
		String[] showItem = showItems.split(",");
		StringBuffer sb = new StringBuffer(150);
		for (int i = 0; i < showItem.length; i++) {
			sb.append(ControlContent.INPUTSTART.replace(ControlContent.CONTROLNAMEREPLACE, controlName)
					.replace(ControlContent.CONTROLTYPEREPLACE, controlType)
					.replace(ControlContent.VIEWNAMEREPLACE, viewName));
			if (css != null && !css.trim().equals("")) {
				sb.append(ControlContent.TAGCSS.replace(ControlContent.CSSREPLACE, css));
			}
			if (validateRule != null && !validateRule.trim().equals("")) {
				sb.append(ControlContent.TAGVALIDATE.replace(ControlContent.VALIDATERULEREPLACE, validateRule));
			}
			sb.append(ControlContent.TAGVALUE.replace(ControlContent.VALUEREPLACE, i + ""));
			if (value != null) {
				for (String v : value.split(",")) {
					if (v.equals("" + i)) {
						sb.append(ControlContent.CHECKED);
					}
				}
			}
			if (additional != null) {
				sb.append(" " + additional);
			}
			sb.append(ControlContent.INPUTEND);
			sb.append(showItem[i] + "   ");
		}
		return sb.toString();
	}

}
