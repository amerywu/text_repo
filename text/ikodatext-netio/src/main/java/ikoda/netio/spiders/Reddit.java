package ikoda.netio.spiders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;
import ikoda.utils.Spreadsheet;

public class Reddit extends AbstractWebSite
{

	private static Map<String, String> uniqueTopBranchMap = new HashMap<String, String>();
	private final static String SLASH= "/";
	private final static String BAR= "|";
	private Integer spiderId;
	StringBuilder sb = new StringBuilder();

	public Reddit()
	{

	}
	
	private String getCategory(String uri) {
		int idx = uri.lastIndexOf(SLASH);
		if(idx > 0) {
			return uri.substring(0,idx).replace(SLASH, BAR);
		} else
		{
			return uri.replace(SLASH, BAR);
		}
	}

	protected String generatePromulgationString(Document content, UrlSrc thisUrl, int round)
	{
		String returnString = "";
		String titleText;
		Elements titleElements = content.getElementsByTag("title");
		for (Element title : titleElements)
		{

			// NioLog.getLogger().debug("Title"+title.text());
			titleText = title.text();
			if (round == 1)
			{

				returnString = StringConstantsInterface.SPIDERTAG_HOST_OPEN + thisUrl.getHostAsString()
						+ StringConstantsInterface.SPIDERTAG_HOST_CLOSE + 
						StringConstantsInterface.SPIDERTAG_URI_OPEN
						+ thisUrl.getUriAsString() + StringConstantsInterface.SPIDERTAG_URI_CLOSE
						+ StringConstantsInterface.SPIDERTAG_CATEGORY + getCategory(thisUrl.getUriAsString())+ StringConstantsInterface.SPIDERTAG_CATEGORY_CLOSE
						+ StringConstantsInterface.SPIDERTAG_TBID_OPEN + thisUrl.getUriAsString().replace(SLASH, BAR)+ StringConstantsInterface.SPIDERTAG_TBID_CLOSE
						+ StringConstantsInterface.SPIDERTAG_WEBSITE_OPEN + thisUrl.getWebSiteName()+ StringConstantsInterface.SPIDERTAG_WEBSITE_CLOSE
						+ StringConstantsInterface.SPIDERTAG_URLREPOSITORY_OPEN + thisUrl.getUrlRepository()+ StringConstantsInterface.SPIDERTAG_URLREPOSITORY_CLOSE;
			}
			if (round == 2)
			{
				returnString = thisUrl.getPromulgatedData() + titleText;
			}
			if (round == 3)
			{
				returnString = thisUrl.getPromulgatedData() + titleText;
			}
			returnString = returnString.replaceAll("\r", " ");
			returnString = returnString.replaceAll("\n", " ");
		}
		NioLog.getLogger().debug("round " + round + ": promulgating with data: " + returnString);
		return returnString;
	}

	@Override
	public String processResponse(UrlSrc thisUrl, int round, Integer inspiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCalls)
	{
		try
		{
			
			sb.append("\n\n======================\n\n\nIndeed processing: " + thisUrl.getUrl());
			NioLog.getLogger().debug("Reddit spiderId: " + inspiderId);
			NioLog.getLogger().debug("myname is : " + this.getClass().getName());
			NioLog.getLogger().debug("round is : " + round);
			NioLog.getLogger().debug("promulgationString is : " + thisUrl.getPromulgatedData());

			spiderId = inspiderId;
			Document content = Jsoup.parse(response);
			if (thisUrl.getPromulgatedData().trim().isEmpty())
			{
				thisUrl.setPromulgatedData(generatePromulgationString(content, thisUrl, round));
			}

			String returnString = "";
			if (round == 1)
			{
				NioLog.getLogger().debug(" calling r1ProcessNextRoundCalls");
				returnString = r1ProcessNextRoundCalls(content, nextRoundCalls, thisUrl);
				NioLog.getLogger().debug(" calling r1ProcessUpcomingCalls");
				returnString = r1ProcessUpcomingCalls(content, upComingCalls, thisUrl);
			}
			else if (round == 2)
			{
				returnString = r2ProcessNextRoundCalls(content, nextRoundCalls, thisUrl);
				returnString = r2ProcessUpcomingCalls(content, upComingCalls, thisUrl);

			}
			NioLog.getLogger().info(sb.toString());
			return returnString;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "FAILED";
		}
	}

	private String processRedditCall(Element le, UrlSrc thisURL)
	{
		String linkHref = le.attr("href");
		if (linkHref.startsWith("http"))
		{
			//NioLog.getLogger().debug("processRedditCall " + linkHref);
			
			return linkHref;
		}
		else
		{
			linkHref = thisURL.getHostAndProtocol() + linkHref;
			return super.cleanURL(linkHref);
		}
	}

