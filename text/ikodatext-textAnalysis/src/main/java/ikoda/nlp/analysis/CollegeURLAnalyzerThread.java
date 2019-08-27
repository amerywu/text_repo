package ikoda.nlp.analysis;

import java.io.File;
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

import org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor;


import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Reddit;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.structure.CollegeRawDataUnit;
import ikoda.nlp.structure.CollegeURLRawDataUnit;
import ikoda.nlp.structure.IKodaTextAnalysisException;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.utils.CSVSpreadsheetCreator;
import ikoda.utils.FileList;
import ikoda.utils.IDGenerator;
import ikoda.utils.IKodaUtilsException;
import ikoda.utils.LibSvmProcessor;
import ikoda.utils.ProcessStatus;
import ikoda.utils.SSm;
import ikoda.utils.SimpleHtmlUnit;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;
import ikoda.utils.TicToc;

public class CollegeURLAnalyzerThread extends FileAnalyzerThread
{

	private static boolean saveFile = false;
	private static boolean finalized = false;

	protected static final String LEMMATIZEDSENTENCE = "LEMMATIZEDSENTENCE";
	protected static final String UNDERSCORE = "_";
	protected static final String ZERO = "0";
	protected static final String OUTPUT_FILE = "collegeurlmodel";
	protected static final String INPUT_FILE = "RawTrainingData";
	protected static final String COL_CATEGORY = "CATEGORY";
	protected static final String COL_FULL_URL = "FULL_URL";
	protected static  String col_target = "aa_label";

	private static final String[] ignoreWords =
	{ "Aa","Aat","About","Academic","Academics","acadiau","Admission","Admissions","Affairs",
			"Alabama","Alabama","Alaska","Allard","ALLISON","And","Areas","Arizona","Arkansas",
			"ascc","Associate","Associates","At","Bachelor","Bachelors","Bakersfield",
			"bakersfieldcollege","Berkeley","Bevill","Bishop","bmcc","bmit","British","Brock",
			"Brocku","bscc","bu","Bulletin","By","Calendar","California","Can","Canada","Canada",
			"canton","Career","Careers","Carleton","Catalog","catalogue","CBU","ccfi","Ccsf",
			"ccsf","ceen","Cerro","cerrocoso","Certificate","Certificates","Chair","Citrus",
			"citruscollege","Coastal","coastline","College","CollegeCatalog","Colorado",
			"Columbia","Columbia","Community","Connecticut","Content","Coso","Course","Courses",
			"cuny","current","Dal","Dalhousie","Dean","Degree","Degrees","Delaware","DelawareTechnical",
			"Delhi","Delta","DENVER","Department","Departments","Designation","Disciplines","DTCC",
			"Event","Events","Extraordinary","Facebook","FACULTIES","Faculty","FALL","Florida","Francisco",
			"Fredericton","Footer","Funding","gavilan","georgetown","Georgia","Grad","gradprograms","gradstudies",
			"Graduate","Guide","Hawaii","Home","Html","Header","Human","Idaho","Illinois","Indiana","In","Index",
			"Iowa","Kansas","Kentucky","king","Kings","king's","Kpu","linkedin","Lakeheadu","lethbridge","Lincoln","logo",
			"Louisiana","Ma","macewan","Maine","Manitoba","manoa","Maryland","Massachusetts","Mcmaster","Michigan",
			"Midmich","Minnesota","Mississippi","Missouri","miwsfpa","MontanaNebraska","Moraine","morainevalley","MOUNT",
			"MTA","ncsu","Nebraska","Nebraskalincoln","Nevada","NewHampshire","NewJersey","NewMexico","News","NewYork",
			"NorthCarolina","NorthDakota","Of","Ohio","Oklahoma","Option","Oregon","Page","Pages","paradisevalley",
			"PennsylvaniaRhodeIsland","Phd","Php","Program","Programs","pvcc","redeemer","Research","Resource",
			"Ryerson","Sala","Sauder","San","School","SchoolsFaculties","Search","Sfsu","SFU","smartcatalogiq",
			"smcc","Smu","SouthCarolina","SouthDakota","Southmountaincc","SPRING","Statewide","stu","Student","Students",
			"Studies","Study","Study","Study","SUMMER","SUNY","SUNYCanton","sunydutchess","sunyjefferson","Tamu",
			"Tennessee","Texas","Thomas","THOMPSON","Transfer","Transfers","triton","Tru","Twitter","Tunxis","Ubc",
			"Ucsd","Ucla","uleth","Umanitoba","Unc","Undergrad","undergradprograms","Undergraduate","University",
			"Unl","unomaha","UOIT","ustpaul","Utah","Uwaterloo","Uwo","Vermont","Virginia","w3","Washington","WCM",
			"Welcome","WestVirginia","Wisc","Wisconsin","Wyoming","YOUTUBE","INSTAGRAM","flickr"
	};

