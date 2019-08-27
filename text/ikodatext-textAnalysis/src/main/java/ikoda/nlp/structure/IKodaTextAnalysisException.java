package ikoda.nlp.structure;

public class IKodaTextAnalysisException extends Exception
{

    /**
     * 
     */
    private static final long serialVersionUID = 5305526932630110839L;

    public IKodaTextAnalysisException(String amessage)
    {
        super(amessage);

    }

    public IKodaTextAnalysisException(String amessage, StackTraceElement[] stackTrace)
    {
        super(amessage);

        this.setStackTrace(stackTrace);

    }

    public IKodaTextAnalysisException(String amessage, Throwable e)
    {
        super(amessage, e);

    }

    public IKodaTextAnalysisException(Throwable e)
    {

        super(e);

    }

}
