import com.anywide.dawdler.clientplug.web.session.initializer.DawdlerSessionInitializer;

import jakarta.servlet.ServletContainerInitializer;

module dawdler.client.plug.session {
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires dawdler.serialization;
	requires redis.clients.jedis;
	requires dawdler.jedis.core;
	requires com.github.benmanes.caffeine;
	requires jakarta.servlet;
	requires dawdler.core;

	exports com.anywide.dawdler.clientplug.web.session;

	uses ServletContainerInitializer;

	provides ServletContainerInitializer with DawdlerSessionInitializer;
}