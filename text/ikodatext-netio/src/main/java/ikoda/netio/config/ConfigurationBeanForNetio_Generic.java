package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "urlRepository", "netIoDumpPath", "netIoAccumulatorPath", "maxCallsRoundOne", "maxCallsRoundTwo",
		"maxCallsRoundThree", "maxCallsRoundFour", "netioMinimumCycleInMinutes", "netioRunInCycle",
		"linksForUniversalIgnore" })

@XmlRootElement(name = "configurationNetio")
public class ConfigurationBeanForNetio_Generic implements InterfaceConfigurationBeanNetio, InterfaceBindingConfig
{

	private String urlRepository;
	private String netIoDumpPath;
	private String netIoAccumulatorPath;

	private int maxCallsRoundOne;
	private int maxCallsRoundTwo;
	private int maxCallsRoundThree;
	private int maxCallsRoundFour;

	private int netioMinimumCycleInMinutes;
	private boolean netioRunInCycle;
	private List<String> linksForUniversalIgnore = new ArrayList<String>();

	public int getMaxCallsRoundFour()
	{
		return maxCallsRoundFour;
	}

	public int getMaxCallsRoundOne()
	{
		return maxCallsRoundOne;
	}

	public int getMaxCallsRoundThree()
	{
		return maxCallsRoundThree;
	}

	@Override
	public List<String> getLinksForUniversalIgnore()
	{
		return linksForUniversalIgnore;
	}

	@XmlElement
	public void setLinksForUniversalIgnore(List<String> linksForUniversalIgnore)
	{
		this.linksForUniversalIgnore = linksForUniversalIgnore;
	}

	public int getMaxCallsRoundTwo()
	{
		return maxCallsRoundTwo;
	}

	@Override
	public String getNetIoAccumulatorPath()
	{
		return netIoAccumulatorPath;
	}

	@Override
	public String getNetIoDumpPath()
	{
		return netIoDumpPath;
	}

	@Override
	public int getNetioMinimumCycleInMinutes()
	{
		return netioMinimumCycleInMinutes;
	}

	@Override
	public String getUrlRepository()
	{
		return urlRepository;
	}

	@Override
	public boolean isNetioRunInCycle()
	{
		return netioRunInCycle;
	}

	@XmlElement
	public void setMaxCallsRoundFour(int maxCallsRoundFour)
	{
		this.maxCallsRoundFour = maxCallsRoundFour;
	}

	@XmlElement
	public void setMaxCallsRoundOne(int maxCallsRoundOne)
	{
		this.maxCallsRoundOne = maxCallsRoundOne;
	}

	@XmlElement
	public void setMaxCallsRoundThree(int maxCallsRoundThree)
	{
		this.maxCallsRoundThree = maxCallsRoundThree;
	}

	@XmlElement
	public void setMaxCallsRoundTwo(int maxCallsRoundTwo)
	{
		this.maxCallsRoundTwo = maxCallsRoundTwo;
	}

	@XmlElement
	public void setNetIoAccumulatorPath(String netIoAccumulatorPath)
	{
		this.netIoAccumulatorPath = netIoAccumulatorPath;
	}

	@XmlElement
	public void setNetIoDumpPath(String netIoDumpPath)
	{
		this.netIoDumpPath = netIoDumpPath;
	}

	@XmlElement
	public void setNetioMinimumCycleInMinutes(int netioMinimumCycleInMinutes)
	{
		this.netioMinimumCycleInMinutes = netioMinimumCycleInMinutes;
	}

	@XmlElement
	public void setNetioRunInCycle(boolean netioRunInCycle)
	{
		this.netioRunInCycle = netioRunInCycle;
	}

	@XmlElement
	public void setUrlRepository(String urlRepository)
	{
		this.urlRepository = urlRepository;
	}

	@Override
	public String toString()
	{
		return "ConfigurationBeanNetioForJobAnalysis [urlRepository=" + urlRepository + ", netIoDumpPath="
				+ netIoDumpPath + ", maxCallsRoundOne=" + maxCallsRoundOne + ", maxCallsRoundTwo=" + maxCallsRoundTwo
				+ ", maxCallsRoundThree=" + maxCallsRoundThree + ", maxCallsRoundFour=" + maxCallsRoundFour
				+ ", netioMinimumCycleInMinutes=" + netioMinimumCycleInMinutes + "]";
	}

}
