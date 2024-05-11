module dawdler.server.plug.load {
	requires java.base;
	requires transitive dawdler.util;
	requires dawdler.server;
	requires dawdler.core;
	requires dawdler.load.bean;
	requires org.slf4j;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	requires dawdler.service.core;

	exports com.anywide.dawdler.serverplug.service;
}