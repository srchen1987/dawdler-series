import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.conf.client.fire.ConfigLoaderFire;

module dawdler.config.clientside {
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with ConfigLoaderFire;
	requires java.base;
	requires dawdler.client.plug;
	requires dawdler.config.core;
	requires dawdler.core;
	requires org.slf4j;
}