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
package com.anywide.dawdler.clientplug.web.session.store;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.anywide.dawdler.clientplug.web.session.http.DawdlerHttpSession;
import com.anywide.dawdler.core.serializer.Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolAbstract;
import redis.clients.jedis.Pipeline;

/**
 * 
 * @Title:  RedisSessionStore.java
 * @Description:  session存储 基于redis的实现
 * @author: jackson.song    
 * @date:   2016年6月16日  
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class RedisSessionStore implements SessionStore {
	private static Logger logger = LoggerFactory.getLogger(RedisSessionStore.class);
	private JedisPoolAbstract jedisPool;
	private Serializer serializer;
	public RedisSessionStore(JedisPoolAbstract jedisPool, Serializer serializer){
		this.jedisPool = jedisPool;
		this.serializer = serializer;
	}
	 
	private <T> T execute(JedisPoolAbstract jedisPool, JedisExecutor<T> executor,Object attr) throws Exception{
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
		 
			return executor.execute(jedis,attr);
		}finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	
	public static interface JedisExecutor<T> {
		public T execute(Jedis jedis,Object attr) throws Exception;
	}
	

	/**
	 * 
		 * @Title: saveSession   
		 * @Description: 将session序列化到redis中，由于redis不支持put与expire一起执行，lua写又没办法传入hmap结构 所以采用了pipeline
		 * @param session
		 * @param serializer
		 * @throws Exception     
		 * @return void   
		 * @author: jackson.song     
		 * @date:   2016年6月16日
	 */
	@Override
	public void saveSession(DawdlerHttpSession session) throws Exception {
		execute(jedisPool,saveSessionJedisExecutor,session);
	}
	

	@Override
	public Map<byte[], byte[]> getAttributes(byte[] sessionkey) throws Exception {
		return execute(jedisPool,getAttributesJedisExecutor,sessionkey);
	}

	
	
	
	GetAttributesJedisExecutor getAttributesJedisExecutor = new GetAttributesJedisExecutor();
	public class GetAttributesJedisExecutor implements JedisExecutor<Map<byte[], byte[]>>{
		@Override
		public Map<byte[], byte[]> execute(Jedis jedis, Object attr) throws Exception {
			return jedis.hgetAll((byte[])attr);
		}
	}
	
	
	SaveSessionJedisExecutor saveSessionJedisExecutor = new SaveSessionJedisExecutor();
	public class SaveSessionJedisExecutor implements JedisExecutor<Void>{
		@Override
		public Void execute(Jedis jedis,Object attr) throws Exception {
			DawdlerHttpSession session = (DawdlerHttpSession) attr;
			Pipeline pipeline = jedis.pipelined();
			Map<String,Object> attributesAddNew = session.getAttributesAddNew();
			Map<byte[],byte[]> addData = new HashMap<byte[], byte[]>();
			attributesAddNew.forEach((k,v)->{
				try {
					addData.put(k.getBytes(),serializer.serialize(v));
				} catch (Exception e) {
					logger.error("",e);
				}
			});
			String id = session.getId();
			if(!addData.isEmpty()) 
				pipeline.hmset(id.getBytes(),addData);

			List<String> removeKeys = session.getAttributesRemoveNewKeys();
			if(!removeKeys.isEmpty()) {
				pipeline.hdel(id,removeKeys.toArray(new String[0]));
			}
			pipeline.expire(id,session.getMaxInactiveInterval());
			pipeline.sync();
			return null;
		}
	}
}
