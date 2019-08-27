package ikoda.netio;

import java.io.File;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;

public class NioLog
{

	private static Logger log = LogManager.getLogger("ikoda.netio");
	private static Logger oculog = LogManager.getLogger("ikoda.opencpu");

	public static Logger getLogger()
	{

		return log;
	}

	public static Logger getOcuLogger()
	{

		return oculog;
	}

	public static void saveStringToFile(String s)
	{

		try
		{

			// log.debug("\n\n\n" + s + "\n\n\n\n");

			PrintWriter out1 = new PrintWriter("testoutput.txt", "UTF-8");

			out1.println(s);
			out1.close();

		}
		catch (Exception e)
		{

			log.error(e.getMessage(), e);

		}
	}

	public static String getDirectoryForAppender()
	{
		return getDirectoryForAppender("FileAppender");
	}

	public static String getDirectoryForAppender(String appenderName)
	{

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();

		FileAppender techical = (FileAppender) config.getAppender(appenderName);

		File dir = new File(techical.getFileName().replaceFirst("[^\\/]+$", ""));
		return dir.toString();

	}

}