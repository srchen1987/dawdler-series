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
package com.anywide.dawdler.es.restclient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.function.Function;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.async_search.ElasticsearchAsyncSearchClient;
import co.elastic.clients.elasticsearch.autoscaling.ElasticsearchAutoscalingClient;
import co.elastic.clients.elasticsearch.cat.ElasticsearchCatClient;
import co.elastic.clients.elasticsearch.ccr.ElasticsearchCcrClient;
import co.elastic.clients.elasticsearch.cluster.ElasticsearchClusterClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.ClearScrollRequest;
import co.elastic.clients.elasticsearch.core.ClearScrollResponse;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeRequest;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeResponse;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.CreateRequest;
import co.elastic.clients.elasticsearch.core.CreateResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryResponse;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRethrottleRequest;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRethrottleResponse;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.DeleteScriptRequest;
import co.elastic.clients.elasticsearch.core.DeleteScriptResponse;
import co.elastic.clients.elasticsearch.core.ExistsRequest;
import co.elastic.clients.elasticsearch.core.ExistsSourceRequest;
import co.elastic.clients.elasticsearch.core.ExplainRequest;
import co.elastic.clients.elasticsearch.core.ExplainResponse;
import co.elastic.clients.elasticsearch.core.FieldCapsRequest;
import co.elastic.clients.elasticsearch.core.FieldCapsResponse;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.GetScriptContextResponse;
import co.elastic.clients.elasticsearch.core.GetScriptLanguagesResponse;
import co.elastic.clients.elasticsearch.core.GetScriptRequest;
import co.elastic.clients.elasticsearch.core.GetScriptResponse;
import co.elastic.clients.elasticsearch.core.GetSourceRequest;
import co.elastic.clients.elasticsearch.core.GetSourceResponse;
import co.elastic.clients.elasticsearch.core.HealthReportRequest;
import co.elastic.clients.elasticsearch.core.HealthReportResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.KnnSearchRequest;
import co.elastic.clients.elasticsearch.core.KnnSearchResponse;
import co.elastic.clients.elasticsearch.core.MgetRequest;
import co.elastic.clients.elasticsearch.core.MgetResponse;
import co.elastic.clients.elasticsearch.core.MsearchRequest;
import co.elastic.clients.elasticsearch.core.MsearchResponse;
import co.elastic.clients.elasticsearch.core.MsearchTemplateRequest;
import co.elastic.clients.elasticsearch.core.MsearchTemplateResponse;
import co.elastic.clients.elasticsearch.core.MtermvectorsRequest;
import co.elastic.clients.elasticsearch.core.MtermvectorsResponse;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeRequest;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;
import co.elastic.clients.elasticsearch.core.PutScriptRequest;
import co.elastic.clients.elasticsearch.core.PutScriptResponse;
import co.elastic.clients.elasticsearch.core.RankEvalRequest;
import co.elastic.clients.elasticsearch.core.RankEvalResponse;
import co.elastic.clients.elasticsearch.core.ReindexRequest;
import co.elastic.clients.elasticsearch.core.ReindexResponse;
import co.elastic.clients.elasticsearch.core.ReindexRethrottleRequest;
import co.elastic.clients.elasticsearch.core.ReindexRethrottleResponse;
import co.elastic.clients.elasticsearch.core.RenderSearchTemplateRequest;
import co.elastic.clients.elasticsearch.core.RenderSearchTemplateResponse;
import co.elastic.clients.elasticsearch.core.ScriptsPainlessExecuteRequest;
import co.elastic.clients.elasticsearch.core.ScriptsPainlessExecuteResponse;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchMvtRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.SearchShardsRequest;
import co.elastic.clients.elasticsearch.core.SearchShardsResponse;
import co.elastic.clients.elasticsearch.core.SearchTemplateRequest;
import co.elastic.clients.elasticsearch.core.SearchTemplateResponse;
import co.elastic.clients.elasticsearch.core.TermsEnumRequest;
import co.elastic.clients.elasticsearch.core.TermsEnumResponse;
import co.elastic.clients.elasticsearch.core.TermvectorsRequest;
import co.elastic.clients.elasticsearch.core.TermvectorsResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.elasticsearch.core.UpdateByQueryResponse;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRethrottleRequest;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRethrottleResponse;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.dangling_indices.ElasticsearchDanglingIndicesClient;
import co.elastic.clients.elasticsearch.enrich.ElasticsearchEnrichClient;
import co.elastic.clients.elasticsearch.eql.ElasticsearchEqlClient;
import co.elastic.clients.elasticsearch.esql.ElasticsearchEsqlClient;
import co.elastic.clients.elasticsearch.features.ElasticsearchFeaturesClient;
import co.elastic.clients.elasticsearch.fleet.ElasticsearchFleetClient;
import co.elastic.clients.elasticsearch.graph.ElasticsearchGraphClient;
import co.elastic.clients.elasticsearch.ilm.ElasticsearchIlmClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.inference.ElasticsearchInferenceClient;
import co.elastic.clients.elasticsearch.ingest.ElasticsearchIngestClient;
import co.elastic.clients.elasticsearch.license.ElasticsearchLicenseClient;
import co.elastic.clients.elasticsearch.logstash.ElasticsearchLogstashClient;
import co.elastic.clients.elasticsearch.migration.ElasticsearchMigrationClient;
import co.elastic.clients.elasticsearch.ml.ElasticsearchMlClient;
import co.elastic.clients.elasticsearch.monitoring.ElasticsearchMonitoringClient;
import co.elastic.clients.elasticsearch.nodes.ElasticsearchNodesClient;
import co.elastic.clients.elasticsearch.query_rules.ElasticsearchQueryRulesClient;
import co.elastic.clients.elasticsearch.rollup.ElasticsearchRollupClient;
import co.elastic.clients.elasticsearch.search_application.ElasticsearchSearchApplicationClient;
import co.elastic.clients.elasticsearch.searchable_snapshots.ElasticsearchSearchableSnapshotsClient;
import co.elastic.clients.elasticsearch.security.ElasticsearchSecurityClient;
import co.elastic.clients.elasticsearch.shutdown.ElasticsearchShutdownClient;
import co.elastic.clients.elasticsearch.slm.ElasticsearchSlmClient;
import co.elastic.clients.elasticsearch.snapshot.ElasticsearchSnapshotClient;
import co.elastic.clients.elasticsearch.sql.ElasticsearchSqlClient;
import co.elastic.clients.elasticsearch.ssl.ElasticsearchSslClient;
import co.elastic.clients.elasticsearch.synonyms.ElasticsearchSynonymsClient;
import co.elastic.clients.elasticsearch.tasks.ElasticsearchTasksClient;
import co.elastic.clients.elasticsearch.transform.ElasticsearchTransformClient;
import co.elastic.clients.elasticsearch.watcher.ElasticsearchWatcherClient;
import co.elastic.clients.elasticsearch.xpack.ElasticsearchXpackClient;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.endpoints.BinaryResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import co.elastic.clients.util.ObjectBuilder;

