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
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.InfoResponse;
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
import co.elastic.clients.elasticsearch.features.ElasticsearchFeaturesClient;
import co.elastic.clients.elasticsearch.graph.ElasticsearchGraphClient;
import co.elastic.clients.elasticsearch.ilm.ElasticsearchIlmClient;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.ingest.ElasticsearchIngestClient;
import co.elastic.clients.elasticsearch.license.ElasticsearchLicenseClient;
import co.elastic.clients.elasticsearch.logstash.ElasticsearchLogstashClient;
import co.elastic.clients.elasticsearch.migration.ElasticsearchMigrationClient;
import co.elastic.clients.elasticsearch.ml.ElasticsearchMlClient;
import co.elastic.clients.elasticsearch.monitoring.ElasticsearchMonitoringClient;
import co.elastic.clients.elasticsearch.nodes.ElasticsearchNodesClient;
import co.elastic.clients.elasticsearch.rollup.ElasticsearchRollupClient;
import co.elastic.clients.elasticsearch.searchable_snapshots.ElasticsearchSearchableSnapshotsClient;
import co.elastic.clients.elasticsearch.security.ElasticsearchSecurityClient;
import co.elastic.clients.elasticsearch.shutdown.ElasticsearchShutdownClient;
import co.elastic.clients.elasticsearch.slm.ElasticsearchSlmClient;
import co.elastic.clients.elasticsearch.snapshot.ElasticsearchSnapshotClient;
import co.elastic.clients.elasticsearch.sql.ElasticsearchSqlClient;
import co.elastic.clients.elasticsearch.ssl.ElasticsearchSslClient;
import co.elastic.clients.elasticsearch.tasks.ElasticsearchTasksClient;
import co.elastic.clients.elasticsearch.transform.ElasticsearchTransformClient;
import co.elastic.clients.elasticsearch.watcher.ElasticsearchWatcherClient;
import co.elastic.clients.elasticsearch.xpack.ElasticsearchXpackClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.endpoints.BooleanResponse;

