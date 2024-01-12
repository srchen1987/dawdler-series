import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.jedis.health.JedisIndicator;
import com.anywide.dawdler.jedis.resource.JedisLifeCycle;

module dawdler.jedis.core {
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.slf4j;
	requires org.apache.commons.pool2;
	requires transitive redis.clients.jedis;

	exports com.anywide.dawdler.jedis;
	exports com.anywide.dawdler.jedis.lock;

	uses ComponentLifeCycle;
	provides ComponentLifeCycle with JedisLifeCycle;

	uses HealthIndicator;
	provides HealthIndicator with JedisIndicator;
}