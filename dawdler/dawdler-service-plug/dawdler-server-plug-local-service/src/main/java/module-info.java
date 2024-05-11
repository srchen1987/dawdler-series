import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.local.service.resource.LocalServiceLifeCycle;

module dawdler.server.plug.local.service {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.core;
	requires org.slf4j;
	requires dawdler.local.service.core;
	requires dawdler.server;
	requires dawdler.service.core;
	
	provides ComponentLifeCycle with LocalServiceLifeCycle;
}