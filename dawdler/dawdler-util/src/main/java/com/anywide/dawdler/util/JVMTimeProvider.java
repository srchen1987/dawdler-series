package com.anywide.dawdler.util;

import java.util.concurrent.TimeUnit;
/**
 * 
 * @ClassName:  JVMTimeProvider   
 * @Description:   
 * @author: srchen    
 * @date:   2015年11月7日 上午10:35:12
 */
public final class JVMTimeProvider {
	private static volatile long currentTimeMillis;
	static {
		currentTimeMillis = System.currentTimeMillis();
		Thread daemon = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					currentTimeMillis = System.currentTimeMillis();
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (Throwable e) {

					}
				}
			}
		});
		daemon.setDaemon(false);
		daemon.setName("dawdler-time-tick-thread");
		daemon.start();
	}

	public static long currentTimeMillis() {
		return currentTimeMillis;
	}

	public static long currentTimeSeconds() {
		return currentTimeMillis / 1000;
	}
}
