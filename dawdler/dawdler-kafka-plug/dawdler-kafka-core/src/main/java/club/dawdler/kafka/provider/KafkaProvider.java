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
package club.dawdler.kafka.provider;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.dawdler.kafka.config.KafkaConfig;
import club.dawdler.kafka.config.KafkaConfigProvider;

/**
 * @author jackson.song
 * @version V1.0
 * kafka消息提供者
 */
public class KafkaProvider {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProvider.class);
	private KafkaProducer<String, byte[]> producer;

	public KafkaProvider(String fileName) throws Exception {
		KafkaConfig config = KafkaConfigProvider.getInstance(fileName);
		
		String krb5Conf = config.getKerberosKrb5Conf();
		if (krb5Conf != null && !krb5Conf.isEmpty()) {
			System.setProperty("java.security.krb5.conf", krb5Conf);
		}
		
		Properties kafkaProps = new Properties();
		kafkaProps.put("bootstrap.servers", config.getBootstrapServers());
		kafkaProps.put("key.serializer", config.getKeySerializer());
		kafkaProps.put("value.serializer", config.getValueSerializer());
		kafkaProps.put("acks", config.getAcks());
		kafkaProps.put("retries", config.getRetries());
		kafkaProps.put("batch.size", config.getBatchSize());
		kafkaProps.put("linger.ms", config.getLingerMs());
		kafkaProps.put("buffer.memory", config.getBufferMemory());
		
		// 添加安全配置
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
			kafkaProps.put("sasl.kerberos.ticket.renew.window.factor", config.getSaslKerberosTicketRenewWindowFactor());
		}
		if (config.getSaslKerberosTicketRenewJitter() != null) {
			kafkaProps.put("sasl.kerberos.ticket.renew.jitter", config.getSaslKerberosTicketRenewJitter());
		}
		if (config.getSaslKerberosMinTimeBeforeRelogin() != null) {
			kafkaProps.put("sasl.kerberos.min.time.before.relogin", config.getSaslKerberosMinTimeBeforeRelogin());
		}
		
		this.producer = new KafkaProducer<>(kafkaProps);
	}

	public RecordMetadata publish(String topic, byte[] message) throws Exception {
		ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, message);
		try {
			return producer.send(record).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to send message", e);
			throw e;
		}
	}

	public RecordMetadata publish(String topic, String key, byte[] message) throws Exception {
		ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, message);
		try {
			return producer.send(record).get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to send message", e);
			throw e;
		}
	}

	public Future<RecordMetadata> publish(String topic, byte[] message, Callback callback) throws Exception {
		ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, message);
			return producer.send(record, callback);
	}

	public Future<RecordMetadata> publish(String topic, String key, byte[] message, Callback callback) throws Exception {
		ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, message);
			return producer.send(record, callback);
	}

	public void close() {
		if (producer != null) {
			producer.close();
		}
	}
}