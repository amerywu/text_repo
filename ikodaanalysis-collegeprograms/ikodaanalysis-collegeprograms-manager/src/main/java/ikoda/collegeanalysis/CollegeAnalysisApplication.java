package ikoda.collegeanalysis;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ikoda.netio.config.ConfigurationBeanFactory;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InterfaceConfigurationBeanParent;
import ikoda.utils.IKodaUtilsException;
import ikoda.utils.SSm;

public class CollegeAnalysisApplication
{
	
	private String message;
	private static Logger logger=LogManager.getLogger("ikoda.collegeanalysis");

	public CollegeAnalysisApplication()
	{
		
		
	}
	
	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

    public static ConfigurationBeanParent getConfig() throws IKodaUtilsException
    {

        try
        {
            return (ConfigurationBeanParent) ConfigurationBeanFactory.getInstance().getConfigurationBean(
                    ConfigurationBeanParent.COLLEGE_ANALYSIS_CONFIGURATION,
                    InterfaceConfigurationBeanParent.FILENAME_PARENT, new ReentrantLock());

        }
        catch (Exception e)
        {

            logger.error(e.getMessage(), e);
            throw new IKodaUtilsException(e.getMessage(),e);


        }

    }

	public static void main(String[] args) throws Exception
	{
		
		System.out.println("CollegeAnalysisApplication");
		SSm.getLogger("ikoda").info("Starting collegeAnalysisApplication");
		
		
		

		
		
		
		ApplicationContext context = 
	             new ClassPathXmlApplicationContext("CollegeBeans.xml");
		SSm.getLogger("ikoda").info("Starting collegeAnalysisApplication 1");
		
		ConfigurationBeanParent config=CollegeAnalysisApplication.getConfig() ;
		
		if(config.isRunFileio()||config.isRunNetio()||config.isRunTextAnalysis()||config.isRunTraining())
		{
			CollegeAnalysisThread t = (CollegeAnalysisThread) context.getBean("collegeAnalysisThread");	    
	
			t.runTextProcesses();
			t.start();
		    t.join();
		}
		else if(config.isRunSimpleStream())
		{
			SimpleDataStreamer t = (SimpleDataStreamer) context.getBean("simpleDataStreamer");	    
			t.runStreamer();
			t.start();
		    t.join();
		}

	}

}
