import org.slf4j.spi.SLF4JServiceProvider;

import com.anywide.dawdler.server.log.DawdlerLogbackServiceProvider;


module dawdler.server {
	exports com.anywide.dawdler.server.filter;
	exports com.anywide.dawdler.server.listener;
	exports com.anywide.dawdler.server.context;
	exports com.anywide.dawdler.server.service.listener;
	exports com.anywide.dawdler.server.bean;
	exports com.anywide.dawdler.server.thread.processor;
	exports com.anywide.dawdler.server.deploys to dawdler.server.plug.db,dawdler.server.plug.redis,dawdler.server.plug.config,dawdler.server.plug.rabbitmq,dawdler.server.plug.es;
	exports com.anywide.dawdler.server.net.aio.session;
	exports com.anywide.dawdler.server.conf to dawdler.server;
	uses SLF4JServiceProvider;
	provides SLF4JServiceProvider with DawdlerLogbackServiceProvider;
	requires dawdler.util;
	requires dawdler.serialization;
	requires dawdler.core;
	requires java.base;
	requires jdk.httpserver;
	requires java.naming;
	requires java.se;
	requires org.slf4j;
	requires dom4j;
	requires cglib;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
}