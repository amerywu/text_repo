package ikoda.netio.spiders;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.WebSiteFactory;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class BCJobBank extends AbstractWebSite
{
	private final static String JOBBANKROOT = "http://www.jobbank.gc.ca/";
	private final static String JOBLISTING = "jobposting.do";
	private final static String NEXTPAGE = "job_search_results.do";
	private final static String ROUND2TEXT = "View the full";

	private Integer spiderId;

	@Override
	public String processResponse(UrlSrc thisUrl, int round, Integer inspiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall)
	{

		try
		{
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
				returnString = StringConstantsInterface.GRAB_BLOCK_START + title.text()
						+ StringConstantsInterface.GRAB_BLOCK_END;
			}
			if (round == 1)
			{
				processRoundOne(elements, upComingCalls, nextRoundCall, titleText);
			}
			else if (round == 2)
			{

				processRoundTwo(bodyElement, upComingCalls, nextRoundCall, titleText);
			}
			else if (round == 3)
			{
				NioLog.getLogger().debug("round3: ");
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

	private void processRoundOne(Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall,
			String promulgationString)
	{
		try
		{
			for (Element link : links)
			{
				String linkHref = link.attr("href");
				String linkText = link.text();

				if (linkHref.contains(NEXTPAGE))
				{
					UrlSrc url = new UrlSrc();
					url.setUrl(JOBBANKROOT + linkHref);
					url.setWebSiteName(WebSiteFactory.BCJOBBANK);

					url.setSpiderId(spiderId);
					url.setPromulgatedData(promulgationString);
					upComingCalls.add(url);
					NioLog.getLogger().debug("round1 added " + JOBBANKROOT + linkHref);
				}
				if (linkHref.contains(JOBLISTING))
				{
					UrlSrc url = new UrlSrc();
					url.setUrl(JOBBANKROOT + linkHref);
					url.setWebSiteName(WebSiteFactory.BCJOBBANK);

					url.setSpiderId(spiderId);
					url.setPromulgatedData(promulgationString);
					nextRoundCall.add(url);
					NioLog.getLogger().debug("round2 added " + JOBBANKROOT + linkHref);
				}
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void processRoundTwo(Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall,
			String promulgationString)
	{
		try
		{
			for (Element wholebody : links)
			{
				Elements jobdivs = wholebody.getElementsByClass("JobPostingSection");
				for (Element jobdiv : jobdivs)
				{
					Elements elements = jobdiv.getElementsByTag("a");
					{
						for (Element link : elements)
						{
							String linkHref = link.attr("href");
							String linkText = link.text();
							if (linkText.contains(ROUND2TEXT))
							{
								if (!(linkHref.contains("emploiquebec")))
								{
									UrlSrc url = new UrlSrc();
									url.setUrl(linkHref);
									url.setWebSiteName(WebSiteFactory.BCJOBBANK);
									url.setSpiderId(spiderId);

									url.setPromulgatedData(promulgationString);
									nextRoundCall.add(url);
								}
								NioLog.getLogger().debug("round2 added href:   " + linkHref);
								NioLog.getLogger().debug("round2 because text:   " + linkText);
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}
}
