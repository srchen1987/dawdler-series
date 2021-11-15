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
package com.anywide.dawdler.es.restclient.warpper;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.elasticsearch.client.RestHighLevelClient;

/**
*
* @Title ElasticSearchClient.java
* @Description ElasticSearchClient是RestHighLevelClient的包装类
* @author jackson.song
* @date 2021年11月14日
* @version V1.0
* @email suxuan696@gmail.com
*/
public class ElasticSearchClient implements Closeable {
	private RestHighLevelClient restHighLevelClient;
	private GenericObjectPool<ElasticSearchClient> genericObjectPool;

	public ElasticSearchClient(RestHighLevelClient restHighLevelClient,
			GenericObjectPool<ElasticSearchClient> genericObjectPool) {
		this.restHighLevelClient = restHighLevelClient;
		this.genericObjectPool = genericObjectPool;
	}

	public RestHighLevelClient getRestHighLevelClient() {
		return restHighLevelClient;
	}

	@Override
	public void close() throws IOException {
		genericObjectPool.returnObject(this);
	}

}
