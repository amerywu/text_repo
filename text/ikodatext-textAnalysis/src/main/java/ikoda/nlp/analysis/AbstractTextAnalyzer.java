package ikoda.nlp.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.util.StringUtils;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.spiders.AbstractWebSite;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PropertiesSingleton;

public abstract class AbstractTextAnalyzer
{

    static HashMap<String, Integer> jobCounter = new HashMap<String, Integer>();
    protected String finalStatus = "";

    protected StanfordCoreNLP pipeline;
    protected ConfigurationBeanParent config;

    public AbstractTextAnalyzer(StanfordCoreNLP inpipeline, ConfigurationBeanParent inconfig)
    {
        
        this.pipeline = inpipeline;
        this.config = inconfig;
    }

    protected String capitalize(String line, String region)
    {
        try
        {

            if (region.toUpperCase().contains("ZH"))
            {
                return line;
            }

            StringTokenizer token = new StringTokenizer(line);
            String capLine = "";
            while (token.hasMoreTokens())
            {
                String tok = token.nextToken().toString();
                capLine += Character.toUpperCase(tok.charAt(0)) + tok.substring(1).toLowerCase();
                capLine += " ";
            }

            return capLine.trim();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return line;
        }
    }

    protected String cleanString(String inString)
    {

        String outString = inString;
        if (StringUtils.countOccurrencesOf(inString, "\r\n\r\n\r\n\r\n\r\n") > 4)
        {
            TALog.getLogger().debug("Lots of new blocks");
            if (inString.contains(AbstractWebSite.LIST))
            {
                outString = inString.replace("\r\n\r\n\r\n\r\n\r\n", "\r\n" + AnalyzedFile.NEWBLOCK + "\r\n");
            }
        }
        
        outString = outString.replace("|[", "7u8e4");
        outString = outString.replace("]|", "7u8e5");
        outString = outString.replace(".com", "_com");
        outString = outString.replace(".co", "_co");
        outString = outString.replace("?", " .");
        outString = outString.replace("&", " and ");
        outString = outString.replace(".ca", "_ca");
        outString = outString.replace(".au", "_au");
        outString = outString.replace(".org", "_org");
        outString = outString.replace("www.", "www_");
        outString = outString.replace("\r\n", "\n");
        outString = outString.replace("\n\n", "\n");
        outString = outString.replace("\n", ".\n");
        outString = outString.replace("\n.\n", ".\n");
        outString = outString.replace("..", ".");
        outString = outString.replace("£", "$");
        outString = outString.replace("|", ",");
        outString = outString.replace("[", " ");
        outString = outString.replace("]", " ");
        outString = outString.replace("<", " ");
        outString = outString.replace(">", " ");
        outString = outString.replace(" \\.", ".");
        outString = outString.replace("   ", " ");
        outString = outString.replace("  ", " ");
        outString = outString.replace("`", "");
        outString = outString.replace("\"", "");
        outString = outString.replace(";", ".");
        outString = outString.replace("...", ".");
        outString = outString.replace("7u8e4", "|[");
        outString = outString.replace("7u8e5", "]|");
        char grave = 96;
        char apostrophe = 39;
        outString = outString.replace(grave, apostrophe);
        outString = outString.replaceAll("[^\\x00-\\x7E]", "");

        outString = outString.replace("\\", " ");
        outString = outString.replace("/", " ");
        outString = outString.replace(";", ".");

        return outString;

    }

    public ConfigurationBeanParent getConfig()
    {
        return config;
    }

    public String getFinalStatus()
    {
        return finalStatus;
    }

    public void printJobCount()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n");
        Iterator<String> itr = jobCounter.keySet().iterator();
        while (itr.hasNext())
        {
            String key = itr.next();
            Integer count = jobCounter.get(key);
            if (count >= 0)
            {
                sb.append(key);
                sb.append("\t");
                sb.append(count);
                sb.append("\n");
            }
        }
        TALog.getLogger().debug(sb.toString());
    }

    public abstract boolean processFile(Path file, ReentrantLock inLock);

    public abstract boolean processString(String s);

    // this is a hack to cancel out NER default tags
    protected String replaceWithJobCoreTagIfNecessary(String tag, String token, String previousWords, String[] badTags)
    {
        try
        {

            for (int i = 0; i < badTags.length; i++)
            {

                if (badTags[i].equals(tag))
                {

                    if (previousWords.length() > 0)
                    {
                        String phrase = previousWords.toUpperCase() + " " + token;
                        String newNERToken = PropertiesSingleton.getInstance().getNerPropertyTypeByToken(phrase);
                        if (null != newNERToken)
                        {
                            
                            return newNERToken;
                        }
                    }
                    String newNERToken = PropertiesSingleton.getInstance()
                            .getNerPropertyTypeByToken(token.toUpperCase());
                    if (null != newNERToken)
                    {
                        
                        return newNERToken;
                    }

                    return tag;
                }
            }

            return tag;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return IdentifiedToken.JOBCORE;

        }
    }

    public void setFinalStatus(String finalStatus)
    {
        this.finalStatus = finalStatus;
    }

    protected String validateString(String inString)
    {
        try
        {
            String cleanString = cleanString(inString);
            // TALog.getLogger().debug(cleanString);

            String[] sentenceArray = cleanString.split("[\\.\\?!]");
            List<String> sarray = new ArrayList<>();

            for (int i = 0; i < sentenceArray.length; i++)
            {
                sarray.add(sentenceArray[i]);
            }

            Iterator<String> itr = sarray.iterator();
            List<String> temp = new ArrayList<>();
            while (itr.hasNext())
            {
                String s = itr.next();
                if (s.length() > 400)
                {
                    TALog.getLogger().debug("removing " + s);

                    itr.remove();
                    String[] subArray = s.split("[,;]");
                    for (int j = 0; j < subArray.length; j++)
                    {
                        String substring = subArray[j];
                        if (substring.length() < 300)
                        {
                            temp.add(substring);

                        }
                    }

                }
            }
            sarray.addAll(temp);

            StringBuilder sb = new StringBuilder();
            for (String s : sarray)
            {
                sb.append(s);
                sb.append(".");
                sb.append(" ");
            }
            // TALog.getLogger().debug("\nreturning\n\n\n\n\n\n" + sb);
            return sb.toString();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "";
        }
    }

    protected int wordCountFromSentence(String sentence)
    {
        String words = sentence.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String words1 = words.replace("  ", " ");
        String[] wordsArray = words1.split(" ");
        return wordsArray.length;
    }
}
