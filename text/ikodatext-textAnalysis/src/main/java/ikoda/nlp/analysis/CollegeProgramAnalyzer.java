package ikoda.nlp.analysis;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import ikoda.netio.config.ConfigurationBeanParent;
import ikoda.nlp.structure.IdentifiedToken;
import ikoda.nlp.structure.PossibleSentence;

public class CollegeProgramAnalyzer extends IdentifiedTokenGeneratingAnalyzer
{
    
    


    
    
    private Path file;
    
    
    

    public CollegeProgramAnalyzer(StanfordCoreNLP pipeline, ConfigurationBeanParent config)
    {

        super(pipeline, config);
        
    }
    

    
    
    

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
        TALog.getLogger().warn("\n\n\nMETHOD STUB ONLY\n\n\n");
    }

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
    
    
    

}
