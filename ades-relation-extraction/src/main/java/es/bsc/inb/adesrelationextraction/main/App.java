package es.bsc.inb.adesrelationextraction.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.creole.Plugin;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * Adverse Drug Events Extraction Relation.
 * 
 * @author jcorvi
 *
 */
public class App {
	
	static final Logger log = Logger.getLogger("log");
	
	public static void main( String[] args ){
    	
    	Options options = new Options();
    	
        Option input = new Option("i", "input", true, "input directory path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "output directory path");
        output.setRequired(true);
        options.addOption(output);
        
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
        
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		log.error("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
    	File outputDirectory = new File(outputFilePath);
	    if(!outputDirectory.exists())
	    	outputDirectory.mkdirs();
	    
	    try {
			Gate.init();
		} catch (GateException e) {
			log.error("Wrapper::generatePlainText :: Gate Exception  ", e);
			System.exit(1);
		}
 
	    if(workdirPath==null) {
	    	workdirPath="";
	    }
	    
        try {
			processJapeRules(inputFilePath, outputFilePath);
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	/**
	 * 
	 * @param inputDirectory
	 * @param outputDirectory
	 * @throws GateException
	 * @throws IOException
	 */
    private static void processJapeRules(String inputDirectory, String outputDirectory) throws GateException, IOException {
    	Corpus corpus = Factory.newCorpus("My XML Files"); 
    	File directory = new File(inputDirectory); 
    	ExtensionFileFilter filter = new ExtensionFileFilter("XML files", "xml"); 
    	URL url = directory.toURL(); 
    	corpus.populate(url, filter, null, false);
    	
    	Plugin anniePlugin = new Plugin.Maven("uk.ac.gate.plugins", "annie", "8.5"); 
    	  Gate.getCreoleRegister().registerPlugin(anniePlugin); 
    	  // create a serial analyser controller to run ANNIE with 
    	  SerialAnalyserController annieController =  (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",  
    	      Factory.newFeatureMap(), 
    	       Factory.newFeatureMap(), "ANNIE"); 
    	  String[] PR_NAMES = {
    			       "gate.creole.annotdelete.AnnotationDeletePR",
    			      "gate.creole.tokeniser.DefaultTokeniser",
    			      "gate.creole.gazetteer.DefaultGazetteer",
    			     "gate.creole.splitter.SentenceSplitter",
    			    "gate.creole.POSTagger",
    			     "gate.creole.ANNIETransducer",
    			    "gate.creole.orthomatcher.OrthoMatcher"
    			  };
    	  
    	  for(int i = 0; i < PR_NAMES.length; i++) { 
    	    // use default parameters 
    	    FeatureMap params = Factory.newFeatureMap(); 
    	    ProcessingResource pr = (ProcessingResource) 
    	        Factory.createResource(PR_NAMES[i], 
    	                               params); 
    	    // add the PR to the pipeline controller 
    	    annieController.add(pr); 
    	  } // for each ANNIE PR 
    	  
    	  /*Gate.getCreoleRegister().registerPlugin(new Plugin.Maven( 
    			 "uk.ac.gate.plugins", "tagger-numbers", "8.5")); 
    	  Gate.getCreoleRegister().registerPlugin(new Plugin.Maven( 
    				 "uk.ac.gate.plugins", "tagger-measurements", "8.5"));
    	  
    	  ProcessingResource numbers = (ProcessingResource) 
    	  Factory.createResource("gate.creole.numbers.NumbersTagger");
    	  annieController.add(numbers);
    	  ProcessingResource measurements = (ProcessingResource) 
    	  Factory.createResource("gate.creole.measurements.MeasurementsTagger");
    	  
    	  annieController.add(measurements);*/
    	  
    	  
    	  // Tell ANNIE’s controller about the corpus you want to run on 
    	  //annieController.setCorpus(corpus); 
    	  
//    	  
//    	  FeatureMap params = Factory.newFeatureMap(); 
//    	  try {
//    		params.put("listsURL", new File("/home/jcorvi/eTRANSAFE_DATA/dictionaries/lists.def").toURL());
//    	  } catch (MalformedURLException e) {
//    		// TODO Auto-generated catch block
//    		e.printStackTrace();
//    	  }
//    	  params.put("gazetteerFeatureSeparator", "\t");
//    	  ProcessingResource treatment_related_finding_gazetter = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params); 
//    	  annieController.add(treatment_related_finding_gazetter);
//    	  // Run ANNIE 
//    	  annieController.execute();
//    	  
    	  try {
    		  
    		  SerialAnalyserController japecontroller =  (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",  
    	    	      Factory.newFeatureMap(), 
    	    	       Factory.newFeatureMap(), "JAPE");
    		  
//    		  	LanguageAnalyser jape = (LanguageAnalyser)gate.Factory.createResource(
//    		          "gate.creole.Transducer", gate.Utils.featureMap("grammarURL", new File("/home/jcorvi/eTRANSAFE_DATA/jape_rules/study_domain.jape").toURI().toURL(),
//    		              "encoding", "UTF-8"));
//    		  	jape.setParameterValue("inputASName", "BSC");
//    		  	jape.setParameterValue("outputASName", "BSC");
//    		  	japecontroller.add(jape);
    		  	
    		  	LanguageAnalyser jape_token = (LanguageAnalyser)gate.Factory.createResource(
      		          "gate.creole.Transducer", gate.Utils.featureMap("grammarURL", new File("jape_rules/token_rule.jape").toURI().toURL(),
      		              "encoding", "UTF-8"));
    		  	jape_token.setParameterValue("inputASName", "PREPROCESSING");
    		  	jape_token.setParameterValue("outputASName", "BSC");
    		  	japecontroller.add(jape_token);
    		  	
    		  
    		  	LanguageAnalyser jape_sentence = (LanguageAnalyser)gate.Factory.createResource(
      		          "gate.creole.Transducer", gate.Utils.featureMap("grammarURL", new File("jape_rules/sentence_rule.jape").toURI().toURL(),
      		              "encoding", "UTF-8"));
    		  	jape_sentence.setParameterValue("inputASName", "PREPROCESSING");
    		  	jape_sentence.setParameterValue("outputASName", "BSC");
    		  	japecontroller.add(jape_sentence);
    		  	
    		  	LanguageAnalyser jape_trf = (LanguageAnalyser)gate.Factory.createResource(
        		          "gate.creole.Transducer", gate.Utils.featureMap("grammarURL", new File("jape_rules/treatment_related_finding_rule.jape").toURI().toURL(),
        		              "encoding", "UTF-8"));
    		  	jape_trf.setParameterValue("inputASName", "BSC");
    		  	jape_trf.setParameterValue("outputASName", "TEMPLATE");
    		  	japecontroller.add(jape_trf);
    		  	
    		  	japecontroller.setCorpus(corpus);
    			japecontroller.execute();
    		} catch (MalformedURLException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
    		}
    	
    	  
    	  
    	for (Document  document : corpus) {
    		java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(new File(outputDirectory + File.separator +document.getName().substring(0, document.getName().indexOf(".xml")+4) ), false)));
		    out.write(document.toXml());
		    out.close();
		}
    	 
    	System.out.println("End process");
    	  
		
	}

	
    
    /**
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void processExtractionRelation(String inputDirectoryPath, String outputDirectoryPath, String workdir) throws IOException {
    	log.info("App::processExtractionRelation :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("Wrapper::processExtractionRelation :: processing file : " + file.getAbsolutePath());
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + file.getName());
						executeDocument(file, outputGATEFile);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::processExtractionRelation :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::processExtractionRelation :: error with document " + file.getAbsolutePath(), e);
					} catch (InvalidOffsetException e) {
						log.error("Wrapper::processExtractionRelation :: error with document " + file.getAbsolutePath(), e);
					} catch (GateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
		}else {
			System.out.println("No directory :  " + inputDirectoryPath);
		}
		log.info("Wrapper::processExtractionRelation :: END ");
	}

		
	private static void executeDocument(File inputFile, File outputGATEFile) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
		gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		try {
	    	Set<String> annotationsSet = gateDocument.getAnnotationSetNames();
			for (String annotationSet : annotationsSet) {
				System.out.println("App :: executeDocument :: SET: " + annotationSet);
				AnnotationSet annSet = gateDocument.getAnnotations(annotationSet); 
				for (String annotationType : annSet.getAllTypes()) {
					System.out.println(annotationType);
					
				}
			}
			java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
			out.write(gateDocument.toXml());
			out.close();
		} catch (IOException e) {
			System.out.println("App :: executeDocument :: error IO Exception");
			e.printStackTrace();
		}
	}

	
	
	
}
