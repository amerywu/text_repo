package ikoda.netio.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "javaCallingClass", "openCpuUrl", "openCpuScheme", "openCpuRootPath", "openCpuApp", "openCpuFunction", "openCpuPort",
		"openCpuArg1", "openCpuArg2", "openCpuArg3", "openCpuArg4", "openCpuArg5", "openCpuArg6", "openCpuArg7",
		"openCpuArg8" })
@XmlRootElement(name = "openCpuCall")
public class OpenCpuCall
{

	private String javaCallingClass;
	private String openCpuUrl;
	private String openCpuScheme;
	private String openCpuRootPath;
	private String openCpuApp;
	private String openCpuFunction;
	private int openCpuPort;
	private String openCpuArg1;
	private String openCpuArg2;
	private String openCpuArg3;
	private String openCpuArg4;
	private String openCpuArg5;
	private String openCpuArg6;
	private String openCpuArg7;
	private String openCpuArg8;

	public String getJavaCallingClass()
	{
		return javaCallingClass;
	}

	public String getOpenCpuApp()
	{
		return openCpuApp;
	}

	public String getOpenCpuArg1()
	{
		return openCpuArg1;
	}

	public String getOpenCpuArg2()
	{
		return openCpuArg2;
	}

	public String getOpenCpuArg3()
	{
		return openCpuArg3;
	}

	public String getOpenCpuArg4()
	{
		return openCpuArg4;
	}

	public String getOpenCpuArg5()
	{
		return openCpuArg5;
	}

	public String getOpenCpuArg6()
	{
		return openCpuArg6;
	}

	public String getOpenCpuArg7()
	{
		return openCpuArg7;
	}

	public String getOpenCpuArg8()
	{
		return openCpuArg8;
	}

	public String getOpenCpuFunction()
	{
		return openCpuFunction;
	}

	public int getOpenCpuPort()
	{
		return openCpuPort;
	}

	public String getOpenCpuRootPath()
	{
		return openCpuRootPath;
	}

	public String getOpenCpuScheme()
	{
		return openCpuScheme;
	}

	public String getOpenCpuUrl()
	{
		return openCpuUrl;
	}

	@XmlElement
	public void setJavaCallingClass(String javaCallingClass)
	{
		this.javaCallingClass = javaCallingClass;
	}

	@XmlElement
	public void setOpenCpuApp(String openCpuApp)
	{
		this.openCpuApp = openCpuApp;
	}

	@XmlElement
	public void setOpenCpuArg1(String openCpuArg1)
	{
		this.openCpuArg1 = openCpuArg1;
	}

	@XmlElement
	public void setOpenCpuArg2(String openCpuArg2)
	{
		this.openCpuArg2 = openCpuArg2;
	}

	@XmlElement
	public void setOpenCpuArg3(String openCpuArg3)
	{
		this.openCpuArg3 = openCpuArg3;
	}

	@XmlElement
	public void setOpenCpuArg4(String openCpuArg4)
	{
		this.openCpuArg4 = openCpuArg4;
	}

	@XmlElement
	public void setOpenCpuArg5(String openCpuArg5)
	{
		this.openCpuArg5 = openCpuArg5;
	}

	@XmlElement
	public void setOpenCpuArg6(String openCpuArg6)
	{
		this.openCpuArg6 = openCpuArg6;
	}

	@XmlElement
	public void setOpenCpuArg7(String openCpuArg7)
	{
		this.openCpuArg7 = openCpuArg7;
	}

	@XmlElement
	public void setOpenCpuArg8(String openCpuArg8)
	{
		this.openCpuArg8 = openCpuArg8;
	}

	@XmlElement
	public void setOpenCpuFunction(String openCpuFunction)
	{
		this.openCpuFunction = openCpuFunction;
	}

	@XmlElement
	public void setOpenCpuPort(int openCpuPort)
	{
		this.openCpuPort = openCpuPort;
	}

	@XmlElement
	public void setOpenCpuRootPath(String openCpuRootPath)
	{
		this.openCpuRootPath = openCpuRootPath;
	}

	@XmlElement
	public void setOpenCpuScheme(String openCpuScheme)
	{
		this.openCpuScheme = openCpuScheme;
	}

	@XmlElement
	public void setOpenCpuUrl(String openCpuUrl)
	{
		this.openCpuUrl = openCpuUrl;
	}

}
