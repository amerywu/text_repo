package ikoda.nlp.structure;

public class PossibleToken
{

    private final static String[] FLAGS = { "DEGREE", "QUALIFICATION", "POSITION", "TITLE", "KNOWLEDGE OF",
            "EXPERIENCED IN", "ABILITY", "EXPERIENCE", "YEARS", "SALARY", "COMPENSATION", "PAY" };

    public static synchronized boolean isPossibleTokenFromLemma(String token)
    {
        if (null == token)
        {
            return false;
        }
        for (int i = 0; i < FLAGS.length; i++)
        {
            if (FLAGS[i].toUpperCase().equals(token.toUpperCase()))
            {
                return true;
            }
        }
        return false;
    }

    private String lemma;
    private String sentence;

    public PossibleToken(String lemma, String sentence)
    {
        // NioLog.getLogger().debug("\nNew PossibleToken: "+ lemma +
        // "\n"+sentence);
        this.lemma = lemma;
        this.sentence = sentence;

    }

    public String getLemma()
    {
        return lemma;
    }

    public String getSentence()
    {
        return sentence;
    }

}