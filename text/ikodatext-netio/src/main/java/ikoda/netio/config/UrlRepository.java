package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "urls" })
@XmlRootElement(name = "urlrepository")
public class UrlRepository
{

	private List<ConfigurationBeanURL> urls = new ArrayList<ConfigurationBeanURL>();

	public List<ConfigurationBeanURL> getUrls()
	{
		return urls;
	}

	@XmlElement
	public void setUrls(List<ConfigurationBeanURL> urls)
	{
		this.urls = urls;
	}

}
