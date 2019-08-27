package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.nlp.analysis.TALog;

public abstract class AbstractAnalyzedText
{

    private class AnalyzedFileTokenCounter
    {
        private Map<Integer, Map<String, IdentifiedToken>> tokenMapByBlock = new HashMap<Integer, Map<String, IdentifiedToken>>();
        private Map<String, IdentifiedToken> tokenMap = new HashMap<>();

        private void addToken(IdentifiedToken itoken, Integer blockId)
        {
            addTokenByBlock(itoken, blockId);
            addTokenForFile(itoken);

        }

        private void addTokenByBlock(IdentifiedToken itoken, Integer blockId)
        {
            Map<String, IdentifiedToken> tokenCountMap = tokenMapByBlock.get(blockId);
            if (null == tokenCountMap)
            {
                tokenCountMap = new HashMap<String, IdentifiedToken>();
                tokenMapByBlock.put(blockId, tokenCountMap);
            }

            IdentifiedToken extantToken = tokenCountMap.get(generateKey(itoken));
            if (null == extantToken)
            {
                extantToken = new IdentifiedToken(itoken);
                tokenCountMap.put(generateKey(extantToken), extantToken);
                if (itoken.getFrequencyCount() > 1)
                {
                    extantToken.incrementFrequency(itoken.getFrequencyCount() - 1);
                }

            }
            else
            {
                extantToken.incrementFrequency(itoken.getFrequencyCount());
            }
        }

        private void addTokenForFile(IdentifiedToken itoken)
        {

            String key = generateKey(itoken);


            IdentifiedToken extantToken = tokenMap.get(key);
            if (null == extantToken)
            {
                extantToken = new IdentifiedToken(itoken);
                tokenMap.put(key, extantToken);

            }
            else
            {

                extantToken.incrementFrequency(itoken.getFrequencyCount());

            }

        }

        private String generateKey(IdentifiedToken itoken)
        {
            return itoken.getType() + TWOHASHTAG + itoken.getValue();
        }

        private Collection<IdentifiedToken> getCountedTokens()
        {
            return tokenMap.values();
        }

        private int getCountForIdentifiedTokenForBlock(Integer blockId, String type, String value)
        {

            Map<String, IdentifiedToken> tokenCountMap = tokenMapByBlock.get(blockId);
            if (null == tokenCountMap)
            {
                return 0;
            }
            Iterator<String> itr = tokenCountMap.keySet().iterator();
            while (itr.hasNext())
            {
                String key = itr.next();
                IdentifiedToken extantToken = tokenCountMap.get(key);
                if (generateKey(extantToken).equals(key))
                {
                    return extantToken.getFrequencyCount();
                }
            }
            return 0;

        }

        private int getCountForIdentifiedTokenForFile(String type, String value)
        {
            Iterator<String> itr = tokenMap.keySet().iterator();
            String searchKey = type + TWOHASHTAG + value;
 
            while (itr.hasNext())
            {
                String key = itr.next();
                IdentifiedToken extantToken = tokenMap.get(key);
                if (searchKey.equals(key))
                {

                    return extantToken.getFrequencyCount();
                }
            }
            return 0;

        }

        private int getCountMostFrequentIdenitifiedTokenInFile(String type)
        {

            int highestFrequency = 0;
            boolean equalValue = false;
            IdentifiedToken returnToken = null;

            Iterator<String> itr = tokenMap.keySet().iterator();
            while (itr.hasNext())
            {
                String key = itr.next();
                IdentifiedToken extantToken = tokenMap.get(key);
                if (extantToken.getType().equals(type))
                {

                    TALog.getLogger()
                            .trace(extantToken.getValue() + " has frequency of " + extantToken.getFrequencyCount());
                    if (extantToken.getFrequencyCount() > highestFrequency)
                    {
                        highestFrequency = extantToken.getFrequencyCount();
                        returnToken = extantToken;
                        equalValue = false;

                    }
                    else if (extantToken.getFrequencyCount() == highestFrequency)
                    {
                        equalValue = true;
                    }
                }
            }
            if (null == returnToken)
            {
                return 0;
            }
            if (equalValue)
            {
                TALog.getLogger().trace("Returning 0");
                return 0;
            }

            return returnToken.getFrequencyCount();

        }

