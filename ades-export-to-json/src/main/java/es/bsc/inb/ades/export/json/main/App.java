package es.bsc.inb.ades.export.json.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * ADES Export to JSON. 
 * 
 * Export from GATE format to JSON. 
 * 
 * 
 * @author jcorvi
 *
 */
public class App {
	
	public static void main(String[] args ){
    	
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
    		System.out.println("Please set the inputDirectoryPath ");
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
			process(inputFilePath, outputFilePath,workdirPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	/**
	 * Process directory and convert XML GATE format to JSON 
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void process(String inputDirectoryPath, String outputDirectoryPath, String workdir) throws IOException {
    	System.out.println("App::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("App::process :: processing file : " + file.getAbsolutePath());
						String fileOutPutName = file.getName().replace(".xml", ".json");
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + fileOutPutName);
						processDocument2(file, outputGATEFile);
					} catch (ResourceInstantiationException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} catch (MalformedURLException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
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
	private static void processDocument(File inputFile, File outputGATEFile) throws ResourceInstantiationException, JsonGenerationException, IOException{
		gate.Document doc = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		AnnotationSet as = doc.getAnnotations("BSC");
	    Map<String, Collection<Annotation>> anns = new HashMap<String, Collection<Annotation>>();
	    anns.put("FINDING", as.get("FINDING"));
	    anns.put("SEX", as.get("SEX"));
	    anns.put("SPECIMEN", as.get("SPECIMEN"));
	    anns.put("GROUP", as.get("GROUP"));
	    anns.put("DOSE_DURATION", as.get("DOSE_DURATION"));
	    anns.put("DOSE_QUANTITY", as.get("DOSE_QUANTITY"));
	    anns.put("DOSE_FREQUENCY", as.get("DOSE_FREQUENCY"));
	    anns.put("MANIFESTATION_FINDING", as.get("MANIFESTATION_FINDING"));
	    anns.put("RISK_LEVEL", as.get("RISK_LEVEL"));
	    anns.put("NO_TREATMENT_RELATED_TRIGGER", as.get("NO_TREATMENT_RELATED_TRIGGER"));
	    anns.put("TREATMENT_RELATED_TRIGGER", as.get("TREATMENT_RELATED_TRIGGER"));
	    anns.put("STUDY_DOMAIN", as.get("STUDY_DOMAIN"));
	    anns.put("STUDY_DAY_FINDING", as.get("STUDY_DAY_FINDING"));
	    anns.put("STUDY_TESTCD", as.get("STUDY_TESTCD"));
	    anns.put("ROUTE_OF_ADMINISTRATION", as.get("ROUTE_OF_ADMINISTRATION"));
	    anns.put("MODE_OF_ACTION", as.get("MODE_OF_ACTION"));
	    anns.put("STATISTICAL_SIGNIFICANCE", as.get("STATISTICAL_SIGNIFICANCE"));
	    anns.put("CYPS", as.get("CYPS"));
	    
	    
	    AnnotationSet as2 = doc.getAnnotations("TREATMENT_RELATED_FINDINGS");
	    //Map<String, Collection<Annotation>> anns_findings = new HashMap<String, Collection<Annotation>>();
	    //Collection<Annotation> coll = new ArrayList<Annotation>();
	    for (String finding : as2.getAllTypes()) {
	    	anns.put(finding, as2.get(finding));
	    }
	    //anns.put("TREATMENT_RELATED_FINDINGS", coll);
	    java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
    	gate.corpora.DocumentJsonUtils.writeDocument(doc, anns, out);
		out.close();
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
	private static void processDocument2(File inputFile, File outputGATEFile) throws ResourceInstantiationException, JsonGenerationException, IOException, InvalidOffsetException{
		gate.Document doc = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		Gson gsonBuilder = new GsonBuilder().create();
		JsonObject document = new JsonObject();
		document.addProperty("name", doc.getName().substring(0, doc.getName().indexOf(".xml")+4));
		String plainText = doc.getContent().getContent(0l, gate.Utils.lengthLong(doc)).toString();
		document.addProperty("text", plainText);
		document.addProperty("id", System.currentTimeMillis());
		JsonObject section = new JsonObject();
		section.addProperty("name", "document");
		Set<String> types = Stream.of("FINDING","SEX","SPECIMEN","GROUP","DOSE_DURATION","DOSE_QUANTITY","DOSE_FREQUENCY","MANIFESTATION_FINDING","RISK_LEVEL","NO_TREATMENT_RELATED_TRIGGER",
				"TREATMENT_RELATED_TRIGGER","STUDY_DOMAIN","STUDY_DAY_FINDING","STUDY_TESTCD", "ROUTE_OF_ADMINISTRATION","MODE_OF_ACTION","STATISTICAL_SIGNIFICANCE","CYPS").collect(Collectors.toCollection(HashSet::new));
		JsonObject entities = new JsonObject();
		AnnotationSet as = doc.getAnnotations("BSC").get(types);
	    for (String type : as.getAllTypes()) {
	    	JsonArray type_array = new JsonArray();
	    	for (Annotation annotation : as.get(type)) {
		    	JsonObject annotationObject = new JsonObject();
		    	annotationObject.addProperty("type", annotation.getType());
		    	annotationObject.addProperty("text", gate.Utils.stringFor(doc, annotation));
		    	annotationObject.addProperty("startOffset", annotation.getStartNode().getOffset());
		    	annotationObject.addProperty("endOffset", annotation.getEndNode().getOffset());
		    	JsonArray features = new JsonArray();
		    	for (Object key : annotation.getFeatures().keySet()) {
		    		JsonObject feature = new JsonObject();
		    		feature.addProperty("name", key.toString());
		    		feature.addProperty("value", annotation.getFeatures().get(key).toString());
		    		features.add(feature);
				}
		    	annotationObject.add("features", features);
		    	type_array.add(annotationObject);
		    }
	    	entities.add(type, type_array);
		}
		
	    document.add("annotations", entities);
	    JsonArray findings = new JsonArray();
	    AnnotationSet as2 = doc.getAnnotations("TREATMENT_RELATED_FINDINGS");
	    int id = 0;
	    for (String finding : as2.getAllTypes()) {
	    	JsonObject findingObject = new JsonObject();
	    	id = id +1;
	    	findingObject.addProperty("id", id);
	    	JsonArray findingsElements = new JsonArray();
	    	for (Annotation findingElement : as2.get(finding)) {
	    		JsonObject findingElementObject = new JsonObject();
	    		Object annotationType = findingElement.getFeatures().get("ANNOTATION_TYPE");
	    		if(annotationType!=null) {
	    			findingElementObject.addProperty("type", annotationType.toString());
	    		}
	    		findingElementObject.addProperty("text", gate.Utils.stringFor(doc, findingElement));
	    		findingElementObject.addProperty("startOffset", findingElement.getStartNode().getOffset());
	    		findingElementObject.addProperty("endOffset", findingElement.getEndNode().getOffset());
	    		JsonArray features = new JsonArray();
		    	for (Object key : findingElement.getFeatures().keySet()) {
		    		JsonObject feature = new JsonObject();
		    		feature.addProperty("name", key.toString());
		    		feature.addProperty("value", findingElement.getFeatures().get(key).toString());
		    		features.add(feature);
				}
		    	findingElementObject.add("features", features);
		    	findingsElements.add(findingElementObject);
		    	findingObject.add("finding_fields", findingsElements);
		    }
	    	findings.add(findingObject);
	    }
	    
	    document.add("findings", findings);
	    
	    java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
	    out.write(gsonBuilder.toJson(document));
	    out.close();
    }
	
}
