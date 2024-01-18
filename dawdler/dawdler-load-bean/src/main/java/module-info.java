module dawdler.load.bean {
	requires java.base;
	requires transitive java.xml;
	requires com.fasterxml.jackson.annotation;

	exports com.anywide.dawdler.serverplug.bean;
	exports com.anywide.dawdler.serverplug.load.bean;
	exports com.anywide.dawdler.core.result;

	opens com.anywide.dawdler.serverplug.load.bean;
	opens com.anywide.dawdler.serverplug.bean;
	opens com.anywide.dawdler.core.result;
}