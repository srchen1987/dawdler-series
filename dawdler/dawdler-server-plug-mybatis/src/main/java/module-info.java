module dawdler.server.plug.mybatis {
	requires java.base;
	requires dawdler.server.plug;
	requires dawdler.util;
	requires dawdler.server;
	requires dawdler.server.plug.db;
	requires java.sql;
	requires org.slf4j;
	requires org.aspectj.runtime;
	requires cglib;
	requires javassist;
	requires java.naming;
	requires ognl;
	requires org.dom4j;
	requires dawdler.mybatis.core;
}