package ikoda.nlp.analyzers;

import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class JOBNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NNP = "NNP";
    private final static String NNPS = "NNPS";
    private final static String NNS = "NNS";
    private final static String NN = "NN";
    private final static String IN = "IN";
    private final static String VBG = "VBG";
    private final static String CC = "CC";
    private final static String JJ = "JJ";
    private final static String LRB = "-LRB-";
    private final static String RRB = "-RRB-";
    private final static String RSB = "-RSB-";
    private final static String LSB = "-LSB-";
    private final static String LCB = "-LCB-";
    private final static String RCB = "-RCB-";

    private final static String CUE_JOBTITLE = "JOB TITLE";
    private final static String CUE_TITLE = "TITLE";
    private final static String CUE_DESCRIPION = "JOB DESCRIPTION";
    private final static String CUE_POSITION = "POSITION :";
    private final static String CUE_ROLE = "ROLE :";

    private final static String VICE = "VICE";
    private final static String ASSISTANT = "ASSISTANT";
    private final static String ASSOCIATE = "ASSOCIATE";

    private final static String[] nounPhrasePOS = { JJ, LRB, RRB, RSB, LSB, LCB, LCB, RCB, CC, NNPS, VBG, NNP, NNS, NN,
            IN, ":", ",", "-", "." };
    private final static String[] jobTitleCues = { CUE_JOBTITLE, CUE_DESCRIPION, CUE_POSITION, CUE_ROLE };
    private final static String[] assistantJobTitles = { VICE, ASSISTANT, ASSOCIATE };

    JOBNerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
    }

    private void addAssistantPrefix(PossibleSentence psentence, IdentifiedToken itoken)
    {
        try
        {
            String sentence = psentence.toString().toUpperCase();
            String jobtitlevalue = itoken.getValue().toUpperCase().trim();
            if (-1 == sentence.indexOf(jobtitlevalue))
            {
                TALog.getLogger().debug(jobtitlevalue + " is absent from " + sentence);
                return;
            }
            for (int i = 0; i < assistantJobTitles.length; i++)
            {
                if (sentence.contains(assistantJobTitles[i]))
                {
                    if (sentence.indexOf(jobtitlevalue) > sentence.indexOf(assistantJobTitles[i]))
                    {
                        TALog.getLogger().debug("adding " + assistantJobTitles[i] + " to " + jobtitlevalue);
                        String prefix = super.capitalize(assistantJobTitles[i], af.getRegion());
                        String newTitle = prefix + SPACE + itoken.getValue();
                        itoken.setValue(newTitle);
                    }
                }
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void addToInfoBox(String jobTitle, PossibleSentence psentence)
    {
        /*
         * NioLog.getLogger().debug(psentence.iTokensGet(IdentifiedToken. JOBTITLE));
         * int count=af.identifiedTokenCountForType(IdentifiedToken.JOBTITLE)-
         * psentence.getITokenCountForType(IdentifiedToken.JOBTITLE);
         * NioLog.getLogger().debug("There are "+count+
         * " jobs already collected including this one "); if(count > 2) {
         * NioLog.getLogger().debug(af.getExtantIdentifiedTokensForType(
         * JobPostAnalyzedFile.INFOBOX_JOBTITLE));
         * 
         * for(IdentifiedToken itoken: psentence.iTokensGet(IdentifiedToken.JOBTITLE)) {
         * af.incrementFrequencyForToken(itoken); }
         * 
         * return; }
         */
        for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.JOBTITLE))
        {
            af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);
            if (psentence.getPosition() < 3)
            {
                af.incrementFrequencyForToken(itoken);
                af.incrementFrequencyForToken(itoken);
            }
            for (int i = 0; i < jobTitleCues.length; i++)
            {
                if (psentence.containsPhrase(jobTitleCues[i]))
                {
                    af.incrementFrequencyForToken(itoken);
                }
            }

        }
    }

    private void analyzeTriple(RelationTriple triple, PossibleSentence psentence)
    {
        try
        {

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected void checkPreviousSentence(PossibleSentence psentence)
    {
        try
        {

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected void checkTokens(PossibleSentence psentence)
    {
        try
        {
            // NioLog.getLogger().debug("checkTokens");
            StringBuffer sb = new StringBuffer();
            CoreMap sentence = psentence.getSentence();
            Tree tree = sentence.get(TreeAnnotation.class);

            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.JOBTITLE);
            if (null != itokens && itokens.size() == 1)
            {
                IdentifiedToken itoken = itokens.get(0);
                int itokenLength = itoken.getValue().length();
                int sentenceLength = psentence.getSentence().toString().length();
                if ((sentenceLength / itokenLength) <= 2)
                {
                    TALog.getLogger().debug("Majority of sentence: high possibility: " + itoken.getValue());
                    psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                }
            }

            for (IdentifiedToken itoken : itokens)
            {
                if (itoken.getFrequencyCount() > 2)
                {
                    psentence.incrementPossibility(PossibleSentence.BINGO);
                    TALog.getLogger().debug(" 2 repeat mention: bingo possibility: " + itoken.getValue());
                }
                else if (itoken.getFrequencyCount() > 1)
                {
                    psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                    TALog.getLogger().debug("repeat mention: high possibility: " + itoken.getValue());
                }
            }

            boolean isNNP = true;
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                // three things.
                // If first mention of job or
                // if is noun phrase
                // mentions word position or title
                String nerToken = token.get(NamedEntityTagAnnotation.class);
                if (nerToken.equals(IdentifiedToken.JOBTITLE))
                    ;
                {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    // NioLog.getLogger().debug("JOBCORE pos is "+pos);
                    if (!isNounPhrasePOS(pos))
                    {
                        isNNP = false;
                    }

                    int countOfJobTitleType = af.identifiedTokenCountForType(IdentifiedToken.JOBTITLE);
                    int countOfJobCoreType = af.identifiedTokenCountForType(IdentifiedToken.JOBCORE);
                    int countOfType = countOfJobTitleType + countOfJobCoreType;
                    if (countOfType == 1)
                    {
                        TALog.getLogger().debug("First mention: high possibility");
                        psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                    }

                    if (token.lemma().toLowerCase().equals("title") || token.lemma().toLowerCase().equals("position"))
                    {
                        psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                    }
                }
            }
            if (isNNP)
            {
                TALog.getLogger().debug("Noun phrase stand alone: high");
                psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private boolean isNounPhrasePOS(String pos)
    {
        boolean pass = false;
        for (int i = 0; i < nounPhrasePOS.length; i++)
        {
            if (nounPhrasePOS[i].equals(pos))
            {
                pass = true;
            }
        }
        return pass;
    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            PossibleSentence psentence = af.getCurrentSentence();
            TALog.getLogger()
                    .debug("\n\n\n JOBNerTokenAnalyzer processSentence \n\n\n" + psentence.getSentence().toString());
            TALog.getLogger().debug("\n\n\n JOBNerTokenAnalyzer Process Triples\n\n\n");

            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.JOBTITLE);
            for (IdentifiedToken itoken : itokens)
            {
                addAssistantPrefix(psentence, itoken);
                af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);
                if (psentence.getPosition() < 3)
                {
                    af.incrementFrequencyForToken(itoken);

                }
                for (int i = 0; i < jobTitleCues.length; i++)
                {
                    if (psentence.containsPhrase(jobTitleCues[i]))
                    {
                        af.incrementFrequencyForToken(itoken);
                    }
                }
            }

            /*
             * processTriples(psentence); if (psentence.getPossibility() <
             * PossibleSentence.BINGO) { TALog.getLogger().debug(
             * "\n\n\n JOBNerTokenAnalyzer checkTokens\n\n\n");
             * psentence.resetPossibility(); checkTokens(psentence); }
             * 
             * 
             * 
             * if(psentence.getPossibility()>=PossibleSentence.HIGH_POSSIBILITY) {
             * addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, psentence); }
             */
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected void processTriples(PossibleSentence psentence)
    {

    }

}
