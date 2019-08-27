package ikoda.utils.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ikoda.utils.test.tests.TestUtils;




@RunWith(Suite.class)
@Suite.SuiteClasses(
{
		TestUtils.class
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