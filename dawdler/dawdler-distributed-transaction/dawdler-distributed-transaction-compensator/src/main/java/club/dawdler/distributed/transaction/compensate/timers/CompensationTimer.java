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
package club.dawdler.distributed.transaction.compensate.timers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.distributed.transaction.TransactionStatus;
import club.dawdler.distributed.transaction.compensate.config.CompensationConfig;
import club.dawdler.distributed.transaction.context.DistributedTransactionContext;
import club.dawdler.distributed.transaction.message.MessageSender;
import club.dawdler.distributed.transaction.message.amqp.rabbitmq.AMQPSender;
import club.dawdler.distributed.transaction.repository.TransactionRepository;
import club.dawdler.util.JsonProcessUtil;

/**
 * @author jackson.song
 * @version V1.0
 * 定时补偿器
 */
public class CompensationTimer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(CompensationTimer.class);
	private TransactionRepository transactionRepository;
	private MessageSender messageSender;
	private ScheduledExecutorService scheduled;
	private final CompensationConfig config;

	public CompensationTimer(TransactionRepository transactionRepository) {
		this(transactionRepository, CompensationConfig.getInstance());
	}

	public CompensationTimer(TransactionRepository transactionRepository, CompensationConfig config) {
		this.transactionRepository = transactionRepository;
		this.config = config;
		messageSender = new AMQPSender();
	}

	public void start() {
		scheduled = Executors.newScheduledThreadPool(1);
		scheduled.scheduleWithFixedDelay(this, config.getInitialDelaySeconds(), config.getDelaySeconds(),
				TimeUnit.SECONDS);
	}

	public void shutdown() {
		scheduled.shutdown();
	}

	@Override
	public void run() {
		try {
			List<DistributedTransactionContext> list = transactionRepository.findALLBySecondsLater();
			for (DistributedTransactionContext dc : list) {
				if (dc.getRetryTime() >= config.getMaxRetryTimes()) {
					logger.warn(
							"transaction exceeds max retry times, give up. globalTxId:{} branchTxId:{} action:{} retryTime:{}",
							dc.getGlobalTxId(), dc.getBranchTxId(), dc.getAction(), dc.getRetryTime());
					continue;
				}
				String status = dc.getStatus();
				if (TransactionStatus.TRYING.equals(status)) {
					if (logger.isDebugEnabled()) {
						logger.debug("transaction trying timeout, force cancel. globalTxId:{} branchTxId:{} action:{}",
								dc.getGlobalTxId(), dc.getBranchTxId(), dc.getAction());
					}
					status = TransactionStatus.CANCEL;
				}
				Map<String, Object> data = new HashMap<>(8);
				data.put("status", status);
				data.put("action", dc.getAction());
				data.put("globalTxId", dc.getGlobalTxId());
				String msg = JsonProcessUtil.beanToJson(data);
				if (logger.isDebugEnabled()) {
					logger.debug("transaction compensate:{}", msg);
				}
				messageSender.sent(msg);
			}
		} catch (Exception e) {
			logger.error("compensation timer error", e);
		}
	}

}
