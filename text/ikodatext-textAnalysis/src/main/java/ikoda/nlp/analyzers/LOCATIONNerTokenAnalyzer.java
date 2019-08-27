package ikoda.nlp.analyzers;

import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class LOCATIONNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private static final String NNP = "NNP";
    private static final String NNPS = "NNPS";
    private static final String NNS = "NNS";
    private static final String NN = "NN";
    private static final String IN = "IN";
    private static final String VBG = "VBG";
    private static final String CC = "CC";
    private static final String JJ = "JJ";
    private static final String LRB = "-LRB-";
    private static final String RRB = "-RRB-";
    private static final String RSB = "-RSB-";
    private static final String LSB = "-LSB-";
    private static final String LCB = "-LCB-";
    private static final String RCB = "-RCB-";
    private static final String DOLLAR = "$";

    private static final String[] nounPhrasePOS = { JJ, LRB, RRB, RSB, LSB, LCB, LCB, RCB, CC, NNPS, VBG, NNP, NNS, NN,
            IN, ":", ",", "-", "." };

    LOCATIONNerTokenAnalyzer(AnalyzedFile af)
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
            TALog.getLogger().debug("\n\n\n LOCATIONNerTokenAnalyzer processSentence \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            String s = psentence.toString();
            TALog.getLogger().debug(s);
            int startindex = (s.indexOf(StringConstantsInterface.SPIDERTAGLOCATIONOPEN)
                    + StringConstantsInterface.SPIDERTAGLOCATIONOPEN.length());
            int endIndex = s.indexOf(StringConstantsInterface.SPIDERTAGLOCATIONCLOSE);

            TALog.getLogger().debug(startindex);
            TALog.getLogger().debug(endIndex);

            String location = s.substring(startindex, endIndex);

            TALog.getLogger().debug("location " + location);
            IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.LOCATION, location.trim().toUpperCase());

            af.addToInfoBox(StringConstantsInterface.INFOBOX_LOCATION, itoken);

        }
        catch (Exception e)
        {
            TALog.getLogger().warn(e.getMessage(), e);
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
