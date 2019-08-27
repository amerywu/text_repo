package ikoda.nlp.analyzers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

public class DEGREENerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String BEST = "BEST";
    private final static String BASE = "BASE";
    private final static String BUSY = "BUSY";

    private final static String EDUCATION = "EDUCATION";
    private final static String SPACE = " ";

    private final static String NNS = "NNS";
    private final static String NN = "NN";
    private final static String JJ = "JJ";

    // CC Coordinating conjunction

    private final static String[] degreeNameMatchPos = { NNS, NN, JJ };
    private final static String[] misassigned = { BEST, BASE, BUSY };

    public static boolean isMisassigned(String token)
    {
        for (int i = 0; i < misassigned.length; i++)
        {
            if (token.toUpperCase().trim().equals(misassigned[i]))
            {
                return true;
            }
        }
        return false;
    }

    DEGREENerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
    }

    private void analyzeTriple(RelationTriple triple, PossibleSentence psentence)
    {
        try
        {
            /// looks for specific structure "qualification IN program"
            psentence.setType(PossibleSentence.QUALIFICATION);

            boolean gotDegree = false;

            List<String> subjectPreview = new ArrayList<String>();
            List<String> objectPreview = new ArrayList<String>();
            List<CoreLabel> objectPreviewTokens = new ArrayList<CoreLabel>();
            for (CoreLabel token : triple.subject)
            {
                subjectPreview.add(token.word());
            }
            for (CoreLabel token : triple.object)
            {
                objectPreview.add(token.word());
                objectPreviewTokens.add(token);
            }
            TALog.getLogger().debug(psentence.getSentence().toString());
            TALog.getLogger().debug(subjectPreview);
            TALog.getLogger().debug(objectPreview);

            IdentifiedToken itokenDegree = null;
            for (CoreLabel token : triple.subject)
            {

                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(IdentifiedToken.DEGREE))
                {

                    if (!gotDegree)
                    {

                        /// OK, so let's make sure we got the right token, then
                        /// add it immediately.
                        TALog.getLogger().debug(token.word() + "  is a degree or part thereof");
                        List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.DEGREE);
                        for (IdentifiedToken itoken : itokens)
                        {
                            TALog.getLogger().debug(
                                    "Matching  degree to subject triple then adding to infobox. Looking at itoken "
                                            + itoken.getValue());

                            if (itoken.getValue().toUpperCase().contains(token.word().toUpperCase()))
                            {
                                TALog.getLogger().debug("Yup, possible match");
                                itokenDegree = itoken;
                                gotDegree = true;

                            }
                        }

                    }
                }

                if (gotDegree && triple.relationLemmaGloss().toUpperCase().equals(PossibleSentence.RELATION_BE_IN))
                {

                    TALog.getLogger().debug("relation is BE IN");

                    boolean degreesAdded = false;
                    for (CoreLabel otoken : triple.object)
                    {
                        String currNeoToken = otoken.get(NamedEntityTagAnnotation.class);
                        TALog.getLogger().debug("token " + otoken.word());
                        if (currNeoToken.equals(IdentifiedToken.SKILL))
                        {
                            TALog.getLogger().debug(otoken.word() + " is a SKILL");

                            boolean degreeAdded = false;
                            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.SKILL);
                            TALog.getLogger().debug("skill token count is " + itokens.size());
                            for (IdentifiedToken itoken : itokens)
                            {
                                TALog.getLogger().debug("Matching program to object triple then adding to infobox "
                                        + itoken.getValue().toUpperCase() + " | " + otoken.word().toUpperCase());
                                if (itoken.getValue().toUpperCase().contains(otoken.word().toUpperCase()))
                                {
                                    if (matchInPreview(itoken.getValue(), objectPreview))
                                    {
                                        TALog.getLogger().debug("Got program in object: +=  " + PossibleSentence.BINGO);
                                        TALog.getLogger().debug("Confirmed: adding to infobox");
                                        psentence.incrementPossibility(PossibleSentence.BINGO);
                                        af.addToInfoBox(StringConstantsInterface.INFOBOX_AREASOFSTUDY, itoken);
                                        if (!degreeAdded)
                                        {
                                            af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION,
                                                    itokenDegree);
                                            degreeAdded = true;
                                            degreesAdded = true;
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (!degreesAdded)
                    {
                        processTripleObject(objectPreviewTokens);
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

            /*
             * Integer currentPosition = psentence.getPosition(); PossibleSentence
             * previousSentence = af.getPossibleSentenceByPosition(currentPosition - 1); if
             * (null != previousSentence) { //NioLog.getLogger().debug("previous was: " +
             * previousSentence.getSentence().toString()); if
             * (previousSentence.containsIdenitifiedToken(IdentifiedToken.SKILL) ) {
             * //NioLog.getLogger().debug("Previous sentence contains program" ); if
             * (psentence.containsIdenitifiedToken(IdentifiedToken.DEGREE)) {
             * //NioLog.getLogger().debug(
             * "Previous sentence contains incementing previous sentence " // +
             * PossibleSentence.MED_POSSIBILITY);
             * previousSentence.incrementPossibility(PossibleSentence. MED_POSSIBILITY);
             * //NioLog.getLogger().debug( "incementing current sentence " +
             * PossibleSentence.HIGH_POSSIBILITY);
             * psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY) ;
             * 
             * List<IdentifiedToken>previousSentenceSkillsList=previousSentence.
             * iTokensGet(IdentifiedToken.SKILL); long
             * count=previousSentenceSkillsList.stream().filter(prevToken ->
             * prevToken.getValue().equalsIgnoreCase("EDUCATION")).count(); if(count==0) {
             * psentence.iTokensAddAll(previousSentenceSkillsList); }
             * 
             * 
             * } }
             * 
             * }
             * 
             * CoreMap sentence = psentence.getSentence(); boolean gotDegree = false;
             * boolean gotProgram = false;
             * 
             * for (CoreLabel token : sentence.get(TokensAnnotation.class)) { String
             * currNeToken = token.get(NamedEntityTagAnnotation.class);
             * 
             * if (currNeToken.equals(IdentifiedToken.DEGREE)) { if (!gotDegree) {
             * psentence.incrementPossibility(PossibleSentence. MED_HIGH_POSSIBILITY);
             * gotDegree = true; }
             * 
             * } else if (currNeToken.equals(IdentifiedToken.SKILL)) { if (!gotProgram) {
             * gotProgram = true; psentence.incrementPossibility(PossibleSentence.
             * MED_LOW_POSSIBILITY); } }
             * 
             * if (gotProgram && gotDegree)
             * psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY) ; }
             */

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
            CoreMap sentence = psentence.getSentence();
            boolean gotDegree = false;
            boolean gotProgram = false;

            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                TALog.getLogger().debug(currNeToken);
                if (currNeToken.equals(IdentifiedToken.DEGREE))
                {
                    TALog.getLogger().debug("got degree");
                    if (!gotDegree)
                    {
                        // NioLog.getLogger().debug("got degree: " +
                        // PossibleSentence.MED_HIGH_POSSIBILITY);
                        psentence.incrementPossibility(PossibleSentence.MED_HIGH_POSSIBILITY);
                        gotDegree = true;
                    }

                }
                else if (currNeToken.equals(IdentifiedToken.SKILL))
                {
                    TALog.getLogger().debug("got program");
                    if (!gotProgram)
                    {
                        // we only get education when clearly specified in
                        // triple.
                        if (token.word().toUpperCase().equals(EDUCATION))
                        {
                            TALog.getLogger().debug("education is problematic...bailing");
                            boolean b = psentence.iTokensRemove(new IdentifiedToken(IdentifiedToken.SKILL, EDUCATION));
                            TALog.getLogger().debug(b + " =  successfully removed education itoken");
                            continue;
                        }
                        gotProgram = true;
                        TALog.getLogger().debug("got program: " + PossibleSentence.MED_LOW_POSSIBILITY);
                        psentence.incrementPossibility(PossibleSentence.MED_LOW_POSSIBILITY);
                    }
                }

                if (gotProgram && gotDegree)
                {
                    TALog.getLogger().debug("got both: " + PossibleSentence.HIGH_POSSIBILITY);

                    psentence.incrementPossibility(PossibleSentence.HIGH_POSSIBILITY);
                }
            }
            if (psentence.getPossibility() >= PossibleSentence.HIGH_POSSIBILITY)
            {
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.DEGREE))
                {
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION, itoken);
                }
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.SKILL))
                {
                    af.addToInfoBox(StringConstantsInterface.INFOBOX_AREASOFSTUDY, itoken);
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private boolean matchDegreeNameByPos(String pos)
    {
        for (int i = 0; i < degreeNameMatchPos.length; i++)
        {
            if (degreeNameMatchPos[i].equals(pos))
            {
                return true;
            }
        }
        return false;
    }

    private boolean matchInPreview(String itokenValue, List<String> preview)
    {

        String[] tv = itokenValue.split(" ");
        for (int i = 0; i < tv.length; i++)
        {
            boolean wordPass = false;
            for (String s : preview)
            {
                TALog.getLogger().debug(s.toUpperCase() + "---" + tv[i].toUpperCase());
                if (tv[i].toUpperCase().contains(s.toUpperCase()))
                {
                    TALog.getLogger().debug("match");
                    wordPass = true;
                }
            }
            if (!wordPass)
            {
                TALog.getLogger().debug("failed ");
                return false;
            }

        }
        return true;

    }

    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            // NioLog.getLogger().debug("\n\nprocessSentenceWithNerToken\n\n");
            PossibleSentence psentence = af.getCurrentSentence();
            List<IdentifiedToken> degreeTokens = psentence.iTokensGet(IdentifiedToken.DEGREE);
            for (IdentifiedToken itoken : degreeTokens)
            {
                if (isMisassigned(itoken.getValue()))
                {
                    TALog.getLogger().warn("misassigned token " + itoken.getValue());
                    return;
                }
            }

            processTriples(psentence);
            if (psentence.getPossibility() < PossibleSentence.BINGO)
            {
                psentence.resetPossibility();
                checkTokens(psentence);
            }
            else
            {
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.SKILL))
                {
                    if (itoken.getValue().toUpperCase().equals(EDUCATION))
                    {
                        continue;
                    }
                    af.addToInfoBox(ikoda.netio.config.StringConstantsInterface.INFOBOX_AREASOFSTUDY, itoken);
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void processTripleObject(List<CoreLabel> objectPhrase)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            for (CoreLabel token : objectPhrase)
            {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                TALog.getLogger().debug("before: " + token.before());
                TALog.getLogger().debug("after: " + token.after());
                TALog.getLogger().debug("pos: " + pos + "  word " + token.word());
                if (!matchDegreeNameByPos(pos))
                {
                    return;
                }

                sb.append(token.word());
                sb.append(SPACE);
            }

            IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.SKILL, sb.toString());
            af.addToInfoBox(StringConstantsInterface.INFOBOX_AREASOFSTUDY, itoken);

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
