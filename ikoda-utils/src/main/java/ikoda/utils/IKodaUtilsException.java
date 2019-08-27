package ikoda.utils;

public class IKodaUtilsException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5305526932630110839L;

	public IKodaUtilsException(String amessage)
	{
		super(amessage);

	}

	public IKodaUtilsException(String amessage, StackTraceElement[] stackTrace)
	{
		super(amessage);

		this.setStackTrace(stackTrace);

	}

	public IKodaUtilsException(String amessage, Throwable e)
	{
		super(amessage, e);

	}

	public IKodaUtilsException(Throwable e)
	{

		super(e);

	}

}
