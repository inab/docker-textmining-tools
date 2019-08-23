package es.bsc.inb.nlp.standardpreprocessing.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * Standard Preprocessing process. Using the Standford Core NLP and the Gate TEI format. 
 * Tokenization
 * Sentence Spliting
 * 
 * @author jcorvi
 *
 */
public class App {
	
	public static void main( String[] args ){
    	
    	Options options = new Options();
    	
        Option input = new Option("i", "input", true, "input directory path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "output directory path");
        output.setRequired(true);
        options.addOption(output);
        
        Option set = new Option("a", "annotation_set", true, "Annotation set where the annotation will be included");
        set.setRequired(true);
        options.addOption(set);
        
        Option workdir = new Option("workdir", "workdir", true, "workDir directory path");
        workdir.setRequired(false);
        options.addOption(workdir);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
    	try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
    	
        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String workdirPath = cmd.getOptionValue("workdir");
        String annotationSet = cmd.getOptionValue("annotation_set");
        
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		System.out.println("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
        if (annotationSet==null) {
        	System.out.println("Please set the annotation set where the annotation will be included");
			System.exit(1);
    	}
        
    	File outputDirectory = new File(outputFilePath);
	    if(!outputDirectory.exists())
	    	outputDirectory.mkdirs();
	    
	    try {
			Gate.init();
		} catch (GateException e) {
			System.out.println("App::main :: Gate Exception  ");
			e.printStackTrace();
			System.exit(1);
		}
 
	    if(workdirPath==null) {
	    	workdirPath="";
	    }
	    
		try {
			process(inputFilePath, outputFilePath,workdirPath, annotationSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    /**
	 * Preprocessing NLP Standardization 
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void process(String inputDirectoryPath, String outputDirectoryPath, String workdir, String annotationSet) throws IOException {
    	Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		//props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, depparse");
		props.put("ssplit.newlineIsSentenceBreak", "always");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		System.out.println("App::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml") || file.getName().endsWith(".txt")){
					try {
						System.out.println("App::process :: processing file : " + file.getAbsolutePath());
						String fileOutPutName = file.getName();
						if(fileOutPutName.endsWith(".txt")) {
							fileOutPutName = fileOutPutName.replace(".txt", ".xml");
						}
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + fileOutPutName);
						processDocument(pipeline, file, outputGATEFile, annotationSet);
					} catch (ResourceInstantiationException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} catch (MalformedURLException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} catch (InvalidOffsetException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} 
				}
			}
		}else {
			System.out.println("No directory :  " + inputDirectoryPath);
		}
		System.out.println("App::process :: END ");
	}

	/**
	 * Execute process in a document
	 * @param pipeline
	 * @param inputFile
	 * @param outputGATEFile
	 * @throws ResourceInstantiationException
	 * @throws MalformedURLException
	 * @throws InvalidOffsetException
	 */
	private static void processDocument(StanfordCoreNLP pipeline,File inputFile, File outputGATEFile, String annotationSet) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
		long startTime = System.currentTimeMillis();
		gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		String plainText = gateDocument.getContent().getContent(0l, gate.Utils.lengthLong(gateDocument)).toString();
		//String plainText = "Reversible effects on the liver with hepatocellular hypertrophy were found from the lowest dose upwards together with changes in coagulation parameters and fibrinogen at the high dose.";
		/*String plainText = "Hepatocellular hypertrophy were found from the lowest dose upwards together with changes in coagulation parameters and fibrinogen at the high dose, suspected to be a treatment related finding;"
				+ " by the other hand the decrease in food consumption were not related to the treatment.";*/
		Annotation document = new Annotation(plainText);
		pipeline.annotate(document);
		long endTime = System.currentTimeMillis();
		System.out.println(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
		try {	
	      	List<CoreMap> sentences= document.get(SentencesAnnotation.class);
		    for(CoreMap sentence: sentences) {
		    	List<CoreLabel> tokens= sentence.get(TokensAnnotation.class);
		    	annotateTokensAndSentences(gateDocument, sentence, tokens, annotationSet);
		    	//sentence.get(DependencyParseAnnotator.class);
				// this is the parse tree of the current sentence this is with ----> parse. this is not included in depparse
//				Tree tree = sentence.get(TreeAnnotation.class);
//				System.out.println(tree+"\n");
//				for (Tree subTree : tree.children()) {
//			        System.err.println(subTree.label());
//			    }
//				
//				// this is the Stanford dependency graph of the current sentence with depparse.  parse include depparse
//				SemanticGraph graph = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//				
//				
//				/*for (SemanticGraphEdge edge : graph.edgeIterable()) {
//					  int headIndex = edge.getGovernor().get(TokensAnnotation.class);
//					  int depIndex = edge.getDependent().index();
//					  
//					  System.out.printf("%d %d %d%n", headIndex, depIndex, edge.getWeight());
//					}*/
//				
//				List<SemanticGraphEdge> edges = graph.getOutEdgesSorted(graph.getFirstRoot());
//			    for (SemanticGraphEdge e : edges) {
//			    	e.getSource().backingLabel();
//			        System.out.println(e.getGovernor()  + " relacion: "+ e.getRelation() + " dependent: " + e.getDependent());
//			    }
//				//EnglishGrammaticalRelations
//				
//				System.out.println(sentence);
//				System.out.println(graph+"\n");
				//Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
			    // Print the triples
			   /* for (RelationTriple triple : triples) {
			    	System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss() + "\n");
			    }*/
			}
		    java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
		    out.write(gateDocument.toXml());
		    out.close();
		} catch (IOException e) {
			System.out.println("App :: executeDocument :: IOException ");
			e.printStackTrace();
		}
	}

	/**
	* Tokens and sentences annotation
	* @param gateDocument
	* @param sentence
	* @param tokens
	* @throws InvalidOffsetException
	*/
	private static void annotateTokensAndSentences(gate.Document gateDocument, CoreMap sentence, List<CoreLabel> tokens, String annotationSet) throws InvalidOffsetException {
		FeatureMap features = Factory.newFeatureMap();
		Integer sentenceBegin = sentence.get(CharacterOffsetBeginAnnotation.class);
	    Integer sentenceEnd = sentence.get(CharacterOffsetEndAnnotation.class);
	    gateDocument.getAnnotations(annotationSet).add(new Long(sentenceBegin), new Long(sentenceEnd), "Sentence", features);
	    for (CoreLabel token : tokens) {
	    	FeatureMap features_tokens = Factory.newFeatureMap();
	    	String word = token.get(TextAnnotation.class);
	    	features_tokens.put("word", word);
	    	features_tokens.put("length", word.length());
	    	features_tokens.put("pos", token.get(PartOfSpeechAnnotation.class));
	    	String kind = token.get(NamedEntityTagAnnotation.class);
	    	if(kind!=null) {
	    		features_tokens.put("kind", kind);
	    	}
	    	Object[] tokenFeatures = tokenFeatures(word);
	    	features_tokens.put("case", tokenFeatures[0]);
	    	features_tokens.put("mask", tokenFeatures[1]);
	    	gateDocument.getAnnotations(annotationSet).add(new Long(token.beginPosition()), new Long(token.endPosition()), "Token", features_tokens);
	    }
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	private static Object[] tokenFeatures(String word){
        //convert String to char array
        char[] charArray = word.toCharArray();
        Boolean isUpperCase = true;
        Boolean initialUpper = false;
        if(Character.isUpperCase( charArray[0])) {
        	initialUpper  =true;
        }
        
        String mask = "";
        for(int i=0; i < charArray.length; i++){
            //if any character is not in upper case, return false
            if(!Character.isUpperCase( charArray[i])) {
            	isUpperCase = false;
            	mask = mask + "x";
            }else {
            	mask = mask + "X";
            }
        }
        String case_ = "lower_case";
        if(isUpperCase) {
        	case_ = "upper_case";
        }else if(initialUpper) {
        	case_ = "initial_upper_case";
        }
        return new Object[] {case_, mask};
    }
	
}
