import com.anywide.dawdler.clientplug.web.session.initializer.DawdlerSessionInitializer;

import jakarta.servlet.ServletContainerInitializer;

module dawdler.client.plug.session {
	requires java.base;
	requires dawdler.util;
	requires org.slf4j;
	requires transitive dawdler.serialization;
	requires transitive redis.clients.jedis;
	requires dawdler.jedis.core;
	requires com.github.benmanes.caffeine;
	requires jakarta.servlet;
	requires dawdler.core;

	exports com.anywide.dawdler.clientplug.web.session;
	exports com.anywide.dawdler.clientplug.web.session.http;
	exports com.anywide.dawdler.clientplug.web.session.message;
	exports com.anywide.dawdler.clientplug.web.session.base;
	exports com.anywide.dawdler.clientplug.web.session.store;

	uses ServletContainerInitializer;

	provides ServletContainerInitializer with DawdlerSessionInitializer;
}
