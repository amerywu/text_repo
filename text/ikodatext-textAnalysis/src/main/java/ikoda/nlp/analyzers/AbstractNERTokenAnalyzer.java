package ikoda.nlp.analyzers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;
import ikoda.nlp.structure.PropertiesSingleton;
import ikoda.persistence.model.PossibleTokenCount;
import ikoda.persistence.service.JobAnalysisService;
import ikoda.persistence.service.JobApplicationContextProvider;

public abstract class AbstractNERTokenAnalyzer extends AbstractTokenAnalyzer
{

    protected final static String SPACE = " ";
    protected final static String UNDERSCORE = "_";
    protected static HashMap<String, Integer> phraseCounter = new HashMap<String, Integer>();
    public final static String NN = "NN";

    public final static String CC = "CC";
    public final static String DT = "DT";
    public final static String IN = "IN";
    public final static String VBD = "VBD";

    public final static String VBG = "VBG";
    public final static String VB = "VB";
    public final static String VBN = "VBN";
    public final static String VBP = "VBP";
    public final static String VBZ = "VBZ";
    public final static String MD = "MD";
    public final static String NNP = "NNP";
    public final static String NNS = "NNS";
    public final static String JJ = "JJ";
    public final static String JJR = "JJR";
    public final static String JJS = "JJS";
    public final static String NNPS = "NNPS";
    public final static String TITLE = "TITLE";
    public final static String PRP = "PRP";
    public final static String LRB = "-LRB-";
    public final static String RRB = "-RRB-";

    public final static String RSB = "-RSB-";
    public final static String LSB = "-LSB-";
    public final static String LCB = "-LCB-";
    public final static String RCB = "-RCB-";
    public final static String PRPDOLLAR = "PRP$";

    public final static String OR_OPERATOR = "|";
    public final static String COLON = ":";
    public final static String SEMICOLON = ":";
    public final static String COMMA = ",";
    public final static String PERIOD = ".";
    public final static String ORGANIZATION = "ORGANIZATION";
    public final static String JOBSUFFIX_IST = "IST";
    public final static String JOBSUFFIX_IAN = "IAN";
    public final static String DEGREE_IN = "DEGREE IN";
    protected AnalyzedFile af;

    private JobAnalysisService jobAnalysisService;

    public AbstractNERTokenAnalyzer(AnalyzedFile inaf)
    {
        super(inaf);
        af = inaf;

    }

    private void addToCounter(String inpossibleJob, String itokeType)
    {

        String possiblePhraseUC = inpossibleJob.toUpperCase().trim();
        // TALog.getLogger().debug("adding " + possibleJobUC);
        Integer count = phraseCounter.get(possiblePhraseUC);
        // TALog.getLogger().debug("count " + count);

        if (null == count)
        {
            phraseCounter.put(possiblePhraseUC, new Integer(1));
        }
        else
        {
            int newCount = count.intValue() + 1;
            // TALog.getLogger().debug("incrementing count" + newCount);
            phraseCounter.put(itokeType + UNDERSCORE + possiblePhraseUC, new Integer(newCount));
        }
    }

    protected abstract void checkPreviousSentence(PossibleSentence psentence);

    protected abstract void checkTokens(PossibleSentence psentence);

    protected List<IdentifiedToken> findPhrases(PossibleSentence psentence, String[] breakFromPOSArray,
            String itokenTypeForAnalysis)
    {
        String itokenTypeForReturn = itokenTypeForAnalysis;
        String[] stopLemmas = new String[0];
        return findPhrases(psentence, breakFromPOSArray, stopLemmas, itokenTypeForAnalysis, itokenTypeForReturn);
    }

    protected List<IdentifiedToken> findPhrases(PossibleSentence psentence, String[] breakFromPOSArray,
            String[] stopLemmas, String itokenTypeForAnalysis)
    {
        String itokenTypeForReturn = itokenTypeForAnalysis;

        return findPhrases(psentence, breakFromPOSArray, stopLemmas, itokenTypeForAnalysis, itokenTypeForReturn);
    }

    protected List<IdentifiedToken> findPhrases(PossibleSentence psentence, String[] breakFromPOSArray,
            String[] stopLemmas, String itokenTypeForAnalysis, String itokenTypeForReturn)
    {
        try
        {
            List<IdentifiedToken> identifiedTokens = new ArrayList<IdentifiedToken>();
            CoreMap sentence = psentence.getSentence();
            // TALog.getLogger().debug("findJobTitles: " + sentence.toString());
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {

                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                String word = token.get(TextAnnotation.class);
                if (currNeToken.equals(itokenTypeForAnalysis))
                {

                    TALog.getLogger().debug("Starting with: " + word);

                    int position = token.index();
                    // TALog.getLogger().debug("Index is: " + position);
                    List<String> possiblePhrases = possiblePhrases(position, sentence, breakFromPOSArray, stopLemmas);
                    String value = longestString(possiblePhrases);
                    IdentifiedToken itoken = new IdentifiedToken(itokenTypeForReturn, value);

                    identifiedTokens.add(itoken);
                    for (String s : possiblePhrases)
                    {
                        TALog.getLogger().debug("FOUND PHRASE: " + s);

                        if (s.trim().contains(SPACE))
                        {
                            saveOrUpdate(itokenTypeForReturn, s);
                        }

                    }

                }
            }
            return identifiedTokens;

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return new ArrayList<IdentifiedToken>();
        }
    }

