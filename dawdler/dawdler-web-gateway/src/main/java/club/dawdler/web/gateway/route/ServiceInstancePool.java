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
package club.dawdler.web.gateway.route;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jackson.song
 * @version V1.0
 * 
 * 网关服务实例池，维护服务名到可用实例地址列表。
 */
public class ServiceInstancePool {

	private static final ConcurrentHashMap<String, ServiceInstancePool> GROUPS = new ConcurrentHashMap<>();

	private final CopyOnWriteArrayList<String> instances = new CopyOnWriteArrayList<>();

	public enum Action {
		ACTION_ADD, ACTION_DEL
	}

	private ServiceInstancePool() {
	}

	public static ServiceInstancePool addGroup(String serviceName) {
		return GROUPS.computeIfAbsent(serviceName, k -> new ServiceInstancePool());
	}

	public static ServiceInstancePool getGroup(String serviceName) {
		return GROUPS.get(serviceName);
	}

	public List<String> getInstances() {
		return instances;
	}

	public void doChange(Action action, String address) {
		switch (action) {
		case ACTION_ADD:
			if (!instances.contains(address)) {
				instances.add(address);
			}
			break;
		case ACTION_DEL:
			instances.remove(address);
			break;
		default:
			break;
		}
	}

}
