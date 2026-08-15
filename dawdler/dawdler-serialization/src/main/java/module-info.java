import club.dawdler.serializer.JDKDefaultSerializer;
import club.dawdler.serializer.KryoSerializer;
import club.dawdler.serializer.Serializer;

module dawdler.serialization {
	requires java.base;
	requires org.objenesis;
	requires org.slf4j;
	requires transitive com.esotericsoftware.kryo;
	requires jdk.unsupported;

	exports club.dawdler.serializer;

	uses Serializer;

	provides Serializer with JDKDefaultSerializer, KryoSerializer;
}
