package ikoda.persistence.service;

import org.springframework.stereotype.Service;

import ikoda.persistence.application.PLog;

@Service("cpcReportingServices")
public class CPCReportingServices
{

	public CPCReportingServices()
	{
		// TODO Auto-generated constructor stub
	}

	public int calculateJobPrevalence(long allJobsInTheWorldCount, Integer thisJobcount)
	{

		PLog.getRLogger().debug("count: " + thisJobcount);
		PLog.getRLogger().debug("allJobsInTheWorldCount: " + allJobsInTheWorldCount);

		double dcount = (double) thisJobcount;
		double dworld = (double) allJobsInTheWorldCount;

		double percentOfAll = dcount / dworld;
		int rank = 7;

		if (percentOfAll > InterfaceReportingParameters.THRESHOLD_RANK1)
		{
			rank = 1;
		}
		else if (percentOfAll > InterfaceReportingParameters.THRESHOLD_RANK2)
		{
			rank = 2;
		}
		else if (percentOfAll > InterfaceReportingParameters.THRESHOLD_RANK3)
		{
			rank = 3;
		}
		else if (percentOfAll > InterfaceReportingParameters.THRESHOLD_RANK4)
		{
			rank = 4;
		}
		else if (percentOfAll > InterfaceReportingParameters.THRESHOLD_RANK5)
		{
			rank = 5;
		}
		else
		{
			rank = 6;
		}

		PLog.getRLogger().debug("\n\n\nproportion: " + percentOfAll + " rank: " + rank);
		return rank;
	}

	public boolean isOverDegreePrevalenceThreshold(Double averageJobCount, Integer thisJobcount)
	{
		
		/*THis happens when a new region is populating*/
		if(null==averageJobCount||null==thisJobcount)
		{
			return false;
		}

		PLog.getRLogger().debug("averageJobCount " + averageJobCount);
		PLog.getRLogger().debug("this.count " + thisJobcount);
		//PLog.getRLogger().debug("threshold is " + averageJobCount * 0.35);
		if (thisJobcount >= (averageJobCount * 0.7))
		{
			// PLog.getRLogger().debug("over threshold ");
			return true;
		}
		// PLog.getRLogger().debug("under threshold ");
		return false;
	}
}