package ikoda.nlp.analysis;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import edu.stanford.nlp.ie.crf.CRFClassifier;
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
import ikoda.nlp.analyzers.TokenAnalyzerFactory;
import ikoda.nlp.analyzers.ZH_DEGREENerTokenAnalyzer;
import ikoda.nlp.analyzers.ZH_EXPERIENCENerTokenAnalyzer;
import ikoda.nlp.analyzers.ZH_JOBTITLENerTokenAnalyzer;
import ikoda.nlp.analyzers.ZH_LOCATIONNerTokenAnalyzer;
import ikoda.nlp.analyzers.ZH_SALARYNerTokenAnalyzer;
import ikoda.nlp.analyzers.ZH_SKILLSETNerTokenAnalyzer;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.AnalyzedFileFactory;
import ikoda.nlp.structure.ChineseJobPostAnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;
import ikoda.nlp.structure.PossibleToken;

public class ChineseJobPostAnalyzer extends AbstractTextAnalyzer
{
    private static Map<String, String> jobIdMap = new TreeMap<String, String>();
    private final static String SPACE = " ";
    protected static CRFClassifier<CoreLabel> segmenter;

    private final static String JJ = "JJ";

    private final static String NN = "NN";
    private final static String CC = "CC";
    private final static String DT = "DT";
    private final static String IN = "IN";
    private final static String VBD = "VBD";
    private final static String VBG = "VBG";
    private final static String VB = "VB";
    private final static String VBN = "VBN";
    private final static String VBP = "VBP";
    private final static String VBZ = "VBZ";
    private final static String MD = "MD";
    private final static String NNP = "NNP";
    private final static String NNPS = "NNPS";
    private final static String TITLE = "TITLE";
    private final static String PRP = "PRP";
    private final static String PRPDOLLAR = "PRP$";

    private final static String JOBSUFFIX_IST = "IST";
    private final static String JOBSUFFIX_IAN = "IAN";
    private final static String DEGREE_IN = "DEGREE IN";

    private final static String IGNORE = "IGNORESENTENCE";

    private final static String INDEEDUK = "INDEEDUK";

    private final static String INDEEDCA = "INDEEDCA";

    private final static String INDEED = "INDEED";
    private final static String BCJOBBANK = "BCJOBBANK";
    private final static String ZHAOPING = "ZHAOPING";
    private final static String SIMPLYHIRED = "SIMPLYHIRED";
    private final static String CAREERBUILDER = "CAREERBUILDER";

    private static String[] verbForms = { VB, VBN, VBP, VBD, VBZ, VBG };

    private static String[] imperativeForms = { VB, VBG };

    private static String[] jobSuffixes = { JOBSUFFIX_IST, JOBSUFFIX_IAN };

    private static String[] breakFromJobTitleSearch = { VB, VBN, VBP, VBD, VBG, VBZ, ".", ":", "?", PRP, PRPDOLLAR, MD,
            DT };

    private final static String PUNCTUATIONMARK = ",-:;()";

    private ReentrantLock lock;

    private Path file;

