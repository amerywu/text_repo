package ikoda.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrossTab implements Serializable
{

	private class CrossTabEntry implements Serializable
	{

		private static final long serialVersionUID = 13465347253645L;
		private List<String> coordinates;
		private List<Object> entry;

		private CrossTabEntry(List<String> coordinates, List<Object> entry)
		{
			this.coordinates = coordinates;
			this.entry = entry;
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
			CrossTabEntry other = (CrossTabEntry) obj;

			boolean match = true;
			;
			for (String s : coordinates)
			{
				boolean innerMatch = false;
				for (String s1 : other.getCoordinates())
				{
					if (s.equals(s1))
					{
						innerMatch = true;
					}
				}
				match = innerMatch;
				if (!innerMatch)
				{
					break;
				}
			}
			// SSm.getLogger().debug("match:"+match);;
			return match;

		}

		public List<String> getCoordinates()
		{
			return coordinates;
		}

		public List<Object> getEntry()
		{
			return entry;
		}

		@Override
		public int hashCode()
		{
			int i = 0;
			for (String s : coordinates)
			{
				i += s.length();
				char[] chararray = s.toLowerCase().toCharArray();
				for (int j = 0; j < chararray.length; j++)
				{
					if (chararray[j] == 'a')
					{
						i += 5;
					}
					if (chararray[j] == 'e')
					{
						i += 7;
					}
				}

			}
			// SSm.getLogger().debug("hashcode value: "+i);
			return i;
		}

		public void setCoordinates(List<String> coordinates)
		{
			this.coordinates = coordinates;
		}

		public void setEntry(List<Object> entry)
		{
			this.entry = entry;
		}
	}

	private static final long serialVersionUID = -1989087643564243L;

	private final static String COORDINATE = "Coordinate";
	private final static String SEPARATOR = "_";
	private List<String> categories;
	private int dimensionCount = 2;

	private Map<CrossTabEntry, CrossTabEntry> entries = new HashMap<CrossTabEntry, CrossTabEntry>();

	public CrossTab(List<String> categories)
	{
		this.categories = categories;

	}

	public CrossTab(List<String> categories, int dimensionCount)
	{
		this.categories = categories;
		this.dimensionCount = dimensionCount;
	}

	/**
	 * @param coordinates
	 * @param entry
	 * @throws Exception
	 */
	public void addEntry(List<String> coordinates, List<Object> entry) throws Exception
	{
		try
		{

			validateCoordinates(coordinates);

			CrossTabEntry cte = new CrossTabEntry(coordinates, entry);

			entries.put(cte, cte);

		}
		catch (Exception e)
		{
			throw new Exception(e.getMessage(), e);
		}
	}

	public boolean addEntryIfEmptyCell(List<String> coordinates, List<Object> entry) throws Exception
	{
		if (null == entries.get(new CrossTabEntry(coordinates, entry)))
		{

			addEntry(coordinates, entry);
			return true;
		}

		return false;
	}

	public int getCategoryCount()
	{
		return categories.size();
	}

	public int getDimensionCount()
	{
		return dimensionCount;
	}

	public List<Object> getEntry(List<String> coordinates) throws Exception
	{
		validateCoordinates(coordinates);
		CrossTabEntry entry = entries.get(new CrossTabEntry(coordinates, new ArrayList<Object>()));
		// SSm.getLogger().debug("returning "+entry);
		return entry.getEntry();
	}

	public boolean isFullyPopulatedExceptDiagonals()
	{

		int c = categories.size();
		int full = ((c * c) - ((c * dimensionCount) - c)) / dimensionCount;
		if (entries.size() >= full)
		{
			return true;
		}
		SSm.getAppLogger().info("entries.size(): " + entries.size());
		SSm.getAppLogger().info("fully Populated: " + full);
		return false;
	}

	private void validateCoordinates(List<String> coordinates) throws Exception
	{
		if (coordinates.size() != dimensionCount)
		{
			SSm.getAppLogger().error("Dimension count mismatch. We need " + dimensionCount + " dimensions.");
			throw new Exception("Dimension count mismatch. We need " + dimensionCount + " dimensions.");
		}
		for (String category : coordinates)
		{
			long count = categories.stream().filter(c -> c.equals(category)).count();
			if (count == 0)
			{
				SSm.getAppLogger().error("This coordinate does not exist in the crosstab:" + category);
				throw new Exception("This coordinate does not exist in the crosstab:" + category);
			}
			if (count > 1)
			{
				SSm.getAppLogger().error("DUPLICATE CATEGORY. This appears twice in each dimension:" + category);
				throw new Exception("DUPLICATE CATEGORY. This appears twice in one dimension:" + category);
			}
		}
	}

}
