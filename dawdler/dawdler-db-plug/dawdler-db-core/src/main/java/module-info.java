import club.dawdler.core.db.health.DataSourceIndicator;
import club.dawdler.core.health.HealthIndicator;

module dawdler.db.core {
	requires java.base;
	requires dawdler.util;
	requires transitive dawdler.core;
	requires transitive java.sql;
	requires java.naming;
	requires org.slf4j;
	requires dawdler.service.core;
	requires transitive org.aspectj.weaver;
	requires commons.jexl3;

	exports club.dawdler.core.db;
	exports club.dawdler.core.db.aspect;
	exports club.dawdler.core.db.conf;
	exports club.dawdler.core.db.annotation;
	exports club.dawdler.core.db.transaction;
	exports club.dawdler.core.db.exception;
	exports club.dawdler.core.db.datasource;
	exports club.dawdler.core.db.sub;
	exports club.dawdler.core.db.sub.rule;

	uses HealthIndicator;

	provides HealthIndicator with DataSourceIndicator;
}
