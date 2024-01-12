import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.shutdown.ContainerGracefulShutdown;
import com.anywide.dawdler.server.component.injector.DawdlerFilterInjector;
import com.anywide.dawdler.server.component.injector.DawdlerServiceCreateListenerInjector;
import com.anywide.dawdler.server.component.injector.DawdlerServiceListenerInjector;
import com.anywide.dawdler.server.component.injector.ServiceInjector;
import com.anywide.dawdler.server.shutdown.DawdlerServerGracefulShutdown;

module dawdler.server {
	requires transitive dawdler.util;
	requires transitive dawdler.serialization;
	requires transitive dawdler.core;
	requires java.base;
	requires jdk.httpserver;
	requires transitive java.naming;
	requires java.se;
	requires transitive org.slf4j;
	requires cglib;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires org.aspectj.weaver;

	exports com.anywide.dawdler.server.filter;
	exports com.anywide.dawdler.server.listener;
	exports com.anywide.dawdler.server.context;
	exports com.anywide.dawdler.server.service.listener;
	exports com.anywide.dawdler.server.bean;
	exports com.anywide.dawdler.server.thread.processor;
	exports com.anywide.dawdler.server.deploys;
	exports com.anywide.dawdler.server.net.aio.session;
	exports com.anywide.dawdler.server.conf;
	exports com.anywide.dawdler.server.service.conf;
	exports com.anywide.dawdler.server.deploys.loader;
	exports com.anywide.dawdler.server.bootstrap;
	exports com.anywide.dawdler.server.loader;
	exports com.anywide.dawdler.server.service;

	uses CustomComponentInjector;

	provides CustomComponentInjector with DawdlerFilterInjector, DawdlerServiceCreateListenerInjector,
			DawdlerServiceListenerInjector, ServiceInjector;

	uses ContainerGracefulShutdown;

	provides ContainerGracefulShutdown with DawdlerServerGracefulShutdown;

}