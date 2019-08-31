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

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Reddit;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.structure.CollegeRawDataUnit;
import ikoda.nlp.structure.EsJson;
import ikoda.nlp.structure.IKodaTextAnalysisException;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.utils.ElasticSearchManager;
import ikoda.utils.FileList;
import ikoda.utils.IDGenerator;
import ikoda.utils.IKodaUtilsException;
import ikoda.utils.ProcessStatus;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;
import ikoda.utils.TicToc;

public class CollegeProgramElasticSearchDispatcher extends FileAnalyzerThread {
	private static boolean finalized = false;
	private static boolean brunOnceSetUp = false;

	protected static final String BY_DOCUMENT = "BY-DOCUMENT";
	protected static final String BY_SENTENCE = "BY-SENTENCE";
	private static final String MAXPERBATCH = "maxRowPerBatch";
	protected static final String LEMMATIZEDSENTENCE = "LEMMATIZEDSENTENCE";
	protected static final String UNDERSCORE = "_";
	protected static final String ZERO = "0";
	private static final String ADJ_PREFIX = "adj_";
	private static final String VERB_PREFIX = "v_";
	private static final String END_TAGS = "CLOSETAGS";

	private static ConfigurationBeanForTextAnalysis_Reddit configt;

	private boolean isNewDocument(CollegeRawDataUnit rdu) {
		try {
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(QueryBuilders.matchQuery("doc_url", rdu.getUrl()));
			SearchRequest searchRequest = new SearchRequest();
			searchRequest.indices(EsJson.REDDIT_INDEX_NAME);
			searchRequest.source(sourceBuilder);
			SearchResponse searchResponse = ElasticSearchManager.getInstance().search(searchRequest);
			if (searchResponse.getHits().getHits().length > 0) {
				TALog.getLogger().info("Document already exists " + rdu.getUrl());
				ProcessStatus.incrementStatus("TA DOC EXISTS");

				return false;
			} else {
				ProcessStatus.incrementStatus("TA DOC IS NEW");

				return true;
			}
		} catch (Exception e) {
			TALog.getLogger().warn("documentExists ERROR" + e.getMessage());
			return true;
		}
	}

	private boolean dispatchToES(CollegeRawDataUnit rdu) {
		try {
			if(isNewDocument(rdu)) {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder.startObject();
			{
				builder.field("category", rdu.getCategory());
				builder.field("content", rdu.getRawData());
				builder.field("doc_id", rdu.getUrl().replaceAll("[^a-zA-Z0-9]", "-"));
				builder.field("doc_url", rdu.getUrl());
				builder.field("created", System.currentTimeMillis());
			}
			builder.endObject();
			ElasticSearchManager.getInstance().addDocument(builder, EsJson.REDDIT_INDEX_NAME);

			return true;
			}
			else {
				return false;
			}
		} catch (Exception e) {
			TALog.getLogger().warn(e.getMessage(), e);
			return false;
		}

	}

	public CollegeProgramElasticSearchDispatcher() {
		super();
	}

	public CollegeProgramElasticSearchDispatcher(int threadId) {
		super(threadId);
	}

	private String stripTags(String content) {
		int idx = content.lastIndexOf(END_TAGS);
		if (idx > 0) {
			return content.substring(idx, content.length());
		} else {
			return content;
		}

	}

