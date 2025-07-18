import club.dawdler.conf.client.ConfigClient;
import club.dawdler.conf.client.consul.impl.ConsulConfigClient;

module dawdler.config.center.consul.core {
	requires org.slf4j;
	requires dawdler.config.center.core;
	requires dawdler.core;
	requires dawdler.util;
	requires consul.api;
	requires org.apache.httpcomponents.httpclient;

	uses ConfigClient;

	provides ConfigClient with ConsulConfigClient;
}
