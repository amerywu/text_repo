package ikoda.ja.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ikoda.ja.test.tests.TestJA;


@RunWith(Suite.class)
@Suite.SuiteClasses(
{
	
		TestJA.class
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