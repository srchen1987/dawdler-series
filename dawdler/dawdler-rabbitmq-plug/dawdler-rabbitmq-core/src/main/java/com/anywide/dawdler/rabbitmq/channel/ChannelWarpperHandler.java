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
package com.anywide.dawdler.rabbitmq.channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title ChannelWarpperHandler.java
 * @Description Channel动态代理类
 * @date 2021年4月11日
 * @email suxuan696@gmail.com
 */
public class ChannelWarpperHandler implements InvocationHandler {
	private static final Logger logger = LoggerFactory.getLogger(ChannelWarpperHandler.class);

	private Channel target;
	private LinkedList<Channel> channels;
	private Semaphore semaphore;
	private GenericObjectPool<Connection> genericObjectPool;

	public ChannelWarpperHandler(Channel target, GenericObjectPool<Connection> genericObjectPool,
			LinkedList<Channel> channels, Semaphore semaphore) {
		this.target = target;
		this.channels = channels;
		this.semaphore = semaphore;
		this.genericObjectPool = genericObjectPool;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String name = method.getName();
		if (name.equals("close")) {
			synchronized (channels) {
				channels.addLast((Channel) proxy);
			}
			semaphore.release();
			return null;
		} else if (name.equals("basicConsume")) {
			return method.invoke(target, args);
		} else if (name.equals("hashCode")) {
			return method.invoke(target, args);
		} else if (name.equals("finalize")) {
			return method.invoke(target, args);
		} else if (name.equals("toString")) {
			return method.invoke(target, args);
		} else if (name.equals("equals")) {
			return method.invoke(target, args);
		} else if (name.equals("clone")) {
			return method.invoke(target, args);
		} else if (name.startsWith("tx")) {
			return method.invoke(target, args);
		} else {
			handleAbnormalDisconnection();
			return method.invoke(target, args);
		}
	}

	public void handleAbnormalDisconnection() {
		while (!target.isOpen()) {
			try {
				target = genericObjectPool.borrowObject().createChannel();
			} catch (Exception e) {
				logger.error("", e);
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
	}

}
