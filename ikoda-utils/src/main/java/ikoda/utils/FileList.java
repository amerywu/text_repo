package ikoda.utils;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Logger;


/**
 * Thread safe file monitoring
 * @author jake
 *
 */
public class FileList
{

	private static FileList fileList;

	public static FileList getInstance()
	{
		if (null == fileList)
		{
			fileList = new FileList();
		}
		return fileList;
	}

	private int fileCount = 0;

	private int rollingRowCurrentCount = 0;

	private Map<String, FileTuple> fileMap = new HashMap<>();

	private Logger logger=SSm.getAppLogger();

	protected ReentrantLock lock;

	private FileList()
	{

	}

	private FileTuple getFileTuple(String name) throws IKodaUtilsException
	{
		FileTuple ft = fileMap.get(name);
		if (null == ft)
		{
			logger.warn("---   -----------WARNING----------   ---\nNo file list has been initialized with name " + name);
			return new FileTuple(name,new ArrayList<Path>());
		}
		return ft;
	}

	public synchronized Path getNextFile(String name) throws IKodaUtilsException
	{
		try
		{
			if(getFileTuple(name).getFiles().size()>0)
			{
				return getFileTuple(name).getFiles().remove(0);
			}
			else 
			{
				logger.warn("No files available for "+name+ " in "+getFileTuple(name).getInputPaths());
				
				return null;
			}
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
	}

	public synchronized int getRollingCount()
	{
		return rollingRowCurrentCount;
	}

	public synchronized int getTotalCount()
	{
		return fileCount;
	}

	public synchronized void incrementCount()
	{
		fileCount++;
	}

	public synchronized void incrementRollingCount()
	{
		rollingRowCurrentCount++;
	}

	public void init(String name, List<Path> paths, Logger inlogger, ReentrantLock inlock)
	{

		if (null == fileMap.get(name))
		{

			FileTuple ft = new FileTuple(name, paths);
			fileMap.put(name, ft);
			logger = initLogger(inlogger);
			lock = inlock;
			logger.info("Paths initialized for " + name+": "+paths);

		}
		else
		{
			logger.warn("FileList already initialized for " + name);
		}
	}

	private Logger initLogger(Logger inlogger)
	{
		Logger alogger = inlogger;
		return alogger;
	}

	private Logger initLogger(String loggerName)
	{
		return SSm.getLogger(loggerName);

	}

	public synchronized boolean isEmptyFileList(String name) throws IKodaUtilsException
	{
		
		logger.debug("isEmptyFileList: "+getFileTuple(name).getFiles().isEmpty());
		return getFileTuple(name).getFiles().isEmpty();

	}

	public synchronized void listFiles(String name)
	{

		try
		{
			if (!getFileTuple(name).getFiles().isEmpty())
			{
				return;
			}

			if (lock.tryLock(6, TimeUnit.SECONDS))
			{

				try
				{
					for (Path p : getFileTuple(name).getInputPaths())
					{

						try
						{

							try (DirectoryStream<Path> stream = Files.newDirectoryStream(p))
							{

								for (Path entry : stream)
								{
									if (Files.isDirectory(entry))
									{
										continue;
									}
		
									if (entry.toString().toUpperCase().contains(".TXT"))
									{
										getFileTuple(name).getFiles().add(entry);
									}
								}
							}
							catch (Exception e)
							{
								logger.error(e.getMessage(), e);
							}
						}
						catch (Exception e)
						{
							logger.error(e.getMessage(), e);
						}
						
						logger.info("Found "+getFileTuple(name).getFiles().size()+" files");

					}
				}
				catch (Exception e)
				{
					logger.error(e);
				}
				finally
				{
					lock.unlock();
				}

			}
			else
			{
				logger.warn("Could not get lock");

			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);

		}
		logger.debug("done.");
	}

	public synchronized boolean remove(String name, int index)
	{

		try
		{
			getFileTuple(name).getFiles().remove(index);
			return true;
		}
		catch (Exception e)
		{
			logger.error(e);
			return false;
		}
	}

	public synchronized void resetRollingCount()
	{
		rollingRowCurrentCount = 0;
	}

	public synchronized int size(String name) throws IKodaUtilsException
	{
		return getFileTuple(name).getFiles().size();
	}

}
