module dawdler.distributed.transaction.api {
	requires java.base;
	requires transitive org.aspectj.weaver;
	exports com.anywide.dawdler.distributed.transaction.interceptor;
	exports com.anywide.dawdler.distributed.transaction.annotation;
	exports com.anywide.dawdler.distributed.transaction.context;
	exports com.anywide.dawdler.distributed.transaction;
}