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
package com.anywide.dawdler.es.restclient.resource;

import com.anywide.dawdler.core.component.resource.ComponentLifeCycle;
import com.anywide.dawdler.es.restclient.pool.factory.ElasticSearchClientFactory;

/**
 * @author jackson.song
 * @version V1.0
 * @Title ElasticSearchLifeCycle.java
 * @Description ElasticSearch释放资源
 * @date 2022年4月16日
 * @email suxuan696@gmail.com
 */
public class ElasticSearchLifeCycle implements ComponentLifeCycle {

	@Override
	public void init() {
	}

	@Override
	public void destroy() {
		ElasticSearchClientFactory.shutdownAll();
	}

}
