import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.rabbitmq.fire.RabbitClassLoaderFire;

module dawdler.client.plug.rabbitmq{
	requires java.base;
	requires dawdler.core;
	requires dawdler.rabbitmq.core;
	requires dawdler.client.plug;
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with RabbitClassLoaderFire;
}