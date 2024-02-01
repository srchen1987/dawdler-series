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

import java.util.HashMap;
import java.util.Map;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.anywide.dawdler.core.serializer.SerializeDecider.SerializeType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;
import com.esotericsoftware.kryo.serializers.JavaSerializer;

/**
 * @author jackson.song
 * @version V1.0
 * @Title KryoSerializer.java
 * @Description kryo实现的序列化 目前升级到新版本 4x
 * @date 2014年12月22日
 * @email suxuan696@gmail.com
 */
public class KryoSerializer implements Serializer {
	private static Map<Thread, KryoLocal> kryos = new HashMap<Thread, KryoSerializer.KryoLocal>();
	private static final StdInstantiatorStrategy STD_INSTANTIATOR_STRATEGY = new StdInstantiatorStrategy();

	private KryoLocal initialValue() {
		KryoLocal kryoLocal = new KryoLocal();
		return kryoLocal;
	}

	public KryoLocal getKryoLocal() {
		Thread thread = Thread.currentThread();
		KryoLocal kryoLocal = kryos.get(thread);
		if (kryoLocal != null) {
			return kryoLocal;
		}
		kryoLocal = initialValue();
		kryos.put(thread, kryoLocal);
		return kryoLocal;
	}

	@Override
	public Object deserialize(byte[] bytes) {
		KryoLocal kryoLocal = getKryoLocal();
		Kryo kryo = kryoLocal.getKryo();
		Input input = kryoLocal.input;
		input.setBuffer(bytes);
		return kryo.readClassAndObject(input);
	}

	@Override
	public byte[] serialize(Object object) throws Exception {
		KryoLocal kryoLocal = getKryoLocal();
		Kryo kryo = kryoLocal.kryo;
		Output out = kryoLocal.out;
		byte[] data;
		try {
			kryo.writeClassAndObject(out, object);
			data = out.toBytes();
		} finally {
			out.clear();
		}
		return data;
	}

	public static class KryoLocal {
		private Kryo kryo;
		private final Input input;
		private final Output out;

		public KryoLocal() {
			kryo = new Kryo();
			kryo.setReferences(true);
			kryo.addDefaultSerializer(java.lang.Throwable.class, JavaSerializer.class);
			((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
					.setFallbackInstantiatorStrategy(STD_INSTANTIATOR_STRATEGY);
			input = new UnsafeInput();
			out = new UnsafeOutput(2048, -1);
		}

		public Kryo getKryo() {
			kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
			return kryo;
		}

		public void setKryo(Kryo kryo) {
			this.kryo = kryo;
		}

		public void close() {
			input.close();
			out.close();
		}
	}

	@Override
	public byte key() {
		return SerializeType.KRYO.getType();
	}

	@Override
	public void destroyed() {
		kryos.forEach((k, v) -> {
			v.close();
		});
		kryos.clear();
	}
}
