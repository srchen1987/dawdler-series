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
package com.anywide.dawdler.redis;

import java.io.Closeable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.ClusterCommands;
import redis.clients.jedis.commands.ControlBinaryCommands;
import redis.clients.jedis.commands.ControlCommands;
import redis.clients.jedis.commands.DatabaseCommands;
import redis.clients.jedis.commands.GenericControlCommands;
import redis.clients.jedis.commands.JedisBinaryCommands;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.commands.ModuleCommands;
import redis.clients.jedis.commands.SentinelCommands;
import redis.clients.jedis.commands.ServerCommands;

/**
 * @author jackson.song
 * @version V1.0
 * @Title JedisOperator.java
 * @Description jedis操作接口
 * @date 2022年4月17日
 * @email suxuan696@gmail.com
 */
public interface JedisOperator
		extends ServerCommands, DatabaseCommands, JedisCommands, JedisBinaryCommands, ControlCommands,
		ControlBinaryCommands, ClusterCommands, ModuleCommands, GenericControlCommands, SentinelCommands, Closeable {

	Jedis getJedis();
}
