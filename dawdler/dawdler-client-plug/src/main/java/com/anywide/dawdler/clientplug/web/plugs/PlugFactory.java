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

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import com.anywide.dawdler.clientplug.annotation.RequestMapping.ViewType;

/**
 * @author jackson.song
 * @version V1.0
 * @Title PlugFactory.java
 * @Description 插件工厂
 * @date 2007年4月21日
 * @email suxuan696@gmail.com
 */
public class PlugFactory {
	private static final java.util.concurrent.ConcurrentMap<String, DisplayPlug> displayPlugs = new ConcurrentHashMap<>();
	public static void initFactory(ServletContext servletContext) {
		ServiceLoader.load(DisplayPlug.class).forEach(displayPlug -> {
			displayPlug.init(servletContext);
			displayPlugs.put(displayPlug.plugName(), displayPlug);
		});
	}

	public static DisplayPlug getDisplayPlug(String key) {
		DisplayPlug displayPlug = displayPlugs.get(key);
		if (displayPlug == null)
			return getDisplayPlug(ViewType.json.toString());
		return displayPlug;
	}

}
