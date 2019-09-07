package ikoda.netio.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ikoda.netio.NioLog;
import ikoda.utils.ProcessStatus;

@XmlType(propOrder =
{ "upcomingCalls", "nextRoundCalls", "spiderCallCounts", "currentRound", "urlRepositoryCurrentIndex",
		"repositoryCycleStartTime", "waitForNewRound", "config" })

@XmlRootElement(name = "livestate")
@XmlAccessorType(XmlAccessType.FIELD)
public class NetioLiveState
{
	@XmlElement
	private List<UrlSrc> upcomingCalls = new ArrayList<UrlSrc>();
	@XmlElement
	private List<UrlSrc> nextRoundCalls = new ArrayList<UrlSrc>();
	@XmlElement
	private List<SpiderCallCount> spiderCallCounts = new ArrayList<SpiderCallCount>();
	@XmlElement
	protected int currentRound = 1;

	@XmlElement
	protected ConfigurationBeanParent config;
	@XmlElement
	private int urlRepositoryCurrentIndex = 0;
	@XmlElement
	protected long repositoryCycleStartTime = 0;
	@XmlElement
	private boolean waitForNewRound;

	public NetioLiveState()
	{
		NioLog.getLogger().info("\n\ninit\n\n");
	}

	private void evaluateSpider(SpiderCallCount scc)
	{

		if (scc.getMaxRounds().intValue() >= currentRound)
		{
			if (scc.getCallCount().intValue() < 5)
			{
				ProcessStatus.getStatusMap().put(
						"X Netio: " + this.getCurrentRound() + " Low Call Count: " + scc.getName(),
						String.valueOf(scc.getCallCount()));
			}
			evaluateSpiderSuccessRate(scc);
		} 
	}

	private void evaluateSpiderSuccessRate(SpiderCallCount scc)
	{

		try
		{

			double calls = scc.getCallCount();
			double fails = scc.getFailCount();

			if (fails > 0)
			{
				String key = "X Netio: " + currentRound + " High Fail Rate:  " + scc.getName();
				if (calls == 0)
				{

					ProcessStatus.getStatusMap().put(key, "100%");
				}
				else
				{
					double failPercentage = fails / calls;
					if (failPercentage > 0.10)
					{
						ProcessStatus.incrementStatus(key);
						ProcessStatus.getStatusMap().put(key, String.valueOf(failPercentage));
					}
					else
					{
						ProcessStatus.getStatusMap().remove(key);
					}

				}
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
		}

	}

	public Integer getCallCountBySpiderId(Integer spiderId)
	{
		for (SpiderCallCount scc : spiderCallCounts)
		{
			if (scc.getSpiderId().equals(spiderId))
			{
				return scc.getCallCount();
			}
		}
		return null;
	}

	public int getCurrentRound()
	{
		return currentRound;
	}

	public long getDuration()
	{
		return System.currentTimeMillis() - repositoryCycleStartTime;
	}

	public List<UrlSrc> getNextRoundCalls()
	{
		return nextRoundCalls;
	}

	public int getNextRoundCallsAccumulatedForSpider(Integer spiderId)
	{
		int count = 0;
		for (UrlSrc us : this.getNextRoundCalls())
		{
			if (us.getSpiderId().equals(spiderId))
			{
				count++;
			}
		}
		return count;
	}

	public long getRepositoryCycleStartTime()
	{
		return repositoryCycleStartTime;
	}

	public List<SpiderCallCount> getSpiderCallCounts()
	{
		return spiderCallCounts;
	}

	public String getSpiderName(Integer spiderId)
	{

		List<String> l = spiderCallCounts.stream().filter(scc -> scc.getSpiderId().equals(spiderId))
				.map(scc -> scc.getName()).collect(Collectors.toList());
		if (!l.isEmpty())
		{
			return l.get(0);
		}
		return "UnknownSpider";
	}

	public List<UrlSrc> getUpcomingCalls()
	{
		return upcomingCalls;
	}

	public int getUpcomingCallsAccumulatedForSpider(Integer spiderId)
	{
		int count = 0;
		for (UrlSrc us : this.getUpcomingCalls())
		{
			if (us.getSpiderId().equals(spiderId))
			{
				count++;
			}
		}
		return count;
	}

	public int getUrlRepositoryCurrentIndex()
	{
		return urlRepositoryCurrentIndex;
	}

	public void incrementCallCount(Integer spiderId)
	{
		for (SpiderCallCount scc : spiderCallCounts)
		{
			if (scc.getSpiderId().equals(spiderId))
			{
				Integer newCount = scc.getCallCount().intValue() + 1;
				scc.setCallCount(newCount);
				evaluateSpiderSuccessRate(scc);

			}
		}
	}

	public void incrementFailCount(Integer spiderId)
	{
		for (SpiderCallCount scc : spiderCallCounts)
		{
			if (scc.getSpiderId().equals(spiderId))
			{
				Integer newCount = new Integer(scc.getFailCount().intValue() + 1);
				scc.setFailCount(newCount);
			}
		}
	}

	public boolean isWaitForNewRound()
	{
		return waitForNewRound;
	}

	public void nextRound()
	{
		this.currentRound++;
	}

	public void resetAllSpiderCallCountsToZero()
	{
		for (SpiderCallCount scc : spiderCallCounts)
		{
			evaluateSpider(scc);
			scc.setCallCount(new Integer(0));

		}
	}

	public void setCurrentRound(int currentRound)
	{
		this.currentRound = currentRound;
	}

	public void setNextRoundCalls(List<UrlSrc> nextRoundCalls)
	{
		this.nextRoundCalls = nextRoundCalls;
	}

	public void setRepositoryCycleStartTime(long repositoryCycleStartTime)
	{
		this.repositoryCycleStartTime = repositoryCycleStartTime;
	}

	public void setSpiderCallCounts(List<SpiderCallCount> spiderCallCounts)
	{
		this.spiderCallCounts = spiderCallCounts;
	}

	public void setUpcomingCalls(List<UrlSrc> upcomingCalls)
	{
		this.upcomingCalls = upcomingCalls;
	}

	public void setUrlRepositoryCurrentIndex(int urlRepositoryCurrentIndex)
	{
		this.urlRepositoryCurrentIndex = urlRepositoryCurrentIndex;
	}

	public void setWaitForNewRound(boolean waitForNewRound)
	{
		this.waitForNewRound = waitForNewRound;
	}

	public ConfigurationBeanParent getConfig()
	{
		return config;
	}

	public void setConfig(ConfigurationBeanParent config)
	{
		this.config = config;
	}

}
