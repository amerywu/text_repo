package ikoda.nlp.analysis;


import ikoda.utils.IKodaUtilsException;
import ikoda.utils.ProcessStatus;

public abstract class FileAnalyzerThreadManagement extends Thread
{

	protected boolean designatedForAbort = false;
	protected long fileProcessStartTime;
	protected boolean ready = false;
	protected int sleepTime = 1000;
	protected int baseSleepTime = 100;
	protected static boolean stopDependsOnFileio;
	protected static boolean upstreamThreadDone = true;
	protected int threadId = 0;
	protected static boolean abort = false;
    protected boolean restart = false;
    protected boolean continueRun = true;

	public FileAnalyzerThreadManagement()
	{
		super();
	}

	public FileAnalyzerThreadManagement(Runnable arg0)
	{
		super(arg0);
	}

	public FileAnalyzerThreadManagement(String arg0)
	{
		super(arg0);
	}

	public FileAnalyzerThreadManagement(ThreadGroup arg0, Runnable arg1)
	{
		super(arg0, arg1);
	}

	public FileAnalyzerThreadManagement(ThreadGroup arg0, String arg1)
	{
		super(arg0, arg1);
	}

	public FileAnalyzerThreadManagement(Runnable arg0, String arg1)
	{
		super(arg0, arg1);
	}

	public FileAnalyzerThreadManagement(ThreadGroup arg0, Runnable arg1, String arg2)
	{
		super(arg0, arg1, arg2);
	}

	public FileAnalyzerThreadManagement(ThreadGroup arg0, Runnable arg1, String arg2, long arg3)
	{
		super(arg0, arg1, arg2, arg3);
	}

	public synchronized void abort()
	{
	    
	    TALog.getLogger().warn("\n\nAbort called. Exit at next loop iteration\n\n");
	    TALog.getLogger().warn("\n\nSTACKTRACE", new Exception());
	    ProcessStatus.incrementStatus("TA Count aborts called");
	    abort = true;
	}
	
	protected String truncate(String s,int maxLength)
	{
		if(s.length()>maxLength)
		{
			return s.substring(0, maxLength);
		}
		return s;
	}

	protected boolean isAborted()
	{
	    return abort;
	}

	public synchronized void doAbort()
	{
	
	
	    continueRun = false;
	}



	public boolean isRestart()
	{
		return restart;
	}

	public void restart()
	{
	    TALog.getLogger().warn("\n\nRestart called. Exit at next loop iteration\n\n");
	    TALog.getLogger().warn("\n\nSTACKTRACE", new Exception());
	    ProcessStatus.incrementStatus("TA Count aborts called");
		this.restart = true;
	}

	public int getThreadId()
	{
	    return threadId;
	}

	public boolean isContinueRun()
	{
	    return continueRun;
	}

	public boolean isDesignatedForAbort()
	{
	    return designatedForAbort;
	}

	public boolean isStopRun()
	{
	    return upstreamThreadDone;
	}

	public boolean isUpstreamThreadDone()
	{
	    return upstreamThreadDone;
	}

	public void setDesignatedForAbort(boolean designatedForAbort)
	{
	    this.designatedForAbort = designatedForAbort;
	}

	protected void setSleepTime(int sleepTime)
	{
	    this.sleepTime = sleepTime;
	}

	public void setUpstreamThreadDone(boolean stopRun)
	{
	    this.upstreamThreadDone = stopRun;
	}
	
	public abstract long getFileProcessTime() throws IKodaUtilsException;

}