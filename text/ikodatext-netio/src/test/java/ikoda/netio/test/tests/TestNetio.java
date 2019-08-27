package ikoda.netio.test.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import ikoda.netio.NioLog;
import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanForPersistence_JobAnalysis;
import ikoda.netio.config.ConfigurationBeanForPersistence_JobDescriptionAnalysis;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Generic;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis;
import ikoda.netio.config.ConfigurationBeanForNetio_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InterfaceBindingConfig;
import ikoda.netio.config.InterfaceConfigurationBeanFileio;
import ikoda.netio.config.InterfaceConfigurationBeanNetio;
import ikoda.netio.config.InterfaceConfigurationBeanParent;
import ikoda.netio.config.InterfaceConfigurationBeanPersist;
import ikoda.netio.config.InterfaceConfigurationBeanTextAnalysis;
import ikoda.netio.config.LanguageForAnalysis;
import ikoda.netio.config.NetioLiveState;
import ikoda.netio.config.OpenCpuCall;
import ikoda.netio.test.testrunner.TestSS;

public class TestNetio
{

	private InterfaceBindingConfig getConfigBeanChild(InterfaceBindingConfig bean, String fileName)
	{

		try
		{
			// NioLog.getLogger().info("reading config");

			InputStream is = this.getClass().getResourceAsStream(fileName);

			JAXBContext jaxbContext = JAXBContext.newInstance(bean.getClass());

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			InterfaceBindingConfig nioConfig = (InterfaceBindingConfig) jaxbUnmarshaller.unmarshal(is);

			return nioConfig;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			fail(e.getMessage());
			return null;
		}

	}

	private ConfigurationBeanParent getConfigBeanParent(String fileName)
	{

		try
		{
			// NioLog.getLogger().info("reading config");
			TestSS.getLogger().info(this.getClass().getResource("."));

			TestSS.getLogger().info(this.getClass().getClassLoader().getResourceAsStream("."));

			InputStream is = this.getClass().getResourceAsStream(fileName);
			TestSS.getLogger().info("is " + is);

			JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBeanParent.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			ConfigurationBeanParent config = (ConfigurationBeanParent) jaxbUnmarshaller.unmarshal(is);
			// NioLog.getLogger().debug("config " + config);
			return config;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			fail(e.getMessage());
			return null;
		}

	}

	private InterfaceConfigurationBeanParent getConfigurationBean(String beanType)
	{

		ConfigurationBeanParent config = null;

		if (beanType.equals(InterfaceConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION))
		{
			config = getConfigBeanParent(InterfaceConfigurationBeanParent.FILENAME_PARENT);
			InterfaceConfigurationBeanNetio nioConfig = (InterfaceConfigurationBeanNetio) getConfigBeanChild(
					new ConfigurationBeanForNetio_Generic(), InterfaceConfigurationBeanParent.FILENAME_NETIO);
			config.setNetioConfig(nioConfig);
			InterfaceConfigurationBeanFileio fioConfig = (InterfaceConfigurationBeanFileio) getConfigBeanChild(
					new ConfigurationBeanForFileIo_Generic(), InterfaceConfigurationBeanParent.FILENAME_FILEIO);
			config.setFioConfig(fioConfig);
			InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
					new ConfigurationBeanForTextAnalysis_Generic(),
					InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
			config.setTaConfig(taConfig);
			InterfaceConfigurationBeanPersist pConfig = (InterfaceConfigurationBeanPersist) getConfigBeanChild(
					new ConfigurationBeanForPersistence_JobAnalysis(),
					InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE);
			config.setPersistConfig(pConfig);
			return config;
		}
		else if (beanType.equals(InterfaceConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION))
		{
			config = getConfigBeanParent(InterfaceConfigurationBeanParent.FILENAME_PARENT + "jda");
			InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
					new ConfigurationBeanForTextAnalysis_Generic(),
					InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS + "jda");
			config.setTaConfig(taConfig);
			InterfaceConfigurationBeanPersist pConfig = (InterfaceConfigurationBeanPersist) getConfigBeanChild(
					new ConfigurationBeanForPersistence_JobAnalysis(),
					InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE + "jda");
			config.setPersistConfig(pConfig);
			return config;
		}

