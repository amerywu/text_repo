package ikoda.persistence.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.MajorMappingConfig;
import ikoda.persistence.application.PLog;
import ikoda.persistence.application.PersistenceException;
import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.model.IkodaDegreeSpecifiedDegree;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.reporting.CPCJob;
import ikoda.persistence.model.reporting.CPCPayByJobTitleAndDegree;
import ikoda.persistence.model.reporting.CPCSalaryByDegree;

@Service("jobReportingManager")
public class JobReportingManager
{

	private Map<String, Double> averageCountByRegion = new HashMap<String, Double>();

	private boolean abort = false;

	@Autowired
	private CPCReportingServices cpcReportingServices;

	@Autowired
	private JobAnalysisService jobAnalysisService;

	@Autowired
	private JobReportingByIkodaDegreeService jobReportingService;

	private Map<String, Long> totalCountJobsMap = new HashMap<String, Long>();

	private Map<String, Double> averageCountJobsMap = new HashMap<String, Double>();

	private List<String> availableRegions = new ArrayList<String>();

	public JobReportingManager() throws PersistenceException
	{

	}

	private void calculateAverageCount(List<ValueSalaryByDegreeAndJobTitle> vsdjtList1)
	{

		try
		{
			HashMap<String, List<ValueSalaryByDegreeAndJobTitle>> hm = new HashMap<String, List<ValueSalaryByDegreeAndJobTitle>>();

			/// divide by region
			for (ValueSalaryByDegreeAndJobTitle vsdjt : vsdjtList1)
			{
				List<ValueSalaryByDegreeAndJobTitle> list = hm.get(vsdjt.getRegion());

				if (null == list)
				{
					list = new ArrayList<ValueSalaryByDegreeAndJobTitle>();
					hm.put(vsdjt.getRegion(), list);
				}
				list.add(vsdjt);

			}

			// set average for region
			Iterator<String> itr = hm.keySet().iterator();
			while (itr.hasNext())
			{
				String key = itr.next();
				List<ValueSalaryByDegreeAndJobTitle> listByRegion = hm.get(key);
				// PLog.getRLogger().debug(key + " " + listByRegion.size());
				Collections.sort(listByRegion, new ComparatoByVsdjtCount());

				double d = listByRegion.stream().filter(vsdjt -> vsdjt.getCount() > 0)
						.mapToInt(ValueSalaryByDegreeAndJobTitle::getCount).average().getAsDouble();
				averageCountByRegion.put(generateKeyForAverageCountByRegion(listByRegion.get(0)), new Double(d));

			}
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
		}
	}

	private String generateCPCSalaryByDegreeKey(Long countDegreeId, String region)
	{
		return countDegreeId + "_" + region;
	}

	private String generateKeyForAverageCountByRegion(ValueSalaryByDegreeAndJobTitle vsdjt)
	{
		try
		{
			String s = vsdjt.getRegion() + "_" + vsdjt.getDegreeName();
			// PLog.getRLogger().debug(s);
			return s;
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			return "";
		}
	}

	private String generateKeyForCPCPayByJobTitle(Long countDegreeId, Long uniqueJobId, String region,
			Long ikodaDegreeId)
	{
		return "DegreeId" + countDegreeId + "_ujid_" + uniqueJobId + "_" + region + "_ikodadegree_" + ikodaDegreeId;
	}

	private String generateKeyForJobDetailCollection(String ujId, String degreeName, String region)
	{
		return ujId + degreeName + region;
	}

	public List<IkodaDegree> getAllIkodaDegrees() throws PersistenceException
	{
		try
		{
			return jobReportingService.getIkodaDegrees();
		}
		catch (Exception e)
		{
			throw new PersistenceException(e);
		}
	}
	
