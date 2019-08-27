package ikoda.nlp.analyzers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
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

public class ZH_JOBTITLENerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NN = "NN";
    private final static String IGNORE1 = "行业 类别 ";
    private final static String IGNORE2 = "职位 类别";
    private final static String IGNORE3 = "描述";

    private final static String[] ignore = { IGNORE1, IGNORE2, IGNORE3 };
    private static Map<String, Integer> possibleJobs = new HashMap<String, Integer>();
    private static Map<String, Integer> possibleJobs1 = new HashMap<String, Integer>();

    ZH_JOBTITLENerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
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

    private void findCandidateJOBS(CoreMap sentence)
    {

        try
        {

            TALog.getLogger().debug(sentence.toString());
            TALog.getPTLogger().debug(sentence.toString());

            Integer count1 = possibleJobs1.get(sentence.toString());

            if (null == count1)
            {
                possibleJobs1.put(sentence.toString(), new Integer(1));
            }
            else
            {
                possibleJobs1.put(sentence.toString(), new Integer(count1.intValue() + 1));
            }

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                String ner = token.get(NamedEntityTagAnnotation.class);

                TALog.getLogger().debug(token.word() + " | " + pos + " | " + ner);
                if (pos.equals(NN))
                {

                    TALog.getLogger().debug("previousToken: " + token.word());
                    if (token.word().length() > 0)
                    {
                        Integer count = possibleJobs.get(token.word());

                        if (null == count)
                        {
                            possibleJobs.put(token.word(), new Integer(1));
                        }
                        else
                        {
                            possibleJobs.put(token.word(), new Integer(count.intValue() + 1));
                        }

                    }
                }

            }

            TALog.getPTLogger().debug("POSSIBLE JOBS:\n" + possibleJobs);
            TALog.getPTLogger().debug("POSSIBLE JOBS FULL:\n" + possibleJobs1);

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }

    }

    private boolean ignore(String s)
    {
        for (int i = 0; i < ignore.length; i++)
        {
            if (s.contains(ignore[i]))
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
            findCandidateJOBS(psentence.getSentence());

            List<IdentifiedToken> itokens = psentence.iTokensGetAll();
            if (null != itokens && itokens.size() > 0)
            {
                for (IdentifiedToken itoken : itokens)
                {

                    af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);

                }
            }
            else if (psentence.getSentence().toString().length() > 4)
            {
                if (!ignore(psentence.getSentence().toString()))
                {
                    IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.JOBCORE,
                            psentence.getSentence().toString());
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);
                }
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

}
