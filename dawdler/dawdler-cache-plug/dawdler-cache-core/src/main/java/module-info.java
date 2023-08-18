module dawdler.cache.core {
	requires java.base;
	requires commons.jexl3;
	requires dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires org.aspectj.weaver;

	exports com.anywide.dawdler.cache;
	exports com.anywide.dawdler.cache.annotation;
	exports com.anywide.dawdler.cache.exception;
}