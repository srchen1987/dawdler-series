module dawdler.rabbitmq.plug{
	exports com.anywide.dawdler.rabbitmq.connection.pool.factory;
	requires java.base;
	requires org.apache.commons.pool2;
	requires dawdler.util;
	requires com.rabbitmq.client;
	requires org.slf4j;
}