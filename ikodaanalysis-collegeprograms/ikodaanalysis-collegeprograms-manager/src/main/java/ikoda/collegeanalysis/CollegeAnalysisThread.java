package ikoda.collegeanalysis;

import ikoda.fileio.CollegeFileIoThread;

import ikoda.netio.CollegeNetioThread;
import ikoda.nlp.analysis.CollegeProgramAnalyzerThread2;
import ikoda.nlp.analysis.CollegeURLAnalyzerThread;
import ikoda.utils.ProcessStatus;
import ikoda.utils.SSm;
import org.apache.logging.log4j.*;

public class CollegeAnalysisThread extends CollegeAnalysisCoordinator
{

    public CollegeAnalysisThread()
    {

    }

    protected synchronized boolean doFileProcess()
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            if (!stopProcedure_isContinueRun())
            {
                logger.info("All threads complete");
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
                logger.error(e.getMessage(), e);
            }

            stopProcedure_elegantStop(sb);
            checkRestarts();
            checkTimeOuts();
            printLog();
            cycleCount++;
            return true;
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return true;
        }
    }
    
    public void runTextProcesses() throws JAException
    {
        try
        {
        	System.out.println("runTextProcesses 1");
            logger.info("Starting College Analysis Thread");
        	System.out.println("runTextProcesses 2");
            config = getConfig();
            System.out.println("runTextProcesses 3");
            logger.info(config);
            System.out.println("runTextProcesses "+config);
            if (null == config)
            {
                logger.error("\n\nNULL CONFIGURATION\n\nMost likely malformed or misnamed xml file\n\n");
                stopProcedure_elegantStop(new StringBuilder());
                return;
            }

            if (config.isElegantStop())
            {
                logger.info("resetting config");
                config.setElegantStop(false);
                saveConfig();
                config = getConfig();
                logger.info(config.toString());
            }

            if (config.isRunFileio())
            {
                fileio = new CollegeFileIoThread();
                fileio.runFileIo(config.isRunNetio(), config, lock);
                fileio.start();
                logger.info("File IO started");
            }


            if (config.isRunTextAnalysis())
            {
                for (int i = 0; i < config.getTaConfig().getTaThreadCount(); i++)
                {
                    CollegeProgramAnalyzerThread2 taThread = new CollegeProgramAnalyzerThread2(i);
                    taThread.runFileAnalyzer(config.isRunFileio(), config, lock);
                    taThread.start();
                    taThreadsList.add(taThread);
                }
                logger.info("TA started");
            }
            if (config.isRunNetio())
            {
                CollegeNetioThread netio = new CollegeNetioThread();
                netioThreads.add(netio);

                netio.runNetIo(1, config, lock);
                netio.start();
                logger.info("Net IO started");
            }
            if (config.isRunTraining())
            {
            	
            	System.out.println("runTextProcesses 4");
                trainingThread = new CollegeURLAnalyzerThread();
                trainingThread.runFileAnalyzer(config.isRunFileio(), config, lock);
                trainingThread.start();


                logger.info("TA started");
            }

            ready = true;
        }
        catch (Exception e)
        {
            config.setElegantStop(true);
            logger.error(e.getMessage(), e);
            stopProcedure_elegantStop(new StringBuilder());
            throw new JAException(e);
        }

    }

}
