package ikoda.nlp.test.tests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import ikoda.nlp.analysis.TALog;

public class TestTA
{

    /**
     * demoAPI demonstrates other ways of calling the parser with already tokenized
     * text, or in some cases, raw text that needs to be tokenized as a single
     * sentence. Output is handled with a TreePrint object. Note that the options
     * used when creating the TreePrint can determine what results to print out.
     * Once again, one can capture the output by passing a PrintWriter to
     * TreePrint.printTree. This code is for English.
     */
    public void demoAPI(LexicalizedParser lp)
    {
       /***
        // This option shows parsing a list of correctly tokenized words
        try
        {
            System.out.print(".");
            StringBuffer sb = new StringBuffer();
            sb.append("\n\n\n\n                           demoAPI           \n\n\n\n\n");

            for (List<HasWord> sentence : new DocumentPreprocessor("s.txt"))
            {

                sb.append("\n** sentence: ");
                sb.append(sentence);

                List<CoreLabel> rawWords = Sentence.toCoreLabelList(sentence);
                Tree parseTree = lp.apply(rawWords);
                // parseTree.pennPrint();
                sb.append("\n\n");
                sb.append(parseTree.pennString());

                // This option shows loading and using an explicit tokenizer

                String s = new String("My hovercraft is full of eels");

                StringBuffer sbSentence = new StringBuffer();
                for (HasWord word : sentence)
                {
                    sbSentence.append(word.word());
                    sbSentence.append(" ");
                }
                sbSentence.append(".");

                TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
                Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sbSentence.toString()));
                List<CoreLabel> rawWords2 = tok.tokenize();
                parseTree = lp.apply(rawWords2);

                TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack
                                                                      // for
                                                                      // English
                GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
                sb.append(gsf.toString());
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
                List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
                sb.append("\n**  tdl:\n");
                sb.append(tdl);

                // You can also use a TreePrint object to print trees and
                // dependencies
                // TreePrint tp = new
                // TreePrint("penn,typedDependenciesCollapsed");
                // tp.printTree(parseTree);
                sb.append(parseTree.pennString());

            }
            TALog.getLogger().debug(sb.toString());
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            fail(e.getMessage());
        }
        **/

    }

