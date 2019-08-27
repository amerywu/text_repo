package ikoda.nlp.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.nlp.structure.AnalyzedFileFactory;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.JobDescriptionAnalyzedFile;
import ikoda.nlp.structure.PossibleSentence;
import ikoda.utils.DuplicateEntryChecker;
import ikoda.utils.ProcessStatus;
import ikoda.utils.TicToc;

public abstract class IdentifiedTokenGeneratingAnalyzer extends AbstractTextAnalyzer
{

    protected static final String SPACE = " ";
    protected static final String END = "\n";
    protected static final String STOP = ".";
    private static final String JJ = "JJ";
    private static final String JJR = "JJR";
    private static final String JJS = "JJS";
    private static final String RB = "RB";

    private static final String NN = "NN";

    private static final String NNS = "NNS";
    private static final String CC = "CC";
    private static final String DT = "DT";
    private static final String IN = "IN";
    private static final String VBD = "VBD";
    private static final String VBG = "VBG";
    private static final String VB = "VB";
    private static final String VBN = "VBN";
    private static final String VBP = "VBP";
    private static final String VBZ = "VBZ";
    private static final String MD = "MD";
    private static final String NNP = "NNP";
    private static final String NNPS = "NNPS";
    private static final String TITLE = "TITLE";
    private static final String PRP = "PRP";
    private static final String PRPDOLLAR = "PRP$";
    private static final String RP = "RP";
    private static final String HASH = "#";
    private static final String GT = ">";
    private static final String LT = "<";
    private static final String COMMA = ",";
    private static final String UNDERSCORE = "_";
    private static final String DOLLAR = "$";
    private static final String PERCENT = "%";
    private static final String DOT = ".";
    private static final String ASTERISK = "*";
    private static final String RDT = "REDDIT";
    private static final String AT = "@";
    private static final String WWW = "WWW";
    private static final String LSB = "-lsb-";
    private static final String RSB = "-rsb-";
    private static final String RRB = "-lrb-";
    private static final String LRB = "-rrb-";
    private static final String SMILEY = ":-rrb-";
    private static final String FROWNY = ":-lrb-";

    private static String[] verbForms = { VB, VBN, VBP, VBD, VBZ, VBG };

    private static String[] reflexVerbForms = { RP };

    private static String[] ignore = { RRB, LRB, SMILEY, FROWNY, GT, LT, RDT, LSB, RSB, WWW, HASH, UNDERSCORE, DOLLAR,
            COMMA, PERCENT, DOT, ASTERISK, AT };

    private static String[] imperativeForms = { VB, VBG };

    private static String[] nounForms = { NN, NNS, NNP };

    private static String[] adjForms = { JJ, RB, JJS, JJR };

    private static String[] breakFromJobTitleSearch = { VB, VBN, VBP, VBD, VBG, VBZ, ".", ":", "?", PRP, PRPDOLLAR, MD,
            DT };

    private static final String PUNCTUATIONMARK = ",-:;()";

    protected ReentrantLock lock;

    protected HashMap<Integer, List<IdentifiedToken>> countedTokensBySentence = new HashMap<>();

    protected List<IdentifiedToken> countedTokens = new ArrayList<>();

    private String[] ignoreLemmatizedPhrasesContaining = {};

    private String[] ignorePhrasesContaining = {};
    
    private String[] ignoreLemmatizedPhrasesMatchingIgnoreCase = {};

    private String[] ignorePhrasesMatchingIgnoreCase = {};
    
    
    private String[] ignoreLemmatizedWordMatchingIgnoreCase = {};

    private String[] ignoreWordMatchingIgnoreCase = {};
    
    

    protected boolean collectPos = false;

    protected boolean collectAllTerms = false;

    protected boolean lemmatizeText = false;
    protected boolean collectBySentence = false;
    protected boolean banSentenceDuplicates = false;

    public IdentifiedTokenGeneratingAnalyzer(StanfordCoreNLP inpipeline, ConfigurationBeanParent inconfig)
    {
        super(inpipeline, inconfig);

    }

