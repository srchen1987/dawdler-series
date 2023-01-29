module dawdler.load.bean{
	requires java.base;
	requires transitive dom4j;
	exports com.anywide.dawdler.serverplug.bean;
	exports com.anywide.dawdler.serverplug.load.bean;
}