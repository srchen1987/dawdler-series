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
package com.anywide.dawdler.cache.component.injector;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import com.anywide.dawdler.cache.CacheConfig;
import com.anywide.dawdler.cache.CacheManager;
import com.anywide.dawdler.cache.CacheManagerProvider;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title CacheConfigInjector.java
 * @Description 注入CacheConfig
 * @date 2023年7月30日
 * @email suxuan696@gmail.com
 */
@Order(1) 
public class CacheConfigInjector implements CustomComponentInjector {

	@Override
	public void inject(Class<?> type, Object target) throws Throwable {
		Collection<CacheManager> cacheManagers = CacheManagerProvider.getCacheManagers();
		for (CacheManager cacheManager : cacheManagers) {
			cacheManager.createCache((CacheConfig) target);
		}
	}

	@Override
	public Class<?>[] getMatchTypes() {
		return new Class<?>[] { CacheConfig.class };
	}

	@Override
	public Set<? extends Class<? extends Annotation>> getMatchAnnotations() {
		return null;
	}

}
