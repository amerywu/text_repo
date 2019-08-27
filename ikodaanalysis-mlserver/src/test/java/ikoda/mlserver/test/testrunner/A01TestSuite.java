package ikoda.mlserver.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ikoda.mlserver.test.tests.TestMLServer;






@RunWith(Suite.class)
@Suite.SuiteClasses(
{
		TestMLServer.class
		//TestPersistence.class,
		
		 })
public class A01TestSuite
{
	@AfterClass
	public static void tearDown()
	{
		//HibernateDAO.getSessionFactory().close();
	}

}