	private List<ValueSalaryByDegree> mergeSalaryByDegree(List<ValueSalaryByDegree> regionalList, MajorMappingConfig mergeMap, String region )
	{
		try
		{
			PLog.getRLogger().debug("regionalList starts with size "+regionalList.size());
			Map <String,List<ValueSalaryByDegree>>forMergingMap=new HashMap<String,List<ValueSalaryByDegree>>();
			Iterator<ValueSalaryByDegree>itr=regionalList.iterator();
			List<ValueSalaryByDegree>deletedList = new ArrayList<ValueSalaryByDegree>();
			
			
			
			while(itr.hasNext())
			{
				ValueSalaryByDegree vsd=itr.next();
				//PLog.getRLogger().debug(vsd.getDegreeName().toUpperCase());
				//PLog.getRLogger().debug(mergeMap.specifiedMajorMap().get(vsd.getDegreeName().toUpperCase()));
				
				List<String> keyList=mergeMap.specifiedMajorMap().get(vsd.getDegreeName().toUpperCase());
				
				/**If there is more than one entry in the keyList then the configuration is wrong. The major is configured to merge with more
				 * than one other major*/
				if(null!=keyList&&keyList.size()==1)
				{
					List<ValueSalaryByDegree> toBeMergedList=forMergingMap.get(keyList.get(0).toUpperCase());
					if(null==toBeMergedList)
					{
						toBeMergedList=new ArrayList<ValueSalaryByDegree>();
						forMergingMap.put(keyList.get(0).toUpperCase(), toBeMergedList);
					}
					PLog.getRLogger().debug("adding "+vsd.getDegreeName()+" count: "+vsd.getCount()+" medianSalary: "+vsd.getMedianSalary());
					toBeMergedList.add(vsd);
					deletedList.add(vsd);
					itr.remove();
					
				}
			}
			
			Iterator <String> itrForMerging = forMergingMap.keySet().iterator();
			while(itrForMerging.hasNext())
			{
				String key = itrForMerging.next();
				
				PLog.getRLogger().debug(key);
				Optional vsdMasterO=regionalList.stream().filter(vsd ->vsd.getDegreeName().toUpperCase().equals(key.toUpperCase())&vsd.getRegion().equals(region)).findFirst();
				if(!vsdMasterO.isPresent())
				{
					PLog.getRLogger().debug("vsdMasterO not present. ABORTING");
					continue;
				}
				ValueSalaryByDegree vsdMaster= (ValueSalaryByDegree)vsdMasterO.get();
				List<ValueSalaryByDegree>vsdMergees=forMergingMap.get(key);
				if(null==vsdMergees||vsdMergees.size()==0)
				{
					PLog.getRLogger().debug("vsdMergees:"+vsdMergees+"ABORTING");
					continue;
				}
				List<Double>salaryList=new ArrayList<Double>();
				List<Integer>countList=new ArrayList<Integer>();
				countList.add(vsdMaster.getCount());
				salaryList.add(new Double(vsdMaster.getCount().intValue()*vsdMaster.getMedianSalary().intValue()));
				for(ValueSalaryByDegree vsdm: vsdMergees)
				{
					countList.add(vsdm.getCount());
					salaryList.add(new Double(vsdm.getCount().intValue()*vsdm.getMedianSalary().intValue()));
				}
				double averageFromMedians=salaryList.stream().mapToDouble(x->x).sum();
				int count=countList.stream().mapToInt(x->x).sum();
				int newMedian=(int)averageFromMedians/count;
				
				PLog.getRLogger().debug("final median: "+newMedian);
				PLog.getRLogger().debug("final count: "+count);
				
				vsdMaster.setCount(new Integer(count));
				vsdMaster.setMedianSalary(new Integer(newMedian));
				
			}
			
			/**not retaining any in list, so this will be empty*/
			Map<String, String> matchedIds = new HashMap<String, String>();
			removeOutDatedCPCSalaryByDegreeEntries(deletedList, matchedIds, new StringBuffer() );
			
			PLog.getRLogger().debug("regionalList ends with size "+regionalList.size());
			return regionalList;
		}
		catch(Exception e)
		{
			PLog.getRLogger().error(e.getMessage(),e);
			return regionalList;
			
		}
	}

	private List<ValueSalaryByDegree>  initializeMaps(ConfigurationBeanParent config, List<ValueSalaryByDegree> resultList, MajorMappingConfig mergeMap)
	{

		StringBuilder sb = new StringBuilder();
		
		List<ValueSalaryByDegree>prunedList=new ArrayList<ValueSalaryByDegree>();
		
		availableRegions = config.retrieveRegionsForCurrentLocale();

		sb.append("\n\n------------");
		sb.append("\n\nList Size: ");
		sb.append(resultList.size());

		for (String region : availableRegions)
		{
			sb.append("\n\nRegion: " + region);

			List<ValueSalaryByDegree> regionalListUnmerged = removeOutliersAndFilterByRegion(region, resultList);
			List<ValueSalaryByDegree> regionalList=mergeSalaryByDegree(regionalListUnmerged, mergeMap,region);
			prunedList.addAll(regionalList);
			PLog.getRLogger().debug(region);
			Long totalCountJobs = regionalList.stream().mapToLong(ValueSalaryByDegree::getCount).sum();

			OptionalDouble averageCountJobs = regionalList.stream().mapToLong(ValueSalaryByDegree::getCount).average();

			if(averageCountJobs.isPresent())
			{
				sb.append("\ntotalCountJobs " + totalCountJobs);			
				sb.append("\naverageCountJobs " + averageCountJobs.getAsDouble());
				
				PLog.getRLogger().debug(sb);
	
				totalCountJobsMap.put(region, totalCountJobs);
				averageCountJobsMap.put(region, new Double(averageCountJobs.getAsDouble()));
			}
		}
		return prunedList;
	}

