import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.core.shutdown.ContainerGracefulShutdown;
import club.dawdler.rabbitmq.health.RabbitIndicator;
import club.dawdler.rabbitmq.resource.RabbitLifeCycle;
import club.dawdler.rabbitmq.shutdown.RabbitmqGracefulShutdown;

module dawdler.rabbitmq.core {
	requires java.base;
	requires dawdler.util;
	requires transitive com.rabbitmq.client;
	requires dawdler.core;
	requires org.apache.commons.pool2;
	requires org.slf4j;

	exports club.dawdler.rabbitmq.connection.pool.factory;
	exports club.dawdler.rabbitmq.consumer;
	exports club.dawdler.rabbitmq.consumer.annotation;
	exports club.dawdler.rabbitmq.provider;
	exports club.dawdler.rabbitmq.provider.annotation;

	uses ComponentLifeCycle;
	provides ComponentLifeCycle with RabbitLifeCycle;

	uses HealthIndicator;
	provides HealthIndicator with RabbitIndicator;
	
	uses ContainerGracefulShutdown;
	provides ContainerGracefulShutdown with RabbitmqGracefulShutdown;
}
