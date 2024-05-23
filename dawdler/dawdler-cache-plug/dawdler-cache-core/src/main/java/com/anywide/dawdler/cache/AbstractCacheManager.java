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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jackson.song
 * @version V1.0
 * CacheManager抽象类,一般扩展CacheManager需要继承此类
 */
public abstract class AbstractCacheManager implements CacheManager {
	protected final Map<String, Cache> cacheMap = new ConcurrentHashMap<>();

	@Override
	public Cache getCache(String name) {
		return cacheMap.get(name);
	}

	@Override
	public Collection<String> getAllCacheNames() {
		return Collections.unmodifiableSet(cacheMap.keySet());
	}

	@Override
	public void createCache(CacheConfig cacheConfig) throws Exception {
		if (cacheMap.get(cacheConfig.getName()) != null) {
			return;
		}
		synchronized (cacheMap) {
			Cache cache = cacheMap.get(cacheConfig.getName());
			if (cache == null) {
				cache = createCacheNative(cacheConfig);
				Cache preCache = cacheMap.putIfAbsent(cacheConfig.getName(), cache);
				if (preCache != null) {
					cache.clear();
				}
			}
		}
	}

	@Override
	public String cacheManagerName() {
		return getClass().getSimpleName();
	}

	protected abstract Cache createCacheNative(CacheConfig cacheConfig) throws Exception;

}
