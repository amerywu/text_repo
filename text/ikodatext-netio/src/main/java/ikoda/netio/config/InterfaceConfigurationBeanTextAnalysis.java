package ikoda.netio.config;

import java.util.List;

public interface InterfaceConfigurationBeanTextAnalysis
{

	String getProjectPrefix();

	public int getTaThreadCount();

	String getTextAnalysisDonePath();

	String getTextAnalysisFailedPath();

	public boolean isCollectBySentence();

	public String getTargetColumnName();

	public List<String> getColumnsToExcludeFromLibSvm();

	public XMLPropertyMap getConfigProperties();

}