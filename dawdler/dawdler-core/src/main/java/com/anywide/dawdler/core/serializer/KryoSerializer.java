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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.esotericsoftware.kryo.io.UnsafeOutput;
/**
 * 
 * @Title:  KryoSerializer.java
 * @Description:    kroy实现的序列化 目前升级到最新版本 4x   
 * @author: jackson.song    
 * @date:   2014年12月22日     
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class KryoSerializer implements Serializer {
	private static final ThreadLocal<KryoLocal> kryos = new ThreadLocal<KryoLocal>() {
		protected KryoLocal initialValue() {
			KryoLocal kryoLocal = new KryoLocal();
			return kryoLocal;
		};
	};
	public static class KryoLocal{
		private Kryo kryo;
		private Input input;
		private Output out;
		public KryoLocal() {
			kryo = new Kryo();
			kryo.setReferences(true);
			kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
			
//			  ((Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy())
//              .setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
			input = new UnsafeInput();
			out = new UnsafeOutput(2048,-1);
		}
		public Kryo getKryo() {
			kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
			return kryo;
		}
		public void setKryo(Kryo kryo) {
			this.kryo = kryo;
		}
	}
	@Override
	public Object deserialize(byte[] bytes) throws Exception {
		KryoLocal kryoLocal = kryos.get();
		Kryo kryo = kryoLocal.getKryo();
		Input input = kryoLocal.input;
//		Input input = new UnsafeInput();
		input.setBuffer(bytes);
	Object obj = kryo.readClassAndObject(input);
		return obj;
	}

	@Override
	public byte[] serialize(Object object) throws Exception {
		KryoLocal kryoLocal = kryos.get();
		Kryo kryo = kryoLocal.kryo;
		Output out = kryoLocal.out;
//		Output out = new UnsafeOutput(2048,-1);
		byte []datas = null;
		try {
			kryo.writeClassAndObject(out, object);
			datas = out.toBytes();
		}finally {
			out.clear();
		}
		return datas;
	}
	public static void main(String[] args) throws Exception {
		Thread.sleep(5000);
		int count = 65535;
		ExecutorService es = Executors.newFixedThreadPool(32);
		CountDownLatch dl = new CountDownLatch(count);
		long t1 = System.currentTimeMillis();
		for(int i=0;i<count;i++) {
			es.execute(()->{
				KryoSerializer sk = new KryoSerializer();
				byte[] data;
				try {
					Map map = new HashMap();
					map.put("jackson", "12345676");
					for(int j=0;j<10;j++) {
						data = sk.serialize(map);
						sk.deserialize(data);
						data = sk.serialize(map);
						sk.deserialize(data);
						data = sk.serialize(map);
						sk.deserialize(data);
						data = sk.serialize(map);
						sk.deserialize(data);
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dl.countDown();
			});
		}
		dl.await();
		long t2 = System.currentTimeMillis();
		System.out.println("cost:"+(t2-t1));
		es.shutdown();
		
	}
}
