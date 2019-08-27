package ikoda.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/***
 * Utility for managining multiple properties files
 * @author jake
 *
 */
public class MultiplePropertiesSingleton
{
	private static MultiplePropertiesSingleton mpSingleton;

	public static MultiplePropertiesSingleton getInstance() throws Exception
	{
		if (null == mpSingleton)
		{
			mpSingleton = new MultiplePropertiesSingleton();
		}

		return mpSingleton;
	}

	private String logName = "jake.app";

	private HashMap<String, Properties> properties = new HashMap<String, Properties>();

	private MultiplePropertiesSingleton() throws Exception
	{

		try
		{

		}
		catch (Exception e)
		{
			SSm.getLogger(logName).error("\n\n\n\n\n                 FAILED TO LOAD PROPERTIES: \n\n\n\n\n\n", e);
			throw new Exception(e);

		}
	}

	public Properties getProperties(String fileName) throws Exception
	{
		try
		{

			Properties p = properties.get(fileName);
			if (null == p)
			{
				p = initializeProperties(fileName);
				properties.put(fileName, p);
			}
			return p;
		}
		catch (Exception e)
		{
			throw new Exception("\n\n\n\n\n                 FAILED TO LOAD PROPERTIES: \n\n\n\n\n\n" + e.getMessage(),
					e);
		}
	}

	private Properties initializeProperties(String fileName) throws Exception
	{
		try
		{
			SSm.getLogger(logName).info("Loading " + fileName);
			InputStream stream = this.getClass().getResourceAsStream("/" + fileName);

			if (null == stream)
			{
				throw new Exception("Cannot load " + fileName);
			}

			Properties p = new Properties();
			p.load(stream);
			return p;
		}
		catch (Exception e)
		{
			SSm.getLogger(logName).error(fileName + " " + e.getMessage(), e);
			throw new Exception(e);
		}
	}

	public void setLogName(String s)
	{
		logName = s;
	}

}
