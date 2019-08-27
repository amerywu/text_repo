package ikoda.netio.config;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import ikoda.netio.NetioException;
import ikoda.netio.NioLog;

public class ConfigurationBeanFactory
{

	private static ConfigurationBeanFactory configurationBeanFactory;

	public static ConfigurationBeanFactory getInstance()
	{
		if (null == configurationBeanFactory)
		{
			configurationBeanFactory = new ConfigurationBeanFactory();
		}
		return configurationBeanFactory;
	}

	private ConfigurationBeanFactory()
	{

	}

	private InterfaceBindingConfig getConfigBeanChild(InterfaceBindingConfig bean, String fileName)
	{

		try
		{

			InputStream is = this.getClass().getResourceAsStream("/config/" + fileName);

			if (null == is)
			{
				NioLog.getLogger().warn(fileName + " does not exist. Returning null.");
				return null;
			}

			JAXBContext jaxbContext = JAXBContext.newInstance(bean.getClass());

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			InterfaceBindingConfig nioConfig = (InterfaceBindingConfig) jaxbUnmarshaller.unmarshal(is);

			return nioConfig;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

			return null;
		}

	}

	private ConfigurationBeanParent getConfigBeanParent(ReentrantLock lock)
	{

		try
		{

			if (lock.tryLock(10, TimeUnit.SECONDS))
			{
				try
				{

					InputStream is = this.getClass().getResourceAsStream("/config.xml");

					JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBeanParent.class);

					Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

					ConfigurationBeanParent config = (ConfigurationBeanParent) jaxbUnmarshaller.unmarshal(is);

					return config;
				}
				catch (Exception e)
				{
					NioLog.getLogger().error(e.getMessage(), e);
					return null;
				}
				finally
				{
					lock.unlock();
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

	private ConfigurationBeanParent getConfigBeanParent(String fileName)
	{

		try
		{
			NioLog.getLogger().info("Loading config: " +  System.getProperty("user.dir") + "/config/" + fileName);

			InputStream is = this.getClass().getResourceAsStream("/config/" + fileName);

			JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBeanParent.class);

			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			ConfigurationBeanParent config = (ConfigurationBeanParent) jaxbUnmarshaller.unmarshal(is);

			return config;
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

			return null;
		}

	}

	public ConfigurationBeanParent getConfigurationBean(String beanType, String fileName, ReentrantLock lock)
			throws NetioException
	{

		try
		{

			if (lock.tryLock(10, TimeUnit.SECONDS))
			{

				ConfigurationBeanParent config = null;

				if (beanType.equals(InterfaceConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION))
				{
					config = getConfigBeanParent(fileName);
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
				else if (beanType.equals(InterfaceConfigurationBeanParent.COLLEGE_ANALYSIS_CONFIGURATION))
				{
					config = getConfigBeanParent(fileName);
					InterfaceConfigurationBeanNetio nioConfig = (InterfaceConfigurationBeanNetio) getConfigBeanChild(
							new ConfigurationBeanForNetio_Generic(), InterfaceConfigurationBeanParent.FILENAME_NETIO);
					config.setNetioConfig(nioConfig);
					InterfaceConfigurationBeanFileio fioConfig = (InterfaceConfigurationBeanFileio) getConfigBeanChild(
							new ConfigurationBeanForFileIo_Generic(), InterfaceConfigurationBeanParent.FILENAME_FILEIO);
					config.setFioConfig(fioConfig);
					InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
							new ConfigurationBeanForTextAnalysis_Reddit(),
							InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
					config.setTaConfig(taConfig);

					return config;
				}
				else if (beanType.equals(InterfaceConfigurationBeanParent.REDDIT_ANALYSIS_CONFIGURATION))
				{
					config = getConfigBeanParent(fileName);
					InterfaceConfigurationBeanNetio nioConfig = (InterfaceConfigurationBeanNetio) getConfigBeanChild(
							new ConfigurationBeanForNetio_Generic(), InterfaceConfigurationBeanParent.FILENAME_NETIO);
					config.setNetioConfig(nioConfig);
					InterfaceConfigurationBeanFileio fioConfig = (InterfaceConfigurationBeanFileio) getConfigBeanChild(
							new ConfigurationBeanForFileIo_Generic(), InterfaceConfigurationBeanParent.FILENAME_FILEIO);
					config.setFioConfig(fioConfig);
					InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
							new ConfigurationBeanForTextAnalysis_Reddit(),
							InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
					config.setTaConfig(taConfig);
					InterfaceConfigurationBeanPersist pConfig = (InterfaceConfigurationBeanPersist) getConfigBeanChild(
							new ConfigurationBeanForPersistence_JobDescriptionAnalysis(),
							InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE);
					config.setPersistConfig(pConfig);
					return config;
				}
				else if (beanType.equals(InterfaceConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION))
				{
					config = getConfigBeanParent(fileName);
					InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
							new ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis(),
							InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
					config.setTaConfig(taConfig);
					InterfaceConfigurationBeanPersist pConfig = (InterfaceConfigurationBeanPersist) getConfigBeanChild(
							new ConfigurationBeanForPersistence_JobDescriptionAnalysis(),
							InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE);
					config.setPersistConfig(pConfig);
					return config;
				}
				throw new NetioException("Config not found");

			}
			return null;

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			throw new NetioException(e.getMessage());
		}
		finally
		{
			lock.unlock();
		}
	}

	public void loadChildren(ConfigurationBeanParent config, ReentrantLock lock) throws NetioException
	{

		try
		{

			if (lock.tryLock(10, TimeUnit.SECONDS))
			{
				if (config.getAnalysisType().equals(InterfaceConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION))
				{

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
				}
				else if (config.getAnalysisType()
						.equals(InterfaceConfigurationBeanParent.COLLEGE_ANALYSIS_CONFIGURATION))
				{

					InterfaceConfigurationBeanNetio nioConfig = (InterfaceConfigurationBeanNetio) getConfigBeanChild(
							new ConfigurationBeanForNetio_Generic(), InterfaceConfigurationBeanParent.FILENAME_NETIO);
					config.setNetioConfig(nioConfig);
					InterfaceConfigurationBeanFileio fioConfig = (InterfaceConfigurationBeanFileio) getConfigBeanChild(
							new ConfigurationBeanForFileIo_Generic(), InterfaceConfigurationBeanParent.FILENAME_FILEIO);
					config.setFioConfig(fioConfig);
					InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
							new ConfigurationBeanForTextAnalysis_Reddit(),
							InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
					config.setTaConfig(taConfig);

				}
				else if (config.getAnalysisType()
						.equals(InterfaceConfigurationBeanParent.REDDIT_ANALYSIS_CONFIGURATION))
				{

					InterfaceConfigurationBeanNetio nioConfig = (InterfaceConfigurationBeanNetio) getConfigBeanChild(
							new ConfigurationBeanForNetio_Generic(), InterfaceConfigurationBeanParent.FILENAME_NETIO);
					config.setNetioConfig(nioConfig);
					InterfaceConfigurationBeanFileio fioConfig = (InterfaceConfigurationBeanFileio) getConfigBeanChild(
							new ConfigurationBeanForFileIo_Generic(), InterfaceConfigurationBeanParent.FILENAME_FILEIO);
					config.setFioConfig(fioConfig);
					InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
							new ConfigurationBeanForTextAnalysis_Reddit(),
							InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
					config.setTaConfig(taConfig);
					InterfaceConfigurationBeanPersist pConfig = (InterfaceConfigurationBeanPersist) getConfigBeanChild(
							new ConfigurationBeanForPersistence_JobDescriptionAnalysis(),
							InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE);
					config.setPersistConfig(pConfig);

				}
				else if (config.getAnalysisType()
						.equals(InterfaceConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION))
				{

					InterfaceConfigurationBeanTextAnalysis taConfig = (InterfaceConfigurationBeanTextAnalysis) getConfigBeanChild(
							new ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis(),
							InterfaceConfigurationBeanParent.FILENAME_TEXTANALYSIS);
					config.setTaConfig(taConfig);
					InterfaceConfigurationBeanPersist pConfig = (InterfaceConfigurationBeanPersist) getConfigBeanChild(
							new ConfigurationBeanForPersistence_JobDescriptionAnalysis(),
							InterfaceConfigurationBeanParent.FILENAME_PERSISTENCE);
					config.setPersistConfig(pConfig);
				}
				else
				{
					throw new NetioException("Config not found " + config.getAnalysisType());
				}
			}

		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);
			throw new NetioException(e.getMessage());
		}
		finally
		{
			lock.unlock();
		}
	}

	private void saveConfigurationBeanChild(InterfaceBindingConfig configbean, String fileName, String moduleName)
	{

		try
		{
			if (null == moduleName)
			{

				JAXBContext jaxbContext = JAXBContext.newInstance(configbean.getClass());

				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				jaxbMarshaller.marshal(configbean, new File("." + File.separator + "target" + File.separator
						+ "packaged-resources" + File.separator + "config" + File.separator + fileName));
			}
			else
			{
				JAXBContext jaxbContext = JAXBContext.newInstance(configbean.getClass());

				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				jaxbMarshaller.marshal(configbean, new File("." + File.separator + moduleName + File.separator
						+ "target" + File.separator + "classes" + "config" + File.separator + fileName));
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(e.getMessage(), e);

		}

	}

	public void saveConfigurationBeanParent(ConfigurationBeanParent configbean, String fileName, ReentrantLock lock)
	{

		try
		{
			if (lock.tryLock(10, TimeUnit.SECONDS))
			{

				JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationBeanParent.class);

				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

				if (null == configbean.getApplicationMainModuleName()
						|| configbean.getApplicationMainModuleName().equals("null")
						|| configbean.getApplicationMainModuleName().isEmpty())
				{
					String path = "." + File.separator + "target" + File.separator + "packaged-resources"
							+ File.separator + "config" + File.separator + fileName;
					NioLog.getLogger().info("\n\nSaving config to " + path + "\n\n");
					jaxbMarshaller.marshal(configbean, new File(path));
				}
				else
				{
					NioLog.getLogger().info(configbean.getApplicationMainModuleName());

					String path = "." + File.separator + configbean.getApplicationMainModuleName() + File.separator
							+ "target" + File.separator + "classes" + File.separator + "config" + File.separator
							+ fileName;
					NioLog.getLogger().info("\n\nSaving config to " + path + "\n\n");

					jaxbMarshaller.marshal(configbean, new File(path));
				}
			}
		}
		catch (Exception e)
		{
			NioLog.getLogger().error(
					"\n\nIf  running as standalone app, applicationMainModuleName should be deleted from config.xml. "
							+ e.getMessage(),
					e);

		}
		finally
		{
			lock.unlock();
		}

	}

}
