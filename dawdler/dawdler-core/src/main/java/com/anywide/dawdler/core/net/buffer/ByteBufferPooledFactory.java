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

import java.nio.ByteBuffer;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import sun.nio.ch.DirectBuffer;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ByteBufferPooledFactory.java
 * @Description 通过apache的Pooled组件实现了 buffer池，极端情况下才会使用到
 * @date 2015年3月14日
 * @email suxuan696@gmail.com
 */
public class ByteBufferPooledFactory implements PooledObjectFactory<ByteBuffer> {
	private final int capacity;

	public ByteBufferPooledFactory(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public PooledObject<ByteBuffer> makeObject() throws Exception {
		return new DefaultPooledObject<ByteBuffer>(BufferFactory.createDirectBuffer(capacity));
	}

	@Override
	public void destroyObject(PooledObject<ByteBuffer> p) throws Exception {
		ByteBuffer byteBuffer = p.getObject();
		if (byteBuffer.isDirect())
			((DirectBuffer) byteBuffer).cleaner().clean();
		else
			byteBuffer.clear();
	}

	@Override
	public boolean validateObject(PooledObject<ByteBuffer> p) {
		return true;
	}

	@Override
	public void activateObject(PooledObject<ByteBuffer> p) throws Exception {

	}

	@Override
	public void passivateObject(PooledObject<ByteBuffer> p) throws Exception {

	}

}
