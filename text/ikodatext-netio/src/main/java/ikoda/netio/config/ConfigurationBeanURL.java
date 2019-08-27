package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "url", "website", "firstRoundLinks", "secondRoundLinks", "firstRoundIgnore", "secondRoundIgnore", "thirdRoundLinks",
		"thirdRoundIgnore", "propertyMap" })
@XmlRootElement(name = "sourceURL")
public class ConfigurationBeanURL
{

	private String url;
	private String website;
	private List<String> firstRoundLinks = new ArrayList<>();
	private List<String> secondRoundLinks = new ArrayList<>();
	private List<String> firstRoundIgnore = new ArrayList<>();
	private List<String> secondRoundIgnore = new ArrayList<>();
	private List<String> thirdRoundLinks = new ArrayList<>();
	private List<String> thirdRoundIgnore = new ArrayList<>();
	private XMLPropertyMap propertyMap = new XMLPropertyMap();

	public List<String> getFirstRoundIgnore()
	{
		return firstRoundIgnore;
	}

	public List<String> getFirstRoundLinks()

	{
		return firstRoundLinks;
	}

	public XMLPropertyMap getPropertyMap()
	{
		return propertyMap;
	}

	public Integer getRoundCount()
	{
		if (thirdRoundLinks.size() > 0)
		{
			return new Integer(3);
		}
		else if (secondRoundLinks.size() > 0)
		{
			return new Integer(2);
		}
		else if (firstRoundLinks.size() > 0)
		{
			return new Integer(1);
		}
		return new Integer(4);
	}

	public List<String> getSecondRoundIgnore()
	{
		return secondRoundIgnore;
	}

	public List<String> getSecondRoundLinks()
	{
		return secondRoundLinks;
	}

	public List<String> getThirdRoundIgnore()
	{
		return thirdRoundIgnore;
	}

	public List<String> getThirdRoundLinks()
	{
		return thirdRoundLinks;
	}

	public String getUrl()
	{
		return url;
	}

	public String getWebsite()
	{
		return website;
	}

	public void setFirstRoundIgnore(List<String> firstRoundIgnore)
	{
		this.firstRoundIgnore = firstRoundIgnore;
	}

	@XmlElement
	public void setFirstRoundLinks(List<String> firstRoundLinks)
	{
		this.firstRoundLinks = firstRoundLinks;
	}

	@XmlElement
	public void setPropertyMap(XMLPropertyMap propertyMap)
	{
		this.propertyMap = propertyMap;
	}

	public void setSecondRoundIgnore(List<String> secondRoundIgnore)
	{
		this.secondRoundIgnore = secondRoundIgnore;
	}

	@XmlElement
	public void setSecondRoundLinks(List<String> secondRoundLinks)
	{
		this.secondRoundLinks = secondRoundLinks;
	}

	@XmlElement
	public void setThirdRoundIgnore(List<String> thirdRoundIgnore)
	{
		this.thirdRoundIgnore = thirdRoundIgnore;
	}

	@XmlElement
	public void setThirdRoundLinks(List<String> thirdRoundLinks)
	{
		this.thirdRoundLinks = thirdRoundLinks;
	}

	@XmlElement
	public void setUrl(String url)
	{
		this.url = url;
	}

	@XmlElement
	public void setWebsite(String website)
	{
		this.website = website;
	}

}
