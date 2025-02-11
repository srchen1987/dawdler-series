import com.anywide.dawdler.core.db.health.DataSourceIndicator;
import com.anywide.dawdler.core.health.HealthIndicator;

module dawdler.db.core {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.core;
	requires transitive java.sql;
	requires java.naming;
	requires org.slf4j;
	requires dawdler.service.core;
	requires transitive org.aspectj.weaver;

	exports com.anywide.dawdler.core.db;
	exports com.anywide.dawdler.core.db.aspect;
	exports com.anywide.dawdler.core.db.conf;
	exports com.anywide.dawdler.core.db.annotation;
	exports com.anywide.dawdler.core.db.transaction;
	exports com.anywide.dawdler.core.db.exception;
	exports com.anywide.dawdler.core.db.datasource;
	exports com.anywide.dawdler.core.db.sub;
	exports com.anywide.dawdler.core.db.sub.rule;

	uses HealthIndicator;

	provides HealthIndicator with DataSourceIndicator;
}