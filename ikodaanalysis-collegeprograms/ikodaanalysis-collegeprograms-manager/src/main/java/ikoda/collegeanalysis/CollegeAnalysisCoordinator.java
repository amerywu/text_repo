package ikoda.collegeanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ikoda.fileio.CollegeFileIoThread;
import ikoda.netio.CollegeNetioThread;
import ikoda.netio.config.ConfigurationBeanFactory;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InterfaceConfigurationBeanParent;
import ikoda.nlp.analysis.CollegeProgramAnalyzerThread2;
import ikoda.nlp.analysis.CollegeURLAnalyzerThread;
import ikoda.nlp.analysis.FileAnalyzerThreadManagement;
import ikoda.utils.ProcessStatus;

public abstract class CollegeAnalysisCoordinator extends Thread
{

	private static final String TXT = ".txt";
	protected int sleepTime = 30000;
	protected ConfigurationBeanParent config;
	protected boolean ready = false;
	int source = 0;
	protected final ReentrantLock lock = new ReentrantLock();
	boolean restartTA = false;
	boolean restartFileio = false;
	long restartTATime = 0;
	boolean createNewNetio = false;
	boolean continueRun = true;
	protected CollegeURLAnalyzerThread trainingThread = null;
	protected Logger logger = LogManager.getLogger(this.getClass());
	protected CollegeFileIoThread fileio = new CollegeFileIoThread();
	protected List<CollegeProgramAnalyzerThread2> taThreadsList = new ArrayList<>();
	protected List<CollegeNetioThread> netioThreads = new ArrayList<CollegeNetioThread>();
	private List<String> logIgnoreList = new ArrayList<>(Arrays.asList("Netio 02","Low Call Count","High Fail", "Excessive Accumulated Calls"));
	protected int cycleCount = 0;

	public CollegeAnalysisCoordinator()
	{
		super();
	}

	public CollegeAnalysisCoordinator(Runnable arg0)
	{
		super(arg0);
	}

	public CollegeAnalysisCoordinator(String arg0)
	{
		super(arg0);
	}

	public CollegeAnalysisCoordinator(ThreadGroup arg0, Runnable arg1)
	{
		super(arg0, arg1);
	}

	public CollegeAnalysisCoordinator(ThreadGroup arg0, String arg1)
	{
		super(arg0, arg1);
	}

	public CollegeAnalysisCoordinator(Runnable arg0, String arg1)
	{
		super(arg0, arg1);
	}

	public CollegeAnalysisCoordinator(ThreadGroup arg0, Runnable arg1, String arg2)
	{
		super(arg0, arg1, arg2);
	}

	public CollegeAnalysisCoordinator(ThreadGroup arg0, Runnable arg1, String arg2, long arg3)
	{
		super(arg0, arg1, arg2, arg3);
	}

	protected void checkRestarts()
	{
	    /////////////////// restart after shut down of thread
	    try
	    {
	        if (restartFileio)
	        {
	            ProcessStatus.incrementStatus("Fileio Thread Fails");
	
	            logger.info("\n\n RESTARTING FILEIO THREAD \n\n");
	            fileio = new CollegeFileIoThread();
	            fileio.runFileIo(config.isRunNetio(), config, lock);
	            fileio.start();
	            restartFileio = false;
	        }
	
	        if (restartTA)
	        {
	            ProcessStatus.incrementStatus("Text Analysis Thread Fails");
	            if ((System.currentTimeMillis() - restartTATime) > 300000)
	            {
	                Random randomGenerator = new Random();
	                logger.info("\n\n RESTARTING TEXT ANALYSIS THREAD \n\n\n\n\n\n");
	                CollegeProgramAnalyzerThread2 taThread = new CollegeProgramAnalyzerThread2(
	                        randomGenerator.nextInt(10000));
	                taThread.runFileAnalyzer(config.isRunFileio(), config, lock);
	                taThread.start();
	                taThreadsList.add(taThread);
	
	                restartTA = false;
	            }
	        }
	    }
	    catch (Exception e)
	    {
	        logger.error(e);
	    }
	}
	
