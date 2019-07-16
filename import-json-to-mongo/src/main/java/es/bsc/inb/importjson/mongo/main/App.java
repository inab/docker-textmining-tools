package es.bsc.inb.importjson.mongo.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;



/**
 * Import JSON to Mongo DataBase
 * 
 * @author jcorvi
 *
 */
public class App {
	
	public static void main(String[] args ){
    	
    	Options options = new Options();
    	
        Option input = new Option("i", "input", true, "input directory path, contains the json fields");
        input.setRequired(true);
        options.addOption(input);
        
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
        String workdirPath = cmd.getOptionValue("workdir");
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		System.out.println("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
        if(workdirPath==null) {
	    	workdirPath="";
	    }
	    MongoClient mongoClient = new MongoClient("localhost", 27017);
	    DB db = mongoClient.getDB("ADES");
	    try {
			process(inputFilePath, workdirPath, db);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    mongoClient.close();
	}
    
	/**
	 * Process directory and convert XML GATE format to JSON 
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void process(String inputDirectoryPath, String workdir, DB mongoDB ) throws IOException {
    	System.out.println("App::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".json")){
					try {
						System.out.println("App::process :: document: " + file);
						processDocument(file, mongoDB);
					} catch (Exception e) {
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
	 * @param inputFile
	 * @param outputGATEFile
	 * @throws ResourceInstantiationException
	 * @throws IOException 
	 * @throws JsonGenerationException 
	 * @throws InvalidOffsetException
	 */
	private static void processDocument(File inputFile, DB mongoDB){
		DBCollection collection = mongoDB.getCollection("reports");
		try {
			String jsonString = FileUtils.readFileToString(inputFile, "UTF-8");
			DBObject dbo = (DBObject) com.mongodb.util.JSON.parse(jsonString);
			List<DBObject> list = new ArrayList<DBObject>();
			list.add(dbo);
			collection.insert(list);
		} catch (IOException e) {
			System.out.println("App::process::ERROR document " + inputFile);
			e.printStackTrace();
		}
	}
	

}
