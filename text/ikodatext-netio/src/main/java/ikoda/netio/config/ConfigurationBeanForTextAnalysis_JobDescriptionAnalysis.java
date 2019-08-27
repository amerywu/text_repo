package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "csvPath", "maxNumberOfDocuments", "csvForITokenNames", "columnsToExcludeFromLibSvm", "textAnalysisDonePath",
		"textAnalysisFailedPath", "rollingRowCount", "targetColumnName", "projectPrefix", "taThreadCount",
		"collectBySentence", "configProperties" })
@XmlRootElement(name = "configurationTextAnalysis")
public class ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis
		implements InterfaceConfigurationBeanTextAnalysis, InterfaceBindingConfig
{

	private String csvPath;
	private List<String> csvForITokenNames = new ArrayList<String>();
	private List<String> columnsToExcludeFromLibSvm = new ArrayList<String>();
	private String textAnalysisDonePath;
	private String textAnalysisFailedPath;
	private int maxNumberOfDocuments;
	private int rollingRowCount = 1000;
	private String targetColumnName;
	private String projectPrefix = "";
	private int taThreadCount = 1;
	private boolean collectBySentence = false;
	private XMLPropertyMap configProperties = new XMLPropertyMap();

	@Override
	public List<String> getColumnsToExcludeFromLibSvm()
	{
		return columnsToExcludeFromLibSvm;
	}

	public List<String> getCsvForITokenNames()
	{
		return csvForITokenNames;
	}

	public String getCsvPath()
	{
		return csvPath;
	}

	public int getMaxNumberOfDocuments()
	{
		return maxNumberOfDocuments;
	}

	@Override
	public String getProjectPrefix()
	{
		return projectPrefix;
	}

	public int getRollingRowCount()
	{
		return rollingRowCount;
	}

	@Override
	public String getTargetColumnName()
	{
		return targetColumnName;
	}

	@Override
	public int getTaThreadCount()
	{
		return taThreadCount;
	}

	@Override
	public String getTextAnalysisDonePath()
	{
		return textAnalysisDonePath;
	}

	@Override
	public String getTextAnalysisFailedPath()
	{
		return textAnalysisFailedPath;
	}

	@XmlElement
	public void setColumnsToExcludeFromLibSvm(List<String> columnsToExcludeFromLibSvm)
	{
		this.columnsToExcludeFromLibSvm = columnsToExcludeFromLibSvm;
	}

	@XmlElement
	public void setCsvForITokenNames(List<String> csvForITokenNames)
	{
		this.csvForITokenNames = csvForITokenNames;
	}

	@XmlElement
	public void setCsvPath(String csvPath)
	{
		this.csvPath = csvPath;
	}

	@XmlElement
	public void setMaxNumberOfDocuments(int maxNumberOfCsvRows)
	{
		this.maxNumberOfDocuments = maxNumberOfCsvRows;
	}

	public void setProjectPrefix(String outputFilePrefix)
	{
		this.projectPrefix = outputFilePrefix;
	}

	@XmlElement
	public void setRollingRowCount(int rollingRowCount)
	{
		this.rollingRowCount = rollingRowCount;
	}

	@XmlElement
	public void setTargetColumnName(String targetColumnName)
	{
		this.targetColumnName = targetColumnName;
	}

	@XmlElement
	public void setTaThreadCount(int taThreadCount)
	{
		this.taThreadCount = taThreadCount;
	}

	@XmlElement
	public void setTextAnalysisDonePath(String textAnalysisDonePath)
	{
		this.textAnalysisDonePath = textAnalysisDonePath;
	}

	@XmlElement
	public void setTextAnalysisFailedPath(String textAnalysisFailedPath)
	{
		this.textAnalysisFailedPath = textAnalysisFailedPath;
	}

	@Override
	public boolean isCollectBySentence()
	{
		return collectBySentence;
	}

	@XmlElement
	public void setCollectBySentence(boolean collectBySentence)
	{
		this.collectBySentence = collectBySentence;
	}

	@Override
	public XMLPropertyMap getConfigProperties()
	{
		return configProperties;
	}

	@XmlElement
	public void setConfigProperties(XMLPropertyMap configProperties)
	{
		this.configProperties = configProperties;
	}

	@Override
	public String toString()
	{
		return "ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis [csvPath=" + csvPath + ", csvForITokenNames="
				+ csvForITokenNames + ", textAnalysisDonePath=" + textAnalysisDonePath + ", textAnalysisFailedPath="
				+ textAnalysisFailedPath + "]";
	}

}
