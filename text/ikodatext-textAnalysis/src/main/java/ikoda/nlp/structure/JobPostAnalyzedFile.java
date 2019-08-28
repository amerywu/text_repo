package ikoda.nlp.structure;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import bridge.RScalaBridge;
import bridge.RScalaBridgeObject;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InfoBox;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.FileAnalyzerFactory;
import ikoda.nlp.analysis.JobDescriptionAnalyzer;
import ikoda.nlp.analysis.TALog;
import ikoda.nlp.analyzers.DEGREENerTokenAnalyzer;
import ikoda.persistence.model.CountDegree;
import ikoda.utils.DuplicateEntryChecker;
import ikoda.utils.ElasticSearchManager;
import ikoda.utils.MultiplePropertiesSingleton;
import ikoda.utils.ProcessStatus;

public class JobPostAnalyzedFile extends AnalyzedFile
{

    public final static String DEGREE_IN = "DEGREE IN";
    public final static String DEGREE = "Degree";

    public final static String ANYSUBJECT = "Any Subject";
    public final static String RELEVANT = "RELEVANT";
    public final static String RELATED = "RELATED";
    public final static String APPROPRIATE = "APPROPRIATE";

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
    private final static String QUALIFICATION = "QUALIFICATION";
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
    private final static String JOB_TITLE_TRIM1 = "-";
    private final static String JOB_TITLE_TRIM2 = "JOB DESCRIPTION";
    private final static String JOB_TITLE_TRIM3 = "_";
    private final static String COMMA = ",";
    private final static String EMPTY = "";
    private final static String STOP = ". ";
    private final static String UNDERSCORE = "_";

    private static String[] uidIgnoreWords = { HTTP, COPYRIGHT, RIGHTRESERVED, GENDER, ETHNICITY, ETHNIC, INDEED,
            EQUAL_OPPORTUNITY, EQUAL, INTERVIEW, SIMPLYHIRED, CAREERBUILDER, PRIVACY, APPLY, SOCIAL_SECURITY,
            ELIGIBILITY, ELIGIBLE, TERMS_CONDITIONS, LEARN, VISIT, EMPLOYMENT, NAVIGATION, RACE, SEARCH,
            QUALITY_RESULTS };

    private static String[] jobTitleTrim = { JOB_TITLE_TRIM1, JOB_TITLE_TRIM2, JOB_TITLE_TRIM3 };

    private static String[] relatedAreas = { APPROPRIATE, RELATED, RELEVANT };

    private String salaryStartRange = "";
    private String salaryEndRange = "";

