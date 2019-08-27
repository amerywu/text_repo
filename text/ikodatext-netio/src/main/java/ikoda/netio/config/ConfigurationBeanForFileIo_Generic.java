package ikoda.netio.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "fileIoJobPostingPath", "fileIoDonePath", "fileIoXMLPath", "fileIoResultsPath", "fileIoUndeterminedPath",
		"fileIoNonApplicablePath", "fileIoOutBoxPath" })

@XmlRootElement(name = "configurationFileio")
public class ConfigurationBeanForFileIo_Generic implements InterfaceConfigurationBeanFileio, InterfaceBindingConfig
{

	private String fileIoJobPostingPath;
	private String fileIoDonePath;
	private String fileIoXMLPath;
	private String fileIoResultsPath;
	private String fileIoUndeterminedPath;
	private String fileIoNonApplicablePath;
	private String fileIoOutBoxPath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.netio.config.InterfaceConfigurationBeanFileio#getFileIoDonePath()
	 */
	@Override
	public String getFileIoDonePath()
	{
		return fileIoDonePath;
	}

	public String getFileIoJobPostingPath()
	{
		return fileIoJobPostingPath;
	}

	public String getFileIoNonApplicablePath()
	{
		return fileIoNonApplicablePath;
	}

	@Override
	public String getFileIoOutBoxPath()
	{
		return fileIoOutBoxPath;
	}

	public String getFileIoResultsPath()
	{
		return fileIoResultsPath;
	}

	public String getFileIoUndeterminedPath()
	{
		return fileIoUndeterminedPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ikoda.netio.config.InterfaceConfigurationBeanFileio#getFileIoXMLPath()
	 */
	@Override
	public String getFileIoXMLPath()
	{
		return fileIoXMLPath;
	}

	@XmlElement
	public void setFileIoDonePath(String fileIoDonePath)
	{
		this.fileIoDonePath = fileIoDonePath;
	}

	@XmlElement
	public void setFileIoJobPostingPath(String fileIoJobPostingPath)
	{
		this.fileIoJobPostingPath = fileIoJobPostingPath;
	}

	@XmlElement
	public void setFileIoNonApplicablePath(String fileIoNonApplicablePath)
	{
		this.fileIoNonApplicablePath = fileIoNonApplicablePath;
	}

	@XmlElement
	public void setFileIoOutBoxPath(String fileIoOutBoxPath)
	{
		this.fileIoOutBoxPath = fileIoOutBoxPath;
	}

	@XmlElement
	public void setFileIoResultsPath(String fileIoResultsPath)
	{
		this.fileIoResultsPath = fileIoResultsPath;
	}

	@XmlElement
	public void setFileIoUndeterminedPath(String fileIoUndeterminedPath)
	{
		this.fileIoUndeterminedPath = fileIoUndeterminedPath;
	}

	@XmlElement
	public void setFileIoXMLPath(String fileIoXMLPath)
	{
		this.fileIoXMLPath = fileIoXMLPath;
	}

	@Override
	public String toString()
	{
		return "ConfigurationBeanForFileIoGeneric [fileIoJobPostingPath=" + fileIoJobPostingPath + ", fileIoDonePath="
				+ fileIoDonePath + ", fileIoXMLPath=" + fileIoXMLPath + ", fileIoResultsPath=" + fileIoResultsPath
				+ ", fileIoUndeterminedPath=" + fileIoUndeterminedPath + ", fileIoNonApplicablePath="
				+ fileIoNonApplicablePath + "]";
	}

}
