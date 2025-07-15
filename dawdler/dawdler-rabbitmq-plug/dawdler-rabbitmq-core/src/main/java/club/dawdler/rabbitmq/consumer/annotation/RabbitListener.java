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
package club.dawdler.rabbitmq.consumer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jackson.song
 * @version V1.0
 * 标注一个方法是否是rabbitmq的消费者 此方法格式固定为void methodName(Message message)
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RabbitListener {

	/**
	 * 指定rabbitmq的配置文件名
	 */
	String fileName();

	/**
	 * 队列名
	 */
	String queueName();
	
	/**
	 * routingKey
	 */
	String[] routingKey() default {};
	
	/**
	 * 交换器
	 */
	String[] exchange() default {};

	/**
	 * 是否自动ack
	 */
	boolean autoAck() default true;

	/**
	 * 是否重试
	 */
	boolean retry() default false;

	/**
	 * 失败后进入死信队列
	 */
	boolean failedToDLQ() default true;

	/**
	 * 重试次数
	 */
	int retryCount() default 12;

	/**
	 * 当前消费者个数 不能大于channel.size=16 #每个connection中的channel数量
	 */
	int concurrentConsumers() default 1;

	/**
	 * prefetchCount来限制服务器端每次发送给每个消费者的消息数.
	 */
	int prefetchCount() default 1;

}
