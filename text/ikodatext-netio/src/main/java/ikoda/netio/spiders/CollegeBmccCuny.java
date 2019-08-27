package ikoda.netio.spiders;

import java.util.UUID;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class CollegeBmccCuny extends CollegePrograms
{

	public CollegeBmccCuny()
	{

	}

	@Override
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
						+ StringConstantsInterface.SPIDERTAG_HOST_CLOSE + StringConstantsInterface.SPIDERTAG_URI_OPEN
						+ thisUrl.getUriAsString() + StringConstantsInterface.SPIDERTAG_URI_CLOSE
						+ StringConstantsInterface.SPIDERTAG_CATEGORY + thisUrl.getUrlAndUriAsString()
						+ StringConstantsInterface.SPIDERTAG_CATEGORY_CLOSE
						+ StringConstantsInterface.SPIDERTAG_TBID_OPEN + String.valueOf(UUID.randomUUID())
						+ StringConstantsInterface.SPIDERTAG_TBID_CLOSE
						+ StringConstantsInterface.SPIDERTAG_WEBSITE_OPEN + thisUrl.getWebSiteName()
						+ StringConstantsInterface.SPIDERTAG_WEBSITE_CLOSE;
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
		NioLog.getLogger().debug("promulgation string : " + returnString);
		return returnString;
	}

	@Override
	protected String processUrl(UrlSrc thisURL, String linkHref, int round)
	{
		try
		{
			NioLog.getLogger().debug("starting with " + linkHref);

			String ommit = thisURL.getPropertyMap().getAsString(UrlSrc._URI_OMMIT, round);
			if (null != ommit)
			{
				NioLog.getLogger().debug("ommitting " + ommit);
				linkHref = linkHref.replace(ommit, "");
			}
			String prefix = thisURL.getPropertyMap().getAsString(UrlSrc._URI_PREFIX, round);
			if (null != prefix)
			{
				NioLog.getLogger().debug("prefixing " + prefix);
				linkHref = prefix + linkHref;
			}

			if (!(linkHref.startsWith("http")))
			{
				// NioLog.getLogger().warn("\n\nNo URL for " + linkHref +
				// "\n\n");
				linkHref = cleanURL(thisURL.getUrlAndUriAsString() + "/" + linkHref);
				NioLog.getLogger().debug("prefixing " + thisURL.getHostAndProtocol());

			}
			NioLog.getLogger().debug("returning " + linkHref);
			return linkHref;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

			return linkHref;
		}
	}

}
