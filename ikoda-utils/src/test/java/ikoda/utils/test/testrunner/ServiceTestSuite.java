package ikoda.utils.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
{



})
public class ServiceTestSuite
{
	@AfterClass
	public static void tearDown()
	{
		// HibernateDAO.getSessionFactory().close();
	}

}