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
package com.anywide.dawdler.core.db.sub.rule;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author jackson.song
 * @version V1.0
 * 一致性hash分库分表规则
 */
public class ConsistentHashSubRule implements SubRule {
	private int numberOfReplicas = 3;

	private List<String> nodes;

	private final SortedMap<Long, String> circle = new TreeMap<>();

	@Override
	public String getRuleSubfix(Object key) {
		return getNode(key.toString());
	}

	private long hash(String key) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = md.digest(key.getBytes());
			return bytesToLong(hashBytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not available.", e);
		}
	}

	private long bytesToLong(byte[] bytes) {
		long value = 0;
		for (int i = 0; i < Math.min(8, bytes.length); i++) {
			value = (value << 8) | (bytes[i] & 0xFF);
		}
		return value;
	}

	public void addNode(String node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			String replicaKey = node + i;
			long hashValue = hash(replicaKey);
			circle.put(hashValue, node);
		}
	}

	public void removeNode(String node) {
		for (int i = 0; i < numberOfReplicas; i++) {
			String replicaKey = node + i;
			long hashValue = hash(replicaKey);
			circle.remove(hashValue);
		}
	}

	public String getNode(String key) {
		if (circle.isEmpty()) {
			return null;
		}
		long hashValue = hash(key);
		if (!circle.containsKey(hashValue)) {
			SortedMap<Long, String> tailMap = circle.tailMap(hashValue);
			hashValue = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
		}
		return circle.get(hashValue);
	}

	public Map<Long, String> getCircle() {
		return circle;
	}

	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	public void setNumberOfReplicas(int numberOfReplicas) {
		this.numberOfReplicas = numberOfReplicas;
	}

	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		for (String node : nodes) {
			addNode(node);
		}
		this.nodes = nodes;
	}

}
