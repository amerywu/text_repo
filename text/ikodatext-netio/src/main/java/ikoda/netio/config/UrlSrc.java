package ikoda.netio.config;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ikoda.netio.NioLog;

@XmlType(propOrder =
{ "url", "originUrl", "webSiteName", "urlRepository", "spiderId", "promulgatedData", "firstRoundLinks",
		"secondRoundLinks", "firstRoundIgnore", "secondRoundIgnore", "thirdRoundLinks", "thirdRoundIgnore",
		"propertyMap" })
@XmlRootElement(name = "urlsrc")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class UrlSrc
{

	public static final String _ALLOW_DUPLICATES_ANYWHERE = "_ALLOW_DUPLICATES_ANYWHERE";
	public static final String _ALLOW_DUPLICATES_INSIDE_BRANCH = "_ALLOW_DUPLICATES_INSIDE_BRANCH";
	public static final String _REQUIRE_ALL_LINK_CRITERIA = "_REQUIRE_ALL_LINK_CRITERIA";
	public static final String _URI_PREFIX = "_URI_PREFIX";
	public static final String _URI_OMMIT = "_URI_OMMIT";
	public static final String _AS_RESTFUL = "_AS_RESTFUL";

	private String url;
	private String originUrl;
	private String webSiteName;
	private String urlRepository;

	private Integer spiderId = 0;
	private String promulgatedData = "";
	private List<String> firstRoundLinks = new ArrayList<>();
	private List<String> secondRoundLinks = new ArrayList<>();
	private List<String> firstRoundIgnore = new ArrayList<>();
	private List<String> secondRoundIgnore = new ArrayList<>();

	private List<String> thirdRoundLinks = new ArrayList<>();
	private List<String> thirdRoundIgnore = new ArrayList<>();
	private XMLPropertyMap propertyMap = new XMLPropertyMap();

	public void addUniversalIgnore(List<String> universalIgnore)
	{
		List<String> fri = new ArrayList<>();
		fri.addAll(firstRoundIgnore);
		fri.addAll(universalIgnore);
		firstRoundIgnore = fri;

		List<String> sri = new ArrayList<>();
		sri.addAll(secondRoundIgnore);
		sri.addAll(universalIgnore);
		secondRoundIgnore = sri;

		List<String> tri = new ArrayList<>();
		tri.addAll(thirdRoundIgnore);
		tri.addAll(universalIgnore);
		thirdRoundIgnore = tri;
	}

	public String getFileAsString()
	{
		try
		{
			String temp = url.replace("&amp;", "&");
			URL urlURL = new URL(temp);

			String path = urlURL.getPath();
			String file = path.substring(path.lastIndexOf("/"), path.length());

			return file;

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}
	}

	public List<String> getFirstRoundIgnore()
	{
		return firstRoundIgnore;
	}

	public List<String> getFirstRoundLinks()
	{
		return firstRoundLinks;
	}

	public String getHostAndProtocol()
	{
		try
		{
			String temp = url.replace("&amp;", "&");
			URL urlURL = new URL(temp);
			String protocol = urlURL.getProtocol();
			String host = urlURL.getHost();

			return protocol + "://" + host + "/";
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}

	}

	public String getHostAsString()
	{
		try
		{

			URL urlURL = new URL(url);

			return urlURL.getHost();

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}
	}

	public String getOriginUrl()
	{
		return originUrl;
	}

	public String getPromulgatedData()
	{
		return promulgatedData;
	}

	public byte[] getPromulgatedDataAsByteArray()
	{
		return promulgatedData.getBytes(Charset.forName("UTF-8"));
	}

	public XMLPropertyMap getPropertyMap()
	{
		return propertyMap;
	}

	public int getRoundCount()
	{
		if (thirdRoundLinks.size() > 0)
		{
			return 3;
		}
		else if (secondRoundLinks.size() > 0)
		{
			return 2;
		}
		else if (thirdRoundLinks.size() > 0)
		{
			return 1;
		}
		return 4;
	}

	public List<String> getSecondRoundIgnore()
	{
		return secondRoundIgnore;
	}

	public List<String> getSecondRoundLinks()
	{
		return secondRoundLinks;
	}

	public Integer getSpiderId()
	{
		return spiderId;
	}

	public List<String> getThirdRoundIgnore()
	{
		return thirdRoundIgnore;
	}

	public List<String> getThirdRoundLinks()
	{
		return thirdRoundLinks;
	}

	public String getUriAsString()
	{
		try
		{
			String temp = url.replace("&amp;", "&");

			URL urlURL = new URL(temp);

			String path = urlURL.getPath();
			if (null == path || path.isEmpty())
			{
				return "";
			}
			if (!path.contains("/"))
			{
				path = path + "/";
			}
			if (!path.contains(".") && !path.contains("#") && !path.endsWith("/"))
			{
				path = path + "/";
			}

			String uri = path.substring(0, path.lastIndexOf("/"));
			NioLog.getLogger().debug("URI " + uri);
			return uri;

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}
	}

	public String getUrl()
	{
		return url;
	}

	public String getUrlAndUriAsString()
	{
		try
		{
			String temp = url.replace("&amp;", "&");
			URL urlURL = new URL(temp);
			String protocol = urlURL.getProtocol();
			String host = urlURL.getHost();
			String path = urlURL.getPath();

			if (null == path || path.isEmpty())
			{
				return "";
			}
			if (!path.contains("/"))
			{
				path = path + "/";
			}
			String uri = path.substring(0, path.lastIndexOf("/"));

			// NioLog.getLogger().debug(protocol + "://" + host+uri+"/");
			return protocol + "://" + host + uri + "/";
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}
	}

	public String getUrlRepository()
	{
		return urlRepository;
	}

	public String getWebSiteName()
	{
		return webSiteName;
	}

	public void initialize(ConfigurationBeanURL parentUrl) throws Exception
	{
		try
		{
			boolean asRestful = parentUrl.getPropertyMap().getAsBoolean(UrlSrc._AS_RESTFUL);

			this.setUrl(parentUrl.getUrl());
			this.setOriginUrl(parentUrl.getUrl());
			this.setWebSiteName(parentUrl.getWebsite());

			if (!asRestful)
			{
				this.setFirstRoundLinks(parentUrl.getFirstRoundLinks());
				this.setSecondRoundLinks(parentUrl.getSecondRoundLinks());
				this.setThirdRoundLinks(parentUrl.getThirdRoundLinks());
			}
			this.setFirstRoundIgnore(parentUrl.getFirstRoundIgnore());
			this.setSecondRoundIgnore(parentUrl.getSecondRoundIgnore());
			this.setThirdRoundIgnore(parentUrl.getThirdRoundIgnore());

			this.setPropertyMap(parentUrl.getPropertyMap());
		}
		catch (Exception e)
		{
			throw new Exception(e.getMessage(), e);
		}

	}

	public void initialize(UrlSrc configUrl) throws Exception
	{
		try
		{
			this.setUrl(configUrl.getUrl());
			this.setOriginUrl(configUrl.getOriginUrl());
			this.setWebSiteName(configUrl.getWebSiteName());
			this.setSpiderId(configUrl.getSpiderId());
			this.setUrlRepository(configUrl.getUrlRepository());
			this.setFirstRoundLinks(configUrl.getFirstRoundLinks());
			this.setSecondRoundLinks(configUrl.getSecondRoundLinks());
			this.setThirdRoundLinks(configUrl.getThirdRoundLinks());
			this.setFirstRoundIgnore(configUrl.getFirstRoundIgnore());
			this.setSecondRoundIgnore(configUrl.getSecondRoundIgnore());
			this.setThirdRoundIgnore(configUrl.getThirdRoundIgnore());
			this.getPropertyMap().addProperties(configUrl.getPropertyMap().getProperty());

		}
		catch (Exception e)
		{
			throw new Exception(e.getMessage(), e);
		}

	}

	@XmlElement
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
	public void setOriginUrl(String originUrl)
	{
		this.originUrl = originUrl;
	}

	@XmlElement
	public void setPromulgatedData(String promulgatedData)
	{
		this.promulgatedData = promulgatedData;
	}

	@XmlElement
	public void setPropertyMap(XMLPropertyMap propertyMap)
	{
		this.propertyMap = propertyMap;
	}

	@XmlElement
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
	public void setSpiderId(Integer spiderId)
	{
		this.spiderId = spiderId;
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
	public void setUrl(String inurl) throws Exception
	{
		if (!inurl.trim().toUpperCase().startsWith("HTTP"))
		{
			throw new Exception("Malformed Url: " + inurl);
		}
		this.url = inurl;
	}

	@XmlElement
	public void setUrlRepository(String configFileName)
	{
		this.urlRepository = configFileName;
	}

	@XmlElement
	public void setWebSiteName(String webSiteName)
	{
		this.webSiteName = webSiteName;
	}

	public byte[] toByteArray()
	{
		String s = "\n\nUrlSrc [\nurl=" + url + ", \nwebSiteName=" + webSiteName + "]\n\n\n";
		return s.getBytes(Charset.forName("UTF-8"));
	}

	@Override
	public String toString()
	{
		return "UrlSrc [\n\n{{{url=" + url + "}}}\n\n webSiteName=" + webSiteName + "\n spiderId=" + spiderId
				+ "\n promulgatedData=" + promulgatedData + "\nfirstRoundLinks=" + firstRoundLinks
				+ "\n secondRoundLinks=" + secondRoundLinks + "\n firstRoundIgnore=" + firstRoundIgnore
				+ "\n secondRoundIgnore=" + secondRoundIgnore + "\n thirdRoundLinks=" + thirdRoundLinks
				+ "\n thirdRoundIgnore=" + thirdRoundIgnore + "\n propertyMap=" + propertyMap + "\n]";
	}

}
