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
package club.dawdler.kafka.consumer;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.kafka.config.KafkaConfig;
import club.dawdler.kafka.config.KafkaConfigProvider;
import club.dawdler.kafka.consumer.annotation.KafkaListener;

/**
 * @author jackson.song
 * @version V1.0
 * 初始化Kafka Listener
 */
public class KafkaListenerInit {
	private static final Logger logger = LoggerFactory.getLogger(KafkaListenerInit.class);
	private static Map<String, Object> listenerCache = new ConcurrentHashMap<>();
	private static Map<String, ExecutorService> executorServices = new ConcurrentHashMap<>();
	private static Map<String, KafkaConsumer<String, byte[]>> consumers = new ConcurrentHashMap<>();
	private static Map<String, Integer> shutdownTimeoutMap = new ConcurrentHashMap<>();

	public static Map<String, KafkaConsumer<String, byte[]>> getConsumers() {
		return consumers;
	}

	public static void initKafkaListener(Object target, Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			KafkaListener listener = method.getAnnotation(KafkaListener.class);
			if (listener != null) {
				String key = method.toGenericString();
				Object object = listenerCache.get(key);
				if (object == null) {
					listenerCache.put(key, target);
					try {
						KafkaConfig config = KafkaConfigProvider.getInstance(listener.fileName());

						String krb5Conf = config.getKerberosKrb5Conf();
						if (krb5Conf != null && !krb5Conf.isEmpty()) {
							System.setProperty("java.security.krb5.conf", krb5Conf);
						}

						Properties kafkaProps = new Properties();
						kafkaProps.put("bootstrap.servers", config.getBootstrapServers());
						kafkaProps.put("group.id", config.getGroupId());
						String autoOffsetReset = listener.autoOffsetReset();
						if (autoOffsetReset == null || autoOffsetReset.isEmpty()) {
							autoOffsetReset = config.getAutoOffsetReset();
						}
						if (autoOffsetReset != null && !autoOffsetReset.isEmpty()) {
							kafkaProps.put("auto.offset.reset", autoOffsetReset);
						}
						kafkaProps.put("key.deserializer", config.getKeyDeserializer());
						kafkaProps.put("value.deserializer", config.getValueDeserializer());
						kafkaProps.put("enable.auto.commit", String.valueOf(listener.autoCommit()));
						kafkaProps.put("auto.commit.interval.ms", config.getAutoCommitIntervalMs());
						kafkaProps.put("session.timeout.ms", String.valueOf(listener.sessionTimeoutMs()));
						kafkaProps.put("heartbeat.interval.ms", String.valueOf(listener.heartbeatIntervalMs()));
						kafkaProps.put("max.poll.records", listener.maxPollRecords());
						kafkaProps.put("fetch.max.bytes", config.getFetchMaxBytes());
						kafkaProps.put("max.partition.fetch.bytes", config.getMaxPartitionFetchBytes());

						if (config.getSecurityProtocol() != null) {
							kafkaProps.put("security.protocol", config.getSecurityProtocol());
						}
						if (config.getSaslMechanism() != null) {
							kafkaProps.put("sasl.mechanism", config.getSaslMechanism());
						}
						if (config.getSaslJaasConfig() != null) {
							kafkaProps.put("sasl.jaas.config", config.getSaslJaasConfig());
						}
						if (config.getSaslKerberosServiceName() != null) {
							kafkaProps.put("sasl.kerberos.service.name", config.getSaslKerberosServiceName());
						}
						if (config.getSaslKerberosKinitCmd() != null) {
							kafkaProps.put("sasl.kerberos.kinit.cmd", config.getSaslKerberosKinitCmd());
						}
						if (config.getSaslKerberosTicketRenewWindowFactor() != null) {
							kafkaProps.put("sasl.kerberos.ticket.renew.window.factor",
									config.getSaslKerberosTicketRenewWindowFactor());
						}
						if (config.getSaslKerberosTicketRenewJitter() != null) {
							kafkaProps.put("sasl.kerberos.ticket.renew.jitter",
									config.getSaslKerberosTicketRenewJitter());
						}
						if (config.getSaslKerberosMinTimeBeforeRelogin() != null) {
							kafkaProps.put("sasl.kerberos.min.time.before.relogin",
									config.getSaslKerberosMinTimeBeforeRelogin());
						}

						KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(kafkaProps);
						consumer.subscribe(List.of(listener.topic()));

						ExecutorService executor = Executors.newFixedThreadPool(listener.consumerThreads());
						executorServices.put(key, executor);
						consumers.put(key, consumer);
						shutdownTimeoutMap.put(key, listener.shutdownTimeoutMs());

						for (int i = 0; i < listener.consumerThreads(); i++) {
							executor.submit(() -> {
								try {
									while (!Thread.currentThread().isInterrupted()) {
										ConsumerRecords<String, byte[]> records = consumer
												.poll(java.time.Duration.ofMillis(100));
										for (ConsumerRecord<String, byte[]> record : records) {
											Message message = new Message(record);
											try {
												method.invoke(target, message);
											} catch (Throwable e) {
												logger.error(
														"Error processing message from topic {} partition {} offset {}",
														record.topic(), record.partition(), record.offset(), e);
											}
										}
									}
								} catch (org.apache.kafka.common.errors.WakeupException e) {
									logger.warn("Kafka consumer wakeup, exiting");
								} catch (Exception e) {
									logger.error("Kafka consumer error", e);
								}
							});
						}
					} catch (Exception e) {
						logger.error("Failed to initialize Kafka listener", e);
						throw e;
					}
				}
			}
		}
	}

	public static void cancelConsumer() {
		consumers.values().forEach(consumer -> {
			try {
				consumer.unsubscribe();
			} catch (Exception e) {
				logger.error("Error canceling Kafka consumer subscription", e);
			}
		});
	}

	public static void waitAll() {
		CountDownLatch latch = new CountDownLatch(executorServices.size());

		executorServices.forEach((key, executor) -> {
			CompletableFuture.runAsync(() -> {
				try {
					int timeoutMs = shutdownTimeoutMap.getOrDefault(key, 5000);
					executor.shutdown();
					if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
						executor.shutdownNow();
					}
				} catch (InterruptedException e) {
					executor.shutdownNow();
					Thread.currentThread().interrupt();
				} finally {
					latch.countDown();
				}
			});
		});

		try {
			int maxTimeout = shutdownTimeoutMap.values().stream()
					.max(Integer::compareTo)
					.orElse(5000);
			latch.await(maxTimeout * 2L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		shutdownTimeoutMap.clear();
		executorServices.clear();
	}

	public static void closeAllConsumers() {
		consumers.values().forEach(consumer -> {
			try {
				consumer.close();
			} catch (Exception e) {
				logger.error("Error closing Kafka consumer", e);
			}
		});
		consumers.clear();

		executorServices.values().forEach(executor -> {
			executor.shutdownNow();
		});
		executorServices.clear();
	}
}