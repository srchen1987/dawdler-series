module dawdler.distributed.transaction.api {
	requires java.base;
	requires transitive org.aspectj.weaver;

	exports club.dawdler.distributed.transaction.interceptor;
	exports club.dawdler.distributed.transaction.annotation;
	exports club.dawdler.distributed.transaction.context;
	exports club.dawdler.distributed.transaction;
}