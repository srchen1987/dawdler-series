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
 * @Title RadioControl.java
 * @Description 单选框实现
 * @date 2006年8月10日
 * @email suxuan696@gmail.com
 */
public class RadioControl extends Control {
	protected RadioControl(ControlTag tag) {
		super(tag);
	}

	@Override
	protected String replaceContent() {
		String controlname = tag.getControlname();
		String controltype = tag.getControltype();
		String css = tag.getCss();
		String viewName = tag.getViewname();
		String validateRule = tag.getValidaterule();
		String showitems = tag.getShowitems();
		String value = tag.getValue();
		String additional = tag.getAdditional();
		boolean radiodefault = tag.getRadiodefault();
		if (showitems == null) {
			throw new NullPointerException("show item can't null!");
		}
		String[] showitem = showitems.split(",");
		StringBuffer sb = new StringBuffer(150);
		for (int i = 0; i < showitem.length; i++) {
			sb.append(ControlContent.INPUTSTART.replace(ControlContent.CONTROLNAMEREPLACE, controlname)
					.replace(ControlContent.CONTROLTYPEREPLACE, controltype)
					.replace(ControlContent.VIEWNAMEREPLACE, viewName));
			if (css != null && !css.trim().equals(""))
				sb.append(ControlContent.TAGCSS.replace(ControlContent.CSSREPLACE, css));
			if (validateRule != null && !validateRule.trim().equals(""))
				sb.append(ControlContent.TAGVALIDATE.replace(ControlContent.VALIDATERULEREPLACE, validateRule));
			sb.append(ControlContent.TAGVALUE.replace(ControlContent.VALUEREPLACE, i + ""));
			sb.append(value != null ? value.equals("" + i) ? ControlContent.CHECKED : ""
					: (i == 0 && radiodefault) ? ControlContent.CHECKED : "");
			if (additional != null)
				sb.append(" " + additional);
			sb.append(ControlContent.INPUTEND);
			sb.append(showitem[i] + "   ");
		}
		return sb.toString();
	}

}
