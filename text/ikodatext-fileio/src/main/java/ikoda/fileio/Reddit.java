package ikoda.fileio;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanForFileIo_Generic;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.utils.ProcessStatus;
import ikoda.utils.StaticSundryUtils;

public class Reddit extends CollegePrograms
{
    private static final String XML = "xml_";
    private static final String PDF = ".PDF";
    private static final String ONE_UNDERSCORE = "1_";
    ConfigurationBeanForFileIo_Generic fioConfig;
    public Reddit(ConfigurationBeanParent inconfig)
    {
        super( inconfig);
        fioConfig= (ConfigurationBeanForFileIo_Generic)config.getFioConfig();

    }

    @Override
    public void processFile(Path path, ReentrantLock inlock)
    {
        try
        {
            super.processFile(path, inlock);
        }
        catch (Exception e)
        {
            FioLog.getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    protected void triage(String fileContent, Path p)
    {
        try
        {
            if(p.getFileName().toString().toUpperCase().startsWith(ONE_UNDERSCORE))
            {
                return;
            }
            List<String> tags = new ArrayList<>();
            if (p.getFileName().toString().toUpperCase().endsWith(PDF))
            {
                processPDF(p, tags);
                return;
            }

            FioLog.getLogger().debug("triage for " + p.toString());

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

            FioLog.getLogger().debug("tbid: " + tbid);

            if (null == host)
            {
                FioLog.getLogger().debug("nonposting null host" + " for " + p.toString());
                ProcessStatus.incrementStatus("FIO: Null Host");
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath());
            }
            else if (null == tbid)
            {
                FioLog.getLogger().debug("nonposting null tbid" + " for " + p.toString());
                ProcessStatus.incrementStatus("FIO: Null TBID");
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath());
            }
            else if (null == category)
            {
                ProcessStatus.incrementStatus("FIO: Null Category");
                FioLog.getLogger().debug("nonposting null category  for " + p.toString());
                processNonPosting(p, fioConfig.getFileIoNonApplicablePath());
            }
            else
            {
                FioLog.getLogger().debug("posting.===");
                String tag = urlRepository+ host + uri;
                tag = tag.replaceAll("[^A-Za-z0-9]", "");
                tag = "[[" + tag + "]]";

                tags.add(tag);
                FioLog.getLogger().debug("to clean ");
                String cleanerText = StaticSundryUtils.removeListsAndMenus(fileContent);
                ProcessStatus.incrementStatus("FIO: File Processed");
                processPostingNewFile(p, fioConfig.getFileIoOutBoxPath(), tags, tbid, cleanerText);
            }
        }
        catch (Exception e)
        {
            FioLog.getLogger().error(e.getMessage(), e);
        }
    }

}
