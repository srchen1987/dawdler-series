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
package com.anywide.dawdler.rabbitmq.resource;

import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;
import com.anywide.dawdler.rabbitmq.consumer.RabbitListenerInit;

/**
 * @author jackson.song
 * @version V1.0
 * rabbit资源释放类
 */
public class RabbitLifeCycle implements ComponentLifeCycle {

	@Override
	public void destroy() {
		RabbitListenerInit.closeAllConnections();
		AMQPConnectionFactory.shutdownAll();
	}

}
