package ikoda.manager;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import ikoda.netio.config.ConfigurationBeanFactory;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.FileAnalyzerFactory;
import ikoda.nlp.analysis.FileAnalyzerThread;
import ikoda.nlp.analysis.JobDescriptionAnalyzer;


import ikoda.nlp.structure.IdentifiedToken;
import ikoda.persistenceforanalysis.application.PersistenceForAnalysisThread;
import ikoda.persistenceforanalysis.singletons.RawDataUnit;
import ikoda.persistenceforanalysis.singletons.TextQueue;
import ikoda.utils.IDGenerator;
import ikoda.utils.IKodaUtilsException;
import ikoda.utils.LibSvmProcessor;
import ikoda.utils.MultiplePropertiesSingleton;
import ikoda.utils.ProcessStatus;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;
import ikoda.utils.TicToc;

public class ManagerAnalysisThread2 extends Thread
{

	private static final String TXT = ".txt";
	private static final String ALL_DATA = "ALL_DATA";
	private static final String BY_JOB = "BY-JOB";
	private static final String BY_SENTENCE = "BY-SENTENCE";
	private static final String A_ = "adj_";
	private static final String V_ = "v_";
	private static final String UNDERSCORE = "_";
	private static final String MAXPERBATCH="maxRowPerBatch";

	private static final String M1_FILEPROCESS = "doFileProcess";
	private static final String M2_PROCESSSTRING = "processString";
	private static final String M3_PARSESKILLS = "parseSkills";
	private static final String M4_PROCESSSPREADSHEET = "processSpreadsheet";


	private FileAnalyzerThread taThread;

	protected StanfordCoreNLP stanfordPipeline;

	private final ReentrantLock lock = new ReentrantLock();

	private ConfigurationBeanParent config;
	private ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis tConfig;

	private TicToc tt = new TicToc();

	private String[] columnsToIgnore;
	private List<String> columnsOutsideLibsvm = new ArrayList<>();

	private String targetColumnName;

	private int sleepTime = 100;


	private int maxNumberOfDocuments = 0;
	private int maxNumberOfCsvRowsPerFile = 0;
	private int rowCount = 0;

	long restartTATime = 0;

	private boolean ready = false;

	boolean continueRun = true;

	boolean restartTA = false;
	boolean restartFileio = false;
	boolean restartPersistence = false;

	boolean createNewNetio = false;
	
	
	
	
	

	@Autowired
	private PersistenceForAnalysisThread persistenceThread;