    private List<String> breakUpFile(String s)
    {
        try
        {
            List<String> fileParts = new ArrayList<>();

            if (s.length() < 30000)
            {
                fileParts.add(s);
            }
            else
            {
                String remaining = s;
                while (remaining.length() > 30000)
                {
                    String part = remaining.substring(0, 30000);
                    remaining = remaining.substring(30000, remaining.length());
                    fileParts.add(part);
                }
                fileParts.add(remaining);
                TALog.getLogger().trace("processString Broke into " + fileParts.size() + " parts.");
            }
            return fileParts;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return new ArrayList<String>();
        }
    }

    public Collection<IdentifiedToken> getCountedTokens()
    {
        if (null == countedTokens)
        {
            TALog.getLogger().warn("countedTokens is null");
            countedTokens = new ArrayList<IdentifiedToken>();
        }
        Map<String, IdentifiedToken> sortingMap = new HashMap<String, IdentifiedToken>();
        for (IdentifiedToken itoken : countedTokens)
        {
            String key = itoken.getValue();
            IdentifiedToken sortedToken = sortingMap.get(key);
            if (null == sortedToken)
            {
                sortingMap.put(key, itoken);
            }
            else
            {
                sortedToken.incrementFrequency(itoken.getFrequencyCount());
            }
        }
        return sortingMap.values();
    }

    public Map<Integer, List<IdentifiedToken>> getCountedTokensBySentence()
    {

        return countedTokensBySentence;
    }

    public String[] getIgnoreLemmatizedPhrasesContaining()
    {
        return ignoreLemmatizedPhrasesContaining;
    }

    public String[] getIgnoreLemmatizedPhrasesMatchingIgnoreCase()
    {
        return ignoreLemmatizedPhrasesMatchingIgnoreCase;
    }

    public String[] getIgnoreLemmatizedWordMatchingIgnoreCase()
    {
        return ignoreLemmatizedWordMatchingIgnoreCase;
    }

    public String[] getIgnorePhrasesContaining()
    {
        return ignorePhrasesContaining;
    }

    public String[] getIgnorePhrasesMatchingIgnoreCase()
    {
        return ignorePhrasesMatchingIgnoreCase;
    }

    public String[] getIgnoreWordMatchingIgnoreCase()
    {
        return ignoreWordMatchingIgnoreCase;
    }

    protected IdentifiedToken handleiToken(String inKey, String s)
    {

        IdentifiedToken itoken = null;
        if (IdentifiedToken.isRelevantToken(inKey))
        {

            if (inKey.equals(IdentifiedToken.TITLE))
            {
                inKey = IdentifiedToken.JOBTITLE;
            }
            itoken = new IdentifiedToken(inKey, s);

        }

        return itoken;
    }

    protected IdentifiedToken handleiToken(String inKey, StringBuilder inSb)
    {
        return handleiToken(inKey, inSb.toString());
    }

    protected boolean isAcceptedLemma(String posTag)
    {
        if (isAdjective(posTag))
        {
            return true;
        }
        if (isNoun(posTag))
        {
            return true;
        }
        if (isVerb(posTag))
        {
            return true;
        }
        return false;
    }

    protected boolean isAdjective(String posTag)
    {

        for (int i = 0; i < adjForms.length; i++)
        {

            if (adjForms[i].equals(posTag))
            {

                return true;
            }
        }
        return false;
    }

    public boolean isBanSentenceDuplicates()
    {
        return banSentenceDuplicates;
    }

    public boolean isCollectAllTerms()
    {
        return collectAllTerms;
    }

    protected boolean isCollectPos()
    {
        return collectPos;
    }