		return null;
	}

	private void saveConfigurationBeanChild(InterfaceBindingConfig configbean, String fileName)
	{

		try
		{

			JAXBContext jaxbContext = JAXBContext.newInstance(configbean.getClass());

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(configbean, new File(this.getClass().getResource(".").getPath() + fileName));
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			fail(e.toString());

		}

	}

	private void saveConfigurationBeanParent(ConfigurationBeanParent configbean, String fileName)
	{

		try
		{

			JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBeanParent.class);

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			TestSS.getLogger().info("Saving:   " + this.getClass().getResource(".").getPath() + fileName);
			jaxbMarshaller.marshal(configbean, new File(this.getClass().getResource(".").getPath() + fileName));
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			fail(e.toString());

		}

	}

	private void testLiveState() throws Exception
	{

		try
		{
			NetioLiveState netioLiveState = new NetioLiveState();

			JAXBContext jaxbContext = JAXBContext.newInstance(NetioLiveState.class);

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(netioLiveState, new File("./testls.xml"));
		}

		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			throw new Exception(e);

		}

	}

	@Test
	public void testConfig() throws Exception
	{
		try
		{

			ConfigurationBeanParent p = new ConfigurationBeanParent();
			p.setAnalysisSubType("sfad");
			p.setAnalysisType(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION);
			p.setDatabaseDescriptor("dbdes");
			p.setElegantStop(false);
			p.setApplicationMainModuleName("asddas");
			p.setNetioResumeMode(true);

			LanguageForAnalysis la = new LanguageForAnalysis();
			la.getRegions().add("canada");
			la.getRegions().add("us");
			la.setLocaleCode("en");

			LanguageForAnalysis la1 = new LanguageForAnalysis();
			la1.getRegions().add("china");
			la1.setLocaleCode("zh_CN");

			p.getLanguagesForAnalysis().add(la);
			p.getLanguagesForAnalysis().add(la1);
			p.setMinimizeResourceUse(true);
			p.setRapidRandomBrowse(true);
			p.setRunFileio(true);
			p.setRunNetio(true);
			p.setRunPersistence(true);
			p.setRunPersistenceChores(true);
			p.setRunPersistenceReporting(true);
			p.setSpecifiedLanguage("en");
			OpenCpuCall occ = new OpenCpuCall();
			occ.setJavaCallingClass("com.whatever.whatebver");
			occ.setOpenCpuApp("appname");
			occ.setOpenCpuArg1("arg1");
			occ.setOpenCpuArg8("arg8");
			occ.setOpenCpuFunction("myFunction");
			occ.setOpenCpuPort(3421);
			occ.setOpenCpuRootPath("/dte/jut");
			occ.setOpenCpuScheme("scheme");
			occ.setOpenCpuUrl("http://www.safdsadfsda.ca");

			ConfigurationBeanForNetio_Generic nioConfig = new ConfigurationBeanForNetio_Generic();
			nioConfig.setMaxCallsRoundFour(1);
			nioConfig.setMaxCallsRoundThree(1);
			nioConfig.setMaxCallsRoundTwo(1);
			nioConfig.setMaxCallsRoundOne(1);
			nioConfig.setNetIoDumpPath("/asfsadfsda/ghjfjhgfjhgf");
			nioConfig.setNetioMinimumCycleInMinutes(34689);
			nioConfig.setUrlRepository("repo.xml");

			ConfigurationBeanForFileIo_Generic fioConfig = new ConfigurationBeanForFileIo_Generic();
			fioConfig.setFileIoDonePath("xzvxvzcvxzc/xvcvxvxz");
			fioConfig.setFileIoJobPostingPath("safd/asfd");
			fioConfig.setFileIoNonApplicablePath("afs/fdsa");
			fioConfig.setFileIoResultsPath("asdffdsa/asffds");
			fioConfig.setFileIoUndeterminedPath("asdf/asfd");
			fioConfig.setFileIoXMLPath("dsafasdf/dsfsfad");

			ConfigurationBeanForTextAnalysis_Generic taConfig = new ConfigurationBeanForTextAnalysis_Generic();
			taConfig.setNerPropertiesPath("aDS/asdf");
			taConfig.setTextAnalysisDonePath("dsfaasdf/dfghdhgf");
			taConfig.setTextAnalysisFailedPath("mmm/sdaf");
			taConfig.setTextAnalysisInfoBoxPath("asfdasfd");

			ConfigurationBeanForPersistence_JobAnalysis pConfig = new ConfigurationBeanForPersistence_JobAnalysis();
			pConfig.setPersistenceDonePath("asdsadfsfda/kjghkhjg");
			pConfig.setPersistenceFailedPath("fsadfdsafsda/ytruuyi");
			pConfig.setPersistIfCountAreasOfStudy(4);
			pConfig.setPersistIfCountCertification(5);
			pConfig.setPersistIfCountDetailLevel(34);
			pConfig.setPersistIfValueMinimumSalary(5667);
			pConfig.setQualificationMap("fdsadasdfgf");

			saveConfigurationBeanParent(p, InterfaceConfigurationBeanParent.FILENAME_PARENT);
			saveConfigurationBeanChild(nioConfig, InterfaceConfigurationBeanParent.FILENAME_NETIO);
			saveConfigurationBeanChild(fioConfig, InterfaceConfigurationBeanParent.FILENAME_FILEIO);
			saveConfigurationBeanChild(taConfig, InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
			saveConfigurationBeanChild(pConfig, InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE);

			InterfaceConfigurationBeanParent p1 = getConfigurationBean(
					InterfaceConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION);
			assertFalse(p1 == null);

			////////////////////////////////////////////////////////////////////

			TestSS.getLogger().info("\n\n\nJDA\n\n\n\n");
			ConfigurationBeanParent pjda = new ConfigurationBeanParent();

			pjda.setAnalysisSubType("sfad");
			pjda.setAnalysisType(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION);
			pjda.setDatabaseDescriptor("dbdes");
			pjda.setElegantStop(false);
			pjda.setApplicationMainModuleName("sadffadsfads");

			LanguageForAnalysis la2 = new LanguageForAnalysis();
			la2.getRegions().add("canada");
			la2.getRegions().add("us");
			la2.setLocaleCode("en");

			LanguageForAnalysis la3 = new LanguageForAnalysis();
			la3.getRegions().add("china");
			la3.setLocaleCode("zh_CN");

			pjda.getLanguagesForAnalysis().add(la3);
			pjda.getLanguagesForAnalysis().add(la2);
			pjda.setMinimizeResourceUse(true);
			pjda.setRapidRandomBrowse(true);
			pjda.setRunFileio(true);
			pjda.setRunNetio(true);
			pjda.setRunPersistence(true);
			pjda.setRunPersistenceChores(true);
			pjda.setRunPersistenceReporting(true);
			pjda.setSpecifiedLanguage("en");

			ConfigurationBeanForPersistence_JobDescriptionAnalysis configpjda = new ConfigurationBeanForPersistence_JobDescriptionAnalysis();
			configpjda.setBiteSize(44);
			configpjda.setMinDetailLevel(23);
			configpjda.setPercentToSample(45);
			configpjda.setPersistenceDonePath("adsffads");
			configpjda.setPersistenceFailedPath("asdffadsfads");

			ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis configtjda = new ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis();
			configtjda.getCsvForITokenNames().add("asdffasfads");
			configtjda.getCsvForITokenNames().add("asdf");
			configtjda.getCsvForITokenNames().add("hg");

			configtjda.setCsvPath("asfd");
			configtjda.setTextAnalysisDonePath("sadffdsa/adsffads");
			configtjda.setTextAnalysisFailedPath("sadffsadfas");
			configtjda.setMaxNumberOfDocuments(60000);

			saveConfigurationBeanParent(pjda, InterfaceConfigurationBeanParent.FILENAME_PARENT + "jda");

			saveConfigurationBeanChild(configtjda, InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS + "jda");
			saveConfigurationBeanChild(configpjda, InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE + "jda");

			InterfaceConfigurationBeanParent p2 = getConfigurationBean(
					InterfaceConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION);
			assertFalse(p1 == null);
			
			testLiveState();

			TestSS.getLogger().info(p2);

		}
		catch (Exception e)
		{
			fail(e.toString());
		}

	}

}
