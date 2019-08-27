package ikoda.jobanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ikoda.fileio.FileIoThread;
import ikoda.netio.NetIoThread;
import ikoda.netio.config.ConfigurationBeanFactory;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InterfaceConfigurationBeanParent;
import ikoda.nlp.analysis.FileAnalyzerThread;
import ikoda.nlp.analysis.FileAnalyzerThreadManagement;
import ikoda.persistence.application.PersistenceChoresThread;
import ikoda.persistence.application.PersistenceReportingThread;
import ikoda.persistence.application.PersistenceThread;
import ikoda.utils.ProcessStatus;

public class JobAnalysisThread extends Thread
{
    private final static String TXT = ".txt";
    private int sleepTime = 30000;
    private ConfigurationBeanParent config;
    private boolean ready = false;

    int source = 0;

    private final ReentrantLock lock = new ReentrantLock();
    boolean restartTA = false;
    boolean restartFileio = false;
    boolean restartPersistence = false;

    boolean createNewNetio = false;

    private FileIoThread fileio = new FileIoThread();
    private List<FileAnalyzerThread> taThreadsList = new ArrayList<>();
    private List<NetIoThread> netioThreads = new ArrayList<>();
    private long cycleCount =0;
    private List<String> logIgnoreList = new ArrayList<>(Arrays.asList("Netio 02","Low Call Count","Excessive Accumulated Calls")); 

    @Autowired
    private PersistenceThread persistenceThread;

    @Autowired
    private PersistenceChoresThread persistenceChoresThread;

    @Autowired
    private PersistenceReportingThread persistenceReportingThread;

    public JobAnalysisThread()
    {

    }

    private void checkRestarts()
    {
        /////////////////// restart after shut down of thread
        try
        {
            if (restartFileio)
            {
                ProcessStatus.incrementStatus("Fileio Thread Fails");

                JALog.getLogger().info("\n\nRESTARTING FILEIO THREAD\n\n");
                fileio = new FileIoThread();
                fileio.runFileIo(config.isRunNetio(), config, lock);
                fileio.start();
                restartFileio = false;
            }
            if (restartPersistence)
            {
                ProcessStatus.incrementStatus("Persistence Thread Fails");

                JALog.getLogger().info("\n\nRESTARTING PERSISTENCE THREAD\n\n");
                ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
                persistenceThread = new PersistenceThread();

                persistenceThread = (PersistenceThread) context.getBean("persistenceThread");
                persistenceThread.runPersistenceProcesses(config, config.isRunTextAnalysis(), lock);
                persistenceThread.start();
                restartPersistence = false;
            }
            if (restartTA)
            {
                for (FileAnalyzerThread taThread : taThreadsList)
                {
                    ProcessStatus.incrementStatus("Text Analysis Thread Fails");
                    if ((System.currentTimeMillis() - taThread.getFileProcessTime()) > 300000)
                    {
                        JALog.getLogger().info("\n\nRESTARTING TEXT ANALYSIS THREAD\n\n\n\n\n\n");
                        taThread = new FileAnalyzerThread();
                        taThread.runFileAnalyzer(config.isRunFileio(), config, lock);
                        taThread.start();

                        restartTA = false;
                        taThreadsList.add(taThread);
                    }
                }
            }
        }
        catch (Exception e)
        {
            JALog.getLogger().error(e);
        }
    }

    private void checkTimeOuts()
    {
        try
        {
        Iterator<NetIoThread> itr = netioThreads.iterator();

        NetIoThread newNetio = null;
        while (itr.hasNext())
        {
            NetIoThread netio = itr.next();

            if (netio.getUrlCallDurationInMillis() > 240000 && true == netio.isContinueRun())
            {

                newNetio = new NetIoThread();
                newNetio.reviveAfterThreadHang(netio);
                createNewNetio = true;
                netio.setStopNow(true);

                itr.remove();
            }
        }
        if (createNewNetio)
        {
            JALog.getLogger().info("\n\n\n\n\nRESTARTING netio due to thread hang\n\n\n\n\n");
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
                    JALog.getLogger().info("\n\n\n\n\naAborting fileio due to thread hang\n\n\n\n\n");
                    fileio.abort();
                    fileio.interrupt();

                    restartFileio = true;

                }
            }
        }

