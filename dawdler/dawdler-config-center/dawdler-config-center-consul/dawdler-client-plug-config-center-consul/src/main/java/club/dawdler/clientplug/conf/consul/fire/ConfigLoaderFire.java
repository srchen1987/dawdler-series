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
package club.dawdler.clientplug.conf.consul.fire;

import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.conf.Refresher;
import club.dawdler.conf.cache.PathMappingTargetCache;
import club.dawdler.core.annotation.Order;

/**
 * @author jackson.song
 * @version V1.0
 * 获取远程类加载时触发通知，实现动态注入config
 */
@Order(1)
public class ConfigLoaderFire implements RemoteClassLoaderFire {

	@Override
	public void onLoadFire(Class<?> clazz, Object target) throws Throwable {
		Refresher.refreshAllConfig(target);
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
		PathMappingTargetCache.removeMappingByTargetClass(clazz);
	}

}
