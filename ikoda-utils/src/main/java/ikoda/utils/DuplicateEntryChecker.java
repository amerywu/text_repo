package ikoda.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/***
 * Checks for duplicate entries in NLP data
 * @author jake
 *
 */
public class DuplicateEntryChecker
{

	private static DuplicateEntryChecker duplicateEntryChecker;

	public static DuplicateEntryChecker getInstance()
	{
		if (null == duplicateEntryChecker)
		{
			duplicateEntryChecker = new DuplicateEntryChecker();
		}
		return duplicateEntryChecker;
	}

	private Map<String, String> uidMap = new HashMap<String, String>();
	private Set<Integer> hashcodeSet = new HashSet<>();

	private Set<Integer> encounteredDuplicateHashcodes = new HashSet<>();

	private DuplicateEntryChecker()
	{

	}

	private void cleanHashCodeSet()
	{
		if (hashcodeSet.size() > encounteredDuplicateHashcodes.size() * 3 && hashcodeSet.size() > 5000)
		{
			Iterator<Integer> itr = hashcodeSet.iterator();
			while (itr.hasNext())
			{
				Integer i = itr.next();
				if (!encounteredDuplicateHashcodes.contains(i))
				{
					itr.remove();
				}

			}

		}
	}

	public boolean isDuplicateEntry(String id)
	{
		if (null == uidMap.get(id))
		{
			//SSm.getAppLogger().debug("added key " + id);
			uidMap.put(id, id);
			//SSm.getAppLogger().debug("added infobox to duplicatemap " + id);
			return true;
		}
		SSm.getAppLogger().warn("\n\n\nDUPLICATE INFO BOX ENTRY: " + id);
		return false;
	}

	public boolean isDuplicateHashCode(Integer i)
	{
		cleanHashCodeSet();
		//SSm.getAppLogger().debug("hashcodeSet size" + hashcodeSet.size());
		if (hashcodeSet.contains(i))
		{
			SSm.getAppLogger().debug("DUPLICATE " + i);
			encounteredDuplicateHashcodes.add(i);
			return true;
		}
		else
		{
			hashcodeSet.add(i);
			return false;
		}
	}

}
