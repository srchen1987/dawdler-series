module dawdler.distributed.transaction.api {
	requires java.base;
	requires org.aspectj.runtime;

	exports com.anywide.dawdler.distributed.transaction.interceptor;
	exports com.anywide.dawdler.distributed.transaction.annotation;
	exports com.anywide.dawdler.distributed.transaction.context;
	exports com.anywide.dawdler.distributed.transaction;
}