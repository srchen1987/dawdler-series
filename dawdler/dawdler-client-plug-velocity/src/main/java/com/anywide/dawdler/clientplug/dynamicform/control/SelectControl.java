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
 * @Title:  SelectControl.java   
 * @Description:    下拉列表框的实现   
 * @author: jackson.song    
 * @date:   2006年08月10日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class SelectControl extends Control {
	protected SelectControl(ControlTag tag) {
		super(tag);
	}
	protected String replaceContent(){
		String controlname =tag.getControlname();
		String controltype =tag.getControltype();
		String css =tag.getCss();
		String viewname= tag.getViewname();
		String validaterule =tag.getValidaterule();
		String showitems = tag.getShowitems();
		String value = tag.getValue();
		String additional = tag.getAdditional();
		if(showitems==null){
			throw new NullPointerException("show item can't null!");
		}
		StringBuffer sbt = new StringBuffer(32);
		sbt.append(ControlContent.SELECTSTART.replace(ControlContent.CONTROLNAMEREPLACE,controlname).replace(ControlContent.CONTROLTYPEREPLACE, controltype).replace(ControlContent.VIEWNAMEREPLACE,viewname));
		if(css!=null&&!css.trim().equals(""))sbt.append(ControlContent.TAGCSS.replace(ControlContent.CSSREPLACE,css));
		if(validaterule!=null&&!validaterule.trim().equals(""))sbt.append(ControlContent.TAGVALIDATE.replace(ControlContent.VALIDATERULEREPLACE,validaterule));
		if(additional!=null)sbt.append(" "+additional);
		sbt.append(">");
		String [] showitem = showitems.split(",");
		StringBuffer sb = new StringBuffer(150);
		sb.append(ControlContent.OPTIONSTART.replace(ControlContent.VALUEREPLACE,"").replace(ControlContent.CHECKEDREPLACE,value==null?ControlContent.SELECTED:""));
		sb.append("请选择");
		sb.append(ControlContent.OPTIONOVER);
		for(int i = 0;i<showitem.length;i++){
			String optionstart = ControlContent.OPTIONSTART.replace(ControlContent.VALUEREPLACE,""+i);
			if(value==null){
//				if(i==0)
//					optionstart = optionstart.replace(ControlContent.CHECKEDREPLACE,ControlContent.SELECTED);
//				else
					optionstart = optionstart.replace(ControlContent.CHECKEDREPLACE,"");
			}
			else{
				String values [] = value.split(",");
				for(String v:values){
					if(v.equals(""+i)){
						optionstart = optionstart.replace(ControlContent.CHECKEDREPLACE,ControlContent.SELECTED);
					}else{
						optionstart = optionstart.replace(ControlContent.CHECKEDREPLACE,"");
					}
				}
			}
			sb.append(optionstart);
			sb.append(showitem[i]);
			sb.append(ControlContent.OPTIONOVER);
		}
		sbt.append(sb.toString()+ControlContent.SELECTOVER);
		return sbt.toString();
	} 
}
