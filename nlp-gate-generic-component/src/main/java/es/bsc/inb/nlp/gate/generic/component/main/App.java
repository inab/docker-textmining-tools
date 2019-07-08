package es.bsc.inb.nlp.gate.generic.component.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.creole.Plugin;
import gate.creole.SerialAnalyserController;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;

/**
 * Generic Library for execute GATE Dictionary/Gazetteer and JAPE rules processing in batch mode.
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
        
        Option listDefinitions = new Option("l", "lists_definitions", true, "Dictionary List definitions, Gate format.");
        listDefinitions.setRequired(false);
        options.addOption(listDefinitions);
        
        Option japeMain = new Option("j", "jape_main", true, "Jape Main file for processing rules");
        japeMain.setRequired(false);
        options.addOption(japeMain);
        
        Option set = new Option("a", "annotation_set", true, "Output Annotation Set. Annotation set where the annotation will be included for the gazetter lookup and for the Jape Rules");
        set.setRequired(true);
        options.addOption(set);
        
        Option iset = new Option("ia", "input_annotation_set", true, "Input Annotation Set. If you want to provided different input annotation set this parameter.  By default the -a output annotation set is used as input.");
        iset.setRequired(false);
        options.addOption(iset);
        
        Option workdir = new Option("w", "workdir", true, "workDir directory path");
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
        String inputAnnotationSet = cmd.getOptionValue("input_annotation_set");
        String listsDefinitionsPath = cmd.getOptionValue("lists_definitions");
        String japeMainPath = cmd.getOptionValue("jape_main");
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		System.out.println(" Please set the inputDirectoryPath ");
			System.exit(1);
    	}
        
        if (annotationSet==null) {
        	System.out.println("Please set the annotation set where the annotation will be included");
			System.exit(1);
    	}
        
        if (inputAnnotationSet==null) {
        	System.out.println("The input annotation set not set, same as output is selected");
        	inputAnnotationSet = annotationSet;
		}
        
        if(workdirPath==null) {
    		workdirPath = "";
		}

        Boolean execution = false;
        
        if(listsDefinitionsPath==null) {
        	System.out.println("No dictionary was provided.");
        }else {
        	listsDefinitionsPath = workdirPath+listsDefinitionsPath;
        	execution = true;
        	if (!java.nio.file.Files.isRegularFile(Paths.get(listsDefinitionsPath))) {
        		System.out.println("Please set a correct path to the list of dictionaries to annotate");
    			System.exit(1);
            }
        }
        
        if(japeMainPath==null) {
        	System.out.println("No Jape Main Rules were provided.");
        }else {
        	japeMainPath = workdirPath+japeMainPath;
        	execution = true;
        	if (!java.nio.file.Files.isRegularFile(Paths.get(japeMainPath))) {
        		System.out.println("Please set a correct path to the main jape rules");
    			System.exit(1);
            }
        }
        
        if(!execution) {
        	System.out.println("No gazzeter or Jape Rules were provided. There is nothing to do. Please review your configuration");
        	System.exit(1);
        }
        
        File outputDirectory = new File(outputFilePath);
	    if(!outputDirectory.exists())
	    	outputDirectory.mkdirs();
	    
	    try {
			Gate.init();
		} catch (GateException e) {
			System.out.println("App :: main :: Gate Exception  ");
			e.printStackTrace();
			System.exit(1);
		} 
 
	    try {
	    	process(inputFilePath, outputFilePath, listsDefinitionsPath, japeMainPath, inputAnnotationSet, annotationSet, workdirPath);
		} catch (GateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

    /**
     * Annotation Process
     * @param inputDirectory
     * @param outputDirectory
     * @throws GateException
     * @throws IOException
     */
    private static void process(String inputDirectory, String outputDirectory, String listsDefinitionsPath, String japeRules, String inputAnnotationSet, String outAnnotationSet,  String workdirPath) throws GateException, IOException {
    	try {
    		System.out.println("App :: main :: INIT PROCESS");
	    	Corpus corpus = Factory.newCorpus("My Files"); 
	    	File directory = new File(inputDirectory); 
	    	ExtensionFileFilter filter = new ExtensionFileFilter("Txt files", new String[]{"txt","xml"}); 
	    	URL url = directory.toURL(); 
	    	corpus.populate(url, filter, null, false);
	    	Plugin anniePlugin = new Plugin.Maven("uk.ac.gate.plugins", "annie", "8.5"); 
	    	Gate.getCreoleRegister().registerPlugin(anniePlugin); 
	    	// create a serial analyser controller to run ANNIE with 
	    	SerialAnalyserController annieController =  (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",
	    			Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE"); 
	    	annieController.setCorpus(corpus); 
	    	
	    	ProcessingResource pr_gazetter = null;
	    	if(listsDefinitionsPath!=null) {
		    	//Gazetter parameters
		    	FeatureMap params = Factory.newFeatureMap(); 
		    	params.put("listsURL", new File(listsDefinitionsPath).toURL());
		    	params.put("gazetteerFeatureSeparator", "\t");
		    	params.put("caseSensitive",false);
		    	pr_gazetter = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params);
		    	pr_gazetter.setParameterValue("annotationSetName", outAnnotationSet);
		    	annieController.add(pr_gazetter);
	    	}
	    	
	    	LanguageAnalyser jape = null;
	    	if(japeRules!=null) {
	    		jape = (LanguageAnalyser)gate.Factory.createResource("gate.creole.Transducer", gate.Utils.featureMap(
			              "grammarURL", new File(japeRules).toURI().toURL(),"encoding", "UTF-8"));
	    		jape.setParameterValue("inputASName", inputAnnotationSet);
				jape.setParameterValue("outputASName", outAnnotationSet);

				annieController.add(jape);
			}
		    
	    	// execute controller 
		    annieController.execute();
		    
		    //free resources
		    if(pr_gazetter!=null) {
		    	Factory.deleteResource(pr_gazetter);
		    }
		    if(jape!=null) {
		    	Factory.deleteResource(jape);
		    }
		    Factory.deleteResource(annieController);	
		    Gate.removeKnownPlugin(anniePlugin);	
		    
		    //Save documents in different output
		    for (Document  document : corpus) {
		    	String nameOutput = "";
		    	if(document.getName().indexOf(".txt")!=-1) {
		    		nameOutput =  document.getName().substring(0, document.getName().indexOf(".txt")+4).replace(".txt", ".xml"); 
		    	}else {
		    		nameOutput = document.getName().substring(0, document.getName().indexOf(".xml")+4);
		    	}
		    	java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(new File(outputDirectory + File.separator + nameOutput), false)));
			    out.write(document.toXml());
			    out.close();
			}
    	}catch(Exception e) {
    		System.out.println("App :: main :: ERROR ");
    		e.printStackTrace();
    		System.exit(1);
	    }	 
	    System.out.println("App :: main :: END PROCESS");
    }
    
    
}