    /**
     * demoDP demonstrates turning a file into itokens and then parse trees. Note
     * that the trees are printed by calling pennPrint on the Tree object. It is
     * also possible to pass a PrintWriter to pennPrint if you want to capture the
     * output. This code will work with any supported language.
     */
    public void demoDP(LexicalizedParser lp, String filename)
    {
        // This option shows loading, sentence-segmenting and tokenizing
        // a file using DocumentPreprocessor.
        try
        {
            StringBuffer sb = new StringBuffer();
            TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
                                                                  // PennTreebankLanguagePack
                                                                  // for
                                                                  // English
            GrammaticalStructureFactory gsf = null;
            if (tlp.supportsGrammaticalStructures())
            {
                gsf = tlp.grammaticalStructureFactory();
            }
            // You could also create a tokenizer here (as below) and pass it
            // to DocumentPreprocessor
            sb.append("\n\n");
            for (List<HasWord> sentence : new DocumentPreprocessor(filename))
            {
                System.out.print(".");
                Tree parse = lp.apply(sentence);
                sb.append("\n** List<HasWord> sentence : ");
                sb.append(sentence);
                // parse.pennPrint();
                sb.append("\n**  parse.pennString()");
                sb.append(parse.pennString());
                sb.append("\n");

                if (gsf != null)
                {
                    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                    sb.append(gs.toString());
                    sb.append("\n");
                    Collection tdl = gs.typedDependenciesCCprocessed();
                    sb.append("\n**  gs.typedDependenciesCCprocessed() : ");
                    sb.append(gs.typedDependenciesCCprocessed());
                    sb.append("\n**  tdl:\n");
                    sb.append(tdl);
                }
            }
            TALog.getLogger().debug(sb.toString());
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    public void testEverythingNLP()
    {

        try
        {

            tryNER();
            String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";

            LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
            TALog.getLogger().debug("\n\n\n\n                                   demoApi\n\n\n\n");
            demoAPI(lp);

            TALog.getLogger().debug("\n\n\n\n                                   demoDP\n\n\n\n");
            String textFile = "s.txt";
            demoDP(lp, textFile);

            TALog.getLogger().debug("\n\n\n\n                                   testTA\n\n\n\n");
            testTA();

        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
    }

    private void testTA() throws Exception
    {

        Path p = Paths.get("s.txt");

        byte[] encoded = Files.readAllBytes(p);
        String s = new String(encoded);

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, ner, depparse,  natlog, dcoref, openie");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // read some text in the text variable
        String text = s;

        StringBuffer sb = new StringBuffer();

        sb.append(text);
        sb.append(
                "\n\n\n\n\n\n\n===================================================================\n\n\n\n\n\n\n\n\n\n\n");

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and
        // has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);

        sb.append(
                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n+++++++++++++++++++++++SENTENCES++++++++++++++++++++++++++++\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        for (CoreMap sentence : sentences)
        {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            sb.append("\n\n\n==============SENTENCE==============\n\n\n");
            sb.append(sentence.toString());
            sb.append("\n");
            for (CoreLabel token : sentence.get(TokensAnnotation.class))
            {
                // this is the text of the token
                sb.append("\n==============TOKEN==============\n");
                String word = token.get(TextAnnotation.class);
                sb.append(word);
                sb.append(" : ");
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                sb.append(pos);
                sb.append(" : ");
                String lemma = token.get(LemmaAnnotation.class);
                sb.append(lemma);
                sb.append(" : ");
                String ne = token.get(NamedEntityTagAnnotation.class);
                sb.append(ne);
                sb.append("\n");

            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);
            sb.append("\n\n\n=====================TREE==================\n\n\n");
            sb.append(tree.toString());

            // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            sb.append("\n\n\n");
            sb.append(dependencies.toString());

            sb.append("\n\n\n=====================OPENIE==================\n\n\n");
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
            // Print the triples
            for (RelationTriple triple : triples)
            {
                sb.append(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss()
                        + "\t" + triple.objectLemmaGloss());
                sb.append("\n");

            }

        }

        // This is the coreference link graph
        // Each chain stores a set of mentions that link to each other,
        // along with a method for getting the most representative mention
        // Both sentence and token offsets start at 1!

        sb.append(
                "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n================COREF CHAIN=================\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
        if (null != graph)
        {
            java.util.Iterator<Integer> itr = graph.keySet().iterator();
            while (itr.hasNext())
            {
                Integer key = itr.next();
                CorefChain cc = graph.get(key);
                sb.append(cc.toString());
                sb.append("\n");
            }
        }
        TALog.getLogger().debug(sb.toString());

    }

    private void tryNER() throws IOException
    {

        String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";

        AbstractSequenceClassifier classifier = CRFClassifier.getClassifierNoExceptions(serializedClassifier);

        /*
         * For either a file to annotate or for the hardcoded text example, this demo
         * file shows two ways to process the output, for teaching purposes. For the
         * file, it shows both how to run NER on a String and how to run it on a whole
         * file. For the hard-coded String, it shows how to run it on a single sentence,
         * and how to do this and produce an inline XML output format.
         */

        TALog.getLogger().debug("\n\n\n\n\n\n\n\n\n\n\nPASS 1 CLASSIFY\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        Path p = Paths.get("s.txt");

        byte[] encoded = Files.readAllBytes(p);
        String s = new String(encoded);
        String fileContents = s;
        List<List<CoreLabel>> out = classifier.classify(fileContents);
        for (List<CoreLabel> sentence : out)
        {
            for (CoreLabel word : sentence)
            {
                TALog.getLogger().debug(word.word() + '/' + word.get(AnswerAnnotation.class) + ' ');
            }
            System.out.println();
        }

        TALog.getLogger().debug("\n\n\n\n\n\n\n\n\n\n\nPASS 2 CLASSIFY FILE\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        out = classifier.classifyFile("s.txt");
        for (List<CoreLabel> sentence : out)
        {
            for (CoreLabel word : sentence)
            {
                TALog.getLogger().debug(word.word() + '/' + word.get(AnswerAnnotation.class) + ' ');
            }
            System.out.println();
        }

        String s1 = "Good afternoon Rajat Raina, how are you today?";
        String s2 = "I go to school at Stanford University, which is located in California.";

        TALog.getLogger()
                .debug("\n\n\n\n\n\n\n\n\n\n\nPASS 2 CLASSIFY TO STRING\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        TALog.getLogger().debug(classifier.classifyToString(s1));
        TALog.getLogger()
                .debug("\n\n\n\n\n\n\n\n\n\n\nPASS 2 CLASSIFY INLINE XML\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        TALog.getLogger().debug(classifier.classifyWithInlineXML(s2));
        TALog.getLogger()
                .debug("\n\n\n\n\n\n\n\n\n\n\nPASS 2 CLASSIFY TO STRING XML\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        TALog.getLogger().debug(classifier.classifyToString(s2, "xml", true));

    }

    /*
     * @Test public void testTA() throws Exception {
     * 
     * Path p = Paths.get("s.txt");
     * 
     * byte[] encoded = Files.readAllBytes(p); String s = new String(encoded);
     * 
     * Properties props = new Properties(); props.setProperty("annotators",
     * "tokenize, ssplit, pos, lemma, parse, ner, depparse,  natlog, dcoref, openie"
     * ); StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
     * 
     * // read some text in the text variable String text = s;
     * 
     * StringBuffer sb = new StringBuffer();
     * 
     * sb.append(text); sb.append(
     * "\n\n\n\n\n\n\n===================================================================\n\n\n\n\n\n\n\n\n\n\n"
     * );
     * 
     * // create an empty Annotation just with the given text Annotation document =
     * new Annotation(text);
     * 
     * // run all Annotators on this text pipeline.annotate(document);
     * 
     * // these are all the sentences in this document // a CoreMap is essentially a
     * Map that uses class objects as keys and // has values with custom types
     * List<CoreMap> sentences = document.get(SentencesAnnotation.class);
     * 
     * sb.append(
     * "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n+++++++++++++++++++++++SENTENCES++++++++++++++++++++++++++++\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
     * ); for (CoreMap sentence : sentences) { // traversing the words in the
     * current sentence // a CoreLabel is a CoreMap with additional token-specific
     * methods sb.append("\n\n\n==============SENTENCE==============\n\n\n");
     * sb.append(sentence.toString()); sb.append("\n"); for (CoreLabel token :
     * sentence.get(TokensAnnotation.class)) { // this is the text of the token
     * sb.append("\n==============TOKEN==============\n"); String word =
     * token.get(TextAnnotation.class); sb.append(word); sb.append(" : "); // this
     * is the POS tag of the token String pos =
     * token.get(PartOfSpeechAnnotation.class); // this is the NER label of the
     * token sb.append(pos); sb.append(" : "); String lemma =
     * token.get(LemmaAnnotation.class); sb.append(lemma); sb.append(" : "); String
     * ne = token.get(NamedEntityTagAnnotation.class); sb.append(ne);
     * sb.append("\n");
     * 
     * }
     * 
     * // this is the parse tree of the current sentence Tree tree =
     * sentence.get(TreeAnnotation.class);
     * sb.append("\n\n\n=====================TREE==================\n\n\n");
     * sb.append(tree.toString());
     * 
     * // this is the Stanford dependency graph of the current sentence
     * SemanticGraph dependencies =
     * sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
     * sb.append("\n\n\n"); sb.append(dependencies.toString());
     * 
     * 
     * sb.append("\n\n\n=====================OPENIE==================\n\n\n");
     * Collection<RelationTriple> triples =
     * sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class); //
     * Print the triples for (RelationTriple triple : triples) {
     * sb.append(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" +
     * triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
     * sb.append("\n");
     * 
     * }
     * 
     * 
     * 
     * }
     * 
     * 
     * 
     * // This is the coreference link graph // Each chain stores a set of mentions
     * that link to each other, // along with a method for getting the most
     * representative mention // Both sentence and token offsets start at 1!
     * 
     * sb.append(
     * "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n================COREF CHAIN=================\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n"
     * );
     * 
     * Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class); if
     * (null != graph) { java.util.Iterator<Integer> itr =
     * graph.keySet().iterator(); while (itr.hasNext()) { Integer key = itr.next();
     * CorefChain cc = graph.get(key); sb.append(cc.toString()); sb.append("\n"); }
     * } NioLog.getLogger().debug(sb.toString());
     * 
     * }
     */

    /**
     * @Test public void testTA() throws Exception {
     * 
     *       Path p = Paths.get("s.txt");
     * 
     *       byte[] encoded = Files.readAllBytes(p); String s = new String(encoded);
     *       Document doc = new Document(s);
     * 
     *       for (Sentence sent : doc.sentences()) { // Will iterate over two
     *       sentences // We're only asking for words -- no need to load any models
     *       yet System.out.println("The second word of the sentence '" + sent + "'
     *       is " + sent.word(0)); // When we ask for the lemma, it will load and
     *       run the part of speech tagger System.out.println( "The third lemma of
     *       the sentence '" + sent + "' is " + sent.lemma(0)); // When we ask for
     *       the parse, it will load and run the parser System.out.println("The
     *       parse of the sentence '" + sent + "' is " + sent.parse()); // ... }
     * 
     * 
     *       Document doc1 = new Document(s); for (Sentence sent1 :
     *       doc1.sentences()) { // Will iterate over two sentences // We're only
     *       asking for words -- no need to load any models yet
     * 
     *       System.out.println("The second word of the sentence '" + sent1 + "' is
     *       " + sent1.word(0)); // When we ask for the lemma, it will load and run
     *       the part of speech tagger System.out.println( "The third lemma of the
     *       sentence '" + sent1 + "' is " + sent1.lemma(0)); // When we ask for the
     *       parse, it will load and run the parser System.out.println("The parse of
     *       the sentence '" + sent1 + "' is " + sent1.parse()); // ... } }
     */

}
