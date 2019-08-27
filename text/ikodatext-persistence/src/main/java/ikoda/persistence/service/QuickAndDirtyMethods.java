package ikoda.persistence.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ikoda.persistence.application.PLog;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.dao.QuickAndDirtyImpl;
import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.CountDegreeJob;
import ikoda.persistence.model.CountJobTitleByDegreeName;
import ikoda.persistence.model.Job;

@Service("quickAndDirtyMethods")
public class QuickAndDirtyMethods
{
	@Autowired
	private QuickAndDirtyImpl quickAndDirtyImpl;

	public QuickAndDirtyMethods()
	{
		// TODO Auto-generated constructor stub
	}

	public void doQdTasks()
	{
		PLog.getChoresLogger().warn("\n\n\n\nRUNNING QUICK AND DIRTY METHODS\n\n\n\n");
		try
		{
			List<CountDegree> cdList = quickAndDirtyImpl.getAllCountDegrees();
			int cdProcessedCount = 0;
			for (CountDegree cd : cdList)
			{
				cdProcessedCount++;
				PLog.getChoresLogger()
						.info("\n\n\n\nCountDegree is " + cd.getSpecifiedDegreeName() + " id is " + cd.getId());
				List<CountJobTitleByDegreeName> cjtbdList = quickAndDirtyImpl.getCountJobTitleByDegreeNames(cd.getId());
				if (cjtbdList.size() == 0)
				{
					continue;
				}
				List<Long> ujidList = new ArrayList<Long>();

				for (CountJobTitleByDegreeName cjtbd : cjtbdList)
				{
					PLog.getChoresLogger().info("cjtbd CountDegree is " + cjtbd.getDegreeName());
					PLog.getChoresLogger().info("cjtbd uj is " + cjtbd.getUniqueJob().getFriendlyName());
					ujidList.add(cjtbd.getUniqueJob().getId());

				}

				for (Long ujid : ujidList)
				{
					List<Job> jobList = quickAndDirtyImpl.getListFromJobByUjid(ujid);
					PLog.getChoresLogger().info("jobList size " + jobList.size());
		
					int count = 0;
					for (Job j : jobList)
					{
						if (j.containsAreaOfStudy(cd.getSpecifiedDegreeName()))
						{
							PLog.getChoresLogger()
									.info("Job match on " + cd.getSpecifiedDegreeName() + " for " + j.getJobTitle());
							;
							CountDegreeJob cdj = new CountDegreeJob();
							cdj.setCountDegree(cd);
							cdj.setUniqueJob(j.getUniqueJob());
							cdj.setJob(j);
							PLog.getChoresLogger().info("Job process status " + j.isByExtrapolation());
							;
							if (j.isByExtrapolation().booleanValue() == false)
							{
								if (!quickAndDirtyImpl.countDegreeJobExists(cdj))
								{
									quickAndDirtyImpl.saveCountDegreeJob(cdj);
									j.setByExtrapolation(true);
									quickAndDirtyImpl.updateJob(j);
								}
							}
							else
							{
								PLog.getChoresLogger().info("...already processed");
							}
						}
						count++;
						if (count % 100 == 0)
						{
							PLog.getChoresLogger().info("Processed: " + count);
							;
						}
					}

					
				}//end ujidlist for cd
				PLog.getChoresLogger().info("cdProcessedCount: " + cdProcessedCount);
			}//end cd

		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}

	}

	public List<Job> getListFromJob(List<Long> ujIds, String region, String degreeName) throws PersistenceException
	{
		return quickAndDirtyImpl.getListFromJob(ujIds, region, degreeName);
	}

	public List<Job> getListFromJob1(List<Long> ujIds, String region) throws PersistenceException
	{
		PLog.getChoresLogger().info("getListFromJob1");
		return quickAndDirtyImpl.getListFromJob1(ujIds, region);
	}

}
