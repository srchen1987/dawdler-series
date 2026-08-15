import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.gateway.discovery.consul.resource.ConsulLifeCycle;

module dawdler.gateway.discovery.center.consul {
	requires dawdler.core;
	requires dawdler.discovery.center.core;
	requires dawdler.discovery.center.consul.core;
	requires dawdler.discovery.center.client.plug;
	requires dawdler.client.plug.web;
	requires dawdler.web.gateway;
	requires consul.api;

	provides ComponentLifeCycle with ConsulLifeCycle;
}