    protected boolean isIgnorablePhrase(String phrase)
    {
        
        boolean ignorable=false;
        if(phrase.isEmpty())
        {
            return true;
        }


        if (phrase.substring(0,1).matches(".*\\d.*"))
        {
            ignorable= true;
        }



        for (int j = 0; j < ignorePhrasesContaining.length; j++)
        {
            if (phrase.toUpperCase().contains(ignorePhrasesContaining[j].toUpperCase()))
            {
                ignorable= true;
            }
        }
        
        for (int j = 0; j < ignoreLemmatizedPhrasesContaining.length; j++)
        {
            if (phrase.toUpperCase().contains(ignoreLemmatizedPhrasesContaining[j].toUpperCase()))
            {
                ignorable= true;
            }
        }
        
        for (int j = 0; j < ignorePhrasesMatchingIgnoreCase.length; j++)
        {
            if (phrase.toUpperCase().equalsIgnoreCase(ignorePhrasesMatchingIgnoreCase[j].toUpperCase()))
            {
                ignorable= true;
            }
        }
        
        for (int j = 0; j < ignoreLemmatizedPhrasesMatchingIgnoreCase.length; j++)
        {
            if (phrase.toUpperCase().equalsIgnoreCase(ignoreLemmatizedPhrasesMatchingIgnoreCase[j].toUpperCase()))
            {
                ignorable= true;
            }
        }
        if(ignorable)
        {
            TALog.getLogger().trace("Ignoring "+phrase);
        }

        return ignorable;
    }

    protected boolean isIgnorableWord(String word)
    {
        
        boolean ignorable=false;
        if(word.isEmpty())
        {
            return false;
        }


        if (word.matches(".*\\d.*"))
        {
           
            ignorable= true;
        }

        for (int i = 0; i < ignore.length; i++)
        {
            if (word.toUpperCase().startsWith(ignore[i].toUpperCase()))
            {
                ignorable= true;
            }
        }

        for (int j = 0; j < ignoreWordMatchingIgnoreCase.length; j++)
        {
            if (word.equalsIgnoreCase(ignoreWordMatchingIgnoreCase[j]))
            {
                
                ignorable= true;
            }
        }
        
        for (int j = 0; j < ignoreLemmatizedWordMatchingIgnoreCase.length; j++)
        {
            if (word.equalsIgnoreCase(ignoreLemmatizedWordMatchingIgnoreCase[j]))
            {
                ignorable= true;
            }
        }
        
        if(ignorable)
        {
            TALog.getLogger().trace("Ignoring "+word);
        }
        
      

        return ignorable;
    }

