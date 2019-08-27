package ikoda.persistence.service;

import java.util.Comparator;

import ikoda.persistence.model.reporting.CPCSalaryByDegree;

public class ComparatoByMedianSalaryDesc implements Comparator<CPCSalaryByDegree>
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

		return value2.compareTo(value1);
	}
}
