import com.anywide.dawdler.clientplug.conf.consul.fire.ConfigLoaderFire;
import com.anywide.dawdler.clientplug.conf.consul.resource.ConfigLifeCycle;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;

module dawdler.client.plug.config.center.consul {
	requires dawdler.config.center.consul.core;
	requires dawdler.client.plug.web;
	requires dawdler.core;
	requires dawdler.config.center.core;

	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with ConfigLoaderFire;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ConfigLifeCycle;
}