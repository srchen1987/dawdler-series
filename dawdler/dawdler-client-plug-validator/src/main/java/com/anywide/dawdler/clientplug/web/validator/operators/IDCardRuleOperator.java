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
package com.anywide.dawdler.clientplug.web.validator.operators;
import java.util.List;
import com.anywide.dawdler.clientplug.web.validator.ext.IDCard;
/**
 * 
 * @Title:  IDCardRuleOperator.java   
 * @Description:    身份证验证   
 * @author: jackson.song    
 * @date:   2007年07月22日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class IDCardRuleOperator extends StringRuleOperator {
	public static final String RULEKEY = "IDCard";
	public static final String REGEX = null;
	public static final String EXPLAIN = "身份证验证";

	public IDCardRuleOperator() {
		super(RULEKEY, REGEX, EXPLAIN);
	}

	@Override
	public String toString() {
		return EXPLAIN;
	}

	@Override
	public String validate(Object value) {
		if(value==null)return null;
		boolean flag=true;
		if(value instanceof String){
			flag = isIDCard(value.toString());
		}else if (value instanceof String[]){
			String[] values = (String[]) value;
			for(String v :values){
				if(v==null){
					flag= false;
					break;
				}
				if(!isIDCard(v)){
					flag=false;
					break;	
				}
			}
		}else if(value instanceof List){
			List values = (List) value;
			for(Object o : values){
				if(o==null){
					flag= false;
					break;
				}
				if(!isIDCard(o.toString())){
					flag=false;
					break;	
				}
			}
		}
		if(!flag)return "请输入正确的身份证号码!";
		return null;
	}
	
	private boolean isIDCard(String value){
		try {
			return IDCard.IDCardValidate(value.toString());
		} catch (Exception e) {
		}
		return false;
	}

}

