package ikoda.netio.spiders;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class SimplyHired extends AbstractWebSite
{
	private final static String JOBLISTING = "/a/job-details/?";
	private final static String NEXTPAGE = "/search?q=degree";

	private final static String NEXTPAGE1 = "pn=";
	protected String JOBBANKROOT = "http://www.simplyhired.com/";

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
				r1ProcessNextRoundCalls(thisUrl, content, nextRoundCall, titleText);
				r1ProcessThisRoundUpcoming(thisUrl, content, upComingCalls, titleText);
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

	private void processRoundTwo(Elements links, List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall)
	{

	}

	private void r1ProcessNextRoundCalls(UrlSrc thisUrl, Document content, List<UrlSrc> nextRoundCall,
			String promulgationString)
	{
		try
		{
			int nextRoundAdd = 0;
			int skippedAsDuplicate = 0;
			Elements elements = content.getElementsByTag("div");
			for (Element divElement : elements)
			{
				NioLog.getLogger().debug("CLASS: " + divElement.attr("class"));
				if (divElement.attr("class").toLowerCase().contains("card js-job"))
				{
					NioLog.getLogger().debug("\n\n\nstart job list div\n\n\n");

					Elements spanelements = divElement.getElementsByTag("span");
					for (Element span : spanelements)
					{
						NioLog.getLogger().debug("SPANTEXT :" + span.text());
						NioLog.getLogger().debug("SPANCLASS :" + span.attr("class"));

					}

					StringBuilder sb = new StringBuilder();
					sb.append(promulgationString);

					Elements salaryElements = divElement.getElementsByClass("jobposting-salary");
					for (Element salary : salaryElements)
					{
						NioLog.getLogger().debug("SALARYTEXT :" + salary.text());
						NioLog.getLogger().debug("SALARYCLASS :" + salary.attr("class"));
						if (salary.text().toUpperCase().contains("YEAR"))
						{
							sb.append(StringConstantsInterface.SPIDERTAGSALARYOPEN);
							sb.append(salary.text());
							sb.append(StringConstantsInterface.SPIDERTAGSALARYCLOSE);
							break;
						}

					}
					Elements locationElements = divElement.getElementsByClass("jobposting-location");
					for (Element location : locationElements)
					{
						NioLog.getLogger().debug("LOCATIONTEXT :" + location.text());
						NioLog.getLogger().debug("LOCATIONCLASS :" + location.attr("class"));
						sb.append(StringConstantsInterface.SPIDERTAGLOCATIONOPEN);
						sb.append(location.text());
						sb.append(StringConstantsInterface.SPIDERTAGLOCATIONCLOSE);
						break;
					}

					Elements jtElements = divElement.getElementsByClass("jobposting-title");
					for (Element jt : jtElements)
					{
						NioLog.getLogger().debug("LOCATIONTEXT :" + jt.text());
						NioLog.getLogger().debug("LOCATIONCLASS :" + jt.attr("class"));
						sb.append(StringConstantsInterface.SPIDERTAGJOBTITLE);
						sb.append(jt.text());
						sb.append(" ");
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
						if (linkHref.contains(JOBLISTING))
						{
							NioLog.getLogger().debug("\n\n\nround1 looking at  listing" + linkHref + "\n\n");

							if (isDuplicate(linkHref))
							{
								NioLog.getLogger().info("Skipping + " + linkHref);
								skippedAsDuplicate++;
								continue;
							}
							if (linkHref.startsWith("http"))
							{
								UrlSrc url = new UrlSrc();
								url.setUrl(linkHref);
								url.setWebSiteName(thisUrl.getWebSiteName());

								url.setSpiderId(spiderId);
								url.setPromulgatedData(sb.toString());
								nextRoundCall.add(url);
								nextRoundAdd++;

							}
							else
							{
								UrlSrc url = new UrlSrc();
								url.setUrl(JOBBANKROOT + linkHref);
								url.setWebSiteName(thisUrl.getWebSiteName());

								url.setSpiderId(spiderId);
								url.setPromulgatedData(sb.toString());
								nextRoundCall.add(url);
								nextRoundAdd++;
							}

							NioLog.getLogger().debug("round1 added to next round: " + linkHref);

						}

					}

					// class="jobposting-salary"

					NioLog.getLogger().debug("\r\n\r\n--------end job list div-------\r\n\r\n");
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

	private void r1ProcessThisRoundUpcoming(UrlSrc thisUrl, Document content, List<UrlSrc> upComingCalls,
			String promulgationString)
	{
		try
		{
			int thisRoundAdd = 0;

			int skippedAsDuplicate = 0;
			NioLog.getLogger().debug("r1ProcessThisRoundUpcoming");

			Elements elements = content.getElementsByTag("div");
			for (Element divElement : elements)
			{
				if (divElement.attr("class").toLowerCase().contains("search-pagination"))
				{
					Elements linkelements = divElement.getElementsByTag("a");

					for (Element link : linkelements)
					{
						String linkHref = link.attr("href");

						String linkText = link.text();

						if (!(linkHref.startsWith("http")))
						{
							linkHref = JOBBANKROOT + linkHref;
						}

						if (linkHref.contains(NEXTPAGE) && linkHref.contains(NEXTPAGE1))
						{
							NioLog.getLogger().debug("\n\n\nround1 looking at  " + linkHref + "\n\n");
							if (isDuplicate(linkHref))
							{
								NioLog.getLogger().debug("Skipping + " + linkHref);
								skippedAsDuplicate++;
								continue;
							}

							if (linkHref.startsWith("http"))
							{
								UrlSrc url = new UrlSrc();
								url.setUrl(linkHref);
								url.setWebSiteName(thisUrl.getWebSiteName());

								url.setSpiderId(spiderId);
								url.setPromulgatedData(promulgationString);
								upComingCalls.add(url);
								thisRoundAdd++;
							}
							else
							{
								UrlSrc url = new UrlSrc();
								url.setUrl(JOBBANKROOT + linkHref);
								url.setWebSiteName(thisUrl.getWebSiteName());

								url.setSpiderId(spiderId);
								url.setPromulgatedData(promulgationString);

								upComingCalls.add(url);
								thisRoundAdd++;
							}

						}

					}
				}

				// NioLog.getLogger().debug(linkHref);
				// NioLog.getLogger().debug(linkText);

			}
			NioLog.getLogger()
					.info("\n\nThis round added: " + thisRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

}
