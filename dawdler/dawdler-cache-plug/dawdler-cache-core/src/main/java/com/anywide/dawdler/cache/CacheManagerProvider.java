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
package com.anywide.dawdler.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * CacheManager提供者,通过SPI实现加载
 */
public class CacheManagerProvider {
	private static final Map<String, CacheManager> CACHE_MANAGERS = new ConcurrentHashMap<>();
	static {
		ServiceLoader.load(CacheManager.class).forEach(cacheManager -> {
			CACHE_MANAGERS.put(cacheManager.cacheManagerName(), cacheManager);
		});
	}

	public static CacheManager getCacheManager(String cacheManagerName) {
		return CACHE_MANAGERS.get(cacheManagerName);
	}

	public static Collection<CacheManager> getCacheManagers() {
		return Collections.unmodifiableCollection(CACHE_MANAGERS.values());
	}

}
