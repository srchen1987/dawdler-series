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
package com.anywide.dawdler.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jackson.song
 * @version V1.0
 * @Title TLS.java
 * @Description TLS工具类
 * @date 2007年10月09日
 * @email suxuan696@gmail.com
 */
public class TLS {
	private final static ThreadLocal<Map<Object, Object>> THREAD_LOCAL = new ThreadLocal<Map<Object, Object>>();

	public static void set(Object key, Object value) {
		Map<Object, Object> map = createIfNotExist();
		map.put(key, value);
	}

	public static Object get(Object key) {
		Map<Object, Object> map = createIfNotExist();
		return map.get(key);
	}

	public static Object remove(Object key) {
		Map<Object, Object> map = THREAD_LOCAL.get();
		if (map != null) {
			return map.remove(key);
		}
		return null;
	}

	public static void clear() {
		Map<Object, Object> map = THREAD_LOCAL.get();
		if (map != null) {
			map.clear();
		}
		THREAD_LOCAL.remove();
	}

	private static Map<Object, Object> createIfNotExist() {
		Map<Object, Object> map = THREAD_LOCAL.get();
		if (map == null) {
			map = new HashMap<Object, Object>();
			THREAD_LOCAL.set(map);
		}
		return map;
	}
}
