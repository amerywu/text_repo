package ikoda.persistence.service;

import java.util.Comparator;

import ikoda.persistence.model.reporting.CPCPayByJobTitleAndDegree;

public class ComparatoByMedianSalaryDesc_For_CPCPayByJobTitleAndDegree implements Comparator<CPCPayByJobTitleAndDegree>
{

	@Override
	public int compare(CPCPayByJobTitleAndDegree obj1, CPCPayByJobTitleAndDegree obj2)
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
