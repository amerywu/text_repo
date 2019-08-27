package ikoda.nlp.analysis;

import java.io.PrintWriter;

import org.apache.logging.log4j.*;

public class TALog
{

    private static Logger log = LogManager.getLogger("ikoda.textanalysis");
    private static Logger ptlog = LogManager.getLogger("ikoda.possibletoken");
    private static Logger ptlog1 = LogManager.getLogger("ikoda.possibletoken1");

    public static Logger getLogger()
    {

        return log;
    }

    public static Logger getPTLogger()
    {

        return ptlog;
    }

    public static Logger getPTLogger1()
    {

        return ptlog1;
    }

    public static void saveStringToFile(String s)
    {

        try
        {

            log.debug("\n\n\n" + s + "\n\n\n\n");

            PrintWriter out1 = new PrintWriter("testoutput.txt", "UTF-8");

            out1.println(s);
            out1.close();

        }
        catch (Exception e)
        {

            log.error(e.getMessage(), e);

        }
    }

}