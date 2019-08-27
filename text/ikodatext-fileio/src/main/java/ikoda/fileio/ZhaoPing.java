package ikoda.fileio;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;


public class ZhaoPing extends AbstractFileProcessor
{


	private static HashMap<String,String>jobIdMap=new HashMap<String,String>();

	ConfigurationBeanForFileIo_Generic fioConfig;
	public ZhaoPing(ConfigurationBeanParent inconfig)
	{
	    super(inconfig);
	    fioConfig= (ConfigurationBeanForFileIo_Generic)config.getFioConfig();
		JOB_POSTING = "jobs.zhaopin.com";

	}

	

	@Override
	public void processFile(Path path, ReentrantLock inlock)
	{
		try
		{
			FioLog.getLogger().info("Processing File: "+path.getFileName().toString());
			lock=inlock;

			if (path.getFileName().toString().contains(XML))
			{
				processXmlFile(path,fioConfig.getFileIoXMLPath());
				return;
			}

			byte[] encoded = Files.readAllBytes(path);
			String fileContents = new String(encoded);

			//NioLog.getLogger().debug("Not a duplicate");
				
			triage(fileContents, path);
			
			FioLog.getLogger().info("Processed File: ");

		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}
	
	@Override
	protected  void triage(String fileContent, Path p)
	{
		List<String> tags= new ArrayList<String>();
		FioLog.getLogger().debug("triage");
		if (fileContent.contains(NON_POSTING))
		{
			processNonPosting(p,fioConfig.getFileIoNonApplicablePath());
		}
		
		else if(fileContent.contains(JOB_POSTING))
		{
			FioLog.getLogger().debug("Posting");
			tags.add(StringConstantsInterface.JOBPOSTING_LABEL);
			processPosting(p,fioConfig.getFileIoJobPostingPath(), tags);
		}
		else
		{
			processNonPosting(p,fioConfig.getFileIoNonApplicablePath());
		}
	}

}
