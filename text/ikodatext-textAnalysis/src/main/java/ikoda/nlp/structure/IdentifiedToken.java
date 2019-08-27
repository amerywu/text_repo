package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

import ikoda.nlp.analysis.TALog;

public class IdentifiedToken
{
    
    protected class IdentifiedTokenTuple
    {
        
        private List<IdentifiedToken> itokens = new ArrayList<>();
        
        private IdentifiedTokenTuple(List<IdentifiedToken> initokens)
        {
            TALog.getLogger().trace("init");
            itokens=initokens;
        }
        
        protected List<IdentifiedToken> getItokens()
        {
            return itokens;
        }
        public String toString()
        {
            return itokens.toString();
        }
        
        
        
    }
    

    public static final String TOKENTUPLE = "TOKENTUPLE";
    public static final String DEGREE = "EDUDEGREE";
    public static final String JOBLABEL = "JOBLABEL";
    public static final String DEGREEPROGRAM = "DEGREEPROGRAM";
    public static final String JOBLOCATION = "JOBLOCATION";
    public static final String TITLE = "TITLE";
    public static final String JOBCORE = "JOBCORE";
    public static final String JOBTITLE = "JOBTITLE";
    public static final String SKILL = "SKILL";
    public static final String SKILLSET = "SKILLSET";
    public static final String SKILLSETLABEL = "SKILLSETLABEL";
    public static final String WORKSKILL = "WORKSKILL";
    public static final String DURATION = "DURATION";
    public static final String EXPERIENCE = "EXPERIENCE";
    public static final String MONEY = "MONEY";
    public static final String ENTRYLEVEL = "ENTRYLEVEL";
    public static final String VERB = "VERB";
    public static final String NOUN = "NOUN";
    public static final String ADJECTIVE = "ADJECTIVE";
    public static final String LOCATIONLABEL = "LOCATIONLABEL";
    public static final String LOCATION = "GPE";
    public static final String JOBDESCRIPTIONSENTENCE = "JOBDESCRIPTIONSENTENCE";
    public static final String LDEGREEEDU = "LDEGREEEDU";
    public static final String CERTIFICATION = "CERTIFICATION";
    public static final String LEMMATIZEDSENTENCE = "LEMMATIZEDSENTENCE";
    public static final String RAWSENTENCE = "RAWSENTENCE";
    public static final String TERM = "TERM";

    private static final String LRB = "-LRB-";
    private static final String RRB = "-RRB-";
    private static final String RSB = "-RSB-";
    private static final String LSB = "-LSB-";
    private static final String LCB = "-LCB-";
    private static final String RCB = "-RCB-";
    private static final String DASH = " - ";
    private static final String AMP = "&amp;";
    private static final String AMP1 = "&";
    private static final String AND = "and";

    protected static final String[] RELEVANT_TOKENS = { RAWSENTENCE,SKILLSETLABEL, DEGREE, SKILL, JOBLABEL, JOBCORE, JOBTITLE,
            SKILLSET, DURATION, EXPERIENCE, TITLE, LOCATION, LOCATIONLABEL, DEGREEPROGRAM, MONEY, WORKSKILL, ENTRYLEVEL,
            VERB, NOUN, JOBDESCRIPTIONSENTENCE, LDEGREEEDU, CERTIFICATION, JOBLOCATION, ADJECTIVE, LEMMATIZEDSENTENCE,TERM };

    public static synchronized boolean containsTokenName(String s)
    {
        for (int i = 0; i < RELEVANT_TOKENS.length; i++)
        {
            if (s.toUpperCase().toUpperCase().contains(RELEVANT_TOKENS[i]))
            {
                return true;
            }
        }
        return false;
    }

    public static synchronized String getTokenNameContainedIn(String s)
    {
        for (int i = 0; i < RELEVANT_TOKENS.length; i++)
        {
            if (s.toUpperCase().toUpperCase().contains(RELEVANT_TOKENS[i]))
            {
                return RELEVANT_TOKENS[i];
            }
        }
        return "";
    }

