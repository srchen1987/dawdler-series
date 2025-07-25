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
package club.dawdler.clientplug.web.plugs;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;

import club.dawdler.clientplug.web.annotation.RequestMapping.ViewType;

/**
 * @author jackson.song
 * @version V1.0
 * 插件工厂
 */
public class PlugFactory {
	private PlugFactory() {
	}

	private static final ConcurrentMap<String, DisplayPlug> DISPLAY_PLUGS = new ConcurrentHashMap<>();

	public static void initFactory(ServletContext servletContext) {
		ServiceLoader.load(DisplayPlug.class).forEach(displayPlug -> {
			displayPlug.init(servletContext);
			DISPLAY_PLUGS.put(displayPlug.plugName(), displayPlug);
		});
	}

	public static DisplayPlug getDisplayPlug(String key) {
		DisplayPlug displayPlug = DISPLAY_PLUGS.get(key);
		if (displayPlug == null) {
			return getDisplayPlug(ViewType.json.toString());
		}
		return displayPlug;
	}

}
