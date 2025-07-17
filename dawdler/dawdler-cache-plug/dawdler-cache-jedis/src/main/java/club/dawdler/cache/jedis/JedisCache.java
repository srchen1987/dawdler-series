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
package club.dawdler.cache.jedis;

import club.dawdler.cache.Cache;
import club.dawdler.cache.CacheConfig;

/**
 * @author jackson.song
 * @version V1.0
 * JedisCache Jedis实现的Cache
 */
public class JedisCache implements Cache {

	private String name;

	private JedisInnerCache cache;

	public JedisCache(CacheConfig cacheConfig) throws Exception {
		this.name = cacheConfig.getName();
		cache = new JedisInnerCache(cacheConfig);
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
	public Object get(Object key) throws Exception {
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
	public void put(Object key, Object value) throws Exception {
		cache.put(key, value);
	}

	@Override
	public void remove(Object key) {
		cache.del(key);
	}

	@Override
	public void clear() {
		cache.delAll();
	}

}
