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
package com.anywide.dawdler.core.net.buffer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Unsafe;

/**
 * @author jackson.song
 * @version V1.0
 * @Title DirectBufferCreator.java
 * @Description jvm堆外分配buffer
 * @date 2015年3月14日
 * @email suxuan696@gmail.com
 */
public class DirectBufferCreator implements BufferCreator {
	private static final Logger logger = LoggerFactory.getLogger(DirectBufferCreator.class);
	private static Unsafe unsafe;
	private static Constructor<?> bufferConstructor;
	static {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (Unsafe) field.get(null);
			Class<?> bufferClass = Class.forName("java.nio.DirectByteBuffer");
			bufferConstructor = bufferClass.getDeclaredConstructor(long.class, int.class);
			bufferConstructor.setAccessible(true);
		} catch (Exception e) {
			logger.error("", e);
		}
	}
	
	public static DawdlerByteBuffer createByteBufferByUnsafe(int capacity) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		long addr = unsafe.allocateMemory(capacity);
		ByteBuffer buffer = (ByteBuffer) bufferConstructor.newInstance(addr, capacity);
		DawdlerByteBuffer dawdlerByteBuffer = new DawdlerByteBuffer(buffer);
		dawdlerByteBuffer.setAddr(addr);
		return dawdlerByteBuffer;
	}
	public static void freeMemory(long addr) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		unsafe.freeMemory(addr);
	}
	@Override
	public DawdlerByteBuffer createByteBuffer(int capacity) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException {
		return createByteBufferByUnsafe(capacity);
	}
}
