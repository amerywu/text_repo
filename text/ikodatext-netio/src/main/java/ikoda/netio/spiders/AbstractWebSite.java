package ikoda.netio.spiders;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;
import ikoda.utils.IDGenerator;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;

public abstract class AbstractWebSite
{

	protected static HashMap<String, String> duplicateMap = new HashMap<String, String>();
	public static final String LIST = " | RESULTLIST | ";
	public static final String POST = " | JOBPOST | ";
	private static final String ASTERISK = "*";
	private static final String DOTASTERISK = "./*";
	private static final String BACKSLASH = "/";

	public static void clearDuplicateMap()
	{
		duplicateMap.clear();
	}

	protected int skippedAsDuplicate;

	protected int thisRoundAdd;
	protected int nextRoundAdd;

	public AbstractWebSite()
	{
		NioLog.getLogger().debug("INIT " + this.getClass().getName());
	}

	private String appendHost(UrlSrc thisURL, String linkHref)
	{
		String returnString = "";

		if (thisURL.getUrl().endsWith("/"))
		{
			returnString = cleanURL(processRoot(thisURL, linkHref));
		}
		else
		{
			returnString = cleanURL(thisURL.getHostAndProtocol() + linkHref);

		}
		return returnString;
	}

	protected String cleanURL(String inurl)
	{
		String temp = inurl.replace("://", "2h39i");
		temp = temp.replace("//", "/");
		temp = temp.replace("2h39i", "://");
		temp = temp.replace("&amp;", "&");
		return temp;
	}

