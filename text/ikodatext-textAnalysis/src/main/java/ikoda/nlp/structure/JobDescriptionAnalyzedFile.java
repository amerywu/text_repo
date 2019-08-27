package ikoda.nlp.structure;

import java.util.Iterator;

import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.nlp.analysis.TALog;

public class JobDescriptionAnalyzedFile extends AbstractAnalyzedText
{

    public JobDescriptionAnalyzedFile(ConfigurationBeanParent config)
    {

        super(config);
    }

    @Override
    public void finalizeFile()
    {
        Iterator<Integer> itr = allSentences.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n---------Finalizing--------\n");
        while (itr.hasNext())
        {
            Integer key = itr.next();
            PossibleSentence psentence = allSentences.get(key);
            TALog.getLogger().trace(psentence.iTokensGetAll());
            for (IdentifiedToken itoken : psentence.iTokensGetAll())
            {
                super.addiTokenToCounter(itoken);
                sb.append("\n");
                sb.append(itoken.getType());
                sb.append(" | ");
                sb.append(itoken.getValue());
                sb.append(" | ");
                sb.append(super.getFrequencyCountForIdentifiedTokenValueForFile(itoken.getType(), itoken.getValue()));
                sb.append("\n");
            }
        }
        sb.append("\n-----------------\n\n\n");
        TALog.getLogger().trace(sb);

        dumpStanford();
        super.setSuccessfulRun(true);
        return;
    }

}
