package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "csvPath", "maxNumberOfDocuments", "csvForITokenNames", "columnsToExcludeFromLibSvm", "targetColumnName",
		"textAnalysisDonePath", "textAnalysisFailedPath", "maxFileLength", "minFileLength", "collectPartOfSpeech",
		"convertToLemmas", "collectBySentence", "projectPrefix", "taThreadCount", "preventSentenceDuplicates",
		"configProperties" })
@XmlRootElement(name = "configurationTextAnalysis")
public class ConfigurationBeanForTextAnalysis_Reddit
		implements InterfaceConfigurationBeanTextAnalysis, InterfaceBindingConfig
{

	private String csvPath;
	private List<String> csvForITokenNames = new ArrayList<String>();
	private List<String> columnsToExcludeFromLibSvm = new ArrayList<String>();
	private String textAnalysisDonePath;
	private String textAnalysisFailedPath;
	private int maxNumberOfDocuments;
	private boolean collectPartOfSpeech;
	private boolean convertToLemmas;
	private boolean preventSentenceDuplicates;
	private int maxFileLength = 500000;
	private int minFileLength = 2000;
	private String projectPrefix = "";
	private int taThreadCount = 1;
	private boolean collectBySentence;
	private String targetColumnName;
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

	public int getMaxFileLength()
	{
		return maxFileLength;
	}

	public int getMaxNumberOfDocuments()
	{
		return maxNumberOfDocuments;
	}

	public int getMinFileLength()
	{
		return minFileLength;
	}

	@Override
	public String getProjectPrefix()
	{
		return projectPrefix;
	}

	@Override
	@XmlElement
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

	@Override
	public boolean isCollectBySentence()
	{
		return collectBySentence;
	}

	public boolean isCollectPartOfSpeech()
	{
		return collectPartOfSpeech;
	}

	public boolean isConvertToLemmas()
	{
		return convertToLemmas;
	}

	public boolean isPreventSentenceDuplicates()
	{
		return preventSentenceDuplicates;
	}

	@XmlElement
	public void setCollectBySentence(boolean collectBySentence)
	{
		this.collectBySentence = collectBySentence;
	}

	@XmlElement
	public void setCollectPartOfSpeech(boolean collectPartOfSpeech)
	{
		this.collectPartOfSpeech = collectPartOfSpeech;
	}

	@XmlElement
	public void setColumnsToExcludeFromLibSvm(List<String> columnsToExcludeFromLibSvm)
	{
		this.columnsToExcludeFromLibSvm = columnsToExcludeFromLibSvm;
	}

	@XmlElement
	public void setConvertToLemmas(boolean convertToLemmas)
	{
		this.convertToLemmas = convertToLemmas;
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
	public void setMaxFileLength(int maxFileLength)
	{
		this.maxFileLength = maxFileLength;
	}

	@XmlElement
	public void setMaxNumberOfDocuments(int maxNumberOfCsvRows)
	{
		this.maxNumberOfDocuments = maxNumberOfCsvRows;
	}

	@XmlElement
	public void setMinFileLength(int minFileLength)
	{
		this.minFileLength = minFileLength;
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

	@XmlElement
	public void setPreventSentenceDuplicates(boolean preventSentenceDuplicates)
	{
		this.preventSentenceDuplicates = preventSentenceDuplicates;
	}

	@XmlElement
	public void setProjectPrefix(String outputFilePrefix)
	{
		this.projectPrefix = outputFilePrefix;
	}

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
	public String toString()
	{
		return "ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis [csvPath=" + csvPath + ", csvForITokenNames="
				+ csvForITokenNames + ", textAnalysisDonePath=" + textAnalysisDonePath + ", textAnalysisFailedPath="
				+ textAnalysisFailedPath + "]";
	}

}
