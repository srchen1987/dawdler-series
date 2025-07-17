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
package club.dawdler.rabbitmq.channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import com.rabbitmq.client.Channel;

/**
 * @author jackson.song
 * @version V1.0
 * Channel动态代理类
 */
public class ChannelWrapperHandler implements InvocationHandler {

	private Channel target;
	private LinkedList<Channel> channels;
	private Semaphore semaphore;
	public ChannelWrapperHandler(Channel target,
			LinkedList<Channel> channels, Semaphore semaphore) {
		this.target = target;
		this.channels = channels;
		this.semaphore = semaphore;
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
		} 
		return method.invoke(target, args);
	}

}
