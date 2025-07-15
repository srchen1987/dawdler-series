module dawdler.remote.service.core {
	requires java.base;
	requires dawdler.service.core;
	requires dawdler.core;
	requires dawdler.client;
	
	exports club.dawdler.remote.service.annotation;
	exports club.dawdler.remote.service.injector;
	exports club.dawdler.remote.service.factory;
}