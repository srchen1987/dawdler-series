import com.anywide.dawdler.clientplug.es.fire.EsClassLoaderFire;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;

module dawdler.client.plug.es {
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug.web;
	requires dawdler.es.core;

	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with EsClassLoaderFire;
}