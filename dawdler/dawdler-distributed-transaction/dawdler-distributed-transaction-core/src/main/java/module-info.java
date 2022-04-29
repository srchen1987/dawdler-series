module dawdler.distributed.transaction.core {
	exports com.anywide.dawdler.distributed.transaction.release;
	exports com.anywide.dawdler.distributed.transaction.message;
	exports com.anywide.dawdler.distributed.transaction.message.amqp.rabbitmq;
	exports com.anywide.dawdler.distributed.transaction.repository;
	requires java.base;
	requires org.aspectj.runtime;
	requires org.slf4j;
	requires dawdler.core;
	requires dawdler.distributed.transaction.api;
	requires dawdler.util;
	requires dawdler.rabbitmq.core;
	requires com.rabbitmq.client;
	requires redis.clients.jedis;
	requires dawdler.serialization;
	requires dawdler.redis.core;
}