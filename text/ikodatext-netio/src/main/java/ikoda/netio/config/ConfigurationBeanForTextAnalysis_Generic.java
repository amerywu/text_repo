package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "nerPropertiesPath", "textAnalysisInfoBoxPath", "targetColumnName", "textAnalysisDonePath", "textAnalysisFailedPath",
		"taThreadCount", "projectPrefix", "collectBySentence", "columnsToExcludeFromLibSvm", "configProperties" })

@XmlRootElement(name = "configurationTextAnalysis")
public class ConfigurationBeanForTextAnalysis_Generic
		implements InterfaceConfigurationBeanTextAnalysis, InterfaceBindingConfig
{

	private String nerPropertiesPath;
	private String textAnalysisInfoBoxPath;
	private String textAnalysisDonePath;
	private String textAnalysisFailedPath;
	private String projectPrefix = "";
	private int taThreadCount;
	private boolean collectBySentence;
	private String targetColumnName;
	private List<String> columnsToExcludeFromLibSvm = new ArrayList<String>();
	private XMLPropertyMap configProperties = new XMLPropertyMap();

	public String getNerPropertiesPath()
	{
		return nerPropertiesPath;
	}

	@Override
	public String getProjectPrefix()
	{
		return projectPrefix;
	}

	@Override
	public int getTaThreadCount()
	{
		return taThreadCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.netio.config.InterfaceConfigurationBeanTextAnalysis#
	 * getTextAnalysisDonePath()
	 */
	@Override
	public String getTextAnalysisDonePath()
	{
		return textAnalysisDonePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.netio.config.InterfaceConfigurationBeanTextAnalysis#
	 * getTextAnalysisFailedPath()
	 */
	@Override
	public String getTextAnalysisFailedPath()
	{
		return textAnalysisFailedPath;
	}

	public String getTextAnalysisInfoBoxPath()
	{
		return textAnalysisInfoBoxPath;
	}

	@XmlElement
	public void setNerPropertiesPath(String nerPropertiesPath)
	{
		this.nerPropertiesPath = nerPropertiesPath;
	}

	@XmlElement
	public void setProjectPrefix(String outputFilePrefix)
	{
		this.projectPrefix = outputFilePrefix;
	}

	@XmlElement
	public void setTaThreadCount(int taThreadCount)
	{
		this.taThreadCount = taThreadCount;
	}

	public void setTextAnalysisDonePath(String textAnalysisDonePath)
	{
		this.textAnalysisDonePath = textAnalysisDonePath;
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

	@XmlElement
	public void setTextAnalysisFailedPath(String textAnalysisFailedPath)
	{
		this.textAnalysisFailedPath = textAnalysisFailedPath;
	}

	@Override
	public String getTargetColumnName()
	{
		return targetColumnName;
	}

	@XmlElement
	public void setTargetColumnName(String targetColumnName)
	{
		this.targetColumnName = targetColumnName;
	}

	@XmlElement
	public void setTextAnalysisInfoBoxPath(String textAnalysisInfoBoxPath)
	{
		this.textAnalysisInfoBoxPath = textAnalysisInfoBoxPath;
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
	public List<String> getColumnsToExcludeFromLibSvm()
	{
		return columnsToExcludeFromLibSvm;
	}

	@XmlElement
	public void setColumnsToExcludeFromLibSvm(List<String> columnsToExcludeFromLibSvm)
	{
		this.columnsToExcludeFromLibSvm = columnsToExcludeFromLibSvm;
	}

}
