
module dawdler.discovery.center.consul.core {
	requires org.slf4j;
	requires dawdler.core;
	requires dawdler.util;
	requires dawdler.discovery.center.core;
	requires transitive consul.api;
	requires org.apache.httpcomponents.httpclient;

	exports com.anywide.dawdler.core.discovery.consul;
}