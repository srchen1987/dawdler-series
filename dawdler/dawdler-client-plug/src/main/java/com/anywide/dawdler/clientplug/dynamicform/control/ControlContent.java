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
 * 
 * @Title:  ControlContent.java   
 * @Description:    常量类   
 * @author: jackson.song    
 * @date:   2006年08月10日       
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public interface ControlContent {
	  public static final String CONTROLNAMEREPLACE="controlnamereplace";
	  public static final String CONTROLTYPEREPLACE="controltypereplace";
	  public static final String CSSREPLACE="cssreplace";
	  public static final String VALIDATERULEREPLACE="validaterulereplace";
	  public static final String VALUEREPLACE="valuereplace";
	  public static final String VIEWNAMEREPLACE="viewnamereplace";
	  public static final String TAGVALUE=" value=\""+VALUEREPLACE+"\""; 
	  public static final String TAGVALIDATE=" validaterule=\""+VALIDATERULEREPLACE+"\""; 
	  public static final String TAGCSS=" style=\""+CSSREPLACE+"\""; 
	  public static final String CHECKED=" checked=\"checked\""; 
	  public static final String SELECTED = " selected";
	  public static final String CHECKEDREPLACE="checkedreplace";
	  public static final String INPUTSTART="<input type=\""+CONTROLTYPEREPLACE+"\""+" name=\""+CONTROLNAMEREPLACE+"\""+" id=\""+CONTROLNAMEREPLACE+"\" viewname=\""+VIEWNAMEREPLACE+"\"";
	  public static final String INPUTEND="/>";
	  public static final String SELECTSTART="<select name=\""+CONTROLNAMEREPLACE+"\" id=\""+CONTROLNAMEREPLACE+"\" viewname=\""+VIEWNAMEREPLACE+"\"";
	  public static final String SELECTOVER="</select>";
	  public static final String OPTIONSTART="<option value=\""+VALUEREPLACE+"\""+CHECKEDREPLACE+">";
	  public static final String OPTIONOVER="</option>";
	  public static final String TEXTAREASTART="<textarea name=\""+CONTROLNAMEREPLACE+"\""+" id=\""+CONTROLNAMEREPLACE+"\" viewname=\""+VIEWNAMEREPLACE+"\"";
	  public static final String TEXTAREAOVER="</textarea>";
}
