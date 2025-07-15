module dawdler.server.plug.mybatis {
	requires java.base;
	requires dawdler.util;
	requires java.sql;
	requires org.slf4j;
	requires org.aspectj.weaver;
	requires java.naming;
	requires org.mybatis;
	requires dawdler.service.core;
	requires dawdler.db.core;
	requires dawdler.mybatis.core;
	requires dawdler.server;
	
	exports club.dawdler.serverplug.db.mybatis.listener;
}