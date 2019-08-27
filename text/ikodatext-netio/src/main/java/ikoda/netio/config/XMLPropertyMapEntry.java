package ikoda.netio.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder =
{ "key", "value" })
@XmlRootElement(name = "propertyEntry")
public class XMLPropertyMapEntry
{

	private String key;

	private String value;

	public XMLPropertyMapEntry()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		XMLPropertyMapEntry other = (XMLPropertyMapEntry) obj;
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		return true;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@XmlElement
	public void setKey(String key)
	{
		this.key = key;
	}

	@XmlElement
	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return "\n          XMLPropertyMapEntry [key=" + key + ", value=" + value + "]";
	}

}
