import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.rabbitmq.health.RabbitIndicator;
import com.anywide.dawdler.rabbitmq.resource.RabbitLifeCycle;

module dawdler.rabbitmq.core{
	exports com.anywide.dawdler.rabbitmq.connection.pool.factory;
	exports com.anywide.dawdler.rabbitmq.consumer;
	exports com.anywide.dawdler.rabbitmq.consumer.annotation;
	exports com.anywide.dawdler.rabbitmq.provider;
	exports com.anywide.dawdler.rabbitmq.provider.annotation;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with RabbitLifeCycle;
	uses HealthIndicator;
	provides HealthIndicator with RabbitIndicator;
	requires java.base;
	requires dawdler.util;
	requires com.rabbitmq.client;
	requires dawdler.core;
	requires org.apache.commons.pool2;
	requires org.slf4j;
}