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
 * 负载均衡工厂
 */
public class LoadBalanceFactory<T, K> {
	@SuppressWarnings("rawtypes")
	private static final Map<String, LoadBalance> LOAD_BALANCES = new HashMap<>();
	static {
		@SuppressWarnings("rawtypes")
		ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
		loader.forEach(LoadBalanceFactory::addLoadBalance);
	}

	public static <T, K> LoadBalance<T, K> getLoadBalance(String name) {
		@SuppressWarnings("unchecked")
		LoadBalance<T, K> loadBalance = LOAD_BALANCES.get(name);
		return loadBalance;
	}

	static void addLoadBalance(@SuppressWarnings("rawtypes") LoadBalance loadBalance) {
		LOAD_BALANCES.put(loadBalance.getName(), loadBalance);
	}
}
