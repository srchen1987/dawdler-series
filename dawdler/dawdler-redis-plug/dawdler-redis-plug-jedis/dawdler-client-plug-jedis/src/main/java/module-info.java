import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.jedis.fire.JedisClassLoaderFire;

module dawdler.client.plug.jedis {
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug;
	requires dawdler.jedis.core;
	uses RemoteClassLoaderFire;

	provides RemoteClassLoaderFire with JedisClassLoaderFire;
}