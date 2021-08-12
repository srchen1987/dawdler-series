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
package com.anywide.dawdler.distributed.transaction.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.core.serializer.SerializeDecider;
import com.anywide.dawdler.distributed.transaction.context.DistributedTransactionContext;
import com.anywide.dawdler.redis.JedisPoolFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 *
 * @Title RedisRepository.java
 * @Description 基于redis实现的存储方式
 * @author jackson.song
 * @date 2021年4月10日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public class RedisRepository extends TransactionRepository {
	private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);
	public static String redisFileName = "distributed-transaction-redis";
	private Pool<Jedis> pool;
	public RedisRepository() {
		try {
			pool = JedisPoolFactory.getJedisPool(redisFileName);
		} catch (Exception e) {
			logger.error("", e);
		}
		serializer = SerializeDecider.decide((byte)2);
	}
	
	private static final String PREFIX = "gtid_";

	@Override
	public int create(DistributedTransactionContext transaction) throws Exception {
		byte[] datas = serializer.serialize(transaction);
		Map<byte[], byte[]> map = new HashMap<>();
		map.put(transaction.getBranchTxId().getBytes(), datas);
		return execute(pool, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(Jedis jedis) {
				jedis.hmset((PREFIX + transaction.getGlobalTxId()).getBytes(), map);
				return 1;
			}
		});
	}

	@Override
	public int update(DistributedTransactionContext transaction) throws Exception {
		return execute(pool, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(Jedis jedis) throws Exception {
				byte[] datas = serializer.serialize(transaction);
				Map<byte[], byte[]> map = jedis.hgetAll(transaction.getGlobalTxId().getBytes());
				if (map != null) {
					map.put(transaction.getBranchTxId().getBytes(), datas);
					jedis.hmset((PREFIX + transaction.getGlobalTxId()).getBytes(), map);
					return 1;
				}
				return 0;
			}
		});
	}

	@Override
	public int deleteByBranchTxId(String globalTxId, String branchTxId) throws Exception {
		return execute(pool, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(Jedis jedis) {
				jedis.hdel((PREFIX + globalTxId), branchTxId);
				return 1;
			}
		});
	}

	@Override
	public int deleteByGlobalTxId(String globalTxId) throws Exception {
		return execute(pool, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(Jedis jedis) {
				jedis.del(globalTxId);
				return 1;
			}
		});
	}

	@Override
	public List<DistributedTransactionContext> findAllByGlobalTxId(String globalTxId) throws Exception {
		return execute(pool, new JedisExecutor<List<DistributedTransactionContext>>() {
			@Override
			public List<DistributedTransactionContext> execute(Jedis jedis) throws Exception {
				List<DistributedTransactionContext> list = new ArrayList<>();
				Collection<byte[]> collection = jedis.hgetAll((PREFIX + globalTxId).getBytes()).values();
				for (byte[] bs : collection) {
					list.add((DistributedTransactionContext) serializer.deserialize(bs));
				}
				return list;
			}
		});
	}

	@Override
	public int updateDataByGlobalTxId(String globalTxId, Map<String, Object> data) throws Exception {
		String status = (String) data.get("status");
		Map<byte[], byte[]> map = new HashMap<>();
		return execute(pool, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(Jedis jedis) throws Exception {
				Collection<byte[]> collection = jedis.hgetAll((PREFIX + globalTxId).getBytes()).values();
				for (byte[] bs : collection) {
					DistributedTransactionContext context = (DistributedTransactionContext) serializer.deserialize(bs);
					if (status != null) {
						context.setStatus(status);
					}
					bs = serializer.serialize(context);
					map.put(context.getBranchTxId().getBytes(), bs);
				}
				if (!map.isEmpty()) {
					jedis.hmset((PREFIX + globalTxId).getBytes(), map);
				}
				return 1;
			}
		});
	}

	@Override
	public List<DistributedTransactionContext> findALLBySecondsLater(int seconds) throws Exception {
		return execute(pool, new JedisExecutor<List<DistributedTransactionContext>>() {
			@Override
			public List<DistributedTransactionContext> execute(Jedis jedis) throws Exception {
				List<DistributedTransactionContext> list = new ArrayList<>();
				Set<byte[]> mkeys = jedis.keys((PREFIX + "*").getBytes());
				for (byte[] keys : mkeys) {
					Collection<byte[]> collection = jedis.hgetAll(keys).values();
					for (byte[] bs : collection) {
						DistributedTransactionContext dc = (DistributedTransactionContext) serializer.deserialize(bs);
						int now = (int) (System.currentTimeMillis() / 1000);
						if ((now - dc.getAddtime()) > seconds) {
							list.add(dc);
						}
					}
				}
				return list;
			}
		});
	}

	private <T> T execute(Pool<Jedis> jedisPool, JedisExecutor<T> executor) throws Exception {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return executor.execute(jedis);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	public static interface JedisExecutor<T> {
		public T execute(Jedis jedis) throws Exception;
	}
}