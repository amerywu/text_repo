package ikoda.persistence.application;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ikoda.netio.config.ConfigurationBeanForPersistence_JobAnalysis;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.MajorMappingConfig;
import ikoda.persistence.model.DatabaseDescriptors;
import ikoda.persistence.model.IkodaDegree;
import ikoda.persistence.service.ComparatoByQueuePosition;
import ikoda.persistence.service.JobAnalysisService;
import ikoda.persistence.service.JobReportingByIkodaDegreeService;
import ikoda.persistence.service.JobReportingManager;
import ikoda.persistence.service.QueueObject;

@Component
public class PersistenceReportingThread extends Thread
{

	private final static int SLEEPTIMEDEFAULT = 3000;

	private int sleepTime = SLEEPTIMEDEFAULT;
	private ConfigurationBeanParent config;
	private ConfigurationBeanForPersistence_JobAnalysis pConfig;
	private boolean ready = false;
	private List<IkodaDegree> iKodaDegreeList = new ArrayList<IkodaDegree>();
	private double iKodaDegreeListStartSize;
	private List<QueueObject> cpcJobQueue = new ArrayList<QueueObject>();

	@Autowired
	private JobReportingManager jobReportingManager;

	boolean continueRun = true;
	private boolean abort = false;
	private boolean startRun = true;
	private long persistenceProcessStartTime = System.currentTimeMillis();
	private MajorMappingConfig majorMergeMap;

	@Autowired
	private JobAnalysisService jobAnalysisService;

	@Autowired
	private JobReportingByIkodaDegreeService jobReportingService;

	public PersistenceReportingThread()
	{
		PLog.getRLogger().info("\n\n\n\n\n PersistenceChoresThread  INIT  \n\n\n\n");
	}

