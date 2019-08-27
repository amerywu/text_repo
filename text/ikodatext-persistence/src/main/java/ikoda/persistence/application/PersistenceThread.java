package ikoda.persistence.application;

import java.io.File;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.netio.config.ConfigurationBeanForPersistence_JobAnalysis;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InfoBox;
import ikoda.netio.config.LanguageForAnalysis;
import ikoda.netio.config.MajorMappingConfig;

import ikoda.persistence.model.CountDegree;
import ikoda.persistence.model.DatabaseDescriptors;
import ikoda.persistence.model.Job;
import ikoda.persistence.model.UniqueJob;
import ikoda.persistence.model.ValueSalaryByDegree;
import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;
import ikoda.persistence.model.ValueSalaryByJobTitle;
import ikoda.persistence.service.JobAnalysisService;
import ikoda.persistence.service.PersistenceMethods;
import ikoda.utils.ProcessStatus;


@Component
public class PersistenceThread extends Thread
{

	private final static int SLEEPTIMEDEFAULT = 1500;
	private final static String TXT = ".txt";
	protected static String donePath = ".";
	protected static String failedPath = ".";
	public static String infoBoxPath = ".";
	private int sleepTime = SLEEPTIMEDEFAULT;
	private ConfigurationBeanParent config;
	private ConfigurationBeanForPersistence_JobAnalysis pConfig;
	private boolean ready = false;
	int source = 0;
	private boolean upstreamThreadDone=true;
	private boolean stopDependsOnExternalCall;
	boolean continueRun = true;
	private boolean abort = false;
	private long persistenceProcessStartTime = System.currentTimeMillis();
	private ReentrantLock lock;
	private MajorMappingConfig majorMap;
	private StringBuffer degreeMatchingLog = new StringBuffer();
	private int entryCount = 0;

	private List<Path> files;
	private List<Path> inputPaths = new ArrayList<Path>();
	List<Long> uniqueJobIdList = new ArrayList<Long>();
	List<Long> countDegreeIdList = new ArrayList<Long>();

	@Autowired
	private JobAnalysisService jobAnalysisService;

	@Autowired
	private PersistenceMethods persistenceMethods;

	public PersistenceThread()
	{
		PLog.getLogger().info("\n\n\n\n\n   INIT  \n\n\n\n");
	}

	public void abort()
	{

		this.abort = true;
		continueRun = false;
	}

	private String capitalize(String line, String region)
	{
		try
		{

			PLog.getLogger().debug(line + " | " + region);
			if (region.toUpperCase().contains("ZH"))
			{
				return line.trim();
			}

			String lineNew = line;
			if (line.trim().startsWith("- "))
			{
				lineNew = line.substring(2);
			}

			StringTokenizer token = new StringTokenizer(lineNew);
			String capLine = "";
			while (token.hasMoreTokens())
			{
				String tok = token.nextToken().toString();
				capLine += Character.toUpperCase(tok.charAt(0)) + tok.substring(1);
				capLine += " ";
			}
			PLog.getLogger().debug(capLine);
			return capLine.trim();
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return line;
		}
	}

