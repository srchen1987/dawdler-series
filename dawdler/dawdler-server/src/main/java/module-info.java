import org.slf4j.spi.SLF4JServiceProvider;

import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.server.component.injector.DawdlerFilterInjector;
import com.anywide.dawdler.server.component.injector.DawdlerServiceCreateListenerInjector;
import com.anywide.dawdler.server.component.injector.DawdlerServiceListenerInjector;
import com.anywide.dawdler.server.component.injector.ServiceInjector;
import com.anywide.dawdler.server.log.DawdlerLogbackServiceProvider;

module dawdler.server {
	requires transitive dawdler.util;
	requires transitive dawdler.serialization;
	requires transitive dawdler.core;
	requires java.base;
	requires jdk.httpserver;
	requires transitive java.naming;
	requires java.se;
	requires org.slf4j;
	requires cglib;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires org.aspectj.weaver;

	exports com.anywide.dawdler.server.filter;
	exports com.anywide.dawdler.server.listener;
	exports com.anywide.dawdler.server.context;
	exports com.anywide.dawdler.server.service.listener;
	exports com.anywide.dawdler.server.service to com.anywide.dawdler.server.deploys;
	exports com.anywide.dawdler.server.bean;
	exports com.anywide.dawdler.server.thread.processor;
	exports com.anywide.dawdler.server.deploys
	to dawdler.server.plug.db, dawdler.server.plug.jedis, dawdler.server.plug.config.center.consul,
	dawdler.server.plug.rabbitmq, dawdler.server.plug.es, dawdler.server.plug.schedule, dawdler.discovery.center.core;
	exports com.anywide.dawdler.server.net.aio.session;
	exports com.anywide.dawdler.server.conf to dawdler.server, dawdler.server.plug.discovery.center.consul;
	exports com.anywide.dawdler.server.service.conf;
	uses SLF4JServiceProvider;
	uses CustomComponentInjector;

	provides SLF4JServiceProvider with DawdlerLogbackServiceProvider;
	provides CustomComponentInjector with DawdlerFilterInjector, DawdlerServiceCreateListenerInjector,
			DawdlerServiceListenerInjector, ServiceInjector;
}