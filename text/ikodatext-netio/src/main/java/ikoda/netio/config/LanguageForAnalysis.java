package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "localeCode", "regions" })
@XmlRootElement(name = "languageForAnalysis")
public class LanguageForAnalysis
{

	private String localeCode;
	private List<String> regions = new ArrayList<String>();

	public String getLocaleCode()
	{
		return localeCode;
	}

	public List<String> getRegions()
	{
		return regions;
	}

	@XmlElement
	public void setLocaleCode(String localeCode)
	{
		this.localeCode = localeCode;
	}

	@XmlElement
	public void setRegions(List<String> regions)
	{
		this.regions = regions;
	}

	@Override
	public String toString()
	{
		return "LanguageForAnalysis [localeCode=" + localeCode + ", regions=" + regions + "]";
	}

}
