package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import ikoda.netio.NioLog;

@XmlType(propOrder =
{ "applicationMainModuleName", "databaseDescriptor", "analysisType", "analysisSubType", "specifiedLanguage",
		"languagesForAnalysis", "runNetio", "runFileio", "runTextAnalysis", "runPersistence", "runPersistenceReporting",
		"runPersistenceChores", "runTraining", "runSimpleStream", "elegantStop", "netioResumeMode",
		"minimizeResourceUse", "rapidRandomBrowse", "mlServerUrl", "mlServerPort", "mlLocalStreamPorts", "localUrls",
		"keyspaceName", "configProperties","elasticSearchUser","elasticSearchPassword","elasticSearchUrl",
		"elasticSearchPort", "sendToES" })

@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ConfigurationBeanParent implements InterfaceConfigurationBeanParent, InterfaceBindingConfig
{

	private String applicationMainModuleName;
	private String mlServerUrl;
	private int mlServerPort;
	private String mlLocalStreamPorts;
	private String localUrls;
	private String analysisType;
	private String analysisSubType;
	private String keyspaceName;
	private List<LanguageForAnalysis> languagesForAnalysis = new ArrayList<LanguageForAnalysis>();

	private String specifiedLanguage;
	private String databaseDescriptor;
	private String elasticSearchUser;
	private String elasticSearchPassword;
	private String elasticSearchUrl;
	private String elasticSearchPort;
	private InterfaceConfigurationBeanNetio netioConfig;
	private InterfaceConfigurationBeanFileio fioConfig;
	private InterfaceConfigurationBeanTextAnalysis taConfig;
	private InterfaceConfigurationBeanPersist persistConfig;
	private boolean sendToES;
	private boolean runNetio;
	private boolean runFileio;
	private boolean runTextAnalysis;
	private boolean runPersistence;
	private boolean runPersistenceReporting;
	private boolean runPersistenceChores;
	private boolean runTraining;
	private boolean runSimpleStream;
	private boolean elegantStop;
	private boolean netioResumeMode;
	private boolean minimizeResourceUse;
	private boolean rapidRandomBrowse;
	private boolean childrenLoaded = false;
	private XMLPropertyMap configProperties = new XMLPropertyMap();

	@Override
	public String getAnalysisSubType()
	{
		return analysisSubType;
	}

	@Override
	public String getAnalysisType()
	{
		return analysisType;
	}

	@Override
	public String getApplicationMainModuleName()
	{
		return applicationMainModuleName;
	}

	public XMLPropertyMap getConfigProperties()
	{
		return configProperties;
	}

	@Override
	public String getDatabaseDescriptor()
	{
		return databaseDescriptor;
	}

	@XmlTransient
	public InterfaceConfigurationBeanFileio getFioConfig()
	{
		if (!childrenLoaded)
		{
			loadChildren();
		}
		return fioConfig;
	}

	@Override
	public String getKeyspaceName()
	{
		return keyspaceName;
	}

	@Override
	public List<LanguageForAnalysis> getLanguagesForAnalysis()
	{
		return languagesForAnalysis;
	}

	public String getLocalUrls()
	{
		return localUrls;
	}

	@Override
	public String getMlLocalStreamPorts()
	{
		return mlLocalStreamPorts;
	}

	@Override
	public int getMlServerPort()
	{
		return mlServerPort;
	}

	@Override
	public String getMlServerUrl()
	{
		return mlServerUrl;
	}

	@Override
	@XmlTransient
	public InterfaceConfigurationBeanNetio getNetioConfig()
	{

		if (!childrenLoaded)
		{
			loadChildren();
		}
		return netioConfig;
	}

	@XmlTransient
	public InterfaceConfigurationBeanPersist getPersistConfig()
	{
		if (!childrenLoaded)
		{
			loadChildren();
		}
		return persistConfig;
	}

	@Override
	public String getSpecifiedLanguage()
	{
		return specifiedLanguage;
	}

	@XmlTransient
	public InterfaceConfigurationBeanTextAnalysis getTaConfig()
	{
		if (!childrenLoaded)
		{
			loadChildren();
		}
		return taConfig;
	}

	@Override
	public boolean isElegantStop()
	{
		return elegantStop;
	}

	@Override
	public boolean isMinimizeResourceUse()
	{
		return minimizeResourceUse;
	}

	public boolean isNetioResumeMode()
	{
		return netioResumeMode;
	}

	@Override
	public boolean isRapidRandomBrowse()
	{
		return rapidRandomBrowse;
	}

	@Override
	public boolean isRunFileio()
	{
		return runFileio;
	}

	@Override
	public boolean isRunNetio()
	{
		return runNetio;
	}

	@Override
	public boolean isRunPersistence()
	{
		return runPersistence;
	}

	@Override
	public boolean isRunPersistenceChores()
	{
		return runPersistenceChores;
	}

	@Override
	public boolean isRunPersistenceReporting()
	{
		return runPersistenceReporting;
	}

	public boolean isRunSimpleStream()
	{
		return runSimpleStream;
	}

	@Override
	public boolean isRunTextAnalysis()
	{
		return runTextAnalysis;
	}

	public boolean isRunTraining()
	{
		return runTraining;
	}

	public void loadChildren()
	{
		try
		{
			ConfigurationBeanFactory.getInstance().loadChildren(this, new ReentrantLock());
			childrenLoaded = true;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	@Override
	public List<String> retrieveRegionsForCurrentLocale()
	{
		NioLog.getLogger().debug("specifiedLanguage " + specifiedLanguage);
		NioLog.getLogger().debug("languagesForAnalysis " + languagesForAnalysis);
		for (LanguageForAnalysis la : languagesForAnalysis)
		{
			if (la.getLocaleCode().trim().toUpperCase().equals(specifiedLanguage.trim().toUpperCase()))
			{
				return la.getRegions();
			}
		}
		return null;
	}

	@XmlElement
	public void setAnalysisSubType(String analysisSubType)
	{
		this.analysisSubType = analysisSubType;
	}

	@XmlElement
	public void setAnalysisType(String analysisType)
	{
		this.analysisType = analysisType;
	}

	@XmlElement
	public void setApplicationMainModuleName(String jobAnalysisModuleName)
	{
		this.applicationMainModuleName = jobAnalysisModuleName;
	}

	@XmlElement
	public void setConfigProperties(XMLPropertyMap mlServerUuids)
	{
		this.configProperties = mlServerUuids;
	}

	@XmlElement
	public void setDatabaseDescriptor(String databaseDescriptor)
	{
		this.databaseDescriptor = databaseDescriptor;
	}

	@XmlElement
	public void setElegantStop(boolean elegantStop)
	{
		this.elegantStop = elegantStop;
	}

	public void setFioConfig(InterfaceConfigurationBeanFileio fioConfig)
	{

		this.fioConfig = fioConfig;
	}

	@XmlElement
	public void setKeyspaceName(String keyspaceName)
	{
		this.keyspaceName = keyspaceName;
	}

	@XmlElement
	public void setLanguagesForAnalysis(List<LanguageForAnalysis> languagesForAnalysis)
	{
		this.languagesForAnalysis = languagesForAnalysis;
	}

	@XmlElement
	public void setLocalUrls(String localUrls)
	{
		this.localUrls = localUrls;
	}

	@XmlElement
	public void setMinimizeResourceUse(boolean minimizeResourceUse)
	{
		this.minimizeResourceUse = minimizeResourceUse;
	}

	@XmlElement
	public void setMlLocalStreamPorts(String mlLocalStreamPort)
	{
		this.mlLocalStreamPorts = mlLocalStreamPort;
	}

	@XmlElement
	public void setMlServerPort(int mlServerPort)
	{
		this.mlServerPort = mlServerPort;
	}

	@XmlElement
	public void setMlServerUrl(String mlServerUrl)
	{
		this.mlServerUrl = mlServerUrl;
	}

	public void setNetioConfig(InterfaceConfigurationBeanNetio netioConfig)
	{
		this.netioConfig = netioConfig;
	}

	@XmlElement
	public void setNetioResumeMode(boolean netioResumeMode)
	{
		this.netioResumeMode = netioResumeMode;
	}

	public void setPersistConfig(InterfaceConfigurationBeanPersist persistConfig)
	{

		this.persistConfig = persistConfig;
	}

	@XmlElement
	public void setRapidRandomBrowse(boolean rapidRandomBrowse)
	{
		this.rapidRandomBrowse = rapidRandomBrowse;
	}

	@XmlElement
	public void setRunFileio(boolean runFileio)
	{
		this.runFileio = runFileio;
	}

	@XmlElement
	public void setRunNetio(boolean runNetio)
	{
		this.runNetio = runNetio;
	}

	@XmlElement
	public void setRunPersistence(boolean runPersistence)
	{
		this.runPersistence = runPersistence;
	}

	@XmlElement
	public void setRunPersistenceChores(boolean runPersistenceChores)
	{
		this.runPersistenceChores = runPersistenceChores;
	}

	@XmlElement
	public void setRunPersistenceReporting(boolean runPersistenceReporting)
	{
		this.runPersistenceReporting = runPersistenceReporting;
	}

	@XmlElement
	public void setRunSimpleStream(boolean runSimpleStream)
	{
		this.runSimpleStream = runSimpleStream;
	}

	@XmlElement
	public void setRunTextAnalysis(boolean runTextAnalysis)
	{
		this.runTextAnalysis = runTextAnalysis;
	}

	@XmlElement
	public void setRunTraining(boolean runTraining)
	{
		this.runTraining = runTraining;
	}

	@XmlElement
	public void setSpecifiedLanguage(String specifiedLanguage)
	{
		this.specifiedLanguage = specifiedLanguage;
	}

	public void setTaConfig(InterfaceConfigurationBeanTextAnalysis taConfig)
	{
		this.taConfig = taConfig;
	}
	
	
	
	@Override
	public String getElasticSearchUser() {
		return elasticSearchUser;
	}
	
	@XmlElement
	public void setElasticSearchUser(String elasticSearchUser) {
		this.elasticSearchUser = elasticSearchUser;
	}
	@Override
	public String getElasticSearchPassword() {
		return elasticSearchPassword;
	}
	
	@XmlElement
	public void setElasticSearchPassword(String elasticSearchUserPassword) {
		this.elasticSearchPassword = elasticSearchUserPassword;
	}
	@Override
	public String getElasticSearchUrl() {
		return elasticSearchUrl;
	}
	@XmlElement
	public void setElasticSearchUrl(String elasticSearchUserUrl) {
		this.elasticSearchUrl = elasticSearchUserUrl;
	}
	@Override
	public String getElasticSearchPort() {
		return elasticSearchPort;
	}
	@XmlElement
	public void setElasticSearchPort(String elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}
	@Override
	public boolean isSendToES() {
		return sendToES;
	}
	@XmlElement
	public void setSendToES(boolean sendToES) {
		this.sendToES = sendToES;
	}

	@Override
	public String toString()
	{
		return "ConfigurationBeanParent [applicationMainModuleName=" + applicationMainModuleName + "\n mlServerUrl="
				+ mlServerUrl + "\n mlServerPort=" + mlServerPort + "\n mlLocalStreamPorts=" + mlLocalStreamPorts
				+ "\n localUrls=" + localUrls + "\n analysisType=" + analysisType + "\n analysisSubType="
				+ analysisSubType + "\n keyspaceName=" + keyspaceName + "\n languagesForAnalysis="
				+ languagesForAnalysis + "\n specifiedLanguage=" + specifiedLanguage + "\n databaseDescriptor="
				+ databaseDescriptor + "\n netioConfig=" + netioConfig + "\n fioConfig=" + fioConfig + "\n taConfig="
				+ taConfig + "\n persistConfig=" + persistConfig + "\n runNetio=" + runNetio + "\n runFileio="
				+ runFileio + "\n runTextAnalysis=" + runTextAnalysis + "\n runPersistence=" + runPersistence
				+ "\n runPersistenceReporting=" + runPersistenceReporting + "\n runPersistenceChores="
				+ runPersistenceChores + "\n runTraining=" + runTraining + "\n elegantStop=" + elegantStop
				+ "\n netioResumeMode=" + netioResumeMode + "\n minimizeResourceUse=" + minimizeResourceUse
				+ "\n rapidRandomBrowse=" + rapidRandomBrowse + "\n childrenLoaded=" + childrenLoaded
				+ "\n mlServerUuids=" + configProperties + "\nelasticSearchUrl="+ elasticSearchUrl+"]";
	}

}
