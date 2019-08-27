package ikoda.netio;

import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.spiders.AbstractWebSite;
import ikoda.netio.spiders.BCJobBank;
import ikoda.netio.spiders.CareerBuilder;
import ikoda.netio.spiders.CollegeBmccCuny;
import ikoda.netio.spiders.CollegeCorningcc;
import ikoda.netio.spiders.CollegePrograms;
import ikoda.netio.spiders.Indeed;
import ikoda.netio.spiders.IndeedAU;
import ikoda.netio.spiders.IndeedCA;
import ikoda.netio.spiders.IndeedCADiploma;
import ikoda.netio.spiders.IndeedUK;
import ikoda.netio.spiders.IndeedUSDiploma;
import ikoda.netio.spiders.Reddit;
import ikoda.netio.spiders.SimplyHired;
import ikoda.netio.spiders.ZhaoPing;

public class WebSiteFactory
{

	public static final String BCJOBBANK = "BCJOBBANK";
	public static final String SIMPLYHIRED = "SIMPLYHIRED";
	public static final String CAREERBUILDER = "CAREERBUILDER";
	public static final String INDEED = "INDEED";
	public static final String INDEEDCA = "INDEEDCA";
	public static final String INDEED_CA_DIPLOMA = "INDEED_CA_DIPLOMA";
	public static final String INDEED_US_DIPLOMA = "INDEED_US_DIPLOMA";
	public static final String INDEEDUK = "INDEEDUK";
	public static final String INDEEDAU = "INDEEDAU";
	public static final String ZHAOPING = "ZHAOPING";
	public static final String COLLEGE_PROGRAMS = "COLLEGE_PROGRAMS";
	public static final String COLLEGE_BMCC_CUNY = "COLLEGE_BMCC_CUNY";
	public static final String COLLEGE_CORNINGCC = "COLLEGE_CORNINGCC";
	public static final String REDDIT = "REDDIT";

	private static final String chineseTypes[] =
	{ ZHAOPING };
	private static final String englishTypes[] =
	{ SIMPLYHIRED, INDEED, SIMPLYHIRED, INDEEDCA, INDEEDUK, INDEEDAU, CAREERBUILDER };
	private static final String englishDiplomaTypes[] =
	{ INDEED_US_DIPLOMA, INDEED_CA_DIPLOMA };
	private static final String englishCollegeTypes[] =
	{ COLLEGE_PROGRAMS, COLLEGE_BMCC_CUNY, COLLEGE_CORNINGCC };

	private static final String blogTypes[] =
	{ REDDIT };

	private static boolean checkAnalysisSubType(String subType, String job)
	{
		if (null == subType)
		{
			NioLog.getLogger().warn("\n\nNo subtype. ABORTING call. This may throw a handled exception\n\n");
		}

		if (jobTypeEquals(subType, job))
		{
			return true;
		}
		NioLog.getLogger().warn("\n\nWrong subtype (" + subType + ") for " + job
				+ ". ABORTING call. This may throw a handled exception\n\n");
		return false;

	}

	public static AbstractWebSite getWebSite(String job, String analysisSubType)
	{
		if (!checkAnalysisSubType(analysisSubType, job))
		{
			return null;
		}

		if (job.equals(BCJOBBANK))
		{

			return new BCJobBank();
		}
		if (job.equals(INDEED))
		{
			return new Indeed();
		}
		if (job.equals(INDEEDCA))
		{
			return new IndeedCA();
		}
		if (job.equals(INDEEDUK))
		{
			return new IndeedUK();
		}
		if (job.equals(SIMPLYHIRED))
		{
			return new SimplyHired();
		}
		if (job.equals(CAREERBUILDER))
		{
			return new CareerBuilder();
		}
		if (job.equals(INDEEDAU))
		{
			return new IndeedAU();
		}
		if (job.equals(ZHAOPING))
		{
			return new ZhaoPing();
		}
		if (job.equals(INDEED_US_DIPLOMA))
		{
			return new IndeedUSDiploma();
		}
		if (job.equals(INDEED_CA_DIPLOMA))
		{
			return new IndeedCADiploma();
		}
		if (job.equals(COLLEGE_PROGRAMS))
		{
			return new CollegePrograms();
		}
		if (job.equals(COLLEGE_BMCC_CUNY))
		{
			return new CollegeBmccCuny();
		}
		if (job.equals(COLLEGE_CORNINGCC))
		{
			return new CollegeCorningcc();
		}
		if (job.equals(REDDIT))
		{
			return new Reddit();
		}
		return null;
	}

	private static boolean jobTypeEquals(String subType, String jobType)
	{

		NioLog.getLogger().debug("\n\nsubtype: " + subType + " jobType: " + jobType);

		if (subType.equals(StringConstantsInterface.JOB_ANALYSIS_SUBTYPE_CHINESEDEGREE))
		{
			for (int i = 0; i < chineseTypes.length; i++)
			{
				if (chineseTypes[i].equals(jobType))
				{
					return true;
				}

			}
		}
		else if (subType.equals(StringConstantsInterface.JOB_ANALYSIS_SUBTYPE_DEGREE))
		{
			for (int i = 0; i < englishTypes.length; i++)
			{

				if (englishTypes[i].equals(jobType))
				{
					return true;
				}

			}
		}
		else if (subType.equals(StringConstantsInterface.JOB_ANALYSIS_SUBTYPE_DIPLOMA))
		{
			for (int i = 0; i < englishDiplomaTypes.length; i++)
			{
				if (englishDiplomaTypes[i].equals(jobType))
				{

					return true;
				}

			}
		}
		else if (subType.equals(StringConstantsInterface.BLOG_SUBTYPE))
		{
			NioLog.getLogger().debug("REDDIT_SUBTYPE with job type " + jobType);

			for (int i = 0; i < blogTypes.length; i++)
			{
				NioLog.getLogger().debug(blogTypes[i]);
				if (blogTypes[i].equals(jobType))
				{

					return true;
				}

			}
		}
		else if (subType.equals(StringConstantsInterface.COLLEGE_ANALYSIS_SUBTYPE_DIPLOMA))
		{
			NioLog.getLogger().debug("COLLEGE_ANALYSIS_SUBTYPE_DIPLOMA with job type " + jobType);

			for (int i = 0; i < englishCollegeTypes.length; i++)
			{
				// NioLog.getLogger().debug(englishCollegeTypes[i]);
				if (englishCollegeTypes[i].equals(jobType))
				{

					return true;
				}

			}
		}

		NioLog.getLogger().warn("\n\nWrong subtype (" + subType + ") for " + jobType
				+ ". ABORTING call. This may throw a handled exception\n\n");
		return false;

	}

	public WebSiteFactory()
	{
		// TODO Auto-generated constructor stub
	}

}
