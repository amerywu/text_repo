package ikoda.netio.config;

import java.util.List;

public interface InterfaceConfigurationBeanParent
{

	public static final String JOB_DESCRIPTION_ANALYSIS_CONFIGURATION = "JOB_DESCRIPTION_ANALYSIS_CONFIGURATION";
	public static final String COLLEGE_ANALYSIS_CONFIGURATION = "COLLEGE_ANALYSIS_CONFIGURATION";
	public static final String REDDIT_ANALYSIS_CONFIGURATION = "REDDIT_ANALYSIS_CONFIGURATION";

	public static final String JOB_ANALYSIS_CONFIGURATION = "JOB_ANALYSIS_CONFIGURATION";
	public static final String JOB_ANALYSIS_CONFIGURATION_DIPLOMA_SUBTYPE = "DIPLOMA_LEVEL";
	public static final String JOB_ANALYSIS_CONFIGURATION_DEGREE_SUBTYPE = "DEGREE_LEVEL";

	public static final String LANGUAGE_EN = "en";
	public static final String LANGUAGE_ZH = "zh_CN";

	public static final String FILENAME_PARENT = "config.xml";
	public static final String FILENAME_NETIO = "nioConfig.xml";
	public static final String FILENAME_FILEIO = "fioConfig.xml";
	public static final String FILENAME_TEXTANALYSIS = "taConfig.xml";
	public static final String FILENAME_PERSISTENCE = "persistConfig.xml";

	public String getAnalysisSubType();

	public String getAnalysisType();

	public String getApplicationMainModuleName();

	public String getDatabaseDescriptor();

	public List<LanguageForAnalysis> getLanguagesForAnalysis();

	public InterfaceConfigurationBeanNetio getNetioConfig();

	public String getSpecifiedLanguage();

	public boolean isElegantStop();

	public boolean isMinimizeResourceUse();

	public boolean isRapidRandomBrowse();

	public boolean isRunFileio();

	public boolean isRunNetio();
	
	public boolean isSendToES();

	public boolean isRunPersistence();

	public boolean isRunPersistenceChores();

	public boolean isRunPersistenceReporting();

	public boolean isRunTextAnalysis();

	public List<String> retrieveRegionsForCurrentLocale();

	public String getMlLocalStreamPorts();

	public String getKeyspaceName();

	public int getMlServerPort();

	public String getMlServerUrl();
	
	public String getElasticSearchUser();
	
	public String getElasticSearchPassword() ;
	
	public String getElasticSearchUrl();
	
	public String getElasticSearchPort();
	

}