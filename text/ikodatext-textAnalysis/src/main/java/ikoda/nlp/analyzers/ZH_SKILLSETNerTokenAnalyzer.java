package ikoda.nlp.analyzers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class ZH_SKILLSETNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NN = "NN";
    private final static String CD = "CD";
    private final static String PU = "PU";

    private final static String STOP1 = "工作 地址";
    private final static String STOP2 = "任职 资格";
    private final static String STOP3 = "任职 要求";
    private final static String STOP4 = "福利 待遇";
    private final static String STOP5 = "最新 职位";
    private final static String STOP6 = "公司 介绍";
    private final static String STOP7 = "公司 名称";
    private final static String STOP8 = "最新 职位";
    private final static String STOP9 = "福利";
    private final static String STOP10 = "职位 联系";

    private final static String[] stop = { STOP1, STOP2, STOP3, STOP4, STOP5, STOP6, STOP7, STOP8, STOP9, STOP10 };

    private final static String[] ignore = { CD, PU };

    private static Map<String, Integer> candidateSkills = new HashMap<String, Integer>();
    private static Map<String, Integer> identifiedDescriptionSentences = new HashMap<String, Integer>();

    ZH_SKILLSETNerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
    }

    private void addToInfoBoxIfNeeded(PossibleSentence psentence)
    {
        try
        {
            TALog.getLogger().debug("    addToInfoBoxIfNeeded   ");
            CoreMap sentence = psentence.getSentence();
            String sentenceToSave = "";
            boolean qualifies = false;
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                String ner = token.get(NamedEntityTagAnnotation.class);
                int index = token.get(IndexAnnotation.class);

                TALog.getLogger().debug(token.word() + " | " + pos + " | " + ner);

                if (index < 5 && (!ignorePOS(pos)))
                {
                    if (ner.equals(IdentifiedToken.JOBDESCRIPTIONSENTENCE))
                    {
                        qualifies = true;
                    }
                }
                if (qualifies)
                {
                    sentenceToSave = sentenceToSave + token.word().trim();
                }
            }
            if (qualifies)
            {
                IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBDESCRIPTIONSENTENCE, sentenceToSave);
                af.addToInfoBox(StringConstantsInterface.INFOBOX_SKILLS, itoken);
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void analyzeTriple(RelationTriple triple, PossibleSentence psentence)
    {

    }

    @Override
    protected void checkPreviousSentence(PossibleSentence psentence)
    {

    }

    @Override
    protected void checkTokens(PossibleSentence psentence)
    {
        try
        {

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void findCandidateSKILLS(CoreMap sentence)
    {
        try
        {

            TALog.getLogger().debug(sentence.toString());
            TALog.getPTLogger().debug(sentence.toString());

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                String ner = token.get(NamedEntityTagAnnotation.class);
                int index = token.get(IndexAnnotation.class);

                TALog.getLogger().debug(token.word() + " | " + pos + " | " + ner);
                if (index < 5 && (!ignorePOS(pos)))
                {

                    TALog.getLogger().debug("previousToken: " + token.word());
                    if (token.word().length() > 0)
                    {
                        Integer count = candidateSkills.get(token.word());
                        if (null == count)
                        {
                            candidateSkills.put(token.word(), new Integer(1));
                        }
                        else
                        {
                            candidateSkills.put(token.word(), new Integer(count.intValue() + 1));
                        }
                        break;

                    }
                }

            }

            TALog.getPTLogger().debug("CANDIDATE SKILLS\n" + candidateSkills);

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }
    }

    private void findIdentifiedSKILLS(CoreMap sentence)
    {
        try
        {

            TALog.getLogger().debug(sentence.toString());
            TALog.getPTLogger1().debug(sentence.toString());

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                String ner = token.get(NamedEntityTagAnnotation.class);
                int index = token.get(IndexAnnotation.class);

                TALog.getLogger().debug(token.word() + " | " + pos + " | " + ner);
                if (index < 5 && (!ignorePOS(pos)))
                {

                    TALog.getLogger().debug("previousToken: " + token.word());
                    if (token.word().length() > 0)
                    {
                        Integer count = identifiedDescriptionSentences.get(token.word());
                        if (null == count)
                        {
                            identifiedDescriptionSentences.put(token.word(), new Integer(1));
                        }
                        else
                        {
                            identifiedDescriptionSentences.put(token.word(), new Integer(count.intValue() + 1));
                        }
                        break;
                    }
                }

            }

            TALog.getPTLogger1().debug(identifiedDescriptionSentences);

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }
    }

    private boolean ignorePOS(String s)
    {
        for (int i = 0; i < ignore.length; i++)
        {
            if (s.equals(ignore[i]))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            TALog.getLogger().debug("\n\nprocessSentenceWithNerToken\n\n");
            PossibleSentence psentence = af.getCurrentSentence();

            if (stopCondition(psentence.toString()))
            {
                TALog.getLogger().debug("Stop condition met");
                return;
            }
            else
            {
                IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.SKILLSETLABEL, "");
                psentence.iTokensAdd(itoken);
            }

            if (psentence.toString().length() < 3)
            {
                TALog.getLogger().debug("Sentence too short. Aborting");
                return;
            }
            findCandidateSKILLS(psentence.getSentence());

            if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.JOBDESCRIPTIONSENTENCE))
            {
                findIdentifiedSKILLS(psentence.getSentence());
                addToInfoBoxIfNeeded(psentence);
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected void processTriples(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            // NioLog.getLogger().debug(sentence.toString());
            // Print the triples

            if (null == triples)
            {
                return;
            }
            for (RelationTriple triple : triples)
            {
                StringBuffer sb = new StringBuffer();
                sb.append(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss()
                        + "\t" + triple.objectLemmaGloss());

                TALog.getLogger().debug(sb.toString());
                analyzeTriple(triple, psentence);

            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }

    private boolean stopCondition(String s)
    {
        for (int i = 0; i < stop.length; i++)
        {
            if (s.contains(stop[i]))
            {
                TALog.getPTLogger().debug("\n\nSTOPPED AT " + stop[i] + "\n");
                TALog.getPTLogger1().debug("\n\nSTOPPED AT " + stop[i] + "\n");
                return true;
            }
        }
        return false;
    }

}