    JobPostAnalyzedFile(Path inpath, ReentrantLock inlock, ConfigurationBeanParent config)
    {
        super(inpath, inlock, config);
        id = inpath.getFileName().toString();
        if (id.contains(StringConstantsInterface.JOBPOSTING_LABEL))
        {
            contentType = StringConstantsInterface.JOBPOSTING_LABEL;
        }
        else if (id.contains(StringConstantsInterface.RESULTLIST_LABEL))
        {
            contentType = StringConstantsInterface.RESULTLIST_LABEL;
        }
        else if (id.contains(StringConstantsInterface.UNDETERMINED_LABEL))
        {
            /// seems the safer option as this allows less merging
            contentType = StringConstantsInterface.RESULTLIST_LABEL;
        }
        TALog.getLogger().debug(contentType + "   |    AnalyzedFile:  " + id);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean addToInfoBoxExtension(String type, IdentifiedToken itoken)
    {
        TALog.getLogger().debug("addToInfoBoxExtension. Looking to add " + itoken.getValue());

        if (null == itoken)
        {
            return false;
        }
        if (null == itoken.getValue() || itoken.getValue().isEmpty())
        {
            return false;
        }

        if (type.equals(StringConstantsInterface.INFOBOX_SKILLS))
        {
            if (wordCount(itoken.getValue()) <= 4)
            {
                return false;
            }
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

        if (type.equals(StringConstantsInterface.INFOBOX_STARTSALARY))
        {
            TALog.getLogger().debug("This is a salary");

            /// if we are setting the salary a second time on a job post, there
            /// is a high chance of inaccuracy. Better to unset salary than set
            /// it wrong
            if (salaryStartRange.length() > 0)
            {
                if (contentType.equals(StringConstantsInterface.JOBPOSTING_LABEL))
                {
                    salaryStartRange = "-1";
                    itoken.setValue("-1");
                    TALog.getLogger().warn("Second salary value. I'm confused, ergo resetting to -1");

                    return true;
                }
            }
            else
            {
                salaryStartRange = itoken.getValue();
            }
            TALog.getLogger().debug("It's a pass");
            return true;
        }
        if (type.equals(StringConstantsInterface.INFOBOX_ENDSSALARY))
        {
            if (salaryEndRange.length() > 0)
            {
                if (contentType.equals(StringConstantsInterface.JOBPOSTING_LABEL))
                {
                    salaryEndRange = "-1";
                    itoken.setValue("-1");
                    TALog.getLogger().warn("Second salary value. I'm confused, ergo resetting to -1");

                    return true;
                }
            }
            salaryEndRange = itoken.getValue();
            return true;
        }

        if (type.equals(StringConstantsInterface.INFOBOX_JOBTITLE))
        {
            String key = generateInfoBoxMapKey();
            InfoBox ib = infoBoxMap.get(key);
            if (null == ib)
            {
                return true;
            }

            if (ib.getJobTitles().size() > 0 && contentType.equals(StringConstantsInterface.RESULTLIST_LABEL))
            {
                TALog.getLogger().debug("\n\nAlready have job title. Treating as new Job\n");
                newBlock();
            }
            else if (super.getCountMostFrequentIdenitifiedTokenInFile(IdentifiedToken.JOBTITLE) > 2)
            {
                if (super.getMostFrequentIdenitifiedTokenInFile(IdentifiedToken.JOBTITLE).getValue().toUpperCase()
                        .equals(itoken.getValue().toUpperCase()))
                {
                    return true;
                }
                else
                {

                    return false;
                }
            }
            if (type.equals(StringConstantsInterface.INFOBOX_QUALIFICATION))
            {
                if (itoken.getValue().toUpperCase().trim().equals(DEGREE_IN))
                {
                    TALog.getLogger().debug("changing " + itoken.getValue() + " to " + DEGREE);
                    itoken.setValue(DEGREE);
                }
                else if (DEGREENerTokenAnalyzer.isMisassigned(itoken.getValue()))
                {
                    return false;
                }
            }
        }
        TALog.getLogger().debug("adding " + itoken.getValue());
        return true;

    }

    private void analyzeFailedRun()
    {
        try
        {
            Iterator<String> itrIB = infoBoxMap.keySet().iterator();

            boolean gotJobTitle = false;
            boolean gotAreasOfStudy = false;
            boolean gotQualification = false;
            String region = "";

            while (itrIB.hasNext())
            {
                String key = itrIB.next();
                InfoBox ib = infoBoxMap.get(key);

                TALog.getLogger().debug(ib);

                if (ib.getJobTitles().size() > 0)
                {
                    gotJobTitle = true;
                }
                if (ib.getAreasOfStudy().size() > 0)
                {
                    gotAreasOfStudy = true;
                }
                if (ib.getQualifications().size() > 0)
                {
                    gotQualification = true;
                }
            }
            if (!gotJobTitle)
            {
                ProcessStatus.incrementStatus("TA Failed on JobTitle");
                ProcessStatus.incrementStatus("TA Failed on JobTitle " + region);
                super.finalStatus = super.finalStatus + StringConstantsInterface.STATUS_NO_JOBTITLE;
            }
            if (!gotAreasOfStudy)
            {
                ProcessStatus.incrementStatus("TA Failed on  AreasOfStudy");
                ProcessStatus.incrementStatus("TA Failed on  AreasOfStudy " + region);
                super.finalStatus = super.finalStatus + StringConstantsInterface.STATUS_NO_AREASOFSTUDY;
            }
            if (!gotQualification)
            {
                ProcessStatus.incrementStatus("TA Failed on  Skills");
                ProcessStatus.incrementStatus("TA Failed on  Skills " + region);
                super.finalStatus = super.finalStatus + StringConstantsInterface.STATUS_NO_QUALIFICATION;
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
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
                keepMostFrequentJobTitle(jobTitles, ib.getBlockNumber());
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
        String uidString = s.replaceAll("[^A-Za-z0-9]", "");
        return uidString;
    }

    private String getDegreeLevel(InfoBox ib)
    {
        try
        {
            boolean degreeLevel = false;
            boolean certLevel = false;
            String degreeTag = PropertiesSingleton.getInstance().getNerPropertyTypeByToken("BACHELORS");
            String certTag = PropertiesSingleton.getInstance().getNerPropertyTypeByToken("DIPLOMA");

            for (String s : ib.getQualifications())
            {

                if (null != s)
                {
                    String type = PropertiesSingleton.getInstance().getNerPropertyTypeByToken(s.toUpperCase());
                    if (null != type && type.equals(degreeTag))
                    {
                        degreeLevel = true;
                    }
                    if (null != type && type.equals(certTag))
                    {
                        if (type.contains("ASSOCI"))
                        {
                            certLevel = true;
                            ;
                        }

                    }
                }

            }
            if (certLevel && degreeLevel)
            {
                return CountDegree.CERTIFICATEANDDEGREELEVEL;
            }
            else if (degreeLevel)
            {
                return CountDegree.DEGREELEVEL;
            }

            return CountDegree.CERTIFICATELEVEL;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return "NA";
        }
    }

    private boolean hasJobTitleAndCertification(InfoBox ib)
    {
        if (ib.getJobTitles().size() > 0 && ib.getCertification().size() > 0)
        {
            return true;
        }
        return false;

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
        if (hasJobTitleAndDegree(ib))
        {
            return true;
        }
        else if (hasJobTitleAndSalaryRange(ib))
        {
            return true;
        }
        else if (hasJobTitleAndCertification(ib))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void keepMostFrequentJobTitle(List<String> jobTitles, Integer blockId)
    {
        IdentifiedToken itoken = null;
        if (contentType.equals(StringConstantsInterface.RESULTLIST_LABEL))
        {
            itoken = super.getMostFrequentIdenitifiedTokenInBlock(blockId, IdentifiedToken.JOBTITLE);
        }
        else if (contentType.equals(StringConstantsInterface.JOBPOSTING_LABEL))
        {
            itoken = super.getMostFrequentIdenitifiedTokenInFile(IdentifiedToken.JOBTITLE);
        }

        if (null == itoken)
        {
            return;
        }
        if (jobTitles.size() < 2)
        {
            return;
        }
        TALog.getLogger().debug("Most frequent IdentifiedToken is " + itoken);
        Iterator<String> itr = jobTitles.iterator();
        while (itr.hasNext())
        {
            String jobTitle = itr.next();
            if (!jobTitle.equals(itoken.getValue()))
            {
                TALog.getLogger().debug("Removing less frequent job title " + jobTitle);
                itr.remove();
            }
        }

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

            mergedIb.getCertification().addAll(ib.getCertification());

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
            mergedIb.setLocation(ib.getLocation());
            if (null == mergedIb.getRegion())
            {
                mergedIb.setRegion(ib.getRegion());
            }

        }

        TALog.getLogger().debug("\n\n\n\nmergedIB \n\n==========\n" + mergedIb);
        return mergedIb;
    }

    private InfoBox mergeInfoBoxesTitleWithQuals(InfoBox noJobTitle, InfoBox noQualifications)
    {
        InfoBox mergedIb = new InfoBox(id);
        mergedIb.setDatabaseDescriptor(config.getDatabaseDescriptor());
        TALog.getLogger().debug("\n\n\n\n\n\n############################\n\n\n\n\n\n\n\n\nMerging "
                + noJobTitle.getBlockNumber() + " with " + noQualifications.getBlockNumber());
        mergedIb.setJobTitles(noQualifications.getJobTitles());
        TALog.getLogger().debug("\n\nnoJobTitle:\n" + noJobTitle);
        TALog.getLogger().debug("\n\nnoQualifications:\n" + noQualifications);

        mergedIb.setQualifications(noJobTitle.getQualifications());
        if (noJobTitle.getAreasOfStudy().size() >= noQualifications.getAreasOfStudy().size())
        {
            mergedIb.setAreasOfStudy(noJobTitle.getAreasOfStudy());
        }
        else
        {
            mergedIb.setAreasOfStudy(noQualifications.getAreasOfStudy());
        }
        if (noJobTitle.getSkills().size() >= noQualifications.getSkills().size())
        {
            mergedIb.setSkills(noJobTitle.getSkills());
        }
        else
        {
            mergedIb.setSkills(noQualifications.getSkills());
        }
        mergedIb.getWorkSkills().addAll(noQualifications.getWorkSkills());
        mergedIb.getWorkSkills().addAll(noJobTitle.getWorkSkills());

        if (noJobTitle.getStartSalaryRange() >= noQualifications.getStartSalaryRange())
        {
            mergedIb.setStartSalaryRange(noJobTitle.getStartSalaryRange());
        }
        else
        {
            mergedIb.setStartSalaryRange(noQualifications.getStartSalaryRange());
        }

        if (noJobTitle.getEndSalaryRange() >= noQualifications.getEndSalaryRange())
        {
            mergedIb.setEndSalaryRange(noJobTitle.getEndSalaryRange());
        }
        else
        {
            mergedIb.setEndSalaryRange(noQualifications.getEndSalaryRange());
        }

        if (noJobTitle.getYearsExperienceAsInt() >= noQualifications.getYearsExperienceAsInt())
        {
            mergedIb.setYearsExperienceAsInt(noJobTitle.getYearsExperienceAsInt());
        }
        else
        {
            mergedIb.setYearsExperienceAsInt(noQualifications.getYearsExperienceAsInt());
        }

        if (noJobTitle.getYearsExperience().size() >= noQualifications.getYearsExperience().size())
        {

            mergedIb.setYearsExperience(noJobTitle.getYearsExperience());
        }
        else
        {
            mergedIb.setYearsExperience(noQualifications.getYearsExperience());
        }

        mergedIb.setInfoBoxUID(noJobTitle.getInfoBoxUID() + noQualifications.getInfoBoxUID());

        mergedIb.setDetailLevel(mergedIb.getSkills().size());
        mergedIb.setBlockNumber(noQualifications.getBlockNumber());
        mergedIb.setContentType(noJobTitle.getContentType());
        mergedIb.setLocation(noJobTitle.getLocation());

        TALog.getLogger().debug("\n\n\n\nmergedIB " + mergedIb);
        return mergedIb;
    }
    
    private void postProcessInfoBoxMainAreaOfStudy(List<InfoBox> infoBoxesToSave)
    {
    	try
		{
    		 for (InfoBox ib : infoBoxesToSave)
             {// if no qualifications, best guess from job title
				String jobTitleO = ib.getJobFinal();
				if (null == jobTitleO)
				{
					TALog.getLogger().warn("Null jobtitle in database");
					return;
				}
	
				String jobTitleP = jobTitleO.replaceAll(" ", UNDERSCORE).toUpperCase();
				if (ib.getAreasOfStudy().isEmpty())
				{
	
					String majorP = MultiplePropertiesSingleton.getInstance().getProperties("JobToMajor.properties")
							.getProperty(jobTitleP);
					if (null != majorP)
					{
						String majorO = majorP.replaceAll(UNDERSCORE, " ");
						ib.getAreasOfStudy().add(majorO);
						ib.setMajorFinal(majorO);
	
					}
				}
				else
				{
					String majorO = ib.getAreasOfStudy().get(0);
	
					String majorP = majorO.replaceAll(" ", UNDERSCORE).toUpperCase();
	
					String aggregatedMajorP = MultiplePropertiesSingleton.getInstance()
							.getProperties("aggregatedMajors.properties").getProperty(majorP);
	
					if (null != aggregatedMajorP && !aggregatedMajorP.isEmpty())
					{
						String aggregatedMajorO = aggregatedMajorP.replaceAll(UNDERSCORE, " ");
						// if we get a vague answer, we make a best guess from job title
						if (aggregatedMajorP.equalsIgnoreCase("ANY_SUBJECT"))
						{
							String majorPSuggested = MultiplePropertiesSingleton.getInstance()
									.getProperties("JobToMajor.properties").getProperty(jobTitleP);
							if (null != majorPSuggested)
							{
								String majorOSuggested = majorPSuggested.replaceAll(UNDERSCORE, " ");
								ib.getQualifications().add(majorOSuggested);
								ib.setMajorFinal(majorOSuggested);
							}
							else
							{
								ib.setMajorFinal(majorO.toUpperCase());
							}
						}
						else
						{
							ib.setMajorFinal(aggregatedMajorO.toUpperCase());
						}
					}
					else
					{
	
						ib.setMajorFinal("#" + majorO.toUpperCase());
					}
				}
             }

		}
		catch (Exception e)
		{
			TALog.getLogger().info(e.getMessage(), e);
		}
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
                    super.finalStatus = super.finalStatus + StringConstantsInterface.STATUS_ANYDEGREE;
                }
                if (ib.getCertification().size() > 0)
                {
                    super.finalStatus = super.finalStatus + StringConstantsInterface.STATUS_CERTIFICATION;
                }

                Iterator<String> itr = ib.getQualifications().iterator();
                /// the word qualification is too generic to keep
                while (itr.hasNext())
                {
                    String s = itr.next();
                    if (s.toUpperCase().contains(QUALIFICATION))
                    {
                        itr.remove();
                    }
                }
                ib.setDegreeLevel(getDegreeLevel(ib));
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
    	
        List<InfoBox> infoBoxesToSave = postProcessInfoBoxesMergeConditions();

        TALog.getLogger().debug("infoBoxesToSave.size(): " + infoBoxesToSave.size());
        if (infoBoxesToSave.size() == 0)
        {
            analyzeFailedRun();
            return false;
        }

        postProcessInfoBoxJobTitles(infoBoxesToSave);
        postProcessInfoBoxAreasOfStudy(infoBoxesToSave);
        postProcessInfoBoxSkills(infoBoxesToSave);
        postProcessInfoBoxMainAreaOfStudy(infoBoxesToSave);

        boolean saved = false;
        for (InfoBox ib : infoBoxesToSave)
        {
            TALog.getLogger().debug("saving " + ib);

            if (postProcessInfoBoxesSendToR(ib))
            {
                prepForR(ib);
                TALog.getLogger().debug("Returned from R with " + ib.getAreasOfStudy());
                TALog.getLogger().debug("Derived from" + ib.getSkills());
            }

            boolean b = saveInfoBox(ib);
            if (true == b)
            {
                ProcessStatus.incrementStatus("TA Success");
                ProcessStatus.incrementStatus("TA Success " + ib.getRegion());
                saved = true;
            }
            if(config.isSendToES())
            {
            	if(ib.getDetailLevel() > 2) {
            		dispatchToES(ib);
            	}
            }
        }
        if (!saved)
        {
            ProcessStatus.incrementStatus("TA Failed Files");
            analyzeFailedRun();
        }
        


        return saved;
    }
    
    private String listToString(List<String>  list, String separator) {
    	
    	if(list.isEmpty())
    	{
    		return "";
    	}
    	StringBuilder sb = new StringBuilder();
    	Iterator<String> itr = list.iterator();
    	while (itr.hasNext()) {
    		sb.append(itr.next());
    		if(itr.hasNext()) {
    			sb.append(separator);
    		}
    	}
    	return sb.toString();

    }
    
	private boolean dispatchToES(InfoBox ib) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.startObject();
			{
				builder.field("contentType", ib.getContentType());
				builder.field("location", ib.getLocation());
				builder.field("salaryStart", ib.getStartSalaryRange());
				builder.field("salaryEnd", ib.getEndSalaryRange());
				builder.field("jobTitles", listToString(ib.getJobTitles(), COMMA));
				builder.field("qualifications", listToString(ib.getQualifications(), COMMA));
				builder.field("areasOfStudy", listToString(ib.getAreasOfStudy(), COMMA));
				builder.field("certification", listToString(ib.getCertification(), COMMA));
				builder.field("skills", listToString(ib.getSkills(), STOP));
				builder.field("relatedMajors", listToString(ib.getRelatedMajors(), COMMA));
				builder.field("workSkills", listToString(ib.getWorkSkills(), COMMA));
				builder.field("region", ib.getRegion());
				builder.field("degreeLevel", ib.getDegreeLevel());
				builder.field("detailLevel", ib.getDetailLevel());
				builder.field("yearsExperienceAsInt", String.valueOf(ib.getYearsExperienceAsInt()));
				builder.field("majorFinal", ib.getMajorFinal());
				builder.field("jobFinal", ib.getJobFinal());
				builder.field("databaseDescriptor", ib.getDatabaseDescriptor());
				builder.field("created", System.currentTimeMillis());
			}
			builder.endObject();
			ElasticSearchManager.getInstance().addDocument(builder, EsJson.JOBS_INDEX_NAME);

			return true;
		} catch (Exception e) {
			TALog.getLogger().warn(e.getMessage(), e);
			return false;
		}

	}
    	
    	


    private List<InfoBox> postProcessInfoBoxesMergeConditions()
    {
        try
        {
            /// maybe job title separated from details by to many empty lines
            TALog.getLogger().debug("infoBoxMap size: " + infoBoxMap.size());

            InfoBox noQualifications = null;
            InfoBox noJobTitle = null;

            List<InfoBox> infoBoxesToSave = new ArrayList<InfoBox>();
            /// if less than 4 merge all and be done with it.
            if (infoBoxMap.keySet().size() < 4)
            {
                infoBoxesToSave.add(mergeInfoBoxes());
            }
            /// if less than seven be a bit smarter with the merging
            else if (infoBoxMap.keySet().size() < 7)
            {
                Iterator<String> itrIB = infoBoxMap.keySet().iterator();
                while (itrIB.hasNext())
                {
                    String key = itrIB.next();
                    InfoBox ib = infoBoxMap.get(key);
                    TALog.getLogger().debug(ib);

                    if (hasJobTitleAndDegree(ib))
                    {
                        TALog.getLogger().debug("\n\nJob and Degree: ADDING TO  infoBoxesToSave \n\n" + ib);
                        infoBoxesToSave.add(ib);
                    }
                    else if (hasJobTitleButNoQualifications(ib))
                    {

                        TALog.getLogger().debug(
                                "\n\n job no quals....merging.\n MERGUNG THEN ADDING TO  infoBoxesToSave \n\n" + ib);
                        noQualifications = ib;
                        if (null != noJobTitle)
                        {
                            infoBoxesToSave.add(mergeInfoBoxesTitleWithQuals(noJobTitle, noQualifications));
                            noJobTitle = null;
                            noQualifications = null;
                        }

                    }
                    else if (hasQualificationsButNoJobTitle(ib))
                    {

                        noJobTitle = ib;
                        if (null != noQualifications)
                        {
                            TALog.getLogger().debug(
                                    "\n\n quals no job....merging.\n MERGING THEN ADDING TO  infoBoxesToSave \n\n"
                                            + ib);
                            infoBoxesToSave.add(mergeInfoBoxesTitleWithQuals(noJobTitle, noQualifications));
                            noJobTitle = null;
                            noQualifications = null;
                        }

                    }

                }
            }
            else
            {
                TALog.getLogger().debug("\n\nADDING all to  infoBoxesToSave\n\n ");
                infoBoxesToSave.addAll(infoBoxMap.values());
            }

            return infoBoxesToSave;
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return new ArrayList<InfoBox>();
        }
    }

    private boolean postProcessInfoBoxesSendToR(InfoBox ib)
    {
        TALog.getLogger().debug("postProcessInfoBoxesSendToR");

        if (ib.getAreasOfStudy().size() > 1)
        {
            return false;
        }

        TALog.getLogger().debug(ib.getAreasOfStudy());
        for (String s : ib.getAreasOfStudy())
        {
            for (int i = 0; i < relatedAreas.length; i++)
            {
                if (s.toUpperCase().trim().toUpperCase().contains(relatedAreas[i].toUpperCase()))
                {
                    TALog.getLogger().debug("TRUE");
                    return true;
                }
            }
        }
        return false;
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
                            mostLikelyJobTitle.setValue(trimJobTitle(mostLikelyJobTitle.getValue(), ib.getRegion()));
                            ib.addValueToInfoBox(StringConstantsInterface.INFOBOX_JOBTITLE,
                                    mostLikelyJobTitle.getValue());
                            ib.setJobFinal(mostLikelyJobTitle.getValue());
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
                            ib.setJobFinal(keepString);
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
                ArrayList<String> newList = new ArrayList();
                for(String s : ib.getSkills()) {
                	newList.add(s.replace(".", " ").trim());
                }
                ib.setSkills(newList);
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }
    


    private void prepForR(InfoBox ib)
    {
        try
        {
            TALog.getLogger().debug("prepForR: ");

            if (ib.getSkills().size() < 4)
            {
                TALog.getLogger().debug("Insufficient data: ");
                return;
            }

            StringBuffer sb = new StringBuffer();
            for (String s : ib.getSkills())
            {
                sb.append(s);
                sb.append(" ");
            }

            TALog.getLogger().debug("InfoBox: " + ib.getFileId());
            TALog.getLogger().debug("InfoBox: " + ib.getAreasOfStudy());
            TALog.getLogger().debug("InfoBox: " + ib.getSkills());
            List<String> lemmas = new ArrayList<String>();
            List<Integer> frequency = new ArrayList<Integer>();
            List<String> frequencyAsString = new ArrayList<String>();

            JobDescriptionAnalyzer jda = (JobDescriptionAnalyzer) FileAnalyzerFactory
                    .getProcessorForDataSource(StringConstantsInterface.DATASOURCE_JOB_DESCRIPTION, pipeline, config);
            jda.processString(sb.toString());

            for (IdentifiedToken itoken : jda.getCountedTokens())
            {
                if (itoken.getType().equals(IdentifiedToken.NOUN))
                {
                    lemmas.add(itoken.getValue());
                    frequency.add(itoken.getFrequencyCount());
                    frequencyAsString.add("Yes");
                }
                if (itoken.getType().equals(IdentifiedToken.VERB))
                {
                    lemmas.add(itoken.getValue() + "_V");
                    frequency.add(itoken.getFrequencyCount());
                    frequencyAsString.add("Yes");
                }
            }

            StringBuilder sbLemmas = new StringBuilder();
            StringBuilder sbFrequency = new StringBuilder();

            for (String strLemma : lemmas)
            {
                sbLemmas.append(strLemma);
                sbLemmas.append(COMMA);
            }

            for (Integer iFreq : frequency)
            {
                sbFrequency.append(iFreq);
                sbFrequency.append(COMMA);
            }

            sendToR(ib, sbLemmas.toString(), frequency, sbFrequency.toString());

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);

        }
    }

