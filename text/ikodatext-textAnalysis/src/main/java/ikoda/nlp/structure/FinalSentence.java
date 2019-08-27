package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FinalSentence
{

    public static int LOW_POSSIBILITY = 1;
    public static int MED_LOW_POSSIBILITY = 2;
    public static int MED_POSSIBILITY = 4;
    public static int MED_HIGH_POSSIBILITY = 8;
    public static int HIGH_POSSIBILITY = 12;
    public static int BINGO = 13;

    private String type;

    private String sentence;

    private Integer possibility = 0;

    private Integer position = 0;

    private List<IdentifiedToken> itokens = new ArrayList<IdentifiedToken>();

    private List<PossibleToken> ptokens = new ArrayList<PossibleToken>();

    public FinalSentence(PossibleSentence psentence)
    {
        this.type = psentence.getType();
        this.possibility = psentence.getPossibility();
        this.position = psentence.getPosition();
        this.itokens = psentence.iTokensGetAll();
        this.ptokens = psentence.pTokensGetAll();
        this.sentence = psentence.toString();

    }

    public boolean ContainsIdenitifiedToken(String token)
    {
        Predicate<IdentifiedToken> p = it -> it.getType().equals(token);

        return itokens.stream().anyMatch(p);
    }

    public Integer getPosition()
    {
        return position;
    }

    public int getPossibility()
    {

        return possibility;
    }

    public String getSentence()
    {
        return sentence;
    }

    public String getType()
    {
        return type;
    }

    public boolean iTokensAdd(IdentifiedToken e)
    {

        return itokens.add(e);
    }

    public boolean iTokensAddAll(List<IdentifiedToken> l)
    {

        return itokens.addAll(l);
    }

    public IdentifiedToken iTokensGet(int index)
    {
        return itokens.get(index);
    }

    public List<IdentifiedToken> iTokensGet(String type)
    {
        List<IdentifiedToken> returnList = itokens.stream().filter(itoken -> itoken.getType().equals(type))
                .collect(Collectors.toList());
        // NioLog.getLogger().debug("iTokensGet returnList size: "+returnList.size());

        return returnList;
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

}
