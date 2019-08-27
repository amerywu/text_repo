package ikoda.nlp.analyzers;

import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class DEGREEPROGRAMNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String BEST = "BEST";
    private final static String BASE = "BASE";
    private final static String BUSY = "BUSY";
    private final static String DEGREE = "DEGREE";

    DEGREEPROGRAMNerTokenAnalyzer(AnalyzedFile af)
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

    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            // NioLog.getLogger().debug("\n\nprocessSentenceWithNerToken\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> degreeTokens = psentence.iTokensGet(IdentifiedToken.DEGREEPROGRAM);
            for (IdentifiedToken itoken : degreeTokens)
            {
                if (itoken.getValue().toUpperCase().equals(BEST) || itoken.getValue().toUpperCase().equals(BASE)
                        || itoken.getValue().toUpperCase().equals(BUSY))
                {
                    TALog.getLogger().warn("misassigned token " + itoken.getValue());
                    return;
                }
                String degreeProgram = itoken.getValue();
                String program = degreeProgram.substring(0, degreeProgram.toUpperCase().indexOf(DEGREE));
                IdentifiedToken newToken = new IdentifiedToken(IdentifiedToken.SKILL, program.trim());
                IdentifiedToken newQualToken = new IdentifiedToken(IdentifiedToken.DEGREE, "Degree");
                af.addToInfoBox(StringConstantsInterface.INFOBOX_AREASOFSTUDY, newToken);
                af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION, newQualToken);
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

    }

}
