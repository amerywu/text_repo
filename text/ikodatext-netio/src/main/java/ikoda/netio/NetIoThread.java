package ikoda.netio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import ikoda.netio.config.ConfigurationBeanForNetio_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.ConfigurationBeanURL;
import ikoda.netio.config.NetioLiveState;
import ikoda.netio.config.SpiderCallCount;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.netio.config.UrlRepository;
import ikoda.netio.config.UrlSrc;
import ikoda.netio.spiders.AbstractWebSite;
import ikoda.utils.ProcessStatus;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;

public class NetIoThread extends Thread
{

	protected class SleepTime
	{
		private long sleepTimeCurr = 1000;
		private long baseSleepTime;
		private boolean pause;

		public long getBaseSleepTime()
		{
			return baseSleepTime;
		}

		public long getLastSleepTime()
		{
			return sleepTimeCurr;
		}

		public long getSleepTime()
		{
			if (netioLiveState.isWaitForNewRound())
			{
				sleepTimeCurr = 60000;
			}
			else if (pause)
			{
				sleepTimeCurr = 240000;
				pause = false;

			}
			else if (netioLiveState.getConfig().isRapidRandomBrowse())
			{
				sleepTimeCurr = 200;
			}
			else if (netioLiveState.getConfig().isMinimizeResourceUse())
			{
				sleepTimeCurr = 30000 + (long) (Math.random() * ((12000 - 2000) + 1));
			}
			else
			{
				if(System.currentTimeMillis() % 100 == 0)
				{
					sleepTimeCurr = 90000;
				}
				else {
					sleepTimeCurr = baseSleepTime + (long) (Math.random() * ((12000 - 2000) + 1));
				}
				
			}
			ProcessStatus.getStatusMap().put("Netio 11. SleepTime", millisToString(sleepTimeCurr));
			return sleepTimeCurr;
		}

		public void pause()
		{
			pause = true;
		}

		public void setBaseSleepTime(long baseSleepTime)
		{
			this.baseSleepTime = baseSleepTime;
		}

		public void setSleepTime(long sleepTime)
		{
			this.sleepTimeCurr = sleepTime;
		}

	}

	private static final String DOTSLASH = "./";

	private static final String LIVESTATEFILE = "livestate.xml";

	private static final String SUCCESS = "SUCCESS";
	private static final String FAIL = "FAIL";
	private static final String COL_STATUS = "COL_STATUS";
	private static final String COL_URL = "COL_URL";
	private static final String URLCALL_STATUS = "URLCALL_STATUS.csv";

	private static final String TXT = ".txt";
	private static final String HTML = ".HTML";
	private static final String PDF = ".pdf";
	private static final String XMLPREFIX = "xml_";
	private static final int MAXCALLS = 30000;

	protected String analysisSubType;
	protected int maxCallsRoundOne;
	protected int maxCallsRoundTwo;
	protected int maxCallsRoundThree;
	protected int maxCallsRoundFour;

	protected int currentAccumulation;
	protected UrlRepository tempRepository;
	protected int countStartingAddresses;
	protected String donePath = ".";
	protected String outputPath = ".";
	protected Map<String, String> previousCalls = new HashMap<>();
	protected Instant urlCallStartTime;
	protected List<Path> urlRepositories = new ArrayList<>();
	protected int callCount = 0;
	protected int maxRounds = 10;
	protected String currentUrlRepository = "";

	protected NetioLiveState netioLiveState = new NetioLiveState();
	protected String logDir = "./logs";

	protected ReentrantLock lock;

	protected boolean continueRun = true;

	private UrlSrc nextURL;

	private boolean stopNow = false;

	protected SleepTime sleepTimeCalculator = new SleepTime();

	public NetIoThread()
	{

	}

	public void reviveAfterThreadHang(NetIoThread hungThread) throws IKodaNetioException
	{

		try
		{

			NioLog.getLogger().warn("\n\n\n\n\n\n\nNetio Hung. Restarting\n\n\n\n\n");
			this.previousCalls = hungThread.previousCalls;

			this.netioLiveState = hungThread.netioLiveState;

			this.callCount = hungThread.callCount;
			this.maxRounds = hungThread.maxRounds;
			this.lock = hungThread.lock;
			this.analysisSubType = hungThread.analysisSubType;

			this.maxCallsRoundOne = hungThread.maxCallsRoundOne;
			this.maxCallsRoundTwo = hungThread.maxCallsRoundTwo;
			this.maxCallsRoundThree = hungThread.maxCallsRoundThree;
			this.maxCallsRoundFour = hungThread.maxCallsRoundFour;

			this.currentAccumulation = hungThread.currentAccumulation;
			this.tempRepository = hungThread.tempRepository;
			this.countStartingAddresses = hungThread.countStartingAddresses;

			this.previousCalls = hungThread.previousCalls;
			this.urlCallStartTime = hungThread.urlCallStartTime;
			this.urlRepositories = hungThread.urlRepositories;
			this.callCount = hungThread.callCount;
			this.maxRounds = hungThread.maxRounds;
			this.currentUrlRepository = hungThread.currentUrlRepository;

			donePath = netioLiveState.getConfig().getFioConfig().getFileIoDonePath();
			outputPath = netioLiveState.getConfig().getNetioConfig().getNetIoDumpPath();

			if (!Paths.get(donePath).toFile().exists())
			{
				Files.createDirectories(Paths.get(donePath));
			}
			if (!Paths.get(outputPath).toFile().exists())
			{
				Files.createDirectories(Paths.get(outputPath));
			}

			netioLiveState.setRepositoryCycleStartTime(System.currentTimeMillis());

			listUrlRepositories(Paths.get(netioLiveState.getConfig().getNetioConfig().getUrlRepository()));
			currentUrlRepository = getUrlRepositoryName(netioLiveState.getUrlRepositoryCurrentIndex() - 1);

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			throw new IKodaNetioException(e);

		}

	}

