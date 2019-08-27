package ikoda.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.Logger;

/****
 * 
 * Streaming utility
 * @author jake
 *
 */
public class StreamToSpark
{

	private class StreamingServer extends Thread
	{
		private BlockingQueue<String> eventQueue;
		private boolean runThread = true;

		// private ServerSocket serverSocket;
		// private Socket clientSocket;
		// private PrintWriter out;

		private int unchangedQueueCount = 0;

		private int lastQueueCount = 0;

		public StreamingServer(BlockingQueue<String> eventQueue)
		{
			this.eventQueue = eventQueue;
		}

		private boolean breakBlock()
		{
			lastQueueCount = eventQueue.size();
			if (eventQueue.size() == 0)
			{
				return true;
			}
			else if (eventQueue.size() == lastQueueCount)
			{
				unchangedQueueCount++;
				if (unchangedQueueCount > 300)
				{
					return true;
				}

			}
			unchangedQueueCount = 0;
			return false;

		}

		private void pause(long millis)
		{
			try
			{
				Thread.sleep(millis);
			}
			catch (Exception e)
			{
				logger.warn(e.getMessage(), e);
			}
		}

		@Override
		public void run()
		{
			logger.debug("Thread started. Starting Server on port " + port);

			while (runThread)
			{
				logger.debug("Blocking on port: " + port);
				try (ServerSocket serverSocket = new ServerSocket(port);)
				{
					serverSocket.setSoTimeout(1000);
					try (Socket clientSocket = serverSocket.accept();
							PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);)
					{
						while (!breakBlock())
						{
							String event = eventQueue.take();

							// System.out.print(" q:"+eventQueue.size());
							// logger.debug(port+" "+event);
							out.println(event);
							if (event.equals(StreamingConstants.IKODA_END_STREAM))
							{
								logger.info("\n-----------\n" + event + " Stream completed on port " + getPort()
										+ "\n---------\n");
							}
						}
						logger.info("Exiting ");
					}

					catch (Exception e)
					{
						logger.debug("Will try again. " + e.getMessage());
					}

				}
				catch (Exception e)
				{
					logger.error("Will try again. \nPort:" + port + " " + e.getMessage(), e);
					pause(1000);

				}
			}
		}

		void stopServer()
		{
			logger.info(this.getClass().getName() + " Shutting down.");

			runThread = false;

		}
	}

	private BlockingQueue<String> eventQueue = new ArrayBlockingQueue<>(100000);

	private int port = 9999;
	private Logger logger = SSm.getLogger("ikoda");

	StreamingServer ss = new StreamingServer(eventQueue);

	public StreamToSpark(int inport) throws IOException, InterruptedException
	{
		port = inport;
		logger.info("Initialized StreamToSpark with port: " + port);
		ss.start();
	}

	public int getPort()
	{
		return port;
	}

	public int queueSize()
	{
		return eventQueue.size();
	}

	public void sendLine(String s) throws InterruptedException
	{

		// logger.debug("queue size " + eventQueue.size());

		eventQueue.put(s);
	}

	public void shutdown()
	{
		ss.stopServer();
	}

}
