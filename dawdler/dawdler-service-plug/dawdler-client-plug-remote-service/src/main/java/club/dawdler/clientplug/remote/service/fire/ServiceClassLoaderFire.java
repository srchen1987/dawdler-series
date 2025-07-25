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
package club.dawdler.clientplug.remote.service.fire;

import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.core.annotation.Order;
import club.dawdler.remote.service.injector.RemoteServiceInjector;

/**
 * @author jackson.song
 * @version V1.0
 * 注入远程调用service(替换老版本club.dawdler.clientplug.web.fire.WebComponentClassLoaderFire注入)
 */
@Order(0)
public class ServiceClassLoaderFire implements RemoteClassLoaderFire {
	@Override
	public void onLoadFire(Class<?> clazz, Object target) throws Throwable {
		RemoteServiceInjector.injectRemoteService(target);
	}

}
