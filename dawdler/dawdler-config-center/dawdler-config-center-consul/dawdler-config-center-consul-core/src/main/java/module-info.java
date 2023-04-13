import com.anywide.dawdler.conf.client.ConfigClient;
import com.anywide.dawdler.conf.client.consul.impl.ConsulConfigClient;

module dawdler.config.center.consul.core {
	uses ConfigClient;

	provides ConfigClient with ConsulConfigClient;

	requires org.slf4j;
	requires dawdler.config.center.core;
	requires dawdler.core;
	requires dawdler.util;
	requires consul.api;
}