package ikoda.nlp.test.tests;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ie.util.RelationTriple;
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
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import ikoda.nlp.analysis.TALog;

class ParserDemo
{

    /**
     * demoAPI demonstrates other ways of calling the parser with already tokenized
     * text, or in some cases, raw text that needs to be tokenized as a single
     * sentence. Output is handled with a TreePrint object. Note that the options
     * used when creating the TreePrint can determine what results to print out.
     * Once again, one can capture the output by passing a PrintWriter to
     * TreePrint.printTree. This code is for English.
     */
    public static void demoAPI(LexicalizedParser lp)
    {
        /***
        // This option shows parsing a list of correctly tokenized words
        try
        {
            StringBuffer sb = new StringBuffer();
            sb.append("\n\n\n\n                           demoAPI           \n\n\n\n\n");

            for (List<HasWord> sentence : new DocumentPreprocessor("s.txt"))
            {

                sb.append("sentence: ");
                sb.append(sentence);

                List<CoreLabel> rawWords = Sentence.toCoreLabelList(sentence);
                Tree parseTree = lp.apply(rawWords);
                parseTree.pennPrint();
                sb.append("\n\n");
                sb.append(parseTree.pennString());

                // This option shows loading and using an explicit tokenizer

                String s = new String("My hovercraft is full of eels");

                TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
                Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(s));
                List<CoreLabel> rawWords2 = tok.tokenize();
                parseTree = lp.apply(rawWords2);

                TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack
                                                                      // for
                                                                      // English
                GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
                sb.append(gsf.toString());
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
                List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
                TALog.getLogger().debug(tdl);
                TALog.getLogger().debug(" ");

                // You can also use a TreePrint object to print trees and
                // dependencies
                TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
                tp.printTree(parseTree);

            }
            TALog.getLogger().debug(sb.toString());
        }
        catch (Exception e)
        {
            TALog.getLogger().error(e.getMessage(), e);
        }
        **/

    }

    /**
     * demoDP demonstrates turning a file into itokens and then parse trees. Note
     * that the trees are printed by calling pennPrint on the Tree object. It is
     * also possible to pass a PrintWriter to pennPrint if you want to capture the
     * output. This code will work with any supported language.
     */
    public static void demoDP(LexicalizedParser lp, String filename)
    {
        // This option shows loading, sentence-segmenting and tokenizing
        // a file using DocumentPreprocessor.
        StringBuffer sb = new StringBuffer();
        TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a
                                                              // PennTreebankLanguagePack
                                                              // for English
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
            Tree parse = lp.apply(sentence);
            sb.append("List<HasWord> sentence : ");
            sb.append(sentence);
            parse.pennPrint();
            sb.append("\n");
            sb.append(parse.pennString());
            sb.append("\n");
            TALog.getLogger().debug(" ");

            if (gsf != null)
            {
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                sb.append(gs.toString());
                sb.append("\n");
                Collection tdl = gs.typedDependenciesCCprocessed();
                sb.append("gs.typedDependenciesCCprocessed() : ");
                TALog.getLogger().debug(tdl);
                TALog.getLogger().debug(" ");
            }
        }
        TALog.getLogger().debug(sb.toString());
    }

    /**
     * The main method demonstrates the easiest way to load a parser. Simply call
     * loadModel and specify the path of a serialized grammar model, which can be a
     * file, a resource on the classpath, or even a URL. For example, this
     * demonstrates loading a grammar from the models jar file, which you therefore
     * need to include on the classpath for ParserDemo to work.
     *
     * Usage: {@code java ParserDemo [[model] textFile]} e.g.: java ParserDemo
     * edu/stanford/nlp/models/lexparser/chineseFactored.ser.gz
     * data/chinese-onesent-utf8.txt
     *
     */
    public static void main(String[] args)
    {

        try
        {

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

    private static void testTA() throws Exception
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

    private ParserDemo()
    {
    } // static methods only

}
