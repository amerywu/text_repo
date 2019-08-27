package ikoda.utils;

import java.util.Map;
import java.util.TreeMap;

public class LabelValuesTuple
{
	String targetName = "";

	String value = "";

	Double target = 0.0;

	Map<Integer, Double> sparseMap = new TreeMap<>();

	public void addColValPair(Integer col, Double val) throws IKodaUtilsException
	{
		try
		{
			sparseMap.put(col, val);
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public Map<Integer, Double> getSparseMap()
	{
		return sparseMap;
	}

	public Double getTarget()
	{
		return target;
	}

	public String getTargetAsString()
	{
		return target.toString();
	}

	public String getValue()
	{
		return value;
	}

	public void setTarget(Double target)
	{
		this.target = target;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
