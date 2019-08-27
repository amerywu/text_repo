package ikoda.netio.config;

public interface StringConstantsInterface
{

	public static final String JOBPOSTING_LABEL = "JOBPOSTING_LABEL";
	public static final String RESULTLIST_LABEL = "RESULTLIST_LABEL";
	public static final String UNDETERMINED_LABEL = "UNDETERMINED_LABEL";

	public static final String INFOBOX_LOCATION = "Location";
	public static final String INFOBOX_QUALIFICATION = "Qualifications";
	public static final String INFOBOX_CERTIFICATION = "Certification";
	public static final String INFOBOX_STARTSALARY = "Salary Range Bottom";
	public static final String INFOBOX_ENDSSALARY = "Salary Range Top";
	public static final String INFOBOX_RELATED_MAJORS = "Related Majors";
	public static final String INFOBOX_AREASOFSTUDY = "Areas of Study";
	public static final String INFOBOX_JOBTITLE = "Job Title";
	public static final String INFOBOX_SKILLS = "Knowledge and Skills";
	public static final String INFOBOX_WORKSKILLS = "Work Skills";
	public static final String INFOBOX_YEARS_EXPERIENCE = "Experience";
	public static final String INFOBOX_YEARS_EXPERIENCE_AS_INT = "Years Experience";

	public static final String[] XINFOBOX_VALIDKEYS =
	{ INFOBOX_QUALIFICATION, INFOBOX_LOCATION, INFOBOX_STARTSALARY, INFOBOX_ENDSSALARY, INFOBOX_RELATED_MAJORS,
			INFOBOX_AREASOFSTUDY, INFOBOX_JOBTITLE, INFOBOX_SKILLS, INFOBOX_WORKSKILLS, INFOBOX_YEARS_EXPERIENCE,
			INFOBOX_YEARS_EXPERIENCE_AS_INT, INFOBOX_CERTIFICATION };

	public static final String REGION_CA = "Canada";
	public static final String REGION_UK = "UK";
	public static final String REGION_AU = "Australia";
	public static final String REGION_US = "US";
	public static final String REGIION_ZH = "zh";

	public static final String STATUS_NO_AREASOFSTUDY = "_NOAOS_";
	public static final String STATUS_NO_QUALIFICATION = "_NOQ_";
	public static final String STATUS_NO_JOBTITLE = "_NOJT_";
	public static final String STATUS_ANYDEGREE = "_AD_";
	public static final String STATUS_CERTIFICATION = "_C_";

	public static final String GRAB_BLOCK_START = "|[OPENTAGS]|";
	public static final String GRAB_BLOCK_END = " |[CLOSETAGS]|";
	public static final String SPIDERTAGJOBTITLE = "|[JOB TITLE]| ";
	public static final String SPIDERTAG_CATEGORY = "|[SPIDERTAG_CATEGORY]| ";
	public static final String SPIDERTAG_CATEGORY_CLOSE = "|[SPIDERTAG_CATEGORY_CLOSE]| ";
	public static final String SPIDERTAGLOCATIONOPEN = "|[OPENLOCATION]| ";
	public static final String SPIDERTAGLOCATIONCLOSE = " |[CLOSELOCATION]| ";
	public static final String SPIDERTAGSALARYOPEN = "|[OPENSALARY]| ";
	public static final String SPIDERTAGSALARYCLOSE = " |[CLOSESALARY]| ";
	public static final String SPIDERTAG_HOST_OPEN = "|[SPIDERTAGHOSTOPEN]| ";
	public static final String SPIDERTAG_HOST_CLOSE = " SPIDERTAGHOSTCLOSE]| ";
	public static final String SPIDERTAG_URI_OPEN = "|[SPIDERTAG_URI_OPEN]| ";
	public static final String SPIDERTAG_URI_CLOSE = "|[SPIDERTAG_URI_CLOSE]| ";
	public static final String SPIDERTAG_FULLURL_OPEN = "|[SPIDERTAG_FULLURL_OPEN]|";
	public static final String SPIDERTAG_FULLURLCLOSE = "|[SPIDERTAG_FULLURLCLOSE]|";
	public static final String SPIDERTAG_WEBSITE_OPEN = "|[SPIDERTAG_WEBSITE_OPEN]|";
	public static final String SPIDERTAG_URLREPOSITORY_OPEN = "|[SPIDERTAG_URLREPOSITORY_OPEN]|";
	public static final String SPIDERTAG_URLREPOSITORY_CLOSE = "|[SPIDERTAG_URLREPOSITORY_CLOSE]|";
	public static final String SPIDERTAG_WEBSITE_CLOSE = "|[SPIDERTAG_WEBSITE_CLOSE]|";
	public static final String SPIDERTAG_TBID_OPEN = "|[SPIDERTAG_TBID_OPEN]|";
	public static final String SPIDERTAG_TBID_CLOSE = "|[SPIDERTAG_TBID_CLOSE]|";
	public static final String SPIDERTAG_PREDICTION_CLOSE = "|[SPIDERTAG_PREDICTION_CLOSE]|";
	public static final String SPIDERTAG_PREDICTION_OPEN = "|[SPIDERTAG_PREDICTION_OPEN]|";

	int DATASOURCE_GENERIC_JOB_POST = 0;
	int DATASOURCE_JOB_DESCRIPTION = 1;
	int DATASOURCE_CHINESE_JOB_POST = 2;
	int DATASOURCE_DIPLOMALEVEL_JOB_POST = 3;
	int DATASOURCE_COLLEGE_PROGRAM = 4;

	public static final String BLOG_SUBTYPE = "REDDIT_SUBTYPE";
	public static final String JOB_ANALYSIS_SUBTYPE_DEGREE = "DEGREE_LEVEL";
	public static final String JOB_ANALYSIS_SUBTYPE_CHINESEDEGREE = "CHINESEDEGREE_LEVEL";
	public static final String JOB_ANALYSIS_SUBTYPE_DIPLOMA = "DIPLOMA_LEVEL";
	public static final String COLLEGE_ANALYSIS_SUBTYPE_DIPLOMA = "COLLEGE_DIPLOMA_LEVEL";
	public static final String JOB_DESCRIPTION_ANALYSIS_SUBTYPE = "JOB_DESCRIPTION_ANALYSIS_DEGREE_LEVEL";
}
