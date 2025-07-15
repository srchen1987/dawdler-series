module dawdler.service.core {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.core;
	requires org.slf4j;

	exports club.dawdler.core.service.annotation;
	exports club.dawdler.core.service;
	exports club.dawdler.core.service.context;
	exports club.dawdler.core.service.bean;
	exports club.dawdler.core.service.listener;
	exports club.dawdler.core.service.processor;
}