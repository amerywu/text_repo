package ikoda.nlp.analysis;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analyzers.AbstractNERTokenAnalyzer;
import ikoda.nlp.analyzers.DEGREENerTokenAnalyzer;
import ikoda.nlp.analyzers.TokenAnalyzerFactory;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.AnalyzedFileFactory;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.JobPostAnalyzedFile;
import ikoda.nlp.structure.PossibleSentence;
import ikoda.nlp.structure.PossibleToken;

public class JobPostAnalyzer extends AbstractTextAnalyzer
{
    private static Map<String, String> jobIdMap = new TreeMap<String, String>();
    private final static String SPACE = " ";

    private final static String BASE = "BASE";
    private final static String BEST = "BEST";

    private final static String IGNORE = "IGNORESENTENCE";
    private final static String OFFICER = "OFFICER";

    private final static String INDEEDUK = "INDEEDUK";

    private final static String INDEEDCA = "INDEEDCA";
    private final static String INDEEDAU = "INDEEDAU";

    private final static String INDEED = "INDEED";
    private final static String BCJOBBANK = "BCJOBBANK";
    private final static String SIMPLYHIRED = "SIMPLYHIRED";
    private final static String CAREERBUILDER = "CAREERBUILDER";
    private final static String _US = "_US";
    private final static String _UK = "_UK";
    private final static String _AU = "_AU";
    private final static String _CA = "_CA";

    private final static String EXCELLENT = "EXCELLENT";
    private final static String STRONG = "STRONG";
    private final static String PROVIDE = "PROVIDE";

    private final static String PERSON = "PERSON";

    private final static String COMFORTABLE = "COMFORTABLE";
    private final static String MUST = "MUST";
    private final static String EXCEPTIONAL = "EXCEPTIONAL";
    private final static String THOROUGH = "THOROUGH";
    private final static String ENSURE = "ENSURE";
    private final static String PROFICIENCY = "PROFICIENCY";
    private final static String PREPARE = "PREPARE";
    private final static String BUILD = "BUILD";
    private final static String OVERSEE = "OVERSEE";
    private final static String EXPERIENCE = "EXPERIENCE";
    private final static String MANAGE = "MANAGE";
    private final static String DEVELOP = "DEVELOP";
    private final static String MONITOR = "MONITOR";
    private final static String DEMONSTRATE = "DEMONSTRATE";

    private final static String ENSURES = "ENSURES";
    private final static String PREPARES = "PREPARES";
    private final static String BUILDS = "BUILDS";
    private final static String OVERSEES = "OVERSEES";
    private final static String MANAGES = "MANAGES";
    private final static String DEVELOPS = "DEVELOPS";
    private final static String MONITORS = "MONITORS";
    private final static String ASSISTS = "ASSISTS";
    private final static String INTERACTS = "INTERACTS";
    private final static String ATTENDS = "ATTENDS";
    private final static String IDENTIFIES = "IDENTIFIES";
    private final static String ASSESSES = "ASSESSES";
    private final static String IMPLEMENTS = "IMPLEMENTS";
    private final static String COMMUNICATES = "COMMUNICATES";
    private final static String THEAPPLICANT = "THEAPPLICANT";
    private final static String SUCCESSFULAPPLICANT = "THE SUCCESSFUL APPLICANT";
    private final static String SUCCESSFULCANDIDATE = "THE SUCCESSFUL CANDIDATE";
    private final static String DEMONSTRATES = "DEMONSTRATES";
    private final static String THECANDIDATE = "THE CANDIDATE";

    private static String[] verbForms = { AbstractNERTokenAnalyzer.VB, AbstractNERTokenAnalyzer.VBN,
            AbstractNERTokenAnalyzer.VBP, AbstractNERTokenAnalyzer.VBD, AbstractNERTokenAnalyzer.VBZ,
            AbstractNERTokenAnalyzer.VBG };
    

    
    private static String[] keyStartWords = { EXCELLENT, STRONG, COMFORTABLE, MUST, EXCEPTIONAL, PROFICIENCY, PROVIDE,
            PREPARE, BUILD, ENSURE, OVERSEE, DEMONSTRATE, EXPERIENCE, MANAGE, MONITOR, DEVELOP, ENSURES, PREPARES,
            BUILDS, OVERSEES, MANAGES, DEVELOPS, MONITORS, DEMONSTRATES,THECANDIDATE,THOROUGH,ASSISTS,INTERACTS,ATTENDS,IDENTIFIES,
            ASSESSES,IMPLEMENTS,COMMUNICATES,THEAPPLICANT,SUCCESSFULCANDIDATE,SUCCESSFULAPPLICANT};

