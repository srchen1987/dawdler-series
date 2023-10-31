import com.anywide.dawdler.core.component.injector.CustomComponentInjector;
import com.anywide.dawdler.serverplug.db.mybatis.injector.AspectInjector;

module dawdler.server.plug.mybatis {
	requires java.base;
	requires dawdler.server.plug;
	requires dawdler.util;
	requires dawdler.server;
	requires dawdler.server.plug.db;
	requires java.sql;
	requires org.slf4j;
	requires org.aspectj.weaver;
	requires java.naming;
	requires org.mybatis;
	
	uses CustomComponentInjector;

	provides CustomComponentInjector with AspectInjector;
}