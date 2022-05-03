import com.anywide.dawdler.clientplug.conf.fire.ConfigLoaderFire;
import com.anywide.dawdler.clientplug.conf.resource.ConfigLifeCycle;
import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.config {
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with ConfigLoaderFire;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with ConfigLifeCycle;
	requires dawdler.config.core;
	requires dawdler.client.plug;
	requires dawdler.core;
	
}