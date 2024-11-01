import com.anywide.dawdler.clientplug.db.mybatis.classloader.MybatisClassLoaderMatcher;
import com.anywide.dawdler.clientplug.db.mybatis.injector.AspectInjector;
import com.anywide.dawdler.clientplug.web.classloader.DawdlerClassLoaderMatcher;
import com.anywide.dawdler.core.component.injector.CustomComponentInjector;

module dawdler.client.plug.mybatis {
	requires java.base;
	requires dawdler.util;
	requires java.sql;
	requires org.slf4j;
	requires org.mybatis;
	requires dawdler.service.core;
	requires dawdler.db.core;
	requires dawdler.client.plug.web;
	requires dawdler.mybatis.core;

	exports com.anywide.dawdler.clientplug.db.mybatis;
	exports com.anywide.dawdler.clientplug.db.mybatis.create.listener;

	opens com.anywide.dawdler.clientplug.db.mybatis;

	provides CustomComponentInjector with AspectInjector;

	provides DawdlerClassLoaderMatcher with MybatisClassLoaderMatcher;

}