/**
 *
 * @author jackson.song
 * @version V1.0
 * @Title EsOperator.java
 * @Description es EsOperator客户端接口
 *              es官方的7.15之后RestHighLevelClient不再使用,采用ElasticsearchClient代替.
 * @date 2022年4月16日
 * @email suxuan696@gmail.com
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

	public ElasticsearchFeaturesClient features();

	public ElasticsearchGraphClient graph();

	public ElasticsearchIlmClient ilm();

	public ElasticsearchIndicesClient indices();

	public ElasticsearchIngestClient ingest();

	public ElasticsearchLicenseClient license();

	public ElasticsearchLogstashClient logstash();

	public ElasticsearchMigrationClient migration();

	public ElasticsearchMlClient ml();

	public ElasticsearchMonitoringClient monitoring();

	public ElasticsearchNodesClient nodes();

	public ElasticsearchRollupClient rollup();

	public ElasticsearchSearchableSnapshotsClient searchableSnapshots();

	public ElasticsearchSecurityClient security();

	public ElasticsearchShutdownClient shutdown();

	public ElasticsearchSlmClient slm();

	public ElasticsearchSnapshotClient snapshot();

	public ElasticsearchSqlClient sql();

	public ElasticsearchSslClient ssl();

	public ElasticsearchTasksClient tasks();

	public ElasticsearchTransformClient transform();

	public ElasticsearchWatcherClient watcher();

	public ElasticsearchXpackClient xpack();

	public BulkResponse bulk(BulkRequest request) throws IOException, ElasticsearchException;

	public BulkResponse bulk() throws IOException, ElasticsearchException;

	public ClearScrollResponse clearScroll(ClearScrollRequest request) throws IOException, ElasticsearchException;

	public ClearScrollResponse clearScroll() throws IOException, ElasticsearchException;

	public ClosePointInTimeResponse closePointInTime(ClosePointInTimeRequest request)
			throws IOException, ElasticsearchException;

	public CountResponse count(CountRequest request) throws IOException, ElasticsearchException;

	public CountResponse count() throws IOException, ElasticsearchException;

	public <TDocument> CreateResponse create(CreateRequest<TDocument> request)
			throws IOException, ElasticsearchException;

	public DeleteResponse delete(DeleteRequest request) throws IOException, ElasticsearchException;

	public DeleteByQueryResponse deleteByQuery(DeleteByQueryRequest request) throws IOException, ElasticsearchException;

	public DeleteByQueryRethrottleResponse deleteByQueryRethrottle(DeleteByQueryRethrottleRequest request)
			throws IOException, ElasticsearchException;

	public DeleteScriptResponse deleteScript(DeleteScriptRequest request) throws IOException, ElasticsearchException;

	public BooleanResponse exists(ExistsRequest request) throws IOException, ElasticsearchException;

	public BooleanResponse existsSource(ExistsSourceRequest request) throws IOException, ElasticsearchException;

	public <TDocument> ExplainResponse<TDocument> explain(ExplainRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public FieldCapsResponse fieldCaps(FieldCapsRequest request) throws IOException, ElasticsearchException;

	public FieldCapsResponse fieldCaps() throws IOException, ElasticsearchException;

	public <TDocument> GetResponse<TDocument> get(GetRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public GetScriptResponse getScript(GetScriptRequest request) throws IOException, ElasticsearchException;

	public GetScriptContextResponse getScriptContext() throws IOException, ElasticsearchException;

	public GetScriptLanguagesResponse getScriptLanguages() throws IOException, ElasticsearchException;

	public <TDocument> GetSourceResponse<TDocument> getSource(GetSourceRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public <TDocument> IndexResponse index(IndexRequest<TDocument> request) throws IOException, ElasticsearchException;

	public InfoResponse info() throws IOException, ElasticsearchException;

	public <TDocument> MgetResponse<TDocument> mget(MgetRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public <TDocument> MsearchResponse<TDocument> msearch(MsearchRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public <TDocument> MsearchTemplateResponse<TDocument> msearchTemplate(MsearchTemplateRequest request,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	public MtermvectorsResponse mtermvectors(MtermvectorsRequest request) throws IOException, ElasticsearchException;

	public MtermvectorsResponse mtermvectors() throws IOException, ElasticsearchException;

	public OpenPointInTimeResponse openPointInTime(OpenPointInTimeRequest request)
			throws IOException, ElasticsearchException;

	public BooleanResponse ping() throws IOException, ElasticsearchException;

	public PutScriptResponse putScript(PutScriptRequest request) throws IOException, ElasticsearchException;

	public RankEvalResponse rankEval(RankEvalRequest request) throws IOException, ElasticsearchException;

	public ReindexResponse reindex(ReindexRequest request) throws IOException, ElasticsearchException;

	public ReindexResponse reindex() throws IOException, ElasticsearchException;

	public ReindexRethrottleResponse reindexRethrottle(ReindexRethrottleRequest request)
			throws IOException, ElasticsearchException;

	public RenderSearchTemplateResponse renderSearchTemplate(RenderSearchTemplateRequest request)
			throws IOException, ElasticsearchException;

	public RenderSearchTemplateResponse renderSearchTemplate() throws IOException, ElasticsearchException;

	public <TResult> ScriptsPainlessExecuteResponse<TResult> scriptsPainlessExecute(
			ScriptsPainlessExecuteRequest request, Class<TResult> tResultClass)
			throws IOException, ElasticsearchException;

	public <TDocument> ScrollResponse<TDocument> scroll(ScrollRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public <TDocument> SearchResponse<TDocument> search(SearchRequest request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public SearchShardsResponse searchShards(SearchShardsRequest request) throws IOException, ElasticsearchException;

	public SearchShardsResponse searchShards() throws IOException, ElasticsearchException;

	public <TDocument> SearchTemplateResponse<TDocument> searchTemplate(SearchTemplateRequest request,
			Class<TDocument> tDocumentClass) throws IOException, ElasticsearchException;

	public TermsEnumResponse termsEnum(TermsEnumRequest request) throws IOException, ElasticsearchException;

	public <TDocument> TermvectorsResponse termvectors(TermvectorsRequest<TDocument> request)
			throws IOException, ElasticsearchException;

	public <TDocument, TPartialDocument> UpdateResponse<TDocument> update(
			UpdateRequest<TDocument, TPartialDocument> request, Class<TDocument> tDocumentClass)
			throws IOException, ElasticsearchException;

	public UpdateByQueryResponse updateByQuery(UpdateByQueryRequest request) throws IOException, ElasticsearchException;

	public UpdateByQueryRethrottleResponse updateByQueryRethrottle(UpdateByQueryRethrottleRequest request)
			throws IOException, ElasticsearchException;

	public ElasticsearchTransport _transport();

	public TransportOptions _transportOptions();

}
