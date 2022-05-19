import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.es.health.EsIndicator;
import com.anywide.dawdler.es.restclient.resource.ElasticSearchLifeCycle;

module dawdler.es.core {
	exports com.anywide.dawdler.es.restclient;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with ElasticSearchLifeCycle;
	uses HealthIndicator;
	provides HealthIndicator with EsIndicator;
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.apache.httpcomponents.httpcore;
	requires org.apache.httpcomponents.httpclient;
	requires org.apache.commons.pool2;
	requires org.apache.httpcomponents.httpcore.nio;
	requires org.apache.httpcomponents.httpasyncclient;
	requires elasticsearch.java;
	requires elasticsearch.rest.client;
}