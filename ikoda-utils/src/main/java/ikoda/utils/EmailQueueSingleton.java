package ikoda.utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Email queue
 * @author jake
 *
 */
public class EmailQueueSingleton implements Runnable
{
	private static EmailQueueSingleton emailQueueSingleton;

	public static EmailQueueSingleton getInstance()
	{
		if (null == emailQueueSingleton)
		{
			emailQueueSingleton = new EmailQueueSingleton();
		}
		return emailQueueSingleton;
	}

	private List<EmailOut> emailList = new ArrayList<EmailOut>();

	private Thread t;

	private EmailQueueSingleton()
	{

		this.start();
	}

	public void addEmailOut(EmailOut eo)
	{
		try
		{

			emailList.add(eo);
		}
		catch (Exception e)
		{
			SSm.getAppLogger().error("\n\n\n" + e.getMessage(), e);
		}
	}

	public void run()
	{
		try
		{

			while (true)
			{
				try
				{
					if (emailList.size() > 0)
					{

						EmailOut eo = emailList.remove(0);
						// SSm.getLogger().debug("Sending report.........");
						eo.send();
					}
				}
				catch (Exception e)
				{
					SSm.getAppLogger().warn("Message gas gone pfffft. Ain't coming back. " + e.getMessage(), e);

				}
				Thread.sleep(10000);
			}
		}
		catch (InterruptedException e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
			if (null == t || !t.isAlive())
			{
				this.start();
			}

		}
		catch (Exception use)
		{
			SSm.getAppLogger().error("This is Bad. Thread won't start");
			SSm.getAppLogger().error(use.getMessage(), use);
		}

	}

	public void start()
	{
		SSm.getAppLogger().info("Starting " + "UVCacheMonitor");
		if (t == null)
		{
			t = new Thread(this, "UVCacheMonitor");
			t.start();
		}
	}

}