    public ChineseJobPostAnalyzer(StanfordCoreNLP pipeline, CRFClassifier<CoreLabel> insegmenter,
            ConfigurationBeanParent config)
    {
        super(pipeline, config);
        segmenter = insegmenter;
        TALog.getLogger().debug("ChineseJobPostAnalyzer Init " + segmenter);
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

    private boolean annotateAndRun(String intext, String region, String source)
    {
        try
        {

            Instant start = Instant.now();
            // String segmented=segment(intext);

            ChineseJobPostAnalyzedFile af = (ChineseJobPostAnalyzedFile) AnalyzedFileFactory
                    .getAnalyzedFile(AnalyzedFileFactory.CHINESE_JOB_POST, file, lock, config);
            af.setRegion(region);
            af.setSource(source);
            TALog.getLogger().debug("SEGMENT");

            // String text = "克林顿说，华盛顿将逐步落实对韩国的经济援助。. 石杰克说克林顿虚伪的很。他喜欢学法文。."
            /// + "金大中对克林顿的讲话报以掌声：克林顿总统在会谈中重申，他坚定地支持韩国摆脱经济危机。.";

            // String segmented=segment(intext);

            if (!isValidUTF8(intext.getBytes(Charset.forName("UTF-8"))))
            {
                // TALog.getLogger().debug("\n\n\nInvalid UTF8\nBailing\n\n");
                return false;
            }

            saveToFile(intext.getBytes(Charset.forName("UTF-8")), "annotateandrunUnsegmented");

            String s1 = segmenter.classifyToString(intext);
            // TALog.getLogger().debug("\n\nSEGMENTED AS :\n\n" + s1);
            if (null == s1 || s1.length() < 10)
            {
                return false;
            }
            saveToFile(s1.getBytes(Charset.forName("UTF-8")), "annotateandrunSegged");

            List<String> sentenceList = breakUpFile(s1);
            int count = 0;
            for (String s : sentenceList)
            {

                if (s.length() < 4)
                {

                    continue;

                }
                if (s.length() > 200)
                {
                    continue;
                }
                count++;
                if (af.getSource().equals(ZHAOPING))
                {
                    if (count > 100)
                    {
                        break;
                    }
                }
                Instant startInner = Instant.now();
                // TALog.getLogger().debug("\n\n\n" + count + " Sentence " + s);
                Annotation document = new Annotation(s);
                pipeline.annotate(document);

                processSentences(af, document);

                Long millis = Duration.between(startInner, Instant.now()).toMillis();
                TALog.getLogger().debug("Inner Duration in millis: " + millis + "\n\n\n");
            }

            TALog.getLogger().debug("\n\n\nFile Processing Complete\n\n\n\n\n");

            Long millis1 = Duration.between(start, Instant.now()).toMillis();
            TALog.getLogger().debug("\n\n\nTotal Duration in millis: " + millis1 + "\n\n\n");

            af.finalizeFile();
            return af.isSuccessfulRun();

        }
        catch (Exception e)
        {
            TALog.getLogger().debug(e.getMessage(), e);
            return false;
        }
    }

    private List<String> breakUpFile(String segmentedString)
    {
        String[] s1Array = segmentedString.split("。");
        // TALog.getLogger().debug(" length s1Array " + s1Array.length);
        List<String> returnList = new ArrayList<String>();

        for (int i = 0; i < s1Array.length; i++)
        {

            if (s1Array[i].length() < 3)
            {
                TALog.getLogger().warn("TOO SHORT. Skipping " + s1Array[i]);
                continue;
            }

            // TALog.getLogger().debug("ANNOTATING 1" + s1Array[i]);
            String[] s2Array = s1Array[i].split("；");
            for (int j = 0; j < s2Array.length; j++)
            {

                String[] s3Array = s2Array[j].split("，");
                for (int k = 0; k < s3Array.length; k++)
                {
                    String[] s4Array = s3Array[k].split("：");
                    for (int m = 0; m < s4Array.length; m++)
                    {

                        // TALog.getLogger().debug("ANNOTATING 2" + s2Array[j]);

                        if (s4Array[m].length() > 250)
                        {
                            TALog.getLogger().warn("TOO LONG. Skipping " + s4Array[m]);
                            continue;
                        }
                        if (s4Array[m].length() < 3)
                        {
                            TALog.getLogger().warn("TOO SHORT. Skipping " + s4Array[m]);
                            continue;
                        }
                        returnList.add(s4Array[m]);

                    }
                }
            }

            // Annotation document = new Annotation(segmented);y

        }
        return returnList;
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
        else if (text.contains(INDEED))
        {
            return StringConstantsInterface.REGION_US;
        }
        else if (text.contains(SIMPLYHIRED))
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
        else if (text.contains(ZHAOPING))
        {
            return StringConstantsInterface.REGIION_ZH;
        }
        return "Generic";
    }

    private String getSource(String text)
    {
        if (text.contains(ZHAOPING))
        {
            return ZHAOPING;
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

            itoken = new IdentifiedToken(inKey, inSb.toString());

        }

        return itoken;
    }

    private PossibleToken handlepToken(String inLemma, String sentence)
    {
        if (null == inLemma)
        {
            inLemma = "NA";
        }
        // NioLog.getLogger().debug(inSb + " is a " + inKey);
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

    private boolean isPunctuation(String posTag)
    {
        if (PUNCTUATIONMARK.contains(posTag))
        {
            return true;
        }
        return false;
    }

    private boolean isValidUTF8(byte[] input)
    {

        CharsetDecoder cs = Charset.forName("UTF-8").newDecoder();

        try
        {
            cs.decode(ByteBuffer.wrap(input));
            return true;
        }
        catch (CharacterCodingException e)
        {
            return false;
        }
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

    private void iterateAndLog(CoreMap sentence)
    {
        try
        {
            /// because really short imperatives are typically button and link
            /// titles from the website
            StringBuffer sb = new StringBuffer();
            sb.append("\n___________\n");
            sb.append("sentence: ");
            sb.append(sentence.toString());

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                sb.append("\n");
                sb.append(token.word());
                sb.append(" | ");
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                sb.append(pos);
                sb.append(" | ");
                String ner = token.get(NamedEntityTagAnnotation.class);
                sb.append(ner);
                sb.append(" | ");
                int index = token.get(IndexAnnotation.class);
                sb.append(" index: ");
                sb.append(index);

            }

            TALog.getLogger().debug(sb.toString());

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }
    }

    private void logPossibleSkills(CoreMap sentence)
    {
        try
        {
            /// because really short imperatives are typically button and link
            /// titles from the website
            StringBuffer sb = new StringBuffer();
            sb.append("\n___________\n");
            sb.append("sentence: ");
            sb.append(sentence.toString());

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                sb.append("\n");
                sb.append(token.word());
                sb.append(" | ");

                sb.append(pos);
                sb.append(" | ");
                String ner = token.get(NamedEntityTagAnnotation.class);
                sb.append(ner);
                sb.append(" | ");
                int index = token.get(IndexAnnotation.class);
                sb.append(" index: ");
                sb.append(index);

            }

            TALog.getLogger().debug(sb.toString());

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }
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

            StringBuffer sb = new StringBuffer();
            TALog.getLogger().debug("reading file");
            try
            {
                if (lock.tryLock(180, TimeUnit.SECONDS))
                {
                    try
                    {

                        Charset charset = Charset.forName("UTF-8");

                        List<String> lines = Files.readAllLines(infile, charset);

                        for (String line : lines)
                        {

                            // System.out.println(line);
                            sb.append(line);
                            sb.append("。");
                            sb.append(".");
                            sb.append("\n");

                        }
                        saveToFile(sb.toString().getBytes(Charset.forName("UTF-8")), "processFile");
                        if (!isValidUTF8(sb.toString().getBytes(Charset.forName("UTF-8"))))
                        {
                            TALog.getLogger().error("\n\nInvalid utf-8 in \n\n" + sb.toString());
                            return false;
                        }
                        // NioLog.getLogger().debug("read file");

                    }
                    catch (Exception e)
                    {
                        TALog.getLogger().error(e.getMessage(), e);
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

            String region = getRegion(sb.toString());
            String source = getSource(sb.toString());

            TALog.getLogger().debug("Region: " + region);

            return annotateAndRun(sb.toString(), region, source);

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
                String word = token.get(TextAnnotation.class);

                StringBuilder sbDebug = new StringBuilder();

                sbDebug.append("\nword: " + word);
                sbDebug.append("\noriginal currNeToken: " + token.get(NamedEntityTagAnnotation.class));
                sbDebug.append("\nlemma: " + token.lemma());
                sbDebug.append("\ncombined token: " + sbuilder.toString());
                sbDebug.append("\ncurrNeToken: " + currNeToken);

                TALog.getLogger().debug(sbDebug.toString());

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
            /// but maybe the last token was relevant
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

            TALog.getLogger().debug("processSentences");
            TALog.getLogger().debug("processSentences. Previous sentence was " + af.getPrecedingSentence());
            if (null != af.getPrecedingSentence())
            {
                TALog.getLogger().debug(
                        "processSentences. Previous sentence itokens: " + af.getPrecedingSentence().iTokensGetAll());
            }

            // TALog.getLogger().debug("processSentences. Previous sentence was
            // "+af.);

            for (CoreMap sentence : sentences)
            {

                TALog.getLogger().debug("\n\n\n\n\n\n\n\nSentence is: \n" + sentence.toString());

                PossibleSentence psentence = af.nextSentence(sentence);

                iterateAndLog(sentence);

                processNER(psentence, af);

                if (null != af.getPrecedingSentence())
                {

                    if (af.getPrecedingSentence().containsIdenitifiedTokenOfType(IdentifiedToken.MONEY))
                    {
                        logPossibleSkills(psentence.getSentence());
                        TALog.getLogger().debug("previousIToken is money: " + IdentifiedToken.MONEY);
                        ZH_SALARYNerTokenAnalyzer ta = (ZH_SALARYNerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.MONEY, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();

                    }
                    if (af.getPrecedingSentence().containsIdenitifiedTokenOfType(IdentifiedToken.EXPERIENCE))
                    {
                        logPossibleSkills(psentence.getSentence());
                        TALog.getLogger().debug("previousIToken is : " + IdentifiedToken.EXPERIENCE);
                        ZH_EXPERIENCENerTokenAnalyzer ta = (ZH_EXPERIENCENerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.EXPERIENCE, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();

                    }
                    if (af.getPrecedingSentence().containsIdenitifiedTokenOfType(IdentifiedToken.JOBLABEL))
                    {
                        logPossibleSkills(psentence.getSentence());
                        TALog.getLogger().debug("previousIToken is : " + IdentifiedToken.JOBLABEL);
                        ZH_JOBTITLENerTokenAnalyzer ta = (ZH_JOBTITLENerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.JOBLABEL, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();

                    }
                    if (af.getPrecedingSentence().containsIdenitifiedTokenOfType(IdentifiedToken.LOCATIONLABEL))
                    {
                        logPossibleSkills(psentence.getSentence());
                        TALog.getLogger().debug("previousIToken is : " + IdentifiedToken.LOCATIONLABEL);
                        ZH_LOCATIONNerTokenAnalyzer ta = (ZH_LOCATIONNerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.LOCATIONLABEL, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();

                    }
                    if (af.getPrecedingSentence().containsIdenitifiedTokenOfType(IdentifiedToken.SKILLSETLABEL))
                    {
                        logPossibleSkills(psentence.getSentence());
                        TALog.getLogger().debug("previousIToken is : " + IdentifiedToken.SKILLSETLABEL);
                        ZH_SKILLSETNerTokenAnalyzer ta = (ZH_SKILLSETNerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.SKILLSETLABEL, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();

                    }
                }

                TALog.getLogger().debug("Sentence token count: " + psentence.iTokensSize());
                if (psentence.iTokensSize() > 0)
                {
                    // TALog.getPTLogger().debug(psentence.getSentence().toString());

                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.SKILLSET)
                            || psentence.containsIdenitifiedTokenOfType(IdentifiedToken.DEGREE))
                    {

                        logPossibleSkills(psentence.getSentence());
                        ZH_DEGREENerTokenAnalyzer ta = (ZH_DEGREENerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.DEGREE, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();
                    }
                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBCORE))
                    {
                        if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBLABEL))
                        {
                            TALog.getLogger().debug("undivided job label and job" + IdentifiedToken.JOBLABEL);
                            ZH_JOBTITLENerTokenAnalyzer ta = (ZH_JOBTITLENerTokenAnalyzer) TokenAnalyzerFactory
                                    .getAnalyzer(IdentifiedToken.JOBLABEL, af, TokenAnalyzerFactory.ZH_CN);
                            ta.processSentenceWithNerToken();
                        }
                    }
                    if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.LOCATIONLABEL))
                    {

                        ZH_LOCATIONNerTokenAnalyzer ta = (ZH_LOCATIONNerTokenAnalyzer) TokenAnalyzerFactory
                                .getAnalyzer(IdentifiedToken.LOCATIONLABEL, af, TokenAnalyzerFactory.ZH_CN);
                        ta.processSentenceWithNerToken();

                    }
                }

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

    private void saveToFile(byte[] barray, String filename)

    {
        try
        {
            TALog.getLogger().debug("saving: " + Paths.get("." + File.separator + System.currentTimeMillis() + ".txt"));
            /*
             * Files.write(Paths.get( "." + File.separator + "jalogs" + File.separator +
             * filename + System.currentTimeMillis() + ".txt"), barray);
             */
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private String segment(String sin)
    {
        try
        {

            List<String> segmented = segmenter.segmentString(sin);

            // System.out.println(segmented);
            TALog.getLogger().debug(segmented);
            StringBuffer sb = new StringBuffer();
            for (String s : segmented)
            {
                TALog.getLogger().debug(s);
                sb.append(s);
                sb.append("\n");
            }
            saveToFile(sb.toString().getBytes(Charset.forName("UTF-8")), "testSegment");
            return sb.toString();

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return null;
        }
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
