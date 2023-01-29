import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.conf.resource.ConfigLifeCycle;

module dawdler.server.plug.config {
	requires dawdler.config.core;
	requires dawdler.core;
	requires dawdler.server;
	requires org.slf4j;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ConfigLifeCycle;
}