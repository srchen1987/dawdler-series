import club.dawdler.clientplug.discovery.consul.resource.ConsulLifeCycle;
import club.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.discovery.center.consul {
	requires org.slf4j;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;
	requires dawdler.client;
	requires dawdler.discovery.center.consul.core;
	requires dawdler.discovery.center.client.plug;
	requires dawdler.client.plug.web;
	requires consul.api;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ConsulLifeCycle;
}