	protected List<Path> listFiles(Path path)
	{
		ManagerLog.getLogger().info("started ......");
		List<Path> files = new ArrayList<>();

		try
		{
			if (lock.tryLock(8, TimeUnit.SECONDS))
			{
				try
				{

					try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
					{

						for (Path entry : stream)
						{
							if (Files.isDirectory(entry))
							{

								continue;
							}
							files.add(entry);

						}
						
						
					}
					catch (Exception e)
					{
						ManagerLog.getLogger().error(e.getMessage(), e);
					}
				}
				catch (Exception e)
				{

					ManagerLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
				ManagerLog.getLogger().info("done");
				return files;
			}
			else
			{
				ManagerLog.getLogger().warn("Failed to acquire lock");
				return files;
			}
		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
			return files;
		}
	}

	private void addToSpreadsheet(String file, String uid, String columnHead, String rowValue, StringBuilder sbErrors)
	{
		try
		{

			Spreadsheet.getInstance().getLibSvmProcessor(file).addCell(uid, StaticSundryUtils.cleanColumnName(columnHead), rowValue.toLowerCase());

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void checkRestarts()
	{
		/////////////////// restart after shut down of thread
		try
		{
			if (restartPersistence)
			{
				ManagerLog.getLogger().info("\n\nRESTARTING PERSISTENCE THREAD\n\n");
				ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
				persistenceThread = new PersistenceForAnalysisThread();

				persistenceThread = (PersistenceForAnalysisThread) context.getBean("persistenceForAnalysisThread");
				persistenceThread.runPersistenceProcesses(config, config.isRunTextAnalysis(), lock);
				persistenceThread.start();
				restartPersistence = false;
				ConfigurableApplicationContext ctx = ((ConfigurableApplicationContext) context);
				ctx.close();
			}
			if (restartTA)
			{
				if ((System.currentTimeMillis() - restartTATime) > 300000)
				{
					ManagerLog.getLogger().info("\n\nRESTARTING TEXT ANALYSIS THREAD\n\n\n\n\n\n");
					taThread = new FileAnalyzerThread();
					taThread.runFileAnalyzer(config.isRunFileio(), config, lock);
					taThread.start();
					restartTA = false;
				}
			}
		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e);
		}
	}

	private void checkRowCounts(String bywhat)
	{
		try
		{
			int rowCountJ = Spreadsheet.getInstance().getLibSvmProcessor(bywhat).rowCount();

					

				

				ProcessStatus.put("maxNumberOfCsvRowsPerFile ", maxNumberOfCsvRowsPerFile);
				

				if (rowCountJ > maxNumberOfCsvRowsPerFile)
				{		
					ManagerLog.getLogger().info("STREAMING: "+bywhat + " row count: " + rowCountJ);
					streamToSpark(bywhat);	
				}
			

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
		}
	}
	
	private void streamToSpark(String name) throws IKodaUtilsException
	{
		try
		{
								
			String response=Spreadsheet.getInstance().getLibSvmProcessor(name).sparkStreamRunLibsvm(config.getMlServerUrl(), config.getMlServerPort(),columnsToIgnore);					
			ManagerLog.getLogger().info("sparkStreamRunLibsvm: "+response);
			if(response.equals("SUCCESS"))
			{
				response=Spreadsheet.getInstance().getLibSvmProcessor(name).sparkStreamRun("jobdescriptionssupplement",config.getMlServerUrl(), config.getMlServerPort(), columnsOutsideLibsvm);
				ManagerLog.getLogger().info("sparkStreamRun: "+response);
				if(response.equals("SUCCESS"))
				{
					Spreadsheet.getInstance().resetSpreadsheetLibsvm(name, columnsToIgnore);	
				}
			}		
		}
		catch(Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(),e);
		}
	}

	private boolean checkStopConditions()
	{
		StringBuilder sb = new StringBuilder();

		if (rowCount > maxNumberOfDocuments)
		{
			ManagerLog.getLogger().info("Max Row Count Reached: " + rowCount);
			sb.append("Max Row Count Reached: " + rowCount);

			sb.append("\nPersistence Running: " + persistenceThread.isContinueRun());
			if (config.isRunPersistence())
			{
				ManagerLog.getLogger().info("Human requested stop. Implementing on persistence");
				persistenceThread.abort();
			}
			finalizeFiles(BY_JOB);
			finalizeFiles(BY_SENTENCE);
			return false;
		}

		if (config.isRunPersistence())
		{
			sb.append("\nPersistence Running: " + persistenceThread.isContinueRun());
			if (!persistenceThread.isContinueRun())
			{
				finalizeFiles(BY_JOB);
				finalizeFiles(BY_SENTENCE);
				return false;
			}
			if (config.isElegantStop())
			{
				ManagerLog.getLogger().info("Human requested stop. Implementing on persistence");
				persistenceThread.abort();
			}
		}
		if (config.isElegantStop())
		{
			sb.append("\n\n............Seeking an elegant out..........\n\n.");
			ManagerLog.getLogger().info(sb.toString());
			finalizeFiles(BY_JOB);
			finalizeFiles(BY_SENTENCE);
			return false;
		}
		return true;
	}

	private void checkTimeOuts()
	{
		StringBuilder sb = new StringBuilder();

		if (null != persistenceThread && config.isRunPersistence())
		{
			if (!persistenceThread.isElegantlyCompleted())
			{

				sb.append("\npersistenceThread currentFile currentFileProcessTime: "
						+ persistenceThread.getFileProcessTime());
				if (persistenceThread.getFileProcessTime() > 900000)
				{
					ManagerLog.getLogger().info("\n\nTimeout, Aborting persistenceThread.  \n\n\n\n\n\n");
					persistenceThread.abort();
					persistenceThread.interrupt();
					restartPersistence = true;
				}
			}
		}
	}

	private synchronized boolean doFileProcess()
	{
		try
		{

			tt.tic(M1_FILEPROCESS, 15000);

			//ManagerLog.getLogger().debug("rollingRowCount: " + rollingRowCurrentCount);

			if (config.isElegantStop())
			{
				ManagerLog.getLogger().info("ELEGANT STOP REQUESTED");
			}

			if (!isContinueRun())
			{

				ManagerLog.getLogger().info("All threads complete");
				return false;
			}

			if (TextQueue.getInstance().size() > 100)
			{
				persistenceThread.setPause(true);
			}
			else
			{
				persistenceThread.setPause(false);
			}
			StringBuilder sb = new StringBuilder();
			if (TextQueue.getInstance().size() > 0)
			{
				sb.append("\n\n+++++++++++++++++++++\n");
				RawDataUnit rdu = TextQueue.getInstance().remove(0);

				List<String> skills = rdu.getSkills();

				tt.tic(M3_PARSESKILLS, 15000);
				parseSkillsString(skills, rdu);
				tt.stopTimer(M3_PARSESKILLS);
		
				rowCount++;
			}
			checkRowCounts(BY_JOB);
			checkRowCounts(BY_SENTENCE);

			checkRestarts();
			checkTimeOuts();

			tt.stopTimer(M1_FILEPROCESS);
			logThreadStatus();
			return checkStopConditions();

		}
		catch (Exception e)
		{

			ManagerLog.getLogger().error(e.getMessage(), e);
			return true;
		}
	}

	private void finalizeFiles(String fileName)
	{
		try
		{
			ManagerLog.getLogger().info("\n------------FINALIZING FILES----------------");

			ManagerLog.getLogger().info("\n\nFINALIZING :  " + fileName + " at count " + rowCount + " CurrentCount of "
					+ Spreadsheet.getInstance().getLibSvmProcessor(fileName).rowCount() + "\n\n\n");
			
			streamToSpark(fileName);
		
			

			List<Path> files = listFiles(
					Paths.get(Spreadsheet.getInstance().getLibSvmProcessor(fileName).getPathToDirectory()));

			int count = 0;
			for (Path p : files)
			{
				if (p.getFileName().toString().contains(fileName))
				{
					if (p.getFileName().toString().toLowerCase().contains(".libsvm"))
					{
						Spreadsheet.getInstance().initLibsvm2(fileName + count, targetColumnName,
								Spreadsheet.getInstance().getLibSvmProcessor(fileName).getPathToDirectory());
						LibSvmProcessor toMerge = Spreadsheet.getInstance().getLibSvmProcessor(fileName + count);
						toMerge.loadLibsvmPJ(p.getFileName().toString());
						Spreadsheet.getInstance().getLibSvmProcessor(fileName).mergeIntoLibsvm(toMerge);
					}
				}
			}

			
			Spreadsheet.getInstance().getLibSvmProcessor(fileName).printLibSvmFinal(columnsToIgnore);
			
			Spreadsheet.getInstance().getLibSvmProcessor(fileName).clearAll();
			
			for (Path p : files)
			{
				if(p.toString().contains("FINAL"))
				{
					continue;
				}
				StaticSundryUtils.archiveFile(p);
			}

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
		}
	}

	private ConfigurationBeanParent getConfig()
	{
		try
		{
			return ConfigurationBeanFactory.getInstance().getConfigurationBean(
					ConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION,
					ConfigurationBeanParent.FILENAME_PARENT, lock);

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
			ManagerLog.getLogger().warn("Failed to update configuration");
			return config;

		}
	}

	protected int getSleepTime()
	{
		return sleepTime;
	}

	private boolean isContinueRun()
	{

		if (config.isRunTextAnalysis())
		{
			if (!taThread.isContinueRun())
			{
				persistenceThread.setStop(true);
			}
		}

		if (config.isRunPersistence() && config.isRunTextAnalysis())
		{
			if (persistenceThread.isContinueRun() || taThread.isContinueRun())
			{
				return true;
			}
		}

		else if (config.isRunTextAnalysis())
		{
			if (taThread.isContinueRun())
			{
				return true;
			}
		}
		else if (config.isRunPersistence())
		{
			if (persistenceThread.isContinueRun())
			{
				return true;
			}
		}
		return false;
	}

	private void logThreadStatus()
	{
		try
		{
			if (Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).rowCount() % 100 == 0)
			{

				double mb = 1024 * 1024;

				// Getting the runtime reference from system
				Runtime runtime = Runtime.getRuntime();

				ProcessStatus.put("Text Queue Size", TextQueue.getInstance().size());

				ProcessStatus.put("**Spreadsheet Row Count Total", rowCount);

				

				ProcessStatus.put("Spreadsheet Saved at each ", tConfig.getConfigProperties().getAsInt(MAXPERBATCH));

				// Print used memory
				ProcessStatus.put("Used Memory", (runtime.totalMemory() - runtime.freeMemory()) / mb);

				// Print free memory
				ProcessStatus.put("Free Memory", runtime.freeMemory() / mb);

				// Print total available memory
				ProcessStatus.put("Total Memory", runtime.totalMemory() / mb);

				// Print Maximum available memory
				ProcessStatus.put("Max Memory", runtime.maxMemory() / mb);

				ManagerLog.getLogger().info(ProcessStatus.print());

				ManagerLog.getLogger().info(tt.toc(M1_FILEPROCESS));

				if (tt.isOverTime(M1_FILEPROCESS))
				{
					ManagerLog.getLogger().warn(tt.getOverTimeLog());
				}

				tt.clear();

			}

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
		}
	}