	protected String r1ProcessNextRoundCalls(Document content, List<UrlSrc> nextRoundCall, UrlSrc thisURL)
	{
		try
		{

			boolean allowDuplicates = thisURL.getPropertyMap().getAsBoolean(UrlSrc._ALLOW_DUPLICATES_ANYWHERE, 2);

			sb.append("\n\n\nr1ProcessNextRoundCalls denyDuplicates: " + allowDuplicates);

			for (Element le : content.getAllElements())
			{
				//NioLog.getLogger().trace("Looking at "+le.attr("href"));

				//////////////////////////////////////////////////
				
					if(le.hasAttr("href"))
					{
						UrlSrc newUrlSrc = processCall(le,thisURL, thisURL.getSecondRoundLinks(),thisURL.getSecondRoundIgnore(),2);
						if(null!=newUrlSrc)
						{
							newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 2));
							
							
							nextRoundCall.add(newUrlSrc);
							//NioLog.getLogger().debug("\nr1ProcessNextRoundCalls Created nextRoundCall: " + newUrlSrc.getUrl());
							sb.append("\nr1ProcessNextRoundCalls Created nextRoundCall: " + newUrlSrc.getUrl());
							nextRoundAdd++;
						}
					}
			}

			// class="jobposting-salary"

			// NioLog.getLogger().debug("\r\n\r\n--------end job list
			// div-------\r\n\r\n");

			sb.append("\n\nNext round added: " + nextRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
			//NioLog.getLogger().debug("%%%%%%%%%%Round 1 nextRound calls  " + printCalls(nextRoundCall));
			return thisURL.getPromulgatedData();
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}
	}

	protected String r1ProcessUpcomingCalls(Document content, List<UrlSrc> upComingCalls, UrlSrc thisURL)
	{
		boolean allowDuplicates = thisURL.getPropertyMap().getAsBoolean(UrlSrc._ALLOW_DUPLICATES_ANYWHERE, 1);

		sb.append("\nr1ProcessUpcomingCalls allowDuplicates: " + allowDuplicates);

		NioLog.getLogger().debug("r1ProcessUpcomingCalls");

		// NioLog.getLogger().debug("attr incl
		// href"+content.getElementsByAttributeStarting("href").toString());


		Elements elements1 = content.getElementsByAttribute("link rel");
        NioLog.getLogger().debug("elements1 " + elements1.size());
        for (Element le : content.getAllElements())
		{
        	if(le.hasAttr("href"))
        	{	
        		Element link = le;
				//NioLog.getLogger().debug("\n\nLink is "+link.toString());
	
				UrlSrc newUrlSrc = processCall(processRedditCall(link, thisURL), thisURL, thisURL.getFirstRoundLinks(),
						thisURL.getFirstRoundIgnore(), 1);
	
				if (null == newUrlSrc)
				{
					// NioLog.getLogger().debug("Skipping ");
	
					continue;
				}
	
				//NioLog.getLogger().debug("\nAdding this round " + link.toString());
				sb.append("\nr1r1ProcessUpcomingCalls Created thisRoundCall: " + newUrlSrc.getUrl());
				thisRoundAdd++;
				upComingCalls.add(newUrlSrc);
        	}
			

		}

		sb.append("\n\nr1ProcessUpcomingCalls This round added: " + thisRoundAdd + "\n\nSkipped as Duplicate: "
				+ skippedAsDuplicate);
		NioLog.getLogger().debug("\n^^^^^^^^^^^^^^^^Round 1 upcoming  " + printCalls(upComingCalls));
		return thisURL.getPromulgatedData();

	}

	protected String r2ProcessNextRoundCalls(Document content, List<UrlSrc> nextRoundCall, UrlSrc thisURL)
	{
		try
		{

			boolean allowDuplicates = thisURL.getPropertyMap().getAsBoolean(UrlSrc._ALLOW_DUPLICATES_ANYWHERE, 3);
			// NioLog.getLogger().debug("CLASS: " + divElement.attr("class"));
			Elements elements = content.getElementsByTag("a");

			for (Element le : elements)
			{

				//////////////////////////////////////////////////

				UrlSrc newUrlSrc = processCall(processRedditCall(le, thisURL), thisURL, thisURL.getThirdRoundLinks(),
						thisURL.getThirdRoundIgnore(), 3);
				if (null == newUrlSrc)
				{
					continue;
				}

				newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 3));

				nextRoundCall.add(newUrlSrc);

				nextRoundAdd++;

				sb.append("\nround2 added " + newUrlSrc.getUrl());

			}

			// class="jobposting-salary"

			// NioLog.getLogger().debug("\r\n\r\n--------end job list
			// div-------\r\n\r\n");

			sb.append("\n\nNext round added: " + nextRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
			NioLog.getLogger().info("^^^^^^^^^^^^^^^^Round 2 nextround  " + printCalls(nextRoundCall));
			return thisURL.getPromulgatedData();
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "";
		}
	}

	protected String r2ProcessUpcomingCalls(Document content, List<UrlSrc> upComingCalls, UrlSrc thisURL)
	{
		Elements elements = content.getElementsByTag("a");

		for (Element link : elements)
		{
			UrlSrc newUrlSrc = processCall(processRedditCall(link, thisURL), thisURL, thisURL.getThirdRoundLinks(),
					thisURL.getThirdRoundIgnore(), 3);

			if (null == newUrlSrc)
			{
				continue;
			}

			newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 3));

			upComingCalls.add(newUrlSrc);
			sb.append("\nr2ProcessUpcomingCalls  Created UpcomingCall: " + newUrlSrc.getUrl());
			thisRoundAdd++;
			// NioLog.getLogger().debug(linkHref);
		}
		sb.append("\n\nThis round added: " + thisRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
		NioLog.getLogger().debug("^^^^^^^^^^^^^^^^Round 2 upcoming  " + printCalls(upComingCalls));
		return thisURL.getPromulgatedData();
	}

}
