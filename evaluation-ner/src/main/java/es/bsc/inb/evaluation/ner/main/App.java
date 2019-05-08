package es.bsc.inb.evaluation.ner.main;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import gate.Factory;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * 
 * Application for annotation validation.
 * 
 * @author jcorvi
 *
 */
public class App {
	
	static final Logger log = Logger.getLogger("log");
	
	public static void main( String[] args ) {
    	
    	Options options = new Options();

        Option input = new Option("i", "input", true, "input directory path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "Output file with the results");
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
    	
       
        String inputPath = cmd.getOptionValue("input");
        String outputPath = cmd.getOptionValue("output");
        String workdirPath = cmd.getOptionValue("workdir");
        
        if (!java.nio.file.Files.isDirectory(Paths.get(inputPath))) {
    		log.error("Please set the inputDirectoryPath ");
			System.exit(1);
    	}

	    
	    try {
			Gate.init();
		} catch (GateException e) {
			log.error("Wrapper::generatePlainText :: Gate Exception  ", e);
			System.exit(1);
		}
 
	    
		processEvaluation(inputPath, outputPath);
		
	}

	/**
	 * Process evaluation of Documents
	 */
	private static void processEvaluation(String inputDirectoryPath, String outputPath) {
		AnnotationEvaluator service = new AnnotationEvaluator("EVALUATION", "BSC", AnnotationEvaluator.measuresClassification[0].toString(), 0);
		log.info("App::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("Wrapper::processTagger :: processing file : " + file.getAbsolutePath());
						gate.Document gateDocument = Factory.newDocument(file.toURI().toURL(), "UTF-8");
						service.processDocument(gateDocument);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					}
				}
			}
		}else {
			System.out.println("No directory :  " + inputDirectoryPath);
		}
		
	}

	
	
    
}
