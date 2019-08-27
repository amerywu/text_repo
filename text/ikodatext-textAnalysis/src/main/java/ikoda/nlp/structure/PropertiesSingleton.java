package ikoda.nlp.structure;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import ikoda.nlp.analysis.TALog;

public class PropertiesSingleton
{

    private static PropertiesSingleton businessProperties;

    public static PropertiesSingleton getInstance() throws Exception
    {
        if (null == businessProperties)
        {
            businessProperties = new PropertiesSingleton();
        }

        return businessProperties;
    }

    private final Properties chineseProperties = new Properties();
    private final Properties nerProperties = new Properties();

    private PropertiesSingleton() throws IKodaTextAnalysisException
    {

        try
        {
            initializeChineseProperties();

        }
        catch (Exception e)
        {
            TALog.getLogger().error("\n\n\n\n\n                 FAILED TO LOAD PROPERTIES: \n\n\n\n\n\n", e);
            throw new IKodaTextAnalysisException(e);

        }
    }

    public Properties getChineseProperties()
    {
        return chineseProperties;
    }

    public String getChinesePropertyValue(String key)
    {
        return chineseProperties.getProperty(key);
    }

    public Properties getNerProperties() throws Exception
    {
        if (nerProperties.size() == 0)
        {
            throw new Exception("Properties Not Initialized");
        }
        return nerProperties;
    }

    public String getNerPropertyTypeByToken(String key) throws Exception
    {
        if (nerProperties.size() == 0)
        {
            throw new Exception("Properties Not Initialized");
        }
        String s = nerProperties.getProperty(key.toUpperCase());
        TALog.getLogger().debug("returning " + s + " for " + key);
        ;
        return s;
    }

    private void initializeChineseProperties() throws Exception
    {
        try
        {
            InputStream stream = this.getClass().getResourceAsStream("/stanfordnlp-chineses.properties");
            if (null == stream)
            {
                throw new Exception("Cannot load stanfordnlp-chineses.properties");
            }
            chineseProperties.load(stream);
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    public void initializeNERProperties(String path) throws Exception
    {
        try
        {
            InputStream stream = this.getClass().getResourceAsStream("/" + path);
            TALog.getLogger().debug("initializeNERProperties " + "/" + path);
            if (null == stream)
            {
                throw new Exception("Cannot load ner properties");
            }
            String wholeFile = new BufferedReader(new InputStreamReader(stream)).lines()
                    .collect(Collectors.joining("\n"));
            wholeFile = wholeFile.replaceAll("\r", "");

            String[] allEntries = wholeFile.split("\n");
            TALog.getLogger().debug("All Entries Count " + allEntries.length);
            for (int i = 0; i < allEntries.length; i++)
            {
                TALog.getLogger().debug("Entry:  " + allEntries[i]);
                try
                {
                    String[] oneEntry = allEntries[i].split("\t");
                    String key = oneEntry[0];
                    String value = oneEntry[1];
                    TALog.getLogger().debug("Setting:  " + key + " | " + value);
                    nerProperties.setProperty(key, value);
                }
                catch (Exception e)
                {
                    TALog.getLogger().warn("COULD NOT GET PROPERTY PAIR FROM " + allEntries[i], e);
                }
            }

            TALog.getLogger().debug("Loaded " + nerProperties.size());
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            throw new Exception(e);
        }
    }
}
