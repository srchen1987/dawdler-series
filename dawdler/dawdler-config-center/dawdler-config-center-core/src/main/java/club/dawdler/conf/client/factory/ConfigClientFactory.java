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
package club.dawdler.conf.client.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import club.dawdler.conf.client.ConfigClient;

/**
 * @author jackson.song
 * @version V1.0
 * ConfigClient的工厂，通过SPI初始化之后会加入到此工厂中
 */
public class ConfigClientFactory {
	private static Map<String, ConfigClient> clients = new ConcurrentHashMap<String, ConfigClient>();

	public static boolean addClient(ConfigClient client) {
		return clients.putIfAbsent(client.type(), client) == null;
	}

	public static ConfigClient getClient(String type) {
		return clients.get(type);
	}
}
