package ikoda.nlp.analysis;

import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InterfaceConfigurationBeanParent;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.structure.IKodaTextAnalysisException;

public class FileAnalyzerFactory
{

    protected static CRFClassifier<CoreLabel> segmenter;

    public static AbstractTextAnalyzer getProcessorForDataSource(int job, StanfordCoreNLP pipeline,
            ConfigurationBeanParent config)
    {
        TALog.getLogger().debug("Getting analyzer for " + job);

        if (job == StringConstantsInterface.DATASOURCE_GENERIC_JOB_POST)
        {

            return new JobPostAnalyzer(pipeline, config);

        }
        if (job == StringConstantsInterface.DATASOURCE_DIPLOMALEVEL_JOB_POST)
        {

            return new DiplomaJobPostAnalyzer(pipeline, config);

        }
        if (job == StringConstantsInterface.DATASOURCE_JOB_DESCRIPTION)
        {
            return new JobDescriptionAnalyzer(pipeline, config);
        }
        if (job == StringConstantsInterface.DATASOURCE_COLLEGE_PROGRAM)
        {
            return new CollegeProgramAnalyzer(pipeline, config);
        }
        if (job == StringConstantsInterface.DATASOURCE_CHINESE_JOB_POST)
        {

            if (null == segmenter)
            {
                try
                {
                    initSegmenterForChinese();
                }
                catch (Exception e)
                {
                    return null;
                }
            }
            return new ChineseJobPostAnalyzer(pipeline, segmenter, config);
        }
        return null;
    }

    public static AbstractTextAnalyzer getProcessorForDataSourceByAnalysisType(StanfordCoreNLP pipeline,
            ConfigurationBeanParent config)
    {

        if (config.getAnalysisType().equals(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION))
        {
            if (config.getAnalysisSubType().equals(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION_DEGREE_SUBTYPE))
            {

                if (config.getSpecifiedLanguage().equals(ConfigurationBeanParent.LANGUAGE_ZH))
                {

                    if (null == segmenter)
                    {
                        try
                        {
                            initSegmenterForChinese();
                        }
                        catch (Exception e)
                        {
                            return null;
                        }
                    }
                    return new ChineseJobPostAnalyzer(pipeline, segmenter, config);
                }
                else
                {
                    return new JobPostAnalyzer(pipeline, config);
                }
            }
            else if (config.getAnalysisSubType()
                    .equals(ConfigurationBeanParent.JOB_ANALYSIS_CONFIGURATION_DIPLOMA_SUBTYPE))
            {
                return new DiplomaJobPostAnalyzer(pipeline, config);
            }
            else
            {
                TALog.getLogger().error("No analyzer for subtype" + config.getAnalysisSubType());
                return null;
            }

        }

        if (config.getAnalysisType().equals(ConfigurationBeanParent.JOB_DESCRIPTION_ANALYSIS_CONFIGURATION))
        {
            return new JobDescriptionAnalyzer(pipeline, config);
        }
        if (config.getAnalysisType().equals(ConfigurationBeanParent.COLLEGE_ANALYSIS_CONFIGURATION))
        {
            return new CollegeProgramAnalyzer(pipeline, config);
        }

        return null;
    }

    public static void initSegmenterForChinese() throws Exception
    {
        try
        {
            TALog.getLogger().debug("\n\n\n\ninitSegmenterForChinese\n\n\n");
            Properties props = new Properties();
            props.setProperty("sighanCorporaDict", "edu/stanford/nlp/models/segmenter/chinese");
            // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
            // props.setProperty("normTableEncoding", "UTF-8");
            // below is needed because CTBSegDocumentIteratorFactory accesses it
            props.setProperty("serDictionary", "edu/stanford/nlp/models/segmenter/chinese/dict-chris6.ser.gz");

            props.setProperty("inputEncoding", "UTF-8");
            props.setProperty("sighanPostProcessing", "true");

            segmenter = new CRFClassifier<>(props);
            segmenter.loadClassifierNoExceptions("edu/stanford/nlp/models/segmenter/chinese/ctb.gz", props);

            TALog.getLogger().debug("CRFClassifier " + segmenter);
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            throw e;
        }

    }

    protected static int setSource(ConfigurationBeanParent config) throws IKodaTextAnalysisException
    {
        try
        {
            int source = 0;
            if (config.getSpecifiedLanguage().equals(InterfaceConfigurationBeanParent.LANGUAGE_EN))
            {
                if (config.getAnalysisSubType().equals(StringConstantsInterface.JOB_ANALYSIS_SUBTYPE_DEGREE))
                {
                    source = StringConstantsInterface.DATASOURCE_GENERIC_JOB_POST;
                }
                else if (config.getAnalysisSubType().equals(StringConstantsInterface.JOB_ANALYSIS_SUBTYPE_DIPLOMA))
                {
                    source = StringConstantsInterface.DATASOURCE_DIPLOMALEVEL_JOB_POST;
                }
                else if (config.getAnalysisSubType().equals(StringConstantsInterface.COLLEGE_ANALYSIS_SUBTYPE_DIPLOMA)
                        || config.getAnalysisSubType().equals(StringConstantsInterface.BLOG_SUBTYPE))
                {
                    source = StringConstantsInterface.DATASOURCE_COLLEGE_PROGRAM;
                }
                else if (config.getAnalysisSubType().equals(StringConstantsInterface.JOB_DESCRIPTION_ANALYSIS_SUBTYPE))
                {
                    source = StringConstantsInterface.DATASOURCE_JOB_DESCRIPTION;
                }
                else
                {
                    throw new IKodaTextAnalysisException("Unknown analysisSubType: " + config.getAnalysisSubType());
                }
            }
            else if (config.getSpecifiedLanguage().equals(InterfaceConfigurationBeanParent.LANGUAGE_ZH))
            {
                source = StringConstantsInterface.DATASOURCE_CHINESE_JOB_POST;
            }
            else
            {
                throw new IKodaTextAnalysisException("Unknown Language " + config.getSpecifiedLanguage());
            }
            TALog.getLogger().debug("Source set to " + source);
            return source;
        }
        catch (Exception e)
        {
            throw new IKodaTextAnalysisException(e.getMessage(), e);
        }
    }

    public FileAnalyzerFactory()
    {
        // TODO Auto-generated constructor stub
    }

}
