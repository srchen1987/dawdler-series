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
package club.dawdler.client;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jackson.song
 * @version V1.0
 * 连接管理器
 */
public class ConnectManager {
	private final ConcurrentHashMap<SocketAddress, AtomicInteger> disconnAddress = new ConcurrentHashMap<>();

	public Set<SocketAddress> getDisconnectAddress() {
		return disconnAddress.keySet();
	}

	public void addDisconnectAddress(SocketAddress address) {
		AtomicInteger num = disconnAddress.putIfAbsent(address, new AtomicInteger(1));
		if (num != null) {
			num.getAndIncrement();
		}
	}

	public AtomicInteger removeDisconnect(SocketAddress address) {
		return disconnAddress.remove(address);
	}
}
