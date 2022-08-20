import com.anywide.dawdler.clientplug.load.classloader.RemoteClassLoaderFire;
import com.anywide.dawdler.clientplug.schedule.fire.ScheduleClassLoaderFire;

module dawdler.client.plug.schedule{
	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with ScheduleClassLoaderFire;
	requires java.base;
	requires dawdler.client.plug;
	requires dawdler.core;
	requires dawdler.schedule.core;
}