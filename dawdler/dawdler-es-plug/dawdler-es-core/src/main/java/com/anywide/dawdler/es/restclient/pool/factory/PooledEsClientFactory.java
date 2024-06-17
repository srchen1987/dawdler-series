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
package com.anywide.dawdler.es.restclient.pool.factory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import com.anywide.dawdler.es.restclient.factory.EsClientFactory;
import com.anywide.dawdler.es.restclient.wrapper.ElasticSearchClient;

import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * @author jackson.song
 * @version V1.0
 * es客户端工厂,通过pool2实现
 */
public class PooledEsClientFactory extends BasePooledObjectFactory<ElasticSearchClient> {
	private EsClientFactory esClientFactory;
	private GenericObjectPool<ElasticSearchClient> genericObjectPool;

	public void setGenericObjectPool(GenericObjectPool<ElasticSearchClient> genericObjectPool) {
		this.genericObjectPool = genericObjectPool;
	}

	public PooledEsClientFactory(EsClientFactory esClientFactory) {
		this.esClientFactory = esClientFactory;
	}

	@Override
	public ElasticSearchClient create() throws Exception {
		return new ElasticSearchClient(esClientFactory.create(), genericObjectPool);
	}

	@Override
	public void destroyObject(PooledObject<ElasticSearchClient> p) throws Exception {
		p.getObject().getElasticsearchClient()._transport().close();
	}

	@Override
	public boolean validateObject(PooledObject<ElasticSearchClient> p) {
		ElasticSearchClient client = p.getObject();
		return ((RestClientTransport) client.getElasticsearchClient()._transport()).restClient().isRunning();
	}

	@Override
	public PooledObject<ElasticSearchClient> wrap(ElasticSearchClient obj) {
		return new DefaultPooledObject<ElasticSearchClient>(obj);
	}

}
