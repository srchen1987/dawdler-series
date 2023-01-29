import com.anywide.dawdler.core.serializer.JDKDefaultSerializer;
import com.anywide.dawdler.core.serializer.KryoSerializer;
import com.anywide.dawdler.core.serializer.Serializer;

module dawdler.serialization {
	requires java.base;
	requires objenesis;
	requires org.slf4j;
	requires kryo;
	exports com.anywide.dawdler.core.serializer;
	uses Serializer;
	provides Serializer with JDKDefaultSerializer,KryoSerializer;
}