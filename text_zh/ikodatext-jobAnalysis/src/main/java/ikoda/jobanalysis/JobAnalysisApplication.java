package ikoda.jobanalysis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.net.URL;
import java.net.URLClassLoader;

public class JobAnalysisApplication
{
	
	private String message;

	public JobAnalysisApplication()
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



	public static void main(String[] args) throws Exception
	{
		JALog.getLogger().info("Starting JobAnalysisApplication");
		
		 ClassLoader cl = ClassLoader.getSystemClassLoader();

	        URL[] urls = ((URLClassLoader)cl).getURLs();

	        for(URL url: urls){
	        	JALog.getLogger().info(url.getFile());
	        }
		
		
		
		ApplicationContext context = 
	             new ClassPathXmlApplicationContext("Beans.xml");
		
		JobAnalysisThread t = (JobAnalysisThread) context.getBean("jobAnalysisThread");	    
		
		t.runTextProcesses();
		t.start();
	    t.join();

	}

}
