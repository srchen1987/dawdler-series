import com.anywide.dawdler.clientplug.remote.service.fire.ServiceClassLoaderFire;
import com.anywide.dawdler.clientplug.remote.service.resource.ServiceLifeCycle;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.remote.service {
	requires java.base;
	requires dawdler.util;
	requires dawdler.core;
	requires dawdler.serialization;
	requires dawdler.client;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires jakarta.servlet;
	requires dawdler.jakarta.fileupload;
	requires com.fasterxml.jackson.annotation;
	requires dawdler.client.plug.web;
	requires dawdler.service.core;
	requires dawdler.remote.service.core;

	provides RemoteClassLoaderFire with ServiceClassLoaderFire;
	provides ComponentLifeCycle with ServiceLifeCycle;

}