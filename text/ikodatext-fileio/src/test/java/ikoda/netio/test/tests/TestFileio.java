package ikoda.netio.test.tests;

import static org.junit.Assert.*;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import ikoda.fileio.FileIoThread;
import ikoda.fileio.FileProcessorFactory;
import ikoda.netio.test.testrunner.TestSS;

public class TestFileio
{



	@Test
	public void testHtmlUnit() throws Exception
	{
		FileIoThread t = new FileIoThread();		
		String startingAddress="output";
		                                     
		/*t.start();
		t.runFileIo(startingAddress, FileProcessorFactory.INDEED);
		t.join();*/
	}
}
