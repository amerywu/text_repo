package ikoda.nlp.analyzers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class ZH_LOCATIONNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NN = "NN";

    private final static String IGNORE1 = "行业 类别 ";
    private final static String IGNORE2 = "职位 类别";
    private final static String IGNORE3 = "描述";
    private final static String[] ignore = { IGNORE1, IGNORE2, IGNORE3 };
    private static Map<String, Integer> possibleJobs = new HashMap<String, Integer>();

    ZH_LOCATIONNerTokenAnalyzer(AnalyzedFile af)
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
            // findCandidateJOBS(psentence.getSentence());
            List<IdentifiedToken> itokens = psentence.iTokensGetAll();
            if (null != itokens && itokens.size() > 0)
            {
                for (IdentifiedToken itoken : itokens)
                {
                    if (itoken.getType().equals(IdentifiedToken.LOCATION))
                    {
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_LOCATION, itoken);
                    }
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
