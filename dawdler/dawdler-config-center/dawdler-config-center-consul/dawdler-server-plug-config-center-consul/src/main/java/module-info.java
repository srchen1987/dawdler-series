import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.conf.consul.resource.ConfigLifeCycle;

module dawdler.server.plug.config.center.consul {
	requires dawdler.config.center.core;
	requires dawdler.core;
	requires dawdler.server;
	requires org.slf4j;
	requires dawdler.service.core;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ConfigLifeCycle;
}