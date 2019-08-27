package ikoda.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;


/***
 * An accumulator for status messages that can be passed to logging mechanisms
 * @author jake
 *
 */
public class ProcessStatus
{
	private static Logger logger = SSm.getAppLogger();
	private static Map<String, String> statusMap = new ConcurrentHashMap<>();
	private static Map<String, StatusAccumulator> accumulatorMap = new ConcurrentHashMap<>();
	
	
	
	
	public static synchronized void averageOver(String key, int accumulateCount, double value)
	{
		try
		{
			StatusAccumulator accumulator = accumulatorMap.get(key);

			if (null == accumulator)
			{
				
				StatusAccumulator sa = new StatusAccumulator();
				sa.setCountToAccumulate(accumulateCount);
				sa.setName(key);
				sa.add(value);
				accumulatorMap.put(key, sa);
				
				statusMap.put(key, String.valueOf(sa.getAverage()));

			}
			else
			{
				accumulator.add(value);


			}
		}
		catch (Exception e)
		{

			logger.error(e.getMessage(), e);
		}
	}
	
	

	

	public static synchronized void concatenateStatus(String key, String valueIn)
	{
		try
		{
			String value = statusMap.get(key);

			if (null == value)
			{
				statusMap.put(key, valueIn);

			}
			else
			{

				statusMap.put(key, value+" "+valueIn);

			}
		}
		catch (Exception e)
		{

			logger.error(e.getMessage(), e);
		}
	}

	public static synchronized Map<String, String> getStatusMap()
	{
		try
		{
			return statusMap;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public static synchronized void incrementStatus(String key)
	{
		try
		{
			String value = statusMap.get(key);

			if (null == value)
			{
				statusMap.put(key, "1");

			}
			else
			{
				Double count = new Double(value);

				count++;

				statusMap.put(key, String.valueOf(count));

			}
		}
		catch (Exception e)
		{

			logger.error(e.getMessage(), e);
		}
	}

	public static synchronized String print()
	{
		return print(new ArrayList<String>());
	}

	public static synchronized String print(List<String> ignoreList)
	{
		Iterator<String> itr = statusMap.keySet().iterator();
		List<String> l = new ArrayList<>();
		while (itr.hasNext())
		{
			String key = itr.next();
			if (ignoreList.stream().filter(ignore -> key.contains(ignore)).count() == 0)
			{
				String value = statusMap.get(key);
				l.add(key + " : " + value);
			}

		}
		Collections.sort(l);
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (String s : l)
		{
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}

	public static synchronized void put(String key, double value)
	{

		statusMap.put(key, new Double(value).toString());
	}

	public static synchronized void put(String key, Double value)
	{

		statusMap.put(key, value.toString());
	}

	public static synchronized void put(String key, int value)
	{

		statusMap.put(key, new Integer(value).toString());
	}

	public static synchronized void put(String key, Integer value)
	{

		statusMap.put(key, value.toString());
	}

	public static synchronized void put(String key, Long value)
	{

		statusMap.put(key, value.toString());
	}

	public static synchronized void put(String key, String value)
	{
		statusMap.put(key, value);
	}

	public static void setLogger(Logger logger)
	{
		ProcessStatus.logger = logger;
	}

	public static synchronized void setStatusMap(Map<String, String> statusMap)
	{
		ProcessStatus.statusMap = statusMap;
	}

	private ProcessStatus()
	{

	}

}
