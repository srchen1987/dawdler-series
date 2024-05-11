import com.anywide.dawdler.core.serializer.JDKDefaultSerializer;
import com.anywide.dawdler.core.serializer.KryoSerializer;
import com.anywide.dawdler.core.serializer.Serializer;

module dawdler.serialization {
	requires java.base;
	requires org.objenesis;
	requires org.slf4j;
	requires transitive com.esotericsoftware.kryo;
	requires jdk.unsupported;

	exports com.anywide.dawdler.core.serializer;

	uses Serializer;

	provides Serializer with JDKDefaultSerializer, KryoSerializer;
}