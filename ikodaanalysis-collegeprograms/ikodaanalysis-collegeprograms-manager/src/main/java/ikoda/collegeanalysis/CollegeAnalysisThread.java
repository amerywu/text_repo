package ikoda.collegeanalysis;

import ikoda.fileio.CollegeFileIoThread;

import ikoda.netio.CollegeNetioThread;
import ikoda.nlp.analysis.CollegeProgramAnalyzerThread2;
import ikoda.nlp.analysis.CollegeProgramElasticSearchDispatcher;
import ikoda.nlp.analysis.CollegeURLAnalyzerThread;
import ikoda.nlp.structure.EsJson;
import ikoda.utils.ElasticSearchManager;
import ikoda.utils.ProcessStatus;
import ikoda.utils.SSm;
import org.apache.logging.log4j.*;

public class CollegeAnalysisThread extends CollegeAnalysisCoordinator {

	public CollegeAnalysisThread() {

	}

	protected synchronized boolean doFileProcess() {
		try {
			StringBuilder sb = new StringBuilder();
			if (!stopProcedure_isContinueRun()) {
				logger.info("All threads complete");
				return false;
			}
			try {
				int mb = 1024 * 1024;
				Runtime runtime = Runtime.getRuntime();
				ProcessStatus.getStatusMap().put("Memory Used",
						String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / mb));
				ProcessStatus.getStatusMap().put("Memory Free", String.valueOf((runtime.freeMemory() / mb)));
				ProcessStatus.getStatusMap().put("Memory Total", String.valueOf((runtime.totalMemory() / mb)));
				ProcessStatus.getStatusMap().put("Memory Max", String.valueOf((runtime.maxMemory() / mb))); 
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			stopProcedure_elegantStop(sb);
			checkRestarts();
			checkTimeOuts();
			printLog();
			cycleCount++;
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return true;
		}
	}

	public void runTextProcesses() throws JAException {
		try {
			logger.info("Starting College Analysis Thread");
			config = getConfig();
			logger.info(config);

			if (null == config) {
				logger.error("\n\nNULL CONFIGURATION\n\nMost likely malformed or misnamed xml file\n\n");
				stopProcedure_elegantStop(new StringBuilder());
				return;
			}

			if (config.isSendToES()) {
				ElasticSearchManager.getInstance().init(config.getElasticSearchUser(),
						config.getElasticSearchPassword(), config.getElasticSearchUrl(),
						new Integer(config.getElasticSearchPort()).intValue());
				ElasticSearchManager.getInstance().createIndexIfNotExisting(EsJson.REDDIT_INDEX_NAME,
						EsJson.redditIndexJson());
			}

			if (config.isElegantStop()) {
				logger.info("resetting config");
				config.setElegantStop(false);
				saveConfig();
				config = getConfig();
				logger.info(config.toString());
			}

			if (config.isRunFileio()) {
				fileio = new CollegeFileIoThread();
				fileio.runFileIo(config.isRunNetio(), config, lock);
				fileio.start();
				logger.info("File IO started");
			}

			if (config.isRunTextAnalysis()) {

				CollegeProgramElasticSearchDispatcher taThread = new CollegeProgramElasticSearchDispatcher(0);
				taThread.runFileAnalyzer(config.isRunFileio(), config, lock);
				taThread.start();
				taThreadsList.add(taThread);

				logger.info("TA started");
			}
			if (config.isRunNetio()) {
				CollegeNetioThread netio = new CollegeNetioThread();
				netioThreads.add(netio);

				netio.runNetIo(1, config, lock);
				netio.start();
				logger.info("Net IO started");
			}


			ready = true;
		} catch (Exception e) {
			config.setElegantStop(true);
			logger.error(e.getMessage(), e);
			stopProcedure_elegantStop(new StringBuilder());
			throw new JAException(e);
		}

	}

}
