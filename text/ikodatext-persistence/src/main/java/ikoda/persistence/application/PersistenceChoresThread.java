package ikoda.persistence.application;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.netio.config.ConfigurationBeanForPersistence_JobAnalysis;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.LanguageForAnalysis;
import ikoda.netio.config.MajorMappingConfig;

import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.CountDegreeJob;
import ikoda.persistence.model.CountJobTitleByDegreeName;
import ikoda.persistence.model.DatabaseDescriptors;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.ValueSalaryByJobTitle;
import ikoda.persistence.service.JobAnalysisService;
import ikoda.persistence.service.PersistenceMethods;
import ikoda.persistence.service.QuickAndDirtyMethods;
import ikoda.utils.ProcessStatus;
import ikoda.utils.TicToc;

@Component
public class PersistenceChoresThread extends Thread
{

	private final static int SLEEPTIMEDEFAULT = 10000;

	private int sleepTime = SLEEPTIMEDEFAULT;
	private int baseSleepTime=500;
	private ConfigurationBeanParent config;
	private ConfigurationBeanForPersistence_JobAnalysis pConfig;
	private boolean ready = false;
	int source = 0;

	private boolean stopDependsOnExternalCall;
	boolean continueRun = true;
	private boolean abort = false;
	private long persistenceProcessStartTime = System.currentTimeMillis();
	private ReentrantLock lock;
	private MajorMappingConfig majorMap;
	private StringBuffer degreeMatchingLog = new StringBuffer();
	private int entryCount = 0;

	HashMap<Long, Long> uniqueJobIdMemoryMap = new HashMap<Long, Long>();
	HashMap<Long, Long> uniqueJobIdCacheMap = new HashMap<Long, Long>();
	List<Long> countDegreeIdList = new ArrayList<Long>();
	private int counter = 0;

	@Autowired
	private PersistenceMethods persistenceMethods;

	@Autowired
	private JobAnalysisService jobAnalysisService;
	@Autowired
	private QuickAndDirtyMethods quickAndDirtyMethods;

	public PersistenceChoresThread()
	{
		PLog.getChoresLogger().info("\n\n\n\n\n PersistenceChoresThread  INIT  \n\n\n\n");
	}

	public void abort()
	{
		PLog.getChoresLogger().info("\n\nabort called \n\n\n\n");
		this.abort = true;
		continueRun = false;
	}

