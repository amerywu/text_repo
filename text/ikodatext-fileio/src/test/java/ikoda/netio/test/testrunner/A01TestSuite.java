package ikoda.netio.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ikoda.netio.test.tests.TestFileio;

@RunWith(Suite.class)
@Suite.SuiteClasses(
{
		TestFileio.class
		//TestFileio.class,
		
		 })
public class A01TestSuite
{
	@AfterClass
	public static void tearDown()
	{
		//HibernateDAO.getSessionFactory().close();
	}

}