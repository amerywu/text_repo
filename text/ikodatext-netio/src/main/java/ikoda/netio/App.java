package ikoda.netio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Hello world!
 *
 */
public class App
{

	public static void main(String[] args)
	{
		String resource, host, file;
		int slashPos;

		resource = "www.jobbank.gc.ca";
		slashPos = resource.indexOf('/'); // find host/file separator
		if (slashPos < 0)
		{
			resource = resource + "/";
			slashPos = resource.indexOf('/');
		}
		file = resource.substring(slashPos); // isolate host and file parts
		host = resource.substring(0, slashPos);
		System.out.println("Host to contact: '" + host + "'");
		System.out.println("File to fetch : '" + file + "'");

		SocketChannel channel = null;

		try
		{
			Charset charset = Charset.forName("ISO-8859-1");
			CharsetDecoder decoder = charset.newDecoder();
			CharsetEncoder encoder = charset.newEncoder();

			ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
			CharBuffer charBuffer = CharBuffer.allocate(1024);

			InetSocketAddress socketAddress = new InetSocketAddress(host, 80);
			channel = SocketChannel.open();
			channel.connect(socketAddress);

			String request = "GET " + file + " \r\n\r\n";
			channel.write(encoder.encode(CharBuffer.wrap(request)));

			while ((channel.read(buffer)) != -1)
			{
				buffer.flip();
				decoder.decode(buffer, charBuffer, false);
				charBuffer.flip();
				System.out.println(charBuffer);
				buffer.clear();
				charBuffer.clear();
			}
		}
		catch (UnknownHostException e)
		{
			System.err.println(e);
		}
		catch (IOException e)
		{
			System.err.println(e);
		}
		finally
		{
			if (channel != null)
			{
				try
				{
					channel.close();
				}
				catch (IOException ignored)
				{
				}
			}
		}

		System.out.println("\nDone.");
	}

	public static void oldMain(String[] args)
	{
		SocketChannel channel = null;
		FileChannel localFile = null;

		try
		{
			System.out.println("Retrieving................");
			URL u = new URL("http://www.thinkadvisor.com");
			String host = u.getHost();
			int port = 80;
			String file = "/";

			SocketAddress remote = new InetSocketAddress(host, port);
			channel = SocketChannel.open(remote);
			FileOutputStream out = new FileOutputStream("yourfile.htm");
			localFile = out.getChannel();

			String request = "GET " + file + " HTTP/1.1\r\n" + "User-Agent: HTTPGrab\r\n" + "Accept: text/*\r\n"
					+ "Connection: close\r\n" + "Host: " + host + "\r\n" + "\r\n";

			ByteBuffer header = ByteBuffer.wrap(request.getBytes("US-ASCII"));
			channel.write(header);

			ByteBuffer buffer = ByteBuffer.allocate(8192);
			while (channel.read(buffer) != -1)
			{
				buffer.flip();
				localFile.write(buffer);
				buffer.clear();
			}

			localFile.close();
			channel.close();
			System.out.println("Done.");
		}
		catch (Exception e)
		{
			System.out.println(e);
			if (null != localFile)
			{
				try
				{
					localFile.close();
				}
				catch (Exception re)
				{
					System.out.println(re);
				}
			}

			System.out.println(e);
			if (null != channel)
			{
				try
				{
					channel.close();
				}
				catch (Exception re)
				{
					System.out.println(re);
				}
			}
		}
	}

}
