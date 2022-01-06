import com.anywide.dawdler.client.cluster.LoadBalance;
import com.anywide.dawdler.client.cluster.impl.RandomLoadBalance;
import com.anywide.dawdler.client.cluster.impl.RoundRobinLoadBalance;

module dawdler.client {
	exports com.anywide.dawdler.client;
	exports com.anywide.dawdler.client.conf;
	exports com.anywide.dawdler.client.filter;
	exports com.anywide.dawdler.client.net.aio.session;
	uses com.anywide.dawdler.client.cluster.LoadBalance;
	uses com.anywide.dawdler.client.filter.DawdlerClientFilter;
	provides LoadBalance with RandomLoadBalance,RoundRobinLoadBalance;
	requires java.base;
	requires dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires curator.client;
	requires curator.framework;
	requires zookeeper;
	requires curator.recipes;
	requires org.objectweb.asm;
	requires cglib;
	requires org.slf4j;
	requires dom4j;
}