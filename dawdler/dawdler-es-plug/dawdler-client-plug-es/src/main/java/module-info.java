import com.anywide.dawdler.clientplug.es.fire.EsClassLoaderFire;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;

module dawdler.client.plug.es {
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug;
	requires dawdler.es.core;

	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with EsClassLoaderFire;
}