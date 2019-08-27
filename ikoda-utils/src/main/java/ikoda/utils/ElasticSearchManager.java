package ikoda.utils;



import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;



public class ElasticSearchManager {


	Logger logger = SSm.getLogger(this.getClass());
	
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
		ElasticSearchClientFactory.getInstance().initiateClient(username,password, url, port);
	}
	
	public void createIndexIfNotExisting(String indexName,XContentBuilder indexMapping ) throws IKodaUtilsException{
		try
		{
			GetIndexRequest request = new GetIndexRequest(indexName);
			boolean exists = ElasticSearchClientFactory.getInstance().client().indices().exists(request, RequestOptions.DEFAULT);
			logger.info("utindex exists: " + exists);
			if (!exists) {
				CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
				createRequest.settings(
						Settings.builder().put("index.number_of_shards", 2).put("index.number_of_replicas", 2));
				createRequest.mapping(indexMapping);

				CreateIndexResponse createIndexResponse = ElasticSearchClientFactory.getInstance().client().indices().create(createRequest,
						RequestOptions.DEFAULT);
				boolean acknowledged = createIndexResponse.isAcknowledged();
				boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();

				logger.info("acknowledged: " + acknowledged);
				logger.info("shardsAcknowledged: " + shardsAcknowledged);
			}
		}
		catch(Exception e) {
			throw new IKodaUtilsException(e.getMessage(),e);
		}		
	}
	
	public void addDocument(XContentBuilder builder , String indexName) {
		
		try {
		IndexRequest indexRequest = new IndexRequest(indexName)
			    .id(java.util.UUID.randomUUID().toString()).source(builder);
		
		IndexResponse indexResponse = ElasticSearchClientFactory.getInstance().client().index(indexRequest, RequestOptions.DEFAULT);
		}
		catch(Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}
	
	public void close() {
		try {
			if(null != ElasticSearchClientFactory.getInstance().client()) {
				ElasticSearchClientFactory.getInstance().client().close();
			}
		}
		catch(Exception e)
		{
			logger.warn(e.getMessage(),e);
		}
	}
	
	


}
