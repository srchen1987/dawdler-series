module dawdler.distributed.transaction.compensator {
	requires java.base;
	requires transitive dawdler.distributed.transaction.core;
	requires dawdler.core;
	requires dawdler.distributed.transaction.api;
	requires dawdler.util;
	requires dawdler.client;
	requires com.rabbitmq.client;
	requires jakarta.servlet;
	requires org.slf4j;
	requires dawdler.rabbitmq.core;
	requires dawdler.service.core;
	requires dawdler.remote.service.core;

	exports com.anywide.dawdler.distributed.transaction.compensate.process;

	uses com.anywide.dawdler.distributed.transaction.compensate.process.DistributedTransactionCustomProcessor;
}