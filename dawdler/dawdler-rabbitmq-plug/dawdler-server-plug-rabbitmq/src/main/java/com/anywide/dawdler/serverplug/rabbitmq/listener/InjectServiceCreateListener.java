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
package com.anywide.dawdler.serverplug.rabbitmq.listener;

import com.anywide.dawdler.core.annotation.Order;
import com.anywide.dawdler.rabbitmq.consumer.RabbitListenerInit;
import com.anywide.dawdler.rabbitmq.provider.RabbitProviderFactory;
import com.anywide.dawdler.server.context.DawdlerContext;
import com.anywide.dawdler.server.service.listener.DawdlerServiceCreateListener;

/**
 * @author jackson.song
 * @version V1.0
 * @Title InjectServiceCreateListener.java
 * @Description 实现RabbitProvider的注入
 * @date 2022年4月15日
 * @email suxuan696@gmail.com
 */
@Order(Integer.MAX_VALUE)
public class InjectServiceCreateListener implements DawdlerServiceCreateListener {

	@Override
	public void create(Object service, boolean single, DawdlerContext context) throws Throwable {
		Class<?> serviceType = service.getClass();
		RabbitProviderFactory.initField(service, serviceType);
		RabbitListenerInit.initRabbitListener(service, serviceType);
	}

}
