import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.core.db.mybatis.injector.AspectInjector;

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

	exports com.anywide.dawdler.core.db.mybatis;
	exports com.anywide.dawdler.core.db.mybatis.session;

	provides CustomComponentInjector with AspectInjector;
}