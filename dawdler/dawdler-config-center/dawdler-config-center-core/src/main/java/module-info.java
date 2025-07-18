import club.dawdler.conf.client.ConfigClient;
import club.dawdler.conf.health.ConfigIndicator;
import club.dawdler.core.health.HealthIndicator;

module dawdler.config.center.core {
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires commons.jexl3;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires dawdler.core;

	exports club.dawdler.conf;
	exports club.dawdler.conf.client;
	exports club.dawdler.conf.cache;
	exports club.dawdler.conf.init;
	exports club.dawdler.conf.annotation;

	uses HealthIndicator;

	uses ConfigClient;

	provides HealthIndicator with ConfigIndicator;
}
