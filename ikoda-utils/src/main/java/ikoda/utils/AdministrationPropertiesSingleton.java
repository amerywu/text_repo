package ikoda.utils;

import java.io.InputStream;
import java.util.Properties;

/***
 * Properties management utility
 * @author jake
 *
 */
public class AdministrationPropertiesSingleton
{

	private static AdministrationPropertiesSingleton businessProperties;

	public static AdministrationPropertiesSingleton getInstance() throws Exception
	{
		if (null == businessProperties)
		{
			businessProperties = new AdministrationPropertiesSingleton();
		}

		return businessProperties;
	}

	private final Properties properties = new Properties();

	private AdministrationPropertiesSingleton() throws Exception
	{

		try
		{
			initializeProperties();

		}
		catch (Exception e)
		{
			SSm.getAppLogger().error("\n\n\n\n\n                 FAILED TO LOAD PROPERTIES: \n\n\n\n\n\n", e);
			throw new Exception(e.getMessage());
		}
	}

	public String getPropertyValue(String key)
	{
		return properties.getProperty(key);
	}

	private void initializeProperties() throws Exception
	{
		try
		{
			InputStream stream = this.getClass().getResourceAsStream("/administration.properties");
			if (null == stream)
			{
				throw new Exception("Cannot load business.properties");
			}
			properties.load(stream);
		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
			throw new Exception(e);
		}
	}

}