	private int entryCount = 0;

	private ConfigurationBeanForTextAnalysis_Reddit configt;

	public CollegeURLAnalyzerThread()
	{
		super();
		System.out.println("CollegeURLAnalyzerThread init");
	}

	private void addToSpreadsheet(String file, String uid, String columnHead, String rowValue)
	{
		try
		{
			Spreadsheet.getInstance().getCsvSpreadSheet(file).addCell(uid, columnHead, rowValue);
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
		}
	}

	private Collection<IdentifiedToken> breakIntoTokens(String toAnalyze) throws IKodaTextAnalysisException
	{

		try
		{
			ProcessStatus.put("Entry Count", entryCount);

			CollegeProgramAnalyzer cpa = (CollegeProgramAnalyzer) FileAnalyzerFactory
					.getProcessorForDataSource(StringConstantsInterface.DATASOURCE_COLLEGE_PROGRAM, pipeline, config);
			cpa.setCollectPos(false);
			cpa.setLemmatizeText(false);
			cpa.setBanSentenceDuplicates(false);
			cpa.setCollectBySentence(false);
			cpa.setCollectAllTerms(true);
			cpa.setIgnoreWordMatchingIgnoreCase(ignoreWords);

			boolean result = cpa.processString(toAnalyze);
			if (!result)
			{
				TALog.getLogger().warn("Failed: " + toAnalyze);

			}

			Collection<IdentifiedToken> tokens = cpa.getCountedTokens();

			return tokens;
		}
		catch (Exception e)
		{
			throw new IKodaTextAnalysisException(e.getMessage(), e);
		}
	}

	private String cleanString(String s)
	{
		if (null == s)
		{
			return "";
		}

		String cleanString = s.toLowerCase();
		cleanString = cleanString.replace(".edu", "");
		cleanString = cleanString.replace(".ca", "");
		cleanString = cleanString.replace(".html", "");
		cleanString = cleanString.replace("www", "");
		cleanString = cleanString.replace(".", " ");
		cleanString = cleanString.replace("|", " ");
		cleanString = cleanString.replace("-", " ");
		cleanString = cleanString.replace("_", " ");
		cleanString = cleanString.replace("http:", "");
		cleanString = cleanString.replace("#", "");
		cleanString = cleanString.replace(":", " ");
		cleanString = cleanString.replace("=", " ");
		cleanString = cleanString.replace("?", " ");
		cleanString = cleanString.replace("&", " ");
		cleanString = cleanString.replace("   ", " ");
		cleanString = cleanString.replace("  ", " ");

		return cleanString;
	}

	private CollegeURLRawDataUnit createRDU(Collection<IdentifiedToken> tokens, String category, String url,
			String target) throws IKodaUtilsException
	{
		try
		{
			CollegeURLRawDataUnit rdu = new CollegeURLRawDataUnit(entryCount);
			rdu.getItokenList().addAll(tokens);
			TALog.getLogger().debug("count itokens: " + tokens.size());
			TALog.getLogger().debug("uid: " + rdu.getDocumentUid());
			TALog.getLogger().debug(" itokens: " + tokens);

			rdu.setCategory(category);
			rdu.setUrl(url);
			rdu.setTarget(target);
			return rdu;
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
			return new CollegeURLRawDataUnit(-1);

		}
	}

