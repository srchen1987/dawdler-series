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

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.ClosePointInTimeRequest;
import org.elasticsearch.action.search.ClosePointInTimeResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.OpenPointInTimeRequest;
import org.elasticsearch.action.search.OpenPointInTimeResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AsyncSearchClient;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.CcrClient;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.EnrichClient;
import org.elasticsearch.client.EqlClient;
import org.elasticsearch.client.FeaturesClient;
import org.elasticsearch.client.GraphClient;
import org.elasticsearch.client.IndexLifecycleClient;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.IngestClient;
import org.elasticsearch.client.LicenseClient;
import org.elasticsearch.client.MachineLearningClient;
import org.elasticsearch.client.MigrationClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RethrottleRequest;
import org.elasticsearch.client.RollupClient;
import org.elasticsearch.client.SearchableSnapshotsClient;
import org.elasticsearch.client.SecurityClient;
import org.elasticsearch.client.SnapshotClient;
import org.elasticsearch.client.TasksClient;
import org.elasticsearch.client.TextStructureClient;
import org.elasticsearch.client.TransformClient;
import org.elasticsearch.client.WatcherClient;
import org.elasticsearch.client.XPackClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.core.GetSourceRequest;
import org.elasticsearch.client.core.GetSourceResponse;
import org.elasticsearch.client.core.MainResponse;
import org.elasticsearch.client.core.MultiTermVectorsRequest;
import org.elasticsearch.client.core.MultiTermVectorsResponse;
import org.elasticsearch.client.core.TermVectorsRequest;
import org.elasticsearch.client.core.TermVectorsResponse;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
import org.elasticsearch.index.rankeval.RankEvalRequest;
import org.elasticsearch.index.rankeval.RankEvalResponse;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateRequest;
import org.elasticsearch.script.mustache.MultiSearchTemplateResponse;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;

/**
 *
 * @Title EsRestHighLevelOperator.java
 * @Description es restHighLevel客户端接口
 *              es官方的RestHighLevelClient写的太一般,没有实现接口jdk动态代理不可用,cglib又无法代理final方法(内部大量final方法)
 * @author jackson.song
 * @date 2022年4月16日
 * @version V1.0
 * @email suxuan696@gmail.com
 */
public interface EsRestHighLevelOperator {

	public RestClient getLowLevelClient();

	public IndicesClient indices();

	public ClusterClient cluster();

	public IngestClient ingest();

	public SnapshotClient snapshot();

	public RollupClient rollup();

	public CcrClient ccr();

	public TasksClient tasks();

	public XPackClient xpack();

	public WatcherClient watcher();

	public GraphClient graph();

	public LicenseClient license();

	public IndexLifecycleClient indexLifecycle();

	public AsyncSearchClient asyncSearch();

	public TextStructureClient textStructure();

	public SearchableSnapshotsClient searchableSnapshots();

	public FeaturesClient features();

	public MigrationClient migration();

	public MachineLearningClient machineLearning();

	public SecurityClient security();

	public TransformClient transform();

	public EnrichClient enrich();

	public EqlClient eql();

	public BulkResponse bulk(BulkRequest bulkRequest, RequestOptions options) throws IOException;

	public Cancellable bulkAsync(BulkRequest bulkRequest, RequestOptions options,
			ActionListener<BulkResponse> listener);

	public BulkByScrollResponse reindex(ReindexRequest reindexRequest, RequestOptions options) throws IOException;

	public TaskSubmissionResponse submitReindexTask(ReindexRequest reindexRequest, RequestOptions options)
			throws IOException;

	public Cancellable reindexAsync(ReindexRequest reindexRequest, RequestOptions options,
			ActionListener<BulkByScrollResponse> listener);

	public BulkByScrollResponse updateByQuery(UpdateByQueryRequest updateByQueryRequest, RequestOptions options)
			throws IOException;

	public TaskSubmissionResponse submitUpdateByQueryTask(UpdateByQueryRequest updateByQueryRequest,
			RequestOptions options) throws IOException;

	public Cancellable updateByQueryAsync(UpdateByQueryRequest updateByQueryRequest, RequestOptions options,
			ActionListener<BulkByScrollResponse> listener);

	public BulkByScrollResponse deleteByQuery(DeleteByQueryRequest deleteByQueryRequest, RequestOptions options)
			throws IOException;

	public TaskSubmissionResponse submitDeleteByQueryTask(DeleteByQueryRequest deleteByQueryRequest,
			RequestOptions options) throws IOException;

	public Cancellable deleteByQueryAsync(DeleteByQueryRequest deleteByQueryRequest, RequestOptions options,
			ActionListener<BulkByScrollResponse> listener);

	public ListTasksResponse deleteByQueryRethrottle(RethrottleRequest rethrottleRequest, RequestOptions options)
			throws IOException;

	public Cancellable deleteByQueryRethrottleAsync(RethrottleRequest rethrottleRequest, RequestOptions options,
			ActionListener<ListTasksResponse> listener);

	public ListTasksResponse updateByQueryRethrottle(RethrottleRequest rethrottleRequest, RequestOptions options)
			throws IOException;

	public Cancellable updateByQueryRethrottleAsync(RethrottleRequest rethrottleRequest, RequestOptions options,
			ActionListener<ListTasksResponse> listener);

	public ListTasksResponse reindexRethrottle(RethrottleRequest rethrottleRequest, RequestOptions options)
			throws IOException;

	public Cancellable reindexRethrottleAsync(RethrottleRequest rethrottleRequest, RequestOptions options,
			ActionListener<ListTasksResponse> listener);

	public boolean ping(RequestOptions options) throws IOException;

