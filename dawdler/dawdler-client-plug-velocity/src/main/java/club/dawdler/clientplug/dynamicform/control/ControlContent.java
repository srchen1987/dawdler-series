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

/**
 * @author jackson.song
 * @version V1.0
 * 常量类
 */
public interface ControlContent {
	String CONTROL_NAME_REPLACE = "controlNameReplace";
	String CONTROL_TYPE_REPLACE = "controlTypeReplace";
	String CSS_REPLACE = "cssReplace";
	String VALIDATE_RULE_REPLACE = "validateRuleReplace";
	String VALUE_REPLACE = "valueReplace";
	String VIEW_NAME_REPLACE = "viewNameReplace";
	String TAG_VALUE = " value=\"" + VALUE_REPLACE + "\"";
	String TAG_VALIDATE = " validateRule=\"" + VALIDATE_RULE_REPLACE + "\"";
	String TAG_CSS = " style=\"" + CSS_REPLACE + "\"";
	String CHECKED = " checked=\"checked\"";
	String SELECTED = " selected";
	String CHECKED_REPLACE = "checkedReplace";
	String INPUT_START = "<input type=\"" + CONTROL_TYPE_REPLACE + "\"" + " name=\"" + CONTROL_NAME_REPLACE + "\"" + " id=\""
			+ CONTROL_NAME_REPLACE + "\" viewName=\"" + VIEW_NAME_REPLACE + "\"";
	String INPUT_END = "/>";
	String SELECT_START = "<select name=\"" + CONTROL_NAME_REPLACE + "\" id=\"" + CONTROL_NAME_REPLACE + "\" viewName=\""
			+ VIEW_NAME_REPLACE + "\"";
	String SELECT_OVER = "</select>";
	String OPTION_START = "<option value=\"" + VALUE_REPLACE + "\"" + CHECKED_REPLACE + ">";
	String OPTION_OVER = "</option>";
	String TEXTAREA_START = "<textarea name=\"" + CONTROL_NAME_REPLACE + "\"" + " id=\"" + CONTROL_NAME_REPLACE
			+ "\" viewName=\"" + VIEW_NAME_REPLACE + "\"";
	String TEXTAREA_OVER = "</textarea>";
}
