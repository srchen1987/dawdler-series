import com.anywide.dawdler.clientplug.jedis.fire.JedisClassLoaderFire;
import com.anywide.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;

module dawdler.client.plug.jedis {
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug.web;
	requires dawdler.jedis.core;
	
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with JedisClassLoaderFire;
}