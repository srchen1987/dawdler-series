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
package club.dawdler.kafka.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import club.dawdler.util.DawdlerTool;
import club.dawdler.util.PropertiesUtil;

/**
 * @author jackson.song
 * @version V1.0
 * Kafka配置管理类，统一管理Kafka生产者和消费者的配置
 */
public class KafkaConfig {
	private Properties properties;
	private String fileName;

	KafkaConfig(String fileName) {
		this.fileName = fileName;
		try {
			this.properties = PropertiesUtil.loadPropertiesIfNotExistLoadConfigCenter(fileName);
		} catch (Exception e) {
			throw new RuntimeException("Failed to load Kafka configuration from file: " + fileName, e);
		}
	}

	/**
	 * 获取bootstrap.servers配置
	 */
	public String getBootstrapServers() {
		return properties.getProperty("bootstrap.servers");
	}

	/**
	 * 获取安全协议配置
	 */
	public String getSecurityProtocol() {
		return properties.getProperty("security.protocol");
	}

	/**
	 * 获取SASL机制配置
	 */
	public String getSaslMechanism() {
		return properties.getProperty("sasl.mechanism");
	}

	/**
	 * 获取JAAS配置
	 * 如果配置了kerberos.keytab，则会将keytab文件复制到临时文件并追加到JAAS配置中
	 * 如果配置了kerberos.principal，也会追加到JAAS配置中
	 */
	public String getSaslJaasConfig() {
		String jaasConfig = properties.getProperty("sasl.jaas.config");
		String keytabPath = getKerberosKeyTab();
		String principal = getKerberosPrincipal();

		// 构建追加的配置项
		StringBuilder additionalConfig = new StringBuilder();

		if (keytabPath != null && !keytabPath.isEmpty()) {
			String keytabPathTemp;
			
			// 如果是绝对路径（以/开头），直接使用原路径
			if (keytabPath.startsWith("/")) {
				keytabPathTemp = keytabPath;
			} else {
				// 否则从classpath读取并复制到临时文件
				InputStream input = DawdlerTool.getResourceFromClassPath(keytabPath);
				
				if (input == null) {
					throw new RuntimeException("Keytab resource not found: " + keytabPath);
				}
				
				try {
					// 创建临时文件
					Path tempKeytab = Files.createTempFile("kerberos-"+fileName, ".keytab");
					Files.copy(input, tempKeytab, StandardCopyOption.REPLACE_EXISTING);
					keytabPathTemp = tempKeytab.toAbsolutePath().toString();
				} catch (Exception e) {
					throw new RuntimeException("Failed to process keytab file: " + keytabPath, e);
				}finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
						}
					}
				}
			}
			
			additionalConfig.append(" required useKeyTab=true");
			additionalConfig.append(" keyTab=\"").append(keytabPathTemp).append("\"");
		}

		if (principal != null && !principal.isEmpty()) {
			additionalConfig.append(" principal=\"").append(principal).append("\"");
		}

		// 如果有需要追加的配置项
		if (additionalConfig.length() > 0) {
			if (jaasConfig == null || jaasConfig.isEmpty()) {
				jaasConfig = "com.sun.security.auth.module.Krb5LoginModule " + additionalConfig.toString() + ";";
			} else {
				jaasConfig = jaasConfig.trim();
				if (jaasConfig.endsWith(";")) {
					jaasConfig = jaasConfig.substring(0, jaasConfig.length() - 1);
				}
				jaasConfig += additionalConfig.toString() + ";";
			}
		}

		return jaasConfig;
	}

	/**
	 * 获取Kerberos服务名称配置
	 */
	public String getSaslKerberosServiceName() {
		return properties.getProperty("sasl.kerberos.service.name");
	}

	/**
	 * 获取Kerberos kinit命令配置
	 */
	public String getSaslKerberosKinitCmd() {
		return properties.getProperty("sasl.kerberos.kinit.cmd");
	}

	/**
	 * 获取Kerberos票据续期窗口因子配置
	 */
	public String getSaslKerberosTicketRenewWindowFactor() {
		return properties.getProperty("sasl.kerberos.ticket.renew.window.factor");
	}

	/**
	 * 获取Kerberos票据续期抖动配置
	 */
	public String getSaslKerberosTicketRenewJitter() {
		return properties.getProperty("sasl.kerberos.ticket.renew.jitter");
	}

	/**
	 * 获取Kerberos最小重新登录时间配置
	 */
	public String getSaslKerberosMinTimeBeforeRelogin() {
		return properties.getProperty("sasl.kerberos.min.time.before.relogin");
	}

	/**
	 * 获取Kerberos keyTab文件路径配置
	 */
	public String getKerberosKeyTab() {
		return properties.getProperty("kerberos.keytab");
	}

	/**
	 * 获取Kerberos principal配置
	 */
	public String getKerberosPrincipal() {
		return properties.getProperty("kerberos.principal");
	}
	/**
	 * 获取Kerberos krb5.conf文件路径 (JVM系统属性)
	 * 设置java.security.krb5.conf系统属性
	 * 如果是绝对路径（以/开头），直接使用原路径
	 * 否则从classpath读取并复制到临时文件
	 */
	public String getKerberosKrb5Conf() {
		String krb5ConfPath = properties.getProperty("kerberos.krb5.conf");
		
		if (krb5ConfPath != null && !krb5ConfPath.isEmpty()) {
			// 如果是绝对路径（以/开头），直接使用原路径
			if (!krb5ConfPath.startsWith("/")) {
				// 从classpath读取并复制到临时文件
				InputStream input = DawdlerTool.getResourceFromClassPath(krb5ConfPath);
				
				if (input == null) {
					throw new RuntimeException("krb5.conf resource not found: " + krb5ConfPath);
				}
				
				try {
					// 创建临时文件
					Path tempKrb5Conf = Files.createTempFile("krb5-"+fileName, ".conf");
					Files.copy(input, tempKrb5Conf, StandardCopyOption.REPLACE_EXISTING);
					krb5ConfPath = tempKrb5Conf.toAbsolutePath().toString();
				} catch (Exception e) {
					throw new RuntimeException("Failed to process krb5.conf file: " + krb5ConfPath, e);
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		
		return krb5ConfPath;
	}

	/**
	 * 获取生产者acks配置
	 */
	public String getAcks() {
		return properties.getProperty("acks", "all");
	}

	/**
	 * 获取生产者retries配置
	 */
	public String getRetries() {
		return properties.getProperty("retries", "3");
	}

	/**
	 * 获取生产者batch.size配置
	 */
	public String getBatchSize() {
		return properties.getProperty("batch.size", "16384");
	}

	/**
	 * 获取生产者linger.ms配置
	 */
	public String getLingerMs() {
		return properties.getProperty("linger.ms", "1");
	}

	/**
	 * 获取生产者buffer.memory配置
	 */
	public String getBufferMemory() {
		return properties.getProperty("buffer.memory", "33554432");
	}

	/**
	 * 获取消费者group.id配置
	 */
	public String getGroupId() {
		return properties.getProperty("group.id");
	}

	/**
	 * 获取消费者auto.commit.interval.ms配置
	 */
	public String getAutoCommitIntervalMs() {
		return properties.getProperty("auto.commit.interval.ms", "1000");
	}

	/**
	 * 获取消费者session.timeout.ms配置
	 */
	public String getSessionTimeoutMs() {
		return properties.getProperty("session.timeout.ms", "10000");
	}

	/**
	 * 获取消费者heartbeat.interval.ms配置
	 */
	public String getHeartbeatIntervalMs() {
		return properties.getProperty("heartbeat.interval.ms", "3000");
	}

	/**
	 * 获取消费者max.poll.records配置
	 */
	public String getMaxPollRecords() {
		return properties.getProperty("max.poll.records", "500");
	}

	/**
	 * 获取消费者fetch.max.bytes配置
	 */
	public String getFetchMaxBytes() {
		return properties.getProperty("fetch.max.bytes", "52428800");
	}

	/**
	 * 获取消费者max.partition.fetch.bytes配置
	 */
	public String getMaxPartitionFetchBytes() {
		return properties.getProperty("max.partition.fetch.bytes", "10485760");
	}

	/**
	 * 获取配置文件名
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * 获取消费者key.deserializer配置
	 */
	public String getKeyDeserializer() {
		return properties.getProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	}

	/**
	 * 获取消费者value.deserializer配置
	 */
	public String getValueDeserializer() {
		return properties.getProperty("value.deserializer",
				"org.apache.kafka.common.serialization.ByteArrayDeserializer");
	}

	/**
	 * 获取生产者key.serializer配置
	 */
	public String getKeySerializer() {
		return properties.getProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	}

	/**
	 * 获取生产者value.serializer配置
	 */
	public String getValueSerializer() {
		return properties.getProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
	}

	/**
	 * 获取消费者enable.auto.commit配置
	 */
	public boolean getEnableAutoCommit() {
		return Boolean.parseBoolean(properties.getProperty("enable.auto.commit", "true"));
	}

	/**
	 * 获取消费者auto.offset.reset配置
	 * earliest: 从最早偏移量开始消费
	 * latest: 从最新偏移量开始消费
	 * none: 如果没有找到消费者组则抛出异常
	 * 不配置则使用Kafka官方默认值
	 */
	public String getAutoOffsetReset() {
		return properties.getProperty("auto.offset.reset");
	}

}