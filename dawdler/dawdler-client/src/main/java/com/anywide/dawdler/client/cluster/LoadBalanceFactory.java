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
package com.anywide.dawdler.client.cluster;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
/**
 * @author jackson.song
 * @version V1.0
 * @Title LoadBalanceFactory.java
 * @Description 负载均衡工厂
 * @date 2019年08月16日
 * @email suxuan696@gmail.com
 */
public class LoadBalanceFactory<T, K> {
	private final static Map<String, LoadBalance> loadBalances = new HashMap<>();
	static {
		ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
		loader.forEach(LoadBalanceFactory::addLoadBalance);
	}

	public static <T, K> LoadBalance<T, K> getLoadBalance(String name) {
		LoadBalance<T, K> loadBalance = loadBalances.get(name);
		return loadBalance;
	}

	static void addLoadBalance(LoadBalance loadBalance) {
		loadBalances.put(loadBalance.getName(), loadBalance);
	}
}
