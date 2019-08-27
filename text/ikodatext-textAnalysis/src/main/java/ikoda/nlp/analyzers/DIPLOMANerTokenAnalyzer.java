package ikoda.nlp.analyzers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.structure.AnalyzedFile;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class DIPLOMANerTokenAnalyzer extends AbstractNERTokenAnalyzer
{

    private final static String BEST = "BEST";
    private final static String BASE = "BASE";
    private final static String BUSY = "BUSY";

    private final static String EDUCATION = "EDUCATION";
    private final static String LICENSE = "LICENSE";
    private final static String DIPLOMA = "DIPLOMA";
    private final static String CERTIFICATE = "CERTIFICATE";
    private final static String QUALIFICATION = "QUALIFICATION";
    private final static String APPRENTICESHIP = "APPRENTICESHIP";
    private final static String AASDEGREE = "AAS DEGREE";
    private final static String ACCREDITATION = "ACCREDITATION";
    private final static String ASSOCIATE = "ASSOCIATE";
    private final static String CERTIFICATION = "CERTIFICATION";
    private final static String LICENSURE = "LICENSURE";

    private final static String CURRENT = "CURRENT";
    private final static String PREFER = "PREFER";
    private final static String VALID = "VALID";

    private final static String CERTIFICATIONS = "CERTIFICATIONS";

    private final static String HIGHSCHOOL = "HIGH SCHOOL";
    private final static String SCHOOLDIPLOMA = "SCHOOL DIPLOMA";
    private final static String MARITALSTATUS = "MARITAL STATUS";
    private final static String HS = "HS ";
    private final static String GED = "GED ";
    private final static String VETERAN = "VETERAN";
    private final static String FLSA = " FLSA ";
    private final static String DRIVER = "DRIVER";
    private final static String BIRTHCERTIFICATE = "BIRTH CERTIFICATE";
    private final static String SUSPENDED = "SUSPENDED";
    private final static String APPLICATIONSTATUS = "APPLICATION STATUS";
    private final static String DISABILITYSTATUS = "DISABILITY STATUS";
    private final static String JOBSTATUS = "JOB STATUS";

    private final static String REQUIRE = "REQUIRE";
    private final static String EQUIVALENT = "EQUIVALENT";

    // CC Coordinating conjunction

    private final static String[] degreeNameMatchPos = { NNS, NN, JJ, VBG };
    private final static String[] degreeNameMisMatchPos = { CC, VBD, VB, VBP, VBZ, MD, PRPDOLLAR, PRP, LRB, RRB,
            SEMICOLON, COLON, COMMA, OR_OPERATOR, PERIOD };
    private final static String[] misassigned = { BEST, BASE, BUSY };
    private final static String[] ignoreIfMoreThanOneArray = { LICENSE, DIPLOMA, CERTIFICATE, EDUCATION, CERTIFICATION,
            LICENSURE, ASSOCIATE, ACCREDITATION, CERTIFICATIONS };
    private final static String[] ignoreIfIncludesArray = { HIGHSCHOOL, HS, GED, VETERAN, FLSA, DRIVER,
            BIRTHCERTIFICATE, MARITALSTATUS, APPLICATIONSTATUS, DISABILITYSTATUS, JOBSTATUS, SUSPENDED, VALID, REQUIRE,
            EQUIVALENT, CURRENT, PREFER, SCHOOLDIPLOMA, };
    private final static String[] ignoreQualificationIfIncludesAllArray = { QUALIFICATION, };

    private final static String[] findPhraseStopLemma = { VALID, CURRENT, PREFER };

    private final static String[] diplomaCues = { LICENSE, DIPLOMA, CERTIFICATE, AASDEGREE, ACCREDITATION, ASSOCIATE,
            CERTIFICATION, LICENSURE, CERTIFICATIONS, APPRENTICESHIP };

    public static boolean ignoreIfIncludes(String s)
    {

        for (int i = 0; i < ignoreIfIncludesArray.length; i++)
        {
            if (s.toUpperCase().trim().contains(ignoreIfIncludesArray[i]))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean ignoreIfIncludesQualification(String s)
    {
        for (int i = 0; i < ignoreQualificationIfIncludesAllArray.length; i++)
        {
            if (s.toUpperCase().trim().contains(ignoreQualificationIfIncludesAllArray[i]))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean ignoreIfMoreThanOne(String token)
    {

        int count = 0;
        for (int i = 0; i < ignoreIfMoreThanOneArray.length; i++)
        {
            if (token.toUpperCase().trim().contains(ignoreIfMoreThanOneArray[i]))
            {
                count++;
            }
            if (count > 1)
            {
                return true;
            }
        }
        return false;
    }

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

    DIPLOMANerTokenAnalyzer(AnalyzedFile af)
    {
        super(af);
    }

    private void analyzeTriple(RelationTriple triple, PossibleSentence psentence)
    {
        try
        {
            /// looks for specific structure "qualification IN program"
            psentence.setType(PossibleSentence.CERTIFICATION);

            boolean gotBeIn = false;

            boolean gotDegreeInSubject = false;
            boolean gotJobTitleInSubject = false;
            String jobTitleInSubject = "xxx";

            boolean gotDegreeInObject = false;
            boolean gotJobTitleInObject = false;
            String jobTitleInObject = "xxx";

            List<String> subjectPreview = new ArrayList<String>();
            List<String> objectPreview = new ArrayList<String>();
            List<CoreLabel> objectPreviewTokens = new ArrayList<CoreLabel>();
            List<CoreLabel> subjectPreviewTokens = new ArrayList<CoreLabel>();
            for (CoreLabel token : triple.subject)
            {
                subjectPreview.add(token.word());
                subjectPreviewTokens.add(token);
            }
            for (CoreLabel token : triple.object)
            {
                objectPreview.add(token.word());
                objectPreviewTokens.add(token);
            }
            TALog.getLogger().debug("sentence  " + psentence.getSentence().toString());
            TALog.getLogger().debug("subject  " + subjectPreview);
            TALog.getLogger().debug("object  " + objectPreview);

            for (CoreLabel token : triple.subject)
            {

                String currNeToken = token.get(NamedEntityTagAnnotation.class);
                if (currNeToken.equals(IdentifiedToken.LDEGREEEDU))
                {
                    gotDegreeInSubject = true;
                    String s = processTripleAsStringValue(subjectPreviewTokens, degreeNameMatchPos);
                    IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.CERTIFICATION, s);
                    processTripleAsToken(itoken, StringConstantsInterface.INFOBOX_CERTIFICATION);

                }
                if (currNeToken.equals(IdentifiedToken.JOBTITLE))
                {
                    gotJobTitleInSubject = true;
                    jobTitleInSubject = token.word();
                }
            }

            if (gotDegreeInSubject && gotJobTitleInSubject)
            {
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.JOBTITLE))
                {
                    if (itoken.getValue().toUpperCase().contains(jobTitleInSubject.toUpperCase()))
                    {
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_CERTIFICATION, itoken);
                    }
                }
            }

            if (gotDegreeInSubject && triple.relationLemmaGloss().toUpperCase().equals(PossibleSentence.RELATION_BE_IN))
            {
                TALog.getLogger().debug("relation is BE IN");
                gotBeIn = true;
                String s = processTripleAsStringValue(objectPreviewTokens, degreeNameMatchPos);
                IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.CERTIFICATION, s);
                processTripleAsToken(itoken, StringConstantsInterface.INFOBOX_CERTIFICATION);
                processTripleObjectForSkills(psentence);
            }

            for (CoreLabel otoken : triple.object)
            {
                String currNeoToken = otoken.get(NamedEntityTagAnnotation.class);
                if (currNeoToken.equals(IdentifiedToken.LDEGREEEDU))
                {
                    gotDegreeInObject = true;
                    String s = processTripleAsStringValue(objectPreviewTokens, degreeNameMatchPos);
                    IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.CERTIFICATION, s);
                    processTripleAsToken(itoken, StringConstantsInterface.INFOBOX_CERTIFICATION);
                }
                if (currNeoToken.equals(IdentifiedToken.JOBTITLE))
                {
                    gotJobTitleInObject = true;
                    jobTitleInObject = otoken.word();

                }

            }
            if (gotDegreeInObject && gotJobTitleInObject)
            {
                for (IdentifiedToken itoken : psentence.iTokensGet(IdentifiedToken.JOBTITLE))
                {
                    if (itoken.getValue().toUpperCase().contains(jobTitleInObject.toUpperCase()))
                    {
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_CERTIFICATION, itoken);
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
        try
        {

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private String containsQualification(String phrase, String region)
    {

        for (int i = 0; i < diplomaCues.length; i++)
        {
            if (phrase.toUpperCase().trim().contains(diplomaCues[i]))
            {
                String s = diplomaCues[i];
                return capitalize(s, region);
            }
        }
        return null;
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

    /**
     * We use this class to identify both the qualification, i.e., certificate,
     * diploma, and the area of study, i.e., cpa
     **/
    @Override
    public void processSentenceWithNerToken()
    {
        try
        {

            PossibleSentence psentence = af.getCurrentSentence();
            TALog.getLogger().debug("\n\nprocessSentenceWithNerToken\n\n" + psentence.getSentence().toString());
            List<IdentifiedToken> degreeTokens = psentence.iTokensGet(IdentifiedToken.LDEGREEEDU);

            /// if the certification is subsumed in the area of study, we need
            /// to extract

            if (degreeTokens.size() == 0)
            {
                String diploma = containsQualification(psentence.toString(), af.getRegion());
                if (null == diploma)
                {
                    TALog.getLogger().warn("No Qualification in " + psentence.getSentence().toString());
                    return;
                }
                if (!ignoreIfMoreThanOne(psentence.toString()))
                {
                    if (!ignoreIfIncludes(psentence.toString()))
                    {
                        if (!ignoreIfIncludesQualification(diploma))
                        {
                            TALog.getLogger().debug("Qualification found in area of study");
                            IdentifiedToken itoken = new IdentifiedToken(IdentifiedToken.LDEGREEEDU, diploma);

                            af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION, itoken);
                        }
                    }
                }
            }
            /// add qualification if exists
            else if (!psentence.getSentence().toString().contains(StringConstantsInterface.GRAB_BLOCK_START))
            {
                for (IdentifiedToken itoken : degreeTokens)
                {
                    if (isMisassigned(itoken.getValue()))
                    {
                        TALog.getLogger().warn("misassigned token " + itoken.getValue());
                        return;
                    }

                    if (!ignoreIfMoreThanOne(itoken.getValue()))
                    {
                        if (!ignoreIfIncludes(itoken.getValue()))
                        {
                            if (!ignoreIfIncludesQualification(itoken.getValue()))
                            {
                                TALog.getLogger().debug("Qualification found in pre-IdentifiedToken");
                                af.addToInfoBox(StringConstantsInterface.INFOBOX_QUALIFICATION, itoken);
                            }
                        }
                    }
                }
            }

            /// save identified areas of study
            if (psentence.containsIdenitifiedTokenOfType(IdentifiedToken.CERTIFICATION))
            {
                List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.CERTIFICATION);
                for (IdentifiedToken itoken : itokens)
                {
                    TALog.getLogger().debug("Ceriticate type found in pre-IdentifiedToken");
                    if (!ignoreIfMoreThanOne(itoken.getValue()))
                    {
                        af.addToInfoBox(StringConstantsInterface.INFOBOX_CERTIFICATION, itoken);
                        psentence.incrementPossibility(PossibleSentence.BINGO);
                    }
                }
            }

            /// find areas of study
            if (!psentence.getSentence().toString().contains(StringConstantsInterface.GRAB_BLOCK_START))
            {
                List<IdentifiedToken> itokens = super.findPhrases(psentence, degreeNameMisMatchPos, findPhraseStopLemma,
                        IdentifiedToken.LDEGREEEDU, IdentifiedToken.CERTIFICATION);
                if (psentence.getPossibility() < PossibleSentence.BINGO)
                {
                    for (IdentifiedToken itoken : itokens)
                    {
                        if (wordCountFromSentence(itoken.getValue()) > 1)
                        {
                            if (ignoreIfMoreThanOne(itoken.getValue()) || ignoreIfIncludes(itoken.getValue()))
                            {
                                continue;
                            }
                            else
                            {
                                psentence.incrementPossibility(PossibleSentence.MED_HIGH_POSSIBILITY);
                                TALog.getLogger().debug("Ceriticate type found by text analysis");
                                af.addToInfoBox(StringConstantsInterface.INFOBOX_CERTIFICATION, itoken);
                            }
                        }
                    }
                    processTriples(psentence);
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void processTripleAsToken(IdentifiedToken itoken, String infoBoxCategory)
    {

        super.saveOrUpdate(itoken.getType(), itoken.getValue());

        if (wordCountFromSentence(itoken.getValue()) > 1)
        {
            if (!ignoreIfMoreThanOne(itoken.getValue()) && !ignoreIfIncludes(itoken.getValue()))
            {
                TALog.getLogger().debug(itoken.getType() + "  found in ProcessTriples");
                af.addToInfoBox(infoBoxCategory, itoken);
            }
        }
    }

    private void processTripleObjectForSkills(PossibleSentence psentence)
    {
        try
        {
            List<IdentifiedToken> itokens = psentence.iTokensGet(IdentifiedToken.SKILL);
            TALog.getLogger().debug("count nerskills: " + itokens.size());
            for (IdentifiedToken itoken : itokens)
            {

                af.addToInfoBox(StringConstantsInterface.INFOBOX_CERTIFICATION, itoken);

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
                sb.append(triple.confidence + "\n" + triple.subjectLemmaGloss() + "\n" + triple.relationLemmaGloss()
                        + "\n" + triple.objectLemmaGloss());

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
