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

public class CollegePrograms extends AbstractWebSite
{

	private static Map<String, String> uniqueTopBranchMap = new HashMap<String, String>();
	private Integer spiderId;
	StringBuilder sb = new StringBuilder();

	public CollegePrograms()
	{

	}

	protected String generatePromulgationString(Document content, UrlSrc thisUrl, int round)
	{

		// NioLog.getLogger().debug("generatePromulgationString thisUrl "+
		// thisUrl.getHostAndProtocol()+thisUrl.getUriAsString());

		String returnString = "";
		String titleText = "";
		Elements titleElements = content.getElementsByTag("title");

		for (Element title : titleElements)
		{
			titleText = titleText + title.text();

		}
		if (round == 1)
		{
			returnString = StringConstantsInterface.SPIDERTAG_HOST_OPEN + thisUrl.getHostAsString()
					+ StringConstantsInterface.SPIDERTAG_HOST_CLOSE + StringConstantsInterface.SPIDERTAG_URI_OPEN
					+ thisUrl.getUriAsString() + StringConstantsInterface.SPIDERTAG_URI_CLOSE
					+ StringConstantsInterface.SPIDERTAG_CATEGORY + titleText
					+ StringConstantsInterface.SPIDERTAG_CATEGORY_CLOSE + StringConstantsInterface.SPIDERTAG_TBID_OPEN
					+ String.valueOf(UUID.randomUUID()) + StringConstantsInterface.SPIDERTAG_TBID_CLOSE
					+ StringConstantsInterface.SPIDERTAG_WEBSITE_OPEN + thisUrl.getWebSiteName()
					+ StringConstantsInterface.SPIDERTAG_WEBSITE_CLOSE
					+ StringConstantsInterface.SPIDERTAG_URLREPOSITORY_OPEN + thisUrl.getUrlRepository()
					+ StringConstantsInterface.SPIDERTAG_URLREPOSITORY_CLOSE;
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
		NioLog.getLogger().debug("generatePromulgationString Round " + round + ": " + returnString);
		return returnString;
	}

	private void checkEmptyPromulgationString(UrlSrc thisUrl, int round, Document content)
	{

		if (content.text().contains(StringConstantsInterface.SPIDERTAG_HOST_OPEN))
		{

			return;
		}
		else
		{
			thisUrl.setPromulgatedData(generatePromulgationString(content, thisUrl, round));
		}

	}

	@Override
	public String processResponse(UrlSrc thisUrl, int round, Integer inspiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCalls)
	{
		try
		{

			NioLog.getLogger().info("\n\n======================\n\nIndeed processing: \n" + thisUrl.getFileAsString()
					+ "\n" + thisUrl.getUrl());
			sb.append("\n\n======================\n\n\nIndeed processing: " + thisUrl);

			spiderId = inspiderId;
			Document content = Jsoup.parse(response);
			checkEmptyPromulgationString(thisUrl, round, content);

			String returnString = "";
			if (round == 1)
			{

				returnString = r1ProcessUpcomingCalls(content, upComingCalls, thisUrl);
				r1ProcessNextRoundCalls(content, nextRoundCalls, thisUrl);
			}
			else if (round == 2)
			{
				r2ProcessUpcomingCalls(content, upComingCalls, thisUrl);
				returnString = r2ProcessNextRoundCalls(content, nextRoundCalls, thisUrl);

			}
			else if (round == 3)
			{
				returnString = r3ProcessUpcomingCalls(content, upComingCalls, thisUrl);
			}

			// NioLog.getLogger().debug(sb.toString());
			NioLog.getLogger().debug("\n\n--------------------\n\nprocessRespnse done.\nreturning " + returnString
					+ "\n\n--------------------");
			return returnString;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "FAILED";
		}
	}

	private void r1ProcessNextRoundCallElements(Document content, Elements elements, List<UrlSrc> nextRoundCall,
			UrlSrc thisURL, boolean isRestful, int restfulDepth)
	{
		try
		{
			for (Element le : elements)
			{

				//////////////////////////////////////////////////

				UrlSrc newUrlSrc = processCall(le, thisURL, thisURL.getSecondRoundLinks(),
						thisURL.getSecondRoundIgnore(), 2, restfulDepth);

				if (null == newUrlSrc)
				{
					continue;
				}

				newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 2));

				nextRoundCall.add(newUrlSrc);
				sb.append("\nr1ProcessNextRoundCalls Created nextRoundCall: " + newUrlSrc.getUrl());
				nextRoundAdd++;

			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

		}

	}

