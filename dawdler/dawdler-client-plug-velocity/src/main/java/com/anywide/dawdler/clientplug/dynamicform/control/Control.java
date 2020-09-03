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
 * 
 * @Title:  Control.java   
 * @Description:    抽象控件   
 * @author: jackson.song    
 * @date:   2006年08月10日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public abstract class Control {
	protected ControlTag tag; 
	public static void setShowcolon(boolean showcolon) {
		Control.showcolon = showcolon;
	}
	private static boolean showcolon;
	protected Control(ControlTag tag) {
		this.tag = tag;
	}
	public String showView(){
		ControlTag ntag = (ControlTag) tag;
		if(ntag.getControlname()==null){
			throw new NullPointerException("controlname can't null !");
		}
		if(ntag.getViewname()==null){
			throw new NullPointerException("viewname can't null !");
		}
		return translation();
	}
	protected  abstract String replaceContent();
	protected String translation(){
		String notEmpty="";
		if(tag.getValidaterule()!=null){
			if(tag.getValidaterule().contains("notEmpty"))
				notEmpty="<font color=\"red\">*</font>";
		}
		return notEmpty+(tag.isAutoaddviewname()?tag.getViewname()+(showcolon?" : ":"  "):"")+replaceContent()+((tag.getViewdescription()!=null&&tag.isAutoaddviewdescription())?tag.getViewdescription():"");
	}
}
