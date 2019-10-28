package es.bsc.inb.ades.export.json.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	
	static final String template_value_name = "value";
	
	public static void main(String[] args ){
    	
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
        
        Option annotation_set_relation_extraction = new Option("ar", "annotation_set_relation_extraction", true, "Annotation set where the relation extraction will be included");
        annotation_set_relation_extraction.setRequired(true);
        options.addOption(annotation_set_relation_extraction);
        
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
        String annotationSetRelationExtraction = cmd.getOptionValue("annotation_set_relation_extraction");
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		System.out.println("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
        if (annotationSet==null) {
        	System.out.println("Please set the annotation set where the annotation will be included");
			System.exit(1);
    	}
       
        if (annotationSetRelationExtraction==null) {
        	System.out.println("Please set the annotation relation extraction output set where the relations will be included");
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
			process(inputFilePath, outputFilePath,workdirPath, annotationSet, annotationSetRelationExtraction);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	/**
	 * Process directory and convert XML GATE format to JSON 
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void process(String inputDirectoryPath, String outputDirectoryPath, String workdir, String annotationSet, String annotationSetRelationExtraction) throws IOException {
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
						processDocument(file, outputGATEFile, annotationSet, annotationSetRelationExtraction);
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
	private static void processDocument(File inputFile, File outputGATEFile, String annotationSet, String annotationSetRelationExtraction ) throws ResourceInstantiationException, JsonGenerationException, IOException, InvalidOffsetException{
		gate.Document doc = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		Gson gsonBuilder = new GsonBuilder().create();
		JsonObject document = new JsonObject();
		document.addProperty("name", doc.getName().substring(0, doc.getName().indexOf(".xml")+4));
		String plainText = doc.getContent().getContent(0l, gate.Utils.lengthLong(doc)).toString();
		document.addProperty("text", plainText);
		document.addProperty("textWithAnnotations", new String(Files.readAllBytes(inputFile.getAbsoluteFile().toPath()), StandardCharsets.UTF_8));
		document.addProperty("id", System.currentTimeMillis());
		JsonObject section = new JsonObject();
		section.addProperty("name", "document");
		Set<String> types = Stream.of("FINDING","SEX","SPECIMEN","GROUP","DOSE","DOSE_DURATION","DOSE_QUANTITY","DOSE_FREQUENCY","MANIFESTATION_FINDING","RISK_LEVEL","NO_TREATMENT_RELATED_TRIGGER",
				"TREATMENT_RELATED_TRIGGER","STUDY_DOMAIN","STUDY_DAY_FINDING","STUDY_TESTCD", "ROUTE_OF_ADMINISTRATION","MODE_OF_ACTION","STATISTICAL_SIGNIFICANCE","CYPS").collect(Collectors.toCollection(HashSet::new));
		JsonObject entities = new JsonObject();
		AnnotationSet as = doc.getAnnotations(annotationSet).get(types);
	    for (String type : as.getAllTypes()) {
	    	JsonArray type_array = new JsonArray();
	    	for (Annotation annotation : as.get(type).inDocumentOrder()) {
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
	    AnnotationSet as2 = doc.getAnnotations(annotationSetRelationExtraction);
	    int id = 0;
	    for (String finding : sortFindings(as2.getAllTypes())) {
	    	JsonObject findingObject = new JsonObject();
	    	id = id +1;
	    	findingObject.addProperty("id", id);
	    	Map<String, List<Annotation>> annotations_findings_by_type = new HashMap<String, List<Annotation>>();
	    	for (Annotation findingElement : as2.get(finding).inDocumentOrder()) {
	    		Object annotationType = findingElement.getFeatures().get("ANNOTATION_TYPE");
	    		if(annotationType!=null) {
	    			if(annotations_findings_by_type.get(annotationType)==null) {
	    				annotations_findings_by_type.put(annotationType.toString(), new ArrayList<Annotation>());
	    			}
	    			annotations_findings_by_type.get(annotationType).add(findingElement);
	    		}else {
	    			System.out.print("No tiene annotation type: " + findingElement);
	    		}
	    	}
	    	for (String key : annotations_findings_by_type.keySet()) {
	    		
	    		
	    		List<Annotation> annotations_by_type = annotations_findings_by_type.get(key);
				
	    		Annotation annotation_by_type = annotations_by_type.get(0);
				JsonObject findingElementObject = new JsonObject();
				findingElementObject.addProperty("text", gate.Utils.stringFor(doc, annotation_by_type));
				if(annotation_by_type.getFeatures().get(template_value_name)!=null) {
					findingElementObject.addProperty(template_value_name, annotation_by_type.getFeatures().get(template_value_name).toString());
				}
				
		    	findingElementObject.addProperty("startOffset", annotation_by_type.getStartNode().getOffset());
		    	findingElementObject.addProperty("endOffset", annotation_by_type.getEndNode().getOffset());
		    	JsonArray features = new JsonArray();
			    for (Object key2 : annotation_by_type.getFeatures().keySet()) {
			    	JsonObject feature = new JsonObject();
			    	feature.addProperty("name", key2.toString());
			    	feature.addProperty("value", annotation_by_type.getFeatures().get(key2).toString());
			    	features.add(feature);
				}
			    findingElementObject.add("features", features);
			   
				findingObject.add(key, findingElementObject);
				
			}
	    	findings.add(findingObject);
	    }
	    document.add("findings", findings);
	    java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
	    out.write(gsonBuilder.toJson(document));
	    out.close();
    }
	

	private static List<String> sortFindings(Set<String> allTypes) {
		List<String> mainList = new ArrayList<String>();
		mainList.addAll(allTypes);
		Collections.sort(mainList, new NumberAwareStringComparator());
		return mainList;
	}
}
