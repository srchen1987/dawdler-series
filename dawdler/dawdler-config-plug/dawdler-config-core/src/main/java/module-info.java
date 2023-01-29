import com.anywide.dawdler.conf.client.ConfigClient;
import com.anywide.dawdler.conf.client.impl.ConsulConfigClient;
import com.anywide.dawdler.conf.health.ConfigIndicator;
import com.anywide.dawdler.core.health.HealthIndicator;

module dawdler.config.core {
	requires java.base;
	requires java.sql;
	requires dawdler.util;
	requires org.slf4j;
	requires commons.jexl3;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires consul.api;
	requires dawdler.core;

	exports com.anywide.dawdler.conf;
	exports com.anywide.dawdler.conf.cache;
	exports com.anywide.dawdler.conf.init;
	exports com.anywide.dawdler.conf.annotation;

	uses ConfigClient;

	provides ConfigClient with ConsulConfigClient;

	uses HealthIndicator;

	provides HealthIndicator with ConfigIndicator;
}