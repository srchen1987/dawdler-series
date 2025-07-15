import club.dawdler.clientplug.jedis.fire.JedisClassLoaderFire;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;

module dawdler.client.plug.jedis {
	requires java.base;
	requires dawdler.core;
	requires dawdler.client.plug.web;
	requires dawdler.jedis.core;
	
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with JedisClassLoaderFire;
}