        if (!(taThreadsList.isEmpty()) && config.isRunTextAnalysis())
        {

            for (FileAnalyzerThreadManagement taThread : taThreadsList)
            {

                if (!taThread.isUpstreamThreadDone())
                {
                    ProcessStatus.getStatusMap().put("TA currentFileProcessTime",
                            String.valueOf(taThread.getFileProcessTime()));

                    if (taThread.getFileProcessTime() > 1000000 && false == restartTA)
                    {
                        JALog.getLogger().info("\n\nTimeout, Aborting. Restart in 5 minutes. \n\n\n\n\n\n");
                        taThread.restart();
                        taThread.interrupt();
                        taThread.setDesignatedForAbort(true);
                        restartTA = true;

                    }
                }
            }

        }
        if (null != persistenceThread && config.isRunPersistence())
        {
            ProcessStatus.getStatusMap().put("Persistence Count persisted entries",
                    String.valueOf(persistenceThread.getEntryCount()));
            ProcessStatus.getStatusMap().put("Persistence CurrentFile currentFileProcessTime",
                    String.valueOf(persistenceThread.getFileProcessTime()));

            if (!persistenceThread.isUpstreamThreadDone())
            {
                if (persistenceThread.getFileProcessTime() > 900000)
                {
                    JALog.getLogger().info("\n\nTimeout, Aborting persistenceThread.  \n\n\n\n\n\n");
                    persistenceThread.abort();
                    persistenceThread.interrupt();
                    restartPersistence = true;

                }
            }
        }
        }
        catch(Exception e)
        {
            stopProcedure_elegantStop(new StringBuilder(e.getMessage()));
            JALog.getLogger().error(e.getMessage(),e);
            
        }
    }
    
    private void logProcess()
    {
        if(cycleCount % 15 == 0)
        {
            JALog.getLogger().info(ProcessStatus.print());
        }
        else
        {
            JALog.getLogger().info(ProcessStatus.print(logIgnoreList));
        }
    }

    private synchronized boolean doFileProcess()
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            cycleCount++;

            if (!stopProcedure_isContinueRun())
            {
                JALog.getLogger().info("All threads complete");
                return false;
            }
            try
            {
                int mb = 1024 * 1024;

                // Getting the runtime reference from system
                Runtime runtime = Runtime.getRuntime();
                ProcessStatus.getStatusMap().put("Memory Used",
                        String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / mb));
                ProcessStatus.getStatusMap().put("Memory Free", String.valueOf((runtime.freeMemory() / mb)));
                ProcessStatus.getStatusMap().put("Memory Total", String.valueOf((runtime.totalMemory() / mb)));
                ProcessStatus.getStatusMap().put("Memory Max", String.valueOf((runtime.maxMemory() / mb)));

            }
            catch (Exception e)
            {
                JALog.getLogger().error(e.getMessage(), e);
            }

            stopProcedure_elegantStop(sb);
            checkRestarts();
            checkTimeOuts();
            JALog.getLogger().info(sb.toString());
           logProcess();
            return true;
        }
        catch (Exception e)
        {
            JALog.getLogger().error(e.getMessage(), e);
            return true;
        }
    }

    private ConfigurationBeanParent getConfig()
    {

        try
        {
        	JALog.getLogger().debug("loading config");
            ConfigurationBeanParent configt = ConfigurationBeanFactory.getInstance().getConfigurationBean(
                    InterfaceConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION,
                    InterfaceConfigurationBeanParent.FILENAME_PARENT, lock);
            JALog.getLogger().debug(configt);
            return configt;
        }
        catch (Exception e)
        {
            stopProcedure_elegantStop(new StringBuilder());
            JALog.getLogger().error(e.getMessage(), e);
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
            boolean continueRun = true;

            JALog.getLogger().info("Starting Job Analysis Thread");
            int timeoutNoDataReceived = 0;
            while (continueRun)
            {

                if (lock.isHeldByCurrentThread())
                {
                    JALog.getLogger().warn("Releasing lock before going to sleep. This is bad");
                    lock.unlock();
                }
                JALog.getLogger().info("Going to sleep for " + sleepTime + " millis.");
                Thread.sleep(sleepTime);

                ConfigurationBeanParent cfg = getConfig();

                if (null != cfg)
                {
                    config = cfg;
                    JALog.getLogger().debug("updated cfg isElegantStop=" + config.isElegantStop());
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
                    JALog.getLogger().info("EXITING THREAD");
                    if (null != persistenceChoresThread)
                    {
                        persistenceChoresThread.setStop(true);
                    }
                    continueRun = false;
                }

            }
        }
        catch (InterruptedException e)
        {
            JALog.getLogger().error(e.getMessage(), e);

        }
        catch (Exception e)
        {
            JALog.getLogger().error("This is Bad. Thread won't start");
            JALog.getLogger().error(e.getMessage(), e);
        }
        catch (Error err)
        {
            JALog.getLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
            throw err;
        }

    }

    public void runTextProcesses() throws JAException
    {

        try
        {

            config = getConfig();
            ConfigurationBeanForTextAnalysis_Generic tConfig = (ConfigurationBeanForTextAnalysis_Generic) config
                    .getTaConfig();

            System.out.println("runTextProcesses");
            JALog.getLogger().info(config);

            if (config.isElegantStop())
            {

                config.setElegantStop(false);
                saveConfig();

            }

            if (config.isRunPersistence())
            {
                persistenceThread.runPersistenceProcesses(config, config.isRunTextAnalysis(), lock);
                persistenceThread.start();

                JALog.getLogger().info("Persistence started");
            }
            if (config.isRunFileio())
            {
                fileio = new FileIoThread();
                fileio.runFileIo(config.isRunNetio(), config, lock);
                fileio.start();
                JALog.getLogger().info("File IO started");
            }
            if (config.isRunNetio())
            {
                NetIoThread netio = new NetIoThread();
                netioThreads.add(netio);

                netio.runNetIo(1, config, lock);
                netio.start();
                JALog.getLogger().info("Net IO started");
            }

            if (config.isRunPersistenceChores())
            {
                persistenceChoresThread.runPersistenceChores(config, false, lock);
                persistenceChoresThread.start();

                JALog.getLogger().info("Persistence Chores started");
            }
            if (config.isRunPersistenceReporting())
            {
                persistenceReportingThread.runPersistenceReporting(config, false);
                persistenceReportingThread.start();

                JALog.getLogger().info("Persistence Reporting started");
            }
            if (config.isRunTextAnalysis())
            {
                for (int i = 0; i < tConfig.getTaThreadCount(); i++)
                {
                    FileAnalyzerThread taThread = new FileAnalyzerThread(i);
                    taThread.runFileAnalyzer(config.isRunFileio(), config, lock);
                    taThread.start();
                    taThreadsList.add(taThread);
                }

                JALog.getLogger().info("TA started");

            }
            JALog.getLogger().info("Ready");
            System.out.println("READY");
            ready = true;
        }
        catch (Exception e)
        {
            config.setElegantStop(true);
            JALog.getLogger().error(e.getMessage(), e);
            stopProcedure_elegantStop(new StringBuilder());
            throw new JAException(e);
        }

    }

    private void saveConfig()
    {

        try
        {

            ConfigurationBeanFactory.getInstance().saveConfigurationBeanParent(config,
                    InterfaceConfigurationBeanParent.FILENAME_PARENT, lock);

        }
        catch (Exception e)
        {
        	
            JALog.getLogger().error(e.getMessage(), e);

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
            if (!netioThreads.isEmpty() && !netioThreads.get(0).isContinueRun())
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
        if (config.isRunTextAnalysis() && !(taThreadsList.isEmpty()))
        {
            for (FileAnalyzerThreadManagement taThread : taThreadsList)
            {
                if (!taThread.isContinueRun())
                {
                    persistenceThread.setUpstreamThreadDone(true);
                }
            }
        }
        if (config.isRunPersistence())
        {
            if (!persistenceThread.isContinueRun())
            {
                if (config.isRunPersistenceChores() && config.isRunPersistenceReporting())
                {
                    persistenceChoresThread.abort();
                    persistenceReportingThread.abort();
                }
                else if (config.isRunPersistenceChores())
                {
                    persistenceChoresThread.abort();
                }
                else if (config.isRunPersistenceReporting())
                {
                    persistenceReportingThread.abort();
                }
            }
        }

    }

    private void stopProcedure_elegantStop(StringBuilder sb)
    {

        if (config.isElegantStop())
        {
            ProcessStatus.getStatusMap().put("EXITING", "............Seeking an elegant out..........");
            sb.append("\n\n............Seeking an elegant out..........\n\n.");
            JALog.getLogger().info(sb.toString());

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
                for (FileAnalyzerThreadManagement taThread : taThreadsList)
                {
                    taThread.abort();
                }
            }
            if (null != persistenceChoresThread)
            {
                persistenceChoresThread.abort();
            }
            if (null != persistenceThread)
            {
                persistenceThread.abort();
            }
            if (null != persistenceReportingThread)
            {
                persistenceReportingThread.abort();
            }
            sb.append("\n\n............Stop called on all active threads....be patient..........\n\n.");
            ProcessStatus.getStatusMap().put("STOPPING", "............Abort Called..........");

        }
    }

    private boolean stopProcedure_isContinueRun()
    {

        try
        {
            stopProcedure_checkDependentThreads();

            boolean threadsRunning = false;

            if (config.isRunPersistence())
            {
                if (persistenceThread.isContinueRun())
                {
                    threadsRunning = true;
                }
            }

            if (config.isRunFileio())
            {
                if (fileio.isContinueRun())
                {
                    threadsRunning = true;
                }
            }
            if (config.isRunNetio())
            {
                if (netioThreads.get(0).isContinueRun())
                {
                    threadsRunning = true;
                }
            }
            if (config.isRunTextAnalysis())
            {
                for (FileAnalyzerThreadManagement taThread : taThreadsList)
                {
                    if (taThread.isContinueRun())
                    {
                        threadsRunning = true;
                    }
                }
            }
            if (config.isRunPersistenceChores())
            {
                JALog.getLogger().debug("config.isRunPersistenceChores(): " + config.isRunPersistenceChores());
                if (persistenceChoresThread.isContinueRun())
                {
                    threadsRunning = true;
                }
            }
            else if (config.isRunPersistenceReporting())
            {
                JALog.getLogger().debug("config.isRunPersistenceReporting(): " + config.isRunPersistenceReporting());
                if (persistenceReportingThread.isContinueRun())
                {
                    threadsRunning = true;
                }
            }
            return threadsRunning;
        }
        catch (Exception e)
        {
            JALog.getLogger().error(e.getMessage(), e);
            stopProcedure_elegantStop(new StringBuilder());
            return false;
        }
    }
}
