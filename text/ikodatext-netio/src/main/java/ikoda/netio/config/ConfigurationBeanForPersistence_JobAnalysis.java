package ikoda.netio.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "persistenceDonePath", "persistenceFailedPath", "persistIfCountDetailLevel", "qualificationMap",
		"persistIfCountAreasOfStudy", "persistIfCountCertification", "persistIfValueMinimumSalary" })

@XmlRootElement(name = "configurationPersistence")
public class ConfigurationBeanForPersistence_JobAnalysis
		implements InterfaceConfigurationBeanPersist, InterfaceBindingConfig
{

	private String persistenceDonePath;
	private String persistenceFailedPath;
	private int persistIfCountDetailLevel = 0;
	private int persistIfCountAreasOfStudy = 0;
	private int persistIfCountCertification = 0;
	private int persistIfValueMinimumSalary = 0;
	private String qualificationMap;

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

	public int getPersistIfCountAreasOfStudy()
	{
		return persistIfCountAreasOfStudy;
	}

	public int getPersistIfCountCertification()
	{
		return persistIfCountCertification;
	}

	public int getPersistIfCountDetailLevel()
	{
		return persistIfCountDetailLevel;
	}

	public int getPersistIfValueMinimumSalary()
	{
		return persistIfValueMinimumSalary;
	}

	public String getQualificationMap()
	{
		return qualificationMap;
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

	@XmlElement
	public void setPersistIfCountAreasOfStudy(int persistIfCountAreasOfStudy)
	{
		this.persistIfCountAreasOfStudy = persistIfCountAreasOfStudy;
	}

	@XmlElement
	public void setPersistIfCountCertification(int persistIfCountCertification)
	{
		this.persistIfCountCertification = persistIfCountCertification;
	}

	@XmlElement
	public void setPersistIfCountDetailLevel(int persistIfCountDetailLevel)
	{
		this.persistIfCountDetailLevel = persistIfCountDetailLevel;
	}

	@XmlElement
	public void setPersistIfValueMinimumSalary(int persistIfValueMinimumSalary)
	{
		this.persistIfValueMinimumSalary = persistIfValueMinimumSalary;
	}

	@XmlElement
	public void setQualificationMap(String qualificationMap)
	{
		this.qualificationMap = qualificationMap;
	}

	@Override
	public String toString()
	{
		return "ConfigurationBeanForPersistenceJobAnalysis [persistenceDonePath=" + persistenceDonePath
				+ ", persistenceFailedPath=" + persistenceFailedPath + ", persistIfCountDetailLevel="
				+ persistIfCountDetailLevel + ", persistIfCountAreasOfStudy=" + persistIfCountAreasOfStudy
				+ ", persistIfCountCertification=" + persistIfCountCertification + ", persistIfValueMinimumSalary="
				+ persistIfValueMinimumSalary + "]";
	}

}
