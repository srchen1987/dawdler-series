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
package club.dawdler.distributed.transaction.compensate.message;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.core.thread.DefaultThreadFactory;
import club.dawdler.distributed.transaction.compensate.process.DistributedTransactionCustomProcessor;
import club.dawdler.distributed.transaction.context.DistributedTransactionContext;
import club.dawdler.distributed.transaction.repository.RedisRepository;
import club.dawdler.distributed.transaction.repository.TransactionRepository;
import club.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 消息消费者 将消息分发到不同的处理者上去执行，处理者需要继承DistributedTransactionCustomProcessor
 */
public class MessageConsumer {
	private static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
	private ExecutorService executor;

	private TransactionRepository transactionRepository = new RedisRepository();

	public TransactionRepository getTransactionRepository() {
		return transactionRepository;
	}

	public MessageConsumer() {

	}

	protected void consume(String message) {
		consume(JsonProcessUtil.jsonToBean(message, Map.class));
	}

	protected void consume(byte[] messageByte) {
		consume(JsonProcessUtil.jsonToBean(messageByte, Map.class));
	}

	private void consume(Map<String, String> map) {
		String globalTxId = map.get("globalTxId");
		String status = map.get("status");
		try {
			List<DistributedTransactionContext> list = transactionRepository.findAllByGlobalTxId(globalTxId);
			for (DistributedTransactionContext dt : list) {
				executor.execute(() -> {
					String action = dt.getAction();
					Object obj = DistributedTransactionCustomProcessor.getProcessor(action);
					if (obj == null) {
						throw new NullPointerException("not found processor " + action + " !");
					}
					String branchTxId = dt.getBranchTxId();
					boolean result = ((DistributedTransactionCustomProcessor) obj).process(dt, status);
					if (logger.isDebugEnabled()) {
						logger.debug("compensate_result: globalTxId:{} branchId:{} action:{} status:{} result:{}",
								dt.getGlobalTxId(), dt.getBranchTxId(), action, status, result);
					}
					if (result) {
						try {
							transactionRepository.deleteByBranchTxId(globalTxId, branchTxId);
						} catch (Exception e) {
							logger.error("", e);
						}

					} else {
						dt.retryTimeIncre();
						try {
							transactionRepository.update(dt);
						} catch (Exception e) {
							logger.error("", e);
						}
					}
				});

			}
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	public void start() throws Exception {
		int cpus = Runtime.getRuntime().availableProcessors() * 2 + 1;
		executor = new ThreadPoolExecutor(cpus, cpus, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(1024 * 64), new DefaultThreadFactory("transactionCustomExecutor#"));
	}

	public void shutdown() {
		executor.shutdown();
	}

}
