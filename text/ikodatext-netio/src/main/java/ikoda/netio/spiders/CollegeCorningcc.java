package ikoda.netio.spiders;

import java.util.UUID;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ikoda.netio.NioLog;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlSrc;

public class CollegeCorningcc extends CollegePrograms
{

	public CollegeCorningcc()
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
						+ StringConstantsInterface.SPIDERTAG_CATEGORY + thisUrl.getUriAsString()
						+ thisUrl.getFileAsString() + StringConstantsInterface.SPIDERTAG_CATEGORY_CLOSE
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

}
