package ikoda.utils.test.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;

import ikoda.utils.ElasticSearchClientFactory;
import ikoda.utils.SSm;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StreamToSpark;

public class TestUtils {

	// @Autowired
	// private JobAnalysisServiceImpl jobAnalysisService;

	Logger logger = SSm.getLogger(this.getClass());

	@Test
	public void testUtils() throws Exception {
		try {
			logger.warn("\n\n\n\n\nwarn test\n\n\n\n");
			logger.info("info 1");
			logger.debug("debug 1");

			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			logger.info(" 1");

			Configuration config = ctx.getConfiguration();
			logger.info(" 2");
			logger.info(ctx.getConfiguration().getAppenders());
			FileAppender techical = (FileAppender) config.getAppender("FileAppender");
			logger.info(" 3");
			File dir = new File(techical.getFileName().replaceFirst("[^\\/]+$", ""));
			logger.info(dir);
			testES();

			// testMLSErverStreamOpen();
			// Thread.sleep(30000);
			// testMLServerStream();

			// testMLServerFlush();
			// testMLServerStreamLibSvm();
			// testMLServerStream();
			// testMLServerStreamLibSvm();
			// Thread.sleep(20000);
			// testLoadLibSvm();

			// testMergeLibSvm();
			// testMergeLibSvmValidated();

			assertTrue(1 == 1);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	private XContentBuilder jsonIndex() throws Exception {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.startObject("properties");
			{
				builder.startObject("message");
				{
					builder.field("type", "text");
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	private void testES() throws Exception {

		try {
			try {
				RestHighLevelClient client = ElasticSearchClientFactory.getInstance().initiateClient("elastic",
						"883-8177", "192.168.0.141", 9200);
				logger.info("ES " + client.toString());

				GetIndexRequest request = new GetIndexRequest("utindex");
				boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
				logger.info("utindex exists: " + exists);
				if (!exists) {
					CreateIndexRequest createRequest = new CreateIndexRequest("utindex");
					createRequest.settings(
							Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));
					createRequest.mapping(jsonIndex());

					CreateIndexResponse createIndexResponse = client.indices().create(createRequest,
							RequestOptions.DEFAULT);
					boolean acknowledged = createIndexResponse.isAcknowledged();
					boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();

					logger.info("acknowledged: " + acknowledged);
					logger.info("shardsAcknowledged: " + shardsAcknowledged);
				}
			} catch (Exception e) {
				logger.error(e);
				fail(e.getMessage());
			}

		} catch (Exception e) {

			fail(e.getMessage());
		}

	}

	private void testMLSErverStreamOpen() throws Exception {
		try {
			Spreadsheet.getInstance().initCsvSpreadsheet("CATEGORY_LOG",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");
			Spreadsheet.getInstance().getCsvSpreadSheet("CATEGORY_LOG").setLocalPorts("8081,8082,8083");
			Spreadsheet.getInstance().getCsvSpreadSheet("CATEGORY_LOG").setLocalUrl("192.168.0.15");

			String response = Spreadsheet.getInstance().getCsvSpreadSheet("CATEGORY_LOG")
					.sparkStreamInit("192.168.0.141", 9999);
			logger.info("\n\ntestMLSErverStreamOpen open stream response " + response);
			assertTrue(response.toUpperCase().equals("INITIALIZED"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	private void testMLServerStream() throws Exception {
		try {
			logger.info("testMLServerStream");
			Spreadsheet.getInstance().getCsvSpreadSheet("CATEGORY_LOG").loadCsv("CATEGORY_LOG.csv", "A_RowId");

			String response = Spreadsheet.getInstance().getCsvSpreadSheet("CATEGORY_LOG")
					.sparkStreamRun("192.168.0.141", 9999);
			assertTrue(response.toUpperCase().equals("SUCCESS"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	private void testMLServerStreamLibSvm() throws Exception {
		try {
			logger.info("testMLServerStream");
			Spreadsheet.getInstance().initLibsvm2("test", "TargetColumn",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");
			Spreadsheet.getInstance().getLibSvmProcessor("test").setKeyspaceName("Test-keyspace&NAME");
			Spreadsheet.getInstance().getLibSvmProcessor("test").loadLibsvm("test");

			logger.info("\n\n\n\n\nLoaded Rows" + Spreadsheet.getInstance().getLibSvmProcessor("test").rowCount()
					+ " Cols:" + Spreadsheet.getInstance().getLibSvmProcessor("test").columnCount() + "\n\n\n");
			Spreadsheet.getInstance().getCsvSpreadSheet("test").setLocalPorts("8081,8082,8083,8084");
			Spreadsheet.getInstance().getCsvSpreadSheet("test").setLocalUrl("192.168.0.15");
			String response1 = Spreadsheet.getInstance().getLibSvmProcessor("test").sparkStreamInit("192.168.0.141",
					9999);
			String response = Spreadsheet.getInstance().getLibSvmProcessor("test").sparkStreamRunLibsvm("192.168.0.141",
					9999);
			assertTrue(response.toUpperCase().equals("SUCCESS"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	private void testLoadLibSvm() throws Exception {
		try {
			logger.info("testMLServerStream");
			Spreadsheet.getInstance().initLibsvm2("test", "TargetColumn",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");
			Spreadsheet.getInstance().getLibSvmProcessor("test").loadLibsvm("test", true);
			String[] s = {};
			Spreadsheet.getInstance().getLibSvmProcessor("test").printLibSvmFinal(s);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	private void testMergeLibSvm() throws Exception {
		try {
			logger.info("testMLServerStream");

			Spreadsheet.getInstance().initLibsvm2("_part0", "A_AggregatedMajor",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");
			Spreadsheet.getInstance().getLibSvmProcessor("_part0").setPkColumnName("A_UID");
			Spreadsheet.getInstance().getLibSvmProcessor("_part0").loadLibsvm("_part0");

			Spreadsheet.getInstance().initLibsvm2("_part1", "A_AggregatedMajor",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");

			Spreadsheet.getInstance().getLibSvmProcessor("_part1").setPkColumnName("A_UID");
			Spreadsheet.getInstance().getLibSvmProcessor("_part1").loadLibsvm("_part1");
			Spreadsheet.getInstance().getLibSvmProcessor("_part0")
					.mergeIntoLibsvm(Spreadsheet.getInstance().getLibSvmProcessor("_part1"));

			String[] s = {};
			Spreadsheet.getInstance().getLibSvmProcessor("_part0").printLibSvmFinal("testMergedNew", s);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

	private void testMergeLibSvmValidated() throws Exception {
		try {
			logger.info("testMLServerStream");
			String[] s = {};
			Spreadsheet.getInstance().initLibsvm2("_part00", "A_AggregatedMajor",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");
			Spreadsheet.getInstance().getLibSvmProcessor("_part00").setPkColumnName("A_UID");
			Spreadsheet.getInstance().getLibSvmProcessor("_part00").loadLibsvm("_part0");

			Spreadsheet.getInstance().getLibSvmProcessor("_part00").printLibSvmFinal("testMergedjust1", s);

			Spreadsheet.getInstance().initLibsvm2("_part1", "A_AggregatedMajor",
					"C:\\Users\\jake\\__workspace\\ikoda-utils\\src\\test\\resources");

			Spreadsheet.getInstance().getLibSvmProcessor("_part1").setPkColumnName("A_UID");
			Spreadsheet.getInstance().getLibSvmProcessor("_part1").loadLibsvm("_part1");
			Spreadsheet.getInstance().getLibSvmProcessor("_part00")
					.mergeIntoLibsvmValidating(Spreadsheet.getInstance().getLibSvmProcessor("_part1"));

			Spreadsheet.getInstance().getLibSvmProcessor("_part00").printLibSvmFinal("testMergedOld", s);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			fail(e.getMessage());
		}
	}

}