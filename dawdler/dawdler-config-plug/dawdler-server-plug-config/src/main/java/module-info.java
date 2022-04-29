import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.conf.init.ServerConfigInit;

module dawdler.server.plug.config {
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with ServerConfigInit;
	requires dawdler.config.core;
	requires dawdler.core;
	requires dawdler.server;
	requires org.slf4j;
	
}