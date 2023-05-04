module dawdler.load.bean {
	requires java.base;
	requires transitive org.dom4j;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;

	exports com.anywide.dawdler.serverplug.bean;
	exports com.anywide.dawdler.serverplug.load.bean;
	exports com.anywide.dawdler.core.result;
}