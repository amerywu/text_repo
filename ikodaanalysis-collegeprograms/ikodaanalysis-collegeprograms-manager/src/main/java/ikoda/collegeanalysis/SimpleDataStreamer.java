package ikoda.collegeanalysis;

import ikoda.fileio.CollegeFileIoThread;

import ikoda.netio.CollegeNetioThread;
import ikoda.nlp.analysis.CollegeProgramAnalyzerThread2;
import ikoda.nlp.analysis.CollegeURLAnalyzerThread;
import ikoda.nlp.structure.CollegeRawDataUnit;
import ikoda.utils.LibSvmProcessor;
import ikoda.utils.ProcessStatus;
import ikoda.utils.SSm;
import ikoda.utils.Spreadsheet;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.*;

public class SimpleDataStreamer extends CollegeAnalysisCoordinator
{
	
	private static final String fname="fname";
	private static List<String> columnsOutsideLibsvm = new ArrayList<>();
	private boolean sendFlush=false;
    public SimpleDataStreamer()
    {

    }

    @Override
    protected void stopProcedure_elegantStop(StringBuilder sb)
	{
	    if (config.isElegantStop())
	    {
	    	continueRun=false;

	        ProcessStatus.getStatusMap().put("STOPPIMG", "............Abort Called..........");
	    }
	}
    
    @Override
    protected synchronized boolean doFileProcess()
    {
        try
        {
            try
            {
            	logger.info("Next Batch...");
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

            stopProcedure_elegantStop(new StringBuilder());
            
            
            
            LibSvmProcessor subset=Spreadsheet.getInstance().getLibSvmProcessor(fname).subsetLibsvmNoReplacement(15000);
            if(sendFlush)
            {

            	logger.info("Exiting");
            	return false;
            	
            }
            else if(null==subset)
            {
            	
            	logger.info("LibSvmProcessor returned null. All data transferred?");
            	sendFlush=true;
            	sleepTime=220000;
            	return true;
            }
            if(subset.validate())
            {
	            subset.sparkStreamRunLibsvm(config.getMlServerUrl(), config.getMlServerPort());
	            ProcessStatus.incrementStatus("Batch Streamed");
	            ProcessStatus.put("Remaining Rows", Spreadsheet.getInstance().getLibSvmProcessor(fname).rowCount());
	            printLog();
	            cycleCount++;
	            return true;
            }
            else
            {
            	logger.warn("VALIDATION FAILED");
            	return false;
            }

        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            ProcessStatus.incrementStatus("Batch Failed");
            return false;
        }
        
    }
    
    public void runStreamer() throws JAException
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
            sleepTime=180000;

            
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

            
            String dirPath=config.getConfigProperties().getAsString("libsvmdir");
            String fileName=config.getConfigProperties().getAsString("libsvmfile");
            ProcessStatus.getStatusMap().put("fileName",fileName);
            Spreadsheet.getInstance().initLibsvm2(fname,"aa_label", dirPath);
            logger.info("Loading: "+fileName);
            String keyspaceName=config.getConfigProperties().getAsString("simplestreamks");
            Spreadsheet.getInstance().getLibSvmProcessor(fname).loadLibsvm(fileName);
    		Spreadsheet.getInstance().getLibSvmProcessor(fname).setKeyspaceName(keyspaceName);
    		Spreadsheet.getInstance().getLibSvmProcessor(fname).setKeyspaceUUID(config.getConfigProperties().getAsString(keyspaceName));
    		Spreadsheet.getInstance().getLibSvmProcessor(fname).setLocalPorts(config.getMlLocalStreamPorts());
    		Spreadsheet.getInstance().getLibSvmProcessor(fname).setLocalUrl(config.getLocalUrls());
    		Spreadsheet.getInstance().getLibSvmProcessor(fname).sparkStreamInit(config.getMlServerUrl(),config.getMlServerPort());

    		
    		columnsOutsideLibsvm.add("aa_label");
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