	public void abort()
	{
		PLog.getRLogger().info("\n\nabort called \n\n\n\n");
		this.abort = true;
		jobReportingManager.setAbort(true);
		continueRun=false;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private synchronized boolean doPersistenceProcess()
	{

		persistenceProcessStartTime = System.currentTimeMillis();

		if (abort)
		{
			PLog.getRLogger().warn("\n\n\n Abort called \n\n\n");
			return false;
		}
		
		try
		{
			PLog.getRLogger().info("\n\n\n\n-----------------------------------doPersistenceReporting------------------------\n\n");
			PLog.getRLogger().info("\n\nRemaining Ikoda Degrees: " + iKodaDegreeList.size() + " \n");
			PLog.getRLogger().info("\n\nRemaining Unique Jobs: " + (cpcJobQueue.size()) + " \n\n");
			if (startRun)
			{
				List<QueueObject> localcpcJobQueue = jobReportingManager.populateCPCPayByJobTitleAndAnyDegree();
				startRun = false;
				cpcJobQueue.addAll(localcpcJobQueue);
			}
			if (iKodaDegreeList.size() > 0)
			{
				IkodaDegree id = iKodaDegreeList.remove(0);

				List<QueueObject> localcpcJobQueue = jobReportingManager.populateCPCPayByJobTitleAndDegree(id.getId(),
						id.getIkodaDegreeName());
				cpcJobQueue.addAll(localcpcJobQueue);
				Collections.sort(cpcJobQueue, new ComparatoByQueuePosition());
			}
			else if (cpcJobQueue.size() > 0)
			{
				QueueObject qo = cpcJobQueue.remove(0);
				jobReportingManager.populateCPCJobDetail(qo.getUniqueJobId(), qo.getDegreeName(), qo.getCountDegreeId(),
						qo.getRegion(), qo.getIkodaDegreeId(), qo.getiKodaDegreeName());
			}

			if (cpcJobQueue.size() == 0 && iKodaDegreeList.size() == 0)
			{
				PLog.getRLogger().info("\n\n\n\n++------------------------------- ALL TASKS COMPLETE --------------------++\n\n");
				return false;
			}

			PLog.getRLogger().info(
					"\n\n\n\n++-------------------------------done doPersistenceReporting---------------------++\n\n");
			return true;
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			return false;
		}

	}

	public long getFileProcessTime()
	{
		return System.currentTimeMillis() - persistenceProcessStartTime;
	}
	
	private MajorMappingConfig getMajorMergeMap() throws PersistenceException
	{

		try
		{
		

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
			InputStream is = this.getClass().getResourceAsStream("/" + "majorMergeConfig"+ locale + ".xml");

			JAXBContext jaxbContext = JAXBContext.newInstance(MajorMappingConfig.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			MajorMappingConfig config = (MajorMappingConfig) jaxbUnmarshaller.unmarshal(is);
			PLog.getLogger().debug("MajorMappingConfig " + config);
			return config;
		}
		catch (Exception e)
		{
			PLog.getLogger().error("\n\n\nABORTING:\n"+e.getMessage(), e);
			continueRun=false;
			throw new PersistenceException(e.getMessage(), e);

		}
		
	}

	public JobAnalysisService getJobAnalysisService()
	{
		return jobAnalysisService;
	}

	public JobReportingManager getJobReportingManager()
	{
		return jobReportingManager;
	}

	public JobReportingByIkodaDegreeService getJobReportingService()
	{
		return jobReportingService;
	}

	protected int getSleepTime()
	{
		return sleepTime;
	}

	public boolean isContinueRun()
	{
		return continueRun;
	}

	public boolean isStop()
	{
		return abort;
	}

	@Override
	public void run()
	{
		try
		{
			continueRun = true;
			PLog.getRLogger().info("Starting Persistence Thread");
			int timeoutNoDataReceived = 0;
			while (continueRun)
			{

				Thread.sleep(sleepTime);
				PLog.getRLogger().debug("In run.....");

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
					PLog.getRLogger().info("\n\n\nEXITING THREAD\n\n\n");
					continueRun = false;
				}

			}
		}
		catch (InterruptedException e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			return;

		}
		catch (Exception e)
		{
			PLog.getRLogger().error("This is Bad. Thread won't start");
			PLog.getRLogger().error(e.getMessage(), e);
		}
		catch (Error err)
		{
			PLog.getRLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
			throw err;
		}

	}

	public void runPersistenceReporting(ConfigurationBeanParent inconfig, boolean depends)
			throws PersistenceException
	{
		try
		{
			PLog.getRLogger().info(" Initializing Persistence Reporting Thread  ");
			
			config = inconfig;
			pConfig=(ConfigurationBeanForPersistence_JobAnalysis)inconfig.getPersistConfig();
			if (jobAnalysisService.getDatabaseDescriptors().size() == 0)
			{
				PLog.getRLogger().warn("\n\nBrand new database. Setting Locale from config.xml data to "
						+ config.getLanguagesForAnalysis() + "\n\n\n");
				DatabaseDescriptors dd = new DatabaseDescriptors();
				dd.setDatabaseLocale(config.getSpecifiedLanguage());
				jobAnalysisService.saveDatabaseDescriptors(dd);

			}
			if (!jobAnalysisService.isCorrectLocale(config.getSpecifiedLanguage()))
			{
				PLog.getRLogger().error("\n\nWRONG LANGUAGE LOCALE. ABORTING NOW\n\n\n");
				throw new PersistenceException("\n\nWRONG LANGUAGE LOCALE. ABORTING NOW\n\n\n");
			}
			
			if (!jobAnalysisService.isCorrectDatabaseDescriptor(config.getDatabaseDescriptor()))
			{
				PLog.getLogger().error("\n\nWRONG DATABASE DESCRIPTOR. ABORTING NOW\n\n\n");
				throw new PersistenceException("\n\nWRONG DATABASE DESCRIPTOR. ABORTING NOW\n\n\n");
			}

			PLog.getRLogger().debug("runPersistenceProcesses");

			majorMergeMap=getMajorMergeMap();
			jobReportingManager.populateCPCSalaryByDegree(config,majorMergeMap);
			iKodaDegreeList = jobReportingManager.getAllIkodaDegrees();
			iKodaDegreeListStartSize = iKodaDegreeList.size();
			

			ready = true;
		}
		catch (Exception e)
		{
			PLog.getRLogger().error(e.getMessage(), e);
			continueRun = false;
			throw new PersistenceException(e);
		}

	}

	public void setJobAnalysisService(JobAnalysisService jobAnalysisService)
	{
		this.jobAnalysisService = jobAnalysisService;
	}

	public void setJobReportingManager(JobReportingManager jobReportingManager)
	{
		this.jobReportingManager = jobReportingManager;
	}

	public void setJobReportingService(JobReportingByIkodaDegreeService jobReportingService)
	{
		this.jobReportingService = jobReportingService;
	}

	protected void setSleepTime(int sleepTime)
	{
		this.sleepTime = sleepTime;
	}

	public void setStop(boolean stop)
	{
		PLog.getRLogger().info("Calling STOP  on PersistenceChoresThread");
		this.abort = stop;
	}
}
