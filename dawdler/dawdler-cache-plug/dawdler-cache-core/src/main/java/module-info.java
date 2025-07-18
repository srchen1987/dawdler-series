import club.dawdler.cache.CacheManager;
import club.dawdler.cache.component.injector.CacheConfigInjector;
import club.dawdler.core.component.injector.CustomComponentInjector;

module dawdler.cache.core {
	requires java.base;
	requires commons.jexl3;
	requires dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires org.aspectj.weaver;

	exports club.dawdler.cache;
	exports club.dawdler.cache.annotation;
	exports club.dawdler.cache.exception;

	uses CustomComponentInjector;
	
	uses CacheManager;

	provides CustomComponentInjector with CacheConfigInjector;
}