    private void sendToR(InfoBox ib, String lemmas, List<Integer> frequency, String frequencyAsString)
    {
        TALog.getLogger()
                .debug("\n\nsendToR....\n\n" + lemmas + "\n\n" + frequencyAsString + "\n\n" + ib.getJobTitles());

        try
        {
            if (frequency.size() < 10)
            {
                return;
            }


            ProcessStatus.incrementStatus("TA Sent to R");
            RScalaBridge rbridge = new RScalaBridge();
            HashMap<String, String> argsMap = new HashMap<String, String>();

            argsMap.put("lemmas", lemmas);
            argsMap.put("frequency", frequencyAsString);
            String response = rbridge.invokeRFunction("doPredictionSVM", argsMap,
                    RScalaBridgeObject.RETURNTYPE_STRING(), "");
            TALog.getLogger().debug("\n\nRESPONSE\n\n" + response);

            updateInfoBox(ib, response);
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private String trimJobTitle(String sin, String region)
    {
        String s = capitalize(sin, region);
        String newString = new String();
        for (int i = 0; i < jobTitleTrim.length; i++)
        {
            if (s.toUpperCase().startsWith(jobTitleTrim[i].toUpperCase()))
            {
                newString = s.substring(jobTitleTrim[i].length(), s.length());
                return newString.trim();

            }

        }
        return s.trim();

    }

    private void updateInfoBox(InfoBox ib, String response)
    {
        try
        {
            TALog.getLogger().debug("Processing response: " + response);
            TALog.getLogger().debug("From file: " + ib.getFileId());
            if (null == response)
            {
                TALog.getLogger().debug("NULL RESPONSE");
                return;
            }
            String classifactionString = response.substring((response.indexOf("KAI") + 3), response.indexOf("GUAN"));
            TALog.getLogger().debug(classifactionString);

            if (classifactionString.trim().length() > 1)
            {
                String[] degreeArray = classifactionString.split(",");
                TALog.getLogger().debug("degreeArray length: " + degreeArray.length);
                if (degreeArray.length > 3)
                {
                    return;
                }
                else
                {
                    ib.getAreasOfStudy().clear();
                    for (int i = 0; i < degreeArray.length; i++)
                    {
                        TALog.getLogger().debug("degreeArray : " + degreeArray[i]);
                        String temp = capitalize(degreeArray[i].trim(), ib.getRegion());
                        ib.getAreasOfStudy().add(temp);

                    }
                    ib.setByExtrapolation(true);
                    ProcessStatus.incrementStatus("TA Corrected by R");
                }
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
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
