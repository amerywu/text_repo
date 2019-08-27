package ikoda.persistenceforanalysis.application;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.netio.config.ConfigurationBeanForPersistence_JobDescriptionAnalysis;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis;
import ikoda.netio.config.ConfigurationBeanParent;

import ikoda.persistence.application.PLog;
import ikoda.persistence.model.Job;
import ikoda.persistence.service.JobAnalysisService;
import ikoda.persistenceforanalysis.singletons.RawDataUnit;
import ikoda.persistenceforanalysis.singletons.TextQueue;
import ikoda.utils.IDGenerator;
import ikoda.utils.ProcessStatus;

public class PersistenceForAnalysisThread extends Thread
{

	private final static int SLEEPTIMEDEFAULT = 1000;
	private final static String JOBID = "jobId";
	private final static String JOBTITLE = "jobTitle";
	private final static String AREAOFSTUDY = "AreaOfStudy";

	private final static String TXT = ".txt";

	private static int percentToSample;
	private static long biteSize;

	private static int minDetailLevel;
	private long persistenceProcessStartTime = System.currentTimeMillis();
	private long countEntriesInBite;
	private long countEntriesProcessedInBite;
	

	private int sleepTime = SLEEPTIMEDEFAULT;
	private ConfigurationBeanParent config;
	private boolean ready = false;
	int source = 0;

	private boolean stopDependsOnExternalCall;
	boolean continueRun = true;
	private boolean stop = false;
	private boolean elegantlyCompleted=false;

	private ReentrantLock lock;

	private Long maxId;
	private Long minId;
	
	private Map<Long, Long> duplicateMap = new HashMap<Long, Long>();
	private boolean pause;

	@Autowired
	private JobAnalysisService jobAnalysisService;

	public PersistenceForAnalysisThread()
	{
		PLog.getLogger().info("\n\n\n\n\n   INIT  \n\n\n\n");
	}

	public void abort()
	{

		this.stop = true;
	}