	private CollegeRawDataUnit createRDU(String fileContents) throws IKodaUtilsException {
		try {
			CollegeRawDataUnit rdu = new CollegeRawDataUnit(IDGenerator.getInstance().nextID());

			String category = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_PREDICTION_OPEN,
					StringConstantsInterface.SPIDERTAG_PREDICTION_CLOSE, fileContents);
			String website = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_HOST_OPEN,
					StringConstantsInterface.SPIDERTAG_HOST_CLOSE, fileContents);
			String sid = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_TBID_OPEN,
					StringConstantsInterface.SPIDERTAG_TBID_CLOSE, fileContents);
			String uri = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_URI_OPEN,
					StringConstantsInterface.SPIDERTAG_URI_CLOSE, fileContents).replaceAll("/", "|").trim();
			String url = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_FULLURL_OPEN,
					StringConstantsInterface.SPIDERTAG_FULLURLCLOSE, fileContents);
			String urlRepository = StaticSundryUtils
					.extractHeader(StringConstantsInterface.SPIDERTAG_URLREPOSITORY_OPEN,
							StringConstantsInterface.SPIDERTAG_URLREPOSITORY_CLOSE, fileContents)
					.replaceAll("/", "|");

			category = processCategory(category, uri);

			rdu.setCategory(category.replace("|new","").replace("|controversial", ""));
			rdu.setWebsite(website);
			rdu.setSourceIdentifier(sid);
			rdu.setUri(uri);
			rdu.setUrl(url);
			rdu.setUrlRepository(urlRepository);
			rdu.setRawData(stripTags(fileContents));

			return rdu;
		} catch (Exception e) {
			TALog.getLogger().error(e.getMessage(), e);
			return new CollegeRawDataUnit(IDGenerator.getInstance().nextID());

		}
	}

	@Override
	protected synchronized boolean doFileProcess() {
		try {
			fileProcessStartTime = System.currentTimeMillis();

			TALog.getLogger().info("\n\ndoFileProcess ");
			TALog.getLogger().debug("totalRowCount " + FileList.getInstance().getTotalCount());
			TALog.getLogger().debug("Files Processed: " + FileList.getInstance().getTotalCount());
			TALog.getLogger().debug("Max File Count: " + configt.getMaxNumberOfDocuments());

			if (!runStatusGo()) {
				return false;
			}

			sleepTime = 3000;

			Path p = FileList.getInstance().getNextFile(PATHSNAME);
			if (null == p) {
				replenishFileList();
				return true;
			}

			currentFilePath = p.toString();
			TALog.getLogger().info("PATH: " + currentFilePath);
			if (p.getFileName().toString().contains(TXT)) {
				TALog.getLogger().debug("handling file " + p.getFileName());
				pipeline(p);
			} else {
				moveUndeterminedFile(p);
			}

			TALog.getLogger()
					.trace("\n\n\n\n\n\n\nTA: Number of files processed: " + FileList.getInstance().getTotalCount());
			return true;
		} catch (Exception e) {
			TALog.getLogger().error(e.getMessage(), e);
			ProcessStatus.incrementStatus(super.getThreadId() + " TA errors in FileAnalyzerThread");
			return true;
		}
	}

	private String getFileContents(Path p) {
		String fileContents = null;
		try {
			if (lock.tryLock(10, TimeUnit.SECONDS)) {
				byte[] encoded = Files.readAllBytes(p);
				fileContents = new String(encoded);
			}
		} catch (Exception e) {
			TALog.getLogger().debug(e.getMessage(), e);
			return null;
		} finally {
			lock.unlock();
		}
		return fileContents;
	}

	/** Used when initializing as field variable */
	public void initializeFileAnalyzer(ConfigurationBeanParent inconfig, ReentrantLock inlock)
			throws IOException, Exception {
		super.initializeFileAnalyzer(inconfig, inlock);
	}

	@Override
	protected boolean pipeline(Path p) {

		try {
			TALog.getLogger().debug("\n\n=============pipeline===================\n" + p);
			TicToc tt = new TicToc();

			tt.tic("sendToAnalyzer");

			String fileContents = getFileContents(p);
			if (!validateContent(fileContents, p)) {
				return false;
			}

			CollegeRawDataUnit rdu = createRDU(fileContents);
			dispatchToES(rdu);
			ProcessStatus.incrementStatus("TA SUCCESS");

			TALog.getLogger().trace("::" + tt.toc("sendToAnalyzer"));
			moveSuccessfulFile(p, "SUCCESS");

			FileList.getInstance().incrementCount();

			TALog.getLogger().debug("\n\n----------------------------pipeline done---------------------------\n\n");
			return true;
		} catch (Exception e) {
			TALog.getLogger().error(e.getMessage(), e);
			ProcessStatus.incrementStatus(super.getThreadId() + "TA Error in sendToAnalyzer");
		}
		return false;
	}

	private String processCategory(String category, String uri) {

		if (config.getAnalysisSubType().toLowerCase().contains("reddit")) {
			if (null != uri) {

				if (StaticSundryUtils.countOccurrencesOfChar(uri, '|') > 3) {
					return uri.substring(0, uri.lastIndexOf("|"));
				} else {
					return uri;
				}
			} else {
				return "unknown";
			}
		}

		else if (null == category) {
			return "unknown";
		} else if (category.endsWith("-")) {
			return category.substring(0, category.length() - 1);
		} else {
			return category;
		}
	}

	private static void runOnceSetUp(ConfigurationBeanParent inconfig, ReentrantLock inlock)
			throws IKodaTextAnalysisException {
		try {
			brunOnceSetUp = true;
			config = inconfig;
			configt = (ConfigurationBeanForTextAnalysis_Reddit) inconfig.getTaConfig();
			analysisSubType = config.getAnalysisSubType();
			donePath = configt.getTextAnalysisDonePath();
			String csvPathstr = configt.getCsvPath();

			List<Path> files = StaticSundryUtils.listFiles(Paths.get(csvPathstr));

			for (Path p : files) {
				StaticSundryUtils.archiveFile(p);
			}

			failedPath = configt.getTextAnalysisFailedPath();
			ConfigurationBeanForFileIo_Generic fioConfig = (ConfigurationBeanForFileIo_Generic) inconfig.getFioConfig();
			ignoredPath = fioConfig.getFileIoUndeterminedPath();

			if (!(Paths.get(donePath).toFile().exists())) {
				Files.createDirectories(Paths.get(donePath));
			}
			if (!(Paths.get(csvPathstr).toFile().exists())) {
				Files.createDirectories(Paths.get(csvPathstr));
			}
			if (!(Paths.get(failedPath).toFile().exists())) {
				Files.createDirectories(Paths.get(failedPath));
			}
			if (!(Paths.get(ignoredPath).toFile().exists())) {
				Files.createDirectories(Paths.get(ignoredPath));
			}
			if (!(Paths.get(fioConfig.getFileIoJobPostingPath()).toFile().exists())) {
				Files.createDirectories(Paths.get(fioConfig.getFileIoJobPostingPath()));
			}

			if (!(Paths.get(fioConfig.getFileIoUndeterminedPath()).toFile().exists())) {
				Files.createDirectories(Paths.get(fioConfig.getFileIoUndeterminedPath()));
			}

			if (!(Paths.get(fioConfig.getFileIoOutBoxPath()).toFile().exists())) {
				Files.createDirectories(Paths.get(fioConfig.getFileIoOutBoxPath()));
			}
			String[] inputs = { fioConfig.getFileIoOutBoxPath() };

			List<Path> inputPaths = new ArrayList<>();
			for (int i = 0; i < inputs.length; i++) {
				Path path = Paths.get(inputs[i]);
				inputPaths.add(path);
			}
			FileList.getInstance().init(PATHSNAME, inputPaths, TALog.getLogger(), inlock);
			FileList.getInstance().listFiles(PATHSNAME);

		} catch (Exception e) {
			throw new IKodaTextAnalysisException(e.getMessage(), e);
		}
	}

	public void runFileAnalyzer(boolean depends, ConfigurationBeanParent inconfig, ReentrantLock inlock)
			throws IOException, IKodaTextAnalysisException {
		try {
			TALog.getLogger().info("Starting " + this.toString());
			if (!brunOnceSetUp) {
				runOnceSetUp(inconfig, inlock);
			}
			lock = inlock;

			if (null == analysisSubType) {
				throw new IKodaTextAnalysisException("analysisSubType is NULL");
			}

			FileAnalyzerFactory.setSource(config);

			fileProcessStartTime = System.currentTimeMillis();

			if (inconfig.isMinimizeResourceUse()) {
				baseSleepTime = 60000;
			}

			lock = inlock;

			stopDependsOnFileio = depends;
			if (depends) {
				upstreamThreadDone = false;
			}

			ready = true;

			TALog.getLogger().info("started thread");

		} catch (Exception e) {
			ProcessStatus.put("TA START FAILED WITH ERROR", e.getMessage());
			TALog.getLogger().error(e.getMessage(), e);

		}

	}

	private boolean runStatusGo() throws IKodaUtilsException {

		if (isAborted() || isRestart()) {

			TALog.getLogger().warn("\n\n Abort called \n\n " + this.toString());
			return false;
		}
		if (FileList.getInstance().isEmptyFileList(PATHSNAME)) {
			if (!replenishFileList()) {

				TALog.getLogger().info("\n\n All files processed \n\n " + this.toString());
				abort();
				return false;
			} else {
				return true;
			}

		} else if (FileList.getInstance().getTotalCount() >= configt.getMaxNumberOfDocuments()) {
			TALog.getLogger().debug("Files Processed: " + FileList.getInstance().getTotalCount());
			TALog.getLogger().debug("Max File Count: " + configt.getMaxNumberOfDocuments());
			TALog.getLogger().warn("\n\n maxNumberOfCsvRows met. Ending thread \n\n " + this.toString());

			abort();
			return false;
		}
		return true;
	}

	private boolean validateContent(String fileContents, Path p) {
		if (null == fileContents) {
			moveFailedFile(p, "Null_Contents");
			return false;
		}
		if (fileContents.length() < configt.getMinFileLength()) {
			moveFailedFile(p, "Too_Short");
			TALog.getLogger().debug("\n\nContent too short. Unlikely to be useful. Moving to next file.\n");
			return false;
		}
		if (fileContents.length() > configt.getMaxFileLength()) {
			TALog.getLogger().warn("\n\nContent too large. Unlikely to be useful. Moving to next file.\n");
			moveFailedFile(p, "Too_Long");
			return false;
		}
		return true;
	}

}
