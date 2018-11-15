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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @Title:  RegexRules.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2007年07月21日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class RegexRules {
//	private static Map<String,Pattern> regexRules = Collections.synchronizedMap(new HashMap<String,Pattern>());
//	private static Map<String,String> explains = Collections.synchronizedMap(new HashMap<String,String>());
	private static Map<String,Pattern> regexRules =new ConcurrentHashMap<String, Pattern>();
	private static Map<String,String> explains = new ConcurrentHashMap<String, String>();
	private static Logger logger = LoggerFactory.getLogger(RegexRules.class);
	public static Pattern getPatternRule(String key){
		return regexRules.get(key);
	}
	
	public static void  registerRule(String key,String regex,String explain){
		if(regexRules.containsKey(key)){
			logger.warn(key+" was existed!");
			return;
		}
		Pattern pattern= Pattern.compile(regex);
		regexRules.put(key,pattern);
		explains.put(key,explain+"  状态码：[ "+key+" ] \t pattern is "+regexRules.toString());
	}
	public static void  registerRule(String key,Pattern pattern,String explain){
		if(regexRules.containsKey(key)){
			logger.warn(key+" was existed!");
			return;
		}
		regexRules.put(key,pattern);
		explains.put(key,explain+"  状态码：[ "+key+" ] \t pattern is "+regexRules.toString());
	}
	public static void help(){
		System.out.println(".");
		Set<Entry<String,String>> entrys = explains.entrySet();
		System.out.println(entrys);
		for(Entry<String,String> en : entrys){
			String key = en.getKey();
			System.out.println(key+"\t"+explains.get(key));
		}
	}
	public static void main(String[] args) {
		help();
	}
}

