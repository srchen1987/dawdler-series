import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.serverplug.es.resource.ElasticSearchLifeCycle;

module dawdler.server.plug.es {
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with ElasticSearchLifeCycle;
	requires java.base;
	requires dawdler.core;
	requires dawdler.server.plug;
	requires dawdler.es.core;
	requires dawdler.server;
}