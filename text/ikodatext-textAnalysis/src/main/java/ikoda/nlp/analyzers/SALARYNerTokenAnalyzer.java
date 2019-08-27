package ikoda.nlp.analyzers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class SALARYNerTokenAnalyzer extends AbstractNERTokenAnalyzer
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
    private final static String STERLING = "£";

    private final static String PAY = "PAY";
    private final static String SALARY = "SALARY";
    private final static String COMPENSATION = "COMPENSATION";
    private final static String FULLTIME = "FULL-TIME";
    private final static String YEAR = "YEAR";
    private final static String ANNUAL = "ANNUAL";
    private final static String EARN = "EARN";

    private final static String[] nounPhrasePOS = { JJ, LRB, RRB, RSB, LSB, LCB, LCB, RCB, CC, NNPS, VBG, NNP, NNS, NN,
            IN, ":", ",", "-", "." };

    private final static String[] salaryIndicators = { PAY, SALARY, COMPENSATION, FULLTIME, YEAR, ANNUAL };

    SALARYNerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
    }

    private void addSalaryRangeToInfoBox(String salary1, String salary2)
    {
        String s1 = processPayRate(cleanMoney(salary1));
        String s2 = processPayRate(cleanMoney(salary2));

        Integer i1 = new Integer(s1);
        Integer i2 = new Integer(s2);

        TALog.getLogger().debug("adding " + s1 + " " + s2);
        if (i1.intValue() <= i2.intValue())
        {
            af.addToInfoBox(StringConstantsInterface.INFOBOX_STARTSALARY,
                    new IdentifiedToken(IdentifiedToken.MONEY, s1));
            af.addToInfoBox(StringConstantsInterface.INFOBOX_ENDSSALARY,
                    new IdentifiedToken(IdentifiedToken.MONEY, s2));
        }
        else
        {
            af.addToInfoBox(StringConstantsInterface.INFOBOX_STARTSALARY,
                    new IdentifiedToken(IdentifiedToken.MONEY, s2));
            af.addToInfoBox(StringConstantsInterface.INFOBOX_ENDSSALARY,
                    new IdentifiedToken(IdentifiedToken.MONEY, s1));
        }
    }

    private void addToInfoBox(String jobTitle, PossibleSentence psentence)
    {
        /*
         * NioLog.getLogger().debug(psentence.iTokensGet(IdentifiedToken.JOBTITLE)) ;
         * int count=af.identifiedTokenCountForType(IdentifiedToken.JOBTITLE)-
         * psentence.getITokenCountForType(IdentifiedToken.JOBTITLE);
         * NioLog.getLogger().debug("There are "+count+
         * " jobs already collected including this one "); if(count > 2) {
         * NioLog.getLogger().debug(af.getExtantIdentifiedTokensForType(
         * JobPostAnalyzedFile.INFOBOX_JOBTITLE));
         * 
         * for(IdentifiedToken itoken: psentence.iTokensGet(IdentifiedToken.JOBTITLE)) {
         * af.incrementFrequencyForToken(itoken); }
         * 
         * return; }
         */
        for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.JOBTITLE))
        {
            af.addToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, itoken);
        }
    }

    private void analyzeTriple(RelationTriple triple, PossibleSentence psentence)
    {
        try
        {
            psentence.setType(PossibleSentence.JOBTITLE);

            boolean gotJobTitle = false;

            for (CoreLabel token : triple.subject)
            {

                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(IdentifiedToken.JOBTITLE))
                {
                    if (!gotJobTitle)
                    {
                        psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                        // NioLog.getLogger().debug("Analyze Triples Got jobtitle:
                        // " +token.word());
                        gotJobTitle = true;
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

    private String cleanMoney(String toClean)
    {
        String s0 = toClean.replaceAll("k", "000");
        String s1 = s0.replaceAll("K", "000");
        String s2 = s1.replaceAll("[^0-9.]", "");
        TALog.getLogger().debug(s2);
        return s2;

    }

    private boolean isIndicatingSalary(String s)
    {
        boolean pass = false;
        for (int i = 0; i < salaryIndicators.length; i++)
        {
            if (s.toUpperCase().contains(salaryIndicators[i]))
            {
                pass = true;
            }
        }
        return pass;
    }

    private boolean isNounPhrasePOS(String pos)
    {
        boolean pass = false;
        for (int i = 0; i < nounPhrasePOS.length; i++)
        {
            if (nounPhrasePOS[i].equals(pos))
            {
                pass = true;
            }
        }
        return pass;
    }

    // TODO add hourly pay conversion
    private String processPayRate(String payRate)
    {
        try
        {
            TALog.getLogger().debug(payRate);
            Double pay = new Double(payRate);
            if (pay.intValue() > 180000)
            {
                return "-1";
            }
            if (pay.intValue() < 20000)
            {
                return "-1";
            }
            return String.valueOf(pay.intValue());
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "-1";
        }
    }

    private void processSalaryGivenInASingleItoken()
    {
        try
        {
            TALog.getLogger().debug("\n\n\n SALARYNerTokenAnalyzer processSalaryGivenInASingleItoken \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.MONEY);
            List<String> salaries = new ArrayList<String>();
            for (IdentifiedToken itoken : itokens)
            {
                TALog.getLogger().debug(itoken.getValue());
                salaries.add(processPayRate(cleanMoney(itoken.getValue())));

            }
            if (salaries.size() == 1)
            {
                int topRange = new Integer(salaries.get(0)).intValue();
                topRange += 10;

                addSalaryRangeToInfoBox(salaries.get(0), new Integer(topRange).toString());
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }

    private void processSalaryRangeGivenInASingleItoken()
    {
        try
        {
            TALog.getLogger().debug("\n\n\n SALARYNerTokenAnalyzer processSalaryRangeGivenInASingleItoken \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.MONEY);
            for (IdentifiedToken itoken : itokens)
            {
                TALog.getLogger().debug(itoken.getValue());
                String value = itoken.getValue();
                int dollarsign1 = -1;
                int dollarsign2 = -1;

                if (value.contains(DOLLAR))
                {
                    dollarsign1 = value.indexOf(DOLLAR);
                    dollarsign2 = value.lastIndexOf(DOLLAR);
                }
                else if (value.contains(STERLING))
                {
                    dollarsign1 = value.indexOf(STERLING);
                    dollarsign2 = value.lastIndexOf(STERLING);
                }

                if (dollarsign2 == dollarsign1 || dollarsign2 == -1)
                {
                    return;
                }
                String startRange = value.substring(dollarsign1, dollarsign2 - 1).trim();
                TALog.getLogger().debug("Salary start: " + startRange);
                String endRange = value.substring(dollarsign2, value.length()).trim();
                TALog.getLogger().debug("Salary end: " + endRange);
                String startRangeClean = processPayRate(cleanMoney(startRange));
                String endRangeClean = processPayRate(cleanMoney(endRange));
                af.addToInfoBox(StringConstantsInterface.INFOBOX_STARTSALARY,
                        new IdentifiedToken(IdentifiedToken.MONEY, processPayRate(startRangeClean)));
                af.addToInfoBox(StringConstantsInterface.INFOBOX_ENDSSALARY,
                        new IdentifiedToken(IdentifiedToken.MONEY, processPayRate(endRangeClean)));

            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }

    private void processSalaryRangeGivenInTwoItokens()
    {
        try
        {
            TALog.getLogger().debug("\n\n\n SALARYNerTokenAnalyzer processSalaryRangeGivenInTwoItokens \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.MONEY);
            List<String> salaries = new ArrayList<String>();
            for (IdentifiedToken itoken : itokens)
            {
                TALog.getLogger().debug(itoken.getValue());
                salaries.add(processPayRate(cleanMoney(itoken.getValue())));

            }
            if (salaries.size() == 2)
            {
                addSalaryRangeToInfoBox(salaries.get(0), salaries.get(1));
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
            TALog.getLogger().debug("\n\n\n SALARYNerTokenAnalyzer processSentence \n\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.MONEY);
            TALog.getLogger().debug("Money itoken count " + itokens.size());
            if (itokens.size() == 1)
            {
                TALog.getLogger().debug(
                        "countOccurrencesOf $" + StringUtils.countOccurrencesOf(itokens.get(0).getValue(), DOLLAR));
                if (StringUtils.countOccurrencesOf(itokens.get(0).getValue(), DOLLAR) == 2
                        || StringUtils.countOccurrencesOf(itokens.get(0).getValue(), STERLING) == 2)
                {
                    processSalaryRangeGivenInASingleItoken();
                    return;
                }
                if (StringUtils.countOccurrencesOf(itokens.get(0).getValue(), DOLLAR) == 1
                        || StringUtils.countOccurrencesOf(itokens.get(0).getValue(), STERLING) == 1)
                {
                    if (isIndicatingSalary(psentence.getSentence().toString()))
                    {
                        processSalaryGivenInASingleItoken();
                    }
                    return;
                }
            }
            else if (itokens.size() == 2)
            {
                processSalaryRangeGivenInTwoItokens();
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
