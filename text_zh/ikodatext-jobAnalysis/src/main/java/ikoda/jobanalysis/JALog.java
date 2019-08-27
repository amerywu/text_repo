package ikoda.jobanalysis;

import java.io.PrintWriter;

import org.apache.log4j.*;

public class JALog
{

	private static Logger log;

	public static Logger getLogger()
	{
		if(null==log)
		{
			log= LogManager.getLogger("ikoda.jobanalysis");
			
		}
		
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