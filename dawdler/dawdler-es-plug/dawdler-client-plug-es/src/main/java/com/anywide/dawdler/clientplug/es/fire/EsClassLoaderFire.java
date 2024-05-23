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
package com.anywide.dawdler.clientplug.es.fire;

import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.es.restclient.EsOperatorFactory;

/**
 * @author jackson.song
 * @version V1.0
 * 客户端加载类通知类，初始化各种监听器 拦截器 controller,注入EsOperator
 */
@Order(1)
public class EsClassLoaderFire implements RemoteClassLoaderFire {

	@Override
	public void onLoadFire(Class<?> clazz, Object target) throws Throwable {
		EsOperatorFactory.initField(target, clazz);
	}

}
