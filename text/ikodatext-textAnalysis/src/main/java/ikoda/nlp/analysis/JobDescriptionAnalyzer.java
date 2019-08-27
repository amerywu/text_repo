package ikoda.nlp.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class JobDescriptionAnalyzer extends IdentifiedTokenGeneratingAnalyzer
{

    private Path file;

    public JobDescriptionAnalyzer(StanfordCoreNLP pipeline, ConfigurationBeanParent config)
    {

        super(pipeline, config);
        TALog.getLogger().debug("JobDescriptionAnalyzer");
    }

    protected boolean isIgnorableSentence(String s)
    {

        return false;
    }

    @Override
    protected String lemmatizeSentence(PossibleSentence psentence)
    {
        StringBuilder sb = new StringBuilder();
        CoreMap sentence = psentence.getSentence();
        
        
        for (CoreLabel token : sentence.get(TokensAnnotation.class))
        {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (isIgnorablePhrase(token.word()))
            {
                continue;
            }
            if (isAcceptedLemma(pos))
            {
                sb.append(token.lemma());
                sb.append(SPACE);
            }
        }
        sb.append(STOP + END);
        return sb.toString();
    }

    protected void processTriples(PossibleSentence psentence)
    {
        try
        {
            CoreMap sentence = psentence.getSentence();
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            TALog.getLogger().debug(sentence.toString());
            // Print the triples

            if (null == triples)
            {
                return;
            }
            for (RelationTriple triple : triples)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("\nConfidence: " + triple.confidence + "\n" + triple.subjectLemmaGloss() + "\n"
                        + triple.relationLemmaGloss() + "\n" + triple.objectLemmaGloss());

                List<CoreLabel> relations = triple.relation;
                for (CoreLabel t : relations)
                {
                    String pos = t.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    sb.append("\n\nr: ");
                    sb.append(t.lemma());
                    sb.append(" ");
                    sb.append(pos);
                    sb.append("  |  ");
                }

                sb.append("\n");

                List<CoreLabel> objects = triple.object;
                for (CoreLabel t : objects)
                {
                    String pos = t.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    sb.append("o: ");
                    sb.append(t.lemma());
                    sb.append(" ");
                    sb.append(pos);
                    sb.append("  |  ");
                }

                sb.append("\n---\n");
                TALog.getLogger().debug(sb.toString());
            }

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }

    }
    
    @Override
    protected void tokenizeLemmatizedSentence(String s, PossibleSentence psentence)
    {
        if(!isIgnorablePhrase(s))
        {
            IdentifiedToken itoken = handleiToken(IdentifiedToken.LEMMATIZEDSENTENCE, s);
            IdentifiedToken itokenr = handleiToken(IdentifiedToken.RAWSENTENCE, psentence.toString());
            List<IdentifiedToken> l = new ArrayList<>();
            l.add(itoken);
            l.add(itokenr);
            IdentifiedToken itokenTuple=new IdentifiedToken(l);
            psentence.iTokensAdd(itokenTuple);

        }
    }



    protected String validateString(String inString)
    {

        String cleanString = cleanString(inString);
        TALog.getLogger().debug(cleanString);

        String[] sentenceArray = cleanString.split("[\\.\\?!]");
        List<String> sarray = new ArrayList<String>();

        for (int i = 0; i < sentenceArray.length; i++)
        {
            sarray.add(sentenceArray[i]);
        }

        Iterator<String> itr = sarray.iterator();
        while (itr.hasNext())
        {
            String s = itr.next();
            if (s.length() > 400)
            {
                TALog.getLogger().debug("removing " + s);
                itr.remove();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String s : sarray)
        {
            sb.append(s);
            sb.append(".");
            sb.append(" ");
        }

        return sb.toString();
    }

}