	protected abstract  boolean doFileProcess();

	protected void checkTimeOuts()
	{
	    try
	    {
	        Iterator<CollegeNetioThread> itr = netioThreads.iterator();
	
	        CollegeNetioThread newNetio = null;
	        while (itr.hasNext())
	        {
	            CollegeNetioThread netio = itr.next();
	
	            if (netio.getUrlCallDurationInMillis() > 240000 && true == netio.isContinueRun())
	            {
	                logger.warn("\n\nNetio Thread timed out\n\n");
	                newNetio = new CollegeNetioThread();
	                newNetio.reviveAfterThreadHang(netio);
	                createNewNetio = true;
	                netio.setStopNow(true);
	                itr.remove();
	            }
	        }
	        if (createNewNetio)
	        {
	            logger.info("\n\n\n\n\nRESTARTING netio due to thread hang\n\n\n\n\n");
	            newNetio.start();
	            netioThreads.add(newNetio);
	            createNewNetio = false;
	        }
	    
	
	        if (null != fileio && config.isRunFileio())
	        {
	            ProcessStatus.getStatusMap().put("FileioThread currentFile currentFileProcessTime",
	                    String.valueOf(fileio.getFileProcessTime()));
	
	            if (!fileio.isUpstreamThreadDone())
	            {
	                if (fileio.getFileProcessTime() > 180000)
	                {
	                    logger.info("\n\n\n\n\naAborting fileio due to thread hang\n\n\n\n\n");
	                    
	                    fileio.interrupt();
	                    restartFileio = true;
	                }
	            }
	        }
	
	        if (!(taThreadsList.isEmpty()) && config.isRunTextAnalysis())
	        {
	
	            Iterator<CollegeProgramAnalyzerThread2> itrTA = taThreadsList.iterator();
	
	            while (itrTA.hasNext())
	            {
	                FileAnalyzerThreadManagement taThread = itrTA.next();
	                ProcessStatus.getStatusMap().put("TA currentFileProcessTime",
	                        String.valueOf(taThread.getFileProcessTime()));
	                if (!taThread.isUpstreamThreadDone())
	                {
	                	try
	                	{
	                    if (taThread.getFileProcessTime() > 1000000 && !restartTA)
	                    {
	                        logger.info("\n\nTimeout, Aborting. Restart in 5 minutes. \n\n\n\n\n\n");
	                        taThread.restart();
	                        taThread.interrupt();
	                        restartTA = true;
	                        restartTATime = System.currentTimeMillis();
	                        itrTA.remove();
	                    }
	                	}
	                	catch(Exception e)
	                	{
	                		logger.warn("\nMay be benign threading issue at init \n"+e.getMessage(),e);
	                	}
	                }
	            }
	        }
	    }
	    catch (Exception e)
	    {
	        logger.error("Failed to check times. ABORTING"+e.getMessage(), e);
	        stopProcedure_elegantStop(new StringBuilder());
	        continueRun=false;
	        
	    }
	}

	protected void printLog()
	{
		if(cycleCount % 25 ==0)
		{

	       logger.info(ProcessStatus.print());
		}
		else
		{
			logger.info(ProcessStatus.print(logIgnoreList));
		}

	}

	protected ConfigurationBeanParent getConfig()
	{
	
	    try
	    {
	
	        return (ConfigurationBeanParent) ConfigurationBeanFactory.getInstance().getConfigurationBean(
	                ConfigurationBeanParent.COLLEGE_ANALYSIS_CONFIGURATION,
	                InterfaceConfigurationBeanParent.FILENAME_PARENT, lock);
	
	    }
	    catch (Exception e)
	    {
	
	        logger.error(e.getMessage(), e);
	        logger.warn("Failed to update configuration");
	        return config;
	
	    }
	
	}

	protected int getSleepTime()
	{
	    return sleepTime;
	}

