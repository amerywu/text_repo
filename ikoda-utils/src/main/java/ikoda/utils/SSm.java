package ikoda.utils;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

/**Log setup
 * **/
public class SSm
{

	private static boolean configured = false;

	private static void buildLog()
	{
		try
		{

			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			System.out.println("Configuration found at " + ctx.getConfiguration().toString());

			if (ctx.getConfiguration().toString().contains(".config.DefaultConfiguration"))
			{

				System.out.println("\n\n\nNo log4j2 config available. Configuring programmatically\n\n");

				ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory
						.newConfigurationBuilder();

				builder.setStatusLevel(Level.ERROR);
				builder.setConfigurationName("IkodaLogBuilder");

				AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
						.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
				appenderBuilder
						.add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%C]  %msg%n%throwable"));
				builder.add(appenderBuilder);

				LayoutComponentBuilder layoutBuilder = builder.newLayout("PatternLayout").addAttribute("pattern",
						"%d [%C] %-5level: %msg%n");

				appenderBuilder = builder.newAppender("file", "File").addAttribute("fileName", "./logs/ikoda.log")
						.add(layoutBuilder);
				builder.add(appenderBuilder);

				builder.add(builder.newLogger("ikoda", Level.DEBUG).add(builder.newAppenderRef("file"))
						.add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false));

				builder.add(builder.newRootLogger(Level.DEBUG).add(builder.newAppenderRef("file"))
						.add(builder.newAppenderRef("Stdout")));
				((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).start(builder.build());
				ctx.updateLoggers();
			}
			else
			{
				System.out.println("Configuration file found.");
			}
			configured = true;
		}
		catch (Exception e)
		{
			System.out.println("\n\n\n\nFAILED TO CONFIGURE LOG4J2" + e.getMessage());
			configured = true;
		}
	}

	public static Logger getAppLogger()
	{

		if (!configured)
		{
			buildLog();
		}
		return LogManager.getLogger("ikoda");
	}

	public static String getDirectoryForAppender()
	{
		return getDirectoryForAppender("FileAppender");
	}

	public static String getDirectoryForAppender(String appenderName)
	{

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();

		FileAppender techical = (FileAppender) config.getAppender("FileAppender");

		File dir = new File(techical.getFileName().replaceFirst("[^\\/]+$", ""));
		return dir.toString();

	}

	public static Logger getLogger(Class c)
	{

		try
		{
			if (!configured)
			{
				System.out.println("checking config ");
				buildLog();
			}

			return LogManager.getLogger(c.getClass().getName());
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
	}

	public static Logger getLogger(String s)
	{
		if (!configured)
		{
			buildLog();
		}
		return LogManager.getLogger(s);
	}

	public static synchronized String getUserReadableTypeName(String contentElementTypeClassName)
	{
		String packageName = "jake.prototype2.model.assessment.";
		StringBuffer sb = new StringBuffer("");

		for (int i = packageName.length(); i < contentElementTypeClassName.length(); i++)
		{
			Character ch = contentElementTypeClassName.charAt(i);
			if (Character.isUpperCase(ch))
				sb.append(" " + ch);
			else
				sb.append(ch);
		}

		return sb.toString();
	}

	public static void logClasspath() throws IKodaUtilsException
	{
		try
		{
			ClassLoader c = new SSm().getClass().getClassLoader();
			// SSm.getLogger().debug("c="+c);
			StringBuffer sb = new StringBuffer();
			URLClassLoader u = (URLClassLoader) c;
			URL[] urls = u.getURLs();
			for (URL i : urls)
			{
				sb.append("url: ");
				sb.append(i);
				sb.append("\n");
			}
		}
		catch (Exception e)
		{
			SSm.getAppLogger().error("Oh, that didn't quite go as expected.", e);
		}
	}

	public static String logMapContents(Map map)
	{
		if (null == map)
		{
			return "Map is null\n\n";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("\n\n");
		Set keyset = map.keySet();
		for (Object key : keyset)
		{
			sb.append("\nKEY:");
			sb.append(key);
			sb.append("|  ");
			sb.append(map.get(key));
		}
		return sb.toString();
	}

	public static void saveStringToFile(String s)
	{

		try
		{

			// log.debug("\n\n\n"+s+"\n\n\n\n");

			PrintWriter out1 = new PrintWriter("testoutput.txt", "UTF-8");

			out1.println(s);
			out1.close();

		}
		catch (Exception e)
		{

			getAppLogger().error(e.getMessage(), e);

		}
	}

}