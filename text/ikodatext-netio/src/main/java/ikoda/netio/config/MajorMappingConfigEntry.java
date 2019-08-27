package ikoda.netio.config;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "ikodamajor", "localIkodamajor", "degreeLevel", "relatedMajors", })
@XmlRootElement(name = "mapEntry")
public class MajorMappingConfigEntry
{

	private String ikodamajor;
	private String localIkodamajor;
	private String degreeLevel;
	private ArrayList<String> relatedMajors;

	public String getDegreeLevel()
	{
		return degreeLevel;
	}

	public String getIkodamajor()
	{
		return ikodamajor;
	}

	public String getLocalIkodamajor()
	{
		if (null == localIkodamajor)
		{
			return ikodamajor;
		}
		return localIkodamajor;
	}

	public ArrayList<String> getRelatedMajors()
	{
		return relatedMajors;
	}

	@XmlElement
	public void setDegreeLevel(String degreeLevel)
	{
		this.degreeLevel = degreeLevel;
	}

	@XmlElement
	public void setIkodamajor(String ikodamajor)
	{
		this.ikodamajor = ikodamajor;
	}

	@XmlElement
	public void setLocalIkodamajor(String localIkodamajor)
	{
		this.localIkodamajor = localIkodamajor;
	}

	@XmlElement
	public void setRelatedMajors(ArrayList<String> relatedMajors)
	{
		this.relatedMajors = relatedMajors;
	}

}
