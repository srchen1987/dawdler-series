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

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author jackson.song
 * @version V1.0
 * kafka消息封装
 */
public class Message {
	private ConsumerRecord<String, byte[]> consumerRecord;

	public Message(ConsumerRecord<String, byte[]> consumerRecord) {
		this.consumerRecord = consumerRecord;
	}

	public String getTopic() {
		return consumerRecord.topic();
	}

	public int getPartition() {
		return consumerRecord.partition();
	}

	public long getOffset() {
		return consumerRecord.offset();
	}

	public String getKey() {
		return consumerRecord.key();
	}

	public byte[] getValue() {
		return consumerRecord.value();
	}

	public ConsumerRecord<String, byte[]> getConsumerRecord() {
		return consumerRecord;
	}
}