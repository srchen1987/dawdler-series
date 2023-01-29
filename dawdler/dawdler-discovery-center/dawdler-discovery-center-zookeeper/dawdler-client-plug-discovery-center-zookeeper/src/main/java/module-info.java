import com.anywide.dawdler.clientplug.discovery.zookeeper.resource.ZookeeperLifeCycle;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.discovery.center.zookeeper {
	requires org.slf4j;
	requires dawdler.client;
	requires dawdler.core;
	requires curator.recipes;
	requires curator.framework;
	requires dawdler.discovery.center.zookeeper.core;
	requires dawdler.discovery.center.core;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with ZookeeperLifeCycle;
}