module dawdler.service.core {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.core;
	requires org.slf4j;

	exports com.anywide.dawdler.core.service.annotation;
	exports com.anywide.dawdler.core.service;
	exports com.anywide.dawdler.core.service.context;
	exports com.anywide.dawdler.core.service.bean;
	exports com.anywide.dawdler.core.service.listener;
	exports com.anywide.dawdler.core.service.processor;
}