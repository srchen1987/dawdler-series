module dawdler.util {
	requires java.base;
	requires java.management;
	requires transitive com.fasterxml.jackson.core;
	requires transitive com.fasterxml.jackson.databind;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	requires transitive java.xml;
	requires transitive dom4j;
	requires org.slf4j;

	exports com.anywide.dawdler.util;
	exports com.anywide.dawdler.util.aspect;
	exports com.anywide.dawdler.util.reflectasm;
	exports com.anywide.dawdler.util.spring.antpath;
}