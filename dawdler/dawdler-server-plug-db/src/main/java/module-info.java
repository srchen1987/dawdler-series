module dawdler.server.plug.db {
	exports com.anywide.dawdler.serverplug.db.init;
	exports com.anywide.dawdler.serverplug.db.annotation;
	exports com.anywide.dawdler.serverplug.db.transaction;
	exports com.anywide.dawdler.serverplug.db.datasource;
	requires java.base;
	requires dawdler.util;
	requires dawdler.server;
	requires dawdler.core;
	requires java.sql;
	requires java.naming;
	requires org.slf4j;
	requires dom4j;
	requires dawdler.server.plug;
}