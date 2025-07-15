import club.dawdler.client.cluster.LoadBalance;
import club.dawdler.client.cluster.impl.RandomLoadBalance;
import club.dawdler.client.cluster.impl.RoundRobinLoadBalance;
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

	exports club.dawdler.client;
	exports club.dawdler.client.conf;
	exports club.dawdler.client.filter;
	exports club.dawdler.client.net.aio.session;
	exports club.dawdler.client.cluster;

	uses club.dawdler.client.cluster.LoadBalance;
	uses club.dawdler.client.filter.DawdlerClientFilter;

	provides LoadBalance with RandomLoadBalance, RoundRobinLoadBalance;
}
