package ikoda.persistenceforanalysis.application;

import java.io.PrintWriter;

import org.apache.logging.log4j.*;

public class PLog
{

	private static Logger log;
	private static Logger clog;
	

	public static Logger getLogger()
	{
		if (null == log)
		{
			log = LogManager.getLogger("ikoda.persistence");

		}

		return log;
	}
	
	public static Logger getChoresLogger()
	{
		if (null == clog)
		{
			clog = LogManager.getLogger("ikoda.persistencechores");

		}

		return clog;
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