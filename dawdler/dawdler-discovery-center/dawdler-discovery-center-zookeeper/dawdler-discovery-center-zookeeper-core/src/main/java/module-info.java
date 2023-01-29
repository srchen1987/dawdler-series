
module dawdler.discovery.center.zookeeper.core {
	requires org.slf4j;
	requires dawdler.core;
	requires dawdler.util;
	requires curator.client;
	requires transitive curator.framework;
	requires curator.recipes;
	requires zookeeper;
	requires zookeeper.jute;
	requires dawdler.discovery.center.core;

	exports com.anywide.dawdler.core.discovery.zookeeper;
}