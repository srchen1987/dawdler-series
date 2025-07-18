import club.dawdler.clientplug.db.mybatis.classloader.MybatisClassLoaderMatcher;
import club.dawdler.clientplug.db.mybatis.injector.AspectInjector;
import club.dawdler.clientplug.web.classloader.DawdlerClassLoaderMatcher;
import club.dawdler.core.component.injector.CustomComponentInjector;

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

	exports club.dawdler.clientplug.db.mybatis;
	exports club.dawdler.clientplug.db.mybatis.create.listener;

	opens club.dawdler.clientplug.db.mybatis;

	provides CustomComponentInjector with AspectInjector;

	provides DawdlerClassLoaderMatcher with MybatisClassLoaderMatcher;

}
