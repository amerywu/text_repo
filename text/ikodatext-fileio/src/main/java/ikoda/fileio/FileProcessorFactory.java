package ikoda.fileio;

import java.nio.file.Path;

import ikoda.netio.config.ConfigurationBeanParent;

public class FileProcessorFactory
{

	public final static String BCJOBBANK = "BCJOBBANK";
	public final static String INDEED = "INDEED";
	public final static String INDEED_US_DIPLOMA = "INDEED_US_DIPLOMA";
	public final static String INDEED_CA_DIPLOMA = "INDEED_CA_DIPLOMA";
	public final static String INDEEDCA = "INDEEDCA";
	public final static String INDEEDUK = "INDEEDUK";
	public final static String INDEEDAU = "INDEEDAU";
	public final static String SIMPLYHIRED = "SIMPLYHIRED";
	public final static String CAREERBUILDER = "CAREERBUILDER";
	public final static String ZHAOPING = "ZHAOPING";
	public final static String COLLEGE_PROGRAMS = "COLLEGE_PROGRAMS";
	public final static String COLLEGE_BMCC_CUNY = "COLLEGE_BMCC_CUNY";
	public final static String COLLEGE_ = "COLLEGE_";
	public final static String REDDIT = "REDDIT";
	
	
	
	
	
	public static AbstractFileProcessor getProcessor(Path p,ConfigurationBeanParent config)
	{
		////////////////////////////////
		// this has to be first
		/////////////////////////
		FioLog.getLogger().debug(p.getFileName().toString());
		if (p.getFileName().toString().contains(FileProcessorFactory.INDEEDCA))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.INDEEDCA,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.INDEEDUK))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.INDEEDUK,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.INDEEDAU))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.INDEEDAU,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.INDEED))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.INDEED,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.BCJOBBANK))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.BCJOBBANK,config);
		}

		else if (p.getFileName().toString().contains(FileProcessorFactory.SIMPLYHIRED))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.SIMPLYHIRED,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.CAREERBUILDER))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.CAREERBUILDER,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.ZHAOPING))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.ZHAOPING,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.COLLEGE_PROGRAMS))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.COLLEGE_PROGRAMS,config);
		}
		else if (p.getFileName().toString().contains(FileProcessorFactory.COLLEGE_BMCC_CUNY))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.COLLEGE_PROGRAMS,config);
		}
        else if (p.getFileName().toString().contains(FileProcessorFactory.REDDIT))
        {
            return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.REDDIT,config);
        }
		
		/**GENERIC   KEEP AT BOTTOM*/
		else if (p.getFileName().toString().contains(FileProcessorFactory.COLLEGE_))
		{
			return FileProcessorFactory.getProcessorForDataSource(FileProcessorFactory.COLLEGE_PROGRAMS,config);
		}
		else
		{
			return null;
		}
	}
	
	
	
	private static AbstractFileProcessor getProcessorForDataSource(String job,ConfigurationBeanParent config)
	{
		if(job.equals(BCJOBBANK))
		{
			return new BCJobBank(config);
		}
		else if(job.equals(INDEED))
		{
			return new Indeed(config);
		}
		else if(job.equals(INDEED_US_DIPLOMA))
		{
			return new IndeedUSDiploma(config);
		}
		else if(job.equals(INDEED_CA_DIPLOMA))
		{
			return new IndeedCADiploma(config);
		}
		else if(job.equals(INDEEDCA))
		{
			return new IndeedCA(config);
		}
		else if(job.equals(INDEEDAU))
		{
			return new IndeedAU(config);
		}
		else if(job.equals(INDEEDUK))
		{
			return new IndeedUK(config);
		}
		else if(job.equals(SIMPLYHIRED))
		{
			return new SimplyHired(config);
		}
		else if(job.equals(CAREERBUILDER))
		{
			return new CareerBuilder(config);
		}
		else if(job.equals(ZHAOPING))
		{
			return new ZhaoPing(config);
		}
		else if(job.equals(COLLEGE_PROGRAMS))
		{
			return new CollegePrograms(config);
		}
        else if(job.equals(REDDIT))
        {
            return new Reddit(config);
        }
		return null;
	}
	
	public FileProcessorFactory()
	{
		// TODO Auto-generated constructor stub
	}

}
