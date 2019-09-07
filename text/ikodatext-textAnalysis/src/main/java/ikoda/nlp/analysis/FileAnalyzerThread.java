package ikoda.nlp.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.TokensRegexNERAnnotator;
import edu.stanford.nlp.util.PropertiesUtils;
import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanForTextAnalysis_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.nlp.structure.IKodaTextAnalysisException;
import ikoda.nlp.structure.PropertiesSingleton;
import ikoda.utils.FileList;
import ikoda.utils.IKodaUtilsException;
import ikoda.utils.ProcessStatus;

public class FileAnalyzerThread extends FileAnalyzerThreadManagement
{

    public static String infoBoxPath = ".";
    protected static final String TXT = ".txt";
    protected static final String PATHSNAME="FileAnalyzerThread";




    protected static String donePath = ".";

    protected static String failedPath = ".";
    protected static String ignoredPath = ".";




    protected static ConfigurationBeanParent config;
    
    protected ReentrantLock lock;
    protected static String analysisSubType;
    protected static String nerPropertiesPath;

    protected static String currentFilePath;

    protected StanfordCoreNLP pipeline;
    int source = 0;

    public FileAnalyzerThread()
    {
        fileProcessStartTime = System.currentTimeMillis();
    }

    public FileAnalyzerThread(int inthreadId)
    {
        fileProcessStartTime = System.currentTimeMillis();
        threadId = inthreadId;
    }
    
	public synchronized void abort()
	{
	    
	    TALog.getLogger().warn("\n\nCurrent File: " + currentFilePath + "\n\n");
	    super.abort();
	}
	
	public synchronized void restart()
	{
	    TALog.getLogger().warn("\n\nCurrent File: " + currentFilePath + "\n\n");
	    super.restart(); 
	}
	
	
	public long getFileProcessTime() throws IKodaUtilsException
	{
	    if(FileList.getInstance().isEmptyFileList(PATHSNAME))
	    {
	        return 0;
	    }
	    if (!continueRun)
	    {
	        return 0;
	    }
	    return System.currentTimeMillis() - fileProcessStartTime;
	}
    
    protected synchronized boolean doFileProcess()
    {

        try
        {
            fileProcessStartTime = System.currentTimeMillis();

           
            if (isAborted() || isRestart())
            {
                TALog.getLogger().warn("\n\n Abort called \n\n " + this.toString());
                return false;
            }
            if (FileList.getInstance().isEmptyFileList(PATHSNAME))
            {
                return replenishFileList();

            }
            sleepTime = 3000;
            

            
            Path p = FileList.getInstance().getNextFile(PATHSNAME);
            TALog.getLogger().debug("Processing " + threadId + " : " + p);
            if (null == p)
            {
            	replenishFileList();
                return true;
            }
            currentFilePath = p.toString();
            if (p.getFileName().toString().contains(TXT))
            {
                TALog.getLogger().debug("handling file " + p.getFileName());
                boolean result = pipeline(p);
            }
            else
            {
                moveUndeterminedFile(p);
            }

            TALog.getLogger().debug("\n\n\n\n\n\n\n File IO: Number of files processed: " + FileList.getInstance().getTotalCount());

            ProcessStatus.getStatusMap().put("TA Files with Text Analyzed T:" + getThreadId(),
                    String.valueOf(getFileCount()));

            return true;
        }
        catch (Exception e)
        {
            FileList.getInstance().remove(PATHSNAME,0);
            TALog.getLogger().error(threadId + " : " + e.getMessage(), e);
            ProcessStatus.incrementStatus(getThreadId() + "TA errors in FileAnalyzerThread");
            return true;
        }
    }

    public int getFileCount()
    {
        return FileList.getInstance().getTotalCount();
    }

    public StanfordCoreNLP getPipeline() throws IKodaTextAnalysisException
    {
        if (null == pipeline)
        {
            initializeNlp(config.getSpecifiedLanguage());
        }
        return pipeline;
    }

    protected int getSleepTime()
    {
        return sleepTime;
    }

