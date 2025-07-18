module dawdler.load.bean {
	requires java.base;
	requires transitive java.xml;
	requires com.fasterxml.jackson.annotation;

	exports club.dawdler.serverplug.bean;
	exports club.dawdler.serverplug.load.bean;
	exports club.dawdler.core.result;

	opens club.dawdler.serverplug.load.bean;
	opens club.dawdler.serverplug.bean;
	opens club.dawdler.core.result;
}