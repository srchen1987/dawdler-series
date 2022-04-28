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
package com.anywide.dawdler.clientplug.redis.fire;

import com.anywide.dawdler.clientplug.annotation.Controller;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.web.TransactionController;
import com.anywide.dawdler.clientplug.web.interceptor.HandlerInterceptor;
import com.anywide.dawdler.clientplug.web.listener.WebContextListener;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.redis.JedisOperatorFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisClassLoaderFire.java
 * @Description 客户端加载类通知类，初始化各种监听器 拦截器 controller,注入JedisOperator
 * @date 2022年4月16日
 * @email suxuan696@gmail.com
 */
@Order(1)
public class JedisClassLoaderFire implements RemoteClassLoaderFire {

	@Override
	public void onLoadFire(Class<?> clazz, Object target, byte[] classCodes) throws Throwable{
		initListener(clazz, target);
		initInterceptor(clazz, target);
		initMapping(clazz, target, classCodes);
	}

	@Override
	public void onRemoveFire(Class<?> clazz) {
	}

	private void initListener(Class<?> clazz, Object target) throws Throwable {
		if (WebContextListener.class.isAssignableFrom(clazz)) {
			JedisOperatorFactory.initField(target, clazz);
		}
	}

	private void initInterceptor(Class<?> clazz, Object target) throws Throwable {
		if (HandlerInterceptor.class.isAssignableFrom(clazz)) {
			JedisOperatorFactory.initField(target, clazz);
		}
	}

	private void initMapping(Class<?> clazz, Object target, byte[] classCodes) throws Throwable {
		if (clazz.isInterface())
			return;
		if (clazz.getAnnotation(Controller.class) != null || TransactionController.class.isAssignableFrom(clazz)) {
			JedisOperatorFactory.initField(target, clazz);
		}
	}
	 
}
