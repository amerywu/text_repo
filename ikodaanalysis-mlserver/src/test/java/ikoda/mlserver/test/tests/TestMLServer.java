package ikoda.mlserver.test.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;



import org.junit.Test;


import ikoda.mlserver.SimpleClient;
import ikoda.utils.SSm;









public class TestMLServer
{

	//@Autowired
	//private JobAnalysisServiceImpl jobAnalysisService;



	@Test
	public void testml() throws Exception
	{
	
		try
		{

			SSm.getAppLogger().warn("\n\n\n\n\ntestml\n\n\n\n");
			SSm.getAppLogger().info("info 1");
			SSm.getAppLogger().debug("debug 1");
			
			/**SimpleClient sc = new SimpleClient();
			SSm.getAppLogger().info("info 1");
			sc.startConnection("192.168.0.18", 8181);
			SSm.getAppLogger().info(sc.sendMessage("192.168.0.18"));
			

			//serverTest();
			assertTrue(1==1);*/
		}
		catch(Exception e)
		{

			fail(e.getMessage());
		}
		
	}
	







	
	
}