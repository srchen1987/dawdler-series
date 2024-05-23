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
package com.anywide.dawdler.distributed.transaction.release;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.anywide.dawdler.distributed.transaction.message.amqp.rabbitmq.DistributedTransactionAMQPConnectionFactoryProvider;
import com.anywide.dawdler.distributed.transaction.repository.RedisRepository;
import com.anywide.dawdler.jedis.JedisPoolFactory;
import com.anywide.dawdler.rabbitmq.connection.pool.factory.AMQPConnectionFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Pool;

/**
 * @author jackson.song
 * @version V1.0
 * 资源释放者 释放redis rabbitmq
 */
public class ResourceReleaser {
	private static final Logger logger = LoggerFactory.getLogger(ResourceReleaser.class);

	public static void release() {
		AMQPConnectionFactory connectionFactory = DistributedTransactionAMQPConnectionFactoryProvider.getInstance()
				.getConnectionFactory();
		Pool<Jedis> redisPool = null;
		try {
			redisPool = JedisPoolFactory.getJedisPool(RedisRepository.REDIS_FILE_NAME);
		} catch (Exception e) {
			logger.error("", e);
		}

		if (connectionFactory != null) {
			connectionFactory.close();
		}

		if (redisPool != null) {
			redisPool.close();
		}

	}

}
