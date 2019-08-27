package ikoda.persistence.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
{
		// TestPersistence.class
		// TestPersistence.class,

})
public class A01TestSuite
{
	@AfterClass
	public static void tearDown()
	{
		// HibernateDAO.getSessionFactory().close();
	}

}