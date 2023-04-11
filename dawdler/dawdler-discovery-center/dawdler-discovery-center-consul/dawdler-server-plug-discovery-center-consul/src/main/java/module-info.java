import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.discovery.consul.resource.ConsulLifeCycle;

module dawdler.server.plug.discovery.center.consul {
	requires org.slf4j;
	requires dawdler.server;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;
	requires dawdler.discovery.center.consul.core;

	provides ComponentLifeCycle with ConsulLifeCycle;
}