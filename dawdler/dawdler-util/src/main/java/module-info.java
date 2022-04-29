module dawdler.util {
	exports com.anywide.dawdler.util;
	exports com.anywide.dawdler.util.aspect;
	exports com.anywide.dawdler.util.reflectasm;
	exports com.anywide.dawdler.util.spring.antpath;
	
	requires java.base;
	requires java.management;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	requires java.xml;
	requires dom4j;
}