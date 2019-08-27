package ikoda.netio.config;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ikoda.netio.NioLog;

@XmlType(propOrder =
{ "property" })
@XmlRootElement(name = "propertyMap")
public class XMLPropertyMap
{

	private Set<XMLPropertyMapEntry> property = new HashSet<XMLPropertyMapEntry>();

	public XMLPropertyMap()
	{
		// TODO Auto-generated constructor stub
	}

	public boolean getAsBoolean(String inkey, int round)
	{
		String prefix = "R" + String.valueOf(round);
		String key = prefix + inkey;
		return getAsBoolean(key);

	}

	public void addProperties(Set<XMLPropertyMapEntry> p)
	{
		property.addAll(p);
	}

	public boolean getAsBoolean(String key)
	{
		try
		{

			Optional<XMLPropertyMapEntry> oEntry = property.stream()
					.filter(entry -> entry.getKey().toUpperCase().equals(key.toUpperCase())).findFirst();

			if (oEntry.isPresent())
			{
				String value = oEntry.get().getValue();

				if (value.equalsIgnoreCase("TRUE"))
				{

					return true;
				}
				else if (value.equalsIgnoreCase("FALSE"))
				{
					return false;
				}
				else
				{
					NioLog.getLogger().error("malformed value for " + key + " ");
					return false;
				}
			}
			return false;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return false;
		}
	}

	public String getAsString(String inkey, int round)
	{
		String prefix = "R" + round;
		String key = prefix + inkey;

		Optional<XMLPropertyMapEntry> oEntry = property.stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(key.toUpperCase())).findFirst();
		if (oEntry.isPresent())
		{
			NioLog.getLogger().debug(oEntry.get().getValue());
			return oEntry.get().getValue();
		}
		return null;
	}

	public String getAsString(String inkey)
	{

		Optional<XMLPropertyMapEntry> oEntry = property.stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(inkey.toUpperCase())).findFirst();
		if (oEntry.isPresent())
		{

			return oEntry.get().getValue();
		}
		NioLog.getLogger().warn(inkey + "NOT FOUND IN CONFIG FILE");
		return inkey + " NOT FOUND IN CONFIG FILE";
	}

	public int getAsInt(String inkey)
	{

		Optional<XMLPropertyMapEntry> oEntry = property.stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(inkey.toUpperCase())).findFirst();
		if (oEntry.isPresent())
		{
			try
			{
				return Integer.valueOf(oEntry.get().getValue());
			}
			catch (Exception e)
			{
				NioLog.getLogger().error(e.getMessage(), e);
				return 0;
			}
		}
		NioLog.getLogger().warn(inkey + "NOT FOUND IN CONFIG FILE");
		return 0;
	}

	public Set<XMLPropertyMapEntry> getProperty()
	{
		return property;
	}

	public XMLPropertyMapEntry getProperty(String inkey, int round)
	{

		String prefix = "R" + round;
		String key = prefix + inkey;
		Optional<XMLPropertyMapEntry> oEntry = property.stream()
				.filter(entry -> entry.getKey().equalsIgnoreCase(key.toUpperCase())).findFirst();
		if (oEntry.isPresent())
		{
			return oEntry.get();
		}
		return null;
	}

	public void put(String key, String value, int round)
	{

		XMLPropertyMapEntry entry = getProperty(key, round);
		if (null == entry)
		{
			entry = new XMLPropertyMapEntry();
			entry.setKey(key);
			entry.setValue(value);
			property.add(entry);
		}
		else
		{
			entry.setValue(value);
		}
	}

	@XmlElement
	public void setProperty(Set<XMLPropertyMapEntry> property)
	{
		this.property = property;
	}

	@Override
	public String toString()
	{
		return "\n     XMLPropertyMap [property=" + property + "]";
	}

}
