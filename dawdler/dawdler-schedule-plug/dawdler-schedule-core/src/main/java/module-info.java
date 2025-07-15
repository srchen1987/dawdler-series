import club.dawdler.core.shutdown.ContainerGracefulShutdown;
import club.dawdler.schedule.shutdown.ScheduleGracefulShutdown;

module dawdler.schedule.core {
	requires java.base;
	requires dawdler.core;
	requires org.slf4j;
	requires org.quartz;

	exports club.dawdler.schedule;
	
	uses ContainerGracefulShutdown;
	provides ContainerGracefulShutdown with ScheduleGracefulShutdown;
}
