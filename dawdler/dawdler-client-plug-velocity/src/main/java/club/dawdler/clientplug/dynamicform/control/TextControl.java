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
 * 文本框的实现
 */
public class TextControl extends Control {
	protected TextControl(ControlTag tag) {
		super(tag);
	}

	@Override
	protected String replaceContent() {
		String controlName = tag.getControlName();
		String controlType = tag.getControlType();
		String css = tag.getCss();
		String viewName = tag.getViewName();
		String validateRule = tag.getValidateRule();
		String value = tag.getValue();
		String additional = tag.getAdditional();
		StringBuilder sb = new StringBuilder(128);
		sb.append(ControlContent.INPUT_START.replace(ControlContent.CONTROL_NAME_REPLACE, controlName)
				.replace(ControlContent.CONTROL_TYPE_REPLACE, controlType)
				.replace(ControlContent.VIEW_NAME_REPLACE, viewName));
		if (css != null && !css.trim().equals("")) {
			sb.append(ControlContent.TAG_CSS.replace(ControlContent.CSS_REPLACE, css));
		}
		if (validateRule != null && !validateRule.trim().equals("")) {
			sb.append(ControlContent.TAG_VALIDATE.replace(ControlContent.VALIDATE_RULE_REPLACE, validateRule));
		}
		if (value != null && !value.trim().equals("")) {
			sb.append(ControlContent.TAG_VALUE.replace(ControlContent.VALUE_REPLACE, value));
		}
		if (additional != null) {
			sb.append(" " + additional);
		}
		sb.append(ControlContent.INPUT_END);
		return sb.toString();
	}
}
