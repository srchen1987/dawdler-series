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
package club.dawdler.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author jackson.song
 * @version V1.0
 * YAML 工具类 支持统一配置中心配置
 */
public class YAMLUtil {

	public static <T> T loadYAMLIfNotExistLoadConfigCenter(String fileName, Class<T> clazz) throws Exception {
		T t = null;
		try {
			t = loadActiveProfileIfNotExistUseDefaultYAML(fileName, clazz);
		} catch (Exception e) {
			try {
				Class<?> configMappingDataCacheClass = Thread.currentThread().getContextClassLoader()
						.loadClass("club.dawdler.conf.cache.ConfigMappingDataCache");
				Method method = configMappingDataCacheClass.getMethod("getMappingDataCache", String.class, Class.class);
				t = (T) method.invoke(null,
						getYAMLPathOrDefault(fileName), clazz);
				if (t == null) {
					t = (T) method.invoke(null, fileName, clazz);
					if (t == null) {
						throw e;
					}
				}

			} catch (ClassNotFoundException ignore) {
				throw e;
			}
		}
		return t;
	}

	public static <T> T loadYAML(String fileName, Class<T> clazz) throws Exception {
		InputStream inStream = null;
		StringBuilder sb = new StringBuilder();
		try {
			inStream = DawdlerTool.getResourceFromClassPath(fileName, ".yml");
			if (inStream == null) {
				throw new FileNotFoundException("not found " + fileName + ".yml in classPath!");
			}
			sb = new StringBuilder();
			byte[] b = new byte[1024];
			int len = -1;
			while ((len = inStream.read(b)) != -1) {
				sb.append(new String(b, 0, len));
			}
		} finally {
			if (inStream != null) {
				inStream.close();
			}
		}
		String content;
		if (ConfigContentDecryptor.useDecrypt()) {
			content = ConfigContentDecryptor.decryptAndReplaceTag(sb.toString());
		} else {
			content = sb.toString();
		}

		return YAMLMapperFactory.getYAMLMapper().readValue(content, clazz);
	}

	private static String getYAMLPathOrDefault(String fileName) {
		String activeProfile = System.getProperty("dawdler.profiles.active");
		if (activeProfile != null) {
			return fileName + "-" + activeProfile;
		}
		return fileName;
	}

	public static <T> T loadActiveProfileIfNotExistUseDefaultYAML(String fileName, Class<T> clazz)
			throws Exception {
		try {
			return loadYAML(getYAMLPathOrDefault(fileName), clazz);
		} catch (Exception e) {
			return loadYAML(fileName, clazz);
		}
	}

}