        private IdentifiedToken getMostFrequentIdenitifiedTokenInBlock(Integer blockId, String type)
        {
            Map<String, IdentifiedToken> tokenCountMap = tokenMapByBlock.get(blockId);

            int highestFrequency = 0;
            boolean equalValue = false;
            IdentifiedToken returnToken = null;
            if (null == tokenCountMap)
            {
                return null;
            }
            Iterator<String> itr = tokenCountMap.keySet().iterator();
            while (itr.hasNext())
            {
                String key = itr.next();
                IdentifiedToken extantToken = tokenCountMap.get(key);
                if (extantToken.getType().equals(type))
                {

                    if (extantToken.getFrequencyCount() > highestFrequency)
                    {
                        highestFrequency = extantToken.getFrequencyCount();
                        returnToken = extantToken;
                        equalValue = false;

                    }
                    else if (extantToken.getFrequencyCount() == highestFrequency)
                    {
                        equalValue = true;
                    }
                }
            }
            if (equalValue)
            {
                return null;
            }
            return returnToken;

        }

        private IdentifiedToken getMostFrequentIdenitifiedTokenInFile(String type)
        {

            int highestFrequency = 0;
            boolean equalValue = false;
            IdentifiedToken returnToken = null;

            Iterator<String> itr = tokenMap.keySet().iterator();
            while (itr.hasNext())
            {
                String key = itr.next();
                IdentifiedToken extantToken = tokenMap.get(key);
                if (extantToken.getType().equals(type))
                {

                    TALog.getLogger()
                            .trace(extantToken.getValue() + " has frequency of " + extantToken.getFrequencyCount());
                    if (extantToken.getFrequencyCount() > highestFrequency)
                    {
                        highestFrequency = extantToken.getFrequencyCount();
                        returnToken = extantToken;
                        equalValue = false;

                    }
                    else if (extantToken.getFrequencyCount() == highestFrequency)
                    {
                        equalValue = true;
                    }
                }
            }
            if (equalValue)
            {
                TALog.getLogger().trace("EQUAL FREQUENCY");

            }
            TALog.getLogger().trace("Returning " + returnToken);
            return returnToken;

        }

        private void incrementTokenIfExists(IdentifiedToken itoken, Integer blockId)
        {
            Map<String, IdentifiedToken> tokenCountMap = tokenMapByBlock.get(blockId);
            if (null == tokenCountMap)
            {
                return;
            }

            IdentifiedToken extantToken = tokenCountMap.get(generateKey(itoken));
            if (null == extantToken)
            {
                return;
            }
            else
            {
                extantToken.incrementFrequency(itoken.getFrequencyCount());
            }

            IdentifiedToken extantTokenFileLevel = tokenMap.get(generateKey(itoken));
            if (null == extantTokenFileLevel)
            {
                return;
            }
            else
            {
                extantTokenFileLevel.incrementFrequency(itoken.getFrequencyCount());
            }
        }

    }

    protected final static String TWOHASHTAG = "##";

    protected StanfordCoreNLP pipeline;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private AnalyzedFileTokenCounter analyzedFileTokenCounter = new AnalyzedFileTokenCounter();
    protected int blocknumber = 10;
    protected Map<Integer, PossibleSentence> allSentences = new HashMap<>();
    protected Map<Integer, FinalSentence> finalSentences = new HashMap<>();
    protected PossibleSentence currentSentence;
    protected PossibleSentence precedingSentence;
    protected ConfigurationBeanParent config;

    protected String finalStatus = "";
    protected boolean successfulRun = false;

