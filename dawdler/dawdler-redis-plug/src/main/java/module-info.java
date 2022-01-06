module dawdler.redis.plug{
	exports com.anywide.dawdler.redis;
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires org.apache.commons.pool2;
	requires redis.clients.jedis;
}