package ikoda.nlp.test.tests;

import org.junit.Test;

import ikoda.nlp.analysis.TALog;

public class LaunchTA
{

    @Test
    public void testEverythingNLP()
    {

        try
        {

            /*
             * FileAnalyzerThread a = new FileAnalyzerThread(); a.runFileAnalyzer("input",
             * "locations.txt", FileAnalyzerFactory.GENERIC_JOB_POST); a.join();
             */

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

}
