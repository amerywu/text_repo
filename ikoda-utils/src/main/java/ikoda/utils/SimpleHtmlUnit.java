package ikoda.utils;

import java.util.HashMap;
import java.util.Iterator;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/****
 * Simple web page reader
 * @author jake
 *
 */
public class SimpleHtmlUnit
{

	String url;
	int port;

	public SimpleHtmlUnit()
	{

	}

	public SimpleHtmlUnit(String inurl, int inport)
	{
		url = inurl;
		port = inport;

		System.out.println("connecting to " + inurl + ":" + inport);

	}

	public String getAsText(String url) throws IKodaUtilsException
	{
		final WebClient webClient = new WebClient();
		try
		{

			HtmlPage page = webClient.getPage(url);
			return page.asText();
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
		finally
		{
			webClient.close();
		}

	}

	public String getAsText(String uri, HashMap<String, String> params) throws IKodaUtilsException
	{
		return getAsText(url, port, uri, params, false);
	}

	public String getAsText(String uri, HashMap<String, String> params, boolean https) throws IKodaUtilsException
	{
		return getAsText(url, port, uri, params, https);
	}

	public String getAsText(String inurl, int inport, String uri, HashMap<String, String> params)
			throws IKodaUtilsException
	{
		return getAsText(inurl, inport, uri, params, false);
	}

	public String getAsText(String url, int port, String uri, HashMap<String, String> params, boolean https)
			throws IKodaUtilsException
	{
		final WebClient webClient = new WebClient();
		try
		{

			StringBuilder sb = new StringBuilder();
			Iterator<String> itr = params.keySet().iterator();
			while (itr.hasNext())
			{
				String param = itr.next();
				String value = params.get(param);
				sb.append(param);
				sb.append("=");
				sb.append(value);
				if (itr.hasNext())
				{
					sb.append("&");
				}
			}

			StringBuilder sbRequest = new StringBuilder();
			if (https)
			{
				sbRequest.append("https://");
			}
			else
			{
				sbRequest.append("http://");
			}
			sbRequest.append(url);
			sbRequest.append(":");
			sbRequest.append(port);
			sbRequest.append("/");
			sbRequest.append(uri);
			if (!params.isEmpty())
			{
				sbRequest.append("?");
				sbRequest.append(sb.toString());
			}
			HtmlPage page = webClient.getPage(sbRequest.toString());
			return page.asText();
		}
		catch (Exception e)
		{
			throw new IKodaUtilsException(e.getMessage(), e);
		}
		finally
		{
			webClient.close();
		}

	}
}
