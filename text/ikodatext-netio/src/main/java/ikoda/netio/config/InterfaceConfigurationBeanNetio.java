package ikoda.netio.config;

import java.util.List;

public interface InterfaceConfigurationBeanNetio
{
	public String getNetIoAccumulatorPath();

	public String getNetIoDumpPath();

	public int getNetioMinimumCycleInMinutes();

	public String getUrlRepository();

	public boolean isNetioRunInCycle();

	public List<String> getLinksForUniversalIgnore();

}