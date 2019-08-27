package ikoda.nlp.analyzers;

import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;

public class TokenAnalyzerFactory
{

    public final static String EN = "en";
    public final static String ZH_CN = "zh_CN";

    public static AbstractNERTokenAnalyzer getAnalyzer(String nerType, AnalyzedFile af)
    {
        if (nerType.equals(IdentifiedToken.DEGREE))
        {
            return new DEGREENerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.LDEGREEEDU))
        {
            return new DIPLOMANerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.JOBTITLE))
        {
            return new JOBNerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.JOBCORE))
        {
            return new JOBCORENerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.SKILLSET))
        {
            return new SKILLSETNerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.DEGREEPROGRAM))
        {
            return new DEGREEPROGRAMNerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.MONEY))
        {
            return new SALARYNerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.WORKSKILL))
        {
            return new WORKSKILLNerTokenAnalyzer(af);
        }
        else if (nerType.equals(IdentifiedToken.JOBLOCATION))
        {
            return new LOCATIONNerTokenAnalyzer(af);
        }
        return null;
    }

    public static AbstractNERTokenAnalyzer getAnalyzer(String nerType, AnalyzedFile af, String language)
    {
        if (language.equals(ZH_CN))
        {
            if (nerType.equals(IdentifiedToken.DEGREE))
            {
                return new ZH_DEGREENerTokenAnalyzer(af);
            }
            if (nerType.equals(IdentifiedToken.MONEY))
            {
                return new ZH_SALARYNerTokenAnalyzer(af);
            }
            if (nerType.equals(IdentifiedToken.EXPERIENCE))
            {
                return new ZH_EXPERIENCENerTokenAnalyzer(af);
            }
            if (nerType.equals(IdentifiedToken.JOBLABEL))
            {
                return new ZH_JOBTITLENerTokenAnalyzer(af);
            }
            if (nerType.equals(IdentifiedToken.LOCATIONLABEL) || nerType.equals(IdentifiedToken.LOCATION))
            {
                return new ZH_LOCATIONNerTokenAnalyzer(af);
            }
            if (nerType.equals(IdentifiedToken.SKILLSET) || nerType.equals(IdentifiedToken.SKILLSETLABEL))
            {
                return new ZH_SKILLSETNerTokenAnalyzer(af);
            }
        }
        return getAnalyzer(nerType, af);

    }

    public TokenAnalyzerFactory()
    {
        // TODO Auto-generated constructor stub
    }

}
