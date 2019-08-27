package ikoda.netio.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ikoda.netio.NioLog;

@XmlType(propOrder =
{ "databaseDescriptor", "majorMapEntry" })
@XmlRootElement(name = "majormapconfiguration")
public class MajorMappingConfig
{

	private ArrayList<MajorMappingConfigEntry> majorMapEntry = new ArrayList<MajorMappingConfigEntry>();
	private String databaseDescriptor;

	private Map<String, List<String>> ikodaMajorMap;
	private Map<String, List<String>> specifiedMajorMap;

	public MajorMappingConfig()
	{
		// TODO Auto-generated constructor stub
	}

	public String getDatabaseDescriptor()
	{
		return databaseDescriptor;
	}

	public Set<String> getDegreeLevelsForSpecifiedDegreeName(String inSpecifiedDegreeName)
	{
		Set<String> returnSet = new HashSet<String>();
		try
		{
			Iterator<MajorMappingConfigEntry> itr = majorMapEntry.iterator();
			while (itr.hasNext())
			{
				MajorMappingConfigEntry entry = itr.next();
				for (String specifiedDegreeName : entry.getRelatedMajors())
				{
					if (inSpecifiedDegreeName.equals(specifiedDegreeName))
					{
						returnSet.add(entry.getDegreeLevel());
					}
				}
			}
			return returnSet;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return null;
		}
	}

	public ArrayList<MajorMappingConfigEntry> getMajorMapEntry()
	{
		return majorMapEntry;
	}

	public Map<String, List<String>> ikodaMajorMap()
	{
		if (null == ikodaMajorMap)
		{
			initializeRelatedMajorMap();
		}
		return ikodaMajorMap;
	}

	private void initializeActualMajorMap()
	{
		specifiedMajorMap = new HashMap<String, List<String>>();
		for (MajorMappingConfigEntry mmce : majorMapEntry)
		{
			NioLog.getLogger().debug("Specified Majors for " + mmce.getIkodamajor());
			NioLog.getLogger().debug("count: " + mmce.getRelatedMajors());
			for (String s : mmce.getRelatedMajors())
			{
				NioLog.getLogger().debug("Specified Major: " + s);
				List<String> ikodaEntries = specifiedMajorMap.get(s);
				if (null == specifiedMajorMap.get(s))
				{
					ikodaEntries = new ArrayList<String>();
					ikodaEntries.add(mmce.getIkodamajor());
					NioLog.getLogger().debug("adding first " + mmce.getIkodamajor());
					specifiedMajorMap.put(s, ikodaEntries);
				}
				else
				{
					NioLog.getLogger().debug("adding " + mmce.getIkodamajor());
					ikodaEntries.add(mmce.getIkodamajor());
				}
			}
		}

	}

	private void initializeRelatedMajorMap()
	{
		ikodaMajorMap = new HashMap<String, List<String>>();
		for (MajorMappingConfigEntry mmce : majorMapEntry)
		{
			ikodaMajorMap.put(mmce.getIkodamajor(), mmce.getRelatedMajors());
		}
	}

	@XmlElement
	public void setDatabaseDescriptor(String databaseDescriptor)
	{
		this.databaseDescriptor = databaseDescriptor;
	}

	@XmlElement
	public void setMajorMapEntry(ArrayList<MajorMappingConfigEntry> majorMapEntry)
	{
		this.majorMapEntry = majorMapEntry;
	}

	public Map<String, List<String>> specifiedMajorMap()
	{
		if (null == specifiedMajorMap)
		{
			initializeActualMajorMap();
		}
		return specifiedMajorMap;
	}

}