	public void parseSkillsString(List<String> skills, RawDataUnit rdu)
	{
		try
		{
			if (config.isRunTextAnalysis())
			{
				StringBuilder sb = new StringBuilder();
				for (String s : skills)
				{
					sb.append(s);
					sb.append(". ");
				}

				taThread.resetFileProcessStartTime();
				JobDescriptionAnalyzer jda = (JobDescriptionAnalyzer) FileAnalyzerFactory.getProcessorForDataSource(
						StringConstantsInterface.DATASOURCE_JOB_DESCRIPTION, stanfordPipeline, config);
				jda.setCollectPos(true);
				jda.setLemmatizeText(true);

				tt.tic(M2_PROCESSSTRING, 15000);
				jda.processString(sb.toString(), 15000);
				tt.stopTimer(M2_PROCESSSTRING);

				processMajor(rdu);

				processSpreadsheet(rdu, jda.getCountedTokens(), BY_JOB);

				Iterator<Integer> itr = jda.getCountedTokensBySentence().keySet().iterator();
				
				while (itr.hasNext())
				{
					Integer pos = itr.next();

					List<IdentifiedToken> l = jda.getCountedTokensBySentence().get(pos);
					// ManagerLog.getLogger().debug("Sentence pos "+pos);
					rdu.setSection(pos);
					processSpreadsheet(rdu, l, BY_SENTENCE);
				}
			}
			else
			{
				ManagerLog.getLogger().warn("Text Analysis not configured to run.");

			}
		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);

		}
	}

	private void processMajor(RawDataUnit rdu)
	{
		try
		{
			// if no qualifications, best guess from job title
			String jobTitleO = rdu.getJobTitle();
			if (null == jobTitleO)
			{
				ManagerLog.getLogger().warn("Null jobtitle in database");
				return;
			}

			String jobTitleP = jobTitleO.replaceAll(" ", UNDERSCORE).toUpperCase();
			if (rdu.getAreasOfStudy().isEmpty())
			{

				String majorP = MultiplePropertiesSingleton.getInstance().getProperties("JobToMajor.properties")
						.getProperty(jobTitleP);
				if (null != majorP)
				{
					String majorO = majorP.replaceAll(UNDERSCORE, " ");
					rdu.getAreasOfStudy().add(majorO);
					rdu.setMajorFinal(majorO);

				}
			}
			else
			{
				String majorO = rdu.getAreasOfStudy().get(0);

				String majorP = majorO.replaceAll(" ", UNDERSCORE).toUpperCase();

				String aggregatedMajorP = MultiplePropertiesSingleton.getInstance()
						.getProperties("aggregatedMajors.properties").getProperty(majorP);

				if (null != aggregatedMajorP && !aggregatedMajorP.isEmpty())
				{
					String aggregatedMajorO = aggregatedMajorP.replaceAll(UNDERSCORE, " ");
					// if we get a vague answer, we make a best guess from job title
					if (aggregatedMajorP.equalsIgnoreCase("ANY_SUBJECT"))
					{
						String majorPSuggested = MultiplePropertiesSingleton.getInstance()
								.getProperties("JobToMajor.properties").getProperty(jobTitleP);
						if (null != majorPSuggested)
						{
							String majorOSuggested = majorPSuggested.replaceAll(UNDERSCORE, " ");
							rdu.getQualifications().add(majorOSuggested);
							rdu.setMajorFinal(majorOSuggested);
						}
						else
						{
							rdu.setMajorFinal(majorO.toUpperCase());
						}
					}
					else
					{
						rdu.setMajorFinal(aggregatedMajorO.toUpperCase());
					}
				}
				else
				{

					rdu.setMajorFinal("#" + majorO.toUpperCase());
				}
			}

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().info(e.getMessage(), e);
		}
	}
	
	
	private void processLemmatizedSentence(String bywhat, RawDataUnit rdu, Collection<IdentifiedToken> tokens, String uuid, StringBuilder sbErrors)
	{
		try
		{
		
			//ManagerLog.getLogger().debug(" processLemmatizedSentence ");
			for (IdentifiedToken itoken : tokens)
			{
				if (itoken.getType().equals(IdentifiedToken.TOKENTUPLE))
				{
					//ManagerLog.getLogger().debug("itoken is TOKENTUPLE " + itoken);
					

					String uid = uuid;

					for (IdentifiedToken child : itoken.getChildren())
					{
						//ManagerLog.getLogger().trace("adding " + child.getType()+", " + child.getValue()+" rduid "+uuid);
						addToSpreadsheet(bywhat, 
								uid, 
								child.getType().toLowerCase(), 
								child.getValue()
								.replace(",","-")
								.replace(".", " ")
								.replace(";", " ")
								.replace(":", " ")
								.replace("\r", " ")
								.replace("\n", " ")
								.replace("  ", " "),
								sbErrors
								);
					}
				}
			}
		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);

		}
	}

	private void processSpreadsheet(RawDataUnit rdu, Collection<IdentifiedToken> tokens, String bywhat)
	{
		try
		{
			tt.tic(M4_PROCESSSPREADSHEET, 5000);
			StringBuilder sbErrors = new StringBuilder();
			//ManagerLog.getLogger().debug("processSpreadsheet "+bywhat);
			
			String uuid =String.valueOf(IDGenerator.getInstance().nextID());

			ProcessStatus.incrementStatus(bywhat);

			addToSpreadsheet(bywhat, uuid, RawDataUnit.COLUMN_HEAD_ID.toLowerCase(), uuid,
					sbErrors);

			addToSpreadsheet(bywhat, uuid, RawDataUnit.COLUMN_HEAD_LOCATION.toLowerCase(), rdu.getLocation(),
					sbErrors);
			addToSpreadsheet(bywhat, uuid, RawDataUnit.COLUMN_HEAD_REGION.toLowerCase(), rdu.getRegion(),
					sbErrors);
			addToSpreadsheet(bywhat, uuid, RawDataUnit.COLUMN_HEAD_JOB_TITLE.toLowerCase(), rdu.getJobTitle(),
					sbErrors);
			addToSpreadsheet(bywhat, uuid, targetColumnName.toLowerCase(), rdu.getMajorFinal(), sbErrors);

			int majorCount = 0;
			for (String area : rdu.getAreasOfStudy())
			{
				addToSpreadsheet(bywhat, uuid, (RawDataUnit.COLUMN_HEAD_MAJOR + majorCount).toLowerCase(), area, sbErrors);
				if (majorCount <= 10)
				{
					majorCount++;
				}
			}
			
			if(bywhat.equals(BY_SENTENCE))
			{
				//ManagerLog.getLogger().debug("calling  processLemmatizedSentence "+bywhat);
				processLemmatizedSentence(bywhat, rdu, tokens, uuid, sbErrors);
			}
			
			for (IdentifiedToken itoken : tokens)
			{
				processToken(itoken, bywhat, rdu, uuid,sbErrors);
			}

			if (sbErrors.length() > 0)
			{
				ManagerLog.getLogger().debug(sbErrors);
			}

			tt.stopTimer(M4_PROCESSSPREADSHEET);
		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void processToken(IdentifiedToken itoken, String fileName, RawDataUnit rdu,  String uuid, StringBuilder sbErrors)
	{
		String valuePrefix = "";
		if (itoken.getType().equals(IdentifiedToken.VERB))
		{
			valuePrefix = V_;
		}
		else if (itoken.getType().equals(IdentifiedToken.ADJECTIVE))
		{
			valuePrefix = A_;
		}

		if (!itoken.getValue().matches(".*\\d+.*"))
		{

			addToSpreadsheet(fileName, uuid, (valuePrefix + itoken.getValue()).toLowerCase(),
					String.valueOf(itoken.getFrequencyCount()), sbErrors);

		}
	}

	@Override
	public void run()
	{
		try
		{
			ManagerLog.getLogger().info("Starting Job Analysis Thread");
			int timeoutNoDataReceived = 0;
			while (continueRun)
			{
				if (lock.isHeldByCurrentThread())
				{
					ManagerLog.getLogger().warn("Releasing lock before going to sleep. This is bad");
					lock.unlock();
				}

				Thread.sleep(sleepTime);

				ConfigurationBeanParent cfg = getConfig();

				if (null != cfg)
				{
					config = cfg;

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
					ManagerLog.getLogger().info("EXITING THREAD");

					continueRun = false;

				}
			}
		}
		catch (InterruptedException e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error("This is Bad. Thread won't start");
			ManagerLog.getLogger().error(e.getMessage(), e);
		}
		catch (Error err)
		{
			ManagerLog.getLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
			throw err;
		}
	}

	
	public void runAnalysisProcesses() throws JAException
	{
		try
		{
			ManagerLog.getLogger().info("Initializing Manager");
			MultiplePropertiesSingleton.getInstance().setLogName("ikoda.manager");
			config = getConfig();
			if (null == config)
			{
				ManagerLog.getLogger()
						.error("\n\nNULL CONFIGURATION\n\nMost likely malformed or misnamed xml file\n\n");
				continueRun = false;
				return;
			}
			tConfig = (ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis) config.getTaConfig();

			String[] temp = new String[tConfig.getColumnsToExcludeFromLibSvm().size()];
			columnsToIgnore = tConfig.getColumnsToExcludeFromLibSvm().toArray(temp);

			maxNumberOfDocuments = tConfig.getMaxNumberOfDocuments();
			maxNumberOfCsvRowsPerFile = tConfig.getConfigProperties().getAsInt(MAXPERBATCH);

			ManagerLog.getLogger().info("is elegant stop = " + config.isElegantStop());
			if (config.isElegantStop())
			{
				ManagerLog.getLogger().debug("changing.....");
				config.setElegantStop(false);
				saveConfig();
				config = getConfig();

			}

			ManagerLog.getLogger().debug(" is elegant stop = " + config.isElegantStop());
			Path csvPath = Paths.get(tConfig.getCsvPath().trim());

			targetColumnName = tConfig.getTargetColumnName();

			Spreadsheet.getInstance().initLibsvm2(BY_JOB, ManagerLog.getLogger(), targetColumnName, csvPath.toString());
			Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).setKeyspaceName(config.getKeyspaceName()+BY_JOB);
			Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).setKeyspaceUUID(config.getConfigProperties().getAsString(Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).getKeyspaceName()));
			Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).setProjectPrefix(tConfig.getProjectPrefix());
			Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).setLocalPorts(config.getMlLocalStreamPorts());
			Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).setLocalUrl(config.getLocalUrls());
			
			
			
			Spreadsheet.getInstance().initLibsvm2(BY_SENTENCE, ManagerLog.getLogger(), targetColumnName, csvPath.toString());
			Spreadsheet.getInstance().getLibSvmProcessor(BY_SENTENCE).setKeyspaceName(config.getKeyspaceName()+BY_SENTENCE);
			Spreadsheet.getInstance().getLibSvmProcessor(BY_SENTENCE).setKeyspaceUUID(config.getConfigProperties().getAsString(Spreadsheet.getInstance().getLibSvmProcessor(BY_SENTENCE).getKeyspaceName()));
			Spreadsheet.getInstance().getLibSvmProcessor(BY_SENTENCE).setProjectPrefix(tConfig.getProjectPrefix());
			Spreadsheet.getInstance().getLibSvmProcessor(BY_SENTENCE).setLocalPorts(config.getMlLocalStreamPorts());
			Spreadsheet.getInstance().getLibSvmProcessor(BY_SENTENCE).setLocalUrl(config.getLocalUrls());
			
			Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).sparkStreamInit(config.getMlServerUrl(), config.getMlServerPort());

			

			columnsOutsideLibsvm.add(RawDataUnit.COLUMN_HEAD_AGGREGATEDMAJOR.toLowerCase());
			columnsOutsideLibsvm.add(RawDataUnit.COLUMN_HEAD_ID.toLowerCase());
			columnsOutsideLibsvm.add(RawDataUnit.COLUMN_HEAD_JOB_TITLE.toLowerCase());
			columnsOutsideLibsvm.add(RawDataUnit.COLUMN_HEAD_LOCATION.toLowerCase());
			columnsOutsideLibsvm.add(RawDataUnit.COLUMN_HEAD_REGION.toLowerCase());
			columnsOutsideLibsvm.add(IdentifiedToken.LEMMATIZEDSENTENCE.toLowerCase());
			columnsOutsideLibsvm.add(IdentifiedToken.RAWSENTENCE.toLowerCase());
			

			
			List<Path> files = listFiles(
					Paths.get(Spreadsheet.getInstance().getLibSvmProcessor(BY_JOB).getPathToDirectory()));
			
			for (Path p : files)
			{
				StaticSundryUtils.archiveFile(p);
			}


			if (config.isRunTextAnalysis())
			{
				taThread = new FileAnalyzerThread();
				taThread.initializeFileAnalyzer(config, lock);
				stanfordPipeline = taThread.getPipeline();
				ManagerLog.getLogger().info("TA started");
			}

			if (config.isRunPersistence())
			{
				persistenceThread.runPersistenceProcesses(config, config.isRunTextAnalysis(), lock);
				persistenceThread.start();
				ManagerLog.getLogger().info("Persistence started");
			}

			ready = true;
		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);
			throw new JAException(e);
		}
	}

	private void saveConfig()
	{
		try
		{
			ManagerLog.getLogger().debug("saving config: " + config.isElegantStop());
			ConfigurationBeanFactory.getInstance().saveConfigurationBeanParent(config,
					ConfigurationBeanParent.FILENAME_PARENT, lock);
			ManagerLog.getLogger().debug("saved config: " + config.isElegantStop());

		}
		catch (Exception e)
		{
			ManagerLog.getLogger().error(e.getMessage(), e);

		}
	}

	protected void setSleepTime(int sleepTime)
	{
		this.sleepTime = sleepTime;
	}

}