	@Override
	protected synchronized boolean doFileProcess()
	{
		try
		{
			fileProcessStartTime = System.currentTimeMillis();
			CSVSpreadsheetCreator inputCsv = Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE);
			Map<String, HashMap<String, String>> data = inputCsv.getData();
			Iterator<String> itrData = data.keySet().iterator();

			sleepTime = 10;
			baseSleepTime = 5;
			while (itrData.hasNext())
			{
				entryCount = entryCount + 1;
				System.out.print(entryCount + " ");
				String key = itrData.next();
				Map<String, String> row = data.get(key);

				String category = row.get(COL_CATEGORY);
				String url = row.get(COL_FULL_URL);
				String target = row.get(col_target);
				String toAnalyze = cleanString(category) + cleanString(url);

				Collection<IdentifiedToken> tokens = breakIntoTokens(toAnalyze);
				CollegeURLRawDataUnit rdu = createRDU(tokens, category, url, target);

				processSpreadsheet(rdu);

			}

			String[] ignore =
			{ "AA_CATEGORY", "AA_URL", "A_RowId" };

			
			Spreadsheet.getInstance().getLibSvmProcessor(OUTPUT_FILE).printLibSvmFinal("trainingSet", ignore);



			TALog.getLogger().info("Streaming ..."+Spreadsheet.getInstance().getLibSvmProcessor(OUTPUT_FILE).info());
			Spreadsheet.getInstance().getLibSvmProcessor(OUTPUT_FILE).sparkStreamRunLibsvm(config.getMlServerUrl(),
					config.getMlServerPort(), ignore);
			
			TALog.getLogger().info("Pausing for 8 minutes.");
			Thread.sleep(440000);

			SSm.getAppLogger().info("\n\n\nGenerating Model\n\n\n");
			SimpleHtmlUnit shu = new SimpleHtmlUnit(config.getMlServerUrl(), config.getMlServerPort());
			HashMap<String, String> params = new HashMap<>();
			params.put("DATA_SOURCE", Spreadsheet.getInstance().getLibSvmProcessor(OUTPUT_FILE).getProjectPrefix()+OUTPUT_FILE);

			String response = shu.getAsText("generateURLPredictionModel", params);
			SSm.getAppLogger().info("\n\nPROCESS COMPLETE "+response+ "\n\n");
			System.out.println("\n\nPROCESS COMPLETE "+response+ "\n\n");
			return false;
		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);

