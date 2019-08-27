package ikoda.fileio;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;


public class BCJobBank extends AbstractFileProcessor
{

	
	
	private final static String RESULTS_PAGE = "job_search_results.do";
	private final static String NON_POSTING = "View the full job posting";
	
	ConfigurationBeanForFileIo_Generic fioConfig;

	public BCJobBank(ConfigurationBeanParent inconfig)
	{
	    super(inconfig);
	    fioConfig= (ConfigurationBeanForFileIo_Generic)config.getFioConfig();
	}

	@Override
	public void processFile(Path path, ReentrantLock inlock)
	{
		try
		{
			lock=inlock;
			if (path.getFileName().toString().contains(XML))
			{
				processXmlFile(path,config.getFioConfig().getFileIoXMLPath());
				return;
			}

			byte[] encoded = Files.readAllBytes(path);
			String fileContents = new String(encoded);
			triage(fileContents, path);

		}
		catch (Exception e)
		{
			FioLog.getLogger().error(e.getMessage(), e);
		}
	}

	
	@Override
	protected void triage(String fileContent, Path p)
	{
		List<String> tags= new ArrayList<String>();
		if (fileContent.contains(NON_POSTING))
		{
			
			processNonPosting(p,fioConfig.getFileIoNonApplicablePath());
		}
		if (fileContent.contains(RESULTS_PAGE))
		{
			tags.add(StringConstantsInterface.RESULTLIST_LABEL);
			processResultsPage(p,fioConfig.getFileIoResultsPath(),tags);
		}
		else
		{
			tags.add(StringConstantsInterface.JOBPOSTING_LABEL);
			processPosting(p,fioConfig.getFileIoJobPostingPath(),tags);
		}
	}

}
