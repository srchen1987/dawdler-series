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
package com.anywide.dawdler.client.cluster.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.anywide.dawdler.client.cluster.AbstractLoadBalance;
import com.anywide.dawdler.core.bean.RequestBean;
/**
 * @author jackson.song
 * @version V1.0
 * @Title RoundRobinLoadBalance.java
 * @Description RoundRobin方式负载均衡实现
 * @date 2019年08月16日
 * @email suxuan696@gmail.com
 */
public class RoundRobinLoadBalance<T> extends AbstractLoadBalance<T, Integer> {
	AtomicInteger index = new AtomicInteger(0);

	public RoundRobinLoadBalance() {
		super("roundRobin");
	}

	@Override
	public T doSelect(RequestBean request, List<T> connections) {
		int size = connections.size();
		return connections.get(Math.abs(index.get()) % size);
	}

	@Override
	public RoundRobinLoadBalance<T> preSelect(RequestBean request) {
		index.getAndIncrement();
		return this;
	}

	@Override
	public Integer getKey() {
		return index.get();
	}
}
