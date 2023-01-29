module dawdler.schedule.core {
	requires java.base;
	requires dawdler.core;
	requires org.slf4j;
	requires quartz;

	exports com.anywide.dawdler.schedule;
}