    public static synchronized boolean isRelevantToken(String nerToken)
    {
        for (int i = 0; i < RELEVANT_TOKENS.length; i++)
        {
            if (RELEVANT_TOKENS[i].equalsIgnoreCase(nerToken))
            {
                return true;
            }
        }
        return false;
    }

    public static synchronized boolean tokenNameContains(String s)
    {
        for (int i = 0; i < RELEVANT_TOKENS.length; i++)
        {
            if (RELEVANT_TOKENS[i].toUpperCase().contains(s.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }

    private int infoBoxId = -1;
    private String type;

    private IdentifiedTokenTuple itokenTuple;
    private String value;

    private int frequencyCount = 1;
    


    public IdentifiedToken(List<IdentifiedToken> initokens)
    {


        this.type = TOKENTUPLE;
        String value1=initokens.stream().map(it -> it.getType()+it.hashCode()).collect(Collectors.toList()).toString().replaceAll(", ", "-");
        
        this.value = value1;
        itokenTuple= new IdentifiedTokenTuple(initokens);
        TALog.getLogger().trace("itokenTuple "+itokenTuple);

    }
    
    public IdentifiedToken(String intype, String invalue)
    {

        if (null == invalue || invalue.isEmpty())
        {
            invalue = "NA";
        }
        if (null == intype || intype.isEmpty())
        {
            intype = "NA";
        }
        this.type = intype;

        this.value = cleanString(invalue);

    }
    
    public IdentifiedToken(IdentifiedToken itoken)
    {


        this.type = itoken.type;

        this.value = itoken.value;
        
        this.frequencyCount=itoken.frequencyCount;
        
        this.infoBoxId=itoken.infoBoxId;
        
        this.itokenTuple=itoken.itokenTuple;
        

    }

    private String cleanString(String s)
    {
        String pass1 = Character.toUpperCase(s.charAt(0)) + s.substring(1);
         pass1 = pass1.replace(LRB, DASH);
         pass1 = pass1.replace(RRB, DASH);
         pass1 = pass1.replace(RSB, DASH);
         pass1 = pass1.replace(LSB, DASH);
         pass1 = pass1.replace(LCB, DASH);
         pass1 = pass1.replace(RCB, DASH);
         pass1 = pass1.replace(" '", "'");
         pass1 = pass1.replace(" ,", ",");
         pass1 = pass1.replace(" .", ".");
         pass1 = pass1.replace(AMP, AND);
         pass1 = pass1.replace(AMP1, AND);
        return pass1;
    }
    
    

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        IdentifiedToken other = (IdentifiedToken) obj;
        if (type == null)
        {
            if (other.type != null)
            {
                return false;
            }
        }
        else if (!type.equals(other.type))
        {
            return false;
        }
        if (value == null)
        {
            if (other.value != null)
            {
                return false;
            }
        }
        else if (!value.toUpperCase().equals(other.value.toUpperCase()))
        {
            return false;
        }
        return true;
    }

    public List<IdentifiedToken> getChildren()
    {
        if(null==itokenTuple)
        {
            TALog.getLogger().warn("tokenTuple is null. Nothing to return");
            return new ArrayList<IdentifiedToken>();
        }
        return itokenTuple.getItokens();
    }

    public int getFrequencyCount()
    {
        return frequencyCount;
    }

    public int getInfoBoxId()
    {
        return infoBoxId;
    }

    public String getType()
    {
        return type;
    }
    
    public String getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    public void incrementFrequency()
    {
        
        frequencyCount++;
    }

    public void incrementFrequency(int frequencyCountIn)
    {
        frequencyCount += frequencyCountIn;
    }

    /**
     * @param infoBoxId
     */
    public void setInfoBoxId(int infoBoxId)
    {
        this.infoBoxId = infoBoxId;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "IdentifiedToken [infoBoxId=" + infoBoxId + ", type=" + type + ", value=" + value + ", frequencyCount="
                + frequencyCount + " IdentifiedTokenTuple="+itokenTuple+"]\n";
    }

}
