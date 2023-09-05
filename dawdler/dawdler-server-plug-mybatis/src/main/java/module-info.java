module dawdler.server.plug.mybatis {
	requires java.base;
	requires dawdler.server.plug;
	requires dawdler.util;
	requires dawdler.server;
	requires dawdler.server.plug.db;
	requires java.sql;
	requires org.slf4j;
	requires org.aspectj.weaver;
//	requires cglib;
//	requires javassist;
	requires java.naming;
//	requires ognl;
//	requires dawdler.mybatis.core;
	requires org.mybatis;
}