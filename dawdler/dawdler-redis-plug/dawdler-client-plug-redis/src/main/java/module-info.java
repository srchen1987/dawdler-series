import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.redis.fire.JedisClassLoaderFire;

module dawdler.client.plug.redis {
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with JedisClassLoaderFire;
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug;
	requires dawdler.redis.core;
}