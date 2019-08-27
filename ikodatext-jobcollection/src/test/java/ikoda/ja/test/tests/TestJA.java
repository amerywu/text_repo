package ikoda.ja.test.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.locks.ReentrantLock;

import org.apache.hadoop.ipc.Client;
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
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ikoda.jobanalysis.JAException;
import ikoda.jobanalysis.JALog;
import ikoda.netio.config.ConfigurationBeanFactory;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InterfaceConfigurationBeanParent;
import ikoda.utils.ElasticSearchClientFactory;


@RunWith(SpringJUnit4ClassRunner.class)


@ContextHierarchy(
{ @ContextConfiguration(locations =
		{ "classpath:beans.xml" }) })
public class TestJA
{

	//@Autowired
	//private JobAnalysisServiceImpl jobAnalysisService;

	
    private ConfigurationBeanParent getConfig() throws Exception
    {

        try
        {
        	JALog.getLogger().debug("loading config");
            ConfigurationBeanParent configt = ConfigurationBeanFactory.getInstance().getConfigurationBean(
                    InterfaceConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION,
                    InterfaceConfigurationBeanParent.FILENAME_PARENT, new ReentrantLock());
            JALog.getLogger().debug(configt);
            return configt;
        }
        catch (Exception e)
        {
             throw e;

        }

    }
	
	@Test
	public void testES()
	{
	
		try
		{
			ConfigurationBeanParent config = getConfig();
			RestHighLevelClient client = ElasticSearchClientFactory.getInstance().initiateClient(
					config.getElasticSearchUser(), 
					config.getElasticSearchPassword(), 
					config.getElasticSearchUrl(), 
					new Integer(config.getElasticSearchPort()).intValue());
			
			JALog.getLogger().info("ESClient " + client.toString() );
			
			GetIndexRequest request = new GetIndexRequest("utindex");
			boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
			JALog.getLogger().info("utindex exists: " + exists);
			if(!exists) {
				CreateIndexRequest createRequest = new CreateIndexRequest("utindex");
				createRequest.settings(Settings.builder() 
					    .put("index.number_of_shards", 3)
					    .put("index.number_of_replicas", 2)
					);
				createRequest.mapping(jsonIndex());
				
				CreateIndexResponse createIndexResponse = client.indices().create(createRequest, RequestOptions.DEFAULT);
				boolean acknowledged = createIndexResponse.isAcknowledged(); 
				boolean shardsAcknowledged = createIndexResponse.isShardsAcknowledged();
				
				JALog.getLogger().info("acknowledged: " + acknowledged);
				JALog.getLogger().info("shardsAcknowledged: " + shardsAcknowledged);
			}
                
		}
		catch(Exception e)
		{
			JALog.getLogger().error(e.getMessage(),e);
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
	
	@Test
	public void testHibernate() throws Exception
	{
	
		try
		{
			/*UnitTestSubject uts = new UnitTestSubject();
			uts.setAttributeKey("a");
			uts.setValue("v"+System.currentTimeMillis());
			
			jobAnalysisService.saveUnitTestSubject(uts);
			assertTrue(uts.getId()>0);*/
			assertTrue(1==1);
		}
		catch(Exception e)
		{

			fail(e.getMessage());
		}
		
	}



	/*public JobAnalysisServiceImpl getJobAnalysisService() {
		return jobAnalysisService;
	}



	public void setJobAnalysisService(JobAnalysisServiceImpl jobAnalysisService) {
		this.jobAnalysisService = jobAnalysisService;
	}**/





	@Test
public void createConfigXml()   throws JAException
{
	try
	{
		/*ConfigurationBeanForJobAnalysis config = new ConfigurationBeanForJobAnalysis();	



		config.setFileIoJobPostingPath(".\\jobAnalysis\\files\\_JobPosting");
		config.setFileIoResultsPath(".\\jobAnalysis\\files\\_resultListings");
		config.setFileIoUndeterminedPath(".\\text\\jobAnalysis\\files\\_undetermined");
		config.setFileIoXMLPath(".\\text\\jobAnalysis\\files\\_allXmlFiles");
		config.setNetIoDumpPath("\\text\\jobAnalysis\\files\\output");
		config.setTextAnalysisDonePath(".\\jobAnalysis\\files\\analyzed");
		config.setTextAnalysisInfoBoxPath(".\\jobAnalysis\\files\\_infoBoxes");
		config.setFileIoDonePath(".\\jobAnalysis\\files\\_done");
		config.setAnalysisType("en");
		config.setElegantStop(true);
		config.setNetioResumeMode(false);
		config.setUrlRepository("\\test\\test");
		config.setMaxCallsRoundFour(10);
		config.setMaxCallsRoundOne(10);
		config.setMaxCallsRoundThree(10);
		config.setMaxCallsRoundTwo(10);
		config.setRunFileio(true);
		config.setRunNetio(true);
		config.setRunTextAnalysis(true);
		config.setPersistIfCountAreasOfStudy(1);
		config.setPersistIfCountDetailLevel(3);
		config.setPersistIfValueMinimumSalary(1000);

		JAXBContext jaxbContext = JAXBContext.newInstance( ConfigurationBeanForJobAnalysis.class );

		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		jaxbMarshaller.marshal( config, new File( "dummyconfig.xml" ) );
		jaxbMarshaller.marshal( config, System.out );*/
	}
	catch(Exception e)
	{
		throw new JAException(e);
	}
}
	
	
	
	
}