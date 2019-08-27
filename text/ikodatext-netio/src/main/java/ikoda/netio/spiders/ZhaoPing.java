package ikoda.netio.spiders;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class ZhaoPing extends AbstractWebSite
{

	private final static String JOBLISTING1 = "rc/clk";

	private final static String NEXTPAGE = "pd=";
	private final static String NEXTPAGE1 = "el=4";
	private final static String NEXTPAGE2 = "&p=";
	private final static String NEXTPAGEVETO = "&p=1&";
	private final static Map<String, String> pageNumbers = new HashMap<String, String>();
	private final static String[] nextPageArray =
	{ NEXTPAGE, NEXTPAGE1, NEXTPAGE2 };

	private final static String[] nextPageVetoArray =
	{ NEXTPAGEVETO };

	protected String JOBBANKROOT = "http://sou.zhaopin.com/";

	protected String JOBBANKROOT1 = "http://jobs.zhaopin.com/";

	private Integer spiderId;

	private boolean isNewPage(String linkHref)
	{
		// http://sou.zhaopin.com/jobs/searchresult.ashx?pd=7&jl=%e5%a4%a7%e8%bf%9e&sm=0&sf=0&st=99999&el=4&isadv=1&sg=d2e7b341ffca49a7a87ccd60a596bbc4&p=6,
		String p = linkHref.substring(linkHref.indexOf("p="), linkHref.length());
		NioLog.getLogger().debug("page is " + p);
		if (p.contains("&"))
		{
			p = p.substring(0, p.indexOf("&"));
		}
		String pnumber = p.replaceAll("[^0-9]", "");

		String key = spiderId.toString() + "_" + pnumber;
		NioLog.getLogger().debug("key is " + key);
		if (null == pageNumbers.get(key))
		{
			pageNumbers.put(key, key);
			return true;
		}
		NioLog.getLogger().debug("failed. Repeat of  " + key);
		return false;

	}

	private boolean isNextPageLink(String s)
	{
		for (int i = 0; i < nextPageArray.length; i++)
		{
			if (!s.contains(nextPageArray[i]))
			{
				// NioLog.getLogger().debug(nextPageArray[i]+"is absent from
				// "+s);
				return false;
			}
		}

		for (int j = 0; j < nextPageVetoArray.length; j++)
		{
			if (s.contains(nextPageVetoArray[j]))
			{
				// NioLog.getLogger().debug(nextPageArray[j]+"is contained in
				// "+s);
				return false;
			}
		}
		return true;
	}

	@Override
	public String processResponse(UrlSrc thisUrl, int round, Integer inspiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCalls)
	{

		try
		{
			// NioLog.getLogger().info("\n\n======================\n\n\nIndeed
			// processing: "+thisUrl);
			// NioLog.getLogger().debug("Indeed spiderId: "+inspiderId);
			// NioLog.getLogger().debug("I am: "+this);
			// NioLog.getLogger().debug("myname is : "+myName);

			spiderId = inspiderId;
			Document content = Jsoup.parse(response);
			Elements elements = content.getElementsByTag("a");
			Elements bodyElement = content.getElementsByTag("body");
			Elements titleElements = content.getElementsByTag("title");

			String returnString = "";
			byte[] titleText = {};

			for (Element title : titleElements)
			{

				NioLog.getLogger().debug("Title" + title.text());
				titleText = title.text().getBytes(Charset.forName("UTF-8"));

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(StringConstantsInterface.GRAB_BLOCK_START.getBytes(Charset.forName("UTF-8")));
				// outputStream.write( thisUrl.getPromulgatedDataAsByteArray()
				// );
				outputStream.write(StringConstantsInterface.GRAB_BLOCK_END.getBytes(Charset.forName("UTF-8")));
				byte[] b = outputStream.toByteArray();

				returnString = new String(b);
			}

			if (round == 1)
			{
				processRoundOne(thisUrl, elements, upComingCalls, nextRoundCalls, titleText);
			}
			else if (round == 2)
			{

				// NioLog.getLogger().debug("round2: ");
				return returnString;
			}
			else if (round == 3)
			{
				// NioLog.getLogger().debug("round3: ");
				return returnString;
			}

			return returnString;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "FAILED";
		}

	}

	private void processRoundOne(UrlSrc thisUrl, Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall,
			byte[] promulgationByteArray)
	{
		try
		{
			int nextRoundAdd = 0;
			int thisRoundAdd = 0;
			int skippedAsDuplicate = 0;

			// NioLog.getLogger().info(promulgationByteArray);
			for (Element link : links)
			{
				String linkHref = link.attr("href");

				if (!(linkHref.startsWith("http")))
				{
					linkHref = JOBBANKROOT + linkHref;
				}

				// NioLog.getLogger().info("\n\n\nround1 looking at:
				// "+linkHref+"\n\n");
				if (isNextPageLink(linkHref))
				{
					// NioLog.getLogger().info("\n\n\nround1 looking at:
					// "+linkHref+"\n\n");

					if (isDuplicate(linkHref))
					{
						// NioLog.getLogger().debug("Skipping + "+linkHref);
						skippedAsDuplicate++;
						continue;
					}
					if (isNewPage(linkHref))
					{

						UrlSrc url = new UrlSrc();
						url.setUrl(linkHref);
						url.setWebSiteName(thisUrl.getWebSiteName());

						url.setSpiderId(spiderId);
						url.setPromulgatedData(new String(promulgationByteArray));
						upComingCalls.add(url);
						NioLog.getLogger().debug("Approved for this round : " + url);
						thisRoundAdd++;
					}

				}
				if (linkHref.contains(JOBBANKROOT1))
				{

					if (isDuplicate(linkHref))
					{

						skippedAsDuplicate++;
						continue;
					}
					UrlSrc url = new UrlSrc();
					url.setUrl(linkHref);
					url.setWebSiteName(thisUrl.getWebSiteName());

					url.setSpiderId(spiderId);
					url.setPromulgatedData(new String(promulgationByteArray));
					nextRoundCall.add(url);

					nextRoundAdd++;

				}
				// NioLog.getLogger().debug(linkHref);

			}
			NioLog.getLogger().info("\n\nThis round added: " + thisRoundAdd + "\n\nNext round added: " + nextRoundAdd
					+ "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void processRoundTwo(UrlSrc thisUrl, Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall)
	{

	}

}
