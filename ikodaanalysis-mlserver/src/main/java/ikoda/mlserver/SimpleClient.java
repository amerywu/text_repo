package ikoda.mlserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ikoda.utils.SSm;

public class SimpleClient
{
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	public void startConnection(String ip, int port) throws IOException
	{
		try
		{
		SSm.getAppLogger().info("SimpleClient starting connection on " + ip + ":" + port);
		clientSocket = new Socket(ip, port);
		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		SSm.getAppLogger().info("SimpleClient started connection");
		}
		catch(Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(),e);
			stopConnection();
		}
		
	}

	public synchronized String sendMessage(String msg) throws Exception
	{
		try
		{
			SSm.getAppLogger().info("Sending " + msg);

			out.println(msg);
			SSm.getAppLogger().info("Sent " + msg);

				String resp = in.readLine();
				System.out.println("Received " + resp);
				stopConnection();
				System.out.println("Stopped ");
				return resp;

		}
		catch (Exception e)
		{
			System.out.println(e);
			stopConnection();
			throw new Exception(e);
		}
	}

	public void stopConnection() throws IOException
	{
		System.out.println("Closing connection ");
		in.close();
		out.close();
		clientSocket.close();
	}
}