	@Override
	public void run()
	{
	
	    try
	    {
	
	
	        logger.info("Starting College Analysis Thread");
	        int timeoutNoDataReceived = 0;
	        while (continueRun)
	        {
	            if (lock.isHeldByCurrentThread())
	            {
	                logger.warn("Releasing lock before going to sleep. This is bad");
	                lock.unlock();
	            }
	            logger.info("Going to sleep for " + sleepTime + " millis.");
	            Thread.sleep(sleepTime);
	
	            ConfigurationBeanParent cfg = getConfig();
	
	            if (null != cfg)
	            {
	                config = cfg;
	                logger.debug("updated cfg isElegantStop=" + config.isElegantStop());
	            }
	            if (null == config || ready == false)
	            {
	                timeoutNoDataReceived++;
	                if (timeoutNoDataReceived >= 2000)
	                {
	                    continueRun = false;
	                }
	            }
	            else if (!doFileProcess())
	            {
	                logger.info(ProcessStatus.print());
	                logger.info("EXITING THREAD");
	
	                continueRun = false;
	            }
	
	        }
	    }
	    catch (InterruptedException e)
	    {
	        logger.error(e.getMessage(), e);
	
	    }
	    catch (Exception e)
	    {
	        logger.error("This is Bad. Thread won't start");
	        logger.error(e.getMessage(), e);
	    }
	    catch (Error err)
	    {
	        logger.fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
	        throw err;
	    }
	
	}

	protected void saveConfig()
	{
	
	    try
	    {
	
	        ConfigurationBeanFactory.getInstance().saveConfigurationBeanParent(config,
	                InterfaceConfigurationBeanParent.FILENAME_PARENT, lock);
	
	    }
	    catch (Exception e)
	    {
	        logger.error(e.getMessage(), e);
	
	    }
	
	}

	protected void setSleepTime(int sleepTime)
	{
	    this.sleepTime = sleepTime;
	}

	private void stopProcedure_checkDependentThreads()
	{
	
	    if (config.isRunFileio() && config.isRunNetio())
	    {
	
	        if (netioThreads.isEmpty())
	        {
	            fileio.setUpstreamThreadDone(true);
	        }
	        else if (!netioThreads.get(0).isContinueRun())
	        {
	            fileio.setUpstreamThreadDone(true);
	        }
	    }
	    if (config.isRunFileio() && config.isRunTextAnalysis())
	    {
	        if (!fileio.isContinueRun() && !(taThreadsList.isEmpty()))
	        {
	            taThreadsList.stream().forEach(taThread -> taThread.setUpstreamThreadDone(true));
	        }
	    }
	
	}

	protected void stopProcedure_elegantStop(StringBuilder sb)
	{
	    if (config.isElegantStop())
	    {
	        ProcessStatus.getStatusMap().put("EXITING", "............Seeking an elegant out..........");
	        sb.append("\n\n............Seeking an elegant out..........\n\n.");
	        logger.info(sb.toString());
	
	        if (null != netioThreads && netioThreads.size() > 0)
	        {
	            netioThreads.get(0).abort();
	        }
	        
	        if (null != fileio)
	        {
	            fileio.abort();
	        }
	        
	        if (!taThreadsList.isEmpty())
	        {
	            taThreadsList.stream().forEach(taThread -> taThread.abort());
	        }
	        
	        sb.append("\n\n............Stop called on all active threads....be patient..........\n\n.");
	        ProcessStatus.getStatusMap().put("STOPPIMG", "............Abort Called..........");
	    }
	}

	protected boolean stopProcedure_isContinueRun()
	{
	
	    stopProcedure_checkDependentThreads();
	
	    if (config.isRunNetio() && !netioThreads.isEmpty())
	    {
	        if (netioThreads.get(0).isContinueRun())
	        {
	            return true;
	        }
	        else
	        {
	            netioThreads.clear();
	        }
	
	    }
	
	    if (config.isRunFileio() && fileio.isContinueRun())
	    {
	        return true;
	
	    }
	    if (config.isRunTextAnalysis() && !(taThreadsList.isEmpty()))
	    {
	        for (FileAnalyzerThreadManagement taThread : taThreadsList)
	        {
	            if (taThread.isContinueRun())
	            {
	                return true;
	            }
	        }
	
	    }
	
	    return false;
	}

}