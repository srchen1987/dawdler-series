package com.anywide.dawdler.jedis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.anywide.dawdler.jedis.lock.JedisDistributedLockHolder;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisLockInjector
 * 标注一个成员变量 注入JedisDistributedLock
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JedisLockInjector {
	/**
	 * 配置文件名
	 */
	String fileName();

	/**
	 * 下一次重试等待，单位毫秒
	 */
	int intervalInMillis() default JedisDistributedLockHolder.DEFAULT_INTERVAL_IN_MILLIS;

	/**
	 * 锁的过期时长，单位毫秒
	 */
	long lockExpiryInMillis() default JedisDistributedLockHolder.DEFAULT_LOCK_EXPIRY_IN_MILLIS;

	/**
	 * 是否启用看门狗,用于延时未处理完的操作,默认开启
	 */
	boolean useWatchDog() default true;

}