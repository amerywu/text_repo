package ikoda.nlp.analysis;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Reddit;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.structure.CollegeRawDataUnit;
import ikoda.nlp.structure.IKodaTextAnalysisException;
import ikoda.nlp.structure.IdentifiedToken;

import ikoda.utils.FileList;
import ikoda.utils.IDGenerator;
import ikoda.utils.IKodaUtilsException;
import ikoda.utils.ProcessStatus;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;
import ikoda.utils.TicToc;

public class CollegeProgramAnalyzerThread2 extends FileAnalyzerThread
{
	private static boolean finalized = false;
	private static boolean brunOnceSetUp = false;
	private static List<String> columnsOutsideLibsvm = new ArrayList<String>();
	protected static final String BY_DOCUMENT = "BY-DOCUMENT";
	protected static final String BY_SENTENCE = "BY-SENTENCE";
	private static final String MAXPERBATCH="maxRowPerBatch";
	protected static final String LEMMATIZEDSENTENCE = "LEMMATIZEDSENTENCE";
	protected static final String UNDERSCORE = "_";
	protected static final String ZERO = "0";
	private static final String ADJ_PREFIX = "adj_";
	private static final String VERB_PREFIX = "v_";

	private static final String[] ignoreLemmatizedPhrases =
	{ "Abusive offensive spam comment", "Avoid low-effort comment", "Advanced search", "Be register trademark",
			"Be respectful reply", "Blogspam image video", "Find submission", "Front page internet",
			"Include exclude result marked nsfw", "Include exclude self post", "Log sign seconds", "Moderation team",
			"Render pid", "Repeat flagrant offender be ban", "Rights reserve", "Run country code",
			"Search text self post contents", "Search text url", "See search faq detail",
			"Sensationalize title title include model applicable", "Submission be flair contain link",
			"Subscribe thousand community", "User here now", "Do not answer be not knowledgeable",
			"Anything look violate rule", "Ask help homework result", "Submit hour", "Submit day", "day ago",
			"month ago", "year ago", "hour ago" };
	private static final String[] ignorePhrases =
	{ "day ago", "days ago", "hour ago", "hours ago", "minutes ago", "months ago", "month ago", "log in"

	};

	private static ConfigurationBeanForTextAnalysis_Reddit configt;

	private static synchronized void checkRowCounts(String bywhat)
	{
		try
		{


				int rowCountJ = Spreadsheet.getInstance().getCsvSpreadSheet(bywhat).rowCount();
				TALog.getLogger().trace(bywhat +": "+ rowCountJ);
				TALog.getLogger().trace("maxNumberOfCsvRowsPerFile: "+ configt.getConfigProperties().getAsInt(MAXPERBATCH));
				ProcessStatus.put("TA: maxNumberOfCsvRowsPerFile ", configt.getConfigProperties().getAsInt(MAXPERBATCH));

				if (rowCountJ > configt.getConfigProperties().getAsInt(MAXPERBATCH))
				{
					ProcessStatus.incrementStatus("TA new Spreadsheet for " + bywhat);

					streamToSpark(bywhat);
				}

			
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
		}
	}

	private static synchronized boolean isFinalized()
	{
		return finalized;
	}

	private static synchronized void setFinalized(boolean b)
	{
		finalized = b;
	}

