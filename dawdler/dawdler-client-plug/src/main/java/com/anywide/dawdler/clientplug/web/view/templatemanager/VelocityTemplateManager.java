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
package com.anywide.dawdler.clientplug.web.view.templatemanager;
import java.util.Properties;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

/**
 * 
 * @Title:  VelocityTemplateManager.java   
 * @Description:    TODO   
 * @author: jackson.song    
 * @date:   2009年04月19日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class VelocityTemplateManager implements TemplateManager{
	private static VelocityTemplateManager templateManager;
	public static synchronized VelocityTemplateManager getInstance(){
		if(templateManager==null)templateManager = new VelocityTemplateManager();
		return templateManager;
	}
	private VelocityTemplateManager() {
	}
	private VelocityEngine engine = new VelocityEngine();
	public Template getTemplate(String vmname){
		return engine.getTemplate(vmname, "UTF-8");
	}
	public void init(Properties ps){
		engine.init(ps);
	} 

}
