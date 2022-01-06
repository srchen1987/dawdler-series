module dawdler.server {
	exports com.anywide.dawdler.server.filter;
	exports com.anywide.dawdler.server.listener;
	exports com.anywide.dawdler.server.context;
	exports com.anywide.dawdler.server.service.listener;
	exports com.anywide.dawdler.server.bean;
	exports com.anywide.dawdler.server.thread.processor;
	exports com.anywide.dawdler.server.deploys to dawdler.server.plug.db;
	exports com.anywide.dawdler.server.net.aio.session;
	exports com.anywide.dawdler.server.conf to dawdler.server;

	requires dawdler.util;
	requires dawdler.serialization;
	requires dawdler.core;
	requires java.base;
	requires java.naming;
	requires java.se;
	requires org.slf4j;
	requires dom4j;
	requires cglib;
}