    protected boolean isImperativeSentence(PossibleSentence psentence)
    {
        try
        {
            /// because really short imperatives are typically button and link
            /// titles from the website
            if (wordCountFromSentence(psentence.getSentence().toString()) < 4)
            {
                return false;
            }
            CoreMap sentence = psentence.getSentence();
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                for (int i = 0; i < imperativeForms.length; i++)
                {
                    if (imperativeForms[i].equals(pos))
                    {

                        return true;
                    }
                }
                // only interested in first word
                return false;
            }
            return false;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return false;
        }
    }

    protected boolean isLemmatizeText()
    {
        return lemmatizeText;
    }

    protected boolean isNoun(String posTag)
    {

        for (int i = 0; i < nounForms.length; i++)
        {

            if (nounForms[i].equals(posTag))
            {

                return true;
            }
        }
        return false;
    }

    protected boolean isPunctuation(String posTag)
    {
        if (PUNCTUATIONMARK.contains(posTag))
        {
            return true;
        }
        return false;
    }
    
    
    
    protected boolean isReflexVerb(String posTag)
    {

        for (int i = 0; i < reflexVerbForms.length; i++)
        {

            if (reflexVerbForms[i].equals(posTag))
            {
                return true;
            }
        }
        return false;
    }

    protected boolean isVerb(String posTag)
    {

        for (int i = 0; i < verbForms.length; i++)
        {

            if (verbForms[i].equals(posTag))
            {

                return true;
            }
        }
        return false;
    }



    protected abstract String lemmatizeSentence(PossibleSentence psentence);

    protected void logPos(CoreMap sentence)
    {

        StringBuilder sb = new StringBuilder();
        sb.append("\n--------------\n");
        sb.append(sentence.toString());
        sb.append("\n");
        for (CoreLabel token : sentence.get(TokensAnnotation.class))
        {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

            sb.append(token.word());
            sb.append(" | ");
            sb.append(token.lemma());
            sb.append(" | ");
            sb.append(token.index());
            sb.append(" | ");
            sb.append(pos);
            sb.append("\n");

        }
        TALog.getLogger().trace(sb);
    }

    protected void processAll(PossibleSentence psentence)
    {

        TALog.getLogger().trace(psentence.toString());
        CoreMap sentence = psentence.getSentence();

        for (CoreLabel token : sentence.get(TokensAnnotation.class))
        {

            if (isIgnorableWord(token.word()))
            {
                continue;
            }

            IdentifiedToken itoken = handleiToken(IdentifiedToken.TERM, token.lemma());
            psentence.iTokensAdd(itoken);

        }

    }

    @Override
    public boolean processFile(Path file, ReentrantLock inLock)
    {
        return false;
    }

    protected void processPos(PossibleSentence psentence)
    {

        StringBuilder sb = new StringBuilder();
        CoreMap sentence = psentence.getSentence();

        for (CoreLabel token : sentence.get(TokensAnnotation.class))
        {
            if (isIgnorableWord(token.word()))
            {
                continue;
            }
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

            /// and if we already have a verb, we check for reflex
            if (sb.length() > 0)
            {

                if (isReflexVerb(pos))
                {
                    sb.append(" ");
                    sb.append(token.lemma());
                }
                else
                {

                    IdentifiedToken itoken = handleiToken(IdentifiedToken.VERB, sb);
                    psentence.iTokensAdd(itoken);
                    sb.delete(0, sb.length());
                }
            }

            ///// so we check for verb first
            if (isVerb(pos))
            {

                sb.append(token.lemma());
            }

            else if (isNoun(pos))
            {

                sb.append(token.lemma());
                TALog.getLogger().trace(token.lemma() + ": " + pos);
                IdentifiedToken itoken = handleiToken(IdentifiedToken.NOUN, sb);
                psentence.iTokensAdd(itoken);
                sb.delete(0, sb.length());
            }

            else if (isAdjective(pos))
            {
                sb.append(token.lemma());
                IdentifiedToken itoken = handleiToken(IdentifiedToken.ADJECTIVE, sb);
                psentence.iTokensAdd(itoken);
                sb.delete(0, sb.length());
            }
        }

    }

    private void processSentences(JobDescriptionAnalyzedFile af, Annotation document)
    {
        try
        {
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            int positionInFile = 0;

            for (CoreMap sentence : sentences)
            {
                PossibleSentence psentence = af.nextSentence(sentence);
                TALog.getLogger().trace("\n\npsentence is " + psentence + "\n\n");
                if (psentence.toString().length() < 10 || isIgnorablePhrase(psentence.toString()))
                {
                    continue;
                }
                psentence.setPosition(positionInFile);
                if (lemmatizeText)
                {
                    String s = lemmatizeSentence(psentence);
                    if (banSentenceDuplicates && DuplicateEntryChecker.getInstance().isDuplicateHashCode(s.hashCode()) )
                    {
                        continue;
                    }
                    else if(isIgnorablePhrase(s))
                    {
                        continue;
                    }
                    else
                    {
                        if (s.split(" ").length > 3)
                        {
                            tokenizeLemmatizedSentence(s, psentence);
                        }
                    }

                }
                if (collectPos)
                {
                    processPos(psentence);
                }

                if (collectAllTerms)
                {
                    processAll(psentence);
                }

                TALog.getLogger().trace("Collected the following tokens " + psentence.iTokensGetAll());

                countedTokensBySentence.put(positionInFile, new ArrayList<IdentifiedToken>(psentence.iTokensGetAll()));

                positionInFile++;

            }
            TALog.getLogger().trace("\n\nprocessSentence DONE\n\n");
        }
        catch (Exception e)
        {
            af.setSuccessfulRun(false);
            ProcessStatus.incrementStatus("TA processSentences " + e.getMessage());
            TALog.getLogger().trace(e.getMessage(), e);
        }
    }

    @Override
    public boolean processString(String sInput)
    {
        return processString(sInput, System.currentTimeMillis());
    }

    public boolean processString(String sInput, long permittedTimeMillis)
    {
        try
        {
            String s = validateString(sInput);
            TicToc tt = new TicToc();

            TALog.getLogger().trace("processString ");
            String action1 = "processString_part";
            String action2 = "compacting";
            String action3 = "annotating";
            String action4 = "processSentences";
            String action5 = "finalizing";

            List<String> fileParts = breakUpFile(s);

            TALog.getLogger().trace("fileParts: " + fileParts.size());

            for (String content : fileParts)
            {
                tt.tic(action1, permittedTimeMillis);

                tt.tic(action2, permittedTimeMillis);
                JobDescriptionAnalyzedFile af = (JobDescriptionAnalyzedFile) AnalyzedFileFactory
                        .getAnalyzedFile(AnalyzedFileFactory.JOB_DESCRIPTION, null, lock, config);
                Annotation document = new Annotation(cleanString(content));

                document.compact();
                tt.stopTimer(action2);

                tt.tic(action3, permittedTimeMillis);
                pipeline.annotate(document);
                tt.stopTimer(action3);

                tt.tic(action4, permittedTimeMillis);
                processSentences(af, document);
                tt.stopTimer(action4);

                tt.tic(action5, permittedTimeMillis);
                af.finalizeFile();
                tt.stopTimer(action5);

                countedTokens.addAll(af.getCountedTokens());
                tt.stopTimer(action1);
                if (tt.isOverTime(action1))
                {
                    TALog.getLogger().warn(tt.getOverTimeLog());
                }

                if (!af.isSuccessfulRun())
                {
                    return false;
                }

            }

            return true;

        }
        catch (Exception e)
        {

            TALog.getLogger().error(e.getMessage(), e);
            ProcessStatus.incrementStatus("TA failed before or during annotiation.");
            return false;
        }
    }

    protected abstract void processTriples(PossibleSentence psentence);

    public void setBanSentenceDuplicates(boolean banSentenceDuplicates)
    {
        this.banSentenceDuplicates = banSentenceDuplicates;
    }

    public void setCollectAllTerms(boolean collectAllTerms)
    {
        this.collectAllTerms = collectAllTerms;
    }

    public void setCollectBySentence(boolean collect)
    {
        this.collectBySentence = collect;
    }

    public void setCollectPos(boolean collectPos)
    {
        this.collectPos = collectPos;
    }

    public void setIgnoreLemmatizedPhrasesContaining(String[] ignoreLemmatizedPhrases)
    {
        this.ignoreLemmatizedPhrasesContaining = ignoreLemmatizedPhrases;
    }

    public void setIgnoreLemmatizedPhrasesMatchingIgnoreCase(String[] ignoreLemmatizedPhrasesMatchingIgnoreCase)
    {
        this.ignoreLemmatizedPhrasesMatchingIgnoreCase = ignoreLemmatizedPhrasesMatchingIgnoreCase;
    }

    public void setIgnoreLemmatizedWordMatchingIgnoreCase(String[] ignoreLemmatizedWordMatchingIgnoreCase)
    {
        this.ignoreLemmatizedWordMatchingIgnoreCase = ignoreLemmatizedWordMatchingIgnoreCase;
    }

    public void setIgnorePhrasesContaining(String[] ignorePhrases)
    {
        this.ignorePhrasesContaining = ignorePhrases;
    }

    public void setIgnorePhrasesMatchingIgnoreCase(String[] ignorePhrasesContainingMatchingIgnoreCase)
    {
        this.ignorePhrasesMatchingIgnoreCase = ignorePhrasesContainingMatchingIgnoreCase;
    }

    public void setIgnoreWordMatchingIgnoreCase(String[] ignoreWordMatchingIgnoreCase)
    {
        this.ignoreWordMatchingIgnoreCase = ignoreWordMatchingIgnoreCase;
    }

    public void setLemmatizeText(boolean lemmatizeText)
    {
        this.lemmatizeText = lemmatizeText;
    }

    protected abstract void tokenizeLemmatizedSentence(String s, PossibleSentence psentence);

}
