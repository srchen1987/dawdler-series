import club.dawdler.clientplug.schedule.fire.ScheduleClassLoaderFire;
import club.dawdler.clientplug.web.classloader.RemoteClassLoaderFire;

module dawdler.client.plug.schedule {
	requires java.base;
	requires dawdler.client.plug.web;
	requires dawdler.core;
	requires dawdler.schedule.core;

	uses RemoteClassLoaderFire;
	provides RemoteClassLoaderFire with ScheduleClassLoaderFire;
}
