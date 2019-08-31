package ikoda.utils;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

public class ElasticSearchManager {

	Logger logger = SSm.getLogger(this.getClass().getName());

	private static ElasticSearchManager elasticSearchManager;

	private ElasticSearchManager() {

	}

	public static ElasticSearchManager getInstance() {
		if (null == elasticSearchManager) {
			elasticSearchManager = new ElasticSearchManager();
		}
		return elasticSearchManager;
	}

	public void init(String username, String password, String url, int port) throws IKodaUtilsException {
		ElasticSearchClientFactory.getInstance().initiateClient(username, password, url, port);
	}

	public void createIndexIfNotExisting(String indexName, XContentBuilder indexMapping) throws IKodaUtilsException {
		try {
			GetIndexRequest request = new GetIndexRequest(indexName);
			boolean exists = ElasticSearchClientFactory.getInstance().client().indices().exists(request,
					RequestOptions.DEFAULT);
			logger.info("utindex exists: " + exists);
			if (!exists) {
				CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
				createRequest.settings(
						Settings.builder().put("index.number_of_shards", 2).put("index.number_of_replicas", 2));
				createRequest.mapping(indexMapping);

				CreateIndexResponse createIndexResponse = ElasticSearchClientFactory.getInstance().client().indices()
						.create(createRequest, RequestOptions.DEFAULT);
				boolean acknowledged = createIndexResponse.isAcknowledged();
				boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();

				logger.info("acknowledged: " + acknowledged);
				logger.info("shardsAcknowledged: " + shardsAcknowledged);
			}
		} catch (Exception e) {
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public SearchResponse search(SearchRequest searchRequest) throws IOException, IKodaUtilsException {
		return ElasticSearchClientFactory.getInstance().client().search(searchRequest, RequestOptions.DEFAULT);

	}

	public boolean documentExists(String index, String docId) {
		try {
			GetRequest getRequest = new GetRequest(index, docId);
			getRequest.fetchSourceContext(new FetchSourceContext(false));
			getRequest.storedFields("_none_");

			return ElasticSearchClientFactory.getInstance().client().exists(getRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.warn("documentExists " + e.getMessage());
			ProcessStatus.incrementStatus("Error on exist check");
			return false;
		} 
	}

	public void addDocument(XContentBuilder builder, String indexName) {
		addDocument(builder, indexName, java.util.UUID.randomUUID().toString());
	}

	public void addDocument(XContentBuilder builder, String indexName, String uid) {
		try {
			logger.info("dispatching doc to ES");
			IndexRequest indexRequest = new IndexRequest(indexName).id(uid).source(builder);

			IndexResponse indexResponse = ElasticSearchClientFactory.getInstance().client().index(indexRequest,
					RequestOptions.DEFAULT);
			logger.info(indexResponse.toString());
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	public void close() {
		try {
			if (null != ElasticSearchClientFactory.getInstance().client()) {
				ElasticSearchClientFactory.getInstance().client().close();
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

}
