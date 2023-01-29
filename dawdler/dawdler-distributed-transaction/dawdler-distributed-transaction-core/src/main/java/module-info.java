module dawdler.distributed.transaction.core {
	requires java.base;
	requires org.aspectj.runtime;
	requires org.slf4j;
	requires dawdler.core;
	requires transitive dawdler.distributed.transaction.api;
	requires dawdler.util;
	requires transitive dawdler.rabbitmq.core;
	requires transitive com.rabbitmq.client;
	requires transitive redis.clients.jedis;
	requires dawdler.serialization;
	requires dawdler.redis.core;
	requires org.apache.commons.pool2;
	exports com.anywide.dawdler.distributed.transaction.release;
	exports com.anywide.dawdler.distributed.transaction.message;
	exports com.anywide.dawdler.distributed.transaction.message.amqp.rabbitmq;
	exports com.anywide.dawdler.distributed.transaction.repository;
}