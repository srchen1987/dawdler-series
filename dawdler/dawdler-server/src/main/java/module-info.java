import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.shutdown.ContainerGracefulShutdown;
import club.dawdler.server.component.injector.DawdlerFilterInjector;
import club.dawdler.server.component.injector.DawdlerServiceCreateListenerInjector;
import club.dawdler.server.component.injector.DawdlerServiceListenerInjector;
import club.dawdler.server.component.injector.ServiceInjector;
import club.dawdler.server.shutdown.DawdlerServerGracefulShutdown;

module dawdler.server {
	requires transitive dawdler.util;
	requires transitive dawdler.serialization;
	requires transitive dawdler.core;
	requires java.base;
	requires jdk.httpserver;
	requires transitive java.naming;
	requires java.se;
	requires transitive org.slf4j;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;
	requires org.aspectj.weaver;
	requires transitive dawdler.service.core;

	exports club.dawdler.server.filter;
	exports club.dawdler.server.listener;
	exports club.dawdler.server.context;
	exports club.dawdler.server.thread.processor;
	exports club.dawdler.server.deploys;
	exports club.dawdler.server.net.aio.session;
	exports club.dawdler.server.conf;
	exports club.dawdler.server.service.conf;
	exports club.dawdler.server.bootstrap;
	exports club.dawdler.server.deploys.loader;
	exports club.dawdler.server.loader;

	uses CustomComponentInjector;

	provides CustomComponentInjector with DawdlerFilterInjector, DawdlerServiceCreateListenerInjector,
			DawdlerServiceListenerInjector, ServiceInjector;

	uses ContainerGracefulShutdown;

	provides ContainerGracefulShutdown with DawdlerServerGracefulShutdown;

}
