package ikoda.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.Logger;

/***
 * A "quick and dirty" socket utility
 * @author jake
 *
 */
public class MomentSocket
{

	private class ServerThread extends Thread
	{

		@Override
		public void run()
		{
			logger.debug("Thread started. Starting Server on port " + port);
			try
			{
				try (ServerSocket serverSocket = new ServerSocket(port);)
				{
					TimerThread tt = new TimerThread(serverSocket);
					tt.start();

					try (Socket clientSocket = serverSocket.accept();
							PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);)
					{

						logger.debug("socket accepted");

						System.out.println(String.format("Writing \"%s\" to the socket.", response));
						logger.debug("getkeepalive " + clientSocket.getKeepAlive());
						logger.debug("isbound " + clientSocket.isBound());
						logger.debug("isconnected " + clientSocket.isConnected());
						out.println(response);
						logger.debug("line " + response);
						out.flush();
					}
					catch (Exception ex)
					{
						logger.error(ex.getMessage(), ex);
						throw new RuntimeException("Server error", ex);
					}

				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
					throw new RuntimeException("Server error", e);
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}

		}
	}

	private class TimerThread extends Thread
	{

		ServerSocket serverSocket;
		boolean runThread = true;

		public TimerThread(ServerSocket inserverSocket)
		{
			serverSocket = inserverSocket;
		}

		@Override
		public void run()
		{
			logger.debug("Timeout thread started");
			try
			{
				int count = 0;
				while (count < 60 && runThread == true)
				{
					count++;
					Thread.sleep(1000);
				}
				if (runThread)
				{
					serverSocket.close();
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}

		}

		void stopThread()
		{
			runThread = false;
		}
	}

	private int port = 9999;

	private Logger logger = SSm.getLogger(this.getClass());

	private String response = "SUCCESS";

	public MomentSocket(int inport, String inresponse) throws IOException, InterruptedException
	{
		port = inport;
		response = inresponse;

		ServerThread ss = new ServerThread();
		ss.start();
		logger.info("Initialized MomentServer with port: " + port);
	}

}
