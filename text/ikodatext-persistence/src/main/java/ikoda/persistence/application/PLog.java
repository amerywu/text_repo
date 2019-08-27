package ikoda.persistence.application;

import java.io.PrintWriter;

import org.apache.logging.log4j.*;

public class PLog
{

	private static Logger log=LogManager.getLogger("ikoda.persistence");
	private static Logger clog=LogManager.getLogger("ikoda.persistencechores");
	
	private static Logger rlog=LogManager.getLogger("ikoda.persistencereporting");;

	public static Logger getChoresLogger()
	{


		return clog;
	}
	
	public static Logger getLogger()
	{


		return log;
	}


	public static Logger getRLogger()
	{


		return rlog;
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