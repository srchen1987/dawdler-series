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
package com.anywide.dawdler.es.restclient.wrapper;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.pool2.impl.GenericObjectPool;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

/**
 * @author jackson.song
 * @version V1.0
 * ElasticSearchClient是ElasticsearchClient的包装类
 */
public class ElasticSearchClient implements Closeable {
	private ElasticsearchClient elasticsearchClient;
	private GenericObjectPool<ElasticSearchClient> genericObjectPool;

	public ElasticSearchClient(ElasticsearchClient elasticsearchClient,
			GenericObjectPool<ElasticSearchClient> genericObjectPool) {
		this.elasticsearchClient = elasticsearchClient;
		this.genericObjectPool = genericObjectPool;
	}

	public ElasticsearchClient getElasticsearchClient() {
		return elasticsearchClient;
	}

	@Override
	public void close() throws IOException {
		genericObjectPool.returnObject(this);
	}

}
