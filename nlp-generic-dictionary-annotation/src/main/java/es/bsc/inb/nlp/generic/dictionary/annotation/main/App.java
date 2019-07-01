package es.bsc.inb.nlp.generic.dictionary.annotation.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
 * Generic Library for Dictionary based annotation in Gate.
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
        
        Option listDefinitions = new Option("l", "lists_definitions", true, "Dictionary List definitions. "
        		+ "A lists.def Gate-formatted file separated by tab can be provided or a zip file that contains the dictionary/gazetteer files including the lists.def ");
        listDefinitions.setRequired(true);
        options.addOption(listDefinitions);
     
        Option set = new Option("a", "annotation_set", true, "Annotation set where the annotation will be included");
        set.setRequired(true);
        options.addOption(set);
        
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
        String listsDefinitionsPath = cmd.getOptionValue("lists_definitions");
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		System.out.println(" Please set the inputDirectoryPath ");
			System.exit(1);
    	}
        
        if(workdirPath==null) {
    		workdirPath = "";
		}

        listsDefinitionsPath = workdirPath+listsDefinitionsPath;
        if (!java.nio.file.Files.isRegularFile(Paths.get(listsDefinitionsPath))) {
        	System.out.println(" Please set the list of dictionaries to annotate. You can provide the list.def file or a zip file. Please if you provided a zip file remember that it must contain a list.def file inside");
			System.exit(1);
    	}
        
        if(listsDefinitionsPath.endsWith(".zip")) {
        	try {
	       		File file = new File(listsDefinitionsPath);
	       		String dictionaryFolderPath =  file.getName().substring(0, file.getName().indexOf(".zip"));
	       		unZipIt(listsDefinitionsPath,  workdirPath  + dictionaryFolderPath );
	       		listsDefinitionsPath = workdirPath  + dictionaryFolderPath + File.separator + "lists.def";
	       		if (!java.nio.file.Files.isRegularFile(Paths.get(listsDefinitionsPath))) {
	               	System.out.println("Please if you provided a zip file remember that it must contain a list.def file inside.");
	       			System.exit(1);
	           	}
        	}catch(Exception e) {
               	System.out.println("Error unziping directory, please if you provided a zip file remember that it must contain a list.def file inside. ");
               	System.exit(1);
            }
        }else if(listsDefinitionsPath.endsWith(".def")) {
        	System.out.println(" Please set the list of dictionaries to annotate.  No list.def file or .zip file provided.");
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
			System.out.println("App :: main :: Gate Exception  ");
			e.printStackTrace();
			System.exit(1);
		} 
 
	    try {
	    	String japeRules = workdirPath+"jape_rules/main.jape";
	    	process(inputFilePath, outputFilePath, listsDefinitionsPath, japeRules, annotationSet, workdirPath);
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
    private static void process(String inputDirectory, String outputDirectory, String listsDefinitionsPath, String japeRules, String annotationSet, String workdirPath) throws GateException, IOException {
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
	    	//Gazetter parameters
	    	FeatureMap params = Factory.newFeatureMap(); 
	    	
	    	params.put("listsURL", new File(listsDefinitionsPath).toURL());
	    	params.put("gazetteerFeatureSeparator", "\t");
	    	params.put("caseSensitive",false);
	    	//params.put("longestMatchOnly", true);
	    	//params.put("wholeWordsOnly", false);
	    	ProcessingResource pr_gazetter = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params); 
	    	//pr_gazetter.setParameterValue("longestMatchOnly", true);
	    	//pr_gazetter.setParameterValue("wholeWordsOnly", false);
	    	annieController.add(pr_gazetter);
	    	
		    LanguageAnalyser jape = (LanguageAnalyser)gate.Factory.createResource("gate.creole.Transducer", gate.Utils.featureMap(
				              "grammarURL", new File(japeRules).toURI().toURL(),"encoding", "UTF-8"));
			jape.setParameterValue("outputASName", annotationSet);
			annieController.add(jape);
			// execute controller 
		    annieController.execute();
		    Factory.deleteResource(pr_gazetter);
		    Factory.deleteResource(jape);
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
    
    /**
     * Basic unzipping folder method
     * @param input
     * @param output
     * @throws IOException
     */
    private static void unZipIt(String zipFile, String outputFolder){
    	byte[] buffer = new byte[1024];
        try{
	       	//create output directory is not exists
	       	File folder = new File(outputFolder);
	       	if(!folder.exists()){
	       		folder.mkdir();
	       	}
	       	//get the zip file content
	       	ZipInputStream zis =
	       		new ZipInputStream(new FileInputStream(zipFile));
	       	//get the zipped file list entry
	       	ZipEntry ze = zis.getNextEntry();
	       	if(ze==null) {
	       		System.out.println("Error unziping file, please review if you zip file provided is not corrupt file remember that it must contain a list.def file inside.");
               	System.exit(1);
	       	}
	       	while(ze!=null){
	       	   String fileName = ze.getName();
	           File newFile = new File(outputFolder + File.separator + fileName);
	           System.out.println("file unzip : "+ newFile.getAbsoluteFile());
	           //create all non exists folders
	           //else you will hit FileNotFoundException for compressed folder
	           new File(newFile.getParent()).mkdirs();
	           FileOutputStream fos = new FileOutputStream(newFile);
	           int len;
	           while ((len = zis.read(buffer)) > 0) {
	        	   fos.write(buffer, 0, len);
	           }
	           fos.close();
	           ze = zis.getNextEntry();
	       	}
	       	zis.closeEntry();
	       	zis.close();
	       	System.out.println("Done");
       }catch(IOException ex){
          ex.printStackTrace();
       }
    }
}
