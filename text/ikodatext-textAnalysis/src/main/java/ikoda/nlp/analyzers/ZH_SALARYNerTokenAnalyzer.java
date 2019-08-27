package ikoda.nlp.analyzers;

import java.util.Collection;
import java.util.HashMap;
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

public class ZH_SALARYNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NN = "NN";
    private final static String CD = "CD";
    private final static String MISC = "MISC";

    private static Map<String, Integer> possibleSkills = new HashMap<String, Integer>();

    ZH_SALARYNerTokenAnalyzer(AnalyzedFile af)
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

    private void findSalary(CoreMap sentence)
    {

        try
        {
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                String ner = token.get(NamedEntityTagAnnotation.class);

                TALog.getLogger().debug(token.word() + " | " + pos + " | " + ner);
                if (ner.equals(MISC) && pos.equals(CD))
                {
                    if (isSalaryRange(token.word()))
                    {
                        TALog.getLogger().debug(token.word() + " is a salary range");
                        processSalaryRange(token.word());
                    }
                }

            }

            // TALog.getPTLogger().debug(possibleSkills);

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }

    }

    private boolean isSalary(String s)
    {

        if (s.matches("[0-9]+"))
        {
            TALog.getLogger().debug("|" + s + "| returning true");
            return true;
        }
        TALog.getLogger().debug("|" + s + "| returning false");
        return false;
    }

    private boolean isSalaryRange(String s)
    {

        if (s.matches("[0-9]+-[0-9]+"))
        {
            TALog.getLogger().debug("|" + s + "| returning true");
            return true;
        }
        TALog.getLogger().debug("|" + s + "| returning false");
        return false;
    }

    private void processSalaryRange(String s)
    {
        try
        {
            if (!s.contains("-"))
            {
                TALog.getLogger().warn("Invalid String:   " + s);
                return;
            }

            String bottomRange = s.substring(0, (s.indexOf("-")));
            TALog.getLogger().debug("bottomRange " + bottomRange);

            Integer low = new Integer(bottomRange);

            if (low > 1000 && low < 30000)
            {
                IdentifiedToken itokenLow = new IdentifiedToken(IdentifiedToken.MONEY, low.toString());
                af.addToInfoBox(StringConstantsInterface.INFOBOX_STARTSALARY, itokenLow);
            }

            String topRange = s.substring((s.indexOf("-") + 1), s.length());
            TALog.getLogger().debug("topRange " + topRange);

            Integer high = new Integer(topRange);

            if (high > 1000 && high < 40000)
            {
                if (high > low)
                {
                    IdentifiedToken itokenHigh = new IdentifiedToken(IdentifiedToken.MONEY, high.toString());
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_ENDSSALARY, itokenHigh);
                }
            }
            else
            {
                Integer newHigh = new Integer((low.intValue() + 10));
                IdentifiedToken itokenHigh = new IdentifiedToken(IdentifiedToken.MONEY, newHigh.toString());
                af.addToInfoBox(StringConstantsInterface.INFOBOX_ENDSSALARY, itokenHigh);
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            TALog.getLogger().debug("\n\nprocessSentenceWithNerToken\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            findSalary(psentence.getSentence());

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
