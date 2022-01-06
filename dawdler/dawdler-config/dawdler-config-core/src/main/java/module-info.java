import com.anywide.dawdler.conf.client.ConfigClient;
import com.anywide.dawdler.conf.client.impl.ConsulConfigClient;

module dawdler.config.core {
	exports com.anywide.dawdler.conf;
	exports com.anywide.dawdler.conf.cache;
	exports com.anywide.dawdler.conf.init;
	uses ConfigClient;
	provides ConfigClient with ConsulConfigClient;
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires commons.jexl3;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires consul.api;
}