package ikoda.persistence.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.netio.config.MajorMappingConfig;

import ikoda.persistence.application.PLog;
import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.CountDegreeJob;
import ikoda.persistence.model.CountJobTitle;
import ikoda.persistence.model.CountJobTitleByDegreeName;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.UniqueJob;
import ikoda.utils.ProcessStatus;

@Service("persistenceMethods")
public class PersistenceMethods
{
	@Autowired
	private JobAnalysisService jobAnalysisService;

	private StringBuffer degreeMatchingLog = new StringBuffer();

	public PersistenceMethods()
	{
		// TODO Auto-generated constructor stub
	}

	public void checkRelations(CountDegree specifiedDegree, MajorMappingConfig majorMap)
	{
		try
		{
			PLog.getLogger().debug("Relations configured for " + specifiedDegree.getSpecifiedDegreeName());
			List<String> configuredIkodaRelations = majorMap.specifiedMajorMap()
					.get(specifiedDegree.getSpecifiedDegreeName());
			for (String ikodaDegreeName : configuredIkodaRelations)
			{
				PLog.getLogger().debug(ikodaDegreeName);
				degreeMatchingLog.append("\n\t");
				degreeMatchingLog.append(ikodaDegreeName);

				if (!specifiedDegree.containsRelatedIkodaDegree(ikodaDegreeName))
				{
					PLog.getLogger().debug("\n\nNo db relation");
					degreeMatchingLog.append("\n\n No db relation for ");
					degreeMatchingLog.append(specifiedDegree.getSpecifiedDegreeName());
					degreeMatchingLog.append(" and ");
					degreeMatchingLog.append(ikodaDegreeName);
					createRelation(specifiedDegree, ikodaDegreeName);
				}
			}

			//// now see if we need to delete any relations from db
			Iterator<IkodaDegreeSpecifiedDegree> itr = specifiedDegree.getIkodaDegreeSpecifiedDegreeRelations()
					.iterator();
			while (itr.hasNext())
			{
				IkodaDegreeSpecifiedDegree idsd = itr.next();
				if (!configuredIkodaRelations.contains(idsd.getIkodaDegree().getIkodaDegreeName()))
				{

					degreeMatchingLog.append("\n\n No configured relation for ");
					degreeMatchingLog.append(specifiedDegree.getSpecifiedDegreeName());
					degreeMatchingLog.append(" and ");
					degreeMatchingLog.append(idsd.getIkodaDegree().getIkodaDegreeName());
					degreeMatchingLog.append("\nRemoving from persistence ");
					PLog.getLogger().debug("\nRemoving from persistence " + idsd.getIkodaDegree().getIkodaDegreeName()
							+ " " + specifiedDegree.getSpecifiedDegreeName());
					itr.remove();
				}
			}
			PLog.getLogger().debug("Updating " + specifiedDegree.getSpecifiedDegreeName());
			jobAnalysisService.updateCountDegree(specifiedDegree);

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	public void countDegree(Job job)
	{
		try
		{
			PLog.getLogger().info("count Degree");
			if (job.getDegreeLevel().equals(CountDegree.DEGREELEVEL)
					||job.getDegreeLevel().equals(CountDegree.CERTIFICATEANDDEGREELEVEL))
			{
				for (String specifiedDegreeName : job.getAreasOfStudy())
				{
					countDegreeSubProcess(job, specifiedDegreeName, CountDegree.DEGREELEVEL);
					
					
				}
			}

			if (job.getDegreeLevel().equals(CountDegree.CERTIFICATELEVEL)
					||job.getDegreeLevel().equals(CountDegree.CERTIFICATEANDDEGREELEVEL))
			{
				for (String specifiedDegreeName : job.getCertifications())
				{
					countDegreeSubProcess(job, specifiedDegreeName, CountDegree.CERTIFICATELEVEL);
				}
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	public void countDegreeSubProcess(Job job, String specifiedDegreeName, String degreeLevel)
	{
		try
		{
			CountDegree counter = jobAnalysisService.getCountDegree(specifiedDegreeName, degreeLevel);
			PLog.getLogger().debug("Updating "+specifiedDegreeName+" "+ degreeLevel);
			if (null == counter)
			{
				counter = new CountDegree();
				counter.setCount(new Integer(1));
				counter.setSpecifiedDegreeName(specifiedDegreeName);
				counter.setDegreeLevel(degreeLevel);
				jobAnalysisService.saveCountDegree(counter);
				PLog.getLogger().info("count Degree: saving new " + counter);
			}
			else
			{
				Integer count = counter.getCount();
				Integer newCount = new Integer(count.intValue() + 1);
				counter.setCount(newCount);
				counter.setDegreeLevel(degreeLevel);
				jobAnalysisService.updateCountDegree(counter);
				PLog.getLogger().debug("count Degree: updating " + job.getJobTitle() + " " + counter.getCount());
			}
			
			CountDegreeJob cdj = new CountDegreeJob();
			cdj.setCountDegree(counter);
			cdj.setJob(job);
			cdj.setUniqueJob(job.getUniqueJob());
			PLog.getLogger().debug("Saving countDegreeJob " + job.getId() );
			jobAnalysisService.saveCountDegreeJob(cdj);
		}
		catch (Exception e)
		{
			ProcessStatus.incrementStatus("Persist Failed on countDegreeSubProcess");
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	public void countJobTitle(Job job, String region)
	{
		try
		{
			UniqueJob uj = jobAnalysisService.getUniqueJob(uniqueJobTitle(job.getJobTitle(), region));
			if (null == uj)
			{
				uj = new UniqueJob();
				uj.setFriendlyName(job.getJobTitle());
				uj.setUniqueJob(uniqueJobTitle(job.getJobTitle(), region));
				jobAnalysisService.saveUniqueJob(uj);
				
			}

			CountJobTitle counter = jobAnalysisService.getCountJobTitle(uj.getId());
			if (null == counter)
			{
				counter = new CountJobTitle();
				counter.setCount(new Integer(1));
				counter.setJobTitle(job.getJobTitle());
				counter.setUniqueJob(uj);

				jobAnalysisService.saveCountJobTitle(counter);
			}
			else
			{
				Integer count = counter.getCount();
				Integer newCount = new Integer(count.intValue() + 1);
				counter.setCount(newCount);
				jobAnalysisService.updateCountJobTitle(counter);
			}
			job.setUniqueJob(uj);

		}
		catch (Exception e)
		{
			ProcessStatus.incrementStatus("Persist Failed on countJobTitle");
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	public void countJobTitleByDegree(Job job)
	{
		try
		{
			for (String degreeName : job.getAreasOfStudy())
			{
				countJobTitleByDegreeSubProcess(job, degreeName, CountDegree.DEGREELEVEL);
			}
			for (String degreeName : job.getCertifications())
			{
				countJobTitleByDegreeSubProcess(job, degreeName, CountDegree.CERTIFICATELEVEL);
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	public void countJobTitleByDegreeSubProcess(Job job, String degreeName, String degreeLevel)
	{
		try
		{
			CountDegree specifiedDegree = jobAnalysisService.getCountDegree(degreeName, degreeLevel);
			
			PLog.getLogger().debug("job.getUniqueJob().getId(): "+job.getUniqueJob().getId());
			PLog.getLogger().debug("specifiedDegree.getId(): "+specifiedDegree.getId());
			
			CountJobTitleByDegreeName counter = jobAnalysisService
					.getCountJobTitleByDegreeName(job.getUniqueJob().getId(), specifiedDegree.getId());
			if (null == counter)
			{
				counter = new CountJobTitleByDegreeName();
				counter.setCount(new Integer(1));
				counter.setCountSinceMedianUpdate(new Integer(1));
				counter.setJobTitle(job.getJobTitle());
				counter.setDegreeName(degreeName);
				counter.setUniqueJob(job.getUniqueJob());
				counter.setCountDegree(specifiedDegree);
				counter.setDegreeLevel(specifiedDegree.getDegreeLevel());
				jobAnalysisService.saveCountJobTitleByDegreeName(counter);
			}
			else
			{
				Integer count = counter.getCount();
				Integer newCount = new Integer(count.intValue() + 1);
				counter.setCount(newCount);
				counter.setDegreeLevel(specifiedDegree.getDegreeLevel());

				Integer countSinceUpdate = counter.getCountSinceMedianUpdate();
				if (null == countSinceUpdate)
				{
					countSinceUpdate = new Integer(0);
				}
				Integer newCountSinceUpdate = new Integer(countSinceUpdate.intValue() + 1);
				counter.setCountSinceMedianUpdate(newCountSinceUpdate);

				jobAnalysisService.updateCountJobTitleByDegreeName(counter);
			}
		}
		catch (Exception e)
		{
			ProcessStatus.incrementStatus("Persist Failed on countJobTitleByDegree");
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	public CountDegree createCountDegree(String specifiedDegreeName, String degreeLevel)
	{
		try
		{
			CountDegree counter = new CountDegree();
			counter.setCount(new Integer(1));
			counter.setSpecifiedDegreeName(specifiedDegreeName);
			counter.setDegreeLevel(degreeLevel);

			jobAnalysisService.saveCountDegree(counter);
			degreeMatchingLog.append("\nCreated " + specifiedDegreeName);
			return counter;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}

	}

	public IkodaDegree createIkodaDegree(String ikodaDegreeName)
	{
		try
		{
			IkodaDegree ik = new IkodaDegree();
			ik.setIkodaDegreeName(ikodaDegreeName);

			jobAnalysisService.saveIkodaDegreee(ik);
			degreeMatchingLog.append("\nCreated " + ikodaDegreeName);
			return ik;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}

	}

	public void createRelation(CountDegree specifiedDegree, String ikodaDegreeName)
	{
		try
		{
			IkodaDegree ikodaDegree = jobAnalysisService.getIkodaDegree(ikodaDegreeName);
			IkodaDegreeSpecifiedDegree idsd = new IkodaDegreeSpecifiedDegree();
			idsd.setSpecifiedDegree(specifiedDegree);
			idsd.setIkodaDegree(ikodaDegree);
			PLog.getLogger()
					.debug("adding new idsd " + ikodaDegreeName + " " + specifiedDegree.getSpecifiedDegreeName());
			boolean b = specifiedDegree.addIkodaDegreeSpecifiedDegreeRelation(idsd);
			degreeMatchingLog.append("\n added relation status =" + b);

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			degreeMatchingLog.append("\n added relation failed. " + e.getMessage());
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void initalizeIkodaDegrees(MajorMappingConfig majorMap) throws Exception
	{
		try
		{
			Iterator<String> itr = majorMap.ikodaMajorMap().keySet().iterator();
			while (itr.hasNext())
			{
				String ikodaDegreeName = itr.next();
				PLog.getLogger().debug("ikodaDegreeName: " + ikodaDegreeName);
				IkodaDegree ikodaDegree = jobAnalysisService.getIkodaDegree(ikodaDegreeName);
				if (null == ikodaDegree)
				{
					degreeMatchingLog.append("\nNo IkodaDegree database entry for " + ikodaDegreeName);
					PLog.getLogger().debug("No IkodaDegree database entry for " + ikodaDegreeName);
					ikodaDegree = createIkodaDegree(ikodaDegreeName);
					if (null == ikodaDegree)
					{
						degreeMatchingLog.append("\nFailed to create entry for " + ikodaDegreeName);
						continue;
					}
				}
			}

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	public void initalizeRelations(MajorMappingConfig majorMap) throws Exception
	{
		try
		{
			PLog.getLogger().info("initalizeIkodaDegrees");
			initalizeIkodaDegrees(majorMap);
			PLog.getLogger().info("initalizeSpecifiedDegrees");
			initalizeSpecifiedDegrees(majorMap);

			PLog.getLogger().info("initializeUniqueJobIdList");

			PLog.getLogger().info("initializeCountDegreeIdList");
			// initializeCountDegreeIdList();
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	public void initalizeSpecifiedDegrees(MajorMappingConfig majorMap) throws Exception
	{
		try
		{
			Iterator<String> itr = majorMap.specifiedMajorMap().keySet().iterator();
			while (itr.hasNext())
			{
				String specifiedDegreeName = itr.next();
				PLog.getLogger().debug("specifiedDegreeName: " + specifiedDegreeName);
				degreeMatchingLog.append("\n\n\n");
				degreeMatchingLog.append(specifiedDegreeName);

				//// now a specified degree name can be at different levels,
				//// i.e., certificate and bachelors
				for (String degreelevel : majorMap.getDegreeLevelsForSpecifiedDegreeName(specifiedDegreeName))
				{
					CountDegree specifiedDegree = jobAnalysisService.getCountDegree(specifiedDegreeName, degreelevel);
					if (null == specifiedDegree)
					{
						degreeMatchingLog.append("\nNo database entry for " + specifiedDegreeName);
						specifiedDegree = createCountDegree(specifiedDegreeName, degreelevel);
						if (null == specifiedDegree)
						{
							degreeMatchingLog.append("\nFailed to create entry for " + specifiedDegreeName);
							continue;
						}
					}
					checkRelations(specifiedDegree, majorMap);
				}
			}
			Files.write(Paths.get("./degreeRelationsLog" + System.currentTimeMillis() + ".log"),
					degreeMatchingLog.toString().getBytes());
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}

	public String uniqueJobTitle(String jobTitle, String region)
	{
		if (region.toUpperCase().contains("ZH"))
		{
			return jobTitle;
		}

		String s0 = jobTitle.toUpperCase();
		String s1 = s0.replaceAll("[^A-Za-z0-9]", "");
		return s1;
	}

}
