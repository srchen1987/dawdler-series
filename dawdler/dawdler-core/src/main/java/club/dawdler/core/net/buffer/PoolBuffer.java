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
package club.dawdler.core.net.buffer;

import java.time.Duration;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author jackson.song
 * @version V1.0
 * Buffer池，极端情况才会使用到， 初始化了一些buffer，池的配置可以根据实际业务的使用情况进行调整
 */
public class PoolBuffer {
	private static final ConcurrentHashMap<Integer, PoolBuffer> POOL_BUFFERS = new ConcurrentHashMap<>();
	private static final TreeSet<Integer> ORDER = new TreeSet<>();

	static {
		addPool(1024 * 32);
		addPool(1024 * 64);
		addPool(1024 * 128);
		addPool(1024 * 256);
		addPool(1024 * 512);
	}

	private final GenericObjectPool<DawdlerByteBuffer> objectPool;

	private PoolBuffer(int capacity) {
		ByteBufferPooledFactory factory = new ByteBufferPooledFactory(capacity);
		GenericObjectPoolConfig<DawdlerByteBuffer> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setMinIdle(1);
		poolConfig.setMaxIdle(4);
		poolConfig.setMaxTotal(32);
		poolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(180000));
		objectPool = new GenericObjectPool<DawdlerByteBuffer>(factory, poolConfig);
	}

	public static void addPool(int capacity) {
		PoolBuffer poolBuffer = new PoolBuffer(capacity);
		PoolBuffer pre = POOL_BUFFERS.putIfAbsent(capacity, poolBuffer);
		if (pre != null) {
			poolBuffer.close();
		} else {
			ORDER.add(capacity);
		}
	}

	public static PoolBuffer selectPool(int capacity) {
		int key = 0;
		for (int num : ORDER) {
			if (num >= capacity) {
				key = num;
				break;
			}
		}
		if (key == 0) {
			return null;
		}
		return POOL_BUFFERS.get(key);
	}

	public DawdlerByteBuffer getByteBuffer() throws Exception {
		return objectPool.borrowObject();
	}

	public void release(DawdlerByteBuffer buffer) {
		objectPool.returnObject(buffer);
	}

	public void close() {
		objectPool.clear();
		objectPool.close();
	}
}
