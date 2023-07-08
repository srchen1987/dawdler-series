module dawdler.load.bean {
	requires java.base;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires transitive java.xml;

	exports com.anywide.dawdler.serverplug.bean;
	exports com.anywide.dawdler.serverplug.load.bean;
	exports com.anywide.dawdler.core.result;
}