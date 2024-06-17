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
package com.anywide.dawdler.es.health;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.anywide.dawdler.core.health.Health;
import com.anywide.dawdler.core.health.Health.Builder;
import com.anywide.dawdler.core.health.HealthIndicator;
import com.anywide.dawdler.es.restclient.pool.factory.ElasticSearchClientFactory;
import com.anywide.dawdler.es.restclient.wrapper.ElasticSearchClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthRequest;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;

/**
 * @author jackson.song
 * @version V1.0
 * EsIndicator es健康指示器
 */
public class EsIndicator implements HealthIndicator {

	@Override
	public String name() {
		return "elasticSearch";
	}

	@Override
	public Health check(Builder builder) throws Exception {
		ElasticSearchClient elasticSearchClient = null;
		Map<String, ElasticSearchClientFactory> instances = ElasticSearchClientFactory.getInstances();
		Set<Entry<String, ElasticSearchClientFactory>> entrySet = instances.entrySet();
		for (Entry<String, ElasticSearchClientFactory> entry : entrySet) {
			String key = entry.getKey();
			Builder childBuilder = Health.up();
			try {
				ElasticSearchClientFactory factory = entry.getValue();
				elasticSearchClient = factory.getElasticSearchClient();
				ElasticsearchClient elasticsearchClient = elasticSearchClient.getElasticsearchClient();
				HealthRequest request = HealthRequest.of(healthBuilder -> {
					return healthBuilder;
				});
				HealthResponse response = elasticsearchClient.cluster().health(request);
				switch (response.status()) {
				case Green:
				case Yellow:
					builder.up();
					break;
				case Red:
				default:
					builder.down();
					break;
				}
				childBuilder.withDetail("clusterName", response.clusterName());
				childBuilder.withDetail("numberOfNodes", response.numberOfNodes());
				childBuilder.withDetail("numberOfDataNodes", response.numberOfDataNodes());
				childBuilder.withDetail("activePrimaryShards", response.activePrimaryShards());
				childBuilder.withDetail("activeShards", response.activeShards());
				childBuilder.withDetail("relocatingShards", response.relocatingShards());
				childBuilder.withDetail("initializingShards", response.initializingShards());
				childBuilder.withDetail("unassignedShards", response.unassignedShards());
				builder.withDetail(key, childBuilder.build().getData());
			} finally {
				if (elasticSearchClient != null) {
					elasticSearchClient.close();
				}
			}
		}

		return builder.build();
	}

}
