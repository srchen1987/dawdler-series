import org.slf4j.spi.SLF4JServiceProvider;

import com.anywide.dawdler.server.log.DawdlerLogbackServiceProvider;

module dawdler.server.logback {
	requires java.base;
	requires org.slf4j;
	requires ch.qos.logback.classic;
	requires ch.qos.logback.core;

	uses SLF4JServiceProvider;

	provides SLF4JServiceProvider with DawdlerLogbackServiceProvider;
}