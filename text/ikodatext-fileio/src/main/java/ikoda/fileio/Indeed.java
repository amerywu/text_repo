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


public class Indeed extends AbstractFileProcessor
{



    ConfigurationBeanForFileIo_Generic fioConfig;

	public Indeed(ConfigurationBeanParent inconfig)
	{
	    super(inconfig);
	    fioConfig= (ConfigurationBeanForFileIo_Generic)config.getFioConfig();
		RESULTS_PAGE = "/jobs?";
		JOB_POSTING = "/pagead/";
		JOB_POSTING1 = "/rc/clk?";
		NON_POSTING = "View the full job posting";
	}

	private boolean isDuplicateIndeed(String s)
	{
		try
		{
			
			int codeStart=s.indexOf("jk=");
			
			if(codeStart>0)
			{
				String jobId=s.substring(codeStart, codeStart+20);
				//NioLog.getLogger().debug("\n\nid: "+jobId);
				return isDuplicate(jobId);
			}
			else
			{
				codeStart=s.indexOf("&ad=");
				if(codeStart>0)
				{
					String jobId=s.substring(codeStart, codeStart+100);
					return isDuplicate(jobId);
				}
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
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
			if(isDuplicateIndeed(fileContents))
			{

				return;
			}
	
				
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
		else if (fileContent.contains(RESULTS_PAGE))
		{
			FioLog.getLogger().debug("ResultsPage");
			tags.add(StringConstantsInterface.RESULTLIST_LABEL);
			//processResultsPage(p,tags);
			processNonPosting(p,fioConfig.getFileIoNonApplicablePath());
		}
		else if(fileContent.contains(JOB_POSTING)||fileContent.contains(JOB_POSTING1))
		{
			FioLog.getLogger().debug("Posting");
			tags.add(StringConstantsInterface.JOBPOSTING_LABEL);
			processPosting(p,fioConfig.getFileIoJobPostingPath(),tags);
		}
		else
		{
			processUnidentified(p, fioConfig.getFileIoUndeterminedPath());
		}
	}

}
