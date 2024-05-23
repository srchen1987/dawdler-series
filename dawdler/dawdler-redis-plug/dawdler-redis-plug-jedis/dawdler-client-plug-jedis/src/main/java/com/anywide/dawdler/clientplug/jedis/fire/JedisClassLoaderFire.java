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
package com.anywide.dawdler.clientplug.jedis.fire;

import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.jedis.JedisOperatorFactory;
import com.anywide.dawdler.jedis.lock.JedisDistributedLockHolderFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 客户端加载类通知类，初始化各种监听器 拦截器 controller,注入JedisOperator,JedisDistributedLock
 */
@Order(1)
public class JedisClassLoaderFire implements RemoteClassLoaderFire {

	@Override
	public void onLoadFire(Class<?> clazz, Object target) throws Throwable {
		JedisOperatorFactory.initField(target, clazz);
		JedisDistributedLockHolderFactory.initField(target, clazz);
	}

}
