package es.bsc.inb.evaluation.ner.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import gate.Factory;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * 
 * Application for annotation validation in GATE-formatted file.
 * 
 * 
 * @author jcorvi
 *
 */
public class App {
	
	public static void main( String[] args ) {
    	
    	Options options = new Options();

        Option input = new Option("i", "input", true, "input directory path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "Output file with the results");
        output.setRequired(true);
        options.addOption(output);
        
        Option key_set = new Option("k", "key_set", true, "Key Annotation Set with the golden values ");
        key_set.setRequired(true);
        options.addOption(key_set);
        
        Option set = new Option("e", "evaluated_set", true, "Set to evaluate");
        set.setRequired(true);
        options.addOption(set);
        
        Option print_by_doc = new Option("d", "print_by_doc", true, "Indicates if the output has to contain the result by each document");
        print_by_doc.setRequired(false);
        options.addOption(print_by_doc);
        
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
        String keySet = cmd.getOptionValue("key_set");
        String evaluatedSet = cmd.getOptionValue("evaluated_set");
        String printByDocParam = cmd.getOptionValue("print_by_doc");
        if (!java.nio.file.Files.isDirectory(Paths.get(inputPath))) {
        	System.out.println("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
        if (keySet==null) {
        	System.out.println("Please set the key_sest parameter with the golden values ");
			System.exit(1);
    	}
        if (evaluatedSet==null) {
        	System.out.println("Please set the evaluated_set , set to be evaluated ");
			System.exit(1);
    	}
        Boolean printByDoc = false;
        if(printByDocParam!=null && printByDocParam.equals("true")) {
        	printByDoc = true;
        }
        
        try {
			Gate.init();
		} catch (GateException e) {
			System.out.println("Wrapper::generatePlainText :: Gate Exception  ");
			System.out.println(e);
			System.exit(1);
		}
	    processEvaluation(inputPath, outputPath, keySet, evaluatedSet, printByDoc);
	}

	/**
	 * Process evaluation of Documents
	 */
	private static void processEvaluation(String inputDirectoryPath, String outputPath, String key_set, String set, Boolean printByDoc) {
		AnnotationEvaluator service = new AnnotationEvaluator(key_set, set, AnnotationEvaluator.measuresClassification[0].toString(), 0);
		System.out.println("App::processEvaluation :: INIT ");
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
						System.out.println("App::processEvaluation :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					} catch (MalformedURLException e) {
						System.out.println("App::processEvaluation :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					}
				}
			}
			String results = service.getFscoreMeasures(printByDoc);
			try {
				createTxtFile(outputPath, results);
			} catch (FileNotFoundException e) {
				System.out.println("App::processEvaluation :: FileNotFoundException " + outputPath);
				System.out.println(e);
			} catch (IOException e) {
				System.out.println("App::processEvaluation :: IOException " + outputPath);
				System.out.println(e);
			}
			String results3 = service.getFscoreMeasuresCSV(printByDoc);
			System.out.println(results3);
			try {
				createTxtFile(outputPath+".csv", results3);
			} catch (FileNotFoundException e) {
				System.out.println("App::processEvaluation :: FileNotFoundException " + outputPath);
				System.out.println(e);
			} catch (IOException e) {
				System.out.println("App::processEvaluation :: IOException " + outputPath);
				System.out.println(e);
			}
			String resultsJSON = service.getFscoreMeasuresJSON();
			System.out.println(resultsJSON);
			try {
				createTxtFile(outputPath+".json", resultsJSON);
			} catch (FileNotFoundException e) {
				System.out.println("App::processEvaluation :: FileNotFoundException " + outputPath);
				System.out.println(e);
			} catch (IOException e) {
				System.out.println("App::processEvaluation :: IOException " + outputPath);
				System.out.println(e);
			}
		}else {
			System.out.println("App::processEvaluation :: No directory :  " + inputDirectoryPath);
		}
		System.out.println("App::processEvaluation :: END ");
		
	}
	
	/**
	 * Create a plain text file with the given string
	 * @param path
	 * @param plainText
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void createTxtFile(String path, String plainText) throws FileNotFoundException, IOException {
		File fout = new File(path);
		FileOutputStream fos = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,StandardCharsets.UTF_8));
		bw.write(plainText);
		bw.flush();
		bw.close();
	}
	
   
}
