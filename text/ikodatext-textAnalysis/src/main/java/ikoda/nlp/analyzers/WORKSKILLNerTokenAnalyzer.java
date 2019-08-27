package ikoda.nlp.analyzers;

import java.util.List;

import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class WORKSKILLNerTokenAnalyzer extends AbstractNERTokenAnalyzer
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
    private final static String DOLLAR = "$";

    private final static String[] nounPhrasePOS = { JJ, LRB, RRB, RSB, LSB, LCB, LCB, RCB, CC, NNPS, VBG, NNP, NNS, NN,
            IN, ":", ",", "-", "." };

    WORKSKILLNerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
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

    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {
            TALog.getLogger().debug("\n\n\n WORKSKILLNerTokenAnalyzer processSentence \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.WORKSKILL);
            for (IdentifiedToken itoken : itokens)
            {

                af.addToInfoBox(StringConstantsInterface.INFOBOX_WORKSKILLS, itoken);

            }
            List<IdentifiedToken> itokens1 = psentence.iTokensGet(IdentifiedToken.SKILL);
            for (IdentifiedToken itoken1 : itokens1)
            {

                af.addToInfoBox(StringConstantsInterface.INFOBOX_WORKSKILLS, itoken1);

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

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }

}
