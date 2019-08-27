package ikoda.nlp.structure;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.netio.config.InfoBox;
import ikoda.netio.config.StringConstantsInterface;
import ikoda.nlp.analysis.FileAnalyzerThread;
import ikoda.nlp.analysis.TALog;

public abstract class AnalyzedFile extends AbstractAnalyzedText
{

    protected final static String COMMASPACE = ", ";

    protected final static String TAB = "\t";
    protected final static String NEWLINE = "\n";

    public final static String NEWBLOCK = "NEWBLOCK";

    protected Path path;
    protected Map<String, InfoBox> infoBoxMap = new TreeMap<String, InfoBox>();
    private String infoBoxUID = "";
    private boolean blockChange = false;

    protected String id;

    private String region = "Generic";
    private String source = "Generic";
    private ReentrantLock lock;
    protected String contentType = "Undetermined";

    AnalyzedFile(Path inpath, ReentrantLock inLock, ConfigurationBeanParent config)
    {
        super(config);
        lock = inLock;
        path = inpath;
        TALog.getLogger().debug("\n\n+++++++++++++++++++++++++++++++++++++++++++++++");
        TALog.getLogger().debug("\nNEW FILE " + inpath.getFileName());

    }

    public void addToInfoBox(String type, IdentifiedToken itoken)
    {
        if (null == itoken)
        {
            return;
        }
        TALog.getLogger().debug("addToInfoBox itoken: " + itoken.getValue());
        blockChange = true;
        if (addToInfoBoxExtension(type, itoken))
        {

            String value = itoken.getValue();
            TALog.getLogger().debug("addToInfoBox adding: " + itoken.getValue());
            addToInfoBoxForReal(type, value);

            /// must be last line or blocknumber might be wrong

        }
        addiTokenToCounter(itoken, new Integer(blocknumber));
    }

    /*
     * public void addToInfoBox(String type, List<IdentifiedToken> itokens) {
     * blockChange = true;
     * 
     * NioLog.getLogger().debug("addToInfoBox list"); for (IdentifiedToken itoken :
     * itokens) { if (addToInfoBoxExtension(type, itoken)) {
     * addToInfoBoxForReal(type, itoken.getValue()); /// must be last line or
     * blocknumber might be wrong analyzedFileTokenCounter.addToken(itoken, new
     * Integer(blocknumber)); } } }
     * 
     * public void addToInfoBox(String type, String invalue) {
     * 
     * blockChange = true; if (invalue.isEmpty()) { return; } if
     * (addToInfoBoxExtension(type, invalue)) {
     * NioLog.getLogger().debug("addToInfoBox string " + invalue);
     * 
     * addToInfoBoxForReal(type, invalue); }
     * 
     * }
     */

    protected abstract boolean addToInfoBoxExtension(String type, IdentifiedToken itoken);

    private void addToInfoBoxForReal(String type, String value)
    {
        String key = generateInfoBoxMapKey();
        InfoBox infoBox = infoBoxMap.get(key);

        if (null == infoBox)
        {
            TALog.getLogger().debug("addToInfoBox NEW INFO BOX with key: " + key);
            infoBox = new InfoBox(id);
            infoBox.setInfoBoxUID(infoBoxUID);
            infoBox.setDatabaseDescriptor(config.getDatabaseDescriptor());
            TALog.getLogger().debug("ib uid " + infoBoxUID);
            infoBox.setBlockNumber(blocknumber);
            infoBox.setRegion(region);
            infoBox.setContentType(contentType);
            infoBoxMap.put(key, infoBox);

        }

        TALog.getLogger().debug(
                "\n\n\nAdding " + type + " : " + value + "\n\nkey: " + key + "\nibuid: " + infoBox.getInfoBoxUID());

        infoBox.addValueToInfoBox(type, value);
    }

    public void clear()
    {
        dumpStanford();
    }

    /*
     * public abstract boolean addToInfoBoxExtension(String type,
     * List<IdentifiedToken> itokens);
     * 
     * public abstract boolean addToInfoBoxExtension(String type, String invalue);
     */

    protected abstract boolean containsInfoBoxUIDIgnoreWords(String sentence);

    public void finalizeFile()
    {
        try
        {

            TALog.getLogger().debug("\n\n\n\n\nFinalizing file\n\n\n\n\n");
            TALog.getLogger().debug("infoBoxMap size " + infoBoxMap.size());

            ///// make sure info box gets closed out properly
            newBlock();

            dumpStanford();

            allSentences.clear();

            TALog.getLogger().debug(printInfoBox());

            finalizeFileExtension();
            System.gc();
            successfulRun = postProcessInfoBoxes();

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            successfulRun = false;
        }
    }

    public abstract boolean finalizeFileExtension();

