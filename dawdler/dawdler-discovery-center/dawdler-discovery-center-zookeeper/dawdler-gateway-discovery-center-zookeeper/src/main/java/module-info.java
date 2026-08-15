import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.gateway.discovery.zookeeper.resource.ZookeeperLifeCycle;

module dawdler.gateway.discovery.center.zookeeper {
	requires dawdler.core;
	requires dawdler.discovery.center.core;
	requires dawdler.discovery.center.zookeeper.core;
	requires dawdler.discovery.center.client.plug;
	requires dawdler.web.gateway;
	requires curator.recipes;
	requires curator.framework;

	provides ComponentLifeCycle with ZookeeperLifeCycle;
}
