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
package com.anywide.dawdler.distributed.transaction.compensate.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.distributed.transaction.compensate.message.MessageConsumer;
import com.anywide.dawdler.distributed.transaction.compensate.message.amqp.AmqpConsumer;
import com.anywide.dawdler.distributed.transaction.compensate.timers.CompensationTimer;
import com.anywide.dawdler.distributed.transaction.release.ResourceReleaser;

/**
 *
 * @Title WebListener2StartCompensator.java
 * @Description 补偿模块的监听器 启动初始化定时任务，销毁时释放资源
 * @author jackson.song
 * @date 2021年4月17日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
@WebListener
public class WebListener2StartCompensator implements ServletContextListener {
	private static Logger logger = LoggerFactory.getLogger(AmqpConsumer.class);
	private MessageConsumer messageConsumer;
	private CompensationTimer compensationTimer;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		messageConsumer = new AmqpConsumer();
		try {
			messageConsumer.start();
			compensationTimer = new CompensationTimer(messageConsumer.getTransactionRepository());
			compensationTimer.start();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		compensationTimer.shutdown();
		messageConsumer.shutdown();
		ResourceReleaser.release();

	}

}
