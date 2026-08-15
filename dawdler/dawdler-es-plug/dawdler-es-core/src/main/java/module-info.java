import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.es.health.EsIndicator;
import club.dawdler.es.restclient.resource.ElasticSearchLifeCycle;

module dawdler.es.core {
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.apache.commons.pool2;
	requires org.apache.httpcomponents.core5.httpcore5;
	requires org.apache.httpcomponents.client5.httpclient5;
	requires transitive elasticsearch.java;
	requires transitive elasticsearch.rest5.client;

	exports club.dawdler.es.restclient;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with ElasticSearchLifeCycle;

	uses HealthIndicator;

	provides HealthIndicator with EsIndicator;
}