	public MainResponse info(RequestOptions options) throws IOException;

	public GetResponse get(GetRequest getRequest, RequestOptions options) throws IOException;

	public Cancellable getAsync(GetRequest getRequest, RequestOptions options, ActionListener<GetResponse> listener);

	public MultiGetResponse mget(MultiGetRequest multiGetRequest, RequestOptions options) throws IOException;

	public Cancellable mgetAsync(MultiGetRequest multiGetRequest, RequestOptions options,
			ActionListener<MultiGetResponse> listener);

	public boolean exists(GetRequest getRequest, RequestOptions options) throws IOException;

	public Cancellable existsAsync(GetRequest getRequest, RequestOptions options, ActionListener<Boolean> listener);

	public boolean existsSource(GetSourceRequest getSourceRequest, RequestOptions options) throws IOException;

	public Cancellable existsSourceAsync(GetSourceRequest getSourceRequest, RequestOptions options,
			ActionListener<Boolean> listener);

	public GetSourceResponse getSource(GetSourceRequest getSourceRequest, RequestOptions options) throws IOException;

	public Cancellable getSourceAsync(GetSourceRequest getSourceRequest, RequestOptions options,
			ActionListener<GetSourceResponse> listener);

	public IndexResponse index(IndexRequest indexRequest, RequestOptions options) throws IOException;

	public Cancellable indexAsync(IndexRequest indexRequest, RequestOptions options,
			ActionListener<IndexResponse> listener);

	public CountResponse count(CountRequest countRequest, RequestOptions options) throws IOException;

	public Cancellable countAsync(CountRequest countRequest, RequestOptions options,
			ActionListener<CountResponse> listener);

	public UpdateResponse update(UpdateRequest updateRequest, RequestOptions options) throws IOException;

	public Cancellable updateAsync(UpdateRequest updateRequest, RequestOptions options,
			ActionListener<UpdateResponse> listener);

	public DeleteResponse delete(DeleteRequest deleteRequest, RequestOptions options) throws IOException;

	public Cancellable deleteAsync(DeleteRequest deleteRequest, RequestOptions options,
			ActionListener<DeleteResponse> listener);

	public SearchResponse search(SearchRequest searchRequest, RequestOptions options) throws IOException;

	public Cancellable searchAsync(SearchRequest searchRequest, RequestOptions options,
			ActionListener<SearchResponse> listener);

	public MultiSearchResponse msearch(MultiSearchRequest multiSearchRequest, RequestOptions options)
			throws IOException;

	public Cancellable msearchAsync(MultiSearchRequest searchRequest, RequestOptions options,
			ActionListener<MultiSearchResponse> listener);

	public SearchResponse scroll(SearchScrollRequest searchScrollRequest, RequestOptions options) throws IOException;

	public Cancellable scrollAsync(SearchScrollRequest searchScrollRequest, RequestOptions options,
			ActionListener<SearchResponse> listener);

	public ClearScrollResponse clearScroll(ClearScrollRequest clearScrollRequest, RequestOptions options)
			throws IOException;

	public Cancellable clearScrollAsync(ClearScrollRequest clearScrollRequest, RequestOptions options,
			ActionListener<ClearScrollResponse> listener);

	public OpenPointInTimeResponse openPointInTime(OpenPointInTimeRequest openRequest, RequestOptions options)
			throws IOException;

	public Cancellable openPointInTimeAsync(OpenPointInTimeRequest openRequest, RequestOptions options,
			ActionListener<OpenPointInTimeResponse> listener);

	public ClosePointInTimeResponse closePointInTime(ClosePointInTimeRequest closeRequest, RequestOptions options)
			throws IOException;

	public Cancellable closePointInTimeAsync(ClosePointInTimeRequest closeRequest, RequestOptions options,
			ActionListener<ClosePointInTimeResponse> listener);

	public SearchTemplateResponse searchTemplate(SearchTemplateRequest searchTemplateRequest, RequestOptions options)
			throws IOException;

	public Cancellable searchTemplateAsync(SearchTemplateRequest searchTemplateRequest, RequestOptions options,
			ActionListener<SearchTemplateResponse> listener);

	public ExplainResponse explain(ExplainRequest explainRequest, RequestOptions options) throws IOException;

	public Cancellable explainAsync(ExplainRequest explainRequest, RequestOptions options,
			ActionListener<ExplainResponse> listener);

	public TermVectorsResponse termvectors(TermVectorsRequest request, RequestOptions options) throws IOException;

	public Cancellable termvectorsAsync(TermVectorsRequest request, RequestOptions options,
			ActionListener<TermVectorsResponse> listener);

	public MultiTermVectorsResponse mtermvectors(MultiTermVectorsRequest request, RequestOptions options)
			throws IOException;

	public Cancellable mtermvectorsAsync(MultiTermVectorsRequest request, RequestOptions options,
			ActionListener<MultiTermVectorsResponse> listener);

	public RankEvalResponse rankEval(RankEvalRequest rankEvalRequest, RequestOptions options) throws IOException;

	public MultiSearchTemplateResponse msearchTemplate(MultiSearchTemplateRequest multiSearchTemplateRequest,
			RequestOptions options) throws IOException;

	public Cancellable msearchTemplateAsync(MultiSearchTemplateRequest multiSearchTemplateRequest,
			RequestOptions options, ActionListener<MultiSearchTemplateResponse> listener);

	public Cancellable rankEvalAsync(RankEvalRequest rankEvalRequest, RequestOptions options,
			ActionListener<RankEvalResponse> listener);

	public FieldCapabilitiesResponse fieldCaps(FieldCapabilitiesRequest fieldCapabilitiesRequest,
			RequestOptions options) throws IOException;
}
