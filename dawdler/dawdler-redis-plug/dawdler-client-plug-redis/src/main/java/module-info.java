import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.redis.fire.JedisClassLoaderFire;

module dawdler.client.plug.redis {
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug;
	requires dawdler.redis.core;
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with JedisClassLoaderFire;
}