package ikoda.fileio;

import java.io.File;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

import ikoda.netio.config.ConfigurationBeanParent;

public class IndeedUK extends Indeed
{
	private final static String XML = "xml_";



	


	public IndeedUK(ConfigurationBeanParent inconfig)
	{
		super( inconfig);
		 JOB_POSTING = "/pagead/";
		JOB_POSTING1 = "/rc/clk?";
		RESULTS_PAGE = "/jobs?";
	}



}
