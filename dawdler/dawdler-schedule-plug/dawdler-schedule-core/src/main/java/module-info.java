import com.anywide.dawdler.core.shutdown.ContainerGracefulShutdown;
import com.anywide.dawdler.schedule.shutdown.ScheduleGracefulShutdown;

module dawdler.schedule.core {
	requires java.base;
	requires dawdler.core;
	requires org.slf4j;
	requires quartz;

	exports com.anywide.dawdler.schedule;
	
	uses ContainerGracefulShutdown;
	provides ContainerGracefulShutdown with ScheduleGracefulShutdown;
}