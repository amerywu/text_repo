package ikoda.nlp.structure;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InfoBox;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.TALog;
import ikoda.utils.DuplicateEntryChecker;

public class ChineseJobPostAnalyzedFile extends AnalyzedFile
{

    public final static String DEGREE_IN = "DEGREE IN";
    public final static String DEGREE = "Degree";
    public final static String ANYSUBJECT = "Any Subject";

    private final static String HTTP = "HTTP";
    private final static String COPYRIGHT = "COPYRIGHT";
    private final static String GENDER = "GENDER";
    private final static String ETHNIC = "ETHNIC";
    private final static String ETHNICITY = "ETHNICITY";
    private final static String RIGHTRESERVED = "RIGHTS RESERVED";
    private final static String INTERVIEW = "INTERVIEW";
    private final static String EQUAL = "EQUAL";
    private final static String EQUAL_OPPORTUNITY = "EQUAL OPPORTUNITY";
    private final static String INDEED = "INDEED";
    private final static String SIMPLYHIRED = "SIMPLYHIRED";
    private final static String CAREERBUILDER = "CAREERBUILDER";
    private final static String PRIVACY = "PRIVACY";
    private final static String APPLY = "APPLY";
    private final static String SOCIAL_SECURITY = "SOCIAL SECURITY";
    private final static String ELIGIBILITY = "ELIGIBILITY";
    private final static String ELIGIBLE = "ELIGIBLE";
    private final static String TERMS_CONDITIONS = "TERMS AND CONDITIONS";
    private final static String LEARN = "LEARN MORE";
    private final static String VISIT = "VISIT";
    private final static String EMPLOYMENT = "EMPLOYMENT";
    private final static String NAVIGATION = "NAVIGATION";
    private final static String RACE = "RACE";
    private final static String SEARCH = "SEARCH";
    private final static String QUALITY_RESULTS = "QUALITY RESULTS";

    private static String[] uidIgnoreWords = { HTTP, COPYRIGHT, RIGHTRESERVED, GENDER, ETHNICITY, ETHNIC, INDEED,
            EQUAL_OPPORTUNITY, EQUAL, INTERVIEW, SIMPLYHIRED, CAREERBUILDER, PRIVACY, APPLY, SOCIAL_SECURITY,
            ELIGIBILITY, ELIGIBLE, TERMS_CONDITIONS, LEARN, VISIT, EMPLOYMENT, NAVIGATION, RACE, SEARCH,
            QUALITY_RESULTS };

    private String salaryStartRange = "";
    private String salaryEndRange = "";

