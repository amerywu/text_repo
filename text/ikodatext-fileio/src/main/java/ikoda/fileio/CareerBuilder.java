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


public class CareerBuilder extends AbstractFileProcessor
{



	protected static String RESULTS_PAGE1="&location=" ;
	ConfigurationBeanForFileIo_Generic fioConfig;

	public CareerBuilder(ConfigurationBeanParent inconfig)
	{
	    super(inconfig);
	    fioConfig= (ConfigurationBeanForFileIo_Generic)config.getFioConfig();
		RESULTS_PAGE ="page_number=" ;
		RESULTS_PAGE1="&location=" ;
		JOB_POSTING = "/job/";
		JOB_POSTING1 = "/rc/clk?";
		NON_POSTING = "View the full job posting";
		
	}

	@Override
	protected boolean isDuplicate(String s)
	{
		try
		{
			
			int codeStart=s.indexOf("/job/J");
			int codeEnd=s.indexOf("ipath");
			
			if(codeStart>0)
			{
				String jobId=s.substring(codeStart, codeEnd);
				//NioLog.getLogger().debug("\n\nid: "+jobId);
				if(null==duplicateMap.get(jobId))
				{
					//NioLog.getLogger().debug("Putting "+jobId);
				    duplicateMap.put(jobId,jobId);
				    return false;
				}
				else
				{
					FioLog.getLogger().warn("DUPLICATE: "+jobId);
					return true;
				}
			}
		
				
			return false;
			
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
			if(isDuplicate(fileContents))
			{
				FioLog.getLogger().debug("Bailing. This is a duplicate");
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
		else if (fileContent.contains(RESULTS_PAGE)||fileContent.contains(RESULTS_PAGE1))
		{
			FioLog.getLogger().debug("ResultsPage");
			tags.add(StringConstantsInterface.RESULTLIST_LABEL);
			processResultsPage(p,fioConfig.getFileIoResultsPath(),tags);
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
