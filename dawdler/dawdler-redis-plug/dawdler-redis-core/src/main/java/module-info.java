import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.redis.health.JedisIndicator;
import com.anywide.dawdler.redis.resource.JedisLifeCycle;

module dawdler.redis.core{
	exports com.anywide.dawdler.redis;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with JedisLifeCycle;
	uses HealthIndicator;
	provides HealthIndicator with JedisIndicator;
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.slf4j;
	requires org.apache.commons.pool2;
	requires redis.clients.jedis;
}