    private static String[] imperativeForms = { AbstractNERTokenAnalyzer.VB, AbstractNERTokenAnalyzer.VBG,
            AbstractNERTokenAnalyzer.VBZ };
    
    private static String[] adjectiveForms = { AbstractNERTokenAnalyzer.JJ, AbstractNERTokenAnalyzer.JJS,
            AbstractNERTokenAnalyzer.JJR };

    private static String[] redundantTags = { AbstractNERTokenAnalyzer.TITLE, AbstractNERTokenAnalyzer.ORGANIZATION };

    private static String[] jobSuffixes = { AbstractNERTokenAnalyzer.JOBSUFFIX_IST,
            AbstractNERTokenAnalyzer.JOBSUFFIX_IAN };

    private static String[] breakFromJobTitleSearch = { AbstractNERTokenAnalyzer.VB, AbstractNERTokenAnalyzer.VBN,
            AbstractNERTokenAnalyzer.VBP, AbstractNERTokenAnalyzer.VBD, AbstractNERTokenAnalyzer.VBG,
            AbstractNERTokenAnalyzer.VBZ, ".", ":", "?", AbstractNERTokenAnalyzer.PRP,
            AbstractNERTokenAnalyzer.PRPDOLLAR, AbstractNERTokenAnalyzer.MD, AbstractNERTokenAnalyzer.DT };

    private final static String PUNCTUATIONMARK = ",-:;()";

    private ReentrantLock lock;

    private Path file;

    public JobPostAnalyzer(StanfordCoreNLP pipeline, ConfigurationBeanParent config)
    {
        super(pipeline, config);
    }

    private void addToJobCounter(String inpossibleJob)
    {

        String possibleJobUC = inpossibleJob.toUpperCase().trim();
        // NioLog.getLogger().debug("adding " + possibleJobUC);
        Integer count = jobCounter.get(possibleJobUC);
        // NioLog.getLogger().debug("count " + count);

        if (null == count)
        {
            jobCounter.put(possibleJobUC, new Integer(1));
        }
        else
        {
            int newCount = count.intValue() + 1;
            // NioLog.getLogger().debug("incrementing count" + newCount);
            jobCounter.put(possibleJobUC, new Integer(newCount));
        }
    }

    private boolean annotateAndRun(String text, String region)
    {
        JobPostAnalyzedFile af = null;
        try
        {
            String cleanString = validateString(text);
            Instant start = Instant.now();
            af = (JobPostAnalyzedFile) AnalyzedFileFactory.getAnalyzedFile(AnalyzedFileFactory.GENERIC_JOB_POST, file,
                    lock, config);
            Annotation document = new Annotation(cleanString);
            document.compact();
            TALog.getLogger().debug("ANNOTATE");
            pipeline.annotate(document);
            Long millis = Duration.between(start, Instant.now()).toMillis();
            TALog.getLogger().debug("Annotation Duration in millis: " + millis);

            af.setRegion(region);

            processSentences(af, document);

            TALog.getLogger().debug("\n\n\nFile Processing Complete\n\n\n\n\n");

            Long millis1 = Duration.between(start, Instant.now()).toMillis();
            TALog.getLogger().debug("\n\n\nTotal Duration in millis: " + millis1 + "\n\n\n");

            af.setPipeline(pipeline);
            af.finalizeFile();
            super.setFinalStatus(af.getFinalStatus());
            TALog.getLogger().debug("Final Status " + getFinalStatus());
            return af.isSuccessfulRun();

        }
        catch (Exception e)
        {
            TALog.getLogger().debug(e.getMessage(), e);
            if (null != af)
            {
                af.clear();
            }
            return false;
        }
    }