	public void abort()
	{
		stopNow = true;
		continueRun = false;
		NioLog.getLogger().info("abort called on netio ", new Exception());
	}

	private String cleanPageTitle(String inPageTitle)
	{
		try
		{
			if (inPageTitle.toUpperCase().contains("UNASSIGNED"))
			{
				netioLiveState.incrementFailCount(nextURL.getSpiderId());
			}
			else if (inPageTitle.toUpperCase().contains("NOT FOUND"))
			{
				netioLiveState.incrementFailCount(nextURL.getSpiderId());
			}
			String cleanString = inPageTitle.replaceAll("[^A-Za-z0-9]", "");
			return cleanString;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return "Unassigned";
		}
	}

	private void configureNewSpiders(List<ConfigurationBeanURL> startingAddresses, ConfigurationBeanParent config)
			throws IKodaNetioException
	{
		try
		{
			int count = 10;
			for (ConfigurationBeanURL configUrl : startingAddresses)
			{
				boolean asRestful = configUrl.getPropertyMap().getAsBoolean(UrlSrc._AS_RESTFUL);
				if (asRestful && configUrl.getFirstRoundLinks().size() > 0)
				{
					generateMultipleSpiders(configUrl, config, count);
				}
				else
				{
					try
					{
						List<String> ignoreList = config.getNetioConfig().getLinksForUniversalIgnore();
						UrlSrc url = new UrlSrc();
						url.initialize(configUrl);
						url.setSpiderId(count);
						url.addUniversalIgnore(ignoreList);
						url.setUrlRepository(currentUrlRepository);

						SpiderCallCount scc = new SpiderCallCount();
						scc.setName(configUrl.getUrl());
						scc.setSpiderId(count);
						scc.setCallCount(0);
						scc.setMaxRounds(configUrl.getRoundCount());
						netioLiveState.getSpiderCallCounts().add(scc);

						netioLiveState.getUpcomingCalls().add(url);
					}
					catch (Exception er)
					{
						ProcessStatus.put("Netio WARN:",
								"Malformed Url in " + currentUrlRepository + " " + configUrl.getUrl());
					}
				}
				count++;
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			throw new IKodaNetioException(e.getMessage(), e);
		}
	}

	private void logCall(String urlNow, String status)
	{
		try
		{
			String uid = String.valueOf(System.currentTimeMillis());
			uid = uid.substring(5, uid.length());
			Spreadsheet.getInstance().getCsvSpreadSheet(URLCALL_STATUS).addCell(uid, COL_URL, urlNow);
			Spreadsheet.getInstance().getCsvSpreadSheet(URLCALL_STATUS).addCell(uid, COL_STATUS, status);
			Spreadsheet.getInstance().getCsvSpreadSheet(URLCALL_STATUS).printCsvAppend();
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	private synchronized boolean doGrab()
	{

		ProcessStatus.getStatusMap().put("Netio 09. Duration", millisToString(netioLiveState.getDuration()));
		if (stopNow)
		{
			NioLog.getLogger().info("Calling Stop on NetIO Thread.");
			return false;
		}
		urlCallStartTime = Instant.now();
		if (netioLiveState.isWaitForNewRound())
		{
			resetNetio();
			return true;
		}

		NioLog.getLogger().info("\n\n\n\n\n");
		try
		{
			// happens after thread revival
			if (null == nextURL)
			{
				evaluateNextMove();
				return true;
			}
			currentAccumulation = netioLiveState.getUpcomingCalls().size();

			String pageTitle = "Unassigned";

			netioLiveState.incrementCallCount(nextURL.getSpiderId());

			byte[] barrayXml = {};
			byte[] barrayText = {};

			String urlNow = nextURL.getUrl().replace("$amp;", "&");
			if (urlNow.endsWith(".pdf"))
			{
				String pdfString = grabPdf(urlNow, nextURL);
				previousCalls.put(urlNow, urlNow);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(nextURL.toByteArray());
				outputStream.write(pdfString.getBytes(Charset.forName("UTF-8")));

				barrayText = outputStream.toByteArray();
				pageTitle = nextURL.getUriAsString().replaceAll("[^A-Za-z0-9]", "-");

			}

			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
			setWebClientOptions(webClient);
			try
			{
				NioLog.getLogger().info("\n\n NextURL:  " + urlNow + "\n" + currentUrlRepository + "\n\n");
				final HtmlPage page = webClient.getPage(urlNow);

				previousCalls.put(urlNow, urlNow);
				barrayXml = page.asXml().getBytes(Charset.forName("UTF-8"));

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(nextURL.toByteArray());
				outputStream.write(page.asText().getBytes(Charset.forName("UTF-8")));

				barrayText = outputStream.toByteArray();
				pageTitle = page.getPage().getTitleText();
				logCall(urlNow, SUCCESS);

			}
			catch (Exception e)
			{
				logCall(urlNow, FAIL);
				NioLog.getLogger().warn(e.getMessage());
			}
			finally
			{
				webClient.close();

			}

			NioLog.getLogger().debug("analysisSubType: " + analysisSubType);
			AbstractWebSite processor = WebSiteFactory.getWebSite(nextURL.getWebSiteName(), analysisSubType);

			String processString = processor.processResponse(nextURL, netioLiveState.getCurrentRound(),
					nextURL.getSpiderId(), new String(barrayXml), netioLiveState.getUpcomingCalls(),
					netioLiveState.getNextRoundCalls());

			processString = StringConstantsInterface.GRAB_BLOCK_START + StringConstantsInterface.SPIDERTAG_FULLURL_OPEN
					+ nextURL.getUrl() + StringConstantsInterface.SPIDERTAG_FULLURLCLOSE + "\n" + processString + "\n"
					+ StringConstantsInterface.GRAB_BLOCK_END;

			String fileName = nextURL.getUriAsString().replaceAll("[^A-Za-z0-9]", "") + cleanPageTitle(pageTitle);
			String fileNameTruncated = null;
			if (fileName.length() > 90)
			{
				fileNameTruncated = fileName.substring(0, 70);
			}
			else
			{
				fileNameTruncated = fileName;
			}

			saveFiles(fileNameTruncated, barrayXml, barrayText, processString);

			saveLiveState();
			recordStatus();
			evaluateNextMove();
			return true;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(nextURL);

			NioLog.getLogger().error(e.getMessage(), e);
			if (!netioLiveState.getUpcomingCalls().isEmpty())
			{
				nextURL = netioLiveState.getUpcomingCalls().remove(0);
				return true;
			}

			return false;
		}

	}

	private void evaluateNextMove()
	{
		try
		{
			/**Each spider is removed from upcoming calls when it hits max quota. So if empty, then max calls has been 
			 * reached for all spiders*/

			if (null == nextURL || netioLiveState.getUpcomingCalls().isEmpty())
			{
				NioLog.getLogger().info("\n\nending round " + netioLiveState.getCurrentRound()
						+ " now. upcoming calls size is 0 \n\n\n");
				if (!moveToNextRound())
				{
					NioLog.getLogger().info("\n\n\n\nResetting netio. Getting new set of URLs\n\n\n\n\n");

					resetNetio();
					saveLiveState();
					return;
				}

			}

			evaluateCurrentCallCountForSpider();

			evaluateUpcomingCallCountForSpider();

			nextURL = getNextUrlSrc();
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	private void evaluateCurrentCallCountForSpider()
	{
		if (null != nextURL && netioLiveState.getCallCountBySpiderId(nextURL.getSpiderId())
				.intValue() >= getMaxCallThisRound(netioLiveState.getCurrentRound()))
		{
			// so we could be calling from multiple sites. We just dump
			// calls from site that has hit max calls

			Iterator<UrlSrc> itr = netioLiveState.getUpcomingCalls().iterator();
			while (itr.hasNext())
			{
				UrlSrc url = itr.next();
				if (url.getSpiderId().equals(nextURL.getSpiderId()))
				{
					itr.remove();
				}
			}

			NioLog.getLogger()
					.info("\n\nending round " + netioLiveState.getCurrentRound() + "for " + nextURL.getWebSiteName()
							+ " spider id " + nextURL.getSpiderId() + " now.   Exceeded Max Calls roundCallCount>="
							+ getMaxCallThisRound(netioLiveState.getCurrentRound()) + "\n\n\n");

		}

	}

	private void evaluateUpcomingCallCountForSpider()
	{
		if (null == nextURL)
		{
			return;
		}
		int accumulatedUpcoming = netioLiveState.getUpcomingCallsAccumulatedForSpider(nextURL.getSpiderId());

		if (getMaxCallThisRound(netioLiveState.getCurrentRound()) < accumulatedUpcoming)
		{

			ProcessStatus.getStatusMap()
					.put("Netio: Excessive Accumulated Calls: | r " + netioLiveState.getCurrentRound() + " | f "
							+ currentUrlRepository + " | spider [" + nextURL.getSpiderId() + "]",
							String.valueOf(accumulatedUpcoming));
		}
	}

	private void generateMultipleSpiders(ConfigurationBeanURL configUrl, ConfigurationBeanParent config, int count)
			throws IKodaNetioException
	{
		try
		{
			for (String urlLink : configUrl.getFirstRoundLinks())
			{

				try
				{
					List<String> ignoreList = config.getNetioConfig().getLinksForUniversalIgnore();
					UrlSrc url = new UrlSrc();
					url.initialize(configUrl);
					url.setSpiderId(count);
					url.setOriginUrl(urlLink);
					url.setUrl(urlLink);
					url.setUrlRepository(currentUrlRepository);
					url.addUniversalIgnore(ignoreList);

					SpiderCallCount scc = new SpiderCallCount();
					scc.setName(urlLink);
					scc.setSpiderId(count);
					scc.setCallCount(0);
					scc.setMaxRounds(configUrl.getRoundCount());
					netioLiveState.getSpiderCallCounts().add(scc);
					count++;
					netioLiveState.getUpcomingCalls().add(url);
				}
				catch (Exception er)
				{
					ProcessStatus.put("Netio WARN:", "Malformed Url in " + currentUrlRepository + " " + urlLink);
				}

			}
		}
		catch (Exception e)
		{
			throw new IKodaNetioException(e.getMessage(), e);
		}
	}

	public int getCallCount()
	{
		return callCount;
	}

	public int getCountOfNextRoundCalls()
	{

		return netioLiveState.getNextRoundCalls().size();
	}

	private NetioLiveState getLiveSate()
	{
		try
		{
			NioLog.getLogger().debug("Trying to read livestate");

			if (lock.tryLock(180, TimeUnit.SECONDS))
			{
				try
				{

					NioLog.getLogger().debug("got lock");

					JAXBContext jaxbContext = JAXBContext.newInstance(NetioLiveState.class);

					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
					NetioLiveState netioLiveStateTemp = (NetioLiveState) jaxbUnmarshaller
							.unmarshal(new File(LIVESTATEFILE));

					NioLog.getLogger().debug("got live state");
					return netioLiveStateTemp;
				}
				catch (Exception e)
				{
					NioLog.getLogger().error(e.getMessage(), e);
					return null;

				}
				finally
				{
					lock.unlock();
					NioLog.getLogger().debug("released lock");
				}
			}
			else
			{
				NioLog.getLogger().warn("Could not get lock");
				return null;
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return null;

		}
	}


	public int getMaxCallThisRound(int round)
	{
		if (round == 1)
		{
			return maxCallsRoundOne;
		}
		if (round == 2)
		{
			return maxCallsRoundTwo;
		}
		if (round == 3)
		{
			return maxCallsRoundThree;
		}
		if (round == 4)
		{
			return maxCallsRoundFour;
		}
		return 0;

	}

	private UrlSrc getNextUrlSrc()
	{
		if (netioLiveState.getUpcomingCalls().isEmpty())
		{
			return null;
		}

		shuffleLinkOrder();

		UrlSrc us = netioLiveState.getUpcomingCalls().remove(0);

		while (null != previousCalls.get(us) && netioLiveState.getUpcomingCalls().size() > 0)
		{
			us = netioLiveState.getUpcomingCalls().remove(0);

		}
		if (null != currentUrlRepository && !currentUrlRepository.isEmpty())
		{
			us.setUrlRepository(currentUrlRepository);
		}
		return us;

	}

	public int getThisRoundCallsRemaining()
	{
		return netioLiveState.getUpcomingCalls().size();
	}

	public long getUrlCallDurationInMillis()
	{
		if (sleepTimeCalculator.getLastSleepTime() > 200000)
		{
			return 0;
		}
		return Duration.between(urlCallStartTime, Instant.now()).toMillis();

	}

	private UrlRepository getUrlRepository()
	{
		try
		{
			int index = netioLiveState.getUrlRepositoryCurrentIndex();
			lock.lock();
			if (urlRepositories.size() == 0)
			{
				listUrlRepositories(Paths.get(netioLiveState.getConfig().getNetioConfig().getUrlRepository()));
			}

			if (index >= urlRepositories.size())
			{
				urlRepositories.clear();
				listUrlRepositories(Paths.get(netioLiveState.getConfig().getNetioConfig().getUrlRepository()));
			}

			NioLog.getLogger().info("Index is: " + index);
			NioLog.getLogger().info("urlRepositories: " + urlRepositories.toString() + "\n\n");
			Path p = null;
			if (index < urlRepositories.size())
			{
				NioLog.getLogger().info("\n\n\t\tMoving to Next URL Repository\n\n");
				p = urlRepositories.get(index);
				ProcessStatus.concatenateStatus("Netio 12. Completed Repositories", currentUrlRepository);

				currentUrlRepository = p.getFileName().toString();

				index++;
				netioLiveState.setUrlRepositoryCurrentIndex(index);
			}
			else
			{
				if (netioLiveState.getConfig().getNetioConfig().isNetioRunInCycle())
				{
					if (isRestartFullCycle())
					{
						ProcessStatus.clearAll();

						// // restart full cycle if sufficient time has passed
						NioLog.getLogger().info("\n\n\n\n\n\n****Restarting new cycle***\n\n\n\n\n\n\n\n\n\n");
						index = 0;
						p = urlRepositories.get(index);
						index++;
						netioLiveState.setUrlRepositoryCurrentIndex(index);
						NioLog.getLogger().info("\n\n\n\n\nCompleted Full Cycle. Clearing duplicate maps\n\n\n\n\n\n");
						AbstractWebSite.clearDuplicateMap();

						previousCalls.clear();
						netioLiveState.setWaitForNewRound(false);
					}
					else
					{
						NioLog.getLogger().info(
								"\n\n\t\t-----------------\n\nToo early to restart cycle. In holding pattern\n\t\t----------------");
						previousCalls.clear();
						netioLiveState.setWaitForNewRound(true);
						return null;
					}
				}
				else
				{
					NioLog.getLogger().info("\n\n\nAll urls called. Process Complete. Ending now\n\\n\n");
					abort();
					return null;
				}
			}

			String path = p.toString();
			NioLog.getLogger().info("Getting url repository at " + path);

			NioLog.getLogger().info("got lock for " + path);

			try
			{
				JAXBContext jaxbContext = JAXBContext.newInstance(UrlRepository.class);

				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				UrlRepository ur = (UrlRepository) jaxbUnmarshaller.unmarshal(p.toFile());

				NioLog.getLogger().info("got repository");
				NioLog.getLogger().info("Next index is: " + index);
				tempRepository = ur;
				countStartingAddresses = ur.getUrls().size();
				return ur;
			}
			catch (Exception e)
			{
				continueRun = false;
				NioLog.getLogger().error(e.getMessage(), e);

			}
			return null;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return null;

		}
		finally
		{
			if (lock.isLocked())
			{
				lock.unlock();
				NioLog.getLogger().info("released lock");
			}
		}
	}

	private String getUrlRepositoryName()
	{
		int index = netioLiveState.getUrlRepositoryCurrentIndex();
		return getUrlRepositoryName(index);
	}

	private String getUrlRepositoryName(int index)
	{

		if (index < 0)
		{
			index = 0;
		}
		if (urlRepositories.size() == 0)
		{
			listUrlRepositories(Paths.get(netioLiveState.getConfig().getNetioConfig().getUrlRepository()));
		}
		Path p = urlRepositories.get(index);
		return p.getFileName().toString();
	}

	protected String extractTextFromPdf(InputStream is, UrlSrc urlSrc)
	{

		try
		{
			StringBuilder sb = new StringBuilder();

			sb.append(StringConstantsInterface.SPIDERTAG_HOST_OPEN + urlSrc.getHostAsString()
					+ StringConstantsInterface.SPIDERTAG_HOST_CLOSE + StringConstantsInterface.SPIDERTAG_URI_OPEN
					+ urlSrc.getUriAsString() + StringConstantsInterface.SPIDERTAG_URI_CLOSE
					+ StringConstantsInterface.SPIDERTAG_CATEGORY
					+ urlSrc.getFileAsString().replaceAll("[^A-Za-z0-9]", " ")
					+ StringConstantsInterface.SPIDERTAG_CATEGORY_CLOSE + StringConstantsInterface.SPIDERTAG_TBID_OPEN
					+ String.valueOf(UUID.randomUUID()) + StringConstantsInterface.SPIDERTAG_TBID_CLOSE
					+ StringConstantsInterface.SPIDERTAG_WEBSITE_OPEN + urlSrc.getWebSiteName()
					+ StringConstantsInterface.SPIDERTAG_WEBSITE_CLOSE
					+ StringConstantsInterface.SPIDERTAG_URLREPOSITORY_OPEN + urlSrc.getUrlRepository()
					+ StringConstantsInterface.SPIDERTAG_URLREPOSITORY_CLOSE + "\n\n");

			///////////////////////////////////////////////////////
			PdfReader reader = null;

			try
			{
				reader = new PdfReader(is);
				NioLog.getLogger().debug(reader);
				int n = reader.getNumberOfPages();
				NioLog.getLogger().debug("page count " + n);
				sb.append("\n\n\n---EXTRACTED FROM PDF---\n ");

				sb.append("\n\n\n");
				for (int i = 1; i <= n; i++)
				{
					sb.append(PdfTextExtractor.getTextFromPage(reader, i));
					// FioLog.getLogger().debug("page "+sb.toString());
				}
				NioLog.getLogger().debug(sb);

				byte[] b = sb.toString().getBytes(StandardCharsets.US_ASCII);
				String pdfstring = new String(b);
				pdfstring.replaceAll("\\?", " ");
				pdfstring = StaticSundryUtils.removeListsAndMenus(pdfstring);
				return pdfstring;
			}
			catch (Exception e)
			{
				NioLog.getLogger().error(e.getMessage(), e);
				return "";
			}
			finally
			{
				if (null != reader)
				{
					reader.close();
				}
			}

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
		return "";

	}

	private String grabPdf(String urlNow, UrlSrc urlSrc)
	{
		try
		{
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_45);
			OutputStream outStream = null;
			InputStream is = null;

			try
			{
				setWebClientOptions(webClient);
				final UnexpectedPage pdfPage = webClient.getPage(urlNow);
				is = pdfPage.getWebResponse().getContentAsStream();
				String pdfText = extractTextFromPdf(is, urlSrc);
				return pdfText;

			}
			catch (Exception e)
			{
				netioLiveState.incrementFailCount(nextURL.getSpiderId());
				NioLog.getLogger().error(e.getMessage(), e);
				return e.getMessage();
			}
			finally
			{
				if (null != is)
				{
					is.close();
				}
				if (null != outStream)
				{
					outStream.close();
				}
				webClient.close();
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return e.getMessage();
		}

	}

	public boolean isContinueRun()
	{
		return continueRun;
	}

	private boolean isRestartFullCycle()
	{

		long duration = netioLiveState.getDuration();
		int netioMinimumCycleInMinutes = netioLiveState.getConfig().getNetioConfig().getNetioMinimumCycleInMinutes();
		long netioMinimumCycle = netioMinimumCycleInMinutes * 60 * 1000;

		NioLog.getLogger().info("------------------------------------------  Duration (restart set for "
				+ netioMinimumCycle + ") = " + duration);
		if (duration >= netioMinimumCycle)
		{
			netioLiveState.setRepositoryCycleStartTime(System.currentTimeMillis());
			ProcessStatus.getStatusMap().put("Netio 10. Cycle Status", "Running");
			return true;
		}

		ProcessStatus.getStatusMap().put("Netio 10. Cycle Status", "Paused");
		ProcessStatus.getStatusMap().put("Netio 08. Minimum Cycle Time", millisToString(netioMinimumCycle));

		return false;
	}

	public boolean isStopNow()
	{
		return stopNow;
	}

	private void listUrlRepositories(Path path)
	{
		NioLog.getLogger().info("listUrlRepositories ......");
		try
		{
			if (lock.tryLock(8, TimeUnit.SECONDS))
			{
				try
				{

					try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
					{

						for (Path entry : stream)
						{
							if (entry.toFile().isDirectory())
							{
								listUrlRepositories(entry);
							}
							else
							{
								NioLog.getLogger().debug("adding repository to list " + entry.getFileName().toString());
								urlRepositories.add(entry);
							}
						}
					}
					catch (Exception e)
					{
						NioLog.getLogger().error(e.getMessage(), e);
					}
				}
				catch (Exception e)
				{

					NioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
				NioLog.getLogger().debug("done");
			}
			else
			{
				NioLog.getLogger().warn("Failed to acquire lock");
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	private String millisToString(Long millis)
	{
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

	}

	private boolean moveToNextRound()
	{
		if (netioLiveState.getCurrentRound() == maxRounds)
		{
			netioLiveState.resetAllSpiderCallCountsToZero();
			return false;
		}
		if (!netioLiveState.getNextRoundCalls().isEmpty())
		{

			NioLog.getLogger().info("\n\n\n\n\n *****Next Round******* \n\n\n\n\n ");

			netioLiveState.resetAllSpiderCallCountsToZero();
			netioLiveState.nextRound();

			netioLiveState.getUpcomingCalls().clear();
			netioLiveState.getUpcomingCalls().addAll(netioLiveState.getNextRoundCalls());
			netioLiveState.getNextRoundCalls().clear();

			NioLog.getLogger().debug("UpcomingCalls " + netioLiveState.getUpcomingCalls());
			NioLog.getLogger().debug("nextRoundCalls " + netioLiveState.getNextRoundCalls());
			callCount = 0;

			return true;

		}
		NioLog.getLogger().info("\n\n\n\n\n *****No More Rounds after Round " + netioLiveState.getCurrentRound()
				+ " ******* \n\n\n\n\n ");
		netioLiveState.resetAllSpiderCallCountsToZero();
		return false;

	}

	private void recordStatus()
	{
		try
		{
			callCount++;

			ProcessStatus.getStatusMap().put("Netio 01. Current Round",
					String.valueOf(netioLiveState.getCurrentRound()));
			ProcessStatus.getStatusMap().put("Netio 06. Spider id", String.valueOf(nextURL.getSpiderId()));
			ProcessStatus.getStatusMap()
					.put("X Netio 02. r " + netioLiveState.getCurrentRound() + " | spider " + nextURL.getSpiderId()
							+ " | " + netioLiveState.getSpiderName(nextURL.getSpiderId()) + " ||| Call count",
							String.valueOf(netioLiveState.getCallCountBySpiderId(nextURL.getSpiderId())));
			ProcessStatus.getStatusMap().put("Netio 07. CurrentUrlRepository", currentUrlRepository);
			ProcessStatus.getStatusMap().put("Netio 03. Total calls so far this round", String.valueOf(getCallCount()));
			ProcessStatus.getStatusMap().put("Netio 04. Calls remaining  this round",
					String.valueOf(getThisRoundCallsRemaining()));
			ProcessStatus.getStatusMap().put("Netio 05. Next round calls", String.valueOf(getCountOfNextRoundCalls()));
			ProcessStatus.getStatusMap().put("Netio 10. Cycle Status", "THREAD RUNNING");
			Spreadsheet.getInstance().getCsvSpreadSheet("netioCalls").printCsvAppend("netioCalls.csv");

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}
	}

	protected void resetNetio()
	{
		try
		{

			NioLog.getLogger().info("\n\n\t\tResetting Netio\n\n");
			ConfigurationBeanParent config = netioLiveState.getConfig();
			int index = netioLiveState.getUrlRepositoryCurrentIndex();
			long startTime = netioLiveState.getRepositoryCycleStartTime();
			netioLiveState = new NetioLiveState();
			netioLiveState.setUrlRepositoryCurrentIndex(index);
			netioLiveState.setConfig(config);
			netioLiveState.setRepositoryCycleStartTime(startTime);

			UrlRepository repo = getUrlRepository();
			if (null == repo)
			{
				return;
			}
			List<ConfigurationBeanURL> startingAddresses = repo.getUrls();
			NioLog.getLogger().debug("repo.size: " + repo.getUrls().size());

			if (!Files.exists(Paths.get(donePath)))
			{
				Files.createDirectories(Paths.get(donePath));
			}
			if (!Files.exists(Paths.get(outputPath)))
			{
				Files.createDirectories(Paths.get(outputPath));
			}

			ConfigurationBeanForNetio_Generic nioConfig = (ConfigurationBeanForNetio_Generic) config.getNetioConfig();
			maxCallsRoundOne = nioConfig.getMaxCallsRoundOne();
			maxCallsRoundTwo = nioConfig.getMaxCallsRoundTwo();
			maxCallsRoundThree = nioConfig.getMaxCallsRoundThree();
			maxCallsRoundFour = nioConfig.getMaxCallsRoundFour();

			netioLiveState.getSpiderCallCounts().clear();

			configureNewSpiders(startingAddresses, config);
			NioLog.getLogger()
					.debug("netioLiveState.getUpcomingCalls().size: " + netioLiveState.getUpcomingCalls().size());
			netioLiveState.setCurrentRound(1);
			nextURL = getNextUrlSrc();

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			NioLog.getLogger().error("Giving Up: Killing Thread");

			continueRun = false;
		}

	}

	private boolean resumeNetio(ConfigurationBeanParent inconfig, ReentrantLock inlock) throws IKodaNetioException
	{
		try
		{

			donePath = inconfig.getFioConfig().getFileIoDonePath();
			outputPath = inconfig.getNetioConfig().getNetIoDumpPath();
			lock = inlock;
			netioLiveState = getLiveSate();
			if (null == netioLiveState)
			{
				return false;
			}

			ConfigurationBeanForNetio_Generic nioConfig = (ConfigurationBeanForNetio_Generic) netioLiveState.getConfig()
					.getNetioConfig();
			maxCallsRoundOne = nioConfig.getMaxCallsRoundOne();
			maxCallsRoundTwo = nioConfig.getMaxCallsRoundTwo();
			maxCallsRoundThree = nioConfig.getMaxCallsRoundThree();
			maxCallsRoundFour = nioConfig.getMaxCallsRoundFour();
			analysisSubType = netioLiveState.getConfig().getAnalysisSubType();
			if (null == analysisSubType)
			{
				throw new IKodaNetioException("analysisSubtype is null");
			}

			listUrlRepositories(Paths.get(netioLiveState.getConfig().getNetioConfig().getUrlRepository()));
			currentUrlRepository = getUrlRepositoryName(netioLiveState.getUrlRepositoryCurrentIndex() - 1);
			if (null == currentUrlRepository)
			{
				throw new IKodaNetioException("currentUrlRepository is null");
			}

			return true;

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			throw new IKodaNetioException(e.getMessage(), e);
		}
	}

	@Override
	public void run()
	{

		try
		{

			NioLog.getLogger().info("starting thread");
			while (continueRun)
			{
				if (lock.isHeldByCurrentThread())
				{
					NioLog.getLogger().warn("Releasing lock before going to sleep. This is bad");
					lock.unlock();
				}

				long sleep = sleepTimeCalculator.getSleepTime();
				Thread.sleep(sleep);

				NioLog.getLogger().debug("next sleep " + sleep);
				if (!doGrab())
				{
					NioLog.getLogger().info("EXITING THREAD");
					continueRun = false;
				}
			}
			NioLog.getLogger().info("THREAD EXITED");
			ProcessStatus.getStatusMap().put("Netio 10. Cycle Status", "THREAD EXITED");
		}
		catch (InterruptedException e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			return;

		}
		catch (Exception e)
		{
			NioLog.getLogger().error("This is Bad. Thread won't start");
			NioLog.getLogger().error(e.getMessage(), e);
			continueRun = false;
		}
		catch (Error err)
		{
			NioLog.getLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
			continueRun = false;
			throw err;
		}
	}

	public void runNetIo(int startingRound, ConfigurationBeanParent config, ReentrantLock inlock)
			throws IKodaNetioException, IOException
	{

		NioLog.getLogger().info("runNetIo " + inlock);
		Spreadsheet.getInstance().initCsvSpreadsheet("netioCalls",NioLog.getLogger(),System.getProperty("user.dir"));

		logDir = NioLog.getDirectoryForAppender();

		Spreadsheet.getInstance().initCsvSpreadsheet(URLCALL_STATUS, logDir);

		NioLog.getLogger().info("Log Directory: " + logDir);
		boolean resumed = false;

		String urlRepository = config.getNetioConfig().getUrlRepository();
		if (null == urlRepository)
		{
			NioLog.getLogger().error("Cannot find url repository. Not in config file");
			throw new IKodaNetioException("Cannot find url repository. Not in config file");
		}
		if (!(Paths.get(urlRepository).toFile().exists()))
		{
			NioLog.getLogger().error("Cannot find path: " + urlRepository);
			throw new IKodaNetioException("Cannot find path: " + urlRepository);
		}

		if (config.isNetioResumeMode())
		{
			NioLog.getLogger().info("\n\n\n\n\n\nStarting Netio in Resume Mode\n\n\n\n\n\n\n\n\n\n");
			resumed = resumeNetio(config, inlock);
		}

		if (!resumed)
		{
			lock = inlock;
			netioLiveState.setRepositoryCycleStartTime(System.currentTimeMillis());
			listUrlRepositories(Paths.get(urlRepository));

			UrlRepository repo = getUrlRepository();
			List<ConfigurationBeanURL> startingAddresses = repo.getUrls();
			NioLog.getLogger().info("\n\n\n\n\n\nStarting Netio in Fresh Start Mode\n\n\n\n\n\n\n\n\n\n");
			donePath = config.getFioConfig().getFileIoDonePath();
			outputPath = config.getNetioConfig().getNetIoDumpPath();
			analysisSubType = config.getAnalysisSubType();

			netioLiveState.setConfig(config);

			if (!(Paths.get(donePath).toFile().exists()))
			{
				Files.createDirectories(Paths.get(donePath));
			}
			if (!(Paths.get(outputPath).toFile().exists()))
			{
				Files.createDirectories(Paths.get(outputPath));
			}

			ConfigurationBeanForNetio_Generic nioConfig = (ConfigurationBeanForNetio_Generic) config.getNetioConfig();

			maxCallsRoundOne = nioConfig.getMaxCallsRoundOne();
			maxCallsRoundTwo = nioConfig.getMaxCallsRoundTwo();
			maxCallsRoundThree = nioConfig.getMaxCallsRoundThree();
			maxCallsRoundFour = nioConfig.getMaxCallsRoundFour();

			configureNewSpiders(startingAddresses, config);

			NioLog.getLogger()
					.debug("netioLiveState.getUpcomingCalls().size: " + netioLiveState.getUpcomingCalls().size());
			netioLiveState.setCurrentRound(startingRound);
			ProcessStatus.getStatusMap().put("Netio 10. Cycle Status", "Running");
		}
		nextURL = getNextUrlSrc();

	}

	private void saveFiles(String fileName, byte[] barrayXml, byte[] barrayText, String processString)
	{
		try
		{
			if (lock.tryLock(10, TimeUnit.SECONDS))
			{

				try
				{

					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					outputStream.write(
							netioLiveState.getConfig().getDatabaseDescriptor().getBytes(Charset.forName("UTF-8")));
					outputStream.write(processString.getBytes(Charset.forName("UTF-8")));
					outputStream.write(barrayText);

					byte[] barrayTextToOutput = outputStream.toByteArray();

					Files.write(
							Paths.get(outputPath + File.separator + netioLiveState.getCurrentRound() + XMLPREFIX
									+ nextURL.getWebSiteName() + fileName + System.currentTimeMillis() + HTML),
							barrayXml);
					Files.write(Paths.get(outputPath + File.separator + netioLiveState.getCurrentRound() + "_"
							+ fileName + nextURL.getWebSiteName() + System.currentTimeMillis() + TXT),
							barrayTextToOutput);

					NioLog.getLogger().debug(outputPath + File.separator + netioLiveState.getCurrentRound() + "_"
							+ fileName + nextURL.getWebSiteName() + System.currentTimeMillis() + TXT);
					NioLog.getLogger().debug("Promulgation string was " + processString);
				}
				catch (Exception e)
				{
					NioLog.getLogger().error(e.getMessage(), e);

				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				NioLog.getLogger().warn("Could not get lock");

			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

		}
	}

	private void saveLiveState()
	{
		try
		{

			if (lock.tryLock(10, TimeUnit.SECONDS))
			{
				try
				{

					JAXBContext jaxbContext = JAXBContext.newInstance(NetioLiveState.class);

					Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
					jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					jaxbMarshaller.marshal(netioLiveState, new File(LIVESTATEFILE));
				}
				catch (Exception e)
				{
					NioLog.getLogger().error(e.getMessage(), e);
				}
				finally
				{
					lock.unlock();
				}
			}
			else
			{
				NioLog.getLogger().warn("Could not get lock");

			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

		}
	}

	public void setCallCount(int callCount)
	{
		this.callCount = callCount;
	}

	public void setStopNow(boolean stopNow)
	{
		NioLog.getLogger().info("stop called on netio ", new Exception());
		this.stopNow = stopNow;

	}

	private void setWebClientOptions(WebClient webClient)
	{
		if (nextURL.getWebSiteName().equals(WebSiteFactory.ZHAOPING))
		{
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setGeolocationEnabled(false);
			webClient.getOptions().setHistorySizeLimit(3);
			webClient.getOptions().setPopupBlockerEnabled(true);
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setActiveXNative(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setUseInsecureSSL(true);
			webClient.getOptions().setTimeout(30000);
		}
		else
		{
			webClient.getOptions().setThrowExceptionOnScriptError(false);
			webClient.getOptions().setJavaScriptEnabled(false);
			webClient.getOptions().setCssEnabled(false);
			webClient.getOptions().setTimeout(30000);
			webClient.getOptions().setUseInsecureSSL(true);
		}
	}

	private void shuffleLinkOrder()
	{
		if (netioLiveState.getConfig().isRapidRandomBrowse())
		{
			if (callCount % 20 == 0)
			{

				long seed = System.nanoTime();
				Collections.shuffle(netioLiveState.getUpcomingCalls(), new Random(seed));
			}

		}
	}

	private int upcomingCallsAccumulated()
	{
		return netioLiveState.getUpcomingCalls().size() - currentAccumulation;
	}

}
