module dawdler.util {
	requires java.base;
	requires java.management;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires org.objectweb.asm;
	requires org.objectweb.asm.tree;
	requires commons.jexl3;
	requires transitive java.xml;
	requires org.slf4j;
	requires com.fasterxml.jackson.dataformat.yaml;

	exports club.dawdler.util;
	exports club.dawdler.util.aspect;
	exports club.dawdler.util.reflectasm;
	exports club.dawdler.util.spring.antpath;

	opens club.dawdler.util;
}