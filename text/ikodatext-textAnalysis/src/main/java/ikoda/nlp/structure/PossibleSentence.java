package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.stanford.nlp.util.CoreMap;
import ikoda.nlp.analysis.TALog;

public class PossibleSentence
{

    public static final String RELATION_BE_IN = "BE IN";
    public static final String QUALIFICATION = "qualificaton";
    public static final String CERTIFICATION = "certification";
    public static final String JOBTITLE = "jobtitle";
    public static int LOW_POSSIBILITY = 1;
    public static int MED_LOW_POSSIBILITY = 2;
    public static int MED_POSSIBILITY = 4;
    public static int MED_HIGH_POSSIBILITY = 8;
    public static int HIGH_POSSIBILITY = 12;
    public static int BINGO = 13;

    private final static String[] RELEVANT_SENTENCE = { QUALIFICATION };

    public static synchronized boolean isRelevantToken(String nerToken)
    {
        for (int i = 0; i < RELEVANT_SENTENCE.length; i++)
        {
            if (RELEVANT_SENTENCE[i].toUpperCase().equals(nerToken.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }

    private String type;

    private Integer possibility = 0;

    private Integer tempPossibility = 0;

    private Integer position = 0;

    private List<IdentifiedToken> itokens = new ArrayList();

    private List<PossibleToken> ptokens = new ArrayList();

    private CoreMap sentence;

    private String sentenceString;;
    private int infoBoxId;

    PossibleSentence(CoreMap sentence)
    {

        this.sentence = sentence;
        sentenceString = sentence.toString();
    }

    public void clearStanfordReferences()
    {
        try
        {

            sentence = null;

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    public boolean containsIdenitifiedTokenOfType(String token)
    {
        Predicate<IdentifiedToken> p = it -> it.getType().equals(token);

        return itokens.stream().anyMatch(p);
    }

    public boolean containsPhrase(String s)
    {
        if (this.sentenceString.toUpperCase().contains(s.toUpperCase()))
        {
            return true;
        }
        return false;
    }

    public int getInfoBoxId()
    {
        return infoBoxId;
    }

    public int getITokenCountForType(String type)
    {
        int count = 0;
        for (IdentifiedToken itoken : iTokensGetAll())
        {
            TALog.getLogger().debug("itoken from block " + itoken.getInfoBoxId());

            if (itoken.getType().equals(type))
            {
                TALog.getLogger().debug("counting: " + itoken.getValue());
                count++;
            }
        }
        return count;
    }

    public Integer getPosition()
    {
        return position;
    }

    public int getPossibility()
    {

        if (tempPossibility > possibility)
        {
            possibility = tempPossibility;
        }
        return possibility;
    }

    public CoreMap getSentence()
    {
        return sentence;
    }

    public String getType()
    {
        return type;
    }

    public void incrementPossibility(int i)
    {
        tempPossibility += i;
    }

    public boolean iTokensAdd(IdentifiedToken e)
    {
        
        for (IdentifiedToken itoken : itokens)
        {
            if (itoken.equals(e) && itoken.getInfoBoxId() == this.infoBoxId)
            {
                itoken.incrementFrequency();
                return true;
            }
        }
        e.setInfoBoxId(this.infoBoxId);

        return itokens.add(e);
    }

    public boolean iTokensAddAll(List<IdentifiedToken> l)
    {

        for (IdentifiedToken newToken : l)
        {
            boolean found = false;
            for (IdentifiedToken extantItoken : itokens)
            {
                if (extantItoken.equals(newToken) && extantItoken.getInfoBoxId() == this.infoBoxId)
                {
                    extantItoken.incrementFrequency();
                    found = true;

                }
            }
            if (!found)
            {
                newToken.setInfoBoxId(infoBoxId);
                itokens.add(newToken);
            }

        }
        return true;
    }

    public IdentifiedToken iTokensGet(int index)
    {
        return itokens.get(index);
    }

    public List<IdentifiedToken> iTokensGet(String type)
    {
        List<IdentifiedToken> returnList = itokens.stream().filter(itoken -> itoken.getType().equals(type))
                .collect(Collectors.toList());

        return returnList;
    }

    public List<IdentifiedToken> iTokensGetAll()
    {
        return Collections.unmodifiableList(itokens);
    }

    public Iterator<IdentifiedToken> iTokensIterator()
    {
        return itokens.iterator();
    }

    public IdentifiedToken iTokensRemove(int index)
    {
        return itokens.remove(index);
    }

    public boolean iTokensRemove(Object o)
    {
        return itokens.remove(o);
    }

    /******************* DELEGATES ************************/

    public int iTokensSize()
    {
        return itokens.size();
    }

    public boolean pTokensAdd(PossibleToken e)
    {

        return ptokens.add(e);

    }

    public PossibleToken pTokensGet(int index)
    {
        return ptokens.get(index);
    }

    public List<PossibleToken> pTokensGetAll()
    {
        return Collections.unmodifiableList(ptokens);
    }

    public Iterator<PossibleToken> pTokensIterator()
    {
        return ptokens.iterator();
    }

    public PossibleToken pTokensRemove(int index)
    {
        return ptokens.remove(index);
    }

    public boolean pTokensRemove(Object o)
    {
        return ptokens.remove(o);
    }

    ////////////
    public int pTokensSize()
    {
        return ptokens.size();
    }

    public void resetPossibility()
    {
        if (tempPossibility > possibility)
        {
            possibility = tempPossibility;
        }
        tempPossibility = 0;
    }

    public void setInfoBoxId(int infoBoxId)
    {
        this.infoBoxId = infoBoxId;
    }

    public void setItokens(List<IdentifiedToken> itokens)
    {
        this.itokens = itokens;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public void setPtokens(List<PossibleToken> ptokens)
    {
        this.ptokens = ptokens;
    }

    public void setSentence(CoreMap sentence)
    {
        this.sentence = sentence;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String toString()
    {
        return sentenceString;
    }

}
