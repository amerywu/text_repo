package ikoda.nlp.analyzers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class JOBCORENerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String OF = "OF";
    private final static String TO = "TO";
    private final static String SPACE = " ";

    private final static String CUE_JOBTITLE = "JOB TITLE";
    private final static String CUE_TITLE = "TITLE";
    private final static String CUE_JOBDESCRIPION = "JOB DESCRIPTION";
    private final static String CUE_DESCRIPION = "DESCRIPTION";
    private final static String CUE_POSITION = "POSITION";
    private final static String CUE_ROLE = "ROLE :";

    private final static String[] nounPhrasePOS = { JJ, LRB, RRB, RSB, LSB, LCB, LCB, RCB, CC, NNPS, VBG, NNP, NNS,
            NN };
    private final static String[] jobTitlePrepositions = { OF, TO };

    private final static String[] jobTitleCues = { CUE_JOBTITLE, CUE_JOBDESCRIPION, CUE_POSITION, CUE_ROLE, CUE_TITLE,
            CUE_DESCRIPION };

    private final static String[] jobNameMatchPos = { NNS, NN, JJ, VBG };

    JOBCORENerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
    }

    private void analyzeTriple(RelationTriple triple, PossibleSentence psentence)
    {
        try
        {

            List<CoreLabel> objectPreviewTokens = new ArrayList<CoreLabel>();
            List<CoreLabel> subjectPreviewTokens = new ArrayList<CoreLabel>();
            for (CoreLabel token : triple.subject)
            {

                subjectPreviewTokens.add(token);
            }
            for (CoreLabel token : triple.object)
            {

                objectPreviewTokens.add(token);
            }
            TALog.getLogger().debug("sentence  " + psentence.getSentence().toString());

            /// double loop...first determine its a job core
            for (CoreLabel token1 : triple.subject)
            {
                /// then a noun phrase
                String currNeToken = token1.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(IdentifiedToken.JOBCORE))
                {
                    String s = super.processTripleAsStringValue(subjectPreviewTokens, jobNameMatchPos);
                    IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBTITLE, s);
                    processTripleAsToken(itoken, psentence);
                } // end if
            } // end for

            for (CoreLabel token1 : triple.object)
            {

                String currNeToken = token1.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(IdentifiedToken.JOBCORE))
                {
                    String s = super.processTripleAsStringValue(subjectPreviewTokens, jobNameMatchPos);
                    IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBTITLE, s);
                    processTripleAsToken(itoken, psentence);
                } // end if
            } // end for

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

            boolean isNNP = true;
            for (CoreLabel token : psentence.getSentence().get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // NioLog.getLogger().debug("JOBCORE pos is "+pos);
                if (!isNounPhrasePOS(pos))
                {
                    isNNP = false;
                }
            }
            if (isNNP)
            {
                IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBTITLE,
                        psentence.getSentence().toString());
                super.saveOrUpdate(IdentifiedToken.JOBTITLE, psentence.getSentence().toString());
                TALog.getLogger().debug("Noun phrase stand alone: high");
                if (af.identifiedTokenCountForType(IdentifiedToken.JOBTITLE) == 0)
                {
                    psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);

                }
                if (af.getContentType().equals(StringConstantsInterface.RESULTLIST_LABEL))
                {
                    if (af.getFrequencyCountForIdentifiedTokenValueForBlock(itoken.getType(), itoken.getValue()) > 2)
                    {

                        psentence.incrementPossibility(PossibleSentence.BINGO);
                    }
                    else
                    {
                        psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                    }
                }
                else if (af.getContentType().equals(StringConstantsInterface.JOBPOSTING_LABEL))
                {
                    if (af.getFrequencyCountForIdentifiedTokenValueForFile(itoken.getType(), itoken.getValue()) > 2)
                    {

                        psentence.incrementPossibility(PossibleSentence.BINGO);
                    }
                    else
                    {
                        psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                    }
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void checkTokensForJob(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();
            TALog.getLogger().debug("findJobTitles:  " + sentence.toString());

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                String word = token.get(TextAnnotation.class);
                int position = token.index();
                TALog.getLogger().debug(word + "  " + currNeToken);

                if (currNeToken.equals(IdentifiedToken.JOBCORE) || currNeToken.equals(IdentifiedToken.TITLE))
                {
                    String jobTitle = possibleJobTitleBackwards(position, sentence);
                    if (wordCountFromSentence(jobTitle) <= 1)
                    {
                        jobTitle = possibleJobTitleForwards(position, sentence);
                    }
                    // NioLog.getLogger().debug("examining "+jobTitle);
                    if (wordCountFromSentence(jobTitle) > 1)
                    {
                        IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBTITLE, jobTitle);
                        super.saveOrUpdate(IdentifiedToken.JOBTITLE, jobTitle);
                        TALog.getLogger().debug(" Job Title found in checkTokensForJob");
                        psentence.iTokensAdd(itoken);
                        psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                        TALog.getLogger().debug(jobTitle + " high possiblity");

                    }
                    else if (wordCountFromSentence(psentence.getSentence().toString()) < 4)
                    {
                        if (jobTitle.length() > 0)
                        {
                            IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBTITLE, jobTitle);
                            TALog.getLogger().debug(" Job Title found in checkTokensForJob");
                            psentence.iTokensAdd(itoken);

                            super.saveOrUpdate(IdentifiedToken.JOBTITLE, jobTitle);
                            psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                            TALog.getLogger().debug(jobTitle + " high possiblity");
                        }
                    }
                }

            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private boolean containsJobTitleCue(String s)
    {
        for (int i = 0; i < jobTitleCues.length; i++)
        {
            if (s.toUpperCase().contains(jobTitleCues[i]))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isJobTitlePreposition(String s)
    {
        boolean pass = false;

        for (int i = 0; i < jobTitlePrepositions.length; i++)
        {
            if (jobTitlePrepositions[i].equals(s.toUpperCase()))
            {
                pass = true;
            }
        }
        return pass;
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

    private String possibleJobTitleBackwards(int currentIndex, CoreMap sentence)
    {
        try
        {

            StringBuilder sb = new StringBuilder();

            outerLoop: for (int j = currentIndex; j > 0; j--)
            {

                Iterator<CoreLabel> itr = sentence.get(TokensAnnotation.class).iterator();

                while (itr.hasNext())
                {
                    CoreLabel token = itr.next();
                    int index = token.get(IndexAnnotation.class);

                    if (index == j)
                    {
                        ///// we want to start with "experience in" NOT "in"
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        TALog.getLogger()
                                .debug("< got token : " + token.word() + " -- " + token.lemma() + " -- " + pos);

                        if (!isNounPhrasePOS(pos))
                        {
                            break outerLoop;
                        }
                        if (containsJobTitleCue(token.word()))
                        {
                            break outerLoop;
                        }
                        sb.insert(0, SPACE);
                        sb.insert(0, token.word());
                    }

                }
            }

            TALog.getLogger().debug("Job: " + sb.toString());

            return sb.toString();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "";
        }
    }

    private String possibleJobTitleForwards(int currentIndex, CoreMap sentence)
    {
        try
        {

            StringBuilder sb = new StringBuilder();

            List<String> allPOS = new ArrayList<String>();
            outerLoop: for (int j = currentIndex; j > 0; j++)
            {

                Iterator<CoreLabel> itr = sentence.get(TokensAnnotation.class).iterator();
                TALog.getLogger().debug("currentIndex: " + currentIndex);

                while (itr.hasNext())
                {
                    CoreLabel token = itr.next();
                    int index = token.get(IndexAnnotation.class);

                    if (index == j)
                    {

                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                        TALog.getLogger().debug("< got token : " + token.word() + " -- lemma: " + token.lemma()
                                + " -- pos: " + pos + " -- index: " + index);

                        ///// e.g., assistant TO the director, Manager OF farm
                        ///// machinery
                        if (j == currentIndex + 1 && !isNounPhrasePOS(pos))
                        {
                            TALog.getLogger().debug("index is " + index);
                            if (!isJobTitlePreposition(token.word()))
                            {
                                TALog.getLogger().debug("breaking ");
                                break outerLoop;
                            }
                        }
                        else if (!isNounPhrasePOS(pos))
                        {
                            /// make sure we don't end with a preposition
                            if (allPOS.size() > 1)
                            {
                                if (!isNounPhrasePOS(allPOS.get(allPOS.size() - 1)))
                                {
                                    sb.setLength(0);
                                }
                            }
                            break outerLoop;
                        }
                        else if (containsJobTitleCue(token.word()))
                        {
                            break outerLoop;
                        }
                        sb.append(SPACE);
                        sb.append(token.word());
                        allPOS.add(pos);
                    }

                }
            }

            TALog.getLogger().debug("Job: " + sb.toString());

            return sb.toString();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {
            TALog.getLogger().debug("\n\n\n JOBNerTokenAnalyzer processSentence \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            TALog.getLogger().debug(psentence.toString());

            // checkTokensForJob almost always does the trick. If this doesn't
            // work, we try something else
            checkTokensForJob(psentence);

            if (psentence.getPossibility() < PossibleSentence.BINGO)
            {

                processTriples(psentence);

            }
            if (psentence.getPossibility() < PossibleSentence.BINGO)
            {
                psentence.resetPossibility();
                checkTokens(psentence);
            }
            if (psentence.getPossibility() >= PossibleSentence.HIGH_POSSIBILITY)
            {
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.JOBTITLE))
                {

                    af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);
                    for (int i = 0; i < jobTitleCues.length; i++)
                    {
                        if (psentence.containsPhrase(jobTitleCues[i]))
                        {
                            af.incrementFrequencyForToken(itoken);
                        }
                        if (psentence.getPosition() < 4)
                        {
                            af.incrementFrequencyForToken(itoken);
                        }
                    }
                }

            }
            if (psentence.iTokensGet(IdentifiedToken.JOBTITLE).size() == 0
                    || psentence.getPossibility() < PossibleSentence.HIGH_POSSIBILITY)
            {
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.JOBCORE))
                {
                    TALog.getLogger().debug(" JobTitle found in jobtitle itoken");
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);
                    for (int i = 0; i < jobTitleCues.length; i++)
                    {
                        if (psentence.containsPhrase(jobTitleCues[i]))
                        {
                            af.incrementFrequencyForToken(itoken);
                        }
                        if (psentence.getPosition() < 4)
                        {
                            af.incrementFrequencyForToken(itoken);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void processTripleAsToken(IdentifiedToken itoken, PossibleSentence psentence)
    {

        super.saveOrUpdate(itoken.getType(), itoken.getValue());

        if (wordCountFromSentence(itoken.getValue()) > 1)
        {
            TALog.getLogger().debug(" Job Title found in triples");
            psentence.iTokensAdd(itoken);
            psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);

        }
    }

    @Override
    protected void processTriples(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            TALog.getLogger().debug(sentence.toString());
            // Print the triples

            if (null == triples)
            {
                return;
            }
            for (RelationTriple triple : triples)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(triple.confidence + "\n" + triple.subjectLemmaGloss() + "\n" + triple.relationLemmaGloss()
                        + "\n" + triple.objectLemmaGloss());
                sb.append("\n---\n");
                TALog.getLogger().debug(sb.toString());
                analyzeTriple(triple, psentence);
                if (psentence.getPossibility() >= PossibleSentence.BINGO)
                {
                    return;
                }
                psentence.resetPossibility();
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }

}
