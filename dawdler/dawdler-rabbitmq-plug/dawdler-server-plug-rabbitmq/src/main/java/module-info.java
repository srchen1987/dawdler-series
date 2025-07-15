import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.serverplug.rabbitmq.resource.RabbitLifeCycle;

module dawdler.server.plug.rabbitmq {
	requires java.base;
	requires org.apache.commons.pool2;
	requires dawdler.core;
	requires dawdler.rabbitmq.core;
	requires dawdler.server;
	requires dawdler.service.core;

	uses ComponentLifeCycle;
	provides ComponentLifeCycle with RabbitLifeCycle;
}
