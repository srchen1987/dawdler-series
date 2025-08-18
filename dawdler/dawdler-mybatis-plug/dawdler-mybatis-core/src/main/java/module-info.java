import club.dawdler.core.component.injector.CustomComponentInjector;
import club.dawdler.core.db.mybatis.injector.AspectInjector;

module dawdler.mybatis.core {
	requires java.base;
	requires dawdler.util;
	requires transitive java.sql;
	requires org.slf4j;
	requires org.aspectj.weaver;
	requires java.naming;
	requires transitive org.mybatis;
	requires dawdler.service.core;
	requires dawdler.db.core;
	requires transitive commons.jexl3;

	exports club.dawdler.core.db.mybatis;
	exports club.dawdler.core.db.mybatis.session;
	exports club.dawdler.core.db.mybatis.annotation;

	provides CustomComponentInjector with AspectInjector;
}
