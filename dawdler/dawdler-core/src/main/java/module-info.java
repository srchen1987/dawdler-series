import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.core.shutdown.ContainerGracefulShutdown;

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

	exports com.anywide.dawdler.core.order;
	exports com.anywide.dawdler.core.component.injector;
	exports com.anywide.dawdler.core.bean;
	exports com.anywide.dawdler.core.thread;
	exports com.anywide.dawdler.core.rpc.context;
	exports com.anywide.dawdler.core.exception;
	exports com.anywide.dawdler.core.net.aio.session;
	exports com.anywide.dawdler.core.handler;
	exports com.anywide.dawdler.core.net.buffer;
	exports com.anywide.dawdler.core.compression.strategy;
	exports com.anywide.dawdler.core.annotation;
	exports com.anywide.dawdler.core.net.aio.handler;
	exports com.anywide.dawdler.core.component.resource;
	exports com.anywide.dawdler.core.health;
	exports com.anywide.dawdler.core.httpserver;
	exports com.anywide.dawdler.core.scan;
	exports com.anywide.dawdler.core.scan.component.reader;
	exports com.anywide.dawdler.core.shutdown;

	opens com.anywide.dawdler.core.bean;
	opens com.anywide.dawdler.core.shutdown;

	uses HealthIndicator;
	uses ComponentLifeCycle;
	uses CustomComponentInjector;
	uses ContainerGracefulShutdown;

}