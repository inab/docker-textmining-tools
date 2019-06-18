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
 * Standard Preprocessing process.  Using the Standford Core NLP and the Gate TEI format. 
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
		props.put("annotators", "tokenize, ssplit, pos, lemma");
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
		//String plainText = "Header \n Mi sentencia aca mi sentencia.  Mi otra sentencia. ";
		Annotation document = new Annotation(plainText);
		pipeline.annotate(document);
		long endTime = System.currentTimeMillis();
		System.out.println(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
		try {	
	      	List<CoreMap> sentences= document.get(SentencesAnnotation.class);
		    for(CoreMap sentence: sentences) {
		    	List<CoreLabel> tokens= sentence.get(TokensAnnotation.class);
		    	annotateTokensAndSentences(gateDocument, sentence, tokens, annotationSet);
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
	    	features_tokens.put("word", token.get(TextAnnotation.class));
	    	features_tokens.put("pos", token.get(PartOfSpeechAnnotation.class));
	    	gateDocument.getAnnotations(annotationSet).add(new Long(token.beginPosition()), new Long(token.endPosition()), "Token", features_tokens);
	    }
	}
}