    private void assignToInfoBox(AnalyzedFile af, IdentifiedToken itoken)
    {
        try
        {
            if (itoken.getType().equals(IdentifiedToken.DEGREE))
            {
                if (DEGREENerTokenAnalyzer.isMisassigned(itoken.getValue()))
                {
                    TALog.getLogger().warn("misassigned token " + itoken.getValue());
                    return;
                }

                if (itoken.getValue().toUpperCase().equals(AbstractNERTokenAnalyzer.DEGREE_IN))
                {
                    itoken = new IdentifiedToken(IdentifiedToken.DEGREE, "Degree");
                }
                af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION, itoken);
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void findJobTitles(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();
            // NioLog.getLogger().debug("findJobTitles: " + sentence.toString());
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {

                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                String word = token.get(TextAnnotation.class);
                if (currNeToken.equals(IdentifiedToken.JOBCORE))
                {

                    // NioLog.getLogger().debug("Job is: " + word);

                    int position = token.index();
                    // NioLog.getLogger().debug("Index is: " + position);
                    List<String> possibleJobTitles = possibleJobTitles(position, sentence);
                    for (String s : possibleJobTitles)
                    {

                        addToJobCounter(s);
                    }

                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    protected String getRegion(String text)
    {
        if (text.contains(INDEEDCA))
        {
            return StringConstantsInterface.REGION_CA;
        }
        else if (text.contains(INDEEDUK))
        {
            return StringConstantsInterface.REGION_UK;
        }
        else if (text.contains(INDEEDAU))
        {
            return StringConstantsInterface.REGION_AU;
        }
        else if (text.contains(INDEED))
        {
            return StringConstantsInterface.REGION_US;
        }

        else if (text.contains(CAREERBUILDER))
        {
            return StringConstantsInterface.REGION_US;
        }
        else if (text.contains(BCJOBBANK))
        {
            return StringConstantsInterface.REGION_CA;
        }
        else if (text.contains(_US))
        {
            return StringConstantsInterface.REGION_US;
        }
        else if (text.contains(_CA))
        {
            return StringConstantsInterface.REGION_CA;
        }
        else if (text.contains(_UK))
        {
            return StringConstantsInterface.REGION_UK;
        }
        else if (text.contains(_AU))
        {
            return StringConstantsInterface.REGION_AU;
        }
        else if (text.contains(SIMPLYHIRED))
        {
            return StringConstantsInterface.REGION_US;
        }

        return "Generic";
    }

    private IdentifiedToken handleiToken(String inKey, StringBuilder inSb, AnalyzedFile af)
    {
        TALog.getLogger().debug("\n\n\n" + inSb + " is a " + inKey + "\n");

        IdentifiedToken itoken = null;
        if (IdentifiedToken.isRelevantToken(inKey))
        {
            TALog.getLogger().debug("creating itoken");
            if (inKey.equals(IdentifiedToken.TITLE))
            {
                inKey = IdentifiedToken.JOBTITLE;
            }
            itoken = new IdentifiedToken(inKey, inSb.toString());
            assignToInfoBox(af, itoken);

        }

        return itoken;
    }

    private PossibleToken handlepToken(String inLemma, String sentence)
    {
        // NioLog.getLogger().debug(inSb + " is a " + inKey);
        if (null == inLemma)
        {
            inLemma = "NA";
        }
        return new PossibleToken(inLemma, sentence);

    }

    private boolean isBreakTag(String posTag)
    {
        // NioLog.getLogger().debug(posTag);
        for (int i = 0; i < breakFromJobTitleSearch.length; i++)
        {

            if (breakFromJobTitleSearch[i].equals(posTag))
            {
                // NioLog.getLogger().debug("TRUE");
                return true;
            }
        }
        return false;
    }

    private boolean isImperativeSentence(PossibleSentence psentence)
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
                // NioLog.getLogger().debug(pos);
                for (int i = 0; i < imperativeForms.length; i++)
                {
                    if (imperativeForms[i].equals(pos))
                    {
                        TALog.getLogger().debug("TRUE");
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
    
    private boolean isAdjectiveStarting(PossibleSentence psentence)
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
                // NioLog.getLogger().debug(pos);
                for (int i = 0; i < adjectiveForms.length; i++)
                {
                    if (adjectiveForms[i].equals(pos))
                    {
                        TALog.getLogger().debug("TRUE");
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
    
    private boolean isVerbStarting(PossibleSentence psentence)
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
                // NioLog.getLogger().debug(pos);
                for (int i = 0; i < adjectiveForms.length; i++)
                {
                    if (adjectiveForms[i].equals(pos))
                    {
                        TALog.getLogger().debug("TRUE");
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


    private boolean isKeyStartWord(PossibleSentence psentence)
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
                String word = token.word();
                // NioLog.getLogger().debug(word);
                for (int i = 0; i < keyStartWords.length; i++)
                {
                    if (keyStartWords[i].equals(word.toUpperCase()))
                    {
                        TALog.getLogger().debug("TRUE");
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

    private boolean isPunctuation(String posTag)
    {
        if (PUNCTUATIONMARK.contains(posTag))
        {
            return true;
        }
        return false;
    }

    private boolean isVerb(String posTag)
    {
        // NioLog.getLogger().debug(posTag);
        for (int i = 0; i < verbForms.length; i++)
        {

            if (verbForms[i].equals(posTag))
            {
                // NioLog.getLogger().debug("TRUE");
                return true;
            }
        }
        return false;
    }

    private List<String> possibleJobTitles(int currentIndex, CoreMap sentence)
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
                        // NioLog.getLogger().debug("< got token : " +
                        // token.word());
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        // NioLog.getLogger().debug("pos; " + pos);
                        if (isBreakTag(pos))
                        {
                            // NioLog.getLogger().debug("break..");
                            break whileloop1;
                        }
                        else
                        {

                            sb.insert(0, token.word() + SPACE);
                            possibleJobTitles.add(sb.toString());
                            TALog.getLogger().debug("got possible job: " + sb.toString());

                        }

                    }
                }

                i--;

            }
            sb = new StringBuilder();
            int j = currentIndex;

            int sentenceLength = sentence.size();

            whileloop: while (j < sentenceLength)
            {
                for (CoreLabel token : sentence.get(TokensAnnotation.class))
                {
                    int index = token.get(IndexAnnotation.class);
                    if (index == j)
                    {
                        // NioLog.getLogger().debug("> got token : " +
                        // token.word());
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        // NioLog.getLogger().debug("pos; " + pos);
                        if (isBreakTag(pos))
                        {
                            // NioLog.getLogger().debug("break..");
                            break whileloop;
                        }
                        else
                        {
                            sb.append(token.word());
                            possibleJobTitles.add(sb.toString());
                            sb.append(SPACE);
                            TALog.getLogger().debug("got possible job: " + sb.toString());

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

    @Override
    public boolean processFile(Path infile, ReentrantLock inLock)
    {
        try
        {
            file = infile;
            TALog.getLogger().debug(
                    "\n\n\n\n*************************############################********************************\n\n\n");
            TALog.getLogger().debug("\n\n\n\n" + file.getFileName().toString() + "\n\n\n");

            lock = inLock;

            String text = ".";
            TALog.getLogger().debug("reading file");
            try
            {
                if (lock.tryLock(180, TimeUnit.SECONDS))
                {
                    try
                    {

                        byte[] encoded = Files.readAllBytes(file);
                        // NioLog.getLogger().debug("read file");
                        text = new String(encoded);
                    }
                    catch(NoSuchFileException nsf)
                    {
                    	TALog.getLogger().debug(nsf.getMessage());
                        return false;
                    }
                    catch (Exception e)
                    {
                        TALog.getLogger().error(e.getMessage(), e);
                        return false;
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
                else
                {
                    TALog.getLogger().warn("Could not get lock");
                    return false;
                }
            }

            catch (Exception e)
            {
                TALog.getLogger().warn("Could not get lock", e);
                return false;
            }

            TALog.getLogger().debug(" file read");

            String region = getRegion(text);

            int codeStart = text.indexOf("jk=");
            if (codeStart > 0)
            {
                String jobId = text.substring(codeStart, codeStart + 20);
                if (null == jobIdMap.get(jobId))
                {
                    // NioLog.getLogger().debug(jobId);
                    jobIdMap.put(jobId, jobId);
                }
                else
                {
                    TALog.getLogger().debug("DUPLICATE: " + jobId);
                    return true;
                }
            }

            if (file.getFileName().toString().contains(StringConstantsInterface.JOBPOSTING_LABEL))
            {

                if (text.length() < 25000)
                {
                    return annotateAndRun(text, region);
                }
                else
                {
                    boolean pass = false;
                    String[] dividedText = text.split("\n\n\n");
                    for (int i = 0; i < dividedText.length; i++)
                    {
                        TALog.getLogger().debug("DIVIDED TEXT " + dividedText[i]);
                        if (dividedText[i].length() > 20000)
                        {
                            continue;
                        }
                        TALog.getLogger().debug("DIVIDED TEXT " + dividedText[i]);
                        boolean b = annotateAndRun(dividedText[i], region);
                        if (b)
                        {
                            if (!pass)
                            {
                                pass = true;
                            }
                        }
                    }
                    return pass;
                }
            }
            else
            {
                return annotateAndRun(text, region);

            }

        }

        catch (Exception e)
        {
            TALog.getLogger().debug(e.getMessage(), e);
            return false;
        }

    }

    private void processNER(PossibleSentence psentence, AnalyzedFile af)
    {
        try
        {

            StringBuilder sbuilder = new StringBuilder();

            // NioLog.getLogger().debug("\n\n\n\n\n\n\n\nSentence is: \n" +
            // sentence.toString());
            String prevNeToken = "X";
            String currNeToken = "X";
            CoreMap sentence = psentence.getSentence();

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                currNeToken = token.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(PERSON))
                {

                    token.setWord("____");
                    token.setLemma("___");

                }

                String word = token.get(TextAnnotation.class);

                StringBuilder sbDebug = new StringBuilder();

                sbDebug.append("\nword: " + word);
                sbDebug.append("\noriginal currNeToken: " + token.get(NamedEntityTagAnnotation.class));
                sbDebug.append("\nlemma: " + token.lemma());
                sbDebug.append("\ncombined token: " + sbuilder.toString());

                sbDebug.append("\ncurrNeToken: " + currNeToken);

                TALog.getLogger().debug(sbDebug.toString());
                currNeToken = replaceWithJobCoreTagIfNecessary(currNeToken, word, sbuilder.toString(), redundantTags);
                token.setNER(currNeToken);
                // process what we have so far
                if (!currNeToken.equals(prevNeToken))
                {

                    if (sbuilder.length() > 0)
                    {
                        IdentifiedToken itoken = handleiToken(prevNeToken, sbuilder, af);
                        sbuilder.setLength(0);
                        if (null != itoken)
                        {
                            psentence.iTokensAdd(itoken);
                        }
                    }
                }

                if (!currNeToken.equals("O"))
                {
                    if (sbuilder.length() > 0)
                    {
                        sbuilder.append(" ");
                    }
                    sbuilder.append(word);
                }

                /// maybe something we can learn about, let's log and find out
                if (PossibleToken.isPossibleTokenFromLemma(token.lemma()))
                {
                    // but make sure it is not an already identified token
                    if (!IdentifiedToken.isRelevantToken(currNeToken))
                    {
                        PossibleToken ptoken = handlepToken(token.lemma(), psentence.getSentence().toString());
                        psentence.pTokensAdd(ptoken);
                    }
                }

                //// last thing before we exit
                prevNeToken = currNeToken;

            }

        }
        catch (Exception e)
        {
            TALog.getLogger().debug(e.getMessage(), e);
        }
    }

    private void processSentences(AnalyzedFile af, Annotation document)
    {
        try
        {
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);

            int positionInFile = 0;
            TALog.getLogger().debug("processSentences");
            for (CoreMap sentence : sentences)
            {

                TALog.getLogger().debug("\n\n\n\n\n\n\n\nSentence is: \n" + sentence.toString());
                PossibleSentence psentence = af.nextSentence(sentence);
                if (psentence.getSentence().toString().contains(AnalyzedFile.NEWBLOCK))
                {
                    af.newBlock();
                }
                psentence.setPosition(positionInFile);
                // NioLog.getLogger().debug("processNER");
                if (skipAnalysis(psentence))
                {
                    continue;
                }

                processNER(psentence, af);
                TALog.getLogger().debug("psentence.iTokensSize: " + psentence.iTokensSize());
                if (psentence.iTokensSize() > 0)
                {

                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBLOCATION))
                    {
                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.JOBLOCATION, af);
                        TALog.getLogger().debug("processJOBLOCATION");
                        ta.processSentenceWithNerToken();
                    }

                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.DEGREE))
                    {
                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.DEGREE, af);
                        TALog.getLogger().debug("processDEGREE");
                        ta.processSentenceWithNerToken();
                    }
                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.MONEY))
                    {
                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.MONEY, af);
                        TALog.getLogger().debug("processSALARY");
                        ta.processSentenceWithNerToken();
                    }
                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.DEGREEPROGRAM))
                    {
                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.DEGREEPROGRAM,
                                af);
                        TALog.getLogger().debug("DEGREEPROGRAM");
                        ta.processSentenceWithNerToken();
                    }

                    if ((psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBTITLE)
                            || psentence.containsIdenitifiedTokenOfType(IdentifiedToken.TITLE))
                            && psentence.getPossibility() < PossibleSentence.HIGH_POSSIBILITY)
                    {
                        TALog.getLogger().debug("processJOBTITLE");

                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.JOBTITLE, af);
                        ta.processSentenceWithNerToken();

                    }
                    else if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBCORE))
                    {
                        TALog.getLogger().debug("processJOBCORE");

                        /// keep this line for analysis later
                        /// findJobTitles(psentence);
                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.JOBCORE, af);
                        ta.processSentenceWithNerToken();

                    }
                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.SKILLSET)
                            || psentence.containsIdenitifiedTokenOfType(IdentifiedToken.EXPERIENCE)
                            || psentence.containsIdenitifiedTokenOfType(IdentifiedToken.ENTRYLEVEL))
                    {
                        TALog.getLogger().debug("processSKILLSET");

                        AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.SKILLSET, af);
                        ta.processSentenceWithNerToken();

                    }

                }

                if (isImperativeSentence(psentence))
                {
                    TALog.getLogger().debug("save imperative ");
                    if (!psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBCORE)
                            && !psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBTITLE))
                    {
                        IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.SKILL, psentence.toString());
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_SKILLS, itoken);
                    }

                }
                if (isAdjectiveStarting(psentence))
                {
                    TALog.getLogger().debug("save adjective starting ");
                    if (!psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBCORE)
                            && !psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBTITLE))
                    {
                        IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.SKILL, psentence.toString());
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_SKILLS, itoken);
                    }

                }

                if (isKeyStartWord(psentence))
                {
                    TALog.getLogger().debug("save key start word ");
                    if (!psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBCORE)
                            && !psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBTITLE))
                    {
                        IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.SKILL, psentence.toString());
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_SKILLS, itoken);
                    }
                }
                ////// do this last because it does not preclude any previous
                ////// parsing
                if (psentence.iTokensSize() > 0
                        && (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.WORKSKILL)))
                {
                    TALog.getLogger().debug("processWORKSKILL");
                    AbstractNERTokenAnalyzer ta = TokenAnalyzerFactory.getAnalyzer(IdentifiedToken.WORKSKILL, af);
                    ta.processSentenceWithNerToken();
                }

                positionInFile++;

            }

            TALog.getLogger().debug("processSentences DONE");

        }
        catch (Exception e)
        {
            TALog.getLogger().debug(e.getMessage(), e);
        }
    }

    @Override
    public boolean processString(String s)
    {
        return false;
    }

    private boolean skipAnalysis(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(IGNORE))
                {
                    return true;
                }
                else
                {
                    // only interested in first word
                    break;
                }

            }
            return false;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return true;
        }
    }

}
