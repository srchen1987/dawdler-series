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

/**
 * @author jackson.song
 * @version V1.0
 * @Title ControlContent.java
 * @Description 常量类
 * @date 2006年08月10日
 * @email suxuan696@gmail.com
 */
public interface ControlContent {
	String CONTROLNAMEREPLACE = "controlnamereplace";
	String CONTROLTYPEREPLACE = "controltypereplace";
	String CSSREPLACE = "cssreplace";
	String VALIDATERULEREPLACE = "validaterulereplace";
	String VALUEREPLACE = "valuereplace";
	String VIEWNAMEREPLACE = "viewnamereplace";
	String TAGVALUE = " value=\"" + VALUEREPLACE + "\"";
	String TAGVALIDATE = " validateRule=\"" + VALIDATERULEREPLACE + "\"";
	String TAGCSS = " style=\"" + CSSREPLACE + "\"";
	String CHECKED = " checked=\"checked\"";
	String SELECTED = " selected";
	String CHECKEDREPLACE = "checkedreplace";
	String INPUTSTART = "<input type=\"" + CONTROLTYPEREPLACE + "\"" + " name=\"" + CONTROLNAMEREPLACE + "\"" + " id=\""
			+ CONTROLNAMEREPLACE + "\" viewName=\"" + VIEWNAMEREPLACE + "\"";
	String INPUTEND = "/>";
	String SELECTSTART = "<select name=\"" + CONTROLNAMEREPLACE + "\" id=\"" + CONTROLNAMEREPLACE + "\" viewName=\""
			+ VIEWNAMEREPLACE + "\"";
	String SELECTOVER = "</select>";
	String OPTIONSTART = "<option value=\"" + VALUEREPLACE + "\"" + CHECKEDREPLACE + ">";
	String OPTIONOVER = "</option>";
	String TEXTAREASTART = "<textarea name=\"" + CONTROLNAMEREPLACE + "\"" + " id=\"" + CONTROLNAMEREPLACE
			+ "\" viewName=\"" + VIEWNAMEREPLACE + "\"";
	String TEXTAREAOVER = "</textarea>";
}
