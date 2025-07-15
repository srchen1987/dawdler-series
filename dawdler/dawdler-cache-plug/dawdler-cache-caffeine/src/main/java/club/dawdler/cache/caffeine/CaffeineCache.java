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
package club.dawdler.cache.caffeine;

import club.dawdler.cache.Cache;
import club.dawdler.cache.CacheConfig;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * @author jackson.song
 * @version V1.0
 * CaffeineCache 咖啡因实现的Cache
 */
public class CaffeineCache implements Cache {

	private String name;

	private LoadingCache<Object, Object> cache;

	public CaffeineCache(CacheConfig cacheConfig) {
		this.name = cacheConfig.getName();
		Caffeine<Object, Object> caffeine = Caffeine.newBuilder().maximumSize(cacheConfig.getMaxSize());
		if (cacheConfig.getExpireAfterAccessDuration() != null) {
			caffeine.expireAfterAccess(cacheConfig.getExpireAfterAccessDuration());
		}
		if (cacheConfig.getExpireAfterWriteDuration() != null) {
			caffeine.expireAfterWrite(cacheConfig.getExpireAfterWriteDuration());
		}
		cache = caffeine.build(this::createExpensiveGraph);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getCacheImpl() {
		return cache;
	}

	@Override
	public Object get(Object key) {
		return cache.get(key);
	}

	@Override
	public <T> T get(Object key, Class<T> type) throws Exception {
		Object value = cache.get(key);
		if (value != null && type != null) {
			if (!type.isInstance(value)) {
				throw new IllegalStateException(
						"Cached value is not of required type [" + type.getName() + "]: " + value);
			}
			return type.cast(value);
		}
		return null;
	}

	@Override
	public void put(Object key, Object value) {
		cache.put(key, value);
	}

	@Override
	public void remove(Object key) {
		cache.invalidate(key);
	}

	@Override
	public void clear() {
		cache.invalidateAll();
	}

	private Object createExpensiveGraph(Object key) {
		return null;
	}
}
