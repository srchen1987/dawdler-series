import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.core.shutdown.ContainerGracefulShutdown;
import com.anywide.dawdler.rabbitmq.health.RabbitIndicator;
import com.anywide.dawdler.rabbitmq.resource.RabbitLifeCycle;
import com.anywide.dawdler.rabbitmq.shutdown.RabbitmqGracefulShutdown;

module dawdler.rabbitmq.core {
	requires java.base;
	requires dawdler.util;
	requires transitive com.rabbitmq.client;
	requires dawdler.core;
	requires org.apache.commons.pool2;
	requires org.slf4j;

	exports com.anywide.dawdler.rabbitmq.connection.pool.factory;
	exports com.anywide.dawdler.rabbitmq.consumer;
	exports com.anywide.dawdler.rabbitmq.consumer.annotation;
	exports com.anywide.dawdler.rabbitmq.provider;
	exports com.anywide.dawdler.rabbitmq.provider.annotation;

	uses ComponentLifeCycle;
	provides ComponentLifeCycle with RabbitLifeCycle;

	uses HealthIndicator;
	provides HealthIndicator with RabbitIndicator;
	
	uses ContainerGracefulShutdown;
	provides ContainerGracefulShutdown with RabbitmqGracefulShutdown;
}