	private boolean checkSalary(int startSalary, int endSalary, String region)
	{
		PLog.getLogger().debug("startSalary: " + startSalary);
		PLog.getLogger().debug("endSalary: " + endSalary);
		PLog.getLogger().debug("region: " + region);
		try
		{
			if (region.toUpperCase().contains("ZH"))
			{
				if (startSalary < 2000 || endSalary < 2000 || endSalary > 40000)
				{
					PLog.getLogger().warn("Invalid salary " + startSalary + " - " + endSalary);
					return false;
				}
			}
			else if (startSalary < 15000 || endSalary < 15000 || endSalary > 175000)
			{
				PLog.getLogger().warn("Invalid salary " + startSalary + " - " + endSalary);
				return false;
			}
			if (startSalary >= endSalary)
			{
				PLog.getLogger().warn("Invalid salary " + startSalary + " - " + endSalary);
				return false;
			}

			return filterOutlierSalaries(startSalary, endSalary, region);
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return false;
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private synchronized boolean doPersistenceProcess()
	{
		Path p = null;
		PLog.getLogger().info("Remaining infoboxes: " + files.size());
		persistenceProcessStartTime = System.currentTimeMillis();

		if (abort)
		{
			PLog.getLogger().warn("\n\n\nAbort called\n\n\n");
			return false;
		}
		try
		{
			if (files.size() == 0)
			{
				PLog.getLogger().info("calling list files");
				listFiles();
				if (files.size() == 0)
				{
					PLog.getLogger().info("File size 0");
					sleepTime = 40000;
					if (stopDependsOnExternalCall)
					{
						if (upstreamThreadDone)
						{
							PLog.getLogger().info("Stopping because dependency done");
							return false;
						}
						else
						{
							PLog.getLogger().info("Waiting on dependency");
							return true;
							// because we are waiting
						}
					}
					else
					{
						return false;
					}
				}
			}
			sleepTime = SLEEPTIMEDEFAULT;

			p = files.remove(0);
			/// hack...some kinda threading ?
			if (null == p || null == p.getFileName())
			{
				return true;
			}
			
			PLog.getLogger().info("getting info box");
			InfoBox ib = getInfoBox(p);
			if (null == ib)
			{
				return true;
			}
			
			PLog.getLogger().info("doing persistence for " + ib.toString());
			Job job = persistInfoBox(ib);
			if (null != job)
			{

				persistenceMethods.countJobTitle(job, ib.getRegion());
				persistenceMethods.countDegree(job);

				/// previous two methods must precede.
				persistenceMethods.countJobTitleByDegree(job);
				valueSalaryByDegree(job);
				valueSalaryByJobTitle(job);
				valueSalaryByDegreeAndJobTitle(job);
				PLog.getLogger().debug(ib);

				PLog.getLogger().info("moving file");
				moveFile(p);
				ProcessStatus.incrementStatus("Persist_Succeeded");
				ProcessStatus.incrementStatus("Persist Succeeded "+ib.getRegion());
				entryCount++;

			}
			else
			{
				
				ProcessStatus.incrementStatus("Persist_Failed "+ib.getRegion());
				ProcessStatus.incrementStatus("Persist Failed");
				moveFailedFile(p);
			}
			return true;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return true;
		}
	}

	private boolean filterOutlierSalaries(int startRange, int endRange, String region)
	{
		for(LanguageForAnalysis lfa:config.getLanguagesForAnalysis())
		{
			for(String listedRegion:lfa.getRegions())
			{


		if (region.equals(listedRegion))
		{
			if (endRange > 100000)
			{
				return false;
			}
			if (endRange - startRange > 17000)
			{
				PLog.getLogger().warn("Salary Range too Broad");
				return false;
			}
		}
		else if (region.equals(listedRegion))
		{
			if (endRange > 40000)
			{
				return false;
			}
			if (endRange - startRange > 20000)
			{
				PLog.getLogger().warn("Salary Range too Broad");
				return false;
			}
		}
		else
		{
			if (endRange > 150000)
			{
				return false;
			}
			if (endRange - startRange > 25000)
			{
				PLog.getLogger().warn("Salary Range too Broad");
				return false;
			}
		}
		}
		}
		return true;
	}

	public int getEntryCount()
	{
		return entryCount;
	}

	public long getFileProcessTime()
	{
		return System.currentTimeMillis() - persistenceProcessStartTime;
	}

	private InfoBox getInfoBox(Path p)
	{
		try
		{
			String path = p.toString();
			PLog.getLogger().info("getting info box at " + path);
			lock.lock();
			PLog.getLogger().info("got lock" + path);

			JAXBContext jaxbContext = JAXBContext.newInstance(InfoBox.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			InfoBox ib = (InfoBox) jaxbUnmarshaller.unmarshal(p.toFile());

			PLog.getLogger().info("got info box");
			return ib;
		}
		catch (javax.xml.bind.UnmarshalException ue)
		{
			PLog.getLogger().error(ue.getMessage(), ue);
			moveFailedFile(p);
			return null;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}
		finally
		{
			lock.unlock();
			PLog.getLogger().info("released lock");
		}
	}

	public JobAnalysisService getJobAnalysisService()
	{
		return jobAnalysisService;
	}

	private MajorMappingConfig getMajorMap() throws PersistenceException
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

			PLog.getLogger().debug("/" + pConfig.getQualificationMap() + locale + ".xml");
			InputStream is = this.getClass().getResourceAsStream("/" + pConfig.getQualificationMap() + locale + ".xml");
			
			JAXBContext jaxbContext = JAXBContext.newInstance(MajorMappingConfig.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			MajorMappingConfig config = (MajorMappingConfig) jaxbUnmarshaller.unmarshal(is);
			PLog.getLogger().debug("MajorMappingConfig " + config);
			return config;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e.getMessage(), e);

		}
		finally
		{
			lock.unlock();
			PLog.getLogger().info("released lock");
		}
	}

	private UniqueJob getOrCreateUniqueJob(String jobTitle, String region)
	{
		try
		{
			String uniqueJobTitle = persistenceMethods.uniqueJobTitle(jobTitle, region);
			UniqueJob uj = jobAnalysisService.getUniqueJob(uniqueJobTitle);
			if (null == uj)
			{
				uj = new UniqueJob();
				uj.setFriendlyName(capitalize(jobTitle, region));
				uj.setUniqueJob(uniqueJobTitle);
				jobAnalysisService.saveUniqueJob(uj);
			}
			return uj;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}
	}

	protected int getSleepTime()
	{
		return sleepTime;
	}

	/*
	 * private void initializeCountDegreeIdList() { try { countDegreeIdList =
	 * jobAnalysisService.getAllCountDegreeIds(); PLog.getLogger().debug(
	 * "countDegreeIdList size: " + countDegreeIdList.size()); } catch
	 * (Exception e) { PLog.getLogger().error(e.getMessage(), e); } }
	 */

	/*
	 * @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	 * private void initializeUniqueJobIdList() { try { uniqueJobIdList =
	 * jobAnalysisService.getAlUniqueJobIds(); PLog.getLogger().debug(
	 * "uniqueJobIdList size: " + uniqueJobIdList.size()); } catch (Exception e)
	 * { PLog.getLogger().error(e.getMessage(), e); } }
	 */

	public boolean isContinueRun()
	{
		return continueRun;
	}

	public boolean isStop()
	{
		return upstreamThreadDone;
	}

	private void listFiles()
	{
		try
		{
			if (lock.tryLock(3, TimeUnit.SECONDS))
			{
				for (Path p : inputPaths)
				{
					PLog.getLogger().info("list files");

					try
					{

						try (DirectoryStream<Path> stream = Files.newDirectoryStream(p))
						{

							for (Path entry : stream)
							{
								if (Files.isDirectory(entry))
								{
									continue;
								}
								files.add(entry);
							}
							PLog.getLogger().info("list files done");
						}
						catch (Exception e)
						{
							PLog.getLogger().error(e.getMessage(), e);
						}
					}
					catch (Exception e)
					{
						PLog.getLogger().error(e);
					}
					finally
					{
						lock.unlock();
					}
				}
			}
			else
			{
				PLog.getLogger().warn("Could not acquire lock");
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage());
		}

	}

	private void moveFailedFile(Path p)
	{
		try
		{
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{
				try
				{
					PLog.getLogger().info("got lock for " + p.getFileName().toString());

					Path movefrom = p;
					Path target = FileSystems.getDefault().getPath(failedPath + File.separator + p.getFileName());
					// method 1
					Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
					PLog.getLogger().info("moved failed persistence file " + p.getFileName());
				}
				catch (Exception e)
				{
					PLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				PLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void moveFile(Path p)
	{
		try
		{
			if (lock.tryLock(6, TimeUnit.SECONDS))
			{
				try
				{
					PLog.getLogger().info("got lock for " + p.getFileName().toString());

					Path movefrom = p;
					Path target = FileSystems.getDefault().getPath(donePath + File.separator + p.getFileName());
					// method 1
					Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
					PLog.getLogger().info("moved " + p.getFileName());
				}
				catch (Exception e)
				{
					PLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				PLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	private Job persistInfoBox(InfoBox ib)
	{
		try
		{
			if (!validateInfoBox(ib))
			{
				PLog.getLogger().info("InfoBox VALIDATION FAILED");
				return null;
			}
			if (null == jobAnalysisService)
			{
				PLog.getLogger().error("\n\n\n\n\n\n\n\njobAnalysisService IS NULL\n\n\n\n\n\n\n");
				abort();
				return null;
			}
			PLog.getLogger().info("persistInfoBox");
			Job job = new Job();

			for (String s : ib.getAreasOfStudy())
			{
				job.getAreasOfStudy().add(capitalize(s, ib.getRegion()));
			}

			for (String s : ib.getCertification())
			{
				job.getCertifications().add(capitalize(s, ib.getRegion()));
			}

			for (String s : ib.getWorkSkills())
			{
				job.getWorkSkills().add(capitalize(s, ib.getRegion()));
			}

			PLog.getLogger().info("persistInfoBox " + ib.getJobTitles().get(0));
			String jobTitle = capitalize(ib.getJobTitles().get(0), ib.getRegion());

			job.setJobTitle(jobTitle);
			job.setFileId(ib.getFileId());
			job.setByExtrapolation(new Boolean(ib.isByExtrapolation()));
			job.setDegreeLevel(ib.getDegreeLevel());

			job.setQualifications(ib.getQualifications());
			job.setLocation(ib.getLocation());
			for (String s : ib.getSkills())
			{
				if (s.length() < 999)
				{
					job.getSkills().add(s);
				}
			}

			job.setYearsExperience(ib.getYearsExperience());
			job.setDetailLevel(new Integer(ib.getDetailLevel()));
			job.setUniqueJob(getOrCreateUniqueJob(jobTitle, ib.getRegion()));
			job.setYearsExperienceInt(ib.getYearsExperienceAsInt());

			for (String s : ib.getRelatedMajors())
			{
				job.getRelatedMajors().add(capitalize(s, ib.getRegion()));
			}

			job.setRegion(ib.getRegion());
			job.setContentSource(ib.getContentType());

			if (checkSalary(ib.getStartSalaryRange(), ib.getEndSalaryRange(), ib.getRegion()))
			{

				job.setSalaryStartRange(ib.getStartSalaryRange());
				job.setSalaryEndRange(ib.getEndSalaryRange());
			}

			PLog.getLogger().info("persistInfoBox saving " + job);

			jobAnalysisService.saveJob(job);
			PLog.getLogger().info("persistInfoBox done");
			return job;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private boolean processSalaryDataForJobs(CountDegree degree, Long ujId,
			Map<String, DescriptiveStatistics> statByRegionDegreeJobTitle)
	{
		try
		{

			boolean updateMade = false;
			List<Job> jobs = jobAnalysisService.getJobsByUniqueJobId(ujId);
			PLog.getLogger().debug("Count of jobs with this jobtitle: " + jobs.size());
			for (Job job : jobs)
			{
				PLog.getLogger().debug("Job is " + job.getJobTitle());

				if (job.containsAreaOfStudy(degree.getSpecifiedDegreeName()))
				{
					if (null == job.getSalaryEndRange() || null == job.getSalaryStartRange())
					{
						continue;
					}
					if (job.getSalaryEndRange() < 10000 || job.getSalaryStartRange() < 10000)
					{
						continue;
					}
					if (null == job.getRegion() || job.getRegion().isEmpty())
					{
						continue;
					}

					DescriptiveStatistics statsByDegreeandJob = statByRegionDegreeJobTitle.get(job.getRegion());

					if (null == statsByDegreeandJob)
					{
						statsByDegreeandJob = new DescriptiveStatistics();
						statByRegionDegreeJobTitle.put(job.getRegion(), statsByDegreeandJob);
					}
					int salary = (job.getSalaryStartRange() + job.getSalaryEndRange()) / 2;
					PLog.getLogger().debug("\n\n\n\nsalary is " + salary + "\njobtitle is " + job.getJobTitle()
							+ "\ndegree is " + degree.getSpecifiedDegreeName() + "\nregion is " + job.getRegion());

					statsByDegreeandJob.addValue(salary);
					updateMade = true;
				}
				else
				{
					PLog.getLogger().debug("jjob does not require specified degree. ");
				}
			} ///// ends jobs loop for jobs retrieved by degreeid
			return updateMade;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void run()
	{
		try
		{
			continueRun = true;
			PLog.getLogger().info("Starting Persistence Thread");
			int timeoutNoDataReceived = 0;
			while (continueRun)
			{
				if (lock.isHeldByCurrentThread())
				{
					PLog.getLogger().warn("Releasing lock before going to sleep. This is bad");
					lock.unlock();
				}
				Thread.sleep(sleepTime);
				PLog.getLogger().debug("In run.....");

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
					PLog.getLogger().info("\n\n\nEXITING THREAD\n\n\n");
					continueRun = false;
				}

			}
		}
		catch (InterruptedException e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return;

		}
		catch (Exception e)
		{
			PLog.getLogger().error("This is Bad. Thread won't start");
			PLog.getLogger().error(e.getMessage(), e);
		}
		catch (Error err)
		{
			PLog.getLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
			throw err;
		}

	}

	/**
	 * @param inconfig
	 * @param depends
	 * @param inlock
	 * @throws PersistenceException
	 */
	public void runPersistenceProcesses(ConfigurationBeanParent inconfig, boolean depends, ReentrantLock inlock)
			throws PersistenceException
	{
		try
		{
			PLog.getLogger().info(" Initializing Persistence Thread  ");
			if (inconfig.getAnalysisType().equals(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION))
			{
				config =  inconfig;
				pConfig=(ConfigurationBeanForPersistence_JobAnalysis)inconfig.getPersistConfig();
				if (jobAnalysisService.getDatabaseDescriptors().size() == 0)
				{
					PLog.getLogger().warn("\n\nBrand new database. Setting Locale from config.xml data to "
							+ config.getLanguagesForAnalysis() + "\n\n\n");
					DatabaseDescriptors dd = new DatabaseDescriptors();
					dd.setDatabaseLocale(config.getSpecifiedLanguage());
					dd.setDatabaseDescriptor(config.getDatabaseDescriptor());
					jobAnalysisService.saveDatabaseDescriptors(dd);
				}

				if (!jobAnalysisService.isCorrectLocale(config.getSpecifiedLanguage()))
				{
					PLog.getLogger().error("\n\nWRONG LANGUAGE LOCALE. ABORTING NOW\n\n\n");
					throw new PersistenceException("\n\nWRONG LANGUAGE LOCALE. ABORTING NOW\n\n\n");
				}

				if (!jobAnalysisService.isCorrectDatabaseDescriptor(config.getDatabaseDescriptor()))
				{
					PLog.getLogger().error("\n\nWRONG DATABASE DESCRIPTOR. ABORTING NOW\n\n\n");
					throw new PersistenceException("\n\nWRONG DATABASE DESCRIPTOR. ABORTING NOW\n\n\n");
				}

				lock = inlock;
				PLog.getLogger().debug("runPersistenceProcesses");
				stopDependsOnExternalCall = depends;
				if(depends)
				{
					upstreamThreadDone=false;
				}
				files = new ArrayList<Path>();
				donePath = pConfig.getPersistenceDonePath();
				failedPath = pConfig.getPersistenceFailedPath();
				ConfigurationBeanForTextAnalysis_Generic taConfig=(ConfigurationBeanForTextAnalysis_Generic)config.getTaConfig();
				infoBoxPath = taConfig.getTextAnalysisInfoBoxPath();
				majorMap = getMajorMap();
				if(!majorMap.getDatabaseDescriptor().equals(config.getDatabaseDescriptor()))
				{
					PLog.getLogger().error("\n\nWRONG DATABASE DESCRIPTOR IN MAJOR MAPPING CONFIG. ABORTING NOW\n\n\n");
					throw new PersistenceException("\n\nWRONG DATABASE DESCRIPTOR  IN MAJOR MAPPING CONFIG. ABORTING NOW\n\n\n");
				}

				if (!Files.exists(Paths.get(donePath)))
				{
					Files.createDirectories(Paths.get(donePath));
				}
				if (!Files.exists(Paths.get(failedPath)))
				{
					Files.createDirectories(Paths.get(failedPath));
				}
				if (!Files.exists(Paths.get(infoBoxPath)))
				{
					Files.createDirectories(Paths.get(infoBoxPath));
				}

				String[] inputs =
				{ taConfig.getTextAnalysisInfoBoxPath() };

				for (int i = 0; i < inputs.length; i++)
				{
					Path path = Paths.get(inputs[i]);
					inputPaths.add(path);
				}
				listFiles();
				persistenceMethods.initalizeRelations(majorMap);
				ready = true;
			}
			else
			{
				PLog.getLogger().error("\n\n\nI am NOT configured for this analysis. Why did you call me? \n\n");
				throw new PersistenceException("\n\n\nI am NOT configured for this analysis. Why did you call me?\n\n");
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceException(e);
		}

	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private void saveMediansForJobTitleByDegree(Long ujId, Long degreeId,
			Map<String, DescriptiveStatistics> statByRegionDegreeJobTitle)
	{
		try
		{
			Iterator<String> itr = statByRegionDegreeJobTitle.keySet().iterator();

			int countIterators = 0;
			while (itr.hasNext())
			{
				countIterators++;
				PLog.getLogger().debug("\n\n----------------------" + countIterators + "--------\n\n");

				String region = itr.next();
				DescriptiveStatistics statforregionDegreeAndJob = statByRegionDegreeJobTitle.get(region);
				ValueSalaryByDegreeAndJobTitle vsdjt = jobAnalysisService
						.getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(ujId, degreeId, region);

				PLog.getLogger().debug(region + ": Count Values accumulated = " + statforregionDegreeAndJob.getN());

				int median = new Integer((int) statforregionDegreeAndJob.getPercentile(50));

				vsdjt.setMedianSalary(median);
				PLog.getLogger().debug("vsdjt is " + vsdjt);
				jobAnalysisService.updateValueSalaryByDegreeAndJobTitle(vsdjt);
				PLog.getLogger().debug("Median set to " + vsdjt.getMedianSalary() + " for " + vsdjt.getDegreeName()
						+ " mvn compile" + "job title: " + vsdjt.getJobTitle() + " in region: " + region);
				statforregionDegreeAndJob.clear();
			}

			PLog.getLogger().debug("\n\nNext job for same degree\n\n\n\n\n\n\n");
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
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

	public void setUpstreamThreadDone(boolean stop)
	{
		PLog.getLogger().info("Calling STOP  on PersistenceThread");
		this.upstreamThreadDone = stop;
	}

	public boolean isUpstreamThreadDone()
	{
		return upstreamThreadDone;
	}

	private boolean validateInfoBox(InfoBox ib)
	{
		try
		{
			if (ib.getAreasOfStudy().size() < pConfig.getPersistIfCountAreasOfStudy())
			{
				PLog.getLogger().info("\nRejected by area of study: " + ib.getAreasOfStudy());
				ProcessStatus.incrementStatus("Persist Failed on Areas Of Study");
				return false;
			}
			if(ib.getAreasOfStudy().size()>10)
			{
			    PLog.getLogger().info("\nRejected by area of study: " + ib.getAreasOfStudy());
	                ProcessStatus.incrementStatus("Persist Failed on Certification");
	                return false;
			}
			if (ib.getCertification().size() < pConfig.getPersistIfCountCertification())
			{
				PLog.getLogger().info("\nRejected by certification: " + ib.getCertification());
				ProcessStatus.incrementStatus("Persist Failed on Certification");
				return false;
			}
			if (ib.getSkills().size() < pConfig.getPersistIfCountDetailLevel())
			{
				PLog.getLogger().info("\nRejected by skill: " + ib.getSkills());
				ProcessStatus.incrementStatus("Persist Failed on Skills");
				return false;
			}
			if (ib.getStartSalaryRange() < pConfig.getPersistIfValueMinimumSalary())
			{
				PLog.getLogger().info("\nRejected by salary:  " + ib.getStartSalaryRange());
				ProcessStatus.incrementStatus("Persist Failed on Salary");
				return false;
			}
			if(!ib.getDatabaseDescriptor().equals(config.getDatabaseDescriptor()))
			{
				PLog.getLogger().warn("\n\n\n\nInfo Box rejected by database descriptor mismatch:  " + ib.getDatabaseDescriptor());
				ProcessStatus.incrementStatus("Persist Failed on DB Descriptor");
				return false;
			}
			if(null==ib.getDegreeLevel())
			{
				PLog.getLogger().warn("\n\n\n\nInfo Box rejected by missing degreelevel:  " + ib.getDegreeLevel());
				ProcessStatus.incrementStatus("Persist Failed on DegreeLevel");
				return false;
			}
			boolean correctRegion=false;
			for(LanguageForAnalysis lfa:config.getLanguagesForAnalysis())
			{
				for(String r:lfa.getRegions())
				{
					if(r.toUpperCase().equals(ib.getRegion().toUpperCase()))
					{
						correctRegion=true;
					}
				}
			}
			if(!correctRegion)
			{
				PLog.getLogger().warn("\n\n\n\nInfo Box rejected by region mismatch:  " + ib.getDatabaseDescriptor());
				ProcessStatus.incrementStatus("Persist Failed on Region Mismatch");
			}
			return true;
		}
		catch (Exception e)
		{
			PLog.getLogger().debug(e.getMessage(), e);
			return false;
		}
	}

	private void valueSalaryByDegreeSubProcess(Job job, String degreeName, int salary, String degreeLevel)
	{
		try
		{
			CountDegree countDegree = jobAnalysisService.getCountDegree(degreeName, degreeLevel);
			PLog.getLogger().debug("countDegree: " + countDegree);

			ValueSalaryByDegree salaryByDegree = jobAnalysisService.getValueSalaryByDegree(countDegree.getId(),
					job.getRegion());
			PLog.getLogger().debug("valueSalaryByDegreeSubProcess: " + salaryByDegree);
			if (null == salaryByDegree)
			{

				salaryByDegree = new ValueSalaryByDegree();
				salaryByDegree.setCountDegree(countDegree);
				salaryByDegree.setCount(new Integer(1));
				salaryByDegree.setDegreeName(degreeName);
				salaryByDegree.setTotalSalary(new Long(salary));
				salaryByDegree.setRegion(job.getRegion());

				jobAnalysisService.saveValueSalaryByDegree(salaryByDegree);
			}
			else
			{
				Integer count = salaryByDegree.getCount();
				Integer newCount = new Integer(count.intValue() + 1);
				salaryByDegree.setCount(newCount);

				Long totalSalary = salaryByDegree.getTotalSalary();
				Long newTotalSalary = new Long(totalSalary.longValue() + salary);
				salaryByDegree.setTotalSalary(newTotalSalary);

				long averageSalary = salaryByDegree.getTotalSalary().longValue() / salaryByDegree.getCount().intValue();
				salaryByDegree.setAverageSalary(new Integer((int) averageSalary));
				PLog.getLogger().debug("average salary for major in " + degreeName + ":  " + averageSalary);
				jobAnalysisService.updateValueSalaryByDegree(salaryByDegree);
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().debug(e.getMessage(), e);

		}
	}

	private void valueSalaryByDegree(Job job)
	{
		try
		{
			PLog.getLogger().warn("valueSalaryByDegree ");
			/// try to avoid invalid or outlier data
			if (!checkSalary(job.getSalaryStartRange(), job.getSalaryEndRange(), job.getRegion()))
			{
				PLog.getLogger().warn("Invalid: salary ");
				return;
			}

			if (null == job.getRegion() || job.getRegion().isEmpty())
			{
				PLog.getLogger().warn("Invalid: No region ");
				return;
			}
			int salary = (job.getSalaryStartRange().intValue() + job.getSalaryEndRange().intValue()) / 2;

			if (job.getDegreeLevel().equals(CountDegree.DEGREELEVEL)
					||job.getDegreeLevel().equals(CountDegree.CERTIFICATEANDDEGREELEVEL))
			{
				for (String degreeName : job.getAreasOfStudy())
				{
					PLog.getLogger().debug("Processing at degree level ");
					valueSalaryByDegreeSubProcess(job, degreeName, salary, CountDegree.DEGREELEVEL);
				}
			}
			if (job.getDegreeLevel().equals(CountDegree.CERTIFICATELEVEL)
					||job.getDegreeLevel().equals(CountDegree.CERTIFICATEANDDEGREELEVEL))
			{
				for (String degreeName : job.getCertifications())
				{
					PLog.getLogger().debug("Processing at certificate level ");
					valueSalaryByDegreeSubProcess(job, degreeName, salary, CountDegree.CERTIFICATELEVEL);
				}
			}

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void valueSalaryByDegreeAndJobTitleSubProcess(Job job, String degreeName, int salary, String degreeLevel)
	{
		try
		{
			PLog.getLogger().debug("valueSalaryByDegreeAndJobTitleSubProcess  ");
			CountDegree countDegree = jobAnalysisService.getCountDegree(degreeName, degreeLevel);

			ValueSalaryByDegreeAndJobTitle salaryByDegreeJobTitle = jobAnalysisService
					.getValueSalaryByDegreeAndJobTitleByJobTitleAndDegree(job.getUniqueJob().getId(),
							countDegree.getId(), job.getRegion());
			if (null == salaryByDegreeJobTitle)
			{

				salaryByDegreeJobTitle = new ValueSalaryByDegreeAndJobTitle();
				salaryByDegreeJobTitle.setCountDegree(countDegree);
				salaryByDegreeJobTitle.setCount(new Integer(1));
				salaryByDegreeJobTitle.setDegreeName(degreeName);
				salaryByDegreeJobTitle.setTotalSalary(new Long(salary));
				salaryByDegreeJobTitle.setRegion(job.getRegion());
				salaryByDegreeJobTitle.setJobTitle(job.getJobTitle());
				salaryByDegreeJobTitle.setUniqueJob(job.getUniqueJob());

				jobAnalysisService.saveValueSalaryByDegreeAndJobTitle(salaryByDegreeJobTitle);
				PLog.getLogger().debug("valueSalaryByDegreeAndJobTitle new " + salaryByDegreeJobTitle);
			}
			else
			{
				Integer count = salaryByDegreeJobTitle.getCount();
				Integer newCount = new Integer(count.intValue() + 1);
				salaryByDegreeJobTitle.setCount(newCount);

				Long totalSalary = salaryByDegreeJobTitle.getTotalSalary();
				Long newTotalSalary = new Long(totalSalary.longValue() + salary);
				salaryByDegreeJobTitle.setTotalSalary(newTotalSalary);

				long averageSalary = salaryByDegreeJobTitle.getTotalSalary().longValue()
						/ salaryByDegreeJobTitle.getCount().intValue();
				salaryByDegreeJobTitle.setAverageSalary(new Integer((int) averageSalary));
				PLog.getLogger().debug("average salary for major in " + degreeName + ":  " + averageSalary);
				jobAnalysisService.updateValueSalaryByDegreeAndJobTitle(salaryByDegreeJobTitle);
				PLog.getLogger().debug("valueSalaryByDegreeAndJobTitle update " + salaryByDegreeJobTitle);
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void valueSalaryByDegreeAndJobTitle(Job job)
	{
		try
		{
			/// try to avoid invalid or outlier data
			PLog.getLogger().debug("valueSalaryByDegreeAndJobTitle");
			if (!checkSalary(job.getSalaryStartRange(), job.getSalaryEndRange(), job.getRegion()))
			{
				PLog.getLogger().warn("valueSalaryByDegreeAndJobTitle: In valid salary, aborting " + job.getJobTitle()
						+ " | " + job.getSalaryStartRange() + " | " + job.getSalaryEndRange());
				return;
			}

			if (null == job.getRegion() || job.getRegion().isEmpty())
			{
				PLog.getLogger().warn("Invalid: No region ");
				return;
			}

			int salary = (job.getSalaryStartRange().intValue() + job.getSalaryEndRange().intValue()) / 2;

			if (job.getDegreeLevel().equals(CountDegree.DEGREELEVEL)
					||job.getDegreeLevel().equals(CountDegree.CERTIFICATEANDDEGREELEVEL))
			{
				for (String degreeName : job.getAreasOfStudy())
				{
					PLog.getLogger().debug("Processing at degree level ");
					valueSalaryByDegreeAndJobTitleSubProcess(job, degreeName, salary, CountDegree.DEGREELEVEL);
				}
			}

			if (job.getDegreeLevel().equals(CountDegree.CERTIFICATELEVEL)
					||job.getDegreeLevel().equals(CountDegree.CERTIFICATEANDDEGREELEVEL))
			{
				for (String degreeName : job.getCertifications())
				{
					PLog.getLogger().debug("Processing at salary level ");
					valueSalaryByDegreeAndJobTitleSubProcess(job, degreeName, salary, CountDegree.CERTIFICATELEVEL);
				}
			}

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void valueSalaryByJobTitle(Job job)
	{
		PLog.getLogger().debug("valueSalaryByJobTitle");
		try
		{
			/// try to avoid invalid or outlier data
			if (!checkSalary(job.getSalaryStartRange(), job.getSalaryEndRange(), job.getRegion()))
			{
				PLog.getLogger().warn("valueSalaryByJobTitle: Invalid salary, aborting " + job.getJobTitle() + " | "
						+ job.getSalaryStartRange() + " | " + job.getSalaryEndRange());
				return;
			}
			if (null == job.getRegion() || job.getRegion().isEmpty())
			{
				PLog.getLogger().warn("Invalid: No region ");
				return;
			}
			int salary = (job.getSalaryStartRange().intValue() + job.getSalaryEndRange().intValue()) / 2;

			ValueSalaryByJobTitle salaryByJobTitle = jobAnalysisService
					.getValueSalaryByJobTitle(job.getUniqueJob().getId(), job.getRegion());
			if (null == salaryByJobTitle)
			{
				salaryByJobTitle = new ValueSalaryByJobTitle();
				salaryByJobTitle.setCount(new Integer(1));
				salaryByJobTitle.setJobTitle(job.getJobTitle());
				salaryByJobTitle.setUniqueJob(job.getUniqueJob());
				salaryByJobTitle.setTotalSalary(new Long(salary));
				salaryByJobTitle.setRegion(job.getRegion());

				jobAnalysisService.saveValueSalaryByJobTitle(salaryByJobTitle);
				PLog.getLogger().debug("valueSalaryByJobTitle save: " + salaryByJobTitle);
			}
			else
			{
				Integer count = salaryByJobTitle.getCount();
				Integer newCount = new Integer(count.intValue() + 1);
				salaryByJobTitle.setCount(newCount);

				Long totalSalary = salaryByJobTitle.getTotalSalary();
				Long newTotalSalary = new Long(totalSalary.longValue() + salary);
				salaryByJobTitle.setTotalSalary(newTotalSalary);

				long averageSalary = salaryByJobTitle.getTotalSalary().longValue()
						/ salaryByJobTitle.getCount().intValue();

				PLog.getLogger().debug("average salary for " + job.getJobTitle() + ":  " + averageSalary);

				salaryByJobTitle.setAverageSalary(new Integer((int) averageSalary));

				jobAnalysisService.updateValueSalaryByJobTitle(salaryByJobTitle);
				PLog.getLogger().debug("valueSalaryByJobTitle update: " + salaryByJobTitle);
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
		}
	}
}
