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
package com.anywide.dawdler.rabbitmq.consumer;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Envelope;

/**
 * @author jackson.song
 * @version V1.0
 * @Title Message.java
 * @Description mq消费信息包装类
 * @date 2022年4月14日
 * @email suxuan696@gmail.com
 */
public class Message {
	private String consumerTag;
	private Envelope envelope;
	private BasicProperties properties;
	private byte[] body;

	public Message(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
		this.consumerTag = consumerTag;
		this.envelope = envelope;
		this.properties = properties;
		this.body = body;
	}

	public String getConsumerTag() {
		return consumerTag;
	}

	public void setConsumerTag(String consumerTag) {
		this.consumerTag = consumerTag;
	}

	public Envelope getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Envelope envelope) {
		this.envelope = envelope;
	}

	public BasicProperties getProperties() {
		return properties;
	}

	public void setProperties(BasicProperties properties) {
		this.properties = properties;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

}
