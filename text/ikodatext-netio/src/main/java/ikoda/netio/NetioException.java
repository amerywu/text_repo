package ikoda.netio;

public class NetioException extends Exception
{

	private String message = "";

	public NetioException(String amessage)
	{
		super();

	}

	public NetioException(String amessage, StackTraceElement[] stackTrace)
	{
		super(amessage);
		message = amessage;
		this.setStackTrace(stackTrace);

	}

	public NetioException(String amessage, Throwable e)
	{
		super(amessage, e);
		message = amessage;

	}

	public NetioException(Throwable e)
	{

		super(e);

	}

	@Override
	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
