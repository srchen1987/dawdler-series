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
package com.anywide.dawdler.clientplug.web.plugs;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.util.DawdlerTool;
/**
 * 
 * @Title:  PlugFactory.java   
 * @Description:    插件工厂   
 * @author: jackson.song    
 * @date:   2007年04月21日   
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class PlugFactory {
	private static Logger logger = LoggerFactory.getLogger(PlugFactory.class);

	public static void initFactory(ServletContext servletContext) {
		String filepath = DawdlerTool.getcurrentPath()+"displayplugs.properties";
		Properties ps = new Properties();
		File file = new File(filepath);
		if(file.isFile()){
			FileInputStream fin=null;
			try {
				fin = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				logger.error("",e);
			}
			try {
				ps.load(fin);
			} catch (IOException e) {
				logger.error("",e);
			}finally{
				if(fin!=null)
					try {
						fin.close();
					} catch (IOException e) {
						logger.error("",e);
					}
			}
		}
		String defaultplug = null;
		Object defaultplugobj = ps.get("default");
		if(defaultplugobj!=null){
			defaultplug=defaultplugobj.toString();
		}else{
			defaultplug = "velocity";
		}
		Set<Object> set = ps.keySet();
		for (Object o : set) {
			String name = o.toString();
			if(name.equals("default")){
				continue;
			}
			String className = ps.get(name).toString();
			try {
				Class c = Class.forName(className);
				if (!DisplayPlug.class.isAssignableFrom(c)) {
					System.err.println("warn\t" + className
							+ "\tmust implements DisplayPlug!");
					continue;
				}
				try {
					DisplayPlug plugso = (DisplayPlug)c.getConstructor(ServletContext.class).newInstance(servletContext);
					displayPlugs.put(name, plugso);
				} catch (Exception e) {
					logger.error("",e);
				}
			} catch (ClassNotFoundException e) {
				logger.error("",e);
				continue;
			}
		}
		if(displayPlugs.get(defaultplug)==null)
		displayPlugs.put("default",new VelocityDisplayPlug(servletContext));
		else{
			displayPlugs.put("default",displayPlugs.get(defaultplug));
		}
	}
	
	
	private static java.util.concurrent.ConcurrentMap<String,DisplayPlug> displayPlugs = new ConcurrentHashMap<String, DisplayPlug>();
	public static DisplayPlug getDisplayPlug(String key){
		DisplayPlug displayPlug = displayPlugs.get(key);
		if(displayPlug==null)return getDisplayPlug("default");
		return displayPlug;
	}
	
	
}