    /** Used when initializing as field variable */
    public void initializeFileAnalyzer(ConfigurationBeanParent inconfig, ReentrantLock inlock)
            throws IOException, Exception
    {
        TALog.getLogger().debug("Starting " + this.toString());
        fileProcessStartTime = System.currentTimeMillis();

        config = inconfig;
        TALog.getLogger().debug("Language is " + config.getSpecifiedLanguage());
        TALog.getLogger().debug("Analysis Type is " + config.getAnalysisType());
        TALog.getLogger().debug("SubType is " + config.getAnalysisSubType());
        analysisSubType = config.getAnalysisSubType();

        source = FileAnalyzerFactory.setSource(config);

        lock = inlock;

        ready = true;

        initializeNlp(config.getSpecifiedLanguage());

        TALog.getLogger().debug("initialized");

    }
    
    protected void initializeNlp(String language)throws IKodaTextAnalysisException
    {
        initializeNlp(language,"");
    }
    

    protected void initializeNlp(String language, String annotators) throws IKodaTextAnalysisException
    {

        try
        {
            TALog.getLogger().debug("\n\n\nStarting Stanford NLP for " + language);

            
            
            /**
             * creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER,
             * parsing, and
             */
            if (language.equals(ConfigurationBeanParent.LANGUAGE_EN))
            {
                PropertiesUtils.Property[] nerprops = TokensRegexNERAnnotator.SUPPORTED_PROPERTIES;

                for (int i = 0; i < nerprops.length; i++)
                {

                    if (nerprops[i].name().equals("backgroundSymbol"))
                    {

                        nerprops[i] = new PropertiesUtils.Property("backgroundSymbol", "O,MISC,TITLE,ORGANIZATION",
                                "Comma separated list of NER labels to always replace.");

                    }
                }
                Properties props = new Properties();

                if(annotators.isEmpty())
                {
                
                props.put("annotators",
                        "tokenize, ssplit, pos, lemma, ner,  regexner");
                }
                else
                {
                    props.put("annotators",annotators);
                }

                pipeline = new StanfordCoreNLP(props);

                if (null == nerPropertiesPath)
                {
                    TALog.getLogger().warn("\n\nNo NerProperties Configured.\n");
                }
                else
                {
                    if (!(nerPropertiesPath.contains(ConfigurationBeanParent.LANGUAGE_EN)))
                    {
                        throw new IKodaTextAnalysisException("Expected _en in file name for " + nerPropertiesPath);
                    }
                    TokensRegexNERAnnotator nerAnnotator = new TokensRegexNERAnnotator(nerPropertiesPath, true);

                    pipeline.addAnnotator(nerAnnotator);
                    PropertiesSingleton.getInstance().initializeNERProperties(nerPropertiesPath);
                }

            }
            else if (language.equals(ConfigurationBeanParent.LANGUAGE_ZH))
            {

                TALog.getLogger().debug("Chinese configuration");

                Properties props = PropertiesSingleton.getInstance().getChineseProperties();
                // that properties file will run the entire pipeline
                // if you uncomment the following line it will just go up to ner
                // props.setProperty("annotators","segment,ssplit,pos,lemma,ner");
                pipeline = new StanfordCoreNLP(props);

                if (!(nerPropertiesPath.contains(ConfigurationBeanParent.LANGUAGE_ZH)))
                {
                    throw new IKodaTextAnalysisException("Expected _zh_CN in file name for " + nerPropertiesPath);
                }

                TokensRegexNERAnnotator nerAnnotator = new TokensRegexNERAnnotator(nerPropertiesPath, false);

                pipeline.addAnnotator(nerAnnotator);
                PropertiesSingleton.getInstance().initializeNERProperties(nerPropertiesPath);
            }
            TALog.getLogger().info("Stanford Initialized");

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            this.interrupt();
            continueRun = false;

            throw new IKodaTextAnalysisException(e.getMessage(),e);
        }

        TALog.getLogger().debug("\n\n\nStarted Stanford NLP Successfully\n\n\n");
    }

