import club.dawdler.clientplug.local.service.fire.ServiceClassLoaderFire;
import club.dawdler.clientplug.local.service.injector.DawdlerServiceCreateListenerInjector;
import club.dawdler.clientplug.local.service.injector.ServiceInjector;
import club.dawdler.clientplug.local.service.resource.ServiceCreateListenerLifeCycle;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.local.service {
	requires java.base;
	requires org.slf4j;
	requires dawdler.client.plug.web;
	requires transitive dawdler.service.core;
	requires transitive dawdler.local.service.core;

	exports club.dawdler.clientplug.local.service.context;

	provides RemoteClassLoaderFire with ServiceClassLoaderFire;
	provides CustomComponentInjector with ServiceInjector,DawdlerServiceCreateListenerInjector;
	provides ComponentLifeCycle with ServiceCreateListenerLifeCycle;

}
