/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	private static volatile boolean stop = false;
	static {
		currentTimeMillis = System.currentTimeMillis();
		Thread daemon = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!stop) {
					currentTimeMillis = System.currentTimeMillis();
					try {
						TimeUnit.MILLISECONDS.sleep(1);
					} catch (Throwable e) {

					}
				}
			}
		});
		daemon.setDaemon(true);
		daemon.setName("dawdler-time-tick-thread");
		daemon.start();
	}
	
	public static void stop() {
		stop = true;
	}

	public static long currentTimeMillis() {
		return currentTimeMillis;
	}

	public static int currentTimeSeconds() {
		return (int) (currentTimeMillis / 1000);
	}
}