    protected void moveFailedFile(Path p, String status)
    {

        try
        {
            ProcessStatus.incrementStatus("TA Analysis Failed");
            if (lock.tryLock(6, TimeUnit.SECONDS))
            {
                try
                {

                   
                    Path movefrom = p;
                    Path target = FileSystems.getDefault()
                            .getPath(failedPath + File.separator + status + "_" + truncate(p.getFileName().toString(),200));
                    // method 1
                    Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
                    

                }
                catch (Exception e)
                {
                    TALog.getLogger().error(e.getMessage());
                }
                finally
                {
                    lock.unlock();
                    
                }
            }
            else
            {
                TALog.getLogger().warn("Could not acquire lock");
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    protected void moveSuccessfulFile(Path p, String status)
    {
        try
        {

            if (lock.tryLock(6, TimeUnit.SECONDS))
            {
                try
                {
                    
                    Path movefrom = p;
                    Path target = FileSystems.getDefault()
                            .getPath(donePath + File.separator + status + "_" + truncate(p.getFileName().toString(),200));
                    // method 1

                    Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
                    
                }
                catch (Exception e)
                {
                    TALog.getLogger().error(e.getMessage());
                }
                finally
                {
                    lock.unlock();
                   
                }
            }
            else
            {
                TALog.getLogger().warn("Could not acquire lock");
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e);
        }
    }

    protected void moveUndeterminedFile(Path p)
    {

        ProcessStatus.incrementStatus(getThreadId() + "TA Undetermined");
        try
        {
            if (lock.tryLock(6, TimeUnit.SECONDS))
            {
                try
                {
                 
                    Path movefrom = p;
                    Path target = FileSystems.getDefault().getPath(ignoredPath + File.separator + truncate(p.getFileName().toString(),200));
                    // method 1
                    Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
                    

                }
                catch (Exception e)
                {
                    TALog.getLogger().error(e.getMessage(), e);
                }
                finally
                {
                    lock.unlock();
                  
                }
            }
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e);
        }
    }

    protected boolean pipeline(Path file)
    {
        TALog.getLogger().debug("===========pipeline========================");
        AbstractTextAnalyzer fp = FileAnalyzerFactory.getProcessorForDataSource(source, pipeline, config);
        boolean result = fp.processFile(file, lock);
        if (result)
        {
            moveSuccessfulFile(file, fp.getFinalStatus());
        }
        else
        {
            moveFailedFile(file, fp.getFinalStatus());
        }
        return result;
    }

    protected boolean replenishFileList()
    {
        try
        {

            FileList.getInstance().listFiles(PATHSNAME);
            TALog.getLogger().debug("Tried to get more files and came up with " + FileList.getInstance().size(PATHSNAME));
            if (FileList.getInstance().isEmptyFileList(PATHSNAME))
            {
                sleepTime = 20000;
                if (stopDependsOnFileio)
                {
                    if (upstreamThreadDone)
                    {
                        TALog.getLogger().info("Ending. Dependent threads complete and I am complete");
                        return false;
                    }
                    else
                    {
                        TALog.getLogger().info("Dependent threads active. Waiting");
                        return true;
                    }
                }
                else
                {
                    TALog.getLogger().info("Ending.No dependents and I am complete");
                    return false;
                }

            }
            return true;
        }
        catch (Exception e)
        {
            TALog.getLogger().debug(e.getMessage(), e);
            return false;
        }
    }

    public void resetFileProcessStartTime()
    {
        fileProcessStartTime = System.currentTimeMillis();
    }

    public void resetFileProcessTime()
    {
        this.fileProcessStartTime = System.currentTimeMillis();
    }

    @Override
    public void run()
    {
        try
        {
            TALog.getLogger().info("Starting thread");
            int timeoutNoDataReceived = 0;
            while (continueRun)
            {
                if (lock.isHeldByCurrentThread())
                {
                    TALog.getLogger().warn("Releasing lock before going to sleep. This is bad");
                    lock.unlock();
                }
               // TALog.getLogger().debug("Sleeping for " + (sleepTime + baseSleepTime));
                Thread.sleep(sleepTime + baseSleepTime);

                if ( ready == false)
                {
                    timeoutNoDataReceived++;
                    if (timeoutNoDataReceived >= 500)
                    {
                        continueRun = false;
                    }
                }
                else if (!doFileProcess())
                {
                    TALog.getLogger().info("EXITING THREAD");
                    continueRun = false;
                }

            }
        }
        catch (InterruptedException e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return;

        }
        catch (Exception e)
        {
            TALog.getLogger().error("This is Bad. Thread won't start");
            TALog.getLogger().error(e.getMessage(), e);
        }
        catch (Error err)
        {
            TALog.getLogger().fatal("\n\n\n\n\n\n\n\n" + err.getMessage(), err);
            throw err;
        }

    }

    public void runFileAnalyzer(boolean depends, ConfigurationBeanParent inconfig, ReentrantLock inlock)
            throws IOException, IKodaTextAnalysisException, IKodaUtilsException
    {
        try {



        TALog.getLogger().info("Starting " + this.toString());
        config = inconfig;
        analysisSubType = config.getAnalysisSubType();
        if (null == analysisSubType)
        {
            throw new IKodaTextAnalysisException("analysisSubType is NULL");
        }
        source = FileAnalyzerFactory.setSource(config);
        if (inconfig.getAnalysisType().equals(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION))
        {
            ConfigurationBeanForTextAnalysis_Generic configt = (ConfigurationBeanForTextAnalysis_Generic) inconfig
                    .getTaConfig();

            fileProcessStartTime = System.currentTimeMillis();

            donePath = configt.getTextAnalysisDonePath();
            infoBoxPath = configt.getTextAnalysisInfoBoxPath();
            failedPath = configt.getTextAnalysisFailedPath();
            ConfigurationBeanForFileIo_Generic fioConfig = (ConfigurationBeanForFileIo_Generic) inconfig.getFioConfig();
            ignoredPath = fioConfig.getFileIoUndeterminedPath();

            nerPropertiesPath = configt.getNerPropertiesPath();
            if (null == nerPropertiesPath)
            {
                throw new IKodaTextAnalysisException("nerPropertiesPath is NULL");
            }
            if (inconfig.isMinimizeResourceUse())
            {
                baseSleepTime = 60000;
            }

            lock = inlock;

            if (!(Paths.get(donePath).toFile().exists()))
            {
                Files.createDirectories(Paths.get(donePath));
            }
            if (!(Paths.get(infoBoxPath).toFile().exists()))
            {
                Files.createDirectories(Paths.get(infoBoxPath));
            }
            if (!(Paths.get(failedPath).toFile().exists()))
            {
                Files.createDirectories(Paths.get(failedPath));
            }
            if (!(Paths.get(ignoredPath).toFile().exists()))
            {
                Files.createDirectories(Paths.get(ignoredPath));
            }
            if (!(Paths.get(fioConfig.getFileIoJobPostingPath()).toFile().exists()))
            {
                Files.createDirectories(Paths.get(fioConfig.getFileIoJobPostingPath()));
            }
            if (!(Paths.get(fioConfig.getFileIoUndeterminedPath()).toFile().exists()))
            {
                Files.createDirectories(Paths.get(fioConfig.getFileIoUndeterminedPath()));
            }

            stopDependsOnFileio = depends;
            if (depends)
            {
                upstreamThreadDone = false;
            }

            String[] inputs = { fioConfig.getFileIoJobPostingPath(), fioConfig.getFileIoUndeterminedPath() };

            List<Path> inputPaths = new ArrayList<>();
            for (int i = 0; i < inputs.length; i++)
            {
                Path path = Paths.get(inputs[i]);
                inputPaths.add(path);
            }
            FileList.getInstance().init(PATHSNAME, inputPaths, TALog.getLogger(), inlock);
            FileList.getInstance().listFiles(PATHSNAME);
            TALog.getLogger().debug("File Count "+FileList.getInstance().size(PATHSNAME));
            ready = true;

            initializeNlp(inconfig.getSpecifiedLanguage());

            TALog.getLogger().debug("starting thread");
        }
        else
        {
            throw new IKodaTextAnalysisException("Wrong configuration descriptor: "+inconfig.getAnalysisType());
        }
        }
        catch(Exception e)
        {
            throw new IKodaTextAnalysisException("Failed: "+e.getMessage(),e);
        }

    }



}
