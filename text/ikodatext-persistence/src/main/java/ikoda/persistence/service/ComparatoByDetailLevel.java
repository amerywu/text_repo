package ikoda.persistence.service;

import java.util.Comparator;

import ikoda.persistence.model.reporting.CPCJob;

public class ComparatoByDetailLevel implements Comparator<CPCJob>
{

	@Override
	public int compare(CPCJob obj1, CPCJob obj2)
	{
		Integer value1 = obj1.getDetailLevel();
		Integer value2 = obj2.getDetailLevel();

		if (null == value1)
		{
			obj1.setDetailLevel(new Integer(0));
		}
		if (null == value2)
		{
			obj2.setDetailLevel(new Integer(0));
		}
		return value2.compareTo(value1);
	}
}