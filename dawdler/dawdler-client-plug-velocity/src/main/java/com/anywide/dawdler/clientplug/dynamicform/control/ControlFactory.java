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
 * @Title ControlFactory.java
 * @Description 控件工厂
 * @date 2006年8月10日
 * @email suxuan696@gmail.com
 */
public class ControlFactory {
	public static final Control getControl(ControlTag tag) {
		String controltype = tag.getControltype();
		if (controltype == null) {
			throw new NullPointerException("controltype can't null !");
		}
		if (controltype.equals("text") || controltype.equals("password") || controltype.equals("hidden")) {
			return new TextControl(tag);
		} else if (controltype.equals("select")) {
			return new SelectControl(tag);
		} else if (controltype.equals("radio")) {
			return new RadioControl(tag);
		} else if (controltype.equals("textarea")) {
			return new TextareaControl(tag);
		} else if (controltype.equals("checkbox")) {
			return new CheckboxControl(tag);
		} else {
			throw new NullPointerException("unknown " + controltype + " tag!");
		}

	}
}
