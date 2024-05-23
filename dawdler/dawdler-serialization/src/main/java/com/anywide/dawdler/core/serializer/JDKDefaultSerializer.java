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
package com.anywide.dawdler.core.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import com.anywide.dawdler.core.serializer.SerializeDecider.SerializeType;

/**
 * @author jackson.song
 * @version V1.0
 * jdk序列化实现
 */
public class JDKDefaultSerializer implements Serializer {

	@Override
	public byte[] serialize(Object object) throws Exception {
		ByteArrayOutputStream outputStream = null;
		ObjectOutputStream oos = null;
		try {
			outputStream = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(outputStream);
			oos.writeObject(object);
			return outputStream.toByteArray();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws Exception {
		ObjectInputStream ois = null;
		try {
			ois = new DawdlerObjectInputStream(new ByteArrayInputStream(bytes),
					Thread.currentThread().getContextClassLoader());
			return ois.readObject();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}
	}

	private class DawdlerObjectInputStream extends ObjectInputStream {
		private final ClassLoader classLoader;

		protected DawdlerObjectInputStream(InputStream input, ClassLoader classLoader)
				throws IOException, SecurityException {
			super(input);
			this.classLoader = classLoader;
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			String name = desc.getName();
			try {
				return super.resolveClass(desc);
			} catch (ClassNotFoundException ex) {
				return classLoader.loadClass(name);
			}
		}

	}

	@Override
	public byte key() {
		return SerializeType.JDK.getType();
	}

}
