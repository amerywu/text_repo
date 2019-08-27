package ikoda.utils;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

public class ElasticSearchClientFactory {
	private static ElasticSearchClientFactory elasticSearchClientFactory;
	private RestHighLevelClient client;

	private ElasticSearchClientFactory() {

	}

	public static ElasticSearchClientFactory getInstance() {
		if (null == elasticSearchClientFactory) {
			elasticSearchClientFactory = new ElasticSearchClientFactory();
		}
		return elasticSearchClientFactory;
	}

	public void initiateClient(String user, String password, String url, int port) throws IKodaUtilsException {
		if (null == client) {

			try {
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));

				RestClientBuilder builder = RestClient.builder(new HttpHost(url, port))
						.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
							@Override
							public HttpAsyncClientBuilder customizeHttpClient(
									HttpAsyncClientBuilder httpClientBuilder) {
								return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
							}
						});

				client = new RestHighLevelClient(builder);

			} catch (Exception e) {
				throw new IKodaUtilsException(e.getMessage(), e);
			}
		}
	}

	public RestHighLevelClient client() throws IKodaUtilsException {
		if (null == client) {
			throw new IKodaUtilsException("RestHighLevelClient is null");
		}
		return client;

	}
}
