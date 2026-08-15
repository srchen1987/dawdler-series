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
package club.dawdler.distributed.transaction.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.distributed.transaction.context.DistributedTransactionContext;
import club.dawdler.jedis.UnifiedJedisFactory;
import club.dawdler.serializer.SerializeDecider;
import club.dawdler.util.PropertiesUtil;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

/**
 * @author jackson.song
 * @version V1.0
 * 基于redis实现的存储方式
 */
public class RedisRepository extends TransactionRepository {
	private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);
	public static final String REDIS_FILE_NAME = "distributed-transaction-redis";
	private static final String TRANSACTION_CONFIG = "distributed-transaction";
	private long expireTime = 24 * 60 * 60 * 3;
	private int compensateLater = 60;
	private UnifiedJedis unifiedJedis;

	public RedisRepository() {
		try {
			unifiedJedis = UnifiedJedisFactory.getUnifiedJedis(REDIS_FILE_NAME).getUnifiedJedis();
		} catch (Exception e) {
			logger.error("", e);
		}
		serializer = SerializeDecider.decide((byte) 2);
		Properties ps = null;
		try {
			ps = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(TRANSACTION_CONFIG);
		} catch (Exception e) {
			logger.warn(
					"not found distributed-transaction.properties in classpath or unified configuration center, use default set. expireTime={} seconds,compensateLater={} seconds.",
					expireTime, compensateLater);
		}
		if (ps != null) {
			expireTime = PropertiesUtil.getIfNullReturnDefaultValueLong("expireTime", expireTime, ps);
			compensateLater = PropertiesUtil.getIfNullReturnDefaultValueInt("compensateLater", compensateLater, ps);
		}
	}

	private static final String PREFIX = "gtid_";

	@Override
	public int create(DistributedTransactionContext transaction) throws Exception {
		byte[] data = serializer.serialize(transaction);
		Map<byte[], byte[]> map = new HashMap<>(8);
		map.put(transaction.getBranchTxId().getBytes(), data);
		return execute(unifiedJedis, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(UnifiedJedis unifiedJedis) {
				byte[] globalKey = (PREFIX + transaction.getGlobalTxId()).getBytes();
				unifiedJedis.hset(globalKey, map);
				unifiedJedis.expire(globalKey, expireTime);
				return 1;
			}
		});
	}

	@Override
	public int update(DistributedTransactionContext transaction) throws Exception {
		return execute(unifiedJedis, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(UnifiedJedis unifiedJedis) throws Exception {
				byte[] data = serializer.serialize(transaction);
				byte[] globalKey = (PREFIX + transaction.getGlobalTxId()).getBytes();
				unifiedJedis.hset(globalKey, transaction.getBranchTxId().getBytes(), data);
				unifiedJedis.expire(globalKey, expireTime);
				return 1;
			}
		});
	}

	@Override
	public int deleteByBranchTxId(String globalTxId, String branchTxId) throws Exception {
		return execute(unifiedJedis, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(UnifiedJedis unifiedJedis) {
				unifiedJedis.hdel((PREFIX + globalTxId).getBytes(), branchTxId.getBytes());
				return 1;
			}
		});
	}

	@Override
	public int deleteByGlobalTxId(String globalTxId) throws Exception {
		return execute(unifiedJedis, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(UnifiedJedis unifiedJedis) {
				unifiedJedis.del((PREFIX + globalTxId));
				return 1;
			}
		});
	}

	@Override
	public List<DistributedTransactionContext> findAllByGlobalTxId(String globalTxId) throws Exception {
		return execute(unifiedJedis, new JedisExecutor<List<DistributedTransactionContext>>() {
			@Override
			public List<DistributedTransactionContext> execute(UnifiedJedis unifiedJedis) throws Exception {
				List<DistributedTransactionContext> list = new ArrayList<>();
				Collection<byte[]> collection = unifiedJedis.hgetAll((PREFIX + globalTxId).getBytes()).values();
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
		Map<byte[], byte[]> map = new HashMap<>(8);
		return execute(unifiedJedis, new JedisExecutor<Integer>() {
			@Override
			public Integer execute(UnifiedJedis unifiedJedis) throws Exception {
				Collection<byte[]> collection = unifiedJedis.hgetAll((PREFIX + globalTxId).getBytes()).values();
				for (byte[] bs : collection) {
					DistributedTransactionContext context = (DistributedTransactionContext) serializer.deserialize(bs);
					if (status != null) {
						context.setStatus(status);
					}
					bs = serializer.serialize(context);
					map.put(context.getBranchTxId().getBytes(), bs);
				}
				if (!map.isEmpty()) {
					byte[] globalKey = (PREFIX + globalTxId).getBytes();
					unifiedJedis.hset(globalKey, map);
					unifiedJedis.expire(globalKey, expireTime);
				}
				return 1;
			}
		});
	}

	@Override
	public List<DistributedTransactionContext> findALLBySecondsLater() throws Exception {
		final int seconds = compensateLater;
		return execute(unifiedJedis, new JedisExecutor<List<DistributedTransactionContext>>() {
			@Override
			public List<DistributedTransactionContext> execute(UnifiedJedis unifiedJedis) throws Exception {
				List<DistributedTransactionContext> list = new ArrayList<>();
				ScanParams scanParams = new ScanParams();
				scanParams.match(PREFIX + "*");
				scanParams.count(100);
				String cursor = ScanParams.SCAN_POINTER_START;
				do {
					ScanResult<String> scanResult = unifiedJedis.scan(cursor, scanParams);
					cursor = scanResult.getCursor();
					for (String key : scanResult.getResult()) {
						Collection<byte[]> collection = unifiedJedis.hgetAll(key.getBytes()).values();
						for (byte[] bs : collection) {
							DistributedTransactionContext dc = (DistributedTransactionContext) serializer.deserialize(bs);
							int now = (int) (System.currentTimeMillis() / 1000);
							if ((now - dc.getAddtime()) > seconds) {
								list.add(dc);
							}
						}
					}
				} while (!ScanParams.SCAN_POINTER_START.equals(cursor));
				return list;
			}
		});
	}

	private <T> T execute(UnifiedJedis unifiedJedis, JedisExecutor<T> executor) throws Exception {
		return executor.execute(unifiedJedis);
	}

	public static interface JedisExecutor<T> {
		public T execute(UnifiedJedis unifiedJedis) throws Exception;
	}
}
