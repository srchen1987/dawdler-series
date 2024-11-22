import com.anywide.dawdler.client.cluster.LoadBalance;
import com.anywide.dawdler.client.cluster.impl.RandomLoadBalance;
import com.anywide.dawdler.client.cluster.impl.RoundRobinLoadBalance;
@SuppressWarnings("rawtypes")
module dawdler.client {
	requires java.base;
	requires transitive dawdler.util;
	requires transitive dawdler.core;
	requires dawdler.serialization;
	requires org.objectweb.asm;
	requires org.slf4j;
	requires transitive java.xml;
	requires dawdler.service.core;

	exports com.anywide.dawdler.client;
	exports com.anywide.dawdler.client.conf;
	exports com.anywide.dawdler.client.filter;
	exports com.anywide.dawdler.client.net.aio.session;
	exports com.anywide.dawdler.client.cluster;

	uses com.anywide.dawdler.client.cluster.LoadBalance;
	uses com.anywide.dawdler.client.filter.DawdlerClientFilter;

	provides LoadBalance with RandomLoadBalance, RoundRobinLoadBalance;
}