import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.es.health.EsIndicator;
import club.dawdler.es.restclient.resource.ElasticSearchLifeCycle;

module dawdler.es.core {
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.apache.httpcomponents.httpcore;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.commons.pool2;
	requires org.apache.httpcomponents.httpcore.nio;
	requires org.apache.httpcomponents.httpasyncclient;
	requires transitive elasticsearch.java;
	requires transitive elasticsearch.rest.client;

	exports club.dawdler.es.restclient;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ElasticSearchLifeCycle;

	uses HealthIndicator;

	provides HealthIndicator with EsIndicator;
}
