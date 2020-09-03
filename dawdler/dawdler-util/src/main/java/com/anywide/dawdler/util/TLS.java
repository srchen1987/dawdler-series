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
 * 
 * @Title:  TLS.java
 * @Description:    TLS工具类   
 * @author: jackson.song    
 * @date:   2007年10月09日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class TLS {
	private static ThreadLocal<Map<Object, Object>> threadLocal = new ThreadLocal<Map<Object, Object>>();
	public static void set(Object key, Object value) {
		Map<Object, Object> map = createIfNotExist();
		map.put(key, value);
	}
	 
	public static Object get(Object key) {
		Map<Object, Object> map = createIfNotExist();
		return map.get(key);
	}
	
	
	public static Object remove(Object key) {
		Map<Object, Object> map = threadLocal.get();
		if (map != null) {
			return map.remove(key);
		}
		return null;
	}
	
	
	public static Object clear() {
		Map<Object, Object> map = threadLocal.get();
		if (map != null) {
				map.clear();
		}
		threadLocal.remove();
		return null;
	}
	
	private static Map<Object, Object> createIfNotExist() {
		Map<Object, Object> map = threadLocal.get();
		if (map == null) {
			map = new HashMap<Object,Object>();
			threadLocal.set(map);
		}
		return map;
	}
}
