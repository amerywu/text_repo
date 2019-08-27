package ikoda.nlp.test.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import ikoda.nlp.analysis.TALog;

/**
 * Some simple unit tests for the CoreNLP NER
 * (http://nlp.stanford.edu/software/CRF-NER.shtml) short article.
 * 
 * @author hsheil
 *
 */
public class ArticleNlpRunner
{

    private void handleEntity(String inKey, StringBuilder inSb, List<EmbeddedToken> inTokens)
    {
        TALog.getLogger().debug(inSb + " is a " + inKey);
        inTokens.add(new EmbeddedToken(inKey, inSb.toString()));
        inSb.setLength(0);
    }

    @Test
    public void testBasic()
    {
        TALog.getLogger().debug("Starting Stanford NLP");

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization,
        // NER, parsing, and
        Properties props = new Properties();
        boolean useRegexner = true;
        if (useRegexner)
        {
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
            props.put("regexner.mapping", "locations.txt");
        }
        else
        {
            props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        }
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // // We're interested in NER for these things (jt->loc->sal)
        String[] tests = {
                "Partial invoice (€100,000, so roughly 40%) for the consignment C27655 we shipped on 15th August to London from the My Big Foot depot. INV2345 is for the balance.. Customer contact (Sigourney) says they will pay this on the usual credit terms (30 days)." };
        List<EmbeddedToken> tokens = new ArrayList<>();

        for (String s : tests)
        {

            // run all Annotators on the passed-in text
            Annotation document = new Annotation(s);
            pipeline.annotate(document);

            // these are all the sentences in this document
            // a CoreMap is essentially a Map that uses class objects as keys
            // and has values with
            // custom types
            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
            StringBuilder sbuilder = new StringBuilder();

            // I don't know why I can't get this code out of the box from
            // StanfordNLP, multi-token entities
            // are far more interesting and useful..
            // TODO make this code simpler..
            for (CoreMap sentence : sentences)
            {
                // traversing the words in the current sentence, "O" is a
                // sensible default to initialise
                // itokens to since we're not interested in unclassified /
                // unknown things..
                TALog.getLogger().debug("Sentence is " + sentence.toString());
                String prevNeToken = "O";
                String currNeToken = "O";
                boolean newToken = true;
                for (CoreLabel token : sentence.get(TokensAnnotation.class))
                {
                    currNeToken = token.get(NamedEntityTagAnnotation.class);
                    TALog.getLogger().debug("prevNeToken: " + prevNeToken);
                    TALog.getLogger().debug("currNeToken: " + currNeToken);
                    String word = token.get(TextAnnotation.class);
                    TALog.getLogger().debug("word: " + word);
                    // Strip out "O"s completely, makes code below easier to
                    // understand
                    if (currNeToken.equals("O"))
                    {
                        // LOG.debug("Skipping '{}' classified as {}", word,
                        // currNeToken);
                        if (!prevNeToken.equals("O") && (sbuilder.length() > 0))
                        {
                            handleEntity(prevNeToken, sbuilder, tokens);
                            newToken = true;
                        }
                        continue;
                    }

                    if (newToken)
                    {
                        prevNeToken = currNeToken;
                        newToken = false;
                        sbuilder.append(word);
                        continue;
                    }

                    if (currNeToken.equals(prevNeToken))
                    {
                        sbuilder.append(" " + word);
                    }
                    else
                    {
                        // We're done with the current entity - print it out and
                        // reset
                        // TODO save this token into an appropriate ADT to
                        // return for useful processing..
                        handleEntity(prevNeToken, sbuilder, tokens);
                        newToken = true;
                    }
                    prevNeToken = currNeToken;
                }
            }

            // TODO - do some cool stuff with these itokens!
            TALog.getLogger().debug("Extracted  itokens of interest from the input text:" + tokens.size());
            for (EmbeddedToken token : tokens)
            {
                TALog.getLogger().debug(token.getName() + " : " + token.getValue());
            }
        }
    }

}

class EmbeddedToken
{

    private String name;
    private String value;

    public EmbeddedToken(String name, String value)
    {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }
}
