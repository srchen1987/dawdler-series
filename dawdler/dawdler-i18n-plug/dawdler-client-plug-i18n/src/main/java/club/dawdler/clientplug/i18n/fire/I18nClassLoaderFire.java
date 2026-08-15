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
package club.dawdler.clientplug.i18n.fire;

import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.core.annotation.Order;
import club.dawdler.i18n.I18nOperatorFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 客户端类加载通知类 在Web客户端环境中，当类被加载时自动为监听器、拦截器和控制器注入I18nOperator实例。
 */
@Order(1)
public class I18nClassLoaderFire implements RemoteClassLoaderFire {

	/**
	 * 类加载通知方法，在类被加载时自动调用
	 * 
	 * 为加载的类实例注入I18nOperator，使其能够使用国际化功能。
	 * 
	 * @param clazz  被加载的类
	 * @param target 类实例对象
	 * @throws Throwable 注入过程中可能抛出的异常
	 */
	@Override
	public void onLoadFire(Class<?> clazz, Object target) throws Throwable {
		I18nOperatorFactory.initField(target, clazz);
	}

}