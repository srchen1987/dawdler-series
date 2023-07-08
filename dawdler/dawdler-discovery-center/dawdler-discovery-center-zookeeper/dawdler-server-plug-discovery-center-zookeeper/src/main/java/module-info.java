import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.discovery.zookeeper.resource.ZookeeperLifeCycle;

module dawdler.server.plug.discovery.center.zookeeper {
	requires org.slf4j;
	requires dawdler.discovery.center.zookeeper.core;
	requires dawdler.server;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;

	provides ComponentLifeCycle with ZookeeperLifeCycle;
}