	private static synchronized void streamToSpark(String name) throws IKodaUtilsException
	{
		try
		{
			if(Spreadsheet.getInstance().getLibSvmProcessor(name).rowCount()> configt.getConfigProperties().getAsInt(MAXPERBATCH))
			{
				
			TALog.getLogger().info("STREAMING "+name);
			TALog.getLogger().info(Spreadsheet.getInstance().getLibSvmProcessor(name).info());
			String[] temp = new String[configt.getColumnsToExcludeFromLibSvm().size()];
			String[] columnsToIgnore = configt.getColumnsToExcludeFromLibSvm().toArray(temp);
			String response = Spreadsheet.getInstance().getLibSvmProcessor(name)
					.sparkStreamRunLibsvm(config.getMlServerUrl(), config.getMlServerPort(), columnsToIgnore);
		
			TALog.getLogger().info("sparkStreamRunLibsvm: " + response);
			
			if (response.equals("SUCCESS"))
			{
				response = Spreadsheet.getInstance().getLibSvmProcessor(name).sparkStreamRun(
						"datasupplement", config.getMlServerUrl(), config.getMlServerPort(),columnsOutsideLibsvm);
				TALog.getLogger().info("sparkStreamRun: " + response);
				if (response.equals("SUCCESS"))
				{

					Spreadsheet.getInstance().resetSpreadsheetLibsvm(name, columnsToIgnore);
				}
			}
			
			ProcessStatus.incrementStatus("TA: Streamed Batches for "+name+" "+response);
			
			}
			else
			{
				TALog.getLogger().info("sparkStreamRun: Already called by another thread" );
			}
		}
		catch (Exception e)
		{
			ProcessStatus.incrementStatus("TA Streaming FAILED: "+name);
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public CollegeProgramAnalyzerThread2()
	{
		super();
	}

	public CollegeProgramAnalyzerThread2(int threadId)
	{
		super(threadId);
	}

	private void addToSpreadsheet(String bywhat, String uid, String columnHead, String rowValue)
	{
		try
		{
			Spreadsheet.getInstance().getCsvSpreadSheet(bywhat).addCell(uid, StaticSundryUtils.cleanColumnName(columnHead), rowValue);
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
		}
	}
	
	private CollegeRawDataUnit createRDU(Collection<IdentifiedToken> tokens, String fileContents) throws IKodaUtilsException
	{
		try
		{
			CollegeRawDataUnit rdu = new CollegeRawDataUnit(IDGenerator.getInstance().nextID());
			rdu.getItokenList().addAll(tokens);
			TALog.getLogger().trace("count itokens: " + tokens.size());
			TALog.getLogger().trace("uid: " + rdu.getDocumentUid());
			TALog.getLogger().trace(" itokens: " + tokens);
			String category = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_PREDICTION_OPEN,
					StringConstantsInterface.SPIDERTAG_PREDICTION_CLOSE, fileContents);
			String website = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_HOST_OPEN,
					StringConstantsInterface.SPIDERTAG_HOST_CLOSE, fileContents);
			String sid = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_TBID_OPEN,
					StringConstantsInterface.SPIDERTAG_TBID_CLOSE, fileContents);
			String uri = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_URI_OPEN,
					StringConstantsInterface.SPIDERTAG_URI_CLOSE, fileContents).replaceAll("/", "|").trim();
			String url = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_FULLURL_OPEN,
					StringConstantsInterface.SPIDERTAG_FULLURLCLOSE, fileContents).replaceAll("/", "|");
			String urlRepository = StaticSundryUtils
					.extractHeader(StringConstantsInterface.SPIDERTAG_URLREPOSITORY_OPEN,
							StringConstantsInterface.SPIDERTAG_URLREPOSITORY_CLOSE, fileContents)
					.replaceAll("/", "|");

			//TALog.getLogger().trace(" uri: " + uri);
			//TALog.getLogger().trace(" category: " + category);
			

			category=processCategory(category,uri);

			rdu.setCategory(category);
			rdu.setWebsite(website);
			rdu.setSourceIdentifier(sid);
			rdu.setUri(uri);
			rdu.setUrl(url);
			rdu.setUrlRepository(urlRepository);

			return rdu;
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
			return new CollegeRawDataUnit(IDGenerator.getInstance().nextID());

		}
	}

	@Override
	protected synchronized boolean doFileProcess()
	{
		try
		{
			fileProcessStartTime = System.currentTimeMillis();

			TALog.getLogger().trace("\n\ndoFileProcess ");
			TALog.getLogger().trace("totalRowCount " + FileList.getInstance().getTotalCount());
			TALog.getLogger().trace("Files Processed: "+FileList.getInstance().getTotalCount());
			TALog.getLogger().trace("Max File Count: "+configt.getMaxNumberOfDocuments());

			if (!runStatusGo())
			{
				return false;
			}

			sleepTime = 3000;

			Path p = FileList.getInstance().getNextFile(PATHSNAME);
			if (null == p)
			{
				replenishFileList();
				return true;
			}

			currentFilePath = p.toString();
			boolean result = true;
			TALog.getLogger().info("PATH: " + currentFilePath);
			if (p.getFileName().toString().contains(TXT))
			{
				TALog.getLogger().debug("handling file " + p.getFileName());
				pipeline(p);
			}
			else
			{
				moveUndeterminedFile(p);
			}
			checkRowCounts(BY_DOCUMENT);
			if (configt.isCollectBySentence())
			{
				checkRowCounts(BY_SENTENCE);

			}

			TALog.getLogger().trace("\n\n\n\n\n\n\nTA: Number of files processed: " + FileList.getInstance().getTotalCount());
			return true;
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
			ProcessStatus.incrementStatus(super.getThreadId() + " TA errors in FileAnalyzerThread");
			return true;
		}
	}

	private void finalizeFiles(String bywhat)
	{
		try
		{
			if (!isFinalized())
			{
				setFinalized(true);
				Thread.sleep(190000);
				String[] temp = new String[configt.getColumnsToExcludeFromLibSvm().size()];
				String[] columnsToIgnore = configt.getColumnsToExcludeFromLibSvm().toArray(temp);
				TALog.getLogger().info("\n------------FINALIZING FILES----------------");

				TALog.getLogger()
						.info("\n\nFINALIZING CSV:  " + bywhat + " at count " + FileList.getInstance().getTotalCount()
								+ " CurrentCount of " + FileList.getInstance().getRollingCount() + "\n\n\n");

				Spreadsheet.getInstance().getLibSvmProcessor(bywhat).finalizeAndJoinBlocks(bywhat + ".csv");
				Spreadsheet.getInstance().getLibSvmProcessor(bywhat).printLibSvmFinal(bywhat, columnsToIgnore);
				streamToSpark(bywhat);

				Spreadsheet.getInstance().getLibSvmProcessor(bywhat).clearAll();

			}
			TALog.getLogger().info("\n\nALL FILES SUCCESSFULLY FINALIZED\n\n");
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
		}
	}

	private String getFileContents(Path p)
	{
		String fileContents = null;
		try
		{
			if (lock.tryLock(10, TimeUnit.SECONDS))
			{
				byte[] encoded = Files.readAllBytes(p);
				fileContents = new String(encoded);
			}
		}
		catch (Exception e)
		{
			TALog.getLogger().debug(e.getMessage(), e);
			return null;
		}
		finally
		{
			lock.unlock();
		}
		return fileContents;
	}

	/** Used when initializing as field variable */
	public void initializeFileAnalyzer(ConfigurationBeanParent inconfig, ReentrantLock inlock)
			throws IOException, Exception
	{
		super.initializeFileAnalyzer(inconfig, inlock);
	}

	@Override
	protected boolean pipeline(Path p)
	{

		try
		{
			TALog.getLogger().debug("\n\n=============pipeline===================\n"+p);
			TicToc tt = new TicToc();

			tt.tic("sendToAnalyzer");

			String fileContents = getFileContents(p);
			if (!validateContent(fileContents, p))
			{
				return false;
			}

			ProcessStatus.put("TA Rolling Row Count", FileList.getInstance().getRollingCount());
			ProcessStatus.put("TA Total Row Count", FileList.getInstance().getTotalCount());
			CollegeProgramAnalyzer cpa = (CollegeProgramAnalyzer) FileAnalyzerFactory
					.getProcessorForDataSource(StringConstantsInterface.DATASOURCE_COLLEGE_PROGRAM, pipeline, config);
			cpa.setCollectPos(configt.isCollectPartOfSpeech());
			cpa.setLemmatizeText(configt.isConvertToLemmas());
			cpa.setBanSentenceDuplicates(configt.isPreventSentenceDuplicates());
			cpa.setCollectBySentence(configt.isCollectBySentence());
			cpa.setIgnoreLemmatizedPhrasesContaining(ignoreLemmatizedPhrases);
			cpa.setIgnorePhrasesContaining(ignorePhrases);
			tt.tic("process text");
			boolean result = cpa.processString(fileContents);

			if (!result)
			{
				TALog.getLogger().debug("Failed in CollegeProgramAnalyzer ");
				moveFailedFile(p, cpa.getFinalStatus());
				return false;
			}

			Collection<IdentifiedToken> tokens = cpa.getCountedTokens();

			CollegeRawDataUnit rdu = createRDU(tokens, fileContents);

			processSpreadsheet(rdu, BY_DOCUMENT);

			if (configt.isCollectBySentence())
			{
				Map<Integer, List<IdentifiedToken>> tokensBySentenceMap = cpa.getCountedTokensBySentence();
				Iterator<Integer> itr = tokensBySentenceMap.keySet().iterator();
				while (itr.hasNext())
				{
					Integer key = itr.next();
					List<IdentifiedToken> itokensList = tokensBySentenceMap.get(key);
					CollegeRawDataUnit rduBySentence = createRDU(itokensList, fileContents);
					processSpreadsheet(rduBySentence, BY_SENTENCE);
					ProcessStatus.incrementStatus("TA: Total Sentence Count");

				}
			}

			TALog.getLogger().trace("::" + tt.toc("sendToAnalyzer"));
			moveSuccessfulFile(p, cpa.getFinalStatus());

			FileList.getInstance().incrementCount();


			TALog.getLogger().debug("\n\n----------------------------pipeline done---------------------------\n\n");
			return true;
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
			ProcessStatus.incrementStatus(super.getThreadId() + "TA Error in sendToAnalyzer");
		}
		return false;
	}

	private String processCategory(String category, String uri)
	{
		
		if(config.getAnalysisSubType().toLowerCase().contains("reddit"))
		{
			if(null !=uri)
			{
				
				if(StaticSundryUtils.countOccurrencesOfChar(uri, '|')>3)
				{
					return uri.substring(0, uri.lastIndexOf("|"));
				}
				else
				{
					return uri;
				}
			}
			else
			{
				return "unknown";
			}
		}
		
		else if(null==category)
		{
			return "unknown";
		}
		else if(category.endsWith("-"))
		{
			return category.substring(0,category.length()-1);
		}
		else
		{
			return category;
		}
	}

	private void processLemmatizedSentence(String bywhat, CollegeRawDataUnit rdu)
	{
		try
		{
		

			for (IdentifiedToken itoken : rdu.getItokenList())
			{
				if (itoken.getType().equals(IdentifiedToken.TOKENTUPLE))
				{
					TALog.getLogger().trace("itoken is TOKENTUPLE " + itoken);
					

					String uid = rdu.getDocumentUid();

					for (IdentifiedToken child : itoken.getChildren())
					{
						TALog.getLogger().trace("adding " + child.getType()+", " + child.getValue()+" rduid "+rdu.getDocumentUid());
						addToSpreadsheet(bywhat, uid, child.getType().toLowerCase(), child.getValue()
								.replace(",","-")
								.replace(".", " ")
								.replace(";", " ")
								.replace(":", " ")
								.replace("\r", " ")
								.replace("\n", " ")
								.replace("  ", " ")
								);
					}
				}
			}
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);

		}
	}

	private void processSpreadsheet(CollegeRawDataUnit rdu, String bywhat)
	{
		try
		{

			if (rdu.getItokenList().isEmpty())
			{
				TALog.getLogger().warn("No Tokens collected for " + rdu.getUrl());
				ProcessStatus.incrementStatus("TA Analysis Failed");
				return;
			}
			//TALog.getLogger().debug("PROCESSING from rdu id "+rdu.getDocumentUid());
			if (bywhat.equals(BY_SENTENCE))
			{
				processLemmatizedSentence(bywhat, rdu);
			}

			ProcessStatus.incrementStatus("TA added entry to " + bywhat);
			
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_ID.toLowerCase(), rdu.getDocumentUid());
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_CATEGORY.toLowerCase(), rdu.getCategory());
			//TALog.getLogger().debug("Category is "+rdu.getCategory());
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_SOURCE.toLowerCase(), rdu.getSourceIdentifier());
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_WEBSITE.toLowerCase(), rdu.getWebsite());
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_URI.toLowerCase(), rdu.getUri());
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_URL.toLowerCase(), rdu.getUrl());
			addToSpreadsheet(bywhat, rdu.getDocumentUid(), CollegeRawDataUnit.COLUMN_HEAD_URL_REPOSITORY.toLowerCase(),rdu.getUrlRepository());

			for (IdentifiedToken itoken : rdu.getItokenList())
			{
				if (!itoken.getType().equals(IdentifiedToken.TOKENTUPLE))
				{
					processToken(itoken, bywhat, rdu);
				}
			}

		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
		}
	}

	
	private void processToken(IdentifiedToken itoken, String bywhat, CollegeRawDataUnit rdu)
	{
		String valuePrefix = "";

		if (itoken.getType().equals(IdentifiedToken.VERB))
		{
			valuePrefix = VERB_PREFIX;
		}
		else if (itoken.getType().equals(IdentifiedToken.ADJECTIVE))
		{
			valuePrefix = ADJ_PREFIX;
		}

		addToSpreadsheet(bywhat, rdu.getDocumentUid(), valuePrefix + itoken.getValue(),
				String.valueOf(itoken.getFrequencyCount()));
	}
	
	
	private static void runOnceSetUp(ConfigurationBeanParent inconfig, ReentrantLock inlock) throws IKodaTextAnalysisException
	{
		try
		{
			brunOnceSetUp=true;
			config = inconfig;
			configt = (ConfigurationBeanForTextAnalysis_Reddit) inconfig.getTaConfig();
			analysisSubType = config.getAnalysisSubType();
			donePath = configt.getTextAnalysisDonePath();
            String csvPathstr = configt.getCsvPath();
			
			List<Path> files = StaticSundryUtils.listFiles(Paths.get(csvPathstr));
			
			for (Path p : files)
			{
				StaticSundryUtils.archiveFile(p);
			}
            setUpDataFile(configt, inconfig, BY_DOCUMENT);

			if (configt.isCollectBySentence())
			{
				setUpDataFile(configt, inconfig, BY_SENTENCE);
			}
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_CATEGORY.toLowerCase());
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_ID.toLowerCase());
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_WEBSITE.toLowerCase());
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_URI.toLowerCase());
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_SOURCE.toLowerCase());
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_URL.toLowerCase());
			columnsOutsideLibsvm.add(CollegeRawDataUnit.COLUMN_HEAD_URL_REPOSITORY.toLowerCase());
			columnsOutsideLibsvm.add(IdentifiedToken.LEMMATIZEDSENTENCE.toLowerCase());
			columnsOutsideLibsvm.add(IdentifiedToken.RAWSENTENCE.toLowerCase());

			failedPath = configt.getTextAnalysisFailedPath();
			ConfigurationBeanForFileIo_Generic fioConfig = (ConfigurationBeanForFileIo_Generic) inconfig.getFioConfig();
			ignoredPath = fioConfig.getFileIoUndeterminedPath();

			if (!(Paths.get(donePath).toFile().exists()))
			{
				Files.createDirectories(Paths.get(donePath));
			}
			if (!(Paths.get(csvPathstr).toFile().exists()))
			{
				Files.createDirectories(Paths.get(csvPathstr));
			}
			if (!(Paths.get(failedPath).toFile().exists()))
			{
				Files.createDirectories(Paths.get(failedPath));
			}
			if (!(Paths.get(ignoredPath).toFile().exists()))
			{
				Files.createDirectories(Paths.get(ignoredPath));
			}
			if (!(Paths.get(fioConfig.getFileIoJobPostingPath()).toFile().exists()))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoJobPostingPath()));
			}
			
			if (!(Paths.get(fioConfig.getFileIoUndeterminedPath()).toFile().exists()))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoUndeterminedPath()));
			}
			
			if (!(Paths.get(fioConfig.getFileIoOutBoxPath()).toFile().exists()))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoOutBoxPath()));
			}
			String[] inputs =
			{ fioConfig.getFileIoOutBoxPath() };

			List<Path> inputPaths = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++)
			{
				Path path = Paths.get(inputs[i]);
				inputPaths.add(path);
			}
			FileList.getInstance().init(PATHSNAME, inputPaths, TALog.getLogger(), inlock);
			FileList.getInstance().listFiles(PATHSNAME);
			
		}
		catch(Exception e)
		{
			throw new IKodaTextAnalysisException(e.getMessage(),e);
		}
	}
	


	public void runFileAnalyzer(boolean depends, ConfigurationBeanParent inconfig, ReentrantLock inlock)
			throws IOException, IKodaTextAnalysisException
	{
		try
		{
			TALog.getLogger().info("Starting " + this.toString());
			if(!brunOnceSetUp)
			{
				runOnceSetUp(inconfig, inlock);
			}
			lock = inlock;
			
			

			if (null == analysisSubType)
			{
				throw new IKodaTextAnalysisException("analysisSubType is NULL");
			}

			FileAnalyzerFactory.setSource(config);

			

			fileProcessStartTime = System.currentTimeMillis();

			
			if (inconfig.isMinimizeResourceUse())
			{
				baseSleepTime = 60000;
			}

			lock = inlock;
			


			stopDependsOnFileio = depends;
			if (depends)
			{
				upstreamThreadDone = false;
			}


			ready = true;

			initializeNlp(inconfig.getSpecifiedLanguage());
			TALog.getLogger().info("started thread");
			Thread.sleep(60000);
		}
		catch (Exception e)
		{
			ProcessStatus.put("TA START FAILED WITH ERROR", e.getMessage());
			TALog.getLogger().error(e.getMessage(), e);

		}

	}

	private boolean runStatusGo() throws IKodaUtilsException
	{
		
		TALog.getLogger().trace("runStatusGo");
		if (isFinalized())
		{
			return false;
		}
		if (isAborted() || isRestart())
		{

			TALog.getLogger().warn("\n\n Abort called \n\n " + this.toString());
			return false;
		}
		if (FileList.getInstance().isEmptyFileList(PATHSNAME))
		{
			if (!replenishFileList())
			{
				finalizeFiles(BY_DOCUMENT);
				finalizeFiles(BY_SENTENCE);
				TALog.getLogger().info("\n\n All files processed \n\n " + this.toString());
				abort();
				return false;
			}
			else
			{
				return true;
			}

		}
		else if (FileList.getInstance().getTotalCount() >= configt.getMaxNumberOfDocuments())
		{
			TALog.getLogger().debug("Files Processed: "+FileList.getInstance().getTotalCount());
			TALog.getLogger().debug("Max File Count: "+configt.getMaxNumberOfDocuments());
			TALog.getLogger().warn("\n\n maxNumberOfCsvRows met. Ending thread \n\n " + this.toString());
			finalizeFiles(BY_DOCUMENT);
			finalizeFiles(BY_SENTENCE);
			abort();
			return false;
		}
		return true;
	}

	private static synchronized void setUpDataFile(ConfigurationBeanForTextAnalysis_Reddit configt, ConfigurationBeanParent config, String bywhat) throws IKodaUtilsException
	{
		Spreadsheet.getInstance().initLibsvm2(bywhat, TALog.getLogger(), configt.getTargetColumnName(),
				configt.getCsvPath());
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setProjectPrefix(configt.getProjectPrefix());
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setTargetColumnName(configt.getTargetColumnName());
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setKeyspaceName(config.getKeyspaceName() + bywhat);
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setKeyspaceUUID(config.getConfigProperties().getAsString(Spreadsheet.getInstance().getLibSvmProcessor(bywhat).getKeyspaceName()));
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setLocalPorts(config.getMlLocalStreamPorts());
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setLocalUrl(config.getLocalUrls());
		Spreadsheet.getInstance().getLibSvmProcessor(bywhat).setPkColumnName(CollegeRawDataUnit.COLUMN_HEAD_ID);
		
		TALog.getLogger().info(
				"\n"+bywhat+
				"\n"+configt.getProjectPrefix()+
				"\n"+configt.getTargetColumnName()+
				"\n"+CollegeRawDataUnit.COLUMN_HEAD_ID+
				"\n"+config.getKeyspaceName() + bywhat+
				"\n"+config.getMlLocalStreamPorts()+
				"\n"+config.getLocalUrls()
				);
		String response=Spreadsheet.getInstance().getLibSvmProcessor(bywhat).sparkStreamInit(config.getMlServerUrl(),config.getMlServerPort());

		if(response.length()>20)
		{
			throw new IKodaUtilsException("From Server: "+response);
		}
		TALog.getLogger().info(Spreadsheet.getInstance().getLibSvmProcessor(bywhat).info());
	}

	private boolean validateContent(String fileContents, Path p)
	{
		if (null == fileContents)
		{
			moveFailedFile(p, "Null_Contents");
			return false;
		}
		if (fileContents.length() < configt.getMinFileLength())
		{
			moveFailedFile(p, "Too_Short");
			TALog.getLogger().debug("\n\nContent too short. Unlikely to be useful. Moving to next file.\n");
			return false;
		}
		if (fileContents.length() > configt.getMaxFileLength())
		{
			TALog.getLogger().warn("\n\nContent too large. Unlikely to be useful. Moving to next file.\n");
			moveFailedFile(p, "Too_Long");
			return false;
		}
		return true;
	}

}
