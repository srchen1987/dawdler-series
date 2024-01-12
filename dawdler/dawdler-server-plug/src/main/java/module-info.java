module dawdler.server.plug {
	requires java.base;
	requires dawdler.util;
	requires dawdler.server;
	requires dawdler.client;
	requires dawdler.core;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;

	exports com.anywide.dawdler.serverplug.service;
}