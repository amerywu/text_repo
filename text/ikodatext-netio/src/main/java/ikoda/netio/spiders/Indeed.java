package ikoda.netio.spiders;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class Indeed extends AbstractWebSite
{
	// private final static String JOBLISTING = "pagead";
	private final static String JOBLISTING1 = "rc/clk";
	protected static String NEXTPAGE = "jobs?q=degree";

	private final static String NEXTPAGE1 = "start=";

	protected String JOBBANKROOT = "http://www.indeed.com//";

	private Integer spiderId;

	@Override
	protected boolean linkContains(String link, String linkCue)
	{
		NioLog.getLogger().debug("linkContains " + link + " cue " + linkCue);

		if (link.toUpperCase().contains(linkCue.toUpperCase()))
		{
			return true;
		}

		return false;
	}

	protected boolean linkContainsAll(String link, List<String> linkCues)
	{
		NioLog.getLogger().debug("looking at  " + link + " " + linkCues);
		for (String s : linkCues)
		{
			if (!link.toUpperCase().contains(s.toUpperCase()))
			{
				return false;
			}
		}
		NioLog.getLogger().debug(" *** true for " + link + " " + linkCues);
		return true;
	}

	protected boolean linkContainsEither(String link, List<String> linkCues)
	{
		NioLog.getLogger().debug("linkContains " + link + " cue " + linkCues);
		for (String s : linkCues)
		{
			if (link.toUpperCase().contains(s.toUpperCase()))
			{
				NioLog.getLogger().debug("true");
				return true;
			}
		}
		return false;
	}

	@Override
	public String processResponse(UrlSrc thisUrl, int round, Integer inspiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCalls)
	{

		try
		{
			NioLog.getLogger().debug("\n\n======================\n\n\nIndeed processing: " + thisUrl);
			NioLog.getLogger().debug("Indeed spiderId: " + inspiderId);

			NioLog.getLogger().debug("myname is : " + thisUrl.getWebSiteName());

			spiderId = inspiderId;
			Document content = Jsoup.parse(response);
			Elements elements = content.getElementsByTag("a");
			Elements bodyElement = content.getElementsByTag("body");
			Elements titleElements = content.getElementsByTag("title");

			String returnString = "";
			String titleText = "";

			for (Element title : titleElements)
			{

				// NioLog.getLogger().debug("Title"+title.text());
				titleText = title.text();
				if (round == 1)
				{

					returnString = StringConstantsInterface.GRAB_BLOCK_START + titleText + "  "
							+ thisUrl.getPromulgatedData() + LIST + " " + StringConstantsInterface.GRAB_BLOCK_END;
				}
				if (round == 2)
				{
					returnString = StringConstantsInterface.GRAB_BLOCK_START + titleText + " "
							+ thisUrl.getPromulgatedData() + POST + " " + StringConstantsInterface.GRAB_BLOCK_END;
				}
				returnString = returnString.replaceAll("\r", " ");
				returnString = returnString.replaceAll("\n", " ");
			}

			if (round == 1)
			{
				r1ProcessNextRoundCalls(content, nextRoundCalls, titleText, thisUrl);
				r1ProcessUpcomingCalls(elements, upComingCalls, titleText, thisUrl);

			}
			else if (round == 2)
			{

				// NioLog.getLogger().debug("round2: ");
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

	protected void processRoundTwo(Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall)
	{

	}

	protected void r1ProcessNextRoundCalls(Document content, List<UrlSrc> nextRoundCall, String promulgationString,
			UrlSrc thisURL)
	{
		try
		{
			int nextRoundAdd = 0;
			int skippedAsDuplicate = 0;
			Elements elements = content.getElementsByTag("div");
			for (Element divElement : elements)
			{
				NioLog.getLogger().debug("CLASS: " + divElement.attr("class"));
				if (divElement.attr("class").contains("jobsearch-SerpJobCard unifiedRow"))
				{
					NioLog.getLogger().debug("\n\n\nstart job list div\n\n\n");

					StringBuilder sb = new StringBuilder();
					sb.append(promulgationString);

					Elements jobtitleElements = divElement.getElementsByClass("title");
					for (Element jt : jobtitleElements)
					{
						NioLog.getLogger().debug("JTTEXT :" + jt.text());
						NioLog.getLogger().debug("JTCLASS :" + jt.attr("class"));
						sb.append(StringConstantsInterface.SPIDERTAGJOBTITLE);
						sb.append(jt.text());
						sb.append("  ");

					}
					Elements locationElements = divElement.getElementsByClass("location");
					for (Element location : locationElements)
					{
						NioLog.getLogger().debug("LOCATIONTEXT :" + location.text());
						NioLog.getLogger().debug("LOCATIONCLASS :" + location.attr("class"));
						sb.append(StringConstantsInterface.SPIDERTAGLOCATIONOPEN);
						sb.append(location.text());
						sb.append(StringConstantsInterface.SPIDERTAGLOCATIONCLOSE);
						break;
					}

					NioLog.getLogger().debug(sb.toString());
					NioLog.getLogger().debug(divElement.text());

					Elements linkelements = divElement.getElementsByTag("a");
					for (Element le : linkelements)
					{
						NioLog.getLogger().debug("LINKTEXT :" + le.text());
						NioLog.getLogger().debug("LINK :" + le.attr("href"));
						String linkHref = le.attr("href");
						if (!(linkHref.startsWith("http")))
						{
							linkHref = JOBBANKROOT + linkHref;
						}

						boolean isJobListing = false;
						if (null == thisURL.getSecondRoundLinks() || thisURL.getSecondRoundLinks().size() == 0)
						{
							isJobListing = linkContains(linkHref, JOBLISTING1);
						}
						else
						{
							isJobListing = linkContainsEither(linkHref, thisURL.getSecondRoundLinks());
						}
						if (isJobListing)
						{
							NioLog.getLogger().debug(
									"\n\n\nround1{adding for NEXT round} looking at  listing" + linkHref + "\n\n");
							if (isDuplicate(linkHref))
							{
								// NioLog.getLogger().info("Skipping +
								// "+linkHref);
								skippedAsDuplicate++;
								continue;
							}
							UrlSrc url = new UrlSrc();
							url.setUrl(linkHref);
							url.setWebSiteName(thisURL.getWebSiteName());

							url.setSpiderId(spiderId);
							url.setPromulgatedData(sb.toString());
							url.setFirstRoundIgnore(thisURL.getFirstRoundIgnore());
							url.setSecondRoundIgnore(thisURL.getSecondRoundIgnore());
							url.setFirstRoundLinks(thisURL.getFirstRoundLinks());
							url.setSecondRoundLinks(thisURL.getSecondRoundLinks());
							nextRoundCall.add(url);
							NioLog.getLogger().debug("Created nextRoundCall: " + url);
							nextRoundAdd++;

							NioLog.getLogger().debug("round1 added " + linkHref);

						}

					}

					// class="jobposting-salary"

					// NioLog.getLogger().debug("\r\n\r\n--------end job list
					// div-------\r\n\r\n");
				}

			}
			NioLog.getLogger()
					.info("\n\nNext round added: " + nextRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	protected void r1ProcessUpcomingCalls(Elements links, List<UrlSrc> upComingCalls, String promulgationString,
			UrlSrc thisURL)
	{
		try
		{
			int nextRoundAdd = 0;
			int thisRoundAdd = 0;
			int skippedAsDuplicate = 0;

			NioLog.getLogger().info(promulgationString);
			for (Element link : links)
			{
				String linkHref = link.attr("href");

				if (!(linkHref.startsWith("http")))
				{
					linkHref = JOBBANKROOT + linkHref;
				}

				boolean isJobList = false;
				if (null == thisURL.getFirstRoundLinks() || thisURL.getFirstRoundLinks().size() == 0)
				{
					isJobList = (linkContains(linkHref, NEXTPAGE) && linkContains(linkHref, NEXTPAGE1));
				}
				else
				{
					isJobList = linkContainsAll(linkHref, thisURL.getFirstRoundLinks());
				}

				// NioLog.getLogger().debug("\n\n\nround1 looking at:
				// "+linkHref+"\n\n");
				if (isJobList)
				{
					NioLog.getLogger().debug("\n\n\nround1 looking at:  " + linkHref + "\n\n");

					if (isDuplicate(linkHref))
					{
						// NioLog.getLogger().debug("Skipping + "+linkHref);
						skippedAsDuplicate++;
						continue;
					}

					UrlSrc url = new UrlSrc();
					url.setUrl(linkHref);
					url.setWebSiteName(thisURL.getWebSiteName());

					url.setSpiderId(spiderId);
					url.setPromulgatedData(promulgationString);
					url.setFirstRoundIgnore(thisURL.getFirstRoundIgnore());
					url.setSecondRoundIgnore(thisURL.getSecondRoundIgnore());
					url.setFirstRoundLinks(thisURL.getFirstRoundLinks());
					url.setSecondRoundLinks(thisURL.getSecondRoundLinks());
					upComingCalls.add(url);
					NioLog.getLogger().debug("Created UpcomingCall: " + url);
					thisRoundAdd++;

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

}
