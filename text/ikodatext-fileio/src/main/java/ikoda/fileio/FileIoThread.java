package ikoda.fileio;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;

import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.utils.ProcessStatus;


public class FileIoThread extends Thread
{

	private final static String TXT = ".TXT";
	private final static String PDF = ".PDF";
	private static  String jobPostingPath = ".";

	private  static   String jobListingsPath = ".";
	private  static   String jobXmlPath = ".";

	private  static   String jobUndeterminedPath = ".";
	private  static   String outBoxPath=".";
	private  static   String nonApplicablePath = ".";
	protected ReentrantLock lock;
	private int sleepTime = 200;
	protected List<Path> files;
	private boolean ready = false;
	private long fileioProcessStartTime = System.currentTimeMillis();

	protected String donePath = ".";
	private Path path;
	private boolean upstreamThreadDone = true;
	private boolean continueRun = true;
	private boolean stopDependsOnNetio;
	private boolean abort;
	protected ConfigurationBeanParent config;

	public FileIoThread()
	{

	}

	public void abort()
	{
		abort = true;
		continueRun=false;
	}

	protected synchronized boolean doFileProcess()
	{
		try
		{
			if (abort)
			{
				return false;
			}
			fileioProcessStartTime = System.currentTimeMillis();
			Path p = null;
			FioLog.getLogger().debug("files.size: " + files.size());
			if (files.isEmpty())
			{

				listFiles(path);

				/// make sure there's nothing left in the directory
				if (files.isEmpty())
				{
					sleepTime = 30000;
					if (stopDependsOnNetio)
					{
						if (upstreamThreadDone)
						{
							FioLog.getLogger().info("Files done and dependent thread done");
							return false;
						}
						else
						{
							FioLog.getLogger().info("Files done but waiting on dependent thread");
							return true;
							// because we are waiting
						}
					}
					else
					{
						FioLog.getLogger().info("Files done and not waiting on dependent thread");
						return false;
					}
				}
			}
			sleepTime = 200;

			p = files.remove(0);
			/// hack...some kinda threading ?
			if (null == p || null == p.getFileName())
			{
			    FioLog.getLogger().warn("\n\nNull for  "+p);
				return true;
			}

			if (p.getFileName().toString().toUpperCase().contains(TXT)||p.getFileName().toString().toUpperCase().contains(PDF))
			{
			    FioLog.getLogger().debug("Getting processor for "+p.getFileName());
				AbstractFileProcessor fp = FileProcessorFactory.getProcessor(p,config);
				fp.processFile(p, lock);
				ProcessStatus.incrementStatus("FIO: Files Processed");
				
			}
			moveFile(p);
			ProcessStatus.getStatusMap().put("FIO Files waiting in queue", String.valueOf(files.size()));
			return true;
		}
		catch (Exception e)
		{
			if (!files.isEmpty())
			{
				files.remove(0);
			}
			FioLog.getLogger().error(e.getMessage(), e);
			ProcessStatus.incrementStatus("FileioThread Exceptions in doFileProcess");
			return true;
		}
	}

	public long getFileProcessTime()
	{
		return System.currentTimeMillis() - fileioProcessStartTime;
	}


	
	
	protected int getSleepTime()
	{
		return sleepTime;
	}

	public boolean isContinueRun()
	{
		return continueRun;
	}

	public boolean isStop()
	{
		return upstreamThreadDone;
	}

	protected void listFiles(Path path)
	{
		FioLog.getLogger().info("started ......");
		try
		{
			if (lock.tryLock(8, TimeUnit.SECONDS))
			{
				try
				{

					try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
					{

						for (Path entry : stream)
						{
							if (Files.isDirectory(entry))
							{
								listFiles(entry);
							}
							else
							{
								files.add(entry);
							}
						}
						Collections.sort(files);
					}
					catch (Exception e)
					{
						FioLog.getLogger().error(e.getMessage(), e);
					}
				}
				catch (Exception e)
				{

					FioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
				FioLog.getLogger().info("done");
			}
			else
			{
				FioLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	protected void moveFile(Path p)
	{
		try
		{
			Path movefrom = p;
			Path target = FileSystems.getDefault().getPath(donePath + File.separator + p.getFileName());
			// method 1
			Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);

		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	@Override
	public void run()
	{
		try
		{

			FioLog.getLogger().info("starting thread");
			int timeoutNoDataReceived = 0;
			while (continueRun)
			{

				if (lock.isHeldByCurrentThread())
				{
					FioLog.getLogger().warn("Releasing lock before going to sleep. This is bad");
					lock.unlock();
				}
				Thread.sleep(sleepTime);
				if (null == files || ready == false)
				{
					timeoutNoDataReceived++;
					if (timeoutNoDataReceived >= 2000)
					{
						continueRun = false;
					}
				}
				else if (!doFileProcess())
				{
					FioLog.getLogger().info("EXITING THREAD");
					continueRun = false;
				}

			}
		}
		catch (InterruptedException e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
			continueRun = false;
			this.interrupt();
			return;
		}
		catch (Exception e)
		{
			FioLog.getLogger().error("This is Bad. Thread won't start");
			continueRun = false;
			FioLog.getLogger().error(e.getMessage(), e);
		}
		catch (Error err)
		{
			FioLog.getLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
			continueRun = false;
			throw err;
		}
	}

	public void runFileIo(boolean depend, ConfigurationBeanParent inconfig, ReentrantLock inLock) throws Exception
	{

		try
		{
		FioLog.getLogger().info(inLock);

			config=inconfig;
			files = new ArrayList<Path>();
			lock = inLock;
			path = Paths.get(inconfig.getNetioConfig().getNetIoDumpPath());
			donePath = inconfig.getFioConfig().getFileIoDonePath();
			ConfigurationBeanForFileIo_Generic fioConfig= (ConfigurationBeanForFileIo_Generic)inconfig.getFioConfig();
			jobPostingPath = fioConfig.getFileIoJobPostingPath();
			jobListingsPath = fioConfig.getFileIoResultsPath();
			jobXmlPath = fioConfig.getFileIoXMLPath();
			jobUndeterminedPath = fioConfig.getFileIoUndeterminedPath();
			nonApplicablePath = fioConfig.getFileIoNonApplicablePath();
			outBoxPath = fioConfig.getFileIoOutBoxPath();

			if (!Files.exists(Paths.get(inconfig.getNetioConfig().getNetIoDumpPath())))
			{
				Files.createDirectories(Paths.get(inconfig.getNetioConfig().getNetIoDumpPath()));
			}
			if (!Files.exists(Paths.get(inconfig.getFioConfig().getFileIoDonePath())))
			{
				Files.createDirectories(Paths.get(inconfig.getFioConfig().getFileIoDonePath()));
			}
			if (!Files.exists(Paths.get(fioConfig.getFileIoResultsPath())))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoResultsPath()));
			}
			if (!Files.exists(Paths.get(fioConfig.getFileIoXMLPath())))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoXMLPath()));
			}
			if (!Files.exists(Paths.get(fioConfig.getFileIoOutBoxPath())))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoOutBoxPath()));
			}
			if (!Files.exists(Paths.get(fioConfig.getFileIoUndeterminedPath())))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoUndeterminedPath()));
			}
			if (!Files.exists(Paths.get(fioConfig.getFileIoNonApplicablePath())))
			{
				Files.createDirectories(Paths.get(fioConfig.getFileIoNonApplicablePath()));
			}
			listFiles(path);
			ready = true;
			stopDependsOnNetio = depend;
			if(depend)
			{
				upstreamThreadDone=false;
			}

		}
		catch(Exception e)
		{
			FioLog.getLogger().error(e.getMessage(),e);
			continueRun=false;
			throw new Exception (e);
		}
	}
	
	
	protected void setSleepTime(int sleepTime)
	{
		this.sleepTime = sleepTime;
	}
	
		
	public void setUpstreamThreadDone(boolean stop)
	{
		FioLog.getLogger().info("Calling STOP  on FileIo");
		this.upstreamThreadDone = stop;
	}

	public boolean isUpstreamThreadDone()
	{
		return upstreamThreadDone;
	}
	
	
}