    protected String generateInfoBoxMapKey()
    {
        return blocknumber + TWOHASHTAG;
    }

    protected abstract String generateInfoBoxUID(String s);

    public String getContentType()
    {
        return contentType;
    }

    public PossibleSentence getCurrentSentence()
    {
        return currentSentence;
    }

    public List<String> getExtantIdentifiedTokensForType(String tokenType)
    {
        String key = generateInfoBoxMapKey();

        InfoBox infoBox = infoBoxMap.get(key);

        if (null == infoBox)
        {
            return new ArrayList<String>();
        }
        return infoBox.getValuesByType(tokenType);
    }

    public List<FinalSentence> getFinalSentences()
    {
        Collection<FinalSentence> c = finalSentences.values();
        List<FinalSentence> fSentences = new ArrayList<FinalSentence>(c);
        return Collections.unmodifiableList(fSentences);
    }

    public Path getPath()
    {
        return path;
    }

    public PossibleSentence getPossibleSentenceByPosition(Integer position)
    {
        return allSentences.get(position);
    }

    public List<PossibleSentence> getPossibleSentences()
    {
        Collection<PossibleSentence> c = allSentences.values();
        List<PossibleSentence> possibleSentences = new ArrayList<PossibleSentence>(c);
        return Collections.unmodifiableList(possibleSentences);
    }

    public String getRegion()
    {
        return region;
    }

    public String getSource()
    {
        return source;
    }

    public abstract int identifiedTokenCountForType(String tokenType);

    public int identifiedTokenFrequency(String tokenType, String value)
    {
        int frequency = 0;
        for (PossibleSentence psentence : allSentences.values())
        {
            for (IdentifiedToken itoken : psentence.iTokensGetAll())
            {
                if (itoken.getType().equals(tokenType) && itoken.getValue().equals(value)
                        && itoken.getInfoBoxId() == blocknumber)
                {
                    frequency += itoken.getFrequencyCount();
                }
            }
        }
        return frequency;
    }

    protected abstract boolean isSaveableInfoBox(InfoBox ib);

    public void newBlock()
    {
        TALog.getLogger().debug("\n\n+++++++++++++++++++++\n\n NEW BLOCK\n\n++++++++++++++++++++\n");

        ////// set a hopefully unique id for ib to prevent the recording of duplicate
        ////// postings
        String key = generateInfoBoxMapKey();
        InfoBox infoBox = infoBoxMap.get(key);
        if (null != infoBox)
        {
            infoBox.setInfoBoxUID(infoBoxUID);
        }

        if (blockChange)
        {
            blocknumber++;
            blockChange = false;
        }
        infoBoxUID = "";
    }

    @Override
    public PossibleSentence nextSentence(CoreMap sentence)
    {
        if (null != currentSentence && currentSentence.toString().length() > 3)
        {
            precedingSentence = currentSentence;
        }
        dumpStanford();
        PossibleSentence ps = new PossibleSentence(sentence);
        ps.setInfoBoxId(blocknumber);
        String sentenceString = sentence.toString();
        if (sentenceString.length() > infoBoxUID.length())
        {
            if (!containsInfoBoxUIDIgnoreWords(sentenceString))
            {
                String uidString = generateInfoBoxUID(sentenceString);

                infoBoxUID = uidString;
                TALog.getLogger().debug("infoBoxUID changed to " + infoBoxUID);
            }
        }

        Optional<Integer> o = allSentences.keySet().stream().max(Comparator.naturalOrder());
        if (o.isPresent())
        {
            Integer currentTopPosition = o.get();
            // NioLog.getLogger().debug("currentTopPosition: "+currentTopPosition);

            Integer newTopPosition = currentTopPosition + 1;
            // NioLog.getLogger().debug("newTopPosition: "+newTopPosition);
            ps.setPosition(newTopPosition);
        }
        else
        {
            ps.setPosition(0);
        }
        if (null == currentSentence)
        {
            ps.setPosition(new Integer(0));
        }
        else
        {
            Integer nextPosition = new Integer(currentSentence.getPosition().intValue() + 1);
            ps.setPosition(nextPosition);
        }
        currentSentence = ps;

        allSentences.put(ps.getPosition(), ps);
        return ps;

    }

    protected abstract boolean postProcessInfoBoxes();

