import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.rabbitmq.resource.RabbitLifeCycle;

module dawdler.server.plug.rabbitmq{
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with RabbitLifeCycle;
	requires java.base;
	requires org.apache.commons.pool2;
	requires dawdler.core;
	requires dawdler.rabbitmq.core;
	requires dawdler.server;
}