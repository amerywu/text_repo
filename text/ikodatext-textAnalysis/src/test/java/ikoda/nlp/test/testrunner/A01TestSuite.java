package ikoda.nlp.test.testrunner;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ikoda.nlp.test.tests.LaunchTA;

@RunWith(Suite.class)
@Suite.SuiteClasses({ LaunchTA.class
        // ArticleNlpRunner.class
        // TestTA.class

})
public class A01TestSuite
{
    @AfterClass
    public static void tearDown()
    {
        // HibernateDAO.getSessionFactory().close();
    }

}