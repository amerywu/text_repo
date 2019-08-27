package ikoda.fileio;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_JobDescriptionAnalysis;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Reddit;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.utils.IDGenerator;
import ikoda.utils.ProcessStatus;
import ikoda.utils.Spreadsheet;
import ikoda.utils.StaticSundryUtils;
import ikoda.utils.SimpleHtmlUnit;

public class CollegePrograms extends AbstractFileProcessor
{
    private final static String XML = "xml_";
    private final static String PDF = ".PDF";
    private final static String CATEGORY_LOG = "CATEGORY_LOG";
    
    private final static String HOST = "HOST";
    private final static String URI = "URI";
    private final static String CATEGORY = "CATEGORY";
    private final static String FULL_URL = "FULL_URL";
    private final static String PREDICTION = "PREDICTION";
    
    ConfigurationBeanForFileIo_Generic fioConfig;
    public CollegePrograms(ConfigurationBeanParent inconfig)
    {
        
        super(inconfig);
        fioConfig= (ConfigurationBeanForFileIo_Generic)config.getFioConfig();
        try
        {
        ConfigurationBeanForTextAnalysis_Reddit  taConfig=(ConfigurationBeanForTextAnalysis_Reddit )config.getTaConfig();
        String csvPath=taConfig.getCsvPath();
        if (!Files.exists(Paths.get(taConfig.getCsvPath())))
        {
            try
            {
             Files.createDirectories(Paths.get(taConfig.getCsvPath()));
            }
            catch(Exception e)
            {
                
                csvPath=fioConfig.getFileIoNonApplicablePath();
                FioLog.getLogger().warn("Csv path not configured correctly: "+taConfig.getCsvPath()+". Category Log path set to "+csvPath);
            }
        }
        Spreadsheet.getInstance().initCsvSpreadsheet(CATEGORY_LOG, FioLog.getLogger(),csvPath);
        }
        catch(Exception e)
        {
            FioLog.getLogger().error(e.getMessage(),e);
        }
        

    }

    @Override
    public void processFile(Path path, ReentrantLock inlock)
    {
        try
        {
            FioLog.getLogger().info("\n--------\nProcessing File: " + path.getFileName().toString());
            FioLog.getLogger().info("Path: " + path.toString().replace(path.getFileName().toString(),""));
            lock = inlock;

            if (path.getFileName().toString().contains(XML))
            {
                processXmlFile(path, fioConfig.getFileIoXMLPath());
                FioLog.getLogger().debug("XML\n");
                return;
            }
            String fileContents = null;
            try
            {
                if (lock.tryLock(10, TimeUnit.SECONDS))
                {
                    byte[] encoded = Files.readAllBytes(path);
                    fileContents = new String(encoded);
                }
            }
            finally
            {
                lock.unlock();
            }
            if (null == fileContents)
            {
                return;
            }

            triage(fileContents, path);

            FioLog.getLogger().info("Triage Done\n=======================\n\n");

        }
        catch (Exception e)
        {
            FioLog.getLogger().error(e.getMessage(), e);
        }
    }