    private String longestString(List<String> strings)
    {
        String longest = new String();
        for (String s : strings)
        {
            if (s.length() > longest.length())
            {
                longest = s;
            }
        }
        return longest;
    }

    protected boolean matchesPosTag(String posTag, String[] breakPOSArray)
    {
        // TALog.getLogger().debug(posTag);
        for (int i = 0; i < breakPOSArray.length; i++)
        {

            if (breakPOSArray[i].equals(posTag))
            {
                TALog.getLogger().debug("Found " + posTag);
                return true;
            }
        }
        return false;
    }

    protected boolean matchesStopLemma(String lemma, String[] stopLemmas)
    {
        // TALog.getLogger().debug(posTag);
        for (int i = 0; i < stopLemmas.length; i++)
        {

            if (stopLemmas[i].toUpperCase().equals(lemma.toUpperCase()))
            {
                TALog.getLogger().debug("Found stop lemma " + lemma);
                return true;
            }
        }
        return false;
    }

    private List<String> possiblePhrases(int currentIndex, CoreMap sentence, String[] breakFromPOSArray,
            String[] stopLemmas)
    {
        try
        {
            int i = currentIndex;
            StringBuilder sb = new StringBuilder();
            List<String> possibleJobTitles = new ArrayList<String>();
            whileloop1: while (i > 0)
            {

                for (CoreLabel token : sentence.get(TokensAnnotation.class))
                {
                    int index = token.get(IndexAnnotation.class);
                    if (index == i)
                    {
                        TALog.getLogger().debug("< got token : " + token.word());
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        TALog.getLogger().debug("pos; " + pos);
                        if (matchesPosTag(pos, breakFromPOSArray) || matchesStopLemma(token.lemma(), stopLemmas))
                        {
                            TALog.getLogger().debug("break..");
                            break whileloop1;
                        }
                        else
                        {

                            sb.insert(0, token.word() + SPACE);
                            possibleJobTitles.add(sb.toString());
                            TALog.getLogger().debug("got possible phrase: " + sb.toString());

                        }

                    }
                }

                i--;

            }
            sb = new StringBuilder();
            int j = currentIndex;

            int sentenceLength = sentence.size();

            whileloop: while (j <= sentenceLength)
            {
                for (CoreLabel token : sentence.get(TokensAnnotation.class))
                {
                    int index = token.get(IndexAnnotation.class);
                    if (index == j)
                    {
                        TALog.getLogger().debug("> got token : " + token.word());
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        TALog.getLogger().debug("pos; " + pos);
                        if (matchesPosTag(pos, breakFromPOSArray) || matchesStopLemma(token.lemma(), stopLemmas))
                        {
                            TALog.getLogger().debug("break..");
                            break whileloop;
                        }
                        else
                        {
                            sb.append(token.word());
                            possibleJobTitles.add(sb.toString());
                            sb.append(SPACE);
                            TALog.getLogger().debug("got possible phrase: " + sb.toString());

                        }

                    }
                }

                j++;

            }

            return possibleJobTitles;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return new ArrayList<String>();
        }
    }

    public abstract void processSentenceWithNerToken();

    protected String processTripleAsStringValue(List<CoreLabel> objectPhrase, String[] inMatchPos)
    {
        try
        {
            TALog.getLogger().debug("processTripleObject" + objectPhrase);
            StringBuilder sb = new StringBuilder();
            for (CoreLabel token : objectPhrase)
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                TALog.getLogger().debug("before: " + token.before());
                TALog.getLogger().debug("after: " + token.after());
                TALog.getLogger().debug("pos: " + pos + "  word " + token.word());
                if (!matchesPosTag(pos, inMatchPos))
                {
                    return null;
                }

                sb.append(token.word());
                sb.append(SPACE);
            }

            TALog.getLogger().debug("guessing at " + sb);
            return sb.toString();

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return null;
        }
    }

    protected abstract void processTriples(PossibleSentence psentence);

    @Transactional
    protected void saveOrUpdate(String itokenType, String invalue)
    {
        String value = invalue.trim().toUpperCase();

        // TALog.getLogger().debug("Possible Token: "+itokenType+" == "+value);
        try
        {
            String existingNerToken = PropertiesSingleton.getInstance().getNerPropertyTypeByToken(value);
            if (null != existingNerToken)
            {
                return;
            }
            jobAnalysisService = JobApplicationContextProvider.getInstance().getJobAnalysisService();
            PossibleTokenCount ptc = jobAnalysisService.getPossibleTokenCount(itokenType, value);
            if (null == ptc)
            {
                ptc = new PossibleTokenCount();
                ptc.setValue(value.toUpperCase());
                ptc.setTokenType(itokenType);
                ptc.setCount(new Integer(1));
                jobAnalysisService.savePossibleTokenCount(ptc);
                return;
            }
            Integer oldCount = ptc.getCount();
            Integer newCount = oldCount + 1;
            ptc.setCount(newCount);
            jobAnalysisService.updatePossibleTokenCount(ptc);
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
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
