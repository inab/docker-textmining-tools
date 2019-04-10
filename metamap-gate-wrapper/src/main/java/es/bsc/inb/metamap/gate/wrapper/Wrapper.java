package es.bsc.inb.metamap.gate.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.creole.Plugin;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * MetaMap Wrapper for Gate document and TEI standard format
 * @author jcorvi
 *
 */
class Wrapper {
	
	static final Logger log = Logger.getLogger("log");
	
	static Map<String,String> semanticTypesMap = new HashMap<String,String>();
	
	/**
	 * Entry method to execute the system
	 * @param args
	 */
    public static void main(String[] args) {
    	
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
        
        Option metaMapServerOption = new Option("metaMapServer", "metaMapServer", true, "Metamap Server, default localhost");
        metaMapServerOption.setRequired(false);
        options.addOption(metaMapServerOption);
        
        Option portServerOption = new Option("metaMapPortServer", "metaMapPortServer", true, "Metamap port, default 8066");
        portServerOption.setRequired(false);
        options.addOption(portServerOption);
        
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
        String metaMapServer = cmd.getOptionValue("metaMapServer");
        String metaMapPortServer = cmd.getOptionValue("metaMapPortServer");
        
        
        System.out.println("Input : " + inputFilePath );
        System.out.println("Output : " + outputFilePath );
        if(metaMapServer==null) {
        	metaMapServer = "localhost";
        }
        if(metaMapPortServer==null) {
        	metaMapPortServer="8066";
        }
        
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		log.error("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
    	File outputDirectory = new File(outputFilePath);
	    if(!outputDirectory.exists())
	    	outputDirectory.mkdirs();
        
	    //Gate init
    	try {
			Gate.init();
		} catch (GateException e) {
			log.error("Wrapper::main :: Gate Init Exception  ", e);
			System.exit(1);
		}
    	
    	try {
    		loadSemanticTypes(workdirPath);
    	}catch(Exception e) {
    		log.error("Exception ocurred see the log for more information", e);
    		System.exit(1);
    	}
    	
    	//Plugin init
    	SerialAnalyserController metaMapCongroller = null;
    	try {
    		if(workdirPath==null) {
        		workdirPath = "";
    		}
        	String plugin_directory = workdirPath +  "plugins/";
        	metaMapCongroller = init(plugin_directory, semanticTypesMap.get("semanticTypesMetaMapPar").toString(), metaMapServer, metaMapPortServer);
		} catch (MalformedURLException e) {
			log.error("Wrapper::main :: MetaMap Tagger Plugin Init Exception  ", e);
			System.exit(1);
		} catch (GateException e) {
			log.error("Wrapper::main :: MetaMap Tagger Plugin Init Exception  ", e);
			System.exit(1);
		}
    	
    	try {
    		processTagger(metaMapCongroller, inputFilePath, outputFilePath);
    	}catch(Exception e) {
    		log.error("Exception ocurred see the log for more information", e);
    		System.exit(1);
    	}
    	
    }   
    /**
     * Load semantic type to retrieve and the corresponding label mapping
     * @throws IOException 
     * 
     */
    private static void loadSemanticTypes(String workdirPath) throws IOException {
    	if(workdirPath==null) {
    		workdirPath = "";
		}
    	String file = workdirPath + "semantic_types_mapping.txt";
    	if (Files.isRegularFile(Paths.get(file))) {
    		BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    StringJoiner joiner = new StringJoiner(",");
		    while ((line = br.readLine()) != null) {
		    	if(!line.startsWith("#") && !line.trim().equals("")) {
		    		String[] data = line.split("\\|");
			    	if(data.length==4) {
			    		semanticTypesMap.put(data[0], data[3]);
			    	}else {
			    		semanticTypesMap.put(data[0], data[2]);
			    	}
			    	joiner.add(data[0]);
		    	}
		    }
		    semanticTypesMap.put("semanticTypesMetaMapPar", joiner.toString());
		    br.close();
    	}else {
    		log.error("Cannot find semantic types mapping file in: " + file);
    		System.out.println("Cannot find semantic types mapping file in: " + file);
    		System.exit(1);
    	}
	}

	/**
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
	 */
	public static void processTagger(SerialAnalyserController metaMapCongroller, String inputDirectoryPath, String outputDirectoryPath) {
		log.info("Wrapper::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("Wrapper::processTagger :: processing file : " + file.getAbsolutePath());
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + file.getName());
						executeMetaMapTagger(metaMapCongroller, file, outputGATEFile);
						mapUMLSTerminolgy(outputGATEFile, outputGATEFile);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (InvalidOffsetException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (FileNotFoundException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (IOException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (GateException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					}
				}
			}
		}else {
			System.out.println("No directory :  " + inputDirectoryPath);
		}
		log.info("Wrapper::generatePlainText :: END ");
	}
	
	/**
	 * Map the MetaMap annotation to the Treatment related finding template.
	 * @param outputFilePath
	 * @param outputFilePath2
	 * @throws ResourceInstantiationException 
	 * @throws InvalidOffsetException 
	 * @throws IOException 
	 */
    private static void mapUMLSTerminolgy(File inputFilePath, File outputFilePath) throws ResourceInstantiationException, InvalidOffsetException, IOException {
    	log.info("Wrapper :: mapUMLSTerminolgy :: PROCESS DOCUMENT : " +  inputFilePath.getAbsolutePath());
		gate.Document gateDocument = Factory.newDocument((inputFilePath).toURI().toURL(), "UTF-8");
		AnnotationSet annSetDef = gateDocument.getAnnotations();
		AnnotationSet metamapAnnotations = annSetDef.get("METAMAP");
		for (Annotation annotation : metamapAnnotations) {
			FeatureMap features = annotation.getFeatures();
			List<Object> semanticTypes = (List<Object>)features.get("SemanticTypes");
			for (Object semanticType : semanticTypes) {
				String label = semanticTypesMap.get(semanticType.toString());
				features.put("inst", "BSC");
				gateDocument.getAnnotations("BSC").add(annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset(),  label, features);
			}
		}
		annSetDef.removeAll(metamapAnnotations);
		//Save document
		java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputFilePath, false)));
		out.write(gateDocument.toXml());
		out.close();
		
	}
    
	

	/**
     * Plugin initialization
     * @return
     * @throws MalformedURLException 
     * @throws GateException
     * @throws IOException
     */
    public static SerialAnalyserController init(String pluginDirectory,  String semanticTypes,String server, String port) throws MalformedURLException, GateException {
    	Gate.getCreoleRegister().registerPlugin(new Plugin.Directory(new File(pluginDirectory+"ANNIE/").toURI().toURL()));
    	Gate.getCreoleRegister().registerPlugin(new Plugin.Directory(new File(pluginDirectory+"Tagger_MetaMap/").toURI().toURL()));
    	FeatureMap params = Factory.newFeatureMap(); 
	  	params.put("outputASType", "METAMAP");
	  	if(!semanticTypes.equals("")){
	  		params.put("metaMapOptions", "-J " + semanticTypes+" --metamap_server_host "+server+" --metamap_server_port "+port);
	  	}else {
	  		params.put("metaMapOptions", "");
	  	}
	  	params.put("outputMode", "HighestMappingOnly");
	  	params.put("taggerMode", "CoReference");
	  	params.put("annotNormalize", "None");
	  	params.put("annotateNegEx", "false");
	  	params.put("annotatePhrases", "false");
	  	
	  	ProcessingResource tagger = (ProcessingResource) Factory.createResource("gate.metamap.MetaMapPR", params);
	  	SerialAnalyserController metaMapCongroller = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController", Factory.newFeatureMap(), Factory.newFeatureMap(), "METAMAP"); 
	  	metaMapCongroller.add(tagger);
    	return metaMapCongroller;
    } 
    
   
    /**
     * Execute of metamap tagger  
     * @param metaMapCongroller application gate
     * @param inputGATEFile input gate file
     * @param outputGATEFile output gate file
     * @throws GateException
     * @throws IOException
     */
    public static void executeMetaMapTagger(SerialAnalyserController metaMapCongroller, File inputGATEFile, File outputGATEFile) throws GateException, IOException {
  	  	Corpus corpus = Factory.newCorpus("My XML Files"); 
	  	gate.Document gateDocument = Factory.newDocument(inputGATEFile.toURI().toURL(), "UTF-8");
	  	corpus.add(gateDocument);
	  	metaMapCongroller.setCorpus(corpus); 
	  	//Execute in corpus of one document
	  	try {
	  		metaMapCongroller.execute();
	  		gate.Document annotatedGateDocument = corpus.get(0);
		  	java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
			out.write(annotatedGateDocument.toXml());
			out.close();
	  	}catch(Exception e) {
	  		
	  	}
	  	
	  	

	} 

	/**
	 * Force deletion of directory
	 * @param path
	 * @return
	 */
	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
	        File[] files = path.listFiles();
	        for (int i = 0; i < files.length; i++) {
	            if (files[i].isDirectory()) {
	                deleteDirectory(files[i]);
	            } else {
	                files[i].delete();
	            }
	        }
	    }
	    return (path.delete());
	}
}
