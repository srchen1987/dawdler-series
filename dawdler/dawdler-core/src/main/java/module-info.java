import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.core.shutdown.ContainerGracefulShutdown;

module dawdler.core {
	requires jdk.unsupported;
	requires jdk.httpserver;
	requires java.base;
	requires transitive dawdler.util;
	requires dawdler.serialization;
	requires org.apache.commons.pool2;
	requires org.slf4j;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	requires transitive java.xml;
	
	exports club.dawdler.core.order;
	exports club.dawdler.core.component.injector;
	exports club.dawdler.core.bean;
	exports club.dawdler.core.thread;
	exports club.dawdler.core.rpc.context;
	exports club.dawdler.core.exception;
	exports club.dawdler.core.net.aio.session;
	exports club.dawdler.core.handler;
	exports club.dawdler.core.net.buffer;
	exports club.dawdler.core.compression.strategy;
	exports club.dawdler.core.annotation;
	exports club.dawdler.core.net.aio.handler;
	exports club.dawdler.core.component.resource;
	exports club.dawdler.core.health;
	exports club.dawdler.core.httpserver;
	exports club.dawdler.core.scan;
	exports club.dawdler.core.scan.component.reader;
	exports club.dawdler.core.shutdown;
	exports club.dawdler.core.loader;
	exports club.dawdler.core.context;

	opens club.dawdler.core.bean;
	opens club.dawdler.core.shutdown;

	uses HealthIndicator;
	uses ComponentLifeCycle;
	uses CustomComponentInjector;
	uses ContainerGracefulShutdown;

}
