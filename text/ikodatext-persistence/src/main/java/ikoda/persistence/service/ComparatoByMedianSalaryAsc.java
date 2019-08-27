package ikoda.persistence.service;

import java.util.Comparator;

import ikoda.persistence.model.reporting.CPCSalaryByDegree;

public class ComparatoByMedianSalaryAsc implements Comparator<CPCSalaryByDegree>
{

	@Override
	public int compare(CPCSalaryByDegree obj1, CPCSalaryByDegree obj2)
	{
		Integer value1 = obj1.getTypicalSalary();
		Integer value2 = obj2.getTypicalSalary();

		if (null == value1)
		{
			obj1.setTypicalSalary(new Integer(0));
		}
		if (null == value2)
		{
			obj2.setTypicalSalary(new Integer(0));
		}
		return value1.compareTo(value2);
	}
}
