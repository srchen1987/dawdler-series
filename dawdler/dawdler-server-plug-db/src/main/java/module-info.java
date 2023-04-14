import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.serverplug.db.health.DataSourceIndicator;
import com.anywide.dawdler.serverplug.db.resource.TransactionLifeCycle;

module dawdler.server.plug.db {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.server;
	requires transitive dawdler.core;
	requires transitive java.sql;
	requires java.naming;
	requires org.slf4j;
	requires org.dom4j;
	requires dawdler.server.plug;
	requires dawdler.config.center.core;

	exports com.anywide.dawdler.serverplug.db.annotation;
	exports com.anywide.dawdler.serverplug.db.transaction;
	exports com.anywide.dawdler.serverplug.db.datasource;

	uses ComponentLifeCycle;

	provides ComponentLifeCycle with TransactionLifeCycle;

	uses HealthIndicator;

	provides HealthIndicator with DataSourceIndicator;
}