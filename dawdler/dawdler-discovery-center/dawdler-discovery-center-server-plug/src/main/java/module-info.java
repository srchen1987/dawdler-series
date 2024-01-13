module dawdler.discovery.center.server.plug {
	requires org.slf4j;
	requires dawdler.core;
	requires transitive dawdler.util;
	requires dawdler.server;
	requires dawdler.discovery.center.core;

	exports com.anywide.dawdler.server.plug.discoverycenter;
}