    public String printInfoBox()
    {
        TALog.getLogger().debug("printInfoBox");
        if (infoBoxMap.size() > 0)
        {
            TALog.getLogger().debug("printInfoBox has content");

            StringBuffer sb = new StringBuffer();
            Iterator<String> itr = infoBoxMap.keySet().iterator();
            sb.append("\n\n\n\nINFO BOX\n--------------------------\n ");
            String currentEntry = null;
            while (itr.hasNext())
            {

                String key = itr.next();
                // NioLog.getLogger().debug("key: " + key);
                String thisEntry = key.substring(0, key.indexOf(TWOHASHTAG));
                if (!thisEntry.equals(currentEntry))
                {
                    currentEntry = thisEntry;
                    sb.append("\n\n");
                }
                InfoBox ibox = infoBoxMap.get(key);

                // NioLog.getLogger().debug(ibox);
                if (ibox.getJobTitles().size() == 0)
                {
                    TALog.getLogger().debug("No Job Title in IB");

                }
                sb.append(StringConstantsInterface.INFOBOX_JOBTITLE + ": ");
                for (String s : ibox.getJobTitles())
                {
                    if (!s.isEmpty())
                    {
                        sb.append(s);

                    }
                }
                sb.append("\nib-uid");
                sb.append(ibox.getInfoBoxUID());
                sb.append("\n salary start");
                sb.append(ibox.getStartSalaryRange());
                sb.append("\n salary end");
                sb.append(ibox.getEndSalaryRange());

                if (ibox.getQualifications().size() > 0)
                {
                    sb.append(StringConstantsInterface.INFOBOX_QUALIFICATION + ": ");
                }

                for (String s : ibox.getQualifications())
                {
                    if (!s.isEmpty())
                    {
                        sb.append(s);
                        sb.append(", ");
                    }
                }
                sb.append("\n");
                if (ibox.getAreasOfStudy().size() > 0)
                {
                    sb.append(StringConstantsInterface.INFOBOX_AREASOFSTUDY + ": ");
                }
                for (String s : ibox.getAreasOfStudy())
                {
                    if (!s.isEmpty())
                    {
                        sb.append(s);
                        sb.append(", ");
                    }
                }
                sb.append("\n");
                if (ibox.getSkills().size() > 0)
                {
                    sb.append(StringConstantsInterface.INFOBOX_SKILLS + ":  ");
                }
                for (String s : ibox.getSkills())
                {
                    if (!s.isEmpty())
                    {
                        sb.append(s);

                    }
                }
                sb.append("\n");
                if (ibox.getSkills().size() > 0)
                {
                    sb.append(StringConstantsInterface.INFOBOX_YEARS_EXPERIENCE + ": ");
                }

                for (String s : ibox.getYearsExperience())
                {
                    if (!s.isEmpty())
                    {
                        sb.append(s);
                        sb.append(", ");
                    }
                }
                sb.append("\n--------------------------\n");
                sb.append("\n");

            }
            return sb.toString();

        }
        return "";
    }

    protected boolean saveInfoBox(InfoBox ib)
    {
        try
        {
            TALog.getLogger().debug("\n\n\n=========saveInfoBox==========n\n\n");
            TALog.getLogger().debug("saveInfoBox " + ib.getAreasOfStudy());
            TALog.getLogger().debug("saveInfoBox " + ib.getSkills());
            TALog.getLogger().debug("saveInfoBox uid " + ib.getInfoBoxUID());
            lock.lock();
            TALog.getLogger().debug("got lock");
            TALog.getLogger().debug(ib);

            if (isSaveableInfoBox(ib))
            {

                JAXBContext jaxbContext = JAXBContext.newInstance(InfoBox.class);
                String fileName = String.valueOf(System.currentTimeMillis());
                if (fileName.length() > 150)
                {
                    return false;
                }

                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                if (ib.containsAreaOfStudy("ANY SUBJECT") && ib.getCertification().size() > 0)
                {
                    jaxbMarshaller.marshal(ib, new File(FileAnalyzerThread.infoBoxPath + File.separator + "___c_"
                            + fileName + System.currentTimeMillis() + ".xml"));
                }
                else if (ib.containsAreaOfStudy("ANY SUBJECT"))
                {
                    jaxbMarshaller.marshal(ib, new File(FileAnalyzerThread.infoBoxPath + File.separator + "___"
                            + fileName + System.currentTimeMillis() + ".xml"));
                }
                else if (ib.getCertification().size() > 0)
                {
                    jaxbMarshaller.marshal(ib, new File(FileAnalyzerThread.infoBoxPath + File.separator + "_c_"
                            + fileName + System.currentTimeMillis() + ".xml"));
                }
                else
                {
                    jaxbMarshaller.marshal(ib, new File(FileAnalyzerThread.infoBoxPath + File.separator + fileName
                            + System.currentTimeMillis() + ".xml"));
                }
                TALog.getLogger().debug("saved");
                return true;
            }

            TALog.getLogger().debug("NOT saved ");
            return false;

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            return false;
        }
        finally
        {

            lock.unlock();
            TALog.getLogger().debug("released lock");
        }
    }

    /**
     * @param region
     */
    public void setRegion(String region)
    {
        this.region = region;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

}
