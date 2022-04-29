module dawdler.distributed.transaction.compensator {
	exports com.anywide.dawdler.distributed.transaction.compensate.process;
	uses com.anywide.dawdler.distributed.transaction.compensate.process.DistributedTransactionCustomProcessor;
	requires java.base;
	requires dawdler.distributed.transaction.core;
	requires dawdler.core;
	requires dawdler.distributed.transaction.api;
	requires dawdler.util;
	requires dawdler.client;
	requires com.rabbitmq.client;
	requires jakarta.servlet;
	requires org.slf4j;
	requires dawdler.rabbitmq.core;
}