	private void calculateMediansForDegree()
	{
		TicToc tictoc = new TicToc();
		try
		{
			if (countDegreeIdList.size() == 0)
			{
				return;
			}

			//// we pull a degreeid at random
			int index = ThreadLocalRandom.current().nextInt(countDegreeIdList.size());
			Long degreeId = countDegreeIdList.remove(index);

			PLog.getChoresLogger().info("Looking at degree id: " + degreeId);

			tictoc.tic("getCountDegreeJobByCountDegreeId");
			List<CountDegreeJob> cjdList = null;
			try
			{
				cjdList = jobAnalysisService.getCountDegreeJobByCpuntDegreeId(degreeId);
			}
			catch (Exception e)
			{
				continueRun = false;
				PLog.getChoresLogger().error(e.getMessage(), e);
				return;
			}

			PLog.getChoresLogger().debug(tictoc.toc());
			Map<Integer, List<CountDegreeJob>> cdjMap = new HashMap<Integer, List<CountDegreeJob>>();

			PLog.getChoresLogger().debug("Count of jobs for this degree  " + cjdList.size());
			int total = cjdList.size();
			if (cjdList.size() == 0)
			{
				PLog.getChoresLogger().info("Nothing to do. Returning  ");
				return;
			}
			else if (cjdList.size() > 50000)
			{
				List<CountDegreeJob> copy = new LinkedList<CountDegreeJob>(cjdList);
				Collections.shuffle(copy);

				cjdList = copy.subList(0, 50000);
				PLog.getChoresLogger().info("\nCount of jobs for this degree  REDUCED" + cjdList.size());

			}
			PLog.getChoresLogger().debug("CountDegree Specified Degree Name is  "
					+ cjdList.get(0).getCountDegree().getSpecifiedDegreeName());

			HashMap<String, List<CountDegreeJob>> hmCountDegreeJobByRegionAndDegree = new HashMap<String, List<CountDegreeJob>>();
			HashMap<String, List<Integer>> hmSalaryMinByRegionAndDegree = new HashMap<String, List<Integer>>();
			HashMap<String, List<Integer>> hmSalaryMaxByRegionAndDegree = new HashMap<String, List<Integer>>();

			tictoc.tic("Sort by region");

			for (LanguageForAnalysis lfa : config.getLanguagesForAnalysis())
			{
				for (String region : lfa.getRegions())
				{
					hmCountDegreeJobByRegionAndDegree.put(region, new ArrayList<CountDegreeJob>());
					hmSalaryMinByRegionAndDegree.put(region, new ArrayList<Integer>());
					hmSalaryMaxByRegionAndDegree.put(region, new ArrayList<Integer>());
				}
			}

			int count = 0;
			for (CountDegreeJob cjd : cjdList)
			{
				if (!continueRun)
				{
					return;
				}

				Job job = cjd.getJob();
				count++;
				if (null == job)
				{
					PLog.getChoresLogger().warn("\n\n\n\nNULL JOB. CJD ID is " + cjd.getId() + "\n\n\n");
					continue;
				}

				if (null == uniqueJobIdMemoryMap.get(cjd.getUniqueJob().getId()))
				{
					uniqueJobIdMemoryMap.put(cjd.getUniqueJob().getId(), cjd.getUniqueJob().getId());
					uniqueJobIdCacheMap.put(cjd.getUniqueJob().getId(), cjd.getUniqueJob().getId());
				}

				List<CountDegreeJob> cjds = hmCountDegreeJobByRegionAndDegree.get(job.getRegion());

				if (null != cjds)
				{
					cjds.add(cjd);
				}
				List<Integer> mins = hmSalaryMinByRegionAndDegree.get(job.getRegion());
				if (null != mins)
				{
					if (null != job.getSalaryStartRange() && job.getSalaryStartRange() > 1)
					{
						mins.add(job.getSalaryStartRange());
					}

				}

				List<Integer> maxes = hmSalaryMaxByRegionAndDegree.get(job.getRegion());
				if (null != maxes)
				{
					if (null != job.getSalaryEndRange() && job.getSalaryEndRange() > 1)
					{
						maxes.add(job.getSalaryEndRange());
					}

				}
				if (count % 250 == 0)
				{
					PLog.getChoresLogger().info("Loaded " + count + " Jobs out of " + total);
				}
			}
			PLog.getChoresLogger().debug(tictoc.toc());
			tictoc.tic("calculate medians");

			for (LanguageForAnalysis lfa : config.getLanguagesForAnalysis())
			{
				for (String region : lfa.getRegions())
				{
					List<Integer> salaryMinList = hmSalaryMinByRegionAndDegree.get(region);
					PLog.getChoresLogger().info("got salary min list with size " + salaryMinList.size());

					List<Integer> salaryMaxList = hmSalaryMaxByRegionAndDegree.get(region);
					PLog.getChoresLogger().info("got salary max list with size " + salaryMaxList.size());

					PLog.getChoresLogger().debug("Region: " + region);
					PLog.getChoresLogger().debug("MinList: " + salaryMinList);
					PLog.getChoresLogger().debug("MaxList: " + salaryMaxList);

					if (salaryMinList.size() == 0 || salaryMaxList.size() == 0)
					{
						continue;
					}
					Collections.sort(salaryMinList);
					Collections.sort(salaryMaxList);

					int medianIndexMin = salaryMinList.size() / 2;
					int medianIndexMax = salaryMaxList.size() / 2;

					PLog.getChoresLogger().debug("salaryMinList: " + salaryMinList);
					PLog.getChoresLogger().debug("salaryMaxList: " + salaryMaxList);
					PLog.getChoresLogger().debug("medianIndexMin: " + medianIndexMin);
					PLog.getChoresLogger().debug("medianIndexMax: " + medianIndexMax);

					Integer medianMin = salaryMinList.get(medianIndexMin);
					Integer medianMax = salaryMaxList.get(medianIndexMax);

					PLog.getChoresLogger().debug("medianMin: " + medianMin);
					PLog.getChoresLogger().debug("medianMax: " + medianMax);

					Integer median = (medianMin + medianMax) / 2;

					PLog.getChoresLogger().debug("\n\n\nmedian: " + median);

					ValueSalaryByDegree vsd = jobAnalysisService.getValueSalaryByDegree(degreeId, region);
					if (null != vsd)
					{
						PLog.getChoresLogger().debug("ValueSalaryByDegree is " + vsd.getDegreeName());
						vsd.setMedianSalary(median);

						jobAnalysisService.updateValueSalaryByDegree(vsd);
						PLog.getChoresLogger().info("\n\n\n Median set to " + vsd.getMedianSalary() + " for "
								+ vsd.getDegreeName() + " in region " + vsd.getRegion() + "\n\n\n");
					}
				}
			}
			PLog.getChoresLogger().debug(tictoc.toc());

			tictoc.tic("calculateMediansForDegreeByJobTitle");
			calculateMediansForDegreeByJobTitle(degreeId, hmCountDegreeJobByRegionAndDegree);
			PLog.getChoresLogger().debug(tictoc.toc());

		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private void calculateMediansForDegreeByJobTitle(Long countDegreeId,
			HashMap<String, List<CountDegreeJob>> hmCountDegreeJobByRegionAndDegree)
	{
		try
		{
			TicToc tictoc = new TicToc();
			Set<Long> setUjids = new HashSet<Long>();
			tictoc.tic("Loading Ujids");
			Iterator<String> itrCdjMap = hmCountDegreeJobByRegionAndDegree.keySet().iterator();
			HashMap<String, HashMap<Long, List<Job>>> jobByRegionMap = new HashMap<String, HashMap<Long, List<Job>>>();
			while (itrCdjMap.hasNext())
			{
				try
				{
					String key = itrCdjMap.next();
					List<CountDegreeJob> cdjList = hmCountDegreeJobByRegionAndDegree.get(key);
					for (CountDegreeJob cdj : cdjList)
					{
						setUjids.add(cdj.getUniqueJob().getId());
						HashMap<Long, List<Job>> jobsByRegion = jobByRegionMap.get(cdj.getJob().getRegion());
						if (null == jobsByRegion)
						{
							jobsByRegion = new HashMap<Long, List<Job>>();
							jobByRegionMap.put(cdj.getJob().getRegion(), jobsByRegion);
						}

						List<Job> jobList = jobsByRegion.get(cdj.getUniqueJob().getId());
						if (null == jobList)
						{
							jobList = new ArrayList<Job>();
							jobsByRegion.put(cdj.getUniqueJob().getId(), jobList);

						}
						jobList.add(cdj.getJob());

					}
				}
				catch (Exception e)
				{
					PLog.getChoresLogger().error(e.getMessage(), e);
				}

			}
			PLog.getChoresLogger().debug(tictoc.toc());

			tictoc.tic("Processing CountJobTitleByDegreeName");
			for (Long ujid : setUjids)
			{
				if (!continueRun)
				{
					break;
				}
				CountJobTitleByDegreeName jobByDegree = jobAnalysisService.getCountJobTitleByDegreeName(ujid,
						countDegreeId);

				//// look at each uniquejob associated with that degree
				CountDegree countDegree = jobAnalysisService.getCountDegreeById(countDegreeId);
				PLog.getChoresLogger().info("\n\n\n----\nLooking at " + countDegree.getSpecifiedDegreeName()
						+ "   ujid:  " + ujid + " last updated median: " + jobByDegree.getCountSinceMedianUpdate());

				///// make a map of median salary by job title by region, We
				///// only process this map if sufficient change in data
				Map<String, DescriptiveStatistics> statByRegionDegreeJobTitle = new HashMap<String, DescriptiveStatistics>();
				if (null == jobByDegree.getCountSinceMedianUpdate())
				{
					jobByDegree.setCountSinceMedianUpdate(new Integer(0));
					jobAnalysisService.updateCountJobTitleByDegreeName(jobByDegree);
				}
				else if (jobByDegree.getCountSinceMedianUpdate() < 10)
				{
					continue;
				}
				else
				{
					Iterator<String> itr = jobByRegionMap.keySet().iterator();
					while (itr.hasNext())
					{
						String region = itr.next();
						HashMap<Long, List<Job>> hmujids = jobByRegionMap.get(region);
						if (null != hmujids)
						{
							List<Job> jobList = hmujids.get(ujid);
							if (null != jobList)
							{

								if (processSalaryDataForJobs(countDegree, jobList, statByRegionDegreeJobTitle))
								{
									PLog.getChoresLogger().info("\n\n\n----\nUpdating "
											+ countDegree.getSpecifiedDegreeName() + "     " + ujid);
									jobByDegree.setCountSinceMedianUpdate(new Integer(0));
									jobAnalysisService.updateCountJobTitleByDegreeName(jobByDegree);
									PLog.getChoresLogger().info("setting median for  job title with degree ");
									saveMediansForJobTitleByDegree(ujid, countDegree.getId(),
											statByRegionDegreeJobTitle);

								}
							}
						}
					}
				}

			} //// ends jobsByDegree retrieved by degreeid
			PLog.getChoresLogger().debug(tictoc.toc());
			PLog.getChoresLogger().info(" --- ");

		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private void calculateMediansForUniqueJob()
	{
		try
		{
			TicToc tictoc = new TicToc();
			tictoc.tic("calculateMediansForUniqueJob");
			PLog.getChoresLogger().debug("calculateMediansForUniqueJob ");
			if (uniqueJobIdCacheMap.size() == 0)
			{
				return;
			}
			PLog.getChoresLogger().debug("uniqueJobIdCacheMap size " + uniqueJobIdCacheMap.size());
			int counter = 0;
			while (counter < 10)
			{
				if (!continueRun)
				{
					break;
				}
				
				counter++;
				Iterator<Long> itrujid = uniqueJobIdCacheMap.keySet().iterator();
				if(!itrujid.hasNext())
				{
					break;
				}
				Long ujId = itrujid.next();
				itrujid.remove();
				PLog.getChoresLogger().debug("looking at " + ujId);
				List<Job> jobs = jobAnalysisService.getJobsByUniqueJobId(ujId);
				Map<String, DescriptiveStatistics> statByRegion = new HashMap<String, DescriptiveStatistics>();

				/// compile data to apache stats, one set for each region

				PLog.getChoresLogger().debug("retrieved number of jobs " + jobs.size());
				if (jobs.size() == 0)
				{
					return;
				}
				for (Job job : jobs)
				{
					if (null == job.getSalaryEndRange() || null == job.getSalaryStartRange())
					{
						continue;
					}

					if (null == job.getRegion() || job.getRegion().isEmpty())
					{
						continue;
					}
					DescriptiveStatistics stats = statByRegion.get(job.getRegion());
					if (null == stats)
					{
						stats = new DescriptiveStatistics();
						statByRegion.put(job.getRegion(), stats);
					}

					int salary = (job.getSalaryStartRange() + job.getSalaryEndRange()) / 2;
					PLog.getChoresLogger().debug("salary for " + job.getId() + "=" + salary);
					stats.addValue(salary);
				}

				/// calculate median for eacxh region

				Iterator<String> itr = statByRegion.keySet().iterator();
				while (itr.hasNext())
				{
					String region = itr.next();
					DescriptiveStatistics statforregion = statByRegion.get(region);
					int medianSalary = (int) statforregion.getPercentile(50);

					ValueSalaryByJobTitle vsjt = jobAnalysisService.getValueSalaryByJobTitleByUniqueJobId(ujId, region);
					if (null == vsjt)
					{
						continue;
					}
					vsjt.setMedianSalary(new Integer(medianSalary));
					jobAnalysisService.updateValueSalaryByJobTitle(vsjt);
					PLog.getChoresLogger().info("\n\nRegion: " + region + "       vsjtid: " + vsjt.getJobTitle()
							+ ": median salary is $" + vsjt.getMedianSalary());
				}
				
			}
			
			ProcessStatus.getStatusMap().put("PersistantChores: CountUniqueJobs Remaining", String.valueOf(uniqueJobIdCacheMap.size()));
			PLog.getChoresLogger().debug(tictoc.toc());

		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private synchronized boolean doPersistenceProcess()
	{

		persistenceProcessStartTime = System.currentTimeMillis();

		if (abort)
		{
			PLog.getChoresLogger().warn("\n\n\n Abort called \n\n\n");
			return false;
		}
		try
		{

			PLog.getChoresLogger()
					.info("\n\n\n\n-----------------------------------doPersistenceChores------------------------\n\n");
			PLog.getChoresLogger().info("\n\n\n CountDegrees Remaining " + countDegreeIdList.size()
					+ "\n\n\n\t\t_______________________\n\n");

			ProcessStatus.getStatusMap().put("PersistantChores: CountDegrees Remaining", String.valueOf(countDegreeIdList.size()));
			PLog.getChoresLogger().info("calculateMediansForUniqueJob");
			calculateMediansForUniqueJob();
			PLog.getChoresLogger().info("calculateMediansForDegree");
			calculateMediansForDegree();

			PLog.getChoresLogger()
					.info("\n\n\n\n++-------------------------------done PersistenceChores---------------------++\n\n");

			/*
			 * PLog.getChoresLogger() .info(
			 * "\n\n\n\n++DEBUG SET TO ABORT AFTER ONE CYCLE++\n\n");
			 * continueRun=false;
			 */

			return true;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			return true;
		}
	}

	public int getEntryCount()
	{
		return entryCount;
	}

	public long getFileProcessTime()
	{
		return System.currentTimeMillis() - persistenceProcessStartTime;
	}

	public JobAnalysisService getJobAnalysisService()
	{
		return jobAnalysisService;
	}

	private MajorMappingConfig getMajorMap()
	{

		try
		{
			lock.lock();

			String locale = "";
			if (config.getSpecifiedLanguage().equals(ConfigurationBeanParent.LANGUAGE_EN))
			{
				locale = "_en";
			}
			if (config.getSpecifiedLanguage().equals(ConfigurationBeanParent.LANGUAGE_ZH))
			{
				locale = "_zh_CN";
			}

			PLog.getChoresLogger().debug("/" + pConfig.getQualificationMap() + locale + ".xml");
			InputStream is = this.getClass().getResourceAsStream("/" + pConfig.getQualificationMap() + locale + ".xml");

			JAXBContext jaxbContext = JAXBContext.newInstance(MajorMappingConfig.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			MajorMappingConfig config = (MajorMappingConfig) jaxbUnmarshaller.unmarshal(is);
			PLog.getChoresLogger().debug("MajorMappingConfig " + config);
			return config;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			return null;

		}
		finally
		{
			lock.unlock();
			PLog.getChoresLogger().info("released lock");
		}
	}

	protected int getSleepTime()
	{
		return sleepTime;
	}

	private void initializeCountDegreeIdList()
	{
		try
		{
			countDegreeIdList = jobAnalysisService.getAllCountDegreeIds();
			PLog.getChoresLogger().debug("countDegreeIdList size: " + countDegreeIdList.size());
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
	}

	public boolean isContinueRun()
	{
		return continueRun;
	}

	public boolean isStop()
	{
		return abort;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private boolean processSalaryDataForJobs(CountDegree degree, List<Job> jobs,
			Map<String, DescriptiveStatistics> statByRegionDegreeJobTitle)
	{
		try
		{
			TicToc tictoc = new TicToc();
			tictoc.tic("Getting jobs for Ujid");
			boolean updateMade = false;

			PLog.getChoresLogger().debug("Count of jobs with this jobtitle: " + jobs.size());
			PLog.getChoresLogger().debug(tictoc.toc());

			tictoc.tic("Processing jobs");
			for (Job job : jobs)
			{
				// PLog.getChoresLogger().debug("Job is " + job.getJobTitle());

				if (job.containsAreaOfStudy(degree.getSpecifiedDegreeName()))
				{
					if (null == job.getSalaryEndRange() || null == job.getSalaryStartRange())
					{
						PLog.getChoresLogger().debug("Skipping due to null salary ");
						continue;
					}
					if (job.getRegion().toUpperCase().equals("ZH"))
					{
						if (job.getSalaryEndRange() < 3500 || job.getSalaryStartRange() < 2500)
						{
							PLog.getChoresLogger().debug("Skipping due to invalid salary zh");
							continue;
						}
					}
					if (job.getRegion().toUpperCase().equals("UK"))
					{
						if (job.getSalaryEndRange() < 15000 || job.getSalaryStartRange() < 10000)
						{
							PLog.getChoresLogger().debug("Skipping due to invalid salary uk");
							continue;
						}
					}
					else
					{
						if (job.getSalaryEndRange() < 25000 || job.getSalaryStartRange() < 20000)
						{
							PLog.getChoresLogger().debug("Skipping due to invalid salary other (us, ca, au)");
							continue;
						}
					}
					if (null == job.getRegion() || job.getRegion().isEmpty())
					{
						PLog.getChoresLogger().debug("Skipping due to null region");
						continue;
					}

					DescriptiveStatistics statsByDegreeandJob = statByRegionDegreeJobTitle.get(job.getRegion());

					if (null == statsByDegreeandJob)
					{
						statsByDegreeandJob = new DescriptiveStatistics();
						statByRegionDegreeJobTitle.put(job.getRegion(), statsByDegreeandJob);
					}
					int salary = (job.getSalaryStartRange() + job.getSalaryEndRange()) / 2;
					PLog.getChoresLogger().debug("\n\n\n\nsalary is " + salary + "\njobtitle is " + job.getJobTitle()
							+ "\ndegree is " + degree.getSpecifiedDegreeName() + "\nregion is " + job.getRegion());

					statsByDegreeandJob.addValue(salary);

					updateMade = true;

				}
				else
				{
					PLog.getChoresLogger().debug("jjob does not require specified degree. ");
				}
			} ///// ends jobs loop for jobs retrieved by degreeid
			PLog.getChoresLogger().debug(tictoc.toc());
			return updateMade;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void run()
	{
		try
		{
			continueRun = true;
			PLog.getChoresLogger().info("Starting Persistence Thread");
			int timeoutNoDataReceived = 0;
			while (continueRun)
			{
				if (lock.isHeldByCurrentThread())
				{
					PLog.getChoresLogger().warn("Releasing lock before going to sleep. This is bad");
					lock.unlock();
				}
				Thread.sleep(sleepTime+baseSleepTime);
				// PLog.getChoresLogger().debug("In run.....");

				if (null == config || ready == false)
				{
					timeoutNoDataReceived++;
					if (timeoutNoDataReceived >= 30)
					{
						continueRun = false;
					}
				}
				else if (!doPersistenceProcess())
				{
					PLog.getChoresLogger().info("\n\n\nEXITING THREAD\n\n\n");
					continueRun = false;
				}

			}
		}
		catch (InterruptedException e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			return;

		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error("This is Bad. Thread won't start");
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
		catch (Error err)
		{
			PLog.getChoresLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
			throw err;
		}

	}

	public void runPersistenceChores(ConfigurationBeanParent inconfig, boolean depends, ReentrantLock inlock)
			throws PersistenceException
	{
		try
		{
			PLog.getChoresLogger().info(" Initializing PersistenceChores Thread  ");

			config = inconfig;
			pConfig=(ConfigurationBeanForPersistence_JobAnalysis)inconfig.getPersistConfig();
			if (jobAnalysisService.getDatabaseDescriptors().size() == 0)
			{
				PLog.getChoresLogger().warn("\n\nBrand new database. Setting Locale from config.xml data to "
						+ config.getLanguagesForAnalysis() + "\n\n\n");
				DatabaseDescriptors dd = new DatabaseDescriptors();
				dd.setDatabaseLocale(config.getSpecifiedLanguage());
				jobAnalysisService.saveDatabaseDescriptors(dd);

			}
			if (!jobAnalysisService.isCorrectLocale(config.getSpecifiedLanguage()))
			{
				PLog.getChoresLogger().error("\n\nWRONG LANGUAGE LOCALE. ABORTING NOW\n\n\n");
				throw new PersistenceException("\n\nWRONG LANGUAGE LOCALE. ABORTING NOW\n\n\n");
			}

			if (!jobAnalysisService.isCorrectDatabaseDescriptor(config.getDatabaseDescriptor()))
			{
				PLog.getChoresLogger().error("\n\nWRONG DATABASE DESCRIPTOR. ABORTING NOW\n\n\n");
				throw new PersistenceException("\n\nWRONG DATABASE DESCRIPTOR. ABORTING NOW\n\n\n");
			}
			
			if(config.isMinimizeResourceUse())
			{
				baseSleepTime=90000;
			}
			lock = inlock;
			majorMap = getMajorMap();
			PLog.getChoresLogger().debug("runPersistenceProcesses");
			stopDependsOnExternalCall = depends;

			initializeCountDegreeIdList();
			persistenceMethods.initalizeRelations(majorMap);
			ready = true;
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}

	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private void saveMediansForJobTitleByDegree(Long ujId, Long degreeId,
			Map<String, DescriptiveStatistics> statByRegionDegreeJobTitle)
	{
		try
		{
			TicToc tictoc = new TicToc();
			tictoc.tic("saveMediansForJobTitleByDegree");
			Iterator<String> itr = statByRegionDegreeJobTitle.keySet().iterator();

			int countIterators = 0;
			while (itr.hasNext())
			{
				countIterators++;
				PLog.getChoresLogger().debug("\n\n----------------------" + countIterators + "--------\n\n");

				String region = itr.next();
				DescriptiveStatistics statforregionDegreeAndJob = statByRegionDegreeJobTitle.get(region);
				if (null == statforregionDegreeAndJob)
				{
					continue;
				}
				ValueSalaryByDegreeAndJobTitle vsdjt = jobAnalysisService
						.getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(ujId, degreeId, region);

				PLog.getChoresLogger()
						.debug(region + ": Count Values accumulated = " + statforregionDegreeAndJob.getN());

				int median = new Integer((int) statforregionDegreeAndJob.getPercentile(50));

				vsdjt.setMedianSalary(median);
				PLog.getChoresLogger().debug("vsdjt is " + vsdjt);
				jobAnalysisService.updateValueSalaryByDegreeAndJobTitle(vsdjt);
				PLog.getChoresLogger()
						.debug("Median set to " + vsdjt.getMedianSalary() + " for " + vsdjt.getDegreeName()
								+ " mvn compile" + "job title: " + vsdjt.getJobTitle() + " in region: " + region);
				statforregionDegreeAndJob.clear();
			}

			PLog.getChoresLogger().debug(tictoc.toc());
			PLog.getChoresLogger().debug("\n\nNext job for same degree\n\n\n\n\n\n\n");
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
	}

	public void setJobAnalysisService(JobAnalysisService jobAnalysisService)
	{
		this.jobAnalysisService = jobAnalysisService;
	}

	protected void setSleepTime(int sleepTime)
	{
		this.sleepTime = sleepTime;
	}

	public void setStop(boolean stop)
	{
		PLog.getChoresLogger().info("Calling STOP  on PersistenceChoresThread");
		this.abort = stop;
		continueRun = false;
	}

	private void doQdTasks()
	{
		try
		{
			quickAndDirtyMethods.doQdTasks();
		}
		catch (Exception e)
		{
			PLog.getChoresLogger().error(e.getMessage(), e);
		}
	}

}
