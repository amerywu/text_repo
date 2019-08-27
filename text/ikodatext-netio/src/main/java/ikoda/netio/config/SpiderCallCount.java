package ikoda.netio.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "name", "spiderId", "callCount", "failCount", "maxRounds" })
@XmlRootElement(name = "urlsrc")
public class SpiderCallCount
{

	private static int idCounter = 0;
	private Integer spiderId;
	private Integer callCount = 0;
	private Integer failCount = 0;

	private String name = "Unnamed";
	private Integer maxRounds = 0;

	public Integer getCallCount()
	{
		return callCount;
	}

	public Integer getFailCount()
	{
		return failCount;
	}

	public Integer getMaxRounds()
	{
		return maxRounds;
	}

	public String getName()
	{
		return name;
	}

	public Integer getSpiderId()
	{
		return spiderId;
	}

	@XmlElement
	public void setCallCount(Integer callCount)
	{
		this.callCount = callCount;
	}

	@XmlElement
	public void setFailCount(Integer failCount)
	{
		this.failCount = failCount;
	}

	@XmlElement
	public void setMaxRounds(Integer maxRounds)
	{
		this.maxRounds = maxRounds;
	}

	@XmlElement
	public void setName(String name)
	{
		this.name = name;
	}

	@XmlElement
	public void setSpiderId(Integer spiderId)
	{
		this.spiderId = spiderId;
	}

}
