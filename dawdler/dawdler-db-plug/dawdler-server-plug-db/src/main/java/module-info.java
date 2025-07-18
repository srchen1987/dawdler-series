import club.dawdler.core.component.resource.ComponentLifeCycle;
import club.dawdler.serverplug.db.resource.TransactionLifeCycle;

module dawdler.server.plug.db {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.server;
	requires transitive dawdler.core;
	requires transitive java.sql;
	requires java.naming;
	requires org.slf4j;
	requires dawdler.db.core;

	provides ComponentLifeCycle with TransactionLifeCycle;

}