			ProcessStatus.incrementStatus(super.getThreadId() + " TA errors in FileAnalyzerThread");
			return false;
		}
	}

	public synchronized LibSvmProcessor getTokensFromUrlString(String s, String lang) throws IKodaTextAnalysisException
	{
		try
		{

			if (null == pipeline)
			{
				initializeNlp(lang, "tokenize, ssplit, pos, lemma");
			}
			String clean = cleanString(s);
			Collection<IdentifiedToken> tokens = breakIntoTokens(clean);
			String spreadsheetName = "oneline";

			Spreadsheet.getInstance().initLibsvm2(spreadsheetName, "target", 
					new File(".").getCanonicalPath());
			Spreadsheet.getInstance().getLibSvmProcessor(spreadsheetName).setPkColumnName("A_RowId");
			
			int uid = 0;

			for (IdentifiedToken itoken : tokens)
			{
				Spreadsheet.getInstance().getLibSvmProcessor(spreadsheetName).addCell(0, itoken.getValue(),
						itoken.getFrequencyCount());
			}

			LibSvmProcessor libsvm = Spreadsheet.getInstance().getLibSvmProcessor(spreadsheetName);
			Spreadsheet.getInstance().removeSpreadsheet(spreadsheetName);
			return libsvm;

		}
		catch (Exception e)
		{
			throw new IKodaTextAnalysisException(e.getMessage(), e);
		}
	}

	private void processSpreadsheet(CollegeURLRawDataUnit rdu)
	{
		try
		{
			TALog.getLogger().debug("TA added entry to " + OUTPUT_FILE + " for TARGET: " + rdu.getTarget() + " with Id "
					+ rdu.getDocumentUid());

			addToSpreadsheet(OUTPUT_FILE, rdu.getDocumentUid(), CollegeURLRawDataUnit.COLUMN_HEAD_CATEGORY,
					rdu.getCategory());
			addToSpreadsheet(OUTPUT_FILE, rdu.getDocumentUid(), CollegeURLRawDataUnit.COLUMN_HEAD_URL, rdu.getUrl());
			addToSpreadsheet(OUTPUT_FILE, rdu.getDocumentUid(), col_target,
					rdu.getTarget());

			for (IdentifiedToken itoken : rdu.getItokenList())
			{
				if (!itoken.getType().equals(IdentifiedToken.TOKENTUPLE))
				{
					processToken(itoken, OUTPUT_FILE, rdu);
				}
			}

		}
		catch (Exception e)
		{
			TALog.getLogger().error(e.getMessage(), e);
		}
	}

	private void processToken(IdentifiedToken itoken, String coreFileName, CollegeURLRawDataUnit rdu)
	{

		String concatenatedName = coreFileName;

		if (!itoken.getValue().matches(".*\\d+.*"))
		{

			addToSpreadsheet(concatenatedName, rdu.getDocumentUid(), itoken.getValue(),
					String.valueOf(itoken.getFrequencyCount()));
		}

	}

	@Override
	public void runFileAnalyzer(boolean depends, ConfigurationBeanParent inconfig, ReentrantLock inlock)
			throws IOException, IKodaTextAnalysisException, IKodaUtilsException
	{
		System.out.println("runFileAnalyzer 1");
		TALog.getLogger().debug("Starting " + this.toString());
		lock = inlock;
		
		config = inconfig;
		configt = (ConfigurationBeanForTextAnalysis_Reddit) inconfig.getTaConfig();
		
		Spreadsheet.getInstance().initLibsvm2(OUTPUT_FILE, TALog.getLogger().getName(), configt.getTargetColumnName(),
				configt.getCsvPath());
		
		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setLocalPorts(config.getMlLocalStreamPorts());
		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setLocalUrl(config.getLocalUrls());
		Spreadsheet.getInstance().getLibSvmProcessor(OUTPUT_FILE).sparkStreamInit(config.getMlServerUrl(),Integer.valueOf(config.getMlServerPort()).intValue());
		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setKeyspaceUUID(config.getConfigProperties().getAsString("collegeurlmodel"));
		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setKeyspaceName(OUTPUT_FILE);
		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setTargetColumnName(configt.getTargetColumnName());
		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setPkColumnName("uid");
		col_target=configt.getTargetColumnName();
		
		System.out.println("runFileAnalyzer 2");
		analysisSubType = config.getAnalysisSubType();

		FileAnalyzerFactory.setSource(config);

		

		fileProcessStartTime = System.currentTimeMillis();
		System.out.println("runFileAnalyzer 3");
		System.out.println("runFileAnalyzer 3 " + configt);
		donePath = configt.getTextAnalysisDonePath();

		Spreadsheet.getInstance().initCsvSpreadsheet(INPUT_FILE, TALog.getLogger(), configt.getCsvPath());
		Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE).setProjectPrefix(configt.getProjectPrefix());
		Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE).setTargetColumnName(configt.getTargetColumnName());
		Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE).setKeyspaceUUID(java.util.UUID.randomUUID().toString());
		Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE).setKeyspaceName(OUTPUT_FILE);
		Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE).loadCsv("RawTrainingData.csv", "A_RowId");
		TALog.getLogger().debug("Loaded training set. Row count: "
				+ Spreadsheet.getInstance().getCsvSpreadSheet(INPUT_FILE).rowCount());


		Spreadsheet.getInstance().getCsvSpreadSheet(OUTPUT_FILE).setProjectPrefix(configt.getProjectPrefix());

		if (inconfig.isMinimizeResourceUse())
		{
			baseSleepTime = 60000;
		}

		lock = inlock;

		stopDependsOnFileio = false;

		ready = true;

		initializeNlp(inconfig.getSpecifiedLanguage());
		TALog.getLogger().debug("starting thread");

	}

}
