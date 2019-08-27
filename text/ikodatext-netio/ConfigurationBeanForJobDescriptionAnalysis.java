package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "analysisType", "analysisSubType", "databaseDescriptor", "specifiedLanguage", "languagesForAnalysis", "openCpuCalls",
		"biteSize", "minDetailLevel", "percentToSample", "csvPath", "csvForITokenNames", "runNetio", "runFileio",
		"runTextAnalysis", "runPersistence", "runPersistenceChores", "elegantStop", "netioResumeMode", "maxCsvRows" })
@XmlRootElement(name = "configuration")
public class ConfigurationBeanForJobDescriptionAnalysis implements InterfaceConfigurationBean , InterfaceBindingConfig
{
	private String databaseDescriptor;
	private String analysisType;
	private String analysisSubType;

	private int biteSize;
	private int minDetailLevel;
	private int percentToSample;
	private String csvPath;
	private List<String> csvForITokenNames = new ArrayList<String>();
	private List<LanguageForAnalysis> languagesForAnalysis = new ArrayList<LanguageForAnalysis>();;
	private int maxCsvRows;

	private List<OpenCpuCall> openCpuCalls = new ArrayList<OpenCpuCall>();

	private boolean runNetio;
	private boolean runFileio;
	private boolean runTextAnalysis;
	private boolean runPersistence;
	private boolean runPersistenceChores;
	private boolean netioResumeMode;
	private boolean elegantStop;
	private String specifiedLanguage;

	public String getAnalysisSubType()
	{
		return analysisSubType;
	}

	public String getAnalysisType()
	{
		return analysisType;
	}

	public int getBiteSize()
	{
		return biteSize;
	}

	public List<String> getCsvForITokenNames()
	{
		return csvForITokenNames;
	}

	public String getCsvPath()
	{
		return csvPath;
	}

	public String getDatabaseDescriptor()
	{
		return databaseDescriptor;
	}

	@Override
	public String getFileIoDonePath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<LanguageForAnalysis> getLanguagesForAnalysis()
	{
		return languagesForAnalysis;
	}

	@Override
	public int getMaxCallsRoundFour()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxCallsRoundOne()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxCallsRoundThree()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxCallsRoundTwo()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxCsvRows()
	{
		return maxCsvRows;
	}

	public int getMinDetailLevel()
	{
		return minDetailLevel;
	}

	@Override
	public String getNetIoDumpPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNetioMinimumCycleInMinutes()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public List<OpenCpuCall> getOpenCpuCalls()
	{
		return openCpuCalls;
	}

	public int getPercentToSample()
	{
		return percentToSample;
	}

	public String getSpecifiedLanguage()
	{
		return specifiedLanguage;
	}

	@Override
	public String getUrlRepository()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isElegantStop()
	{
		return elegantStop;
	}

	@Override
	public boolean isMinimizeResourceUse()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNetioResumeMode()
	{
		return netioResumeMode;
	}

	public boolean isRunFileio()
	{
		return runFileio;
	}

	public boolean isRunNetio()
	{
		return runNetio;
	}

	public boolean isRunPersistence()
	{
		return runPersistence;
	}

	public boolean isRunPersistenceChores()
	{
		return runPersistenceChores;
	}

	public boolean isRunTextAnalysis()
	{
		return runTextAnalysis;
	}

	public List<String> retrieveRegionsForCurrentLocale()
	{
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
	public void setBiteSize(int biteSize)
	{
		this.biteSize = biteSize;
	}

	@XmlElement
	public void setCsvForITokenNames(List<String> csvFileNames)
	{
		this.csvForITokenNames = csvFileNames;
	}

	@XmlElement
	public void setCsvPath(String csvPath)
	{
		this.csvPath = csvPath;
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

	@XmlElement
	public void setLanguagesForAnalysis(List<LanguageForAnalysis> languagesForAnalysis)
	{
		this.languagesForAnalysis = languagesForAnalysis;
	}

	@XmlElement
	public void setMaxCsvRows(int maxCsvRows)
	{
		this.maxCsvRows = maxCsvRows;
	}

	@XmlElement
	public void setMinDetailLevel(int minDetailLevel)
	{
		this.minDetailLevel = minDetailLevel;
	}

	@XmlElement
	public void setNetioResumeMode(boolean netioResumeMode)
	{
		this.netioResumeMode = netioResumeMode;
	}

	@XmlElement
	public void setOpenCpuCalls(List<OpenCpuCall> openCpuCalls)
	{
		this.openCpuCalls = openCpuCalls;
	}

	@XmlElement
	public void setPercentToSample(int percentToSample)
	{
		this.percentToSample = percentToSample;
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
	public void setRunTextAnalysis(boolean runTextAnalysis)
	{
		this.runTextAnalysis = runTextAnalysis;
	}

	public void setSpecifiedLanguage(String specifiedLanguage)
	{
		this.specifiedLanguage = specifiedLanguage;
	}

}
