package ikoda.mlserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ikoda.ml.cassandra.*;

import ikoda.ml.predictions.MLPCPredictions;
import ikoda.ml.predictions.model.MLPCModelCollegeURL;
import ikoda.ml.streaming.DataStreamManager;
import ikoda.utils.SSm;



@Controller
public class MLController
{

	private static final String PARAM_URL = "url";
	private static final String PARAM_URL_LIST = "urllist";
	private static final String PARAM_TITLE = "title";
	private static final String PARAM_ID = "id";
	private static final String PARAM_PORT = "port";
	private static final String PARAM_PORT_LIST = "portlist";
	private static final String PARAM_DATA_SOURCE = "DATA_SOURCE";
	private static final String PARAM_KEY_SPACE = "KEY_SPACE";
	private static final String PARAM_KEY_SPACE_UUID = "KEY_SPACE_UUID";
	MLPCPredictions predictions = new MLPCPredictions();
	



	public MLController()
	{
		SSm.getLogger(this.getClass()).info("Starting MLController");
		CassandraKeyspaceConfigurationFactory.info();
		DataStreamManager.startStream();

		 Runtime.getRuntime().addShutdownHook(new Thread() {
			
			 
	         @Override
	         public void run() 
	         {
	             tearDown();
	         } 
	         
		 });
	}
		 
		 
	@PreDestroy
	public void tearDown()
	{
		 SSm.getAppLogger().info("Calling stop on Spark Streaming");
		 DataStreamManager.stop();
		
	}
		 
 
	
	
	
	@RequestMapping(value = "/plaintext", method = RequestMethod.GET)
	@ResponseBody
	public String plaintext(HttpServletResponse response, HttpServletRequest request)
	{
		try
		{
			response.setContentType("text/plain");
			response.setCharacterEncoding("UTF-8");

			HashMap<String, String> hm = new HashMap<>();
			String title = request.getParameter(PARAM_TITLE);
			String rowId = request.getParameter(PARAM_ID);
			String url = request.getParameter(PARAM_URL);

			SSm.getLogger(this.getClass().getName()).info("Input parameters: " + rowId + " " + url + " " + title);

			hm.put(MLPCPredictions.title(), title);
			hm.put(MLPCPredictions.url(), url);
			hm.put(MLPCPredictions.rowId(), rowId);

			return predictions.predictMajorbyUrlData(hm);
		}
		catch (Exception e)
		{
			SSm.getLogger("ikoda.ml").error(e.getMessage(), e);
			return "FAILED" + e.getMessage();
		}
	}

	@RequestMapping(value = "/initializeMLCPPrediction", method = RequestMethod.GET)
	@ResponseBody
	public String initialize(HttpServletResponse response, HttpServletRequest request)
	{
		try
		{

			MLPCPredictions predictions = new MLPCPredictions();
			HashMap<String, String> hm = new HashMap<>();
			hm.put(MLPCPredictions.title(), "business");
			hm.put(MLPCPredictions.url(), "12");
			hm.put(MLPCPredictions.rowId(), "business");
			predictions.predictMajorbyUrlData(hm);

			return "Initialized";
		}
		catch (Exception e)
		{
			SSm.getLogger("ikoda.ml").error(e.getMessage(), e);
			return "FAILED " + e.getMessage();
		}
	}
	
	
	@RequestMapping(value = "/generateURLPredictionModel", method = RequestMethod.GET)
	@ResponseBody
	public String generateURLPredictionModel(HttpServletResponse response, HttpServletRequest request)
	{
		try
		{
			
			SSm.getLogger("ikoda.ml").info("generateURLPredictionModel");;
			MLPCModelCollegeURL modelGenerator = new MLPCModelCollegeURL();
			String source=request.getParameter(PARAM_DATA_SOURCE);
			SSm.getLogger("ikoda.ml").info("Source is "+source);
			
			return modelGenerator.generateMLPCMOdelForURLPrediction(source);
		}
		catch (Exception e)
		{
			SSm.getLogger("ikoda.ml").error(e.getMessage(), e);
			return "FAILED " + e.getMessage();
		}
	}
	
	

	@RequestMapping(value = "/initializeMLCPStream", method = RequestMethod.GET)
	@ResponseBody
	public String initializeMLCPStream(HttpServletResponse response, HttpServletRequest request)
	{
		try
		{

			SSm.getLogger(this.getClass().getName()).info("\n\nEstablishing stream\n\n");


			
			DataStreamManager.startStream();
			//Cassandra.testConnect();
			return "Initialized";
		}
		catch (Exception e)
		{
			SSm.getLogger("ikoda.ml").error(e.getMessage(), e);
			return "FAILED " + e.getMessage();
		}
	}
	

	


	
	
	@RequestMapping(value = "/startMLCPStream", method = RequestMethod.GET)
	@ResponseBody
	public String startMLCPStream(HttpServletResponse response, HttpServletRequest request)
	{
		try
		{
			SSm.getLogger(this.getClass().getName()).info("\n\nStarting new data set stream\n\n");
			String port = request.getParameter(PARAM_PORT);

			String url = request.getParameter(PARAM_URL);

			String dataName=request.getParameter(PARAM_DATA_SOURCE);
			
			String keySpaceName=request.getParameter(PARAM_KEY_SPACE);
			
			String keySpaceUUID=request.getParameter(PARAM_KEY_SPACE_UUID);
			SSm.getLogger("ikoda").info(url+" | "+port+" | "+dataName+" | "+keySpaceName+" | "+keySpaceUUID);
			if(keySpaceUUID.length() < 10)
			{
				SSm.getLogger("ikoda").error("FAILED invalidUUID: uuid="+keySpaceUUID );
				return "FAILED invalidUUID: uuid="+keySpaceUUID;
			}
					

			SSm.getLogger(this.getClass()).info("Controller. Starting new data stream on "+url+":"+port+ " "+dataName +" "+keySpaceName);
			DataStreamManager.startStream();
			return DataStreamManager.newDataSet(url, port,dataName,keySpaceName,keySpaceUUID);
		}
		catch (Exception e)
		{
			SSm.getLogger("ikoda.ml").error(e.getMessage(), e);
			return "FAILED " + e.getMessage();
		}
	}

	@RequestMapping(value = "/testConnection", method = RequestMethod.GET)
	@ResponseBody
	public String testConnection(HttpServletResponse response, HttpServletRequest request)
	{
		try
		{
			SSm.getAppLogger().info("\n\ntestConnection\n\n");
			int port = Integer.valueOf(request.getParameter(PARAM_PORT));
			
			String url = request.getParameter(PARAM_URL);
			
			SimpleClient sc = new SimpleClient();
			SSm.getAppLogger().info("\n\nConfirming source at "+url+":"+port+"\n\n");
			sc.startConnection(url, port);
			String  scresponse=sc.sendMessage("MLController checking in");
			SSm.getAppLogger().info("Received "+scresponse);
			if(scresponse.toUpperCase().contains(String.valueOf(port)))
			{
					return "SUCCESS";    
			}
			else
			{
				SSm.getAppLogger().error("\n\n\nFAILED no connection back to source\n\n\n");
				return "FAILED";
			}
		}
		catch (Exception e)
		{
			SSm.getAppLogger().error(e.getMessage(), e);
			return "FAILED " + e.getMessage();
		}
	}
	
}