	public boolean isAbort()
	{
		return abort;
	}

	private boolean isBlacklisted(String jobTitle)
	{
		for (int i = 0; i < InterfaceReportingFilters.jobTitleBlackList.length; i++)
		{
			if (jobTitle.toUpperCase().contains(InterfaceReportingFilters.jobTitleBlackList[i].toUpperCase()))
			{
				return true;
			}
		}
		return false;
	}

	public List<CPCJob> populateCPCJobDetail(Long ujId, String degreeName, Long countDegreeId, String region,
			Long iKodaDegreeId, String iKodaDegreeName) throws PersistenceException
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			int added = 0;
			int preexisting = 0;
			int failed = 0;

			sb.append("\n\n\n populateJobDetail for " + degreeName + " " + region + " ujid" + ujId + "\n\n\n");

			String key = generateKeyForJobDetailCollection(ujId.toString(), degreeName, region);
			// PLog.getRLogger().debug("loadJobDetail " + key);

			long startTime = System.currentTimeMillis();
			List<CPCJob> cpcJobs = new ArrayList<CPCJob>();
			List<Job> jobs1 = jobAnalysisService.getJobsByUniqueJobId(ujId, 3, degreeName, region);
			List<Job> jobstemp = new ArrayList<Job>();
			jobstemp.addAll(jobs1);

			sb.append("\nloadJobDetail retrieved results of size " + jobstemp.size());
			jobstemp.stream().forEach(job -> sb.append(job.getId() + "  | "));

			// CPCJobsByIkodaDegree jbd =
			// StaticDataSingleton.getInstance().getJobByIkodaDegreeMap().get(degreeName);

			sb.append("looking for job with \ndegreeName: " + degreeName + "\nujId:" + ujId + "\nregion: " + region);

			HashMap<String, String> filter = new HashMap<String, String>();

			Iterator<Job> itr = jobstemp.iterator();
			while (itr.hasNext())
			{
				// sb.append("\nloadJobDetail 21");
				Job job = itr.next();

				if (null == job.getRegion() || !(job.getRegion().toUpperCase().equals(region.toUpperCase())))
				{
					itr.remove();
					sb.append("\nFAIL No match on region....continue");
					failed++;
					continue;
				}
				// sb.append("\nloadJobDetail 22");
				if (!job.containsAreaOfStudy(degreeName))
				{
					sb.append("\nFAIL No match on degree....continue");
					itr.remove();
					failed++;
					continue;
				}
				///// filtering in method to save making hashmap a member
				///// variable
				// sb.append("\nloadJobDetail 23");
				if (null != filter.get(job.getFileId()))
				{
					// it's likely to be duplicate
					sb.append("\nFAIL  likely duplicate a" + job.getFileId());
					itr.remove();
					failed++;
					continue;
				}

				// sb.append("\nloadJobDetail fileid pass 24");
				if (job.getSkills().size() > 0)
				{
					if (null != filter.get(job.getSkills().get(0)))
					{
						sb.append("\nFAIL likely duplicate b " + job.getSkills().get(0));
						itr.remove();
						failed++;
						continue;
					}
				}

				// sb.append("\nloadJobDetail 3 skills size pass");
				// adding filter "+job.getFileId());
				filter.put(job.getFileId(), job.getFileId());

				// sb.append("\nloadJobDetail 4");
				if (job.getSkills().size() > 0)
				{
					filter.put(job.getSkills().get(0), job.getSkills().get(0));
				}
				////////////////////////////////////////

				// sb.append("\nlooking at" + job.toString());
				sb.append("\nlooking at" + job.getJobTitle() + " " + job.getRegion() + " detail count: "
						+ job.getAreasOfStudy().size());
						// sb.append("\nloadJobDetail 5");

				// sb.append("\ngot match, adding to list: " +
				// job.getJobTitle());

				CPCJob cpcJob = jobReportingService.getCPCJob(job.getId(), countDegreeId, job.getUniqueJob().getId(),
						region, iKodaDegreeId);
				if (null != cpcJob)
				{
					sb.append("\nFAIL: Exists in reporting database already " + job.getJobTitle());
					preexisting++;
					continue;
				}
				added++;
				sb.append("\nAdding new " + job.getJobTitle() + " | ID: " + job.getId() + "| DEGREE: " + degreeName);
				cpcJob = new CPCJob();
				cpcJob.setDegreeName(degreeName);
				cpcJob.setContentSource(job.getContentSource());
				cpcJob.setDetailLevel(job.getDetailLevel());
				cpcJob.setJobId(job.getId());
				cpcJob.setJobTitle(job.getJobTitle());
				cpcJob.setRegion(job.getRegion());
				cpcJob.setSalaryEndRange(job.getSalaryEndRange());
				cpcJob.setSalaryStartRange(job.getSalaryStartRange());
				cpcJob.setSkills(new ArrayList<String>(job.getSkills()));
				cpcJob.setCountDegreeId(countDegreeId);
				cpcJob.setUniqueJobId(job.getUniqueJob().getId());
				cpcJob.setYearsExperienceInt(job.getYearsExperienceInt());
				cpcJob.setiKodaDegreeId(iKodaDegreeId);
				cpcJob.setiKodaDegreeName(iKodaDegreeName);
				jobReportingService.saveCPCJob(cpcJob);

			}

