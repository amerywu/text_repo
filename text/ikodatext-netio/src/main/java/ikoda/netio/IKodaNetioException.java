package ikoda.netio;

public class IKodaNetioException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5305526932630110839L;

	public IKodaNetioException(String amessage)
	{
		super(amessage);

	}

	public IKodaNetioException(String amessage, StackTraceElement[] stackTrace)
	{
		super(amessage);

		this.setStackTrace(stackTrace);

	}

	public IKodaNetioException(String amessage, Throwable e)
	{
		super(amessage, e);

	}

	public IKodaNetioException(Throwable e)
	{

		super(e);

	}

}
