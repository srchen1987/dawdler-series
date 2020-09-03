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
package com.anywide.dawdler.clientplug.web.validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.web.validator.operators.RegexRuleOperator;
import com.anywide.dawdler.clientplug.web.validator.operators.RuleOperator;
import com.anywide.dawdler.clientplug.web.validator.operators.StringRuleOperator;
import com.anywide.dawdler.clientplug.web.validator.scanner.RuleOperatorScanner;
/**
 * 
 * @Title:  RuleOperatorProvider.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年07月21日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class RuleOperatorProvider {
	private static Logger logger = LoggerFactory.getLogger(RuleOperatorProvider.class);
	private static Map<String,StringRuleOperator> stringRules = new HashMap<String,StringRuleOperator>();
	private static Map<Pattern,RegexRuleOperator> regexRules = new HashMap<Pattern,RegexRuleOperator>();
	private static Map<String,RegexRuleOperator> stringRegexRules = new HashMap<String,RegexRuleOperator>();
	public static Map<String, StringRuleOperator> getStringRules() {
		return stringRules;
	}
	public static Map<Pattern, RegexRuleOperator> getRegexRules() {
		return regexRules;
	}
	public static RegexRuleOperator getRegexRule(String regex){
		return stringRegexRules.get(regex);
	}
	public static StringRuleOperator getStringRule(String rulekey){
		return stringRules.get(rulekey);
	}
	static{
		registerRuleOperatorScanPackage(RuleOperator.class);
	}
	public static void registerRuleOperator(RuleOperator ro){
		if(ro==null)return;
		if(ro.getRuleKey()!=null){
			if(stringRules.containsKey(ro.getRuleKey())){
				logger.warn(ro.getRuleKey()+"\talready exists in "+stringRules.get(ro.getRuleKey()).getClass().getName()+"!");
			}else{
				stringRules.put(ro.getRuleKey(),(StringRuleOperator)ro);
			}
		}
		else if(ro.getPattern()!=null){
			if(stringRegexRules.containsKey(ro.getPattern().pattern())){
				logger.warn(ro.getPattern().pattern()+"\talready exists in "+stringRegexRules.get(ro.getPattern().pattern()).getClass().getName()+"!");
			}else{
				regexRules.put(ro.getPattern(),(RegexRuleOperator)ro);
				stringRegexRules.put(ro.getPattern().pattern(),(RegexRuleOperator)ro);
			}
		}
	}
	
	public static void registerRuleOperatorScanPackage(Class target){
		Set<Class<?>> classes =  RuleOperatorScanner.getAppClasses(target.getPackage().getName());
		for(Class c:classes){
			if(((c.getModifiers()&1024)!=1024)&&((c.getModifiers()&16)!=16)&&((c.getModifiers()&512)!=512)&&RuleOperator.class.isAssignableFrom(c)){
				try {
					RuleOperator ro =  (RuleOperator) c.newInstance();
					registerRuleOperator(ro);
				} catch (InstantiationException e) {
					logger.warn("",e);
				} catch (IllegalAccessException e) {
					logger.warn("",e);
				}
			}
		}
	}
	public static void help(){
		System.out.println("stringRule list\t");
			Set<Entry<String, StringRuleOperator>> entrys = stringRules.entrySet();
			for(Entry<String,StringRuleOperator> en : entrys){
				String key = en.getKey();
				System.out.println("状态码:[ "+key+" ]\t"+stringRules.get(key));
			}
			System.out.println("regexRule list\t");
			Set<Entry<String, RegexRuleOperator>> regexEntrys = stringRegexRules.entrySet();
			for(Entry<String,RegexRuleOperator> en : regexEntrys){
			String key = en.getKey();
			System.out.println("状态码:[ "+key+" ]\t"+stringRegexRules.get(key));
			}
	}
}
