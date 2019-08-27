package ikoda.nlp.structure;

import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

import ikoda.netio.config.ConfigurationBeanParent;

public class AnalyzedFileFactory
{

    public final static int GENERIC_JOB_POST = 0;
    public final static int JOB_DESCRIPTION = 1;
    public final static int CHINESE_JOB_POST = 2;

    public static AbstractAnalyzedText getAnalyzedFile(int type, Path inpath, ReentrantLock inlock,
            ConfigurationBeanParent config)
    {
        if (type == GENERIC_JOB_POST)
        {
            return new JobPostAnalyzedFile(inpath, inlock, config);
        }
        if (type == JOB_DESCRIPTION)
        {
            return new JobDescriptionAnalyzedFile(config);
        }
        if (type == CHINESE_JOB_POST)
        {
            return new ChineseJobPostAnalyzedFile(inpath, inlock, config);
        }
        return null;
    }

    public AnalyzedFileFactory()
    {
        // TODO Auto-generated constructor stub
    }

}
