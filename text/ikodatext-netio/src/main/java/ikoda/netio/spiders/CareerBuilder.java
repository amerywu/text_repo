package ikoda.netio.spiders;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class CareerBuilder extends AbstractWebSite
{
	private final static String JOBLISTING = "/job/";
	private final static String JOBLISTING1 = "rc/clk";
	private final static String NEXTPAGE = "page_number=";

	private final static String IGNORE1 = "sort=";
	protected String JOBBANKROOT = "http://www.careerbuilder.com/";

	private Integer spiderId;

	@Override
	public String processResponse(UrlSrc thisUrl, int round, Integer inspiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall)
	{

		try
		{
			NioLog.getLogger().info("Indeed processing");
			spiderId = inspiderId;
			Document content = Jsoup.parse(response);
			Elements elements = content.getElementsByTag("a");
			Elements bodyElement = content.getElementsByTag("body");
			Elements titleElements = content.getElementsByTag("title");

			String returnString = "";
			String titleText = "";

			for (Element title : titleElements)
			{

				NioLog.getLogger().debug("Title" + title.text());
				titleText = title.text();
				returnString = StringConstantsInterface.GRAB_BLOCK_START + titleText + "\r\n  "
						+ thisUrl.getPromulgatedData() + "\r\n" + StringConstantsInterface.GRAB_BLOCK_END;
			}

			if (round == 1)
			{
				processRoundOne(thisUrl, elements, upComingCalls, nextRoundCall, titleText);
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
			String promulgationString)
	{

		try
		{
			NioLog.getLogger().debug("processRoundOne");
			int nextRoundAdd = 0;
			int thisRoundAdd = 0;
			int skippedAsDuplicate = 0;
			for (Element link : links)
			{
				String linkHref = link.attr("href");

				String linkText = link.text();

				if (linkHref.contains(IGNORE1))
				{
					NioLog.getLogger().debug("Ignoring + " + linkHref);
					continue;
				}

				if (!(linkHref.startsWith("http")))
				{
					linkHref = JOBBANKROOT + linkHref;

				}

				if (linkHref.contains(NEXTPAGE))
				{
					if (isDuplicate(linkHref))
					{
						skippedAsDuplicate++;
						continue;
					}

					NioLog.getLogger().debug("\n\n\nround1 looking at  " + linkHref + "\n\n");

					UrlSrc url = new UrlSrc();
					url.setUrl(linkHref);
					url.setWebSiteName(thisUrl.getWebSiteName());

					url.setSpiderId(spiderId);
					url.setPromulgatedData(promulgationString);
					upComingCalls.add(url);
					thisRoundAdd++;

				}
				if (linkHref.contains(JOBLISTING) || linkHref.contains(JOBLISTING1))
				{
					if (isDuplicate(linkHref))
					{
						NioLog.getLogger().debug("Skipping + " + linkHref);
						skippedAsDuplicate++;
						continue;
					}

					NioLog.getLogger().debug("\n\n\nround1 looking at  listing: " + linkHref + "\n\n");

					UrlSrc url = new UrlSrc();
					url.setUrl(linkHref);
					url.setWebSiteName(thisUrl.getWebSiteName());

					url.setSpiderId(spiderId);
					url.setPromulgatedData(promulgationString);
					nextRoundCall.add(url);
					nextRoundAdd++;

					NioLog.getLogger().debug("round1 added " + linkHref);

				}
				// NioLog.getLogger().debug(linkHref);
				// NioLog.getLogger().debug(linkText);

			}
			NioLog.getLogger().info("\n\nThis round added: " + thisRoundAdd + "\n\nNext round added: " + nextRoundAdd
					+ "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void processRoundTwo(Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall)
	{

	}

}
