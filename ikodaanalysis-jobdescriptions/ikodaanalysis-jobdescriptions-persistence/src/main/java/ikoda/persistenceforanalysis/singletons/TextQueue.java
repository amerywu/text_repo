package ikoda.persistenceforanalysis.singletons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.persistence.application.PLog;



public class TextQueue
{

	private static List<RawDataUnit> textQueueList = new CopyOnWriteArrayList<RawDataUnit>();
	private final ReentrantLock lock = new ReentrantLock();
	private static TextQueue textQueue;
	private int qsize=0;

	private TextQueue()
	{

	}

	public static TextQueue getInstance()
	{
		if (null == textQueue)
		{
			textQueue = new TextQueue();
		}
		return textQueue;
	}

	public synchronized int size()
	{
		return qsize;

	}

	public boolean isEmpty()
	{
		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					return textQueueList.isEmpty();
				}
				return false;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return false;
		}
	}

	public boolean contains(Object o)
	{

		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					return textQueueList.contains(o);
				}
				return false;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return false;
		}

	}

	public Iterator<RawDataUnit> iterator()
	{

		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					return textQueueList.iterator();
				}
				return null;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}

	}

	public boolean add(RawDataUnit s)
	{
		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					qsize=textQueueList.size()+1;
					return textQueueList.add(s);
				}
				return false;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return false;
		}
	}

	
	
	
	public void clear()
	{

		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					textQueueList.clear();
				}
				return;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return;
		}

	}

	public RawDataUnit get(int index)
	{

		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					return textQueueList.get(index);
				}
				return null;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}

	}

	public RawDataUnit remove(int index)
	{
		
		try
		{
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					if(textQueueList.size()>0)
					{
					qsize=textQueueList.size()-1;
					return textQueueList.remove(index);
					}
					else
					{
						PLog.getLogger().warn("Queue is empty");
						return null;
					}
				}
				PLog.getLogger().warn("Failed to get queue. Locked");
				return null;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			PLog.getLogger().error(e.getMessage(), e);
			return null;
		}
		
		
	
	}

}
