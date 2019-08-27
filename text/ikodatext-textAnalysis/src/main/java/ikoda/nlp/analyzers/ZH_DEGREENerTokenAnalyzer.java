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

public class ZH_DEGREENerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NN = "NN";

    private static Map<String, Integer> possibleSkills = new HashMap<String, Integer>();

    ZH_DEGREENerTokenAnalyzer(AnalyzedFile af)
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

    private void findCandidateSkills(CoreMap sentence)
    {

        try
        {
            /// because really short imperatives are typically button and link
            /// titles from the website
            String previousToken = "";

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                String ner = token.get(NamedEntityTagAnnotation.class);

                TALog.getLogger().debug(token.word() + " | " + pos + " | " + ner);
                if (ner.equals(IdentifiedToken.SKILLSET))
                {
                    TALog.getLogger().debug("previousToken: " + previousToken);
                    if (previousToken.length() > 0)
                    {
                        Integer count = possibleSkills.get(previousToken);
                        if (null == count)
                        {
                            possibleSkills.put(previousToken, new Integer(1));
                        }
                        else
                        {
                            possibleSkills.put(previousToken, new Integer(count.intValue() + 1));
                        }
                    }
                }

                if (pos.equals(NN))
                {
                    previousToken = token.word();

                }
                else
                {
                    previousToken = "";
                }

            }

            TALog.getPTLogger().debug("CANDIDATE SKILLS\n" + possibleSkills);

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
            findCandidateSkills(psentence.getSentence());
            if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.SKILLSET))
            {
                TALog.getLogger().debug("processSentenceWithNerToken: contains" + IdentifiedToken.SKILLSET);
                if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.SKILL))
                {
                    TALog.getLogger().debug("processSentenceWithNerToken: contains" + IdentifiedToken.SKILL);

                    for (IdentifiedToken itoken : psentence.iTokensGetAll())
                    {
                        if (itoken.getType().equals(IdentifiedToken.SKILL))
                        {
                            af.addToInfoBox(StringConstantsInterface.INFOBOX_AREASOFSTUDY, itoken);
                        }
                    }
                }
            }
            if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.DEGREE))
            {
                for (IdentifiedToken itoken : psentence.iTokensGetAll())
                {
                    if (itoken.getType().equals(IdentifiedToken.DEGREE))
                    {
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION, itoken);
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
