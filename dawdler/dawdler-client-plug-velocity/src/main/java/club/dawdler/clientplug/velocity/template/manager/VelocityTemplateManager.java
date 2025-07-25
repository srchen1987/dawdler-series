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
package club.dawdler.clientplug.velocity.template.manager;

import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author jackson.song
 * @version V1.0
 * velocity模板管理器
 */
public class VelocityTemplateManager implements TemplateManager {
	private static VelocityTemplateManager templateManager;
	private final VelocityEngine engine = new VelocityEngine();
	private String suffix;
	private String templatePath;

	private VelocityTemplateManager() {
	}

	public static synchronized VelocityTemplateManager getInstance() {
		if (templateManager == null) {
			templateManager = new VelocityTemplateManager();
		}
		return templateManager;
	}

	public Template getTemplate(String vmName) {
		if(templatePath != null){
			vmName = templatePath + vmName;
		}
		if (suffix!= null) {
			vmName = vmName + suffix;
		}
		return engine.getTemplate(vmName, "UTF-8");
	}

	public void init(Properties ps) {
		templatePath = ps.getProperty("template.path");
		suffix = ps.getProperty("template.suffix");
		engine.init(ps);
	}

}