	private String capitalize(String line)
	{
		try
		{
			PLog.getLogger().debug(line);
			StringTokenizer token = new StringTokenizer(line);
			String capLine = "";
			while (token.hasMoreTokens())
			{
				String tok = token.nextToken().toString();
				capLine += Character.toUpperCase(tok.charAt(0)) + tok.substring(1);
				capLine += " ";
			}
			PLog.getLogger().debug(line);
			return capLine.trim();
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return line;
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private synchronized boolean doPersistenceProcess()
	{
		PLog.getLogger().debug("\n\ndoPersistenceProcess\n\n\n");
		persistenceProcessStartTime = System.currentTimeMillis();


		if (stop)
		{
			PLog.getLogger().warn("\n\n\nAbort called\n\n\n");
			return false;
		}
		if(pause)
		{
			PLog.getLogger().debug("\n\nPausing Persistence Queries...\n");
			ProcessStatus.put("_Persistence Status", "Paused");
			return true;
		}
		try
		{
			ProcessStatus.put("_Persistence Status", "Running");
			StringBuffer sb = new StringBuffer();
			long startTime = System.currentTimeMillis();

			List<Job> jobs = jobAnalysisService.getRandomJobListWithinIdRange(30, (maxId - biteSize), maxId,
					minDetailLevel);
			long endTime = System.currentTimeMillis() - startTime;

			sb.append("Query Took " + endTime);
			sb.append("\ngetRandomJobListWithinIdRange returned with size " + jobs.size() + "\n\n");

			for (Job job : jobs)
			{
				sb.append("\nID:" + job.getId() + " Created:" + job.getCreated() + " detailLevel " + job.getDetailLevel());
				processJob(job);

			}
			
			sb.append("\n--------------\nsuccess count: " + countEntriesProcessedInBite);
			sb.append("\ntotal entries count: " + countEntriesInBite);
			double percentComplete = ((double) countEntriesProcessedInBite / (double) countEntriesInBite) * 100;
			sb.append("\nPercentComplete: " + percentComplete + "%");
			PLog.getLogger().debug(sb);
			if (percentComplete >= percentToSample)
			{
				return newBite();

			}
			return true;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return true;
		}
	}

	public long getFileProcessTime()
	{
		return System.currentTimeMillis() - persistenceProcessStartTime;
	}

	public JobAnalysisService getJobAnalysisService()
	{
		return jobAnalysisService;
	}

	protected int getSleepTime()
	{
		return sleepTime;
	}

	public boolean isContinueRun()
	{
		return continueRun;
	}

	private boolean isDuplicate(Job job)
	{
		if (null != duplicateMap.get(job.getId()))
		{
			return true;
		}
		duplicateMap.put(job.getId(), job.getId());
		return false;
	}

	public boolean isElegantlyCompleted()
	{
		return elegantlyCompleted;
	}



	public boolean isPause()
	{
		return pause;
	}

	public boolean isStop()
	{
		return stop;
	}
	
	private void newBiteRange()
	{
		maxId = new Long(maxId - biteSize);

		minId= maxId - biteSize;
		if (minId < 0)
		{
			minId = new Long(0);
		}

	}

	private boolean newBite()
	{
		try
		{
			PLog.getLogger().info("\n\n\nNEW BITE\n\n");
			
			
			duplicateMap.clear();
			countEntriesProcessedInBite = 0;
			countEntriesInBite=0;
			while(countEntriesInBite<2)
			{
				newBiteRange();
				PLog.getLogger().info("New maxId: " + maxId);
				if (maxId < 0)
				{
					PLog.getLogger().info("ENDING. NO NEW BITE");
					elegantlyCompleted=true;
					return false;
				}

				PLog.getLogger().info("New minId: " + minId);
				
				countEntriesInBite = jobAnalysisService.getCountJobListWithinIdRange(minId, maxId,
						minDetailLevel);
				ProcessStatus.put("_Current Max Id", maxId);
			}

			
			
			ConfigurationBeanForPersistence_JobDescriptionAnalysis pconfig=(ConfigurationBeanForPersistence_JobDescriptionAnalysis)config.getPersistConfig();
			biteSize=pconfig.getBiteSize();
			return true;

		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			PLog.getLogger().info("ENDING. on error");
			return false;
		}
	}

	private void processJob(Job job)
	{
		try
		{
			if (isDuplicate(job))
			{
				PLog.getLogger().debug("processJob duplicate record");
				return;
			}

			
			RawDataUnit rdu = new RawDataUnit();
			rdu.setId(String.valueOf(IDGenerator.getInstance().nextID()));
			rdu.setJobTitle(job.getJobTitle());
			rdu.setAreasOfStudy(job.getAreasOfStudy());
			rdu.setRegion(job.getRegion());

			rdu.setSkills(job.getSkills());
			rdu.setLocation(job.getLocation());
			
			TextQueue.getInstance().add(rdu);
			
			//PLog.getLogger().debug("Sentence count: "+rdu.getSkills().size());

			countEntriesProcessedInBite++;
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
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

	public void runPersistenceProcesses(ConfigurationBeanParent inconfig, boolean depends, ReentrantLock inlock)
			throws PersistenceForAnalysisException
	{
		try
		{
			if (inconfig.getAnalysisType().equals(ConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION))
			{
				PLog.getLogger().info(" Initializing Persistence Thread  ");
				IDGenerator.getInstance().resetId();
				config = inconfig;
				lock = inlock;
				PLog.getLogger().debug("runPersistenceProcesses");
				stopDependsOnExternalCall = depends;

				ConfigurationBeanForPersistence_JobDescriptionAnalysis pConfig = (ConfigurationBeanForPersistence_JobDescriptionAnalysis)config.getPersistConfig();
				ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis tConfig=(ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis)config.getTaConfig();
				percentToSample = pConfig.getPercentToSample();
				biteSize = pConfig.getBiteSize();
				minDetailLevel = pConfig.getMinDetailLevel();
				
				PLog.getLogger().info("biteSize " + biteSize);
				PLog.getLogger().info("percentToSample " + percentToSample);
				PLog.getLogger().info("minDetailLevel " + minDetailLevel);
				
				String csvPathString = tConfig.getCsvPath();
				

				if (!Files.exists(Paths.get(csvPathString)))
				{
					Files.createDirectories(Paths.get(csvPathString));
				}

				PLog.getLogger().debug("calling getMaxJobId");
				maxId = jobAnalysisService.getMaxJobId();
				PLog.getLogger().info("MaxId:"+maxId);
				newBite();

				ready = true;
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			throw new PersistenceForAnalysisException(e);
		}

	}

	public void setElegantlyCompleted(boolean elegantlyCompleted)
	{
		this.elegantlyCompleted = elegantlyCompleted;
	}

	public void setJobAnalysisService(JobAnalysisService jobAnalysisService)
	{
		this.jobAnalysisService = jobAnalysisService;
	}

	public void setPause(boolean pause)
	{
		this.pause = pause;
	}

	protected void setSleepTime(int sleepTime)
	{
		this.sleepTime = sleepTime;
	}

	public void setStop(boolean stop)
	{
		
		this.stop = stop;
	}

}
