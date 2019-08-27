package ikoda.utils;

import java.util.LinkedList;

import org.apache.logging.log4j.Logger;

public class StatusAccumulator
{
	private String name;
	private int countToAccumulate=1;
	private LinkedList<Double> accumulated = new LinkedList<>();
	private static Logger logger = SSm.getAppLogger();
	
	protected void add(Double i)
	{
		try
		{
		if(accumulated.size()>=countToAccumulate)
		{
			accumulated.removeFirst();
		}
		accumulated.addLast(i);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		
	}
	
	protected double getAverage()
	{
		try
		{
			double total= accumulated.stream().mapToDouble(i -> i.doubleValue()).sum();
			if(accumulated.size()>0)
			{
				return total/accumulated.size();
			}
			else
			{
				return -1.111;
			}
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			return -2.222;
		}
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public int getCountToAccumulate()
	{
		return countToAccumulate;
	}
	public void setCountToAccumulate(int countToAccumulate)
	{
		this.countToAccumulate = countToAccumulate;
	}
	
	
	
	
	
}
