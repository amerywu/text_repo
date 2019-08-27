package ikoda.persistence.service;

import java.util.Comparator;

import ikoda.persistence.model.ValueSalaryByDegree;

public class ComparatoByVsdCount implements Comparator<ValueSalaryByDegree>
{

	@Override
	public int compare(ValueSalaryByDegree obj1, ValueSalaryByDegree obj2)
	{
		Integer value1 = obj1.getCount();
		Integer value2 = obj2.getCount();

		return value2.compareTo(value1);
	}
}