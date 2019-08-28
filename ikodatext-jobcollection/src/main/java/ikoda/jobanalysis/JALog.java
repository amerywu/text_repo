package ikoda.jobanalysis;


import org.apache.logging.log4j.*;

public class JALog
{

	private static Logger log = LogManager.getLogger("ikoda.jobcollection");

	public static Logger getLogger()
	{

		
		return log;
	}

	

}