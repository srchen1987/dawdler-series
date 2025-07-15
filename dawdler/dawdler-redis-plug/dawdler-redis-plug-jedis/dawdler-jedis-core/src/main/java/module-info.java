import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.core.health.HealthIndicator;
import club.dawdler.jedis.health.JedisIndicator;
import club.dawdler.jedis.resource.JedisLifeCycle;

module dawdler.jedis.core {
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.slf4j;
	requires org.apache.commons.pool2;
	requires transitive redis.clients.jedis;

	exports club.dawdler.jedis;
	exports club.dawdler.jedis.lock;

	uses ComponentLifeCycle;
	provides ComponentLifeCycle with JedisLifeCycle;

	uses HealthIndicator;
	provides HealthIndicator with JedisIndicator;
}