    protected AbstractAnalyzedText(ConfigurationBeanParent inconfig)
    {

        config = inconfig;
    }

    protected void addiTokenToCounter(IdentifiedToken itoken)
    {
        analyzedFileTokenCounter.addToken(itoken, blocknumber);
    }

    protected void addiTokenToCounter(IdentifiedToken itoken, int blocknumber)
    {
        analyzedFileTokenCounter.addToken(itoken, blocknumber);
    }

    protected String capitalize(String line, String region)
    {
        try
        {
            if (null == region)
            {
                return line;
            }
            TALog.getLogger().trace(line + " | " + region);
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
            TALog.getLogger().trace(line);
            return capLine.trim();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return line;
        }
    }

    protected void dumpStanford()
    {
        /// dump out all Stanford references
        Iterator<Integer> itr = allSentences.keySet().iterator();
        while (itr.hasNext())
        {
            Integer key = itr.next();
            PossibleSentence psentence = allSentences.get(key);
            psentence.clearStanfordReferences();

            FinalSentence fsentence = new FinalSentence(psentence);
            if (null == finalSentences.get(key))
            {
                finalSentences.put(key, fsentence);
            }
        }

    }

    public abstract void finalizeFile();

    public List<IdentifiedToken> getCountedTokens()
    {
        return new ArrayList<IdentifiedToken>(analyzedFileTokenCounter.getCountedTokens());
    }

    protected int getCountMostFrequentIdenitifiedTokenInFile(String type)
    {
        return analyzedFileTokenCounter.getCountMostFrequentIdenitifiedTokenInFile(type);
    }

    public String getFinalStatus()
    {
        return finalStatus;
    }

    public int getFrequencyCountForIdentifiedTokenValueForBlock(String type, String value)
    {
        return analyzedFileTokenCounter.getCountForIdentifiedTokenForBlock(blocknumber, type, value);
    }

    public int getFrequencyCountForIdentifiedTokenValueForFile(String type, String value)
    {
        return analyzedFileTokenCounter.getCountForIdentifiedTokenForFile(type, value);
    }

    public IdentifiedToken getMostFrequentIdenitifiedTokenInBlock(int inblockNumber, String type)
    {
        return analyzedFileTokenCounter.getMostFrequentIdenitifiedTokenInBlock(inblockNumber, type);
    }

    public IdentifiedToken getMostFrequentIdenitifiedTokenInFile(String type)
    {
        return analyzedFileTokenCounter.getMostFrequentIdenitifiedTokenInFile(type);
    }

    public StanfordCoreNLP getPipeline()
    {
        return pipeline;
    }

    public PossibleSentence getPrecedingSentence()
    {
        return precedingSentence;
    }

    public void incrementFrequencyForToken(IdentifiedToken itoken)
    {
        analyzedFileTokenCounter.incrementTokenIfExists(itoken, blocknumber);
    }

    public boolean isSuccessfulRun()
    {
        return successfulRun;
    }

    public PossibleSentence nextSentence(CoreMap sentence)
    {
        precedingSentence = currentSentence;
        dumpStanford();
        PossibleSentence ps = new PossibleSentence(sentence);
        ps.setInfoBoxId(blocknumber);

        Optional<Integer> o = allSentences.keySet().stream().max(Comparator.naturalOrder());
        if (o.isPresent())
        {
            Integer currentTopPosition = o.get();
 

            Integer newTopPosition = currentTopPosition + 1;

            ps.setPosition(newTopPosition);
        }
        else
        {
            ps.setPosition(0);
        }

        currentSentence = ps;
        TALog.getLogger().trace("adding to allSentences: "+ps);
        allSentences.put(ps.getPosition(), ps);
        return ps;

    }

    public void setPipeline(StanfordCoreNLP pipeline)
    {
        this.pipeline = pipeline;
    }

    public void setSuccessfulRun(boolean successfulRun)
    {
        this.successfulRun = successfulRun;
    }

}
