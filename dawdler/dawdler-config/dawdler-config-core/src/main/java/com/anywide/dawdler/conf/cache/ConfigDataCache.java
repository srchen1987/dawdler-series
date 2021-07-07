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
package com.anywide.dawdler.conf.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ConfigDataCache.java
 * @Description 缓存配置中心中的数据和版本号
 * @date 2021年05月29日
 * @email suxuan696@gmail.com
 */
public class ConfigDataCache {

	private static Map<String, ConfigData> configDataCache = new ConcurrentHashMap<>();

	public static ConfigData getConfigData(String path) {
		return configDataCache.get(path);
	}

	public static void addConfigData(String path, String content, long version) {
		configDataCache.put(path, new ConfigData(content, version));
	}

	public static class ConfigData {
		private String content;
		private long version;

		public ConfigData(String content, long version) {
			this.content = content;
			this.version = version;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public long getVersion() {
			return version;
		}

		public void setVersion(long version) {
			this.version = version;
		}
	}

}