/**
 * @author jackson.song
 * @version V1.0
 * es EsOperator客户端接口 es官方的7.15之后RestHighLevelClient不再使用,采用ElasticsearchClient代替.
 */
public interface EsOperator {

	public ElasticsearchClient withTransportOptions(TransportOptions transportOptions);

	public ElasticsearchAsyncSearchClient asyncSearch();

	public ElasticsearchAutoscalingClient autoscaling();

	public ElasticsearchCatClient cat();

	public ElasticsearchCcrClient ccr();

	public ElasticsearchClusterClient cluster();

	public ElasticsearchDanglingIndicesClient danglingIndices();

	public ElasticsearchEnrichClient enrich();

	public ElasticsearchEqlClient eql();

	public ElasticsearchEsqlClient esql();

	public ElasticsearchFeaturesClient features();

	public ElasticsearchFleetClient fleet();

	public ElasticsearchGraphClient graph();

	public ElasticsearchIlmClient ilm();

	public ElasticsearchIndicesClient indices();

	public ElasticsearchInferenceClient inference();

	public ElasticsearchIngestClient ingest();

	public ElasticsearchLicenseClient license();

	public ElasticsearchLogstashClient logstash();

	public ElasticsearchMigrationClient migration();

	public ElasticsearchMlClient ml();

	public ElasticsearchMonitoringClient monitoring();

	public ElasticsearchNodesClient nodes();

	public ElasticsearchQueryRulesClient queryRules();

	public ElasticsearchRollupClient rollup();

	public ElasticsearchSearchApplicationClient searchApplication();

	public ElasticsearchSearchableSnapshotsClient searchableSnapshots();

