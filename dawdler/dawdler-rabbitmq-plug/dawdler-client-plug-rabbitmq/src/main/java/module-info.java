import club.dawdler.clientplug.rabbitmq.fire.RabbitClassLoaderFire;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;

module dawdler.client.plug.rabbitmq {
	requires java.base;
	requires dawdler.core;
	requires dawdler.rabbitmq.core;
	requires dawdler.client.plug.web;

	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with RabbitClassLoaderFire;
}
