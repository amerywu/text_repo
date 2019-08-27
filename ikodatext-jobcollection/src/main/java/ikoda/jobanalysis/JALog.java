package ikoda.jobanalysis;

import java.io.PrintWriter;

import org.apache.log4j.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JALog
{

	private static Logger log = LogManager.getLogger("ikoda.jobcollection");

	public static Logger getLogger()
	{

		
		return log;
	}

	

}