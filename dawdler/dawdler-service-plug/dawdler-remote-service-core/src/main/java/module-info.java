module dawdler.remote.service.core {
	requires java.base;
	requires dawdler.service.core;
	requires dawdler.core;
	requires dawdler.client;
	
	exports com.anywide.dawdler.remote.service.annotation;
	exports com.anywide.dawdler.remote.service.injector;
	exports com.anywide.dawdler.remote.service.factory;
}