	public ElasticsearchSecurityClient security();

	public ElasticsearchShutdownClient shutdown();

	public ElasticsearchSlmClient slm();

	public ElasticsearchSnapshotClient snapshot();

	public ElasticsearchSqlClient sql();

	public ElasticsearchSslClient ssl();

	public ElasticsearchSynonymsClient synonyms();

	public ElasticsearchTasksClient tasks();

	public ElasticsearchTransformClient transform();

	public ElasticsearchWatcherClient watcher();

	public ElasticsearchXpackClient xpack();

	// ----- Endpoint: bulk

	/**
	 * Allows to perform multiple index/update/delete operations in a single
	 * request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/docs-bulk.html">Documentation
	 *      on elastic.co</a>
	 */

	public BulkResponse bulk(BulkRequest request) throws IOException, ElasticsearchException;

	/**
	 * Allows to perform multiple index/update/delete operations in a single
	 * request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link BulkRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/docs-bulk.html">Documentation
	 *      on elastic.co</a>
	 */

	public BulkResponse bulk(Function<BulkRequest.Builder, ObjectBuilder<BulkRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to perform multiple index/update/delete operations in a single
	 * request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/docs-bulk.html">Documentation
	 *      on elastic.co</a>
	 */

	public BulkResponse bulk() throws IOException, ElasticsearchException;

	// ----- Endpoint: clear_scroll

	/**
	 * Explicitly clears the search context for a scroll.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/clear-scroll-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public ClearScrollResponse clearScroll(ClearScrollRequest request) throws IOException, ElasticsearchException;

	/**
	 * Explicitly clears the search context for a scroll.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ClearScrollRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/clear-scroll-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public ClearScrollResponse clearScroll(Function<ClearScrollRequest.Builder, ObjectBuilder<ClearScrollRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Explicitly clears the search context for a scroll.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/clear-scroll-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public ClearScrollResponse clearScroll() throws IOException, ElasticsearchException;

	// ----- Endpoint: close_point_in_time

	/**
	 * Close a point in time
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/point-in-time-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public ClosePointInTimeResponse closePointInTime(ClosePointInTimeRequest request)
			throws IOException, ElasticsearchException;

	/**
	 * Close a point in time
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ClosePointInTimeRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/point-in-time-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public ClosePointInTimeResponse closePointInTime(
			Function<ClosePointInTimeRequest.Builder, ObjectBuilder<ClosePointInTimeRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: count

	/**
	 * Returns number of documents matching a query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-count.html">Documentation
	 *      on elastic.co</a>
	 */

	public CountResponse count(CountRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns number of documents matching a query.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link CountRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-count.html">Documentation
	 *      on elastic.co</a>
	 */

	public CountResponse count(Function<CountRequest.Builder, ObjectBuilder<CountRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Returns number of documents matching a query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-count.html">Documentation
	 *      on elastic.co</a>
	 */

	public CountResponse count() throws IOException, ElasticsearchException;

	// ----- Endpoint: create

	/**
	 * Creates a new document in the index.
	 * <p>
	 * Returns a 409 response when a document with a same ID already exists in the
	 * index.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-index_.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> CreateResponse create(CreateRequest<TDocument> request)
			throws IOException, ElasticsearchException;

	/**
	 * Creates a new document in the index.
	 * <p>
	 * Returns a 409 response when a document with a same ID already exists in the
	 * index.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link CreateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-index_.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> CreateResponse create(
			Function<CreateRequest.Builder<TDocument>, ObjectBuilder<CreateRequest<TDocument>>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: delete

	/**
	 * Removes a document from the index.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-delete.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteResponse delete(DeleteRequest request) throws IOException, ElasticsearchException;

	/**
	 * Removes a document from the index.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link DeleteRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-delete.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteResponse delete(Function<DeleteRequest.Builder, ObjectBuilder<DeleteRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: delete_by_query

	/**
	 * Deletes documents matching the provided query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-delete-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteByQueryResponse deleteByQuery(DeleteByQueryRequest request) throws IOException, ElasticsearchException;

	/**
	 * Deletes documents matching the provided query.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link DeleteByQueryRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-delete-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteByQueryResponse deleteByQuery(
			Function<DeleteByQueryRequest.Builder, ObjectBuilder<DeleteByQueryRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: delete_by_query_rethrottle

	/**
	 * Changes the number of requests per second for a particular Delete By Query
	 * operation.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteByQueryRethrottleResponse deleteByQueryRethrottle(DeleteByQueryRethrottleRequest request)
			throws IOException, ElasticsearchException;

	/**
	 * Changes the number of requests per second for a particular Delete By Query
	 * operation.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link DeleteByQueryRethrottleRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteByQueryRethrottleResponse deleteByQueryRethrottle(
			Function<DeleteByQueryRethrottleRequest.Builder, ObjectBuilder<DeleteByQueryRethrottleRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: delete_script

	/**
	 * Deletes a script.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteScriptResponse deleteScript(DeleteScriptRequest request) throws IOException, ElasticsearchException;

	/**
	 * Deletes a script.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link DeleteScriptRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */

	public DeleteScriptResponse deleteScript(
			Function<DeleteScriptRequest.Builder, ObjectBuilder<DeleteScriptRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: exists

	/**
	 * Returns information about whether a document exists in an index.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public BooleanResponse exists(ExistsRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns information about whether a document exists in an index.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ExistsRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public BooleanResponse exists(Function<ExistsRequest.Builder, ObjectBuilder<ExistsRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: exists_source

	/**
	 * Returns information about whether a document source exists in an index.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public BooleanResponse existsSource(ExistsSourceRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns information about whether a document source exists in an index.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ExistsSourceRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public BooleanResponse existsSource(Function<ExistsSourceRequest.Builder, ObjectBuilder<ExistsSourceRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: explain

	/**
	 * Returns information about why a specific matches (or doesn't match) a query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-explain.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ExplainResponse<TDocument> explain(ExplainRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns information about why a specific matches (or doesn't match) a query.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ExplainRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-explain.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ExplainResponse<TDocument> explain(
			Function<ExplainRequest.Builder, ObjectBuilder<ExplainRequest>> fn, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns information about why a specific matches (or doesn't match) a query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-explain.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ExplainResponse<TDocument> explain(ExplainRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Returns information about why a specific matches (or doesn't match) a query.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ExplainRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-explain.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ExplainResponse<TDocument> explain(
			Function<ExplainRequest.Builder, ObjectBuilder<ExplainRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: field_caps

	/**
	 * Returns the information about the capabilities of fields among multiple
	 * indices.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-field-caps.html">Documentation
	 *      on elastic.co</a>
	 */

	public FieldCapsResponse fieldCaps(FieldCapsRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns the information about the capabilities of fields among multiple
	 * indices.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link FieldCapsRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-field-caps.html">Documentation
	 *      on elastic.co</a>
	 */

	public FieldCapsResponse fieldCaps(Function<FieldCapsRequest.Builder, ObjectBuilder<FieldCapsRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Returns the information about the capabilities of fields among multiple
	 * indices.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-field-caps.html">Documentation
	 *      on elastic.co</a>
	 */

	public FieldCapsResponse fieldCaps() throws IOException, ElasticsearchException;

	// ----- Endpoint: get

	/**
	 * Returns a document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetResponse<TDocument> get(GetRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns a document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link GetRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetResponse<TDocument> get(Function<GetRequest.Builder, ObjectBuilder<GetRequest>> fn,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Returns a document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetResponse<TDocument> get(GetRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Returns a document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link GetRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetResponse<TDocument> get(Function<GetRequest.Builder, ObjectBuilder<GetRequest>> fn,
			Type tDocumentType) throws IOException, ElasticsearchException;

	// ----- Endpoint: get_script

	/**
	 * Returns a script.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */

	public GetScriptResponse getScript(GetScriptRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns a script.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link GetScriptRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */

	public GetScriptResponse getScript(Function<GetScriptRequest.Builder, ObjectBuilder<GetScriptRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: get_script_context

	/**
	 * Returns all script contexts.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/painless/master/painless-contexts.html">Documentation
	 *      on elastic.co</a>
	 */
	public GetScriptContextResponse getScriptContext() throws IOException, ElasticsearchException;

	// ----- Endpoint: get_script_languages

	/**
	 * Returns available script types, languages and contexts
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */
	public GetScriptLanguagesResponse getScriptLanguages() throws IOException, ElasticsearchException;

	// ----- Endpoint: get_source

	/**
	 * Returns the source of a document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetSourceResponse<TDocument> getSource(GetSourceRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns the source of a document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link GetSourceRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetSourceResponse<TDocument> getSource(
			Function<GetSourceRequest.Builder, ObjectBuilder<GetSourceRequest>> fn, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns the source of a document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetSourceResponse<TDocument> getSource(GetSourceRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Returns the source of a document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link GetSourceRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> GetSourceResponse<TDocument> getSource(
			Function<GetSourceRequest.Builder, ObjectBuilder<GetSourceRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: health_report

	/**
	 * Returns the health of the cluster.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/health-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public HealthReportResponse healthReport(HealthReportRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns the health of the cluster.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link HealthReportRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/health-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public HealthReportResponse healthReport(
			Function<HealthReportRequest.Builder, ObjectBuilder<HealthReportRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Returns the health of the cluster.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/health-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public HealthReportResponse healthReport() throws IOException, ElasticsearchException;

	// ----- Endpoint: index

	/**
	 * Creates or updates a document in an index.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-index_.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> IndexResponse index(IndexRequest<TDocument> request) throws IOException, ElasticsearchException;

	/**
	 * Creates or updates a document in an index.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link IndexRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-index_.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> IndexResponse index(
			Function<IndexRequest.Builder<TDocument>, ObjectBuilder<IndexRequest<TDocument>>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: info

	/**
	 * Returns basic information about the cluster.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html">Documentation
	 *      on elastic.co</a>
	 */
	public InfoResponse info() throws IOException, ElasticsearchException;

	// ----- Endpoint: knn_search

	/**
	 * Performs a kNN search.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> KnnSearchResponse<TDocument> knnSearch(KnnSearchRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Performs a kNN search.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link KnnSearchRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> KnnSearchResponse<TDocument> knnSearch(
			Function<KnnSearchRequest.Builder, ObjectBuilder<KnnSearchRequest>> fn, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Performs a kNN search.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> KnnSearchResponse<TDocument> knnSearch(KnnSearchRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Performs a kNN search.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link KnnSearchRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> KnnSearchResponse<TDocument> knnSearch(
			Function<KnnSearchRequest.Builder, ObjectBuilder<KnnSearchRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: mget

	/**
	 * Allows to get multiple documents in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MgetResponse<TDocument> mget(MgetRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to get multiple documents in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MgetRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MgetResponse<TDocument> mget(Function<MgetRequest.Builder, ObjectBuilder<MgetRequest>> fn,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Allows to get multiple documents in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MgetResponse<TDocument> mget(MgetRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to get multiple documents in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MgetRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-get.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MgetResponse<TDocument> mget(Function<MgetRequest.Builder, ObjectBuilder<MgetRequest>> fn,
			Type tDocumentType) throws IOException, ElasticsearchException;

	// ----- Endpoint: msearch

	/**
	 * Allows to execute several search operations in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchResponse<TDocument> msearch(MsearchRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to execute several search operations in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MsearchRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchResponse<TDocument> msearch(
			Function<MsearchRequest.Builder, ObjectBuilder<MsearchRequest>> fn, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to execute several search operations in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchResponse<TDocument> msearch(MsearchRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to execute several search operations in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MsearchRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchResponse<TDocument> msearch(
			Function<MsearchRequest.Builder, ObjectBuilder<MsearchRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: msearch_template

	/**
	 * Allows to execute several search template operations in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchTemplateResponse<TDocument> msearchTemplate(MsearchTemplateRequest request,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Allows to execute several search template operations in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MsearchTemplateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchTemplateResponse<TDocument> msearchTemplate(
			Function<MsearchTemplateRequest.Builder, ObjectBuilder<MsearchTemplateRequest>> fn,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Allows to execute several search template operations in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchTemplateResponse<TDocument> msearchTemplate(MsearchTemplateRequest request,
			Type tDocumentType) throws IOException, ElasticsearchException;

	/**
	 * Allows to execute several search template operations in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MsearchTemplateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> MsearchTemplateResponse<TDocument> msearchTemplate(
			Function<MsearchTemplateRequest.Builder, ObjectBuilder<MsearchTemplateRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: mtermvectors

	/**
	 * Returns multiple termvectors in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-termvectors.html">Documentation
	 *      on elastic.co</a>
	 */

	public MtermvectorsResponse mtermvectors(MtermvectorsRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns multiple termvectors in one request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link MtermvectorsRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-termvectors.html">Documentation
	 *      on elastic.co</a>
	 */

	public MtermvectorsResponse mtermvectors(
			Function<MtermvectorsRequest.Builder, ObjectBuilder<MtermvectorsRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Returns multiple termvectors in one request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-multi-termvectors.html">Documentation
	 *      on elastic.co</a>
	 */

	public MtermvectorsResponse mtermvectors() throws IOException, ElasticsearchException;

	// ----- Endpoint: open_point_in_time

	/**
	 * Open a point in time that can be used in subsequent searches
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/point-in-time-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public OpenPointInTimeResponse openPointInTime(OpenPointInTimeRequest request)
			throws IOException, ElasticsearchException;

	/**
	 * Open a point in time that can be used in subsequent searches
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link OpenPointInTimeRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/8.12/point-in-time-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public OpenPointInTimeResponse openPointInTime(
			Function<OpenPointInTimeRequest.Builder, ObjectBuilder<OpenPointInTimeRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: ping

	/**
	 * Returns whether the cluster is running.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html">Documentation
	 *      on elastic.co</a>
	 */
	public BooleanResponse ping() throws IOException, ElasticsearchException;

	// ----- Endpoint: put_script

	/**
	 * Creates or updates a script.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */

	public PutScriptResponse putScript(PutScriptRequest request) throws IOException, ElasticsearchException;

	/**
	 * Creates or updates a script.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link PutScriptRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/modules-scripting.html">Documentation
	 *      on elastic.co</a>
	 */

	public PutScriptResponse putScript(Function<PutScriptRequest.Builder, ObjectBuilder<PutScriptRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: rank_eval

	/**
	 * Allows to evaluate the quality of ranked search results over a set of typical
	 * search queries
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-rank-eval.html">Documentation
	 *      on elastic.co</a>
	 */

	public RankEvalResponse rankEval(RankEvalRequest request) throws IOException, ElasticsearchException;

	/**
	 * Allows to evaluate the quality of ranked search results over a set of typical
	 * search queries
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link RankEvalRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-rank-eval.html">Documentation
	 *      on elastic.co</a>
	 */

	public RankEvalResponse rankEval(Function<RankEvalRequest.Builder, ObjectBuilder<RankEvalRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: reindex

	/**
	 * Allows to copy documents from one index to another, optionally filtering the
	 * source documents by a query, changing the destination index settings, or
	 * fetching the documents from a remote cluster.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-reindex.html">Documentation
	 *      on elastic.co</a>
	 */

	public ReindexResponse reindex(ReindexRequest request) throws IOException, ElasticsearchException;

	/**
	 * Allows to copy documents from one index to another, optionally filtering the
	 * source documents by a query, changing the destination index settings, or
	 * fetching the documents from a remote cluster.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ReindexRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-reindex.html">Documentation
	 *      on elastic.co</a>
	 */

	public ReindexResponse reindex(Function<ReindexRequest.Builder, ObjectBuilder<ReindexRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: reindex_rethrottle

	/**
	 * Changes the number of requests per second for a particular Reindex operation.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-reindex.html">Documentation
	 *      on elastic.co</a>
	 */

	public ReindexRethrottleResponse reindexRethrottle(ReindexRethrottleRequest request)
			throws IOException, ElasticsearchException;

	/**
	 * Changes the number of requests per second for a particular Reindex operation.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ReindexRethrottleRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-reindex.html">Documentation
	 *      on elastic.co</a>
	 */

	public ReindexRethrottleResponse reindexRethrottle(
			Function<ReindexRethrottleRequest.Builder, ObjectBuilder<ReindexRethrottleRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: render_search_template

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/render-search-template-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public RenderSearchTemplateResponse renderSearchTemplate(RenderSearchTemplateRequest request)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link RenderSearchTemplateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/render-search-template-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public RenderSearchTemplateResponse renderSearchTemplate(
			Function<RenderSearchTemplateRequest.Builder, ObjectBuilder<RenderSearchTemplateRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/render-search-template-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public RenderSearchTemplateResponse renderSearchTemplate() throws IOException, ElasticsearchException;

	// ----- Endpoint: scripts_painless_execute

	/**
	 * Allows an arbitrary script to be executed and a result to be returned
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/painless/master/painless-execute-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TResult> ScriptsPainlessExecuteResponse<TResult> scriptsPainlessExecute(
			ScriptsPainlessExecuteRequest request, Class<TResult> tResultClass)
			throws IOException, ElasticsearchException;

	/**
	 * Allows an arbitrary script to be executed and a result to be returned
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ScriptsPainlessExecuteRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/painless/master/painless-execute-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TResult> ScriptsPainlessExecuteResponse<TResult> scriptsPainlessExecute(
			Function<ScriptsPainlessExecuteRequest.Builder, ObjectBuilder<ScriptsPainlessExecuteRequest>> fn,
			Class<TResult> tResultClass) throws IOException, ElasticsearchException;

	/**
	 * Allows an arbitrary script to be executed and a result to be returned
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/painless/master/painless-execute-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TResult> ScriptsPainlessExecuteResponse<TResult> scriptsPainlessExecute(
			ScriptsPainlessExecuteRequest request, Type tResultType) throws IOException, ElasticsearchException;

	/**
	 * Allows an arbitrary script to be executed and a result to be returned
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ScriptsPainlessExecuteRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/painless/master/painless-execute-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TResult> ScriptsPainlessExecuteResponse<TResult> scriptsPainlessExecute(
			Function<ScriptsPainlessExecuteRequest.Builder, ObjectBuilder<ScriptsPainlessExecuteRequest>> fn,
			Type tResultType) throws IOException, ElasticsearchException;

	// ----- Endpoint: scroll

	/**
	 * Allows to retrieve a large numbers of results from a single search request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-request-body.html#request-body-search-scroll">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ScrollResponse<TDocument> scroll(ScrollRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to retrieve a large numbers of results from a single search request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ScrollRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-request-body.html#request-body-search-scroll">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ScrollResponse<TDocument> scroll(
			Function<ScrollRequest.Builder, ObjectBuilder<ScrollRequest>> fn, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to retrieve a large numbers of results from a single search request.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-request-body.html#request-body-search-scroll">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ScrollResponse<TDocument> scroll(ScrollRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Allows to retrieve a large numbers of results from a single search request.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link ScrollRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-request-body.html#request-body-search-scroll">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> ScrollResponse<TDocument> scroll(
			Function<ScrollRequest.Builder, ObjectBuilder<ScrollRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: search

	/**
	 * Returns results matching a query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchResponse<TDocument> search(SearchRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns results matching a query.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link SearchRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchResponse<TDocument> search(
			Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> fn, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Returns results matching a query.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchResponse<TDocument> search(SearchRequest request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Returns results matching a query.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link SearchRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-search.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchResponse<TDocument> search(
			Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: search_mvt

	/**
	 * Searches a vector tile for geospatial values. Returns results as a binary
	 * Mapbox vector tile.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-vector-tile-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public BinaryResponse searchMvt(SearchMvtRequest request) throws IOException, ElasticsearchException;

	/**
	 * Searches a vector tile for geospatial values. Returns results as a binary
	 * Mapbox vector tile.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link SearchMvtRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-vector-tile-api.html">Documentation
	 *      on elastic.co</a>
	 */

	public BinaryResponse searchMvt(Function<SearchMvtRequest.Builder, ObjectBuilder<SearchMvtRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: search_shards

	/**
	 * Returns information about the indices and shards that a search request would
	 * be executed against.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-shards.html">Documentation
	 *      on elastic.co</a>
	 */

	public SearchShardsResponse searchShards(SearchShardsRequest request) throws IOException, ElasticsearchException;

	/**
	 * Returns information about the indices and shards that a search request would
	 * be executed against.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link SearchShardsRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-shards.html">Documentation
	 *      on elastic.co</a>
	 */

	public SearchShardsResponse searchShards(
			Function<SearchShardsRequest.Builder, ObjectBuilder<SearchShardsRequest>> fn)
			throws IOException, ElasticsearchException;

	/**
	 * Returns information about the indices and shards that a search request would
	 * be executed against.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/search-shards.html">Documentation
	 *      on elastic.co</a>
	 */

	public SearchShardsResponse searchShards() throws IOException, ElasticsearchException;

	// ----- Endpoint: search_template

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-template.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchTemplateResponse<TDocument> searchTemplate(SearchTemplateRequest request,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link SearchTemplateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-template.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchTemplateResponse<TDocument> searchTemplate(
			Function<SearchTemplateRequest.Builder, ObjectBuilder<SearchTemplateRequest>> fn,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-template.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchTemplateResponse<TDocument> searchTemplate(SearchTemplateRequest request,
			Type tDocumentType) throws IOException, ElasticsearchException;

	/**
	 * Allows to use the Mustache language to pre-render a search definition.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link SearchTemplateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-template.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> SearchTemplateResponse<TDocument> searchTemplate(
			Function<SearchTemplateRequest.Builder, ObjectBuilder<SearchTemplateRequest>> fn, Type tDocumentType)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: terms_enum

	/**
	 * The terms enum API can be used to discover terms in the index that begin with
	 * the provided string. It is designed for low-latency look-ups used in
	 * auto-complete scenarios.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-terms-enum.html">Documentation
	 *      on elastic.co</a>
	 */

	public TermsEnumResponse termsEnum(TermsEnumRequest request) throws IOException, ElasticsearchException;

	/**
	 * The terms enum API can be used to discover terms in the index that begin with
	 * the provided string. It is designed for low-latency look-ups used in
	 * auto-complete scenarios.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link TermsEnumRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/search-terms-enum.html">Documentation
	 *      on elastic.co</a>
	 */

	public TermsEnumResponse termsEnum(Function<TermsEnumRequest.Builder, ObjectBuilder<TermsEnumRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: termvectors

	/**
	 * Returns information and statistics about terms in the fields of a particular
	 * document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-termvectors.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> TermvectorsResponse termvectors(TermvectorsRequest<TDocument> request)
			throws IOException, ElasticsearchException;

	/**
	 * Returns information and statistics about terms in the fields of a particular
	 * document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link TermvectorsRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-termvectors.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument> TermvectorsResponse termvectors(
			Function<TermvectorsRequest.Builder<TDocument>, ObjectBuilder<TermvectorsRequest<TDocument>>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: update

	/**
	 * Updates a document with a script or partial document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-update.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument, TPartialDocument> UpdateResponse<TDocument> update(
			UpdateRequest<TDocument, TPartialDocument> request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	/**
	 * Updates a document with a script or partial document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link UpdateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-update.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument, TPartialDocument> UpdateResponse<TDocument> update(
			Function<UpdateRequest.Builder<TDocument, TPartialDocument>, ObjectBuilder<UpdateRequest<TDocument, TPartialDocument>>> fn,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	/**
	 * Updates a document with a script or partial document.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-update.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument, TPartialDocument> UpdateResponse<TDocument> update(
			UpdateRequest<TDocument, TPartialDocument> request, Type tDocumentType)
			throws IOException, ElasticsearchException;

	/**
	 * Updates a document with a script or partial document.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link UpdateRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-update.html">Documentation
	 *      on elastic.co</a>
	 */

	public <TDocument, TPartialDocument> UpdateResponse<TDocument> update(
			Function<UpdateRequest.Builder<TDocument, TPartialDocument>, ObjectBuilder<UpdateRequest<TDocument, TPartialDocument>>> fn,
			Type tDocumentType) throws IOException, ElasticsearchException;

	// ----- Endpoint: update_by_query

	/**
	 * Performs an update on every document in the index without changing the
	 * source, for example to pick up a mapping change.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-update-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public UpdateByQueryResponse updateByQuery(UpdateByQueryRequest request) throws IOException, ElasticsearchException;

	/**
	 * Performs an update on every document in the index without changing the
	 * source, for example to pick up a mapping change.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link UpdateByQueryRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/master/docs-update-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public UpdateByQueryResponse updateByQuery(
			Function<UpdateByQueryRequest.Builder, ObjectBuilder<UpdateByQueryRequest>> fn)
			throws IOException, ElasticsearchException;

	// ----- Endpoint: update_by_query_rethrottle

	/**
	 * Changes the number of requests per second for a particular Update By Query
	 * operation.
	 * 
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public UpdateByQueryRethrottleResponse updateByQueryRethrottle(UpdateByQueryRethrottleRequest request)
			throws IOException, ElasticsearchException;

	/**
	 * Changes the number of requests per second for a particular Update By Query
	 * operation.
	 * 
	 * @param fn a function that initializes a builder to create the
	 *           {@link UpdateByQueryRethrottleRequest}
	 * @see <a href=
	 *      "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html">Documentation
	 *      on elastic.co</a>
	 */

	public UpdateByQueryRethrottleResponse updateByQueryRethrottle(
			Function<UpdateByQueryRethrottleRequest.Builder, ObjectBuilder<UpdateByQueryRethrottleRequest>> fn)
			throws IOException, ElasticsearchException;

}
