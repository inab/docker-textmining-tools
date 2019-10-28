package es.bsc.inb.ades.relation.extraction.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

import es.bsc.inb.ades.relation.extraction.model.Finding;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.relations.RelationSet;
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
						String fileOutPutName = file.getName();
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + fileOutPutName);
						processDocumentandaWithStudyAsFinding(file, outputGATEFile, annotationSet, annotationSetRelationExtraction);
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
	private static void processDocument(File inputFile, File outputGATEFile, String annotationSet, String annotationSetRelationExtraction) throws ResourceInstantiationException, JsonGenerationException, IOException{
		gate.Document doc = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		AnnotationSet as = doc.getAnnotations(annotationSet);
	    AnnotationSet findings = (AnnotationSet) as.get("FINDING");
	    System.out.println("**********************************************rule_finding_init************************************************************");
		
		RelationSet relSet = as.getRelations();
		List<Annotation> findings_to_process = new ArrayList<>();
		for (Annotation finding : findings.inDocumentOrder()){
			/*if(finding.getFeatures().get("text").toString().startsWith("Pale")) {
				System.out.println("Smaller term do nothing ");
			}*/
			AnnotationSet findings_to_merge = as.get("FINDING", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
			if(!findings_to_merge.isEmpty() && findings_to_merge.size()>1) {
				int i = 1;
				for (Annotation finding_to_merge : findings_to_merge) {
					//if there is another finding present then ...
					if(!finding_to_merge.getId().equals(finding.getId())){
						if(finding.getEndNode().getOffset()-finding.getStartNode().getOffset() < 
								finding_to_merge.getEndNode().getOffset()-finding_to_merge.getStartNode().getOffset()){
							System.out.println("Smaller term do nothing ");
						}else if (finding.getEndNode().getOffset()-finding.getStartNode().getOffset() >= 
								finding_to_merge.getEndNode().getOffset()-finding_to_merge.getStartNode().getOffset()){
							//insert internal term with the annotation.  This could be used in the future.
							finding.getFeatures().put("internal_term_"+i, finding_to_merge);
							i=i+1;
							//plus add send code or relevant internal information to the biggest annotation
							if(finding_to_merge.getFeatures().get("CDISC_SEND_CODE")!=null) {
								finding.getFeatures().put("CDISC_SEND_CODE",finding_to_merge.getFeatures().get("CDISC_SEND_CODE").toString());
								finding.getFeatures().put("CDISC_CODELIST",finding_to_merge.getFeatures().get("CDISC_CODELIST").toString());
							}
							/*else if(finding_to_merge.getFeatures().get("ETOX_SEND_CODE")!=null) {
								finding.getFeatures().put("ETOX_SEND_CODE",finding_to_merge.getFeatures().get("ETOX_SEND_CODE").toString());
							}else if(finding_to_merge.getFeatures().get("ETOX_SEND_DOMAIN_CODE")!=null) {
								finding.getFeatures().put("ETOX_SEND_DOMAIN_CODE",finding_to_merge.getFeatures().get("ETOX_SEND_DOMAIN_CODE").toString());
							}else if(finding_to_merge.getFeatures().get("MANUAL_SEND_CODE")!=null) {
								finding.getFeatures().put("MANUAL_SEND_CODE",finding_to_merge.getFeatures().get("MANUAL_SEND_CODE").toString());
							}else if(finding_to_merge.getFeatures().get("LIMTOX_HEPATOTOXICITY")!=null) {//limtox hepatotoxicity text missing
								finding.getFeatures().put("LIMTOX_HEPATOTOXICITY",finding_to_merge.getFeatures().get("LIMTOX_HEPATOTOXICITY").toString());
							} else {
								System.out.println(finding_to_merge);
							}*/
							//no merge is needed
							findings_to_process.add(finding);
						}
					}
				}
			}else {
				findings_to_process.add(finding);
			}
			
		}
		//for (Annotation finding : findings.inDocumentOrder()){
		
		Integer finding_id = 1;
		
		for (Annotation finding : findings_to_process){
			String str_finding = gate.Utils.stringFor(doc, finding);
		  	AnnotationSet sentences = as.get("Sentence", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
		  	System.out.println("FINDING: " + str_finding);
		  	for (Annotation sentence : sentences){
				String str_sentence = gate.Utils.stringFor(doc, sentence);
				System.out.println("Sentencia:");
				System.out.println(str_sentence);
				finding.getFeatures().put("ANNOTATION_TYPE","FINDING");
				finding.getFeatures().put(template_value_name, getSendCodeFinding(finding, gate.Utils.stringFor(doc, finding)));
				
				doc.getAnnotations(annotationSetRelationExtraction).add(finding.getStartNode(), finding.getEndNode(), "FINDING_"+finding_id, finding.getFeatures());
				
				
				
				AnnotationSet sentenceFields = as.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
				
				sentence.getFeatures().put("ANNOTATION_TYPE", "RELEVANT_TEXT");
				doc.getAnnotations(annotationSetRelationExtraction).add(sentence.getStartNode(), sentence.getEndNode(), "FINDING_"+finding_id, sentence.getFeatures());
				
				
				Annotation ann = isTreatmentRelatedFinding(doc, finding, sentenceFields);
				if(ann!=null) {
					System.out.println("TREATMENT_RELATED: " + gate.Utils.stringFor(doc, ann));
					ann.getFeatures().put("ANNOTATION_TYPE", "IS_TREATMENT_RELATED");
					doc.getAnnotations(annotationSetRelationExtraction).add(ann.getStartNode(), ann.getEndNode(), "FINDING_"+finding_id, ann.getFeatures());
					
				}else {
					FeatureMap features_uncertain = Factory.newFeatureMap();
					features_uncertain.put(template_value_name, "U");
					features_uncertain.put("ANNOTATION_TYPE", "IS_TREATMENT_RELATED");
					doc.getAnnotations(annotationSetRelationExtraction).add(sentence.getStartNode(), sentence.getEndNode(), "FINDING_"+finding_id, features_uncertain);
				}
				
				Annotation manifestation_finding = getClosestAnnotation(doc,  sentenceFields, finding, "MANIFESTATION_FINDING");
				if(manifestation_finding!=null) {
					System.out.println("MANIFESTATION OF FINDING: " + gate.Utils.stringFor(doc, manifestation_finding));
					manifestation_finding.getFeatures().put(template_value_name, getSendCode(manifestation_finding, gate.Utils.stringFor(doc, manifestation_finding)));
					manifestation_finding.getFeatures().put("ANNOTATION_TYPE",manifestation_finding.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(manifestation_finding.getStartNode(), manifestation_finding.getEndNode(), "FINDING_"+finding_id, manifestation_finding.getFeatures());
					
				}
				/*else {
					FeatureMap features_uncertain = Factory.newFeatureMap();
					features_uncertain.put(template_value_name, "P");
					features_uncertain.put("ANNOTATION_TYPE", "MANIFESTATION_FINDING");
					doc.getAnnotations(annotationSet).add(manifestation_finding.getStartNode(), manifestation_finding.getEndNode(), "FINDING_"+finding_id, manifestation_finding.getFeatures());
				}*/
				
				Annotation specimen = getClosestAnnotation(doc,  sentenceFields, finding, "SPECIMEN");
				if(specimen!=null) {
					System.out.println("SPECIMEN: " + gate.Utils.stringFor(doc, specimen));
					specimen.getFeatures().put(template_value_name, getSendCode(specimen, gate.Utils.stringFor(doc, specimen)));
					specimen.getFeatures().put("ANNOTATION_TYPE",specimen.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(specimen.getStartNode(), specimen.getEndNode(), "FINDING_"+finding_id, specimen.getFeatures());
				}
				
				Annotation STUDY_TESTCD = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_TESTCD");
				if(STUDY_TESTCD!=null) {
					System.out.println("STUDY_TESTCD: " + gate.Utils.stringFor(doc, STUDY_TESTCD));
					STUDY_TESTCD.getFeatures().put("ANNOTATION_TYPE",STUDY_TESTCD.getType());
					STUDY_TESTCD.getFeatures().put(template_value_name, getSendCode(STUDY_TESTCD, gate.Utils.stringFor(doc, STUDY_TESTCD)));
					doc.getAnnotations(annotationSetRelationExtraction).add(STUDY_TESTCD.getStartNode(), STUDY_TESTCD.getEndNode(), "FINDING_"+finding_id, STUDY_TESTCD.getFeatures());
				}
				
				Annotation STUDY_DOMAIN = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_DOMAIN");
				if(STUDY_DOMAIN!=null) {
					System.out.println("STUDY_DOMAIN: " + gate.Utils.stringFor(doc, STUDY_DOMAIN));
					STUDY_DOMAIN.getFeatures().put("ANNOTATION_TYPE",STUDY_DOMAIN.getType());
					STUDY_DOMAIN.getFeatures().put(template_value_name, getSendCode(STUDY_DOMAIN, gate.Utils.stringFor(doc, STUDY_DOMAIN)));
					doc.getAnnotations(annotationSetRelationExtraction).add(STUDY_DOMAIN.getStartNode(), STUDY_DOMAIN.getEndNode(), "FINDING_"+finding_id, STUDY_DOMAIN.getFeatures());
				}
				
				Annotation risk_level = getClosestAnnotation(doc,  sentenceFields, finding, "RISK_LEVEL");
				if(risk_level!=null) {
					System.out.println("RISK_LEVEL: " + gate.Utils.stringFor(doc, risk_level));
					//risk_level.getFeatures().put(template_value_name, getSendCode(risk_level, gate.Utils.stringFor(doc, risk_level)));
					risk_level.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, risk_level));
					risk_level.getFeatures().put("ANNOTATION_TYPE",risk_level.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(risk_level.getStartNode(), risk_level.getEndNode(), "FINDING_"+finding_id, risk_level.getFeatures());
				}
				
				Annotation DOSE_QUANTITY = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_QUANTITY");
				if(DOSE_QUANTITY!=null) {
					System.out.println("DOSE_QUANTITY: " + gate.Utils.stringFor(doc, DOSE_QUANTITY));
					DOSE_QUANTITY.getFeatures().put("ANNOTATION_TYPE",DOSE_QUANTITY.getType());
					DOSE_QUANTITY.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_QUANTITY));
					doc.getAnnotations(annotationSetRelationExtraction).add(DOSE_QUANTITY.getStartNode(), DOSE_QUANTITY.getEndNode(), "FINDING_"+finding_id, DOSE_QUANTITY.getFeatures());
				}
				
				Annotation DOSE_FREQUENCY = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_FREQUENCY");
				if(DOSE_FREQUENCY!=null) {
					System.out.println("DOSE_FREQUENCY: " + gate.Utils.stringFor(doc, DOSE_FREQUENCY));
					DOSE_FREQUENCY.getFeatures().put("ANNOTATION_TYPE",DOSE_FREQUENCY.getType());
					DOSE_FREQUENCY.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_FREQUENCY));
					doc.getAnnotations(annotationSetRelationExtraction).add(DOSE_FREQUENCY.getStartNode(), DOSE_FREQUENCY.getEndNode(), "FINDING_"+finding_id, DOSE_FREQUENCY.getFeatures());
				}
				
				Annotation DOSE_DURATION = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_DURATION");
				if(DOSE_DURATION!=null) {
					System.out.println("DOSE_DURATION: " + gate.Utils.stringFor(doc, DOSE_DURATION));
					DOSE_DURATION.getFeatures().put("ANNOTATION_TYPE",DOSE_DURATION.getType());
					DOSE_DURATION.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_DURATION));
					doc.getAnnotations(annotationSetRelationExtraction).add(DOSE_DURATION.getStartNode(), DOSE_DURATION.getEndNode(), "FINDING_"+finding_id, DOSE_DURATION.getFeatures());
				}
				
				Annotation STUDY_DAY_FINDING = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_DAY_FINDING");
				if(STUDY_DAY_FINDING!=null) {
					System.out.println("STUDY_DAY_FINDING: " + gate.Utils.stringFor(doc, STUDY_DAY_FINDING));
					STUDY_DAY_FINDING.getFeatures().put("ANNOTATION_TYPE",STUDY_DAY_FINDING.getType());
					STUDY_DAY_FINDING.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, STUDY_DAY_FINDING));
					doc.getAnnotations(annotationSetRelationExtraction).add(STUDY_DAY_FINDING.getStartNode(), STUDY_DAY_FINDING.getEndNode(), "FINDING_"+finding_id, STUDY_DAY_FINDING.getFeatures());
				}
				
				Annotation ROUTE_OF_ADMINISTRATION = getClosestAnnotation(doc,  sentenceFields, finding, "ROUTE_OF_ADMINISTRATION");
				if(ROUTE_OF_ADMINISTRATION!=null) {
					System.out.println("ROUTE_OF_ADMINISTRATION: " + gate.Utils.stringFor(doc, ROUTE_OF_ADMINISTRATION));
					ROUTE_OF_ADMINISTRATION.getFeatures().put(template_value_name, getSendCode(ROUTE_OF_ADMINISTRATION, gate.Utils.stringFor(doc, ROUTE_OF_ADMINISTRATION)));
					ROUTE_OF_ADMINISTRATION.getFeatures().put("ANNOTATION_TYPE",ROUTE_OF_ADMINISTRATION.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(ROUTE_OF_ADMINISTRATION.getStartNode(), ROUTE_OF_ADMINISTRATION.getEndNode(), "FINDING_"+finding_id, ROUTE_OF_ADMINISTRATION.getFeatures());
				}
				
				Annotation MODE_OF_ACTION = getClosestAnnotation(doc,  sentenceFields, finding, "MODE_OF_ACTION");
				if(MODE_OF_ACTION!=null) {
					System.out.println("MODE_OF_ACTION: " + gate.Utils.stringFor(doc, MODE_OF_ACTION));
					MODE_OF_ACTION.getFeatures().put("ANNOTATION_TYPE",MODE_OF_ACTION.getType());
					MODE_OF_ACTION.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, MODE_OF_ACTION));
					doc.getAnnotations(annotationSetRelationExtraction).add(MODE_OF_ACTION.getStartNode(), MODE_OF_ACTION.getEndNode(), "FINDING_"+finding_id, MODE_OF_ACTION.getFeatures());
				}
				
				Annotation CYPS = getClosestAnnotation(doc,  sentenceFields, finding, "CYPS");
				if(CYPS!=null) {
					System.out.println("CYPS: " + gate.Utils.stringFor(doc, CYPS));
					CYPS.getFeatures().put("ANNOTATION_TYPE",CYPS.getType());
					CYPS.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, CYPS));
					doc.getAnnotations(annotationSetRelationExtraction).add(CYPS.getStartNode(), CYPS.getEndNode(), "FINDING_"+finding_id, CYPS.getFeatures());
				}
				
				Annotation sex = getClosestAnnotation(doc,  sentenceFields, finding, "SEX");
				if(sex!=null) {
					System.out.println("SEX: " + gate.Utils.stringFor(doc, sex));
					String send_code = getSendCode(sex, gate.Utils.stringFor(doc, sex));
					sex.getFeatures().put(template_value_name, send_code);
					sex.getFeatures().put("ANNOTATION_TYPE",sex.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(sex.getStartNode(), sex.getEndNode(), "FINDING_"+finding_id, sex.getFeatures());
				}
				
				Annotation group = getClosestAnnotation(doc,  sentenceFields, finding, "GROUP");
				if(group!=null) {
					System.out.println("GROUP: " + gate.Utils.stringFor(doc, group));
					group.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, group));
					group.getFeatures().put("ANNOTATION_TYPE",group.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(group.getStartNode(), group.getEndNode(), "FINDING_"+finding_id, group.getFeatures());
				}
			}	
		  	finding_id = finding_id + 1;
		  }
		  System.out.println("**********************************************rule_finding_end************************************************************");
		
		java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
	    out.write(doc.toXml());
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
	private static void processDocumentandaWithStudyAsFinding(File inputFile, File outputGATEFile, String annotationSet, String annotationSetRelationExtraction) throws ResourceInstantiationException, JsonGenerationException, IOException{
		gate.Document doc = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		AnnotationSet as = doc.getAnnotations(annotationSet);
		
	    System.out.println("**********************************************rule_finding_init************************************************************");
		
		RelationSet relSet = as.getRelations();
		List<Annotation> findings_to_process = getFindings(as);
		//for (Annotation finding : findings.inDocumentOrder()){
		Integer finding_id = 1;
		for (Annotation finding : findings_to_process){
			String str_finding = gate.Utils.stringFor(doc, finding);
		  	AnnotationSet sentences = as.get("Sentence", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
		  	System.out.println("FINDING: " + str_finding);
		  	for (Annotation sentence : sentences){
				String str_sentence = gate.Utils.stringFor(doc, sentence);
				System.out.println("Sentencia:");
				System.out.println(str_sentence);
				finding.getFeatures().put("ANNOTATION_TYPE",finding.getType());
				finding.getFeatures().put(template_value_name, getSendCodeFinding(finding, gate.Utils.stringFor(doc, finding)));
				doc.getAnnotations(annotationSetRelationExtraction).add(finding.getStartNode(), finding.getEndNode(), "FINDING_"+finding_id, finding.getFeatures());
				AnnotationSet sentenceFields = as.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
				sentence.getFeatures().put("ANNOTATION_TYPE", "RELEVANT_TEXT");
				doc.getAnnotations(annotationSetRelationExtraction).add(sentence.getStartNode(), sentence.getEndNode(), "FINDING_"+finding_id, sentence.getFeatures());
				Annotation ann = isTreatmentRelatedFinding(doc, finding, sentenceFields);
				if(ann!=null) {
					System.out.println("TREATMENT_RELATED: " + gate.Utils.stringFor(doc, ann));
					ann.getFeatures().put("ANNOTATION_TYPE", "IS_TREATMENT_RELATED");
					doc.getAnnotations(annotationSetRelationExtraction).add(ann.getStartNode(), ann.getEndNode(), "FINDING_"+finding_id, ann.getFeatures());
					
				}else {
					FeatureMap features_uncertain = Factory.newFeatureMap();
					features_uncertain.put(template_value_name, "U");
					features_uncertain.put("ANNOTATION_TYPE", "IS_TREATMENT_RELATED");
					doc.getAnnotations(annotationSetRelationExtraction).add(sentence.getStartNode(), sentence.getEndNode(), "FINDING_"+finding_id, features_uncertain);
				}
				
				Annotation manifestation_finding = getClosestAnnotation(doc,  sentenceFields, finding, "MANIFESTATION_FINDING");
				if(manifestation_finding!=null) {
					System.out.println("MANIFESTATION OF FINDING: " + gate.Utils.stringFor(doc, manifestation_finding));
					manifestation_finding.getFeatures().put(template_value_name, getSendCode(manifestation_finding, gate.Utils.stringFor(doc, manifestation_finding)));
					manifestation_finding.getFeatures().put("ANNOTATION_TYPE",manifestation_finding.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(manifestation_finding.getStartNode(), manifestation_finding.getEndNode(), "FINDING_"+finding_id, manifestation_finding.getFeatures());
					
				}
				/*else {
					FeatureMap features_uncertain = Factory.newFeatureMap();
					features_uncertain.put(template_value_name, "P");
					features_uncertain.put("ANNOTATION_TYPE", "MANIFESTATION_FINDING");
					doc.getAnnotations(annotationSet).add(manifestation_finding.getStartNode(), manifestation_finding.getEndNode(), "FINDING_"+finding_id, manifestation_finding.getFeatures());
				}*/
				
				Annotation specimen = getClosestAnnotation(doc,  sentenceFields, finding, "SPECIMEN");
				if(specimen!=null) {
					System.out.println("SPECIMEN: " + gate.Utils.stringFor(doc, specimen));
					specimen.getFeatures().put(template_value_name, getSendCode(specimen, gate.Utils.stringFor(doc, specimen)));
					specimen.getFeatures().put("ANNOTATION_TYPE",specimen.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(specimen.getStartNode(), specimen.getEndNode(), "FINDING_"+finding_id, specimen.getFeatures());
				}
				
//				Annotation STUDY_TESTCD = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_TESTCD");
//				if(STUDY_TESTCD!=null) {
//					System.out.println("STUDY_TESTCD: " + gate.Utils.stringFor(doc, STUDY_TESTCD));
//					STUDY_TESTCD.getFeatures().put("ANNOTATION_TYPE",STUDY_TESTCD.getType());
//					STUDY_TESTCD.getFeatures().put(template_value_name, getSendCode(STUDY_TESTCD, gate.Utils.stringFor(doc, STUDY_TESTCD)));
//					doc.getAnnotations(annotationSetRelationExtraction).add(STUDY_TESTCD.getStartNode(), STUDY_TESTCD.getEndNode(), "FINDING_"+finding_id, STUDY_TESTCD.getFeatures());
//				}
				
				Annotation STUDY_DOMAIN = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_DOMAIN");
				if(STUDY_DOMAIN!=null) {
					System.out.println("STUDY_DOMAIN: " + gate.Utils.stringFor(doc, STUDY_DOMAIN));
					STUDY_DOMAIN.getFeatures().put("ANNOTATION_TYPE",STUDY_DOMAIN.getType());
					STUDY_DOMAIN.getFeatures().put(template_value_name, getSendCode(STUDY_DOMAIN, gate.Utils.stringFor(doc, STUDY_DOMAIN)));
					doc.getAnnotations(annotationSetRelationExtraction).add(STUDY_DOMAIN.getStartNode(), STUDY_DOMAIN.getEndNode(), "FINDING_"+finding_id, STUDY_DOMAIN.getFeatures());
				}
				
				Annotation risk_level = getClosestAnnotation(doc,  sentenceFields, finding, "RISK_LEVEL");
				if(risk_level!=null) {
					System.out.println("RISK_LEVEL: " + gate.Utils.stringFor(doc, risk_level));
					//risk_level.getFeatures().put(template_value_name, getSendCode(risk_level, gate.Utils.stringFor(doc, risk_level)));
					risk_level.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, risk_level));
					risk_level.getFeatures().put("ANNOTATION_TYPE",risk_level.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(risk_level.getStartNode(), risk_level.getEndNode(), "FINDING_"+finding_id, risk_level.getFeatures());
				}
				
				Annotation DOSE_QUANTITY = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_QUANTITY");
				if(DOSE_QUANTITY!=null) {
					System.out.println("DOSE_QUANTITY: " + gate.Utils.stringFor(doc, DOSE_QUANTITY));
					DOSE_QUANTITY.getFeatures().put("ANNOTATION_TYPE",DOSE_QUANTITY.getType());
					DOSE_QUANTITY.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_QUANTITY));
					doc.getAnnotations(annotationSetRelationExtraction).add(DOSE_QUANTITY.getStartNode(), DOSE_QUANTITY.getEndNode(), "FINDING_"+finding_id, DOSE_QUANTITY.getFeatures());
				}
				
				Annotation DOSE_FREQUENCY = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_FREQUENCY");
				if(DOSE_FREQUENCY!=null) {
					System.out.println("DOSE_FREQUENCY: " + gate.Utils.stringFor(doc, DOSE_FREQUENCY));
					DOSE_FREQUENCY.getFeatures().put("ANNOTATION_TYPE",DOSE_FREQUENCY.getType());
					DOSE_FREQUENCY.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_FREQUENCY));
					doc.getAnnotations(annotationSetRelationExtraction).add(DOSE_FREQUENCY.getStartNode(), DOSE_FREQUENCY.getEndNode(), "FINDING_"+finding_id, DOSE_FREQUENCY.getFeatures());
				}
				
				Annotation DOSE_DURATION = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_DURATION");
				if(DOSE_DURATION!=null) {
					System.out.println("DOSE_DURATION: " + gate.Utils.stringFor(doc, DOSE_DURATION));
					DOSE_DURATION.getFeatures().put("ANNOTATION_TYPE",DOSE_DURATION.getType());
					DOSE_DURATION.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_DURATION));
					doc.getAnnotations(annotationSetRelationExtraction).add(DOSE_DURATION.getStartNode(), DOSE_DURATION.getEndNode(), "FINDING_"+finding_id, DOSE_DURATION.getFeatures());
				}
				
				Annotation STUDY_DAY_FINDING = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_DAY_FINDING");
				if(STUDY_DAY_FINDING!=null) {
					System.out.println("STUDY_DAY_FINDING: " + gate.Utils.stringFor(doc, STUDY_DAY_FINDING));
					STUDY_DAY_FINDING.getFeatures().put("ANNOTATION_TYPE",STUDY_DAY_FINDING.getType());
					STUDY_DAY_FINDING.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, STUDY_DAY_FINDING));
					doc.getAnnotations(annotationSetRelationExtraction).add(STUDY_DAY_FINDING.getStartNode(), STUDY_DAY_FINDING.getEndNode(), "FINDING_"+finding_id, STUDY_DAY_FINDING.getFeatures());
				}
				
				Annotation ROUTE_OF_ADMINISTRATION = getClosestAnnotation(doc,  sentenceFields, finding, "ROUTE_OF_ADMINISTRATION");
				if(ROUTE_OF_ADMINISTRATION!=null) {
					System.out.println("ROUTE_OF_ADMINISTRATION: " + gate.Utils.stringFor(doc, ROUTE_OF_ADMINISTRATION));
					ROUTE_OF_ADMINISTRATION.getFeatures().put(template_value_name, getSendCode(ROUTE_OF_ADMINISTRATION, gate.Utils.stringFor(doc, ROUTE_OF_ADMINISTRATION)));
					ROUTE_OF_ADMINISTRATION.getFeatures().put("ANNOTATION_TYPE",ROUTE_OF_ADMINISTRATION.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(ROUTE_OF_ADMINISTRATION.getStartNode(), ROUTE_OF_ADMINISTRATION.getEndNode(), "FINDING_"+finding_id, ROUTE_OF_ADMINISTRATION.getFeatures());
				}
				
				Annotation MODE_OF_ACTION = getClosestAnnotation(doc,  sentenceFields, finding, "MODE_OF_ACTION");
				if(MODE_OF_ACTION!=null) {
					System.out.println("MODE_OF_ACTION: " + gate.Utils.stringFor(doc, MODE_OF_ACTION));
					MODE_OF_ACTION.getFeatures().put("ANNOTATION_TYPE",MODE_OF_ACTION.getType());
					MODE_OF_ACTION.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, MODE_OF_ACTION));
					doc.getAnnotations(annotationSetRelationExtraction).add(MODE_OF_ACTION.getStartNode(), MODE_OF_ACTION.getEndNode(), "FINDING_"+finding_id, MODE_OF_ACTION.getFeatures());
				}
				
				Annotation CYPS = getClosestAnnotation(doc,  sentenceFields, finding, "CYPS");
				if(CYPS!=null) {
					System.out.println("CYPS: " + gate.Utils.stringFor(doc, CYPS));
					CYPS.getFeatures().put("ANNOTATION_TYPE",CYPS.getType());
					CYPS.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, CYPS));
					doc.getAnnotations(annotationSetRelationExtraction).add(CYPS.getStartNode(), CYPS.getEndNode(), "FINDING_"+finding_id, CYPS.getFeatures());
				}
				
				Annotation sex = getClosestAnnotation(doc,  sentenceFields, finding, "SEX");
				if(sex!=null) {
					System.out.println("SEX: " + gate.Utils.stringFor(doc, sex));
					String send_code = getSendCode(sex, gate.Utils.stringFor(doc, sex));
					sex.getFeatures().put(template_value_name, send_code);
					sex.getFeatures().put("ANNOTATION_TYPE",sex.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(sex.getStartNode(), sex.getEndNode(), "FINDING_"+finding_id, sex.getFeatures());
				}
				
				Annotation group = getClosestAnnotation(doc,  sentenceFields, finding, "GROUP");
				if(group!=null) {
					System.out.println("GROUP: " + gate.Utils.stringFor(doc, group));
					group.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, group));
					group.getFeatures().put("ANNOTATION_TYPE",group.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(group.getStartNode(), group.getEndNode(), "FINDING_"+finding_id, group.getFeatures());
				}
			}	
		  	finding_id = finding_id + 1;
		  }
		  System.out.println("**********************************************rule_finding_end************************************************************");
		
		java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
	    out.write(doc.toXml());
		out.close();
    }
	
	/**
	 * Get Findings.  Could be a FINDING or a STUDY_TESTCD
	 * @param as
	 * @return
	 */
	private static List<Annotation> getFindings(AnnotationSet as) {
		List<Annotation> findings_to_process = new ArrayList<>();
		Set<String> types = Stream.of("FINDING","STUDY_TESTCD").collect(Collectors.toCollection(HashSet::new));
	    AnnotationSet findings = (AnnotationSet) as.get(types);
		for (Annotation finding : findings.inDocumentOrder()){
			if(finding.getType().equals("FINDING")) {
				AnnotationSet findings_to_merge = as.get("FINDING", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
				if(!findings_to_merge.isEmpty() && findings_to_merge.size()>1) {
					int i = 1;
					for (Annotation finding_to_merge : findings_to_merge) {
						//if there is another finding present then ...
						if(!finding_to_merge.getId().equals(finding.getId())){
							if(finding.getEndNode().getOffset()-finding.getStartNode().getOffset() < 
									finding_to_merge.getEndNode().getOffset()-finding_to_merge.getStartNode().getOffset()){
								System.out.println("Smaller term do nothing ");
							}else if (finding.getEndNode().getOffset()-finding.getStartNode().getOffset() >= 
									finding_to_merge.getEndNode().getOffset()-finding_to_merge.getStartNode().getOffset()){
								//insert internal term with the annotation.  This could be used in the future.
								finding.getFeatures().put("internal_term_"+i, finding_to_merge);
								i=i+1;
								//plus add send code or relevant internal information to the biggest annotation
								if(finding_to_merge.getFeatures().get("CDISC_SEND_CODE")!=null) {
									finding.getFeatures().put("CDISC_SEND_CODE",finding_to_merge.getFeatures().get("CDISC_SEND_CODE").toString());
									finding.getFeatures().put("CDISC_CODELIST",finding_to_merge.getFeatures().get("CDISC_CODELIST").toString());
								}
								/*else if(finding_to_merge.getFeatures().get("ETOX_SEND_CODE")!=null) {
									finding.getFeatures().put("ETOX_SEND_CODE",finding_to_merge.getFeatures().get("ETOX_SEND_CODE").toString());
								}else if(finding_to_merge.getFeatures().get("ETOX_SEND_DOMAIN_CODE")!=null) {
									finding.getFeatures().put("ETOX_SEND_DOMAIN_CODE",finding_to_merge.getFeatures().get("ETOX_SEND_DOMAIN_CODE").toString());
								}else if(finding_to_merge.getFeatures().get("MANUAL_SEND_CODE")!=null) {
									finding.getFeatures().put("MANUAL_SEND_CODE",finding_to_merge.getFeatures().get("MANUAL_SEND_CODE").toString());
								}else if(finding_to_merge.getFeatures().get("LIMTOX_HEPATOTOXICITY")!=null) {//limtox hepatotoxicity text missing
									finding.getFeatures().put("LIMTOX_HEPATOTOXICITY",finding_to_merge.getFeatures().get("LIMTOX_HEPATOTOXICITY").toString());
								} else {
									System.out.println(finding_to_merge);
								}*/
								//no merge is needed
								findings_to_process.add(finding);
							}
						}
					}
				}else {
					findings_to_process.add(finding);
				}
			}else {//study test
				/*AnnotationSet sentences = as.get("Sentence", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
				Annotation manifestation_finding = getClosestAnnotation(doc,  sentenceFields, finding, "MANIFESTATION_FINDING");
				if(manifestation_finding!=null) {
					System.out.println("MANIFESTATION OF FINDING: " + gate.Utils.stringFor(doc, manifestation_finding));
					manifestation_finding.getFeatures().put(template_value_name, getSendCode(manifestation_finding, gate.Utils.stringFor(doc, manifestation_finding)));
					manifestation_finding.getFeatures().put("ANNOTATION_TYPE",manifestation_finding.getType());
					doc.getAnnotations(annotationSetRelationExtraction).add(manifestation_finding.getStartNode(), manifestation_finding.getEndNode(), "FINDING_"+finding_id, manifestation_finding.getFeatures());
					
				}*/
				findings_to_process.add(finding);
			}
		}
		return findings_to_process;
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
	private static void processDocument2(File inputFile, File outputGATEFile, String annotationSet, String annotationSetRelationExtraction) throws ResourceInstantiationException, JsonGenerationException, IOException{
		gate.Document doc = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		AnnotationSet as = doc.getAnnotations(annotationSet);
		Set<String> types = Stream.of("FINDING","STUDY_TESTCD").collect(Collectors.toCollection(HashSet::new));
	    AnnotationSet possible_findings = (AnnotationSet) as.get(types);
	    System.out.println("**********************************************rule_finding_init************************************************************");
		
		RelationSet relSet = as.getRelations();
		List<Annotation> findings_to_process = new ArrayList<>();
		for (Annotation finding : possible_findings.inDocumentOrder()){
			//Definir en primera instancia cuando hay un finding.
			if(finding.getType().equals("FINDING")) {
				AnnotationSet findings_to_merge = as.get("FINDING", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
				if(!findings_to_merge.isEmpty() && findings_to_merge.size()>1) {
					int i = 1;
					for (Annotation finding_to_merge : findings_to_merge) {
						//if there is another finding present then ...
						if(!finding_to_merge.getId().equals(finding.getId())){
							if(finding.getEndNode().getOffset()-finding.getStartNode().getOffset() < 
									finding_to_merge.getEndNode().getOffset()-finding_to_merge.getStartNode().getOffset()){
								System.out.println("Smaller term do nothing ");
							}else if (finding.getEndNode().getOffset()-finding.getStartNode().getOffset() >= 
									finding_to_merge.getEndNode().getOffset()-finding_to_merge.getStartNode().getOffset()){
								//insert internal term with the annotation.  This could be used in the future.
								finding.getFeatures().put("internal_term_"+i, finding_to_merge);
								i=i+1;
								//plus add send code or relevant internal information to the biggest annotation
								if(finding_to_merge.getFeatures().get("CDISC_SEND_CODE")!=null) {
									finding.getFeatures().put("CDISC_SEND_CODE",finding_to_merge.getFeatures().get("CDISC_SEND_CODE").toString());
									finding.getFeatures().put("CDISC_CODELIST",finding_to_merge.getFeatures().get("CDISC_CODELIST").toString());
								}
								/*else if(finding_to_merge.getFeatures().get("ETOX_SEND_CODE")!=null) {
									finding.getFeatures().put("ETOX_SEND_CODE",finding_to_merge.getFeatures().get("ETOX_SEND_CODE").toString());
								}else if(finding_to_merge.getFeatures().get("ETOX_SEND_DOMAIN_CODE")!=null) {
									finding.getFeatures().put("ETOX_SEND_DOMAIN_CODE",finding_to_merge.getFeatures().get("ETOX_SEND_DOMAIN_CODE").toString());
								}else if(finding_to_merge.getFeatures().get("MANUAL_SEND_CODE")!=null) {
									finding.getFeatures().put("MANUAL_SEND_CODE",finding_to_merge.getFeatures().get("MANUAL_SEND_CODE").toString());
								}else if(finding_to_merge.getFeatures().get("LIMTOX_HEPATOTOXICITY")!=null) {//limtox hepatotoxicity text missing
									finding.getFeatures().put("LIMTOX_HEPATOTOXICITY",finding_to_merge.getFeatures().get("LIMTOX_HEPATOTOXICITY").toString());
								} else {
									System.out.println(finding_to_merge);
								}*/
								//no merge is needed
								findings_to_process.add(finding);
							}
						}
					}
				}else {
					findings_to_process.add(finding);
				}
			}else if(finding.getType().equals("STUDY_TESTCD")) {
				findings_to_process.add(finding);
			}

		}
		
		//for (Annotation finding : findings.inDocumentOrder()){
		
		Integer finding_id = 1;
		
		for (Annotation finding : findings_to_process){
			
			Finding finding_template = new Finding(finding_id);
			String str_finding = gate.Utils.stringFor(doc, finding);
		  	AnnotationSet sentences = as.get("Sentence", finding.getStartNode().getOffset(), finding.getEndNode().getOffset());
		  	System.out.println("FINDING: " + str_finding);
		  	for (Annotation sentence : sentences){
				String str_sentence = gate.Utils.stringFor(doc, sentence);
				System.out.println("Sentencia:");
				System.out.println(str_sentence);
				finding.getFeatures().put("ANNOTATION_TYPE",finding.getType());
				finding.getFeatures().put(template_value_name, getSendCodeFinding(finding, gate.Utils.stringFor(doc, finding)));
				finding_template.addAnnotation(finding);
				
				AnnotationSet sentenceFields = as.get(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
				sentence.getFeatures().put("ANNOTATION_TYPE", "RELEVANT_TEXT");
				finding_template.addAnnotation(sentence);
				
				Annotation ann = isTreatmentRelatedFinding(doc, finding, sentenceFields);
				if(ann!=null) {
					System.out.println("TREATMENT_RELATED: " + gate.Utils.stringFor(doc, ann));
					ann.getFeatures().put("ANNOTATION_TYPE", "IS_TREATMENT_RELATED");
					finding_template.addAnnotation(ann);
				}else {
					FeatureMap features_uncertain = Factory.newFeatureMap();
					features_uncertain.put(template_value_name, "U");
					features_uncertain.put("ANNOTATION_TYPE", "IS_TREATMENT_RELATED");
					//finding_template.addAnnotation(sentence);
				}
				
				Annotation manifestation_finding = getClosestAnnotation(doc,  sentenceFields, finding, "MANIFESTATION_FINDING");
				if(manifestation_finding!=null) {
					System.out.println("MANIFESTATION OF FINDING: " + gate.Utils.stringFor(doc, manifestation_finding));
					manifestation_finding.getFeatures().put(template_value_name, getSendCode(manifestation_finding, gate.Utils.stringFor(doc, manifestation_finding)));
					manifestation_finding.getFeatures().put("ANNOTATION_TYPE",manifestation_finding.getType());
					finding_template.addAnnotation(manifestation_finding);
				}
				/*else {
					FeatureMap features_uncertain = Factory.newFeatureMap();
					features_uncertain.put(template_value_name, "P");
					features_uncertain.put("ANNOTATION_TYPE", "MANIFESTATION_FINDING");
					doc.getAnnotations(annotationSet).add(manifestation_finding.getStartNode(), manifestation_finding.getEndNode(), "FINDING_"+finding_id, manifestation_finding.getFeatures());
				}*/
				
				Annotation specimen = getClosestAnnotation(doc,  sentenceFields, finding, "SPECIMEN");
				if(specimen!=null) {
					System.out.println("SPECIMEN: " + gate.Utils.stringFor(doc, specimen));
					specimen.getFeatures().put(template_value_name, getSendCode(specimen, gate.Utils.stringFor(doc, specimen)));
					specimen.getFeatures().put("ANNOTATION_TYPE",specimen.getType());
					finding_template.addAnnotation(specimen);
				}
				
				Annotation STUDY_TESTCD = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_TESTCD");
				if(STUDY_TESTCD!=null) {
					System.out.println("STUDY_TESTCD: " + gate.Utils.stringFor(doc, STUDY_TESTCD));
					STUDY_TESTCD.getFeatures().put("ANNOTATION_TYPE",STUDY_TESTCD.getType());
					STUDY_TESTCD.getFeatures().put(template_value_name, getSendCode(STUDY_TESTCD, gate.Utils.stringFor(doc, STUDY_TESTCD)));
					finding_template.addAnnotation(STUDY_TESTCD);
				}
				
				Annotation STUDY_DOMAIN = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_DOMAIN");
				if(STUDY_DOMAIN!=null) {
					System.out.println("STUDY_DOMAIN: " + gate.Utils.stringFor(doc, STUDY_DOMAIN));
					STUDY_DOMAIN.getFeatures().put("ANNOTATION_TYPE",STUDY_DOMAIN.getType());
					STUDY_DOMAIN.getFeatures().put(template_value_name, getSendCode(STUDY_DOMAIN, gate.Utils.stringFor(doc, STUDY_DOMAIN)));
					finding_template.addAnnotation(STUDY_DOMAIN);
				}
				
				Annotation risk_level = getClosestAnnotation(doc,  sentenceFields, finding, "RISK_LEVEL");
				if(risk_level!=null) {
					System.out.println("RISK_LEVEL: " + gate.Utils.stringFor(doc, risk_level));
					//risk_level.getFeatures().put(template_value_name, getSendCode(risk_level, gate.Utils.stringFor(doc, risk_level)));
					risk_level.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, risk_level));
					risk_level.getFeatures().put("ANNOTATION_TYPE",risk_level.getType());
					finding_template.addAnnotation(risk_level);
				}
				
				Annotation DOSE_QUANTITY = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_QUANTITY");
				if(DOSE_QUANTITY!=null) {
					System.out.println("DOSE_QUANTITY: " + gate.Utils.stringFor(doc, DOSE_QUANTITY));
					DOSE_QUANTITY.getFeatures().put("ANNOTATION_TYPE",DOSE_QUANTITY.getType());
					DOSE_QUANTITY.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_QUANTITY));
					finding_template.addAnnotation(DOSE_QUANTITY);
				}
				
				Annotation DOSE_FREQUENCY = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_FREQUENCY");
				if(DOSE_FREQUENCY!=null) {
					System.out.println("DOSE_FREQUENCY: " + gate.Utils.stringFor(doc, DOSE_FREQUENCY));
					DOSE_FREQUENCY.getFeatures().put("ANNOTATION_TYPE",DOSE_FREQUENCY.getType());
					DOSE_FREQUENCY.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_FREQUENCY));
					finding_template.addAnnotation(DOSE_FREQUENCY);
				}
				
				Annotation DOSE_DURATION = getClosestAnnotation(doc,  sentenceFields, finding, "DOSE_DURATION");
				if(DOSE_DURATION!=null) {
					System.out.println("DOSE_DURATION: " + gate.Utils.stringFor(doc, DOSE_DURATION));
					DOSE_DURATION.getFeatures().put("ANNOTATION_TYPE",DOSE_DURATION.getType());
					DOSE_DURATION.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, DOSE_DURATION));
					finding_template.addAnnotation(DOSE_DURATION);
				}
				
				Annotation STUDY_DAY_FINDING = getClosestAnnotation(doc,  sentenceFields, finding, "STUDY_DAY_FINDING");
				if(STUDY_DAY_FINDING!=null) {
					System.out.println("STUDY_DAY_FINDING: " + gate.Utils.stringFor(doc, STUDY_DAY_FINDING));
					STUDY_DAY_FINDING.getFeatures().put("ANNOTATION_TYPE",STUDY_DAY_FINDING.getType());
					STUDY_DAY_FINDING.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, STUDY_DAY_FINDING));
					finding_template.addAnnotation(STUDY_DAY_FINDING);
				}
				
				Annotation ROUTE_OF_ADMINISTRATION = getClosestAnnotation(doc,  sentenceFields, finding, "ROUTE_OF_ADMINISTRATION");
				if(ROUTE_OF_ADMINISTRATION!=null) {
					System.out.println("ROUTE_OF_ADMINISTRATION: " + gate.Utils.stringFor(doc, ROUTE_OF_ADMINISTRATION));
					ROUTE_OF_ADMINISTRATION.getFeatures().put(template_value_name, getSendCode(ROUTE_OF_ADMINISTRATION, gate.Utils.stringFor(doc, ROUTE_OF_ADMINISTRATION)));
					ROUTE_OF_ADMINISTRATION.getFeatures().put("ANNOTATION_TYPE",ROUTE_OF_ADMINISTRATION.getType());
					finding_template.addAnnotation(ROUTE_OF_ADMINISTRATION);
				}
				
				Annotation MODE_OF_ACTION = getClosestAnnotation(doc,  sentenceFields, finding, "MODE_OF_ACTION");
				if(MODE_OF_ACTION!=null) {
					System.out.println("MODE_OF_ACTION: " + gate.Utils.stringFor(doc, MODE_OF_ACTION));
					MODE_OF_ACTION.getFeatures().put("ANNOTATION_TYPE",MODE_OF_ACTION.getType());
					MODE_OF_ACTION.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, MODE_OF_ACTION));
					finding_template.addAnnotation(MODE_OF_ACTION);
				}
				
				Annotation CYPS = getClosestAnnotation(doc,  sentenceFields, finding, "CYPS");
				if(CYPS!=null) {
					System.out.println("CYPS: " + gate.Utils.stringFor(doc, CYPS));
					CYPS.getFeatures().put("ANNOTATION_TYPE",CYPS.getType());
					CYPS.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, CYPS));
					finding_template.addAnnotation(CYPS);
				}
				
				AnnotationSet annotationsSet_sex = sentenceFields.get("SEX");
				if(!annotationsSet_sex.isEmpty()) {
					for (Annotation annotation : annotationsSet_sex) {
						System.out.println("SEX: " + gate.Utils.stringFor(doc, annotation));
						annotation.getFeatures().put("ANNOTATION_TYPE",annotation.getType());
						String send_code = getSendCode(annotation, gate.Utils.stringFor(doc, annotation));
						if(send_code.equals("BOTH")) {
							
							Finding sex_f_finding = finding_template.clone();
							finding_id = finding_id + 1;
							sex_f_finding.setId(finding_id);
							
							annotation.getFeatures().put(template_value_name, "F");
							finding_template.addAnnotation(annotation);
							
							
						}
					}
				}
				
				Annotation group = getClosestAnnotation(doc,  sentenceFields, finding, "GROUP");
				if(group!=null) {
					System.out.println("GROUP: " + gate.Utils.stringFor(doc, group));
					group.getFeatures().put(template_value_name, gate.Utils.stringFor(doc, group));
					group.getFeatures().put("ANNOTATION_TYPE",group.getType());
					
				}
			}	
		  	finding_id = finding_id + 1;
		  }
		  System.out.println("**********************************************rule_finding_end************************************************************");
		
		java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
	    out.write(doc.toXml());
		out.close();
    }
	
	
	private static String getSendCode(Annotation annotation, String text) {
		//String send_code = "UNCERTAIN("+(text)+")";
		String send_code = "UNCERTAIN";
		if(annotation.getFeatures().get("MANUAL_SEND_CODE")!=null) {
			send_code = annotation.getFeatures().get("MANUAL_SEND_CODE").toString();
		}else if(annotation.getFeatures().get("MANUAL_SEND_DOMAIN_CODE")!=null) {
			send_code = annotation.getFeatures().get("MANUAL_SEND_DOMAIN_CODE").toString();
		}else if(annotation.getFeatures().get("CDISC_SEND_CODE")!=null) {
			send_code = annotation.getFeatures().get("CDISC_SEND_CODE").toString();
		}else if(annotation.getFeatures().get("ETOX_SEND_CODE")!=null) {
			send_code = annotation.getFeatures().get("ETOX_SEND_CODE").toString();
		}else if(annotation.getFeatures().get("ETOX_SEND_DOMAIN_CODE")!=null) {
			send_code = annotation.getFeatures().get("ETOX_SEND_DOMAIN_CODE").toString();
		}
		return send_code+"("+text+")";
	}
	
	private static String getSendCodeFinding(Annotation annotation, String text) {
		//String send_code = "UNCERTAIN("+(text)+")";
		String send_code = "UNCERTAIN";
		if(annotation.getFeatures().get("MANUAL_SEND_CODE")!=null) {
			send_code = annotation.getFeatures().get("MANUAL_SEND_CODE").toString();
		}else if(annotation.getFeatures().get("CDISC_SEND_CODE")!=null) {
			send_code = annotation.getFeatures().get("CDISC_SEND_CODE").toString();
		}else if(annotation.getFeatures().get("ETOX_SEND_CODE")!=null) {
			send_code = annotation.getFeatures().get("ETOX_SEND_CODE").toString();
		}
		return send_code+"("+text+")";
	}
	
	
	/**
	 * 
	 * @param doc
	 * @param finding
	 * @param fields
	 */
	private static Annotation isTreatmentRelatedFinding(gate.Document doc, Annotation finding, AnnotationSet fields) {
		Annotation treatment_related_annotation = getClosestAnnotation(doc, fields, finding, "TREATMENT_RELATED_TRIGGER");
		Annotation no_treatment_related_annotation = getClosestAnnotation(doc, fields, finding, "NO_TREATMENT_RELATED_TRIGGER");
		if (no_treatment_related_annotation!=null) {
			System.out.println("IS_TREATMENT_RELATED: " + "N (NO)");
			no_treatment_related_annotation.getFeatures().put(template_value_name, "N");
			return no_treatment_related_annotation;
		}else if(treatment_related_annotation!=null) {
			System.out.println("IS_TREATMENT_RELATED: " + "Y (YES)");
			treatment_related_annotation.getFeatures().put(template_value_name, "Y");
			return treatment_related_annotation;
		}else {
			System.out.println("IS_TREATMENT_RELATED: " + "U (UNCERTAIN)");
		}
		return null;
	}

	private static Annotation getClosestAnnotation(gate.Document doc, AnnotationSet fields, Annotation finding, String type) {
		//AnnotationSet treatment_related_triggers = fields.get(type, sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset());
		AnnotationSet annotations_type = fields.get(type);
		Annotation closest = null;
		Integer token_between_closest = 10000;
		for (Annotation annotation_type : annotations_type) {
			//System.out.println(type + ": " + gate.Utils.stringFor(doc, annotation_type));
			//si el finding esta despues del trigger
			AnnotationSet token_between = null;
			if (finding.getStartNode().getOffset() > annotation_type.getEndNode().getOffset()) {
				token_between =  fields.get("Token", annotation_type.getEndNode().getOffset(), finding.getStartNode().getOffset());
			}else if (annotation_type.getStartNode().getOffset() > finding.getEndNode().getOffset()) {
				token_between =  fields.get("Token", finding.getEndNode().getOffset(), annotation_type.getStartNode().getOffset());
			}else {
				closest = annotation_type;
				token_between_closest = 0;
			}
			if(token_between!=null && token_between.size()<token_between_closest) {
				//token_between.inDocumentOrder();
				closest = annotation_type;
				token_between_closest = token_between.size();
			}
		}
		return closest;
	}

}
