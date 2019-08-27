package ikoda.netio;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.utils.ProcessStatus;

public class CollegeNetioThread extends NetIoThread
{

	public CollegeNetioThread()
	{
		NioLog.getLogger().debug("init");
	}

	private void moveToFio(Path src, Path dest)
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(src))
		{
			if (src.toString().equals(dest.toString()))
			{
				return;
			}

			int count = 0;
			for (Path entry : stream)
			{

				if (Files.isDirectory(entry))
				{
					moveToFio(src, dest);
				}
				else
				{

					try
					{
						if (lock.tryLock(6, TimeUnit.SECONDS))
						{
							Path target = FileSystems.getDefault()
									.getPath(dest.toString() + File.separator + entry.getFileName());
							// method 1
							NioLog.getLogger()
									.debug("Moving " + dest.toString() + File.separator + entry.getFileName());
							Files.move(entry, target, StandardCopyOption.REPLACE_EXISTING);
						}
					}
					catch (Exception e)
					{
						NioLog.getLogger().error(e.getMessage(), e);
					}
					finally
					{
						lock.unlock();
					}
				}
				count++;
			}
			ProcessStatus.put("Netio 12. Last Batch to Fio", count);
			if (count > 0)
			{
				continueRun = true;
				sleepTimeCalculator.pause();
			}

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	@Override
	protected void resetNetio()
	{
		try
		{
			NioLog.getLogger().debug("\n\n\n\n\n\n\n\n\n\n\nreset netio\n\n\n\n\n\n\n\n\n\n");
			super.resetNetio();

			Path dest = Paths.get(netioLiveState.getConfig().getNetioConfig().getNetIoDumpPath());

			Path src = Paths.get(netioLiveState.getConfig().getNetioConfig().getNetIoAccumulatorPath());
			moveToFio(src, dest);

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			NioLog.getLogger().error("Giving Up: Killing Thread");
			abort();
		}

	}

	@Override
	public void runNetIo(int startingRound, ConfigurationBeanParent config, ReentrantLock inlock)
			throws IKodaNetioException, IOException
	{
		NioLog.getLogger().info("CollegeNetioThread Starting Up\n\n");
		super.runNetIo(startingRound, config, inlock);
		ConfigurationBeanForFileIo_Generic fioConfig = (ConfigurationBeanForFileIo_Generic) netioLiveState.getConfig()
				.getFioConfig();

		outputPath = config.getNetioConfig().getNetIoAccumulatorPath();

		if (!Files.exists(Paths.get(outputPath)))
		{
			Files.createDirectories(Paths.get(outputPath));
		}
	}

}
