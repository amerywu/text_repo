package ikoda.persistence.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationContextProvider
{
	
	private static JobApplicationContextProvider jobApplicationContextProvider;
	
	@Autowired
	private JobAnalysisService jobAnalysisService;
	
	@Autowired
	private JobReportingByIkodaDegreeService jobReportingByIkodaDegreeService;

	public JobApplicationContextProvider()
	{
		jobApplicationContextProvider=this;
		
	}
	
	public static JobApplicationContextProvider getInstance()
	{
		if(null==jobApplicationContextProvider)
		{
			jobApplicationContextProvider= new JobApplicationContextProvider();
		}
		return jobApplicationContextProvider;
	}

	public JobAnalysisService getJobAnalysisService()
	{
		return jobAnalysisService;
	}

	public void setJobAnalysisService(JobAnalysisService jobAnalysisService)
	{
		this.jobAnalysisService = jobAnalysisService;
	}

	public JobReportingByIkodaDegreeService getJobReportingByIkodaDegreeService()
	{
		return jobReportingByIkodaDegreeService;
	}

	public void setJobReportingByIkodaDegreeService(JobReportingByIkodaDegreeService jobReportingByIkodaDegreeService)
	{
		this.jobReportingByIkodaDegreeService = jobReportingByIkodaDegreeService;
	}
	
	

}
