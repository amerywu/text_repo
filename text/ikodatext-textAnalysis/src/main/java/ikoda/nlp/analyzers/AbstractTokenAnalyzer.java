package ikoda.nlp.analyzers;

import java.util.StringTokenizer;

import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AbstractAnalyzedText;

public class AbstractTokenAnalyzer
{
    protected AbstractAnalyzedText aat;

    public AbstractTokenAnalyzer(AbstractAnalyzedText inaf)
    {
        aat = inaf;
    }

    protected String capitalize(String line, String region)
    {
        try
        {
            TALog.getLogger().debug(line + " | " + region);
            if (region.toUpperCase().contains("ZH"))
            {
                return line;
            }

            StringTokenizer token = new StringTokenizer(line);
            String capLine = "";
            while (token.hasMoreTokens())
            {
                String tok = token.nextToken().toString();
                capLine += Character.toUpperCase(tok.charAt(0)) + tok.substring(1).toLowerCase();
                capLine += " ";
            }
            TALog.getLogger().debug(line);
            return capLine.trim();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return line;
        }
    }
}
