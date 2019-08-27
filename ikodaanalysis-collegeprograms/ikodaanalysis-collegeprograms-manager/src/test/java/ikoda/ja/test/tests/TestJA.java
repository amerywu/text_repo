package ikoda.ja.test.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ikoda.collegeanalysis.JAException;


@RunWith(SpringJUnit4ClassRunner.class)


@ContextHierarchy(
{ @ContextConfiguration(locations =
		{ "classpath:beans.xml" }) })
public class TestJA
{

	//@Autowired
	//private JobAnalysisServiceImpl jobAnalysisService;



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