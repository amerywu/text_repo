package ikoda.persistence.service;

import java.util.Comparator;

public class ComparatoByQueuePosition implements Comparator<QueueObject>
{

	@Override
	public int compare(QueueObject obj1, QueueObject obj2)
	{
		Integer value1 = obj1.getPositionInQueue();
		;
		Integer value2 = obj2.getPositionInQueue();

		return value2.compareTo(value1);
	}
}