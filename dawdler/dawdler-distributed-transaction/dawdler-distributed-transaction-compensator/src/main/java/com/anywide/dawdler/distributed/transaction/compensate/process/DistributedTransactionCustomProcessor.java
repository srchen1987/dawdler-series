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
package com.anywide.dawdler.distributed.transaction.compensate.process;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.client.ServiceFactory;
import com.anywide.dawdler.distributed.transaction.context.DistributedTransactionContext;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DistributedTransactionCustomProcessor.java
 * @Description 客户端处理者抽象类，补偿器中的实现是通过SPI接入的，不同服务可以继承此类，同时注入服务，具体参考分布式事务的demo
 * @date 2021年4月17日
 * @email suxuan696@gmail.com
 */
public abstract class DistributedTransactionCustomProcessor {
	private static Logger logger = LoggerFactory.getLogger(DistributedTransactionCustomProcessor.class);
	private static Map<String, DistributedTransactionCustomProcessor> processorInstances = new HashMap<>();
	static {
		ServiceLoader<DistributedTransactionCustomProcessor> processors = ServiceLoader
				.load(DistributedTransactionCustomProcessor.class);
		processors.forEach(processor -> {
			String action = processor.action;
			ServiceFactory.injectRemoteService(processor.getClass(), processor, null);
			if (processorInstances.containsKey(action)) {
				logger.error(
						action + " already exists in " + processorInstances.get(action).getClass().getName() + "!");
			} else {
				processorInstances.put(action, processor);
			}
		});
	}
	protected String action;

	public DistributedTransactionCustomProcessor(String action) {
		this.action = action;
	}

	public abstract boolean process(DistributedTransactionContext context, String status);

	public static DistributedTransactionCustomProcessor getProcessor(String action) {
		return processorInstances.get(action);
	}
}