    protected void processPostingAppend(Path p, String toDir, List<String> fileNameTags, String fileId, String content)
    {
        try
        {
            FioLog.getLogger().debug("processPostingAppend for tbid " + fileId);
            if (lock.tryLock(6, TimeUnit.SECONDS))
            {
                try
                {

                    FioLog.getLogger().debug("got lock: " + p.toString());

                    if (!(Paths.get(fioConfig.getFileIoJobPostingPath()).toFile().exists()))
                    {
                        Files.createDirectories(Paths.get(fioConfig.getFileIoJobPostingPath()));
                    }

                    Path dir = Paths.get(fioConfig.getFileIoJobPostingPath());
                    Path foundFile = null;

                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir))
                    {
                        for (Path entry : stream)
                        {

                            if (entry.toString().contains(fileId))
                            {
                                FioLog.getLogger().debug("found" + entry.toString());
                                foundFile = entry;
                                ProcessStatus.incrementStatus("FIO: Append To Existing File");
                                Files.write(entry, content.getBytes(), StandardOpenOption.APPEND);
                                break;
                            }
                        }
                    }
                    catch (IOException x)
                    {
                        // IOException can never be thrown by the iteration.
                        // In this snippet, it can // only be thrown by
                        // newDirectoryStream.
                        FioLog.getLogger().error(x.getMessage(), x);
                    }
                    /** actually, the above break should be a return */
                    if (null != foundFile)
                    {
                        return;
                    }
                    else
                    {

                        String filename = fileId + "_" + tagString(fileNameTags) + System.currentTimeMillis();
                        filename = filename.replaceAll("[^A-Za-z0-9]", "");
                        filename = filename + ".txt";
                        Path TO = Paths.get(fioConfig.getFileIoJobPostingPath() + File.separator + filename);

                        FioLog.getLogger().debug("\\n\\nNEW FILE  " + TO.getFileName().toString());
                        ProcessStatus.incrementStatus("FIO: Create New File");
                        Files.write(TO, content.getBytes(), StandardOpenOption.CREATE);
                        FioLog.getLogger().debug("done");
                    }

                }
                catch (Exception e)
                {
                    FioLog.getLogger().debug(p.toString());
                    FioLog.getLogger().error(e.getMessage(), e);
                }
                finally
                {
                    lock.unlock();
                }
            }
            else
            {
                FioLog.getLogger().warn("Failed to acquire lock");
            }
        }
        catch (Exception e)
        {
            FioLog.getLogger().error(e.getMessage(), e);
        }
    }

    protected void processPostingNewFile(Path p, String toDir, List<String> fileNameTags, String fileId, String content)
            throws InterruptedException
    {

        FioLog.getLogger().debug("processPostingAppend for tbid " + fileId);
        if (lock.tryLock(6, TimeUnit.SECONDS))
        {
            try
            {

                String filename = fileId + "_" + tagString(fileNameTags) + System.currentTimeMillis();
                filename = filename.replaceAll("[^A-Za-z0-9]", "");
                if(filename.length()>200)
                {
                	filename = filename.substring(0, 200);
                }
                filename = filename + ".txt";
                Path TO = Paths.get(toDir + File.separator + filename);

                FioLog.getLogger().debug("\\n\\nNEW FILE  " + TO.getFileName().toString());

                Files.write(TO, content.getBytes(), StandardOpenOption.CREATE);
                FioLog.getLogger().debug("done");

            }
            catch (Exception e)
            {
                FioLog.getLogger().debug(p.toString());
                FioLog.getLogger().error(e.getMessage(), e);
            }
            finally
            {
                lock.unlock();
            }
        }
        else
        {
            FioLog.getLogger().warn("Failed to acquire lock");
        }

    }

    protected void processPDF(Path p, List<String> tags)
    {
    	try
    	{
	        FioLog.getLogger().debug("PDF File");
	
	        String startTag = StringConstantsInterface.SPIDERTAG_TBID_OPEN;
	
	        String endTag = StringConstantsInterface.SPIDERTAG_TBID_CLOSE;
	
	        String tbid = StaticSundryUtils.extractHeader(startTag, endTag, p.toString());
	        if (null == tbid)
	        {
	            FioLog.getLogger().debug("Null tbid    returning");
	            return;
	        }
	        if (tbid.length() < 5)
	        {
	            FioLog.getLogger().debug("invalid tbid " + tbid + "   returning");
	            return;
	        }
	        String pdfText = extractTextFromPdf(p);
	        processPostingNewFile(p, fioConfig.getFileIoJobPostingPath(), tags, tbid, pdfText);
    	}
    	catch(Exception e)
    	{
    		FioLog.getLogger().error(e.getMessage(),e);
    	}
    }

    @Override
    protected void triage(String fileContent, Path p)
    {
        try
        {
            List<String> tags = new ArrayList<String>();
            if (p.getFileName().toString().toUpperCase().endsWith(PDF))
            {
                processPDF(p, tags);
                return;
            }

            FioLog.getLogger().debug("\n==============\ntriage for " + p.toString());

            String urlRepository = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_URLREPOSITORY_OPEN,
                    StringConstantsInterface.SPIDERTAG_URLREPOSITORY_CLOSE, fileContent);
            String host = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_HOST_OPEN,
                    StringConstantsInterface.SPIDERTAG_HOST_CLOSE, fileContent);
            String category = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_CATEGORY,
                    StringConstantsInterface.SPIDERTAG_CATEGORY_CLOSE, fileContent);
            String tbid = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_TBID_OPEN,
                    StringConstantsInterface.SPIDERTAG_TBID_CLOSE, fileContent);
            String uri = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_URI_OPEN,
                    StringConstantsInterface.SPIDERTAG_URI_CLOSE, fileContent);
            String fullURL = StaticSundryUtils.extractHeader(StringConstantsInterface.SPIDERTAG_FULLURL_OPEN,
                    StringConstantsInterface.SPIDERTAG_FULLURLCLOSE, fileContent);
            
            String uniqueKey = host + category + uri + fullURL;

            if (isDuplicate(uniqueKey))
            {
                FioLog.getLogger().debug("ABORT: duplicate " + uniqueKey + " for " + p.toString());
                tags.add("DUPLICATE");
                ProcessStatus.incrementStatus("FIO: Duplicate");
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath(), tags);
            }
            else if (null == host)
            {
                FioLog.getLogger().debug("ABORT: nonposting null host" + " for " + p.toString());
                ProcessStatus.incrementStatus("FIO: Null Host");
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath());
            }
            else if (null == tbid)
            {
                ProcessStatus.incrementStatus("FIO: Null TBID");
                FioLog.getLogger().debug("ABORT: nonposting null tbid" + " for " + p.toString());
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath());
            }
            else if (null == category)
            {
                ProcessStatus.incrementStatus("FIO: Null Category");
                FioLog.getLogger().debug("ABORT: nonposting null category  for " + p.toString());
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath());
            }
            else
            {
                String prediction = StringConstantsInterface.SPIDERTAG_PREDICTION_OPEN+getPrediction(quickClean(category),quickClean(fullURL))+StringConstantsInterface.SPIDERTAG_PREDICTION_CLOSE;
                
                ProcessStatus.put("FIO: Last Prediction", prediction);
                FioLog.getLogger().debug("====>  posting ====>");
                ProcessStatus.incrementStatus("FIO: Processed");
                String tag = urlRepository+host + uri+prediction;
                tag = tag.replaceAll("[^A-Za-z0-9]", "");
                tag = "[[" + tag + "]]";

                tags.add(tag);
                FioLog.getLogger().debug("to clean ");
                String cleanerText = StaticSundryUtils.removeListsAndMenus(prediction+fileContent);
                logCategory(host, category, uri, fullURL,prediction);
                processPostingNewFile(p, fioConfig.getFileIoOutBoxPath(), tags, tbid, cleanerText);
                FioLog.getLogger().info("predicted "+prediction+"\n\n");
                ProcessStatus.incrementStatus("FIO: Sent for TA");
            }
        }
        catch (Exception e)
        {
            FioLog.getLogger().error(e.getMessage(), e);

        }
    }
    private String truncate(String s,int maxLength)
    {
    	if(s.length()>maxLength)
    	{
    		return s.substring(0, maxLength);
    	}
    	return s;
    }
    

    private String getPrediction(String category, String url)
    {
        try
        {
            FioLog.getLogger().info("getPrediction "+category+" "+url);
            SimpleHtmlUnit shu = new SimpleHtmlUnit(config.getMlServerUrl(),config.getMlServerPort());

            HashMap<String,String> params=new HashMap();
            params.put("id","0");
            params.put(category,"title");
            params.put("url",url);
            String response = shu.getAsText("plaintext",params);
            return truncate(response,150);

        }
        catch(Exception e)
        {
            FioLog.getLogger().error(e.getMessage(),e);
            return "FAILED";
        }
    }
    
    private String quickClean(String s)
    {
    	s=cleanString(s);
        s=s.replace("/","|");
        s=s.replace("-"," ");
        s=s.replace(","," ");
        return s;
    }
    
    private void logCategory(String host, String category, String uri, String fullUrl, String prediction)
    {
        try
        {
            host=quickClean(host);
            category=quickClean(category);
            uri=quickClean(uri);

            fullUrl=quickClean(fullUrl);
            
            FioLog.getLogger().debug("logCategory");
            String uid=String.valueOf(IDGenerator.getInstance().nextID());
            FioLog.getLogger().debug(uid);
            Spreadsheet.getInstance().getCsvSpreadSheet(CATEGORY_LOG).addCell(uid, HOST, host);
            Spreadsheet.getInstance().getCsvSpreadSheet(CATEGORY_LOG).addCell(uid, URI, uri);
            Spreadsheet.getInstance().getCsvSpreadSheet(CATEGORY_LOG).addCell(uid, CATEGORY, category);
            Spreadsheet.getInstance().getCsvSpreadSheet(CATEGORY_LOG).addCell(uid, FULL_URL, fullUrl);
            Spreadsheet.getInstance().getCsvSpreadSheet(CATEGORY_LOG).addCell(uid, PREDICTION, prediction);
            Spreadsheet.getInstance().getCsvSpreadSheet(CATEGORY_LOG).printCsvAppend(CATEGORY_LOG+".csv");
        }
        catch(Exception e)
        {
            FioLog.getLogger().error(e.getMessage(),e);
        }
    }

}
