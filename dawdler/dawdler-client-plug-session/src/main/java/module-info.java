module dawdler.client.plug.session {
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires dawdler.serialization;
	requires redis.clients.jedis;
	requires dawdler.redis.core;
	requires com.github.benmanes.caffeine;
	requires jakarta.servlet;
}