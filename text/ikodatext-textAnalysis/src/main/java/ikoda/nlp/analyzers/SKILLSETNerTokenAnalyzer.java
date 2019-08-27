package ikoda.nlp.analyzers;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class SKILLSETNerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String NNP = "NNP";
    private final static String TO = "TO";
    private final static String NNPS = "NNPS";
    private final static String NNS = "NNS";
    private final static String NN = "NN";
    private final static String IN = "IN";
    private final static String VBG = "VBG";
    private final static String CC = "CC";
    private final static String JJ = "JJ";
    private final static String VB = "VB";
    private final static String LRB = "-LRB-";
    private final static String RRB = "-RRB-";
    private final static String RSB = "-RSB-";
    private final static String LSB = "-LSB-";
    private final static String LCB = "-LCB-";
    private final static String RCB = "-RCB-";
    private final static String YEAR = "YEAR";
    private final static String SPACE = " ";
    private final static String NEWLINE = "\n";

    private final static String KNOWLEDGE_AND_EXPERIENCE = "KNOWLEDGE AND EXPERIENCE";
    private final static String EXPERIENCE_AND_KNOWLEDGE = "EXPERIENCE AND KNOWLEDGE";
    private final static String EXPERIENCE_AND_SKILLS = "EXPERIENCE AND SKILLS";
    private final static String SKILLS_AND_EXPERIENCE = "SKILLS AND EXPERIENCE";
    private final static String SKILLS = "SKILLS";
    private final static String EXPERIENCE = "EXPERIENCE";
    private final static String KNOWLEDGE = "KNOWLEDGE";
    private final static String DESIRED = "DESIRED";

    private final static String ONE = "ONE";
    private final static String TWO = "TWO";
    private final static String THREE = "THREE";
    private final static String FOUR = "FOUR";
    private final static String FIVE = "FIVE";
    private final static String SIX = "SIX";
    private final static String SEVEN = "SEVEN";
    private final static String EIGHT = "EIGHT";
    private final static String NINE = "NINE";
    private final static String TEN = "TEN";
    private final static String ELEVEN = "ELEVEN";
    private final static String TWELVE = "TWELVE";
    private final static String THIRTEEN = "THIRTEEN";
    private final static String FOURTEEN = "FOURTEEN";
    private final static String FIFTEEN = "FIFTEEN";
    private final static String TWENTY = "TWENTY";

    private final static String ONE1 = "1";
    private final static String TWO2 = "2";
    private final static String THREE3 = "3";
    private final static String FOUR4 = "4";
    private final static String FIVE5 = "5";
    private final static String SIX6 = "6";
    private final static String SEVEN7 = "7";
    private final static String EIGHT8 = "8";
    private final static String NINE9 = "9";
    private final static String TEN10 = "TEN10";
    private final static String ELEVEN11 = "ELEVEN11";
    private final static String TWELVE12 = "TWELVE12";
    private final static String THIRTEEN13 = "THIRTEEN13";
    private final static String FOURTEEN14 = "FOURTEEN14";
    private final static String FIFTEEN15 = "FIFTEEN15";
    private final static String TWENTY20 = "TWENTY20";

    private final static String[] ordinals = { ONE1, TWO2, THREE3, FOUR4, FIVE5, SIX6, SEVEN7, EIGHT8, NINE9, TEN10,
            ELEVEN11, TWELVE12, THIRTEEN13, FOURTEEN14, FIFTEEN15, TWENTY20, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN,
            EIGHT, NINE, TEN, ELEVEN, TWELVE, THIRTEEN, FOURTEEN, FIFTEEN, TWENTY };

    private final static String EDUCATION = "EDUCATION";
    private final static String COMMUNICATION = "COMMUNICATION";

    private final static String[] nounPhrasePOS = { JJ, LRB, RRB, RSB, LSB, LCB, LCB, RCB, CC, NNPS, VBG, NNP, NNS, NN,
            IN, ":", ",", "-", "." };
    private final static String[] meaningfulPhrasePOS = { JJ, CC, NNPS, VBG, NNP, NNS, NN, IN };

    private final static String[] mootHeadings = { KNOWLEDGE_AND_EXPERIENCE, EXPERIENCE_AND_KNOWLEDGE,
            EXPERIENCE_AND_SKILLS, SKILLS_AND_EXPERIENCE };
    private final static String[] mootWordsInHeadings = { KNOWLEDGE, EXPERIENCE, SKILLS, DESIRED };

    SKILLSETNerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
        TALog.getLogger().debug("\n\n\nSKILLSETNerTokenAnalyzer\n\n");
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
        try
        {

            checkTokensForExperienceDuration(psentence);
            if (psentence.getPossibility() < PossibleSentence.HIGH_POSSIBILITY)
            {

                checkTokensForKnowledge(psentence);
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void checkTokensForExperienceDuration(PossibleSentence psentence)
    {

        List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.DURATION);

        TALog.getLogger().debug(psentence.toString());
        if (null == itokens || itokens.size() == 0)
        {
            return;
        }

        for (IdentifiedToken itoken : itokens)
        {
            if (itoken.getValue().toUpperCase().contains(YEAR))
            {
                TALog.getLogger().debug("itoken: " + itoken.getValue());
                if (hasOrdinal(itoken.getValue()))
                {
                    IdentifiedToken newitoken = new IdentifiedToken(IdentifiedToken.EXPERIENCE,
                            itoken.getValue() + " experience");
                    TALog.getLogger().debug("newitoken 1: " + newitoken.getValue());
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE, newitoken);
                    int yrsexp = convertToInt(newitoken.getValue());
                    IdentifiedToken newitokenInt = new IdentifiedToken(IdentifiedToken.EXPERIENCE,
                            new Integer(yrsexp).toString());
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE_AS_INT, newitokenInt);
                }
                else
                {
                    String s = findOrdinalInSentence(psentence) + itoken.getValue() + " experience";
                    if (s.contains("?"))
                    {
                        continue;
                    }
                    else
                    {
                        IdentifiedToken newitoken = new IdentifiedToken(IdentifiedToken.EXPERIENCE, s);
                        TALog.getLogger().debug("newitoken 2: " + newitoken.getValue());
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE, newitoken);
                        int yrsexp = convertToInt(s);
                        IdentifiedToken newitokenInt = new IdentifiedToken(IdentifiedToken.EXPERIENCE,
                                new Integer(yrsexp).toString());
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE_AS_INT, newitokenInt);
                    }
                }
                psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);

            }

        }

    }

    private void checkTokensForKnowledge(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();
            TALog.getLogger().debug("checkTokensForKnowledge:  " + sentence.toString());
            boolean checkNextToken = false;

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                String word = token.get(TextAnnotation.class);
                String knowledgeString = "";

                int position = token.index();
                if (currNeToken.equals(IdentifiedToken.SKILLSET))
                {
                    knowledgeString = possibleKnowledge(position, sentence);

                }
                if (currNeToken.equals(IdentifiedToken.EXPERIENCE))
                {
                    knowledgeString = possibleKnowledgeBackwards(position, sentence);

                    checkNextToken = nextTokenSignifiesSkillset(token);
                }
                if (checkNextToken)
                {
                    knowledgeString = possibleKnowledgeBackwards(position, sentence);

                    checkNextToken = false;
                }
                if (knowledgeString.length() > 1)
                {
                    IdentifiedToken newitoken = new IdentifiedToken(IdentifiedToken.SKILL, knowledgeString);
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_SKILLS, newitoken);
                    // because we don't want highly similar entries derived from same sentence
                    psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                    return;
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private boolean containsMootHeading(String sentence)
    {
        boolean contains = false;
        for (int i = 0; i < mootHeadings.length; i++)
        {
            if (sentence.toUpperCase().contains(mootHeadings[i]))
            {
                contains = true;
            }
        }
        int countMootWords = 0;
        for (int i = 0; i < mootWordsInHeadings.length; i++)
        {
            if (sentence.toUpperCase().contains(mootWordsInHeadings[i]))
            {
                countMootWords++;
            }
        }
        if (countMootWords > 2)
        {
            contains = true;
        }

        return contains;
    }

    private int convertToInt(String ins)
    {
        TALog.getLogger().debug("converting to int :" + ins.toUpperCase());
        String s = ins.toUpperCase();
        if (s.contains(ONE))
        {
            return 1;
        }
        else if (s.contains(TWO))
        {
            return 2;
        }
        else if (s.contains(THREE))
        {
            return 3;
        }
        else if (s.contains(FOUR))
        {
            return 4;
        }
        else if (s.contains(FIVE))
        {
            return 5;
        }
        else if (s.contains(SIX))
        {
            return 6;
        }
        else if (s.contains(SEVEN))
        {
            return 7;
        }
        else if (s.contains(EIGHT))
        {
            return 8;
        }
        else if (s.contains(NINE))
        {
            return 9;
        }
        else if (s.contains(TEN))
        {
            return 10;
        }
        else if (s.contains(ELEVEN))
        {
            return 11;
        }
        else if (s.contains(TWELVE))
        {
            return 12;
        }
        else if (s.contains(THIRTEEN))
        {
            return 13;
        }
        else if (s.contains(FOURTEEN))
        {
            return 14;
        }
        else if (s.contains(FIFTEEN))
        {
            return 15;
        }
        else if (s.contains(TWENTY))
        {
            return 20;
        }
        else if (s.contains(TEN10))
        {
            return 10;
        }
        else if (s.contains(ELEVEN11))
        {
            return 11;
        }
        else if (s.contains(TWELVE12))
        {
            return 12;
        }
        else if (s.contains(THIRTEEN13))
        {
            return 13;
        }
        else if (s.contains(FOURTEEN14))
        {
            return 14;
        }
        else if (s.contains(FIFTEEN15))
        {
            return 15;
        }
        else if (s.contains(TWENTY20))
        {
            return 20;
        }
        else if (s.contains(ONE1))
        {
            return 1;
        }
        else if (s.contains(TWO2))
        {
            return 2;
        }
        else if (s.contains(THREE3))
        {
            return 3;
        }
        else if (s.contains(FOUR4))
        {
            return 4;
        }
        else if (s.contains(FIVE5))
        {
            return 5;
        }
        else if (s.contains(SIX6))
        {
            return 6;
        }
        else if (s.contains(SEVEN7))
        {
            return 7;
        }
        else if (s.contains(EIGHT8))
        {
            return 8;
        }
        else if (s.contains(NINE9))
        {
            return 9;
        }

        return -1;
    }

    private String findOrdinalInSentence(PossibleSentence psentence)
    {

        TALog.getLogger().debug("findOrdinalInSentence");
        CoreMap sentence = psentence.getSentence();

        int yearIndex = -1;
        for (CoreLabel token : sentence.get(TokensAnnotation.class))
        {
            if (token.lemma().toUpperCase().equals(YEAR))
            {
                yearIndex = token.get(IndexAnnotation.class);
                break;
            }
        }

        if (yearIndex == -1)
        {
            return "? ";
        }

        String ordinal = "? ";
        int count = 0;
        for (CoreLabel token : sentence.get(TokensAnnotation.class))
        {
            // NioLog.getLogger().debug(token.word());
            if (hasOrdinal(token.word()))
            {
                // NioLog.getLogger().debug("Got "+ordinal);
                ordinal = token.word();
            }
            if (count == yearIndex)
            {
                break;
            }

        }
        return ordinal + SPACE;

    }

    private boolean hasOrdinal(String s)
    {
        for (int i = 0; i < ordinals.length; i++)
        {
            if (s.toUpperCase().contains(ordinals[i]))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isMeaningfulPhrasePOS(String pos)
    {
        boolean pass = false;
        for (int i = 0; i < meaningfulPhrasePOS.length; i++)
        {
            if (meaningfulPhrasePOS[i].equals(pos))
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

    private boolean nextTokenSignifiesSkillset(CoreLabel token)
    {
        String word = token.get(TextAnnotation.class);
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        if (pos.equals(VBG))
        {
            return true;
        }
        return false;

    }

    private String possibleKnowledge(int currentIndex, CoreMap sentence)
    {
        try
        {

            StringBuilder sb = new StringBuilder();

            int j = currentIndex;

            Iterator<CoreLabel> itr = sentence.get(TokensAnnotation.class).iterator();

            while (itr.hasNext())
            {
                CoreLabel token = itr.next();
                int index = token.get(IndexAnnotation.class);

                if (index == j)
                {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                    // NioLog.getLogger().debug("> got token : " + token.word() +" -- "+pos);
                    ///// we want to start with "experience in" NOT "in"
                    if (index == currentIndex)
                    {
                        if (pos.equals(IN) || pos.equals(TO))
                        {
                            return "";
                        }
                    }

                    sb.append(token.word());

                    sb.append(SPACE);
                    j++;
                }

            }

            sb.append(NEWLINE);
            TALog.getLogger().debug("Knowledge: " + sb.toString());
            if (wordCountFromSentence(sb.toString()) < 3)
            {
                TALog.getLogger().debug("Sentence too short. Returning");
                return "";
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "";
        }
    }

    private String possibleKnowledgeBackwards(int currentIndex, CoreMap sentence)
    {
        try
        {

            StringBuilder sb = new StringBuilder();

            outerLoop: for (int j = currentIndex; j > 0; j--)
            {

                Iterator<CoreLabel> itr = sentence.get(TokensAnnotation.class).iterator();

                while (itr.hasNext())
                {
                    CoreLabel token = itr.next();
                    int index = token.get(IndexAnnotation.class);

                    if (index == j)
                    {
                        ///// we want to start with "experience in" NOT "in"
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        // NioLog.getLogger().debug("< got token : " + token.word() +" -- "+
                        // token.lemma() +" -- "+pos);

                        if (!isNounPhrasePOS(pos))
                        {
                            break outerLoop;
                        }
                        sb.insert(0, SPACE);
                        sb.insert(0, token.word());
                    }

                }
            }

            return sb.toString();
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "";
        }
    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            PossibleSentence psentence = af.getCurrentSentence();
            TALog.getLogger().debug("\n\n\n  processSentence\n" + psentence + " \n\n\n");

            int wordCount = wordCountFromSentence(psentence.getSentence().toString());
            if (wordCount < 3)
            {
                TALog.getLogger().debug("Sentence too short. Returning");
                return;
            }
            else if (wordCount < 6)
            {

                if (containsMootHeading(psentence.getSentence().toString()))
                {
                    TALog.getLogger().debug("Contains moot heading....bailing");
                    return;
                }

            }
            checkTokens(psentence);

            /// so if didn't get anything useful, check to see if entry level
            if (psentence.getPossibility() < PossibleSentence.HIGH_POSSIBILITY)
            {
                if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.ENTRYLEVEL))
                {
                    IdentifiedToken newitokenInt = new IdentifiedToken(IdentifiedToken.EXPERIENCE,
                            new Integer(0).toString());
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE_AS_INT, newitokenInt);
                }
                TALog.getLogger().debug("\n\n\nUnparsed Sentence\n" + psentence.toString() + "\n\n\n\n\n");
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
            TALog.getLogger().debug(sentence.toString());
            // Print the triples

            if (null == triples)
            {
                return;
            }
            for (RelationTriple triple : triples)
            {

            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }

}
