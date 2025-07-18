import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.serverplug.es.resource.ElasticSearchLifeCycle;

module dawdler.server.plug.es {
	requires java.base;
	requires dawdler.core;
	requires dawdler.es.core;
	requires dawdler.server;
	requires dawdler.service.core;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ElasticSearchLifeCycle;
}
