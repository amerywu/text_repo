package ikoda.netio.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "biteSize", "minDetailLevel", "percentToSample", "persistenceDonePath", "persistenceFailedPath" })
@XmlRootElement(name = "configurationPersistence")
public class ConfigurationBeanForPersistence_JobDescriptionAnalysis
		implements InterfaceConfigurationBeanPersist, InterfaceBindingConfig
{

	private int biteSize;
	private int minDetailLevel;
	private int percentToSample;
	private String persistenceDonePath;
	private String persistenceFailedPath;

	public int getBiteSize()
	{
		return biteSize;
	}

	public int getMinDetailLevel()
	{
		return minDetailLevel;
	}

	public int getPercentToSample()
	{
		return percentToSample;
	}

	@Override
	public String getPersistenceDonePath()
	{
		return persistenceDonePath;
	}

	@Override
	public String getPersistenceFailedPath()
	{
		return persistenceFailedPath;
	}

	@XmlElement
	public void setBiteSize(int biteSize)
	{
		this.biteSize = biteSize;
	}

	@XmlElement
	public void setMinDetailLevel(int minDetailLevel)
	{
		this.minDetailLevel = minDetailLevel;
	}

	@XmlElement
	public void setPercentToSample(int percentToSample)
	{
		this.percentToSample = percentToSample;
	}

	@XmlElement
	public void setPersistenceDonePath(String persistenceDonePath)
	{
		this.persistenceDonePath = persistenceDonePath;
	}

	@XmlElement
	public void setPersistenceFailedPath(String persistenceFailedPath)
	{
		this.persistenceFailedPath = persistenceFailedPath;
	}

	@Override
	public String toString()
	{
		return "ConfigurationBeanForPersistence_JobDescriptionAnalysis [biteSize=" + biteSize + ", minDetailLevel="
				+ minDetailLevel + ", percentToSample=" + percentToSample + ", persistenceDonePath="
				+ persistenceDonePath + ", persistenceFailedPath=" + persistenceFailedPath + "]";
	}

}
