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
package club.dawdler.serializer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.strategy.StdInstantiatorStrategy;

import club.dawdler.serializer.SerializeDecider.SerializeType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;

/**
 * @author jackson.song
 * @version V1.0
 * kryo实现的序列化 目前升级到新版本 5x
 */
public class KryoSerializer implements Serializer {
	private static final StdInstantiatorStrategy STD_INSTANTIATOR_STRATEGY = new StdInstantiatorStrategy();
	private static final Set<KryoLocal> TRACKER = ConcurrentHashMap.newKeySet();
	private static final ThreadLocal<KryoLocal> kryos = ThreadLocal.withInitial(() -> {
		KryoLocal kryoLocal = new KryoLocal();
		TRACKER.add(kryoLocal);
		return kryoLocal;
	});

	@Override
	public Object deserialize(byte[] bytes) throws Exception {
		KryoLocal kryoLocal = kryos.get();
		Kryo kryo = kryoLocal.getKryo();
		Input input = kryoLocal.input;
		input.setBuffer(bytes);
		return kryo.readClassAndObject(input);
	}

	@Override
	public byte[] serialize(Object object) throws Exception {
		KryoLocal kryoLocal = kryos.get();
		Kryo kryo = kryoLocal.kryo;
		Output out = kryoLocal.out;
		try {
			kryo.writeClassAndObject(out, object);
			return out.toBytes();
		} finally {
			out.reset();
		}
	}

	public static class KryoLocal {
		private Kryo kryo;
		private final Input input;
		private final Output out;

		public KryoLocal() {
			kryo = new Kryo();
			kryo.setReferences(true);
			kryo.setRegistrationRequired(false);
			kryo.addDefaultSerializer(java.lang.Throwable.class, JavaSerializer.class);
			((DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
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
		TRACKER.forEach(KryoLocal::close);
		TRACKER.clear();
	}
}
