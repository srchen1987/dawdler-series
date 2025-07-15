import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.serverplug.discovery.consul.resource.ConsulLifeCycle;

module dawdler.server.plug.discovery.center.consul {
	requires org.slf4j;
	requires dawdler.server;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;
	requires dawdler.discovery.center.consul.core;
	requires dawdler.discovery.center.server.plug;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ConsulLifeCycle;
}
