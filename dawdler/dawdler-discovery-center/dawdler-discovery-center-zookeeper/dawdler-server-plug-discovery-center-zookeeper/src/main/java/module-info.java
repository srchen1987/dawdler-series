import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.serverplug.discovery.zookeeper.resource.ZookeeperLifeCycle;

module dawdler.server.plug.discovery.center.zookeeper {
	requires org.slf4j;
	requires dawdler.discovery.center.zookeeper.core;
	requires dawdler.server;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;
	requires dawdler.discovery.center.server.plug;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ZookeeperLifeCycle;
}
