package ikoda.manager;

import java.net.URL;
import java.net.URLClassLoader;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ManagerAnalysisApplication
{
	
	private String message;

	public ManagerAnalysisApplication()
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
		ManagerLog.getLogger().info("Starting JobAnalysisApplication");

		
		
		
		ApplicationContext context = 
	             new ClassPathXmlApplicationContext("Beans.xml");
		
		ManagerAnalysisThread2 t = (ManagerAnalysisThread2) context.getBean("managerAnalysisThread");	    
		
		t.runAnalysisProcesses();
		t.start();
	    t.join();

	}

}