			Collections.sort(cpcJobs, new ComparatoByDetailLevel());
			long endTime = System.currentTimeMillis();
			sb.append("\nADDED: " + added + " | PRE-EXISTING: " + preexisting + " | FAILED: " + failed);
			sb.append("\nThat took " + (endTime - startTime) + " milliseconds.");

			PLog.getRLogger().info(sb);
			return cpcJobs;

		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			PLog.getRLogger().equals(sb);
			throw new PersistenceException(e);
		}
	}

	public List<QueueObject> populateCPCPayByJobTitleAndAnyDegree() throws PersistenceException
	{
		try
		{
			CountDegree cd = jobAnalysisService.getCountDegree(InterfaceReportingParameters.ANY_DEGREE,CountDegree.DEGREELEVEL);
			if(null==cd)
			{
				return new ArrayList<QueueObject>();
			}
			int added = 0;
			int updated = 0;
			
			Map<String, String> matchedIds = new HashMap<String, String>();

			StringBuffer sb = new StringBuffer();
			List<QueueObject> cpcJobQueue = new ArrayList<QueueObject>();

			sb.append("\n\n=========populateCPCPayByJobTitleAndAnyDegree ========== \n\n\n");

			/// get all uniquejobs by specified degree with count greater than x

			List<ValueSalaryByDegreeAndJobTitle> vsdjtList = jobReportingService
					.getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(cd.getId(), 4);

			if (null == vsdjtList || vsdjtList.size() == 0)
			{
				PLog.getRLogger().error("\n\nERROR: No matches for " + InterfaceReportingParameters.ANY_DEGREE + " found in database\n\n");
				return new ArrayList<QueueObject>();
			}

			trimVsdjtList(vsdjtList);

			/// calculates the average count for jobs of a given degree
			calculateAverageCount(vsdjtList);

			/// loops through all degree_uniquejobtitles and creates
			/// CPCPayByJobTitleAndDegree
			for (ValueSalaryByDegreeAndJobTitle vsdjt : vsdjtList)
			{
				PLog.getRLogger().debug(" -- :"+vsdjt);
				String key = generateKeyForAverageCountByRegion(vsdjt);
				double averageCount = averageCountByRegion.get(key);
				boolean newEntry = false;
				sb.append("\n\nlooking at job title  " + vsdjt.getJobTitle());
				if (vsdjt.getCount().intValue() > averageCount)
				{
					CPCPayByJobTitleAndDegree cpcJtd = jobReportingService.getCPCPayByJobTitleAndDegree(
							vsdjt.getCountDegree().getId(), vsdjt.getUniqueJob().getId(), vsdjt.getRegion(),
							cd.getId());

					String cpckey = generateKeyForCPCPayByJobTitle(vsdjt.getCountDegree().getId(),
							vsdjt.getUniqueJob().getId(), vsdjt.getRegion(), cd.getId());
					matchedIds.put(cpckey, cpckey);
					if (null == cpcJtd)
					{
						cpcJtd = new CPCPayByJobTitleAndDegree(vsdjt, cd.getId(),
								InterfaceReportingParameters.ANY_DEGREE,cd.getDegreeLevel());
						newEntry = true;
					}
					else
					{
						cpcJtd.update(vsdjt, cd.getId(), InterfaceReportingParameters.ANY_DEGREE,cd.getDegreeLevel());
					}
					CPCSalaryByDegree sbd = jobReportingService.getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(
							vsdjt.getCountDegree().getId(), vsdjt.getRegion());
					if (null != sbd)
					{
						cpcJtd.setRank(sbd.getRank());
					}
					sb.append("\n\n iKodaDegreeName: " + InterfaceReportingParameters.ANY_DEGREE
							+ "\nAverage count for " + key + ": " + averageCount + "\n" + cpcJtd.getDegreeName()
							+ " Count: " + vsdjt.getCount() + " Job: " + vsdjt.getJobTitle() + " Region: "
							+ vsdjt.getRegion());
					if (!isBlacklisted(cpcJtd.getJobTitle()))
					{
						if (newEntry)
						{
							sb.append("\nSaving new\n");
							jobReportingService.saveCPCPayByJobTitleAndDegree(cpcJtd);
							added++;
						}
						else
						{
							sb.append("\nUpdating\n");
							jobReportingService.updateCPCPayByJobTitleAndDegree(cpcJtd);
							updated++;
						}
						if (abort)
						{
							PLog.getRLogger().info("ABORTING...");
							return new ArrayList<QueueObject>();
						}

						QueueObject qo = new QueueObject();
						qo.setRegion(vsdjt.getRegion());
						qo.setiKodaDegreeName(InterfaceReportingParameters.ANY_DEGREE);
						qo.setIkodaDegreeId(cd.getId());
						qo.setUniqueJobId(vsdjt.getUniqueJob().getId());
						qo.setDegreeName(vsdjt.getDegreeName());
						qo.setCountDegreeId(vsdjt.getCountDegree().getId());
						qo.randomizeQueuePosition();
						cpcJobQueue.add(qo);

					}

				}

			}
			removeOutDatedCPCSalaryByDegreeAndJobTitle(vsdjtList, matchedIds, cd.getId(), sb);
			PLog.getRLogger().info("ADDED: " + added + " UPDATED: " + updated);

			sb.append("\n\n=========populateCPCPayByJobTitleAndDegree DONE========== \n\n\n");
			PLog.getRLogger().debug(sb);
			return cpcJobQueue;
		}

		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	public List<QueueObject> populateCPCPayByJobTitleAndDegree(Long iKodaDegreeId, String iKodaDegreeName)
			throws PersistenceException
	{
		try
		{
			int added = 0;
			int updated = 0;

			StringBuffer sb = new StringBuffer();
			List<QueueObject> cpcJobQueue = new ArrayList<QueueObject>();

			sb.append("\n\n=========populateCPCPayByJobTitleAndDegree " + iKodaDegreeName + "========== \n\n\n");

			//// get related specified degrees for the ikoda degree
			List<IkodaDegreeSpecifiedDegree> partList = jobReportingService
					.getIkodaDegreeSpecifiedDegreeByIkodaDegreeId(iKodaDegreeId);

			Map<String, String> matchedIds = new HashMap<String, String>();

			for (IkodaDegreeSpecifiedDegree idsd : partList)
			{
				/// get all uniquejobs by specified degree with count greater
				/// than x

				List<ValueSalaryByDegreeAndJobTitle> vsdjtList = jobReportingService
						.getValueSalaryByDegreeAndJobTitleBySpecifiedDegreeId(idsd.getSpecifiedDegree().getId(), 4);

				if (null == vsdjtList || vsdjtList.size() == 0)
				{
					continue;
				}
				sb.append("\n\n===================\nNEW DEGREE looking at \nIkoda degree: " + iKodaDegreeName
						+ "\nDegree name is " + vsdjtList.get(0).getDegreeName());
				if (iKodaDegreeId.equals(InterfaceReportingParameters.ANY_DEGREE))
				{
					trimVsdjtList(vsdjtList);
				}
				/// calculates the average count for jobs of a given degree
				calculateAverageCount(vsdjtList);

				/// loops through all degree_uniquejobtitles and creates
				/// CPCPayByJobTitleAndDegree
				for (ValueSalaryByDegreeAndJobTitle vsdjt : vsdjtList)
				{
					// PLog.getRLogger().debug(" -- :"+vsdjt);
					CountDegree cd = vsdjt.getCountDegree();
					String key = generateKeyForAverageCountByRegion(vsdjt);
					double averageCount = averageCountByRegion.get(key);
					boolean newEntry = false;
					sb.append("\n\n\n\nlooking at job title  " + vsdjt.getJobTitle());
					if (vsdjt.getCount().intValue() > averageCount)
					{
						CPCPayByJobTitleAndDegree cpcJtd = jobReportingService.getCPCPayByJobTitleAndDegree(
								vsdjt.getCountDegree().getId(), vsdjt.getUniqueJob().getId(), vsdjt.getRegion(),
								idsd.getIkodaDegree().getId());

						String cpckey = generateKeyForCPCPayByJobTitle(vsdjt.getCountDegree().getId(),
								vsdjt.getUniqueJob().getId(), vsdjt.getRegion(), iKodaDegreeId);
						matchedIds.put(cpckey, cpckey);
						if (null == cpcJtd)
						{
							cpcJtd = new CPCPayByJobTitleAndDegree(vsdjt, idsd.getIkodaDegree().getId(),
									iKodaDegreeName,cd.getDegreeLevel());
							newEntry = true;
						}
						else
						{
							cpcJtd.update(vsdjt, idsd.getIkodaDegree().getId(), iKodaDegreeName,cd.getDegreeLevel());
						}
						CPCSalaryByDegree sbd = jobReportingService.getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(
								vsdjt.getCountDegree().getId(), vsdjt.getRegion());
						if (null != sbd)
						{
							cpcJtd.setRank(sbd.getRank());
						}
						sb.append("\n\n iKodaDegreeName: " + iKodaDegreeName + "\nAverage count for " + key + ": "
								+ averageCount + "\n" + cpcJtd.getDegreeName() + " Count: " + vsdjt.getCount()
								+ " Job: " + vsdjt.getJobTitle() + " Region: " + vsdjt.getRegion());
						if (!isBlacklisted(cpcJtd.getJobTitle()))
						{
							if (newEntry)
							{
								sb.append("\nSaving new\n");
								jobReportingService.saveCPCPayByJobTitleAndDegree(cpcJtd);
								added++;
							}
							else
							{
								sb.append("\nUpdating\n");
								jobReportingService.updateCPCPayByJobTitleAndDegree(cpcJtd);
								updated++;
							}
							if (abort)
							{
								PLog.getRLogger().info("ABORTING...");
								return new ArrayList<QueueObject>();
							}

							QueueObject qo = new QueueObject();
							qo.setRegion(vsdjt.getRegion());
							qo.setiKodaDegreeName(iKodaDegreeName);
							qo.setIkodaDegreeId(iKodaDegreeId);
							qo.setUniqueJobId(vsdjt.getUniqueJob().getId());
							qo.setDegreeName(vsdjt.getDegreeName());
							qo.setCountDegreeId(vsdjt.getCountDegree().getId());
							qo.randomizeQueuePosition();
							cpcJobQueue.add(qo);

						}

					}

				}
				removeOutDatedCPCSalaryByDegreeAndJobTitle(vsdjtList, matchedIds, iKodaDegreeId, sb);
				PLog.getRLogger().info("ADDED: " + added + " UPDATED: " + updated);

			}
			sb.append("\n\n=========populateCPCPayByJobTitleAndDegree DONE========== \n\n\n");
			PLog.getRLogger().debug(sb);
			return cpcJobQueue;
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	public void populateCPCSalaryByDegree(ConfigurationBeanParent config, MajorMappingConfig majorMergeMap) throws PersistenceException
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			int updated = 0;
			int newItems = 0;

			
			sb.append("\n\n\n ++++++populateCPCSalaryByDegree++++++ \n\n\n");

			// initializeCorrectionMap();
			List<ValueSalaryByDegree> resultListFromDB = jobAnalysisService.getAllValueSalaryByDegrees();
			PLog.getRLogger().debug("resultList " + resultListFromDB.size());
			List<ValueSalaryByDegree> resultList=initializeMaps(config, resultListFromDB,majorMergeMap);
			List<String> regions = config.retrieveRegionsForCurrentLocale();
			PLog.getRLogger().debug("vsd resultList size: " + resultList.size());

			Map<String, String> matchedIds = new HashMap<String, String>();

			for (ValueSalaryByDegree vsbd : resultList)
			{
				boolean newDegree = false;
				CPCSalaryByDegree cpcVsbd = jobReportingService.getCPCSalaryByDegreeBySpecifiedDegreeIdAndRegion(
						vsbd.getCountDegree().getId(), vsbd.getRegion());

				sb.append("\n\n\n\nLooking at ");
				sb.append(vsbd.getDegreeName());
				sb.append(" in region ");
				sb.append(vsbd.getRegion());
				sb.append(" with count: ");
				sb.append(vsbd.getCount());
				if (null == cpcVsbd)
				{
					cpcVsbd = new CPCSalaryByDegree(vsbd,vsbd.getCountDegree().getDegreeLevel());
					newDegree = true;
				}
				else
				{
					cpcVsbd.update(vsbd,vsbd.getCountDegree().getDegreeLevel());
				}
				sb.append("\n\n");

				if (cpcVsbd.getTypicalSalary() > 10 && cpcReportingServices.isOverDegreePrevalenceThreshold(
						averageCountJobsMap.get(cpcVsbd.getRegion()), cpcVsbd.getCount()))
				{
					sb.append(cpcVsbd.getDegreeName() + " is over Degree Prevalence Threshold: "
							+ averageCountJobsMap.get(cpcVsbd.getRegion()));
					int rank = cpcReportingServices.calculateJobPrevalence(totalCountJobsMap.get(cpcVsbd.getRegion()),
							cpcVsbd.getCount());
					cpcVsbd.setRank(rank);
					sb.append("\nrank " + cpcVsbd.getRank());
					sb.append(" | degree name " + cpcVsbd.getDegreeName());
					if (cpcVsbd.getRank() <= 5)
					{
						String key = generateCPCSalaryByDegreeKey(cpcVsbd.getCountDegreeId(), cpcVsbd.getRegion());
						matchedIds.put(key, key);
						if (newDegree)
						{
							jobReportingService.saveCPCSalaryByDegree(cpcVsbd);
							sb.append("\n++ adding new \n");
							newItems++;
						}
						else
						{
							jobReportingService.updateCPCSalaryByDegree(cpcVsbd);
							sb.append("\n++ updating \n");
							updated++;

						}
					}
					else
					{
						sb.append("\n-- ignored ");
					}
				}
				else
				{
					sb.append("\n-- ignored ");
				}
				sb.append(cpcVsbd);
			}

			sb.append("\nUpdated " + updated);
			sb.append("\nAdded " + newItems);
			sb.append("\n\n\n ++++++populateCPCSalaryByDegree++++++ \n\n\n");
			removeOutDatedCPCSalaryByDegreeEntries(resultList, matchedIds, sb);
			PLog.getRLogger().info("ADDED: " + newItems + " UPDATED: " + updated);

			PLog.getRLogger().debug(sb.toString());
			setSalaryBands();

		}
		catch (Exception e)
		{
			sb.append(e.getMessage());
			PLog.getRLogger().error(sb+e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);
		}
	}

	private void removeOutDatedCPCSalaryByDegreeAndJobTitle(List<ValueSalaryByDegreeAndJobTitle> vsdjtList,
			Map<String, String> matchedIds, Long ikodaDegreeId, StringBuffer sb) throws PersistenceException
	{
		try
		{
			sb.append("\n\n+++++++++++++++++\n\nREMOVING OUTDATED\n");
			for (ValueSalaryByDegreeAndJobTitle vsdjt : vsdjtList)
			{
				String key = generateKeyForCPCPayByJobTitle(vsdjt.getCountDegree().getId(),
						vsdjt.getUniqueJob().getId(), vsdjt.getRegion(), ikodaDegreeId);
				String result = matchedIds.get(key);
				// PLog.getRLogger().debug(result);
				if (null == result)
				{

					int i = jobReportingService.deleteCPCPayByJobTitleAndDegree(vsdjt.getCountDegree().getId(),
							vsdjt.getUniqueJob().getId(), vsdjt.getRegion(), ikodaDegreeId, vsdjt.getCountDegree().getDegreeLevel());
					if (i > 0)
					{
						PLog.getRLogger().info(i + "Rows Deleted For:" + key + " " + vsdjt.toString());
						sb.append(i + "Rows Deleted For:" + key + " " + vsdjt.toString());
					}
				}
			}
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);

			throw new PersistenceException(e);
		}
	}

	private void removeOutDatedCPCSalaryByDegreeEntries(List<ValueSalaryByDegree> resultList,
			Map<String, String> matchedIds, StringBuffer sb) throws PersistenceException
	{
		try
		{
			int deleted = 0;
			for (ValueSalaryByDegree vsbd : resultList)
			{
				String key = generateCPCSalaryByDegreeKey(vsbd.getCountDegree().getId(), vsbd.getRegion());

				String result = matchedIds.get(key);
				PLog.getRLogger().debug(result);

				if (null == result)
				{
					PLog.getRLogger().debug("deleting");
					int i = jobReportingService.deleteCPCSalaryByDegree(vsbd.getCountDegree().getId(),
							vsbd.getRegion(),vsbd.getCountDegree().getDegreeLevel());
					if (i > 0)
					{
						deleted++;
					}
				}
			}
			sb.append("\n\n deleted " + deleted);

		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);

			throw new PersistenceException(e);
		}
	}

	private List<ValueSalaryByDegree> removeOutliersAndFilterByRegion(String region, List<ValueSalaryByDegree> resultList)
	{
		List<ValueSalaryByDegree> regionalList = resultList.stream()
				.filter(vsbd -> vsbd.getRegion().toUpperCase().equals(region.toUpperCase()))
				.collect(Collectors.toList());

		Collections.sort(regionalList, new ComparatoByVsdCount());
		if (regionalList.size() > 10)
		{
			Iterator<ValueSalaryByDegree>itr=regionalList.iterator();
			int count=0;
			while(itr.hasNext())
			{
				if(count>5)
				{
					break;
				}
				itr.next();
				itr.remove();
				count++;
			}
			PLog.getRLogger().debug(regionalList.get(0).getCount());
			PLog.getRLogger().debug(regionalList.get(1).getCount());
			PLog.getRLogger().debug(regionalList.get(2).getCount());
			PLog.getRLogger().debug(regionalList.get(3).getCount());
			PLog.getRLogger().debug(regionalList.get(4).getCount());
		}
		return regionalList;
	}

	public void setAbort(boolean abort)
	{
		this.abort = abort;
		PLog.getRLogger().info("ABORTING");
	}

	private void setSalaryBands() throws PersistenceException
	{

		try
		{
			for (String s : availableRegions)
			{
				List<CPCSalaryByDegree> salaryByDegreeList = jobReportingService.getAllCpcSalaryByDegreesForRegion(s);
				PLog.getRLogger().debug("salaryByDegreeList size: " + salaryByDegreeList.size());
				if(salaryByDegreeList.size()==0)
				{
					continue;
				}
				CPCSalaryByDegree sbdMax = salaryByDegreeList.stream().max(new ComparatoByMedianSalaryAsc()).get();
				CPCSalaryByDegree sbdMin = salaryByDegreeList.stream().min(new ComparatoByMedianSalaryAsc()).get();

				int bottomOfRange = sbdMin.getTypicalSalary().intValue();
				int topOfRange = sbdMax.getTypicalSalary().intValue();
				// SSm.getRLogger().debug("Min salary = " + bottomOfRange);
				// SSm.getRLogger().debug("Max salary = " + topOfRange);
				int range = sbdMax.getTypicalSalary().intValue() - sbdMin.getTypicalSalary().intValue();
				int sevenBandRange = range / 7;

				for (CPCSalaryByDegree sbd : salaryByDegreeList)
				{
					int typicalSalary = sbd.getTypicalSalary().intValue();

					if (typicalSalary > (bottomOfRange + (sevenBandRange * 6)))
					{
						sbd.setSalaryBand(1);
					}
					else if (typicalSalary > (bottomOfRange + (sevenBandRange * 5)))
					{
						sbd.setSalaryBand(2);
					}
					else if (typicalSalary > (bottomOfRange + (sevenBandRange * 4)))
					{
						sbd.setSalaryBand(3);
					}
					else if (typicalSalary > (bottomOfRange + (sevenBandRange * 3)))
					{
						sbd.setSalaryBand(4);
					}
					else if (typicalSalary > (bottomOfRange + (sevenBandRange * 2)))
					{
						sbd.setSalaryBand(5);
					}
					else if (typicalSalary > (bottomOfRange + (sevenBandRange * 1)))
					{
						sbd.setSalaryBand(6);
					}
					else if (typicalSalary >= (bottomOfRange))
					{
						sbd.setSalaryBand(7);
					}
					jobReportingService.updateCPCSalaryByDegree(sbd);
				}
			}
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}
	}

	private void trimVsdjtList(List<ValueSalaryByDegreeAndJobTitle> vsdjtList)
	{

		List<ValueSalaryByDegreeAndJobTitle> temp = new ArrayList<ValueSalaryByDegreeAndJobTitle>();
		for (ValueSalaryByDegreeAndJobTitle vsdjt : vsdjtList)
		{
			if (vsdjt.getRegion().equals("UK"))
			{
				if (vsdjt.getCount() < 5)
				{
					temp.add(vsdjt);
				}
			}
			if (vsdjt.getRegion().equals("US"))
			{
				if (vsdjt.getCount() < 12)
				{
					temp.add(vsdjt);
				}
			}
			if (vsdjt.getRegion().equals("Canada"))
			{
				if (vsdjt.getCount() < 6)
				{
					temp.add(vsdjt);
				}
			}

		}
		vsdjtList.removeAll(temp);
	}

}
