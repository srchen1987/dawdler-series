import club.dawdler.core.serializer.JDKDefaultSerializer;
import club.dawdler.core.serializer.KryoSerializer;
import club.dawdler.core.serializer.Serializer;

module dawdler.serialization {
	requires java.base;
	requires org.objenesis;
	requires org.slf4j;
	requires transitive com.esotericsoftware.kryo;
	requires jdk.unsupported;

	exports club.dawdler.core.serializer;

	uses Serializer;

	provides Serializer with JDKDefaultSerializer, KryoSerializer;
}