	protected String r1ProcessNextRoundCalls(Document content, List<UrlSrc> nextRoundCall, UrlSrc thisURL)
	{
		try
		{
			NioLog.getLogger().debug("r1ProcessNextRoundCalls");
			boolean isRestful = thisURL.getPropertyMap().getAsBoolean(UrlSrc._AS_RESTFUL);

			Elements elements = content.getElementsByTag("a");
			NioLog.getLogger().debug("r1ProcessNextRoundCalls elements size " + elements.size());

			NioLog.getLogger().debug("nextRoundAdd " + nextRoundAdd);
			if (isRestful)
			{
				r1ProcessNextRoundCallElements(content, elements, nextRoundCall, thisURL, isRestful, 2);
			}
			else
			{
				r1ProcessNextRoundCallElements(content, elements, nextRoundCall, thisURL, isRestful, 1);
			}

			sb.append("\n\nNext round added: " + nextRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
			NioLog.getLogger().debug("%%%%%%%%%%Round 1 nextRound calls  " + printCalls(nextRoundCall));
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

		boolean isRestful = thisURL.getPropertyMap().getAsBoolean(UrlSrc._AS_RESTFUL);
		NioLog.getLogger().debug("r1ProcessUpcomingCalls");

		Elements elements = content.getElementsByTag("a");

		for (Element link : elements)
		{

			UrlSrc newUrlSrc = processCall(link, thisURL, thisURL.getFirstRoundLinks(), thisURL.getFirstRoundIgnore(),
					1, 0);

			if (null == newUrlSrc)
			{
				continue;
			}

			newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 1));

			upComingCalls.add(newUrlSrc);
			sb.append("\nr1ProcessUpcomingCalls Created UpcomingCall: " + newUrlSrc.getUrl() + " ");
			thisRoundAdd++;

			boolean allowDuplicatesAnywhere = thisURL.getPropertyMap().getAsBoolean(UrlSrc._ALLOW_DUPLICATES_ANYWHERE,
					1);
			boolean allowDuplicatesInsideBranch = thisURL.getPropertyMap()
					.getAsBoolean(UrlSrc._ALLOW_DUPLICATES_INSIDE_BRANCH, 1);
			if (false == allowDuplicatesAnywhere || false == allowDuplicatesInsideBranch)
			{
				String key = "";
				if (!allowDuplicatesInsideBranch)
				{
					key = newUrlSrc.getUrl();
				}
				else if (!allowDuplicatesAnywhere)
				{
					key = newUrlSrc.getUrl();
				}
				duplicateMap.remove(key);
				sb.append("\nMisplaced below top branch: " + newUrlSrc.getUrl() + " ");
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

			Elements elements = content.getElementsByTag("a");

			for (Element le : elements)
			{

				//////////////////////////////////////////////////

				UrlSrc newUrlSrc = processCall(le, thisURL, thisURL.getThirdRoundLinks(), thisURL.getThirdRoundIgnore(),
						3, 10);
				if (null == newUrlSrc)
				{
					continue;
				}

				newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 3));

				nextRoundCall.add(newUrlSrc);

				nextRoundAdd++;

				sb.append("\nround2 added " + newUrlSrc.getUrl());

			}

			sb.append("\n\nNext round added: " + nextRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
			NioLog.getLogger().debug("^^^^^^^^^^^^^^^^Round 2 nextround  " + printCalls(nextRoundCall));
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
			UrlSrc newUrlSrc = processCall(link, thisURL, thisURL.getThirdRoundLinks(), thisURL.getSecondRoundIgnore(),
					3, 2);

			if (null == newUrlSrc)
			{
				continue;
			}

			newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 3));

			upComingCalls.add(newUrlSrc);
			sb.append("\nr2ProcessUpcomingCalls  Created UpcomingCall: " + newUrlSrc.getUrl());
			thisRoundAdd++;
		}
		sb.append("\n\nThis round added: " + thisRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
		NioLog.getLogger().debug("^^^^^^^^^^^^^^^^Round 2 upcoming  " + printCalls(upComingCalls));
		return thisURL.getPromulgatedData();
	}

	protected String r3ProcessUpcomingCalls(Document content, List<UrlSrc> upComingCalls, UrlSrc thisURL)
	{
		Elements elements = content.getElementsByTag("a");
		for (Element link : elements)
		{
			UrlSrc newUrlSrc = processCall(link, thisURL, thisURL.getThirdRoundLinks(), thisURL.getThirdRoundIgnore(),
					3, 10);

			if (null == newUrlSrc)
			{
				continue;
			}

			newUrlSrc.setPromulgatedData(generatePromulgationString(content, newUrlSrc, 3));

			upComingCalls.add(newUrlSrc);
			sb.append("\nr2ProcessUpcomingCalls  Created UpcomingCall: " + newUrlSrc.getUrl());
			thisRoundAdd++;
		}
		sb.append("\n\nThis round added: " + thisRoundAdd + "\n\nSkipped as Duplicate: " + skippedAsDuplicate);
		NioLog.getLogger().debug("^^^^^^^^^^^^^^^^Round 3 upcoming  " + printCalls(upComingCalls));
		return thisURL.getPromulgatedData();
	}

}
