import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;

module dawdler.core {
	exports com.anywide.dawdler.core.order;
	exports com.anywide.dawdler.core.bean;
	exports com.anywide.dawdler.core.discoverycenter;
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
	opens com.anywide.dawdler.core.bean;
	uses HealthIndicator;
	uses ComponentLifeCycle;
	requires jdk.unsupported;
	requires jdk.httpserver;
	requires java.base;
	requires dawdler.util;
	requires dawdler.serialization;
	requires curator.recipes;
	requires org.apache.commons.pool2;
	requires org.slf4j;
	requires curator.client;
	requires curator.framework;
	requires zookeeper.jute; 
	requires zookeeper;
}