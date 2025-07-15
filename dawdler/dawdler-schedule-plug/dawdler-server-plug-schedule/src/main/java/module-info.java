import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.serverplug.schedule.resource.ScheduleLifeCycle;

module dawdler.server.plug.schedule {
	requires java.base;
	requires dawdler.core;
	requires dawdler.schedule.core;
	requires dawdler.server;
	requires dawdler.service.core;
	
	provides ComponentLifeCycle with ScheduleLifeCycle;
}