    ChineseJobPostAnalyzedFile(Path inpath, ReentrantLock inlock, ConfigurationBeanParent config)
    {
        super(inpath, inlock, config);
        id = inpath.getFileName().toString();

        contentType = StringConstantsInterface.JOBPOSTING_LABEL;

        TALog.getLogger().debug(contentType + "   |    AnalyzedFile:  " + id);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean addToInfoBoxExtension(String type, IdentifiedToken itoken)
    {

        if (null == itoken)
        {
            TALog.getLogger().warn("RETURNING FALSE addToInfoBoxExtension. itoken " + itoken);
            return false;
        }

        if (null == itoken.getValue() || itoken.getValue().isEmpty())
        {
            TALog.getLogger().debug("RETURNING FALSE addToInfoBoxExtension.  " + itoken.getValue());
            return false;
        }
        if (type.equals(StringConstantsInterface.INFOBOX_LOCATION))
        {
            String key = generateInfoBoxMapKey();
            InfoBox ib = infoBoxMap.get(key);
            TALog.getLogger().debug("ib " + ib);
            if (null == ib)
            {
                return true;
            }
            if (ib.getLocation().isEmpty())
            {

                return true;
            }
            TALog.getLogger().debug("aborting. Location already defined ");
            return false;
        }

        if (type.equals(StringConstantsInterface.INFOBOX_JOBTITLE))
        {
            /*
             * String key = generateInfoBoxMapKey(); InfoBox ib = infoBoxMap.get(key); if
             * (null == ib) { return true; }
             * 
             * if (ib.getJobTitles().size() > 0 &&
             * contentType.equals(StringConstantsInterface.RESULTLIST_LABEL)) {
             * TALog.getLogger().debug("\n\nAlready have job title. Treating as new Job\n");
             * newBlock(); } else if
             * (super.getCountMostFrequentIdenitifiedTokenInFile(IdentifiedToken.JOBTITLE) >
             * 2) { if
             * (super.getMostFrequentIdenitifiedTokenInFile(IdentifiedToken.JOBTITLE).
             * getValue() .toUpperCase().equals(itoken.getValue().toUpperCase())) { return
             * true; } else {
             * 
             * return false; } } if
             * (type.equals(StringConstantsInterface.INFOBOX_QUALIFICATION)) { if
             * (itoken.getValue().toUpperCase().trim().equals(DEGREE_IN)) {
             * TALog.getLogger().debug("changing " + itoken.getValue() + " to " + DEGREE);
             * itoken.setValue(DEGREE); } else if
             * (DEGREENerTokenAnalyzer.isMisassigned(itoken.getValue())) { return false; } }
             */
        }
        TALog.getLogger().debug("TRUE adding " + itoken.getValue());
        return true;

    }

    @Override
    protected boolean containsInfoBoxUIDIgnoreWords(String sentence)
    {
        TALog.getLogger().debug("Examining " + sentence);
        for (int i = 0; i < uidIgnoreWords.length; i++)
        {
            if (sentence.toUpperCase().contains(uidIgnoreWords[i]))
            {
                TALog.getLogger().debug("sentence contains  " + uidIgnoreWords[i]);
                TALog.getLogger().debug("return true ");
                return true;
            }
        }

        TALog.getLogger().debug("return false ");
        return false;
    }

    @Override
    public boolean finalizeFileExtension()
    {
        /// we're going to find the most frequently mentioned jobtitle and dump
        /// the rest

        TALog.getLogger().debug("finalizeFileExtension");
        TALog.getLogger().debug("finalizeFileExtension: " + infoBoxMap.size());
        TALog.getLogger().debug("finalizeFileExtension: " + contentType);
        Iterator<String> itr = infoBoxMap.keySet().iterator();
        while (itr.hasNext())
        {
            String key = itr.next();
            InfoBox ib = infoBoxMap.get(key);

            /// dump out f duplicate, register if not...same method does both
            if (!DuplicateEntryChecker.getInstance().isDuplicateEntry(ib.getInfoBoxUID()))
            {
                TALog.getLogger().debug("\n\n\nRemoving Duplicate " + ib.getInfoBoxUID());
                itr.remove();
            }

            List<String> jobTitles = ib.getJobTitles();
            if (jobTitles.size() > 1)
            {
                selectJobTitle(jobTitles, ib.getBlockNumber());
            }
            if (jobTitles.size() == 0)
            {
                return false;
            }

            ////// because salary range is only given at top of file, need to
            ////// pass to all info boxes
            TALog.getLogger().debug("contentType " + contentType);

            TALog.getLogger().debug("retained salary data " + salaryStartRange + " " + salaryEndRange);
            if (salaryEndRange.length() > 0 && salaryStartRange.length() > 0)
            {

                if (null != ib)
                {
                    if (ib.getStartSalaryRange() <= 0)
                    {
                        ib.addValueToInfoBox(StringConstantsInterface.INFOBOX_STARTSALARY, salaryStartRange);
                    }
                    if (ib.getEndSalaryRange() <= 0)
                    {
                        ib.addValueToInfoBox(StringConstantsInterface.INFOBOX_ENDSSALARY, salaryEndRange);
                    }
                }
            }
        }

        return true;

    }

    @Override
    protected String generateInfoBoxUID(String s)
    {
        String uidString = String.valueOf(s.getBytes().hashCode());
        TALog.getLogger().debug("uidstring " + uidString);
        return uidString;
    }

    private boolean hasJobTitleAndDegree(InfoBox ib)
    {
        if (ib.getJobTitles().size() > 0 && ib.getQualifications().size() > 0)
        {
            return true;
        }
        return false;

    }

    private boolean hasJobTitleAndSalaryRange(InfoBox ib)
    {
        if (ib.getJobTitles().size() > 0 && ib.getStartSalaryRange() > 0 && ib.getEndSalaryRange() > 0)
        {
            return true;
        }
        return false;

    }

    private boolean hasJobTitleButNoQualifications(InfoBox ib)
    {
        if (ib.getJobTitles().size() > 0 && ib.getQualifications().size() == 0)
        {
            return true;
        }
        return false;

    }

    private boolean hasQualificationsButNoJobTitle(InfoBox ib)
    {
        if (ib.getJobTitles().size() == 0 && ib.getQualifications().size() > 0)
        {
            return true;
        }
        return false;

    }

    @Override
    public int identifiedTokenCountForType(String tokenType)
    {
        int count = 0;
        TALog.getLogger().debug("current block " + blocknumber);
        for (PossibleSentence psentence : allSentences.values())
        {
            for (IdentifiedToken itoken : psentence.iTokensGetAll())
            {
                // NioLog.getLogger().debug("itoken from block
                // "+itoken.getInfoBoxId());

                if (itoken.getType().equals(tokenType))
                {
                    // NioLog.getLogger().debug(itoken.getValue() + " in
                    // blocknumber " + itoken.getInfoBoxId());
                    if (contentType.equals(StringConstantsInterface.JOBPOSTING_LABEL))
                    {
                        count++;
                    }
                    else if (itoken.getInfoBoxId() == blocknumber)
                    {
                        TALog.getLogger().debug("counting: " + itoken.getValue());
                        count++;
                    }
                }
            }
        }
        TALog.getLogger().debug(tokenType + " frequency count for infobox is " + count);
        return count;
    }

    protected boolean isSaveableInfoBox(InfoBox ib)
    {
        /*
         * if (hasJobTitleAndDegree(ib)) { return true; } else if
         * (hasJobTitleAndSalaryRange(ib)) { return true; } else { return false; }
         */
        return true;
    }

    private InfoBox mergeInfoBoxes()
    {
        InfoBox mergedIb = new InfoBox(id);
        mergedIb.setDatabaseDescriptor(config.getDatabaseDescriptor());

        TALog.getLogger().debug("Merging into One.");
        Iterator<String> itrIB = infoBoxMap.keySet().iterator();

        while (itrIB.hasNext())
        {
            String key = itrIB.next();
            InfoBox ib = infoBoxMap.get(key);
            TALog.getLogger().debug(ib);
            mergedIb.getJobTitles().addAll(ib.getJobTitles());

            mergedIb.getQualifications().addAll(ib.getQualifications());

            mergedIb.getWorkSkills().addAll(ib.getWorkSkills());

            mergedIb.getAreasOfStudy().addAll(ib.getAreasOfStudy());

            mergedIb.getSkills().addAll(ib.getSkills());

            if (mergedIb.getStartSalaryRange() <= 0)
            {
                mergedIb.setStartSalaryRange(ib.getStartSalaryRange());
            }
            if (mergedIb.getEndSalaryRange() <= 0)
            {
                mergedIb.setEndSalaryRange(ib.getEndSalaryRange());
            }

            if (mergedIb.getStartSalaryRange() > 10000 && ib.getStartSalaryRange() > 10000
                    && (mergedIb.getStartSalaryRange() != ib.getStartSalaryRange()))
            {
                /// too forked up, return and abort
                return new InfoBox();
            }

            if (mergedIb.getYearsExperienceAsInt() < ib.getYearsExperienceAsInt())
            {
                mergedIb.setYearsExperienceAsInt(ib.getYearsExperienceAsInt());
            }

            mergedIb.getYearsExperience().addAll(ib.getYearsExperience());

            mergedIb.setInfoBoxUID(mergedIb.getInfoBoxUID() + ib.getInfoBoxUID());

            mergedIb.setDetailLevel(mergedIb.getSkills().size());
            mergedIb.setBlockNumber(ib.getBlockNumber());
            mergedIb.setContentType(ib.getContentType());
            if (null == mergedIb.getRegion())
            {
                mergedIb.setRegion(ib.getRegion());
            }

        }

        TALog.getLogger().debug("\n\n\n\nmergedIB \n\n==========\n" + mergedIb);
        return mergedIb;
    }

    private void postProcessInfoBoxAreasOfStudy(List<InfoBox> infoBoxesToSave)
    {
        try
        {

            for (InfoBox ib : infoBoxesToSave)
            {
                if (ib.getAreasOfStudy().size() == 0 && ib.getQualifications().size() > 0)
                {
                    ib.getAreasOfStudy().add(ANYSUBJECT);
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected boolean postProcessInfoBoxes()
    {

        /*
         * List<InfoBox> infoBoxesToSave = postProcessInfoBoxesMergeConditions();
         * 
         * TALog.getLogger().debug("infoBoxesToSave.size(): " + infoBoxesToSave.size());
         * if (infoBoxesToSave.size() == 0) { return false; }
         * postProcessInfoBoxJobTitles(infoBoxesToSave);
         * postProcessInfoBoxAreasOfStudy(infoBoxesToSave);
         * postProcessInfoBoxSkills(infoBoxesToSave);
         * 
         * 
         * /// now let's find the most frequent job title IFF this is a full job ///
         * posting
         * 
         * for (InfoBox ib : infoBoxesToSave) { TALog.getLogger().debug("saving " +
         * ib.getJobTitles()); saveInfoBox(ib); }
         */
        TALog.getLogger().debug("Saving infoboxes " + infoBoxMap.size());
        Iterator<String> itr = infoBoxMap.keySet().iterator();

        while (itr.hasNext())
        {
            String key = itr.next();
            InfoBox ib = infoBoxMap.get(key);
            ib.setDegreeLevel("DEGREE_LEVEL");
            TALog.getLogger().debug("key: " + key);
            saveInfoBox(ib);
        }

        return true;
    }

    private void postProcessInfoBoxJobTitles(List<InfoBox> infoBoxesToSave)
    {
        try
        {
            if (contentType.equals(StringConstantsInterface.JOBPOSTING_LABEL))
            {
                TALog.getLogger().debug(StringConstantsInterface.JOBPOSTING_LABEL + "Further processing");
                if (infoBoxesToSave.size() < 3)
                {
                    IdentifiedToken mostLikelyJobTitle = super.getMostFrequentIdenitifiedTokenInFile(
                            IdentifiedToken.JOBTITLE);

                    if (null != mostLikelyJobTitle)
                    {
                        TALog.getLogger().debug("Most frequent job title is " + mostLikelyJobTitle.getValue());
                        for (InfoBox ib : infoBoxesToSave)
                        {

                            ib.getJobTitles().clear();
                            ib.addValueToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE,
                                    mostLikelyJobTitle.getValue());
                        }
                    }
                    else
                    {
                        for (InfoBox ib : infoBoxesToSave)
                        {
                            String keepString = "";
                            for (String jobTitle : ib.getJobTitles())
                            {
                                if (jobTitle.length() >= keepString.length())
                                {
                                    if (jobTitle.length() < 50)
                                    {
                                        keepString = jobTitle;
                                    }
                                }
                            }
                            ib.getJobTitles().clear();
                            ib.addValueToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE, keepString);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void postProcessInfoBoxSkills(List<InfoBox> infoBoxesToSave)
    {
        try
        {

            /// these could be partial sentences from a result listing page
            for (InfoBox ib : infoBoxesToSave)
            {
                if (ib.getSkills().size() <= 2)
                {
                    ib.getSkills().clear();
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void selectJobTitle(List<String> jobTitles, Integer blockId)
    {

        if (jobTitles.size() < 2)
        {
            return;
        }

        Iterator<String> itr = jobTitles.iterator();
        int maxlength = 0;
        String longestJobTitle = "";
        while (itr.hasNext())
        {
            String jobTitle = itr.next();
            if (jobTitle.length() > maxlength)
            {
                maxlength = jobTitle.length();
                longestJobTitle = jobTitle;

            }
        }

        Iterator<String> itr1 = jobTitles.iterator();

        while (itr1.hasNext())
        {
            String jobTitle = itr1.next();
            if (!(jobTitle.equals(longestJobTitle)))
            {
                itr1.remove();

            }
        }

    }

    private int wordCount(String sentence)
    {
        String words = sentence.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String words1 = words.replace("  ", " ");
        String[] wordsArray = words1.split(" ");
        return wordsArray.length;
    }

}
