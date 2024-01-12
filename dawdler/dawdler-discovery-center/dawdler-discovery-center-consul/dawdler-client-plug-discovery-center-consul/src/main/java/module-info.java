import com.anywide.dawdler.clientplug.discovery.consul.resource.ConsulLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.discovery.center.consul {
	requires org.slf4j;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;
	requires dawdler.client;
	requires dawdler.discovery.center.consul.core;
	requires consul.api;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ConsulLifeCycle;
}