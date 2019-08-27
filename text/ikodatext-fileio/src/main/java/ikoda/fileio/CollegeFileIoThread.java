package ikoda.fileio;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.utils.ProcessStatus;
import ikoda.utils.StaticSundryUtils;

public class CollegeFileIoThread extends FileIoThread
{

	private Map<String, Integer> uidCount = new HashMap<String, Integer>();

	public CollegeFileIoThread()
	{
		// TODO Auto-generated constructor stub
	}

	private boolean countByUid(Path entry)
	{
		try
		{
			String key = getUid(entry);
			if (null == key)
			{
				return false;
			}

			Integer count = uidCount.get(key);
			if (null == count)
			{
				uidCount.put(key, new Integer(1));
				return true;
			}

			int newCount = count.intValue() + 1;
			uidCount.put(key, newCount);
			return true;
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
			return false;
		}

	}



	protected String findUniqueFile(Path dirPath, String fileNameCriteria)
	{
		try
		{

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath))
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{

					Path foundFile;
					for (Path entry : stream)
					{

						if (entry.toString().contains(fileNameCriteria))
						{
							FioLog.getLogger().debug("found" + entry.toString());
							foundFile = entry;
							return foundFile.toString();

						}
					}
				}
				return null;

			}
			catch (IOException x)
			{
				// IOException can never be thrown by the iteration.
				// In this snippet, it can // only be thrown by
				// newDirectoryStream.
				FioLog.getLogger().error(x.getMessage(), x);
				return null;
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
			return null;
		}

	}

	private String getUid(Path entry)
	{
		try
		{
			String uid = null;
			if (entry.toString().toUpperCase().endsWith(".PDF"))
			{
				String s = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_TBID_OPEN,
						StringConstantsInterface.SPIDERTAG_TBID_CLOSE, entry.toString());
				if (null != s && s.length() > 5)
				{
					uid = s;
				}
			}
			else if (entry.toString().toUpperCase().endsWith(".TXT"))
			{
				try
				{
					if (lock.tryLock(6, TimeUnit.SECONDS))
					{
						byte[] encoded = Files.readAllBytes(entry);
						String fileContents = new String(encoded);
						String s = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_TBID_OPEN,
								StringConstantsInterface.SPIDERTAG_TBID_CLOSE, fileContents);
						if (null != s && s.length() > 5)
						{
							uid = s;
						}
						
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
			}
			return uid;
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
			return null;
		}
	}
	


	protected void sendToParser(String uid)
	{
		try
		{
		    ConfigurationBeanForFileIo_Generic fioConfig=(ConfigurationBeanForFileIo_Generic)config.getFioConfig();
			Path processedFilePath = Paths.get(fioConfig.getFileIoJobPostingPath());
			String fileToMoveString = findUniqueFile(processedFilePath, uid);
			if (null == fileToMoveString)
			{
				FioLog.getLogger().warn("Looked for: "+processedFilePath+" "+uid);
				FioLog.getLogger().warn("Not accumulating and combining files: File to move is null.");
				return;
			}
			try
			{
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					Path fileToMove = Paths.get(fileToMoveString);

					Path target = FileSystems.getDefault()
							.getPath(fioConfig.getFileIoOutBoxPath() + File.separator + fileToMove.getFileName());
					// method 1
					Files.move(fileToMove, target, StandardCopyOption.REPLACE_EXISTING);
				}

			}
			catch (Exception x)
			{

				FioLog.getLogger().error(x.getMessage(), x);
			}
			finally
			{
				lock.unlock();
			}
		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	@Override
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
						FioLog.getLogger().debug(uidCount);
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
	


	@Override
	protected void moveFile(Path p)
	{
		try
		{
			
			try
			{
				FioLog.getLogger().debug("Parking "+donePath + File.separator + p.getFileName());
				if (lock.tryLock(10, TimeUnit.SECONDS))
				{
					Path movefrom = p;
					Path target = FileSystems.getDefault().getPath(donePath + File.separator + p.getFileName());
					// method 1
					Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
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

		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}

	}

}