	protected String extractHeader(String startTag, String endTag, String fileContent)
	{
		try
		{
			if (fileContent.contains(startTag))
			{
				int start = fileContent.indexOf(startTag) + startTag.length();
				int end = fileContent.indexOf(endTag);
				String value = fileContent.substring(start, end);
				value = value.trim();
				return value;
			}
			else
			{
				NioLog.getLogger().debug("No value for " + startTag);
				return null;
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().debug(startTag + " " + e.getMessage(), e);
			return null;
		}
	}

	protected boolean isDuplicate(String linkHref)
	{
		if (null != duplicateMap.get(linkHref))
		{

			NioLog.getLogger().debug("Duplicate: " + linkHref);
			return true;
		}
		duplicateMap.put(linkHref, linkHref);
		return false;
	}

	protected boolean linkContains(String link, String linkCue)
	{
		if (link.toUpperCase().contains(linkCue.toUpperCase()))
		{
			return true;
		}
		return false;
	}

	protected boolean linkContainsAll(String link, List<String> linkCues, List<String> ignoreCues, String sourceLink, int round)
	{
		// NioLog.getLogger().debug("linkContainsAll " + link + " || linkCues " +
		// linkCues);
		if (!relevantLink(link, ignoreCues))
		{
			return false;
		}

		for (String s : linkCues)
		{
			if (s.equals(ASTERISK))
			{
				logCall(link, sourceLink, "containsAll",round);
				return true;
			}
			else if (s.equals(DOTASTERISK))
			{
				if (link.contains(sourceLink))
				{
					logCall(link, sourceLink, "containsAll",round);
					return true;
				}
			}
			if (!link.toUpperCase().contains(s.toUpperCase()))
			{
				return false;
			}
		}
		// NioLog.getLogger().debug(" *** true for " + link + " " + linkCues);
		return true;
	}

	protected boolean linkContainsEither(String link, List<String> linkCues, List<String> ignoreCues, String sourceLink,int round)
	{
		if (!relevantLink(link, ignoreCues))
		{
			return false;
		}

		for (String s : linkCues)
		{
			if (link.toUpperCase().contains(s.toUpperCase()) || s.equals(ASTERISK))
			{
				logCall(link, sourceLink, "linkContainsEither",round);
				return true;
			}
			else if (s.equals(DOTASTERISK))
			{
				if (link.contains(sourceLink))
				{
					logCall(link, sourceLink, "linkContainsEither",round);
					return true;
				}
			}
		}
		return false;
	}

	protected boolean linkIsRestful(String link, UrlSrc thisUrl, List<String> ignoreCues, int restfulDepth) 
	{
		// NioLog.getLogger().debug("\nlooking at " + link + "\n starting from : " +
		// thisUrl.getOriginUrl());

		if (!relevantLink(link, ignoreCues))
		{
			return false;
		}

		int currentDepth = StringUtils.countMatches(thisUrl.getOriginUrl(), BACKSLASH);
		int newLinkDepth = StringUtils.countMatches(link, BACKSLASH);
		if (!link.endsWith(BACKSLASH))
		{
			newLinkDepth++;
		}

		if (link.contains(thisUrl.getUrl()) && newLinkDepth > currentDepth
				&& newLinkDepth <= (currentDepth + restfulDepth))
		{
			// NioLog.getLogger().debug(" *** true for " + link );
			logCall(link,thisUrl.getUrl(),"restful",0);

			return true;
		}
		return false;
	}
	
	private void logCall(String link, String  source,  String type, int round)
	{
		try
		{


		   Spreadsheet.getInstance().getCsvSpreadSheet("netioCalls").addCell(link, "From", source);
		   Spreadsheet.getInstance().getCsvSpreadSheet("netioCalls").addCell(link, "To", link);
		   Spreadsheet.getInstance().getCsvSpreadSheet("netioCalls").addCell(link, "Type", type);
		   Spreadsheet.getInstance().getCsvSpreadSheet("netioCalls").addCell(link, "Round", String.valueOf(round));
		}
		catch(Exception e)
		{
			NioLog.getLogger().warn(e.getMessage(),e);
		}
	}

	protected String printCalls(List<UrlSrc> l)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n\n");
		for (UrlSrc u : l)
		{
			String uri = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_URI_OPEN,
					StringConstantsInterface.SPIDERTAG_URI_CLOSE, u.getPromulgatedData());
			sb.append(u.getUrl());
			sb.append(" - ");
			sb.append(uri);
			sb.append("\n");
		}
		return sb.toString();
	}

	protected UrlSrc processCall(Element le, UrlSrc thisURL, List<String> links, List<String> ignore, int round)
	{
		String linkHref = le.attr("href");
		return processCall(linkHref, thisURL, links, ignore, round, 0);
	}

	protected UrlSrc processCall(Element le, UrlSrc thisURL, List<String> links, List<String> ignore, int round,
			int restfulDepth)
	{
		String linkHref = le.attr("href");
		return processCall(linkHref, thisURL, links, ignore, round, restfulDepth);
	}

	protected UrlSrc processCall(String linkHref, UrlSrc thisURL, List<String> links, List<String> ignore, int round)
	{
		return processCall(linkHref, thisURL, links, ignore, round, 0);
	}

	protected UrlSrc processCall(String linkHref, UrlSrc thisURL, List<String> links, List<String> ignore, int round,
			int restfulDepth)
	{
		try
		{

			boolean requireAll = thisURL.getPropertyMap().getAsBoolean(UrlSrc._REQUIRE_ALL_LINK_CRITERIA, round);
			boolean asRestful = thisURL.getPropertyMap().getAsBoolean(UrlSrc._AS_RESTFUL);

			//NioLog.getLogger().debug("Round "+ round+" looking at raw link "+linkHref);
			//NioLog.getLogger().debug("Round "+ round+" links "+links);
			//NioLog.getLogger().debug("Round "+ round+" ignore "+ignore);

			boolean isTargetLink = false;
			linkHref = processUrl(thisURL, linkHref, round);
			//NioLog.getLogger().debug(" looking at processed link "+linkHref);
			if (asRestful)
			{
				//NioLog.getLogger().debug(" asRestful ");
				isTargetLink = linkIsRestful(linkHref, thisURL, ignore, restfulDepth);
			}
			else if (requireAll)
			{
				//NioLog.getLogger().debug(" requireAll ");
				isTargetLink = linkContainsAll(linkHref, links, ignore, thisURL.getUrlAndUriAsString(), round);
			}
			else
			{
				//NioLog.getLogger().debug(" requireOne ");
				isTargetLink = linkContainsEither(linkHref, links, ignore, thisURL.getUrlAndUriAsString(),round);
			}

			if (isTargetLink)
			{
				//NioLog.getLogger().debug(" isTargetLink " + isTargetLink);

				return processTargetLink(linkHref, thisURL, round);
			}
			//NioLog.getLogger().debug(" isTargetLink " + isTargetLink);
			return null;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return null;
		}
	}

	private String processRoot(UrlSrc thisUrl, String linkHref)
	{
		try
		{
			if (linkHref.indexOf(BACKSLASH) < 0)
			{
				return thisUrl.getUrl() + BACKSLASH + linkHref;
			}
			String[] linkHrefParts = linkHref.split(BACKSLASH);
			String[] canonicalRootParts = thisUrl.getUrl().split(BACKSLASH);

			for (int j = 0; j < 3; j++)
			{
				if (linkHrefParts.length >= j
						&& linkHrefParts[j].equalsIgnoreCase(canonicalRootParts[canonicalRootParts.length - 1]))
				{
					StringBuilder sb = new StringBuilder();
					sb.append(thisUrl.getUrl());
					for (int i = j + 1; i < linkHrefParts.length; i++)
					{
						sb.append(BACKSLASH);
						sb.append(linkHrefParts[i]);
					}

					return sb.toString();
				}
			}

			return thisUrl.getUrl() + BACKSLASH + linkHref;
		}
		catch (Exception e)
		{
			NioLog.getLogger().debug("FAILED on " + linkHref + " " + e.getMessage());
			NioLog.getLogger().debug("So, returning " + thisUrl.getUrl() + BACKSLASH + linkHref);
			return thisUrl.getUrl() + BACKSLASH + linkHref.replace("./", "/");
		}
	}

	public abstract String processResponse(UrlSrc thisUrl, int round, Integer spiderId, String response,
			List<UrlSrc> upComingCalls, List<UrlSrc> nextRoundCall);

	protected UrlSrc processTargetLink(String linkHref, UrlSrc thisURL, int round)
	{
		try
		{
			boolean allowDuplicatesAnywhere = thisURL.getPropertyMap().getAsBoolean(UrlSrc._ALLOW_DUPLICATES_ANYWHERE,
					round);
			boolean allowDuplicatesInsideBranch = thisURL.getPropertyMap()
					.getAsBoolean(UrlSrc._ALLOW_DUPLICATES_INSIDE_BRANCH, round);

			if (allowDuplicatesInsideBranch)
			{
				if (!allowDuplicatesAnywhere)
				{

					String key = linkHref;

					if (isDuplicate(key))
					{
						skippedAsDuplicate++;
						// NioLog.getLogger().debug("\nDuplicate Skipping + " + key);
						return null;
					}
				}
			}
			else if (!allowDuplicatesAnywhere)
			{
				String key = linkHref;

				if (isDuplicate(key))
				{
					// NioLog.getLogger().debug("\nDuplicate Skipping + " + key);
					skippedAsDuplicate++;
					return null;
				}
			}
			NioLog.getLogger().debug("\n\n\n{adding} " + linkHref + "\n\n");
			UrlSrc url = new UrlSrc();
			url.initialize(thisURL);
			url.setUrl(linkHref);

			url.setPromulgatedData(thisURL.getPromulgatedData());
			return url;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return null;
		}
	}

	protected String processUrl(UrlSrc thisURL, String linkHref, int round)
	{
		try
		{

			String ommit = thisURL.getPropertyMap().getAsString(UrlSrc._URI_OMMIT, round);
			String prefix = thisURL.getPropertyMap().getAsString(UrlSrc._URI_PREFIX, round);
			if (null != ommit)
			{

				linkHref = linkHref.replace(ommit, "");
			}

			if (null != prefix)
			{

				linkHref = prefix + linkHref;
			}

			if (!(linkHref.startsWith("http")))
			{

				linkHref = appendHost(thisURL, linkHref);

			}
			else if (linkHref.startsWith("//"))
			{
				linkHref = "http:" + linkHref;
			}

			return cleanURL(linkHref);
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return linkHref;
		}
	}

	private boolean relevantLink(String link, List<String> ignoreCues)
	{
		for (String s : ignoreCues)
		{
			if (s.endsWith(ASTERISK))
			{
				if (link.contains(s.substring(0, s.indexOf(ASTERISK))))
				{
					if (!link.endsWith(s.substring(0, s.indexOf(ASTERISK))))
					{
						NioLog.getLogger().debug(
								"Blocking " + link + ". URI extends beyond + " + s.substring(0, s.indexOf(ASTERISK)));
						return false;
					}
				}
			}
			else if (link.toUpperCase().contains(s.toUpperCase()))
			{
				return false;
			}
		}
		return true;

	}

}
