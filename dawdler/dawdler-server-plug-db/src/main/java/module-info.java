import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.serverplug.db.health.DataSourceIndicator;
import com.anywide.dawdler.serverplug.db.resource.TransactionLifeCycle;

module dawdler.server.plug.db {
	exports com.anywide.dawdler.serverplug.db.annotation;
	exports com.anywide.dawdler.serverplug.db.transaction;
	exports com.anywide.dawdler.serverplug.db.datasource;
	uses ComponentLifeCycle;
	provides ComponentLifeCycle with TransactionLifeCycle;
	uses HealthIndicator;
	provides HealthIndicator with DataSourceIndicator;
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