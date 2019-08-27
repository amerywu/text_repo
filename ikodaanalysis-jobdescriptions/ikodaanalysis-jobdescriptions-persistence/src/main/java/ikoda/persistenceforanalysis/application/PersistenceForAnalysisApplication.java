package ikoda.persistenceforanalysis.application;

import ikoda.persistence.application.PLog;

public class PersistenceForAnalysisApplication
{

	public static void main(String[] args) throws Exception
	{
		PLog.getLogger().info(" PersistenceApplication main\n\nNothing here right now. I'm an empty shell\n\n");

	}

	private String message;

	public PersistenceForAnalysisApplication()
	{

	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

}
