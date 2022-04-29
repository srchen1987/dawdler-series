import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.redis.resource.JedisLifeCycle;

module dawdler.redis.core{
	exports com.anywide.dawdler.redis;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with JedisLifeCycle;
	requires java.base;
	requires dawdler.core;
	requires dawdler.util;
	requires org.slf4j;
	requires org.apache.commons.pool2;
	requires redis.clients.jedis;
}