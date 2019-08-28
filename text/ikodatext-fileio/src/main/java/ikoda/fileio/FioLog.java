package ikoda.fileio;

import java.io.PrintWriter;


import org.apache.logging.log4j.*;
public class FioLog
{

	private static Logger log = LogManager.getLogger("ikoda.fileio");

	public static Logger getLogger()
	{

		return log;
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