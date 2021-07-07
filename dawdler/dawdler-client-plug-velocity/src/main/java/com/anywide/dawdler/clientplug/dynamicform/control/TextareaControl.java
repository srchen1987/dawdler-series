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
 * @Title TextareaControl.java
 * @Description 文本域的实现
 * @date 2006年08月10日
 * @email suxuan696@gmail.com
 */
public class TextareaControl extends Control {
	protected TextareaControl(ControlTag tag) {
		super(tag);
	}

	@Override
	protected String replaceContent() {
		String controlname = tag.getControlname();
		String controltype = tag.getControltype();
		String css = tag.getCss();
		String viewName = tag.getViewname();
		String validateRule = tag.getValidaterule();
		String value = tag.getValue();
		String additional = tag.getAdditional();
		StringBuffer sb = new StringBuffer(128);
		sb.append(ControlContent.TEXTAREASTART.replace(ControlContent.CONTROLNAMEREPLACE, controlname)
				.replace(ControlContent.CONTROLTYPEREPLACE, controltype)
				.replace(ControlContent.VIEWNAMEREPLACE, viewName));
		if (css != null && !css.trim().equals(""))
			sb.append(ControlContent.TAGCSS.replace(ControlContent.CSSREPLACE, css));
		if (validateRule != null && !validateRule.trim().equals(""))
			sb.append(ControlContent.TAGVALIDATE.replace(ControlContent.VALIDATERULEREPLACE, validateRule));
		if (additional != null)
			sb.append(" " + additional);
		sb.append(">");
		if (value != null && !value.trim().equals(""))
			sb.append(value);
		sb.append(ControlContent.TEXTAREAOVER);
		return sb.toString();
	}

}
