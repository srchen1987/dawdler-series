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
package club.dawdler.clientplug.web.handler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.clientplug.web.annotation.RequestMapping.RequestMethod;

/**
 * @author jackson.song
 * @version V1.0
 * 同一path下的映射聚合容器,按RequestMethod区分不同的RequestUrlData。
 * method为空数组的mapping表示匹配任意method,单独存储,匹配优先级低于具体method。
 * 同一path下"任意method"与"具体method"不能共存,后注册者视为冲突被拒绝。
 */
public class PathMappingData {

	private final Map<RequestMethod, RequestUrlData> methodMappings = new ConcurrentHashMap<>();

	private volatile RequestUrlData anyMethodMapping;

	public synchronized RequestUrlData put(RequestUrlData requestUrlData) {
		RequestMethod[] requestMethods = requestUrlData.getRequestMapping().method();
		if (requestMethods.length == 0) {
			if (anyMethodMapping != null) {
				return anyMethodMapping;
			}
			if (!methodMappings.isEmpty()) {
				return methodMappings.values().iterator().next();
			}
			anyMethodMapping = requestUrlData;
			return null;
		}
		for (RequestMethod requestMethod : requestMethods) {
			RequestUrlData pre = methodMappings.get(requestMethod);
			if (pre != null) {
				return pre;
			}
		}
		for (RequestMethod requestMethod : requestMethods) {
			methodMappings.put(requestMethod, requestUrlData);
		}
		return null;
	}

	public synchronized boolean remove(RequestUrlData requestUrlData) {
		Method method = requestUrlData.getMethod();
		boolean removed = false;
		if (anyMethodMapping != null && anyMethodMapping.getMethod() == method) {
			anyMethodMapping = null;
			removed = true;
		}
		Iterator<Entry<RequestMethod, RequestUrlData>> it = methodMappings.entrySet().iterator();
		while (it.hasNext()) {
			if (it.next().getValue().getMethod() == method) {
				it.remove();
				removed = true;
			}
		}
		return removed;
	}

	public boolean isEmpty() {
		return anyMethodMapping == null && methodMappings.isEmpty();
	}

	public RequestUrlData match(String httpMethod) {
		try {
			RequestMethod requestMethod = RequestMethod.valueOf(httpMethod);
			RequestUrlData data = methodMappings.get(requestMethod);
			if (data != null) {
				return data;
			}
		} catch (IllegalArgumentException e) {
		}
		return anyMethodMapping;
	}

	public Collection<RequestUrlData> allRequestUrlData() {
		Collection<RequestUrlData> result = new ArrayList<>(methodMappings.values());
		if (anyMethodMapping != null) {
			result.add(anyMethodMapping);
		}
		return result;
	}

}
