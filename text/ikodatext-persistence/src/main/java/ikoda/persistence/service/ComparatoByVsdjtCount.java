package ikoda.persistence.service;

import java.util.Comparator;

import ikoda.persistence.model.ValueSalaryByDegreeAndJobTitle;

public class ComparatoByVsdjtCount implements Comparator<ValueSalaryByDegreeAndJobTitle>
{

	@Override
	public int compare(ValueSalaryByDegreeAndJobTitle obj1, ValueSalaryByDegreeAndJobTitle obj2)
	{
		Integer value1 = obj1.getCount();
		Integer value2 = obj2.getCount();

		return value2.compareTo(value1);
	}
}