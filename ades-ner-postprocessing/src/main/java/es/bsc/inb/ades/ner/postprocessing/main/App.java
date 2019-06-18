package es.bsc.inb.ades.ner.postprocessing.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * Standard Postprocessing process.  Using the Standford Core NLP and the Gate TEI format. 
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
        
        Option set = new Option("a", "annotation_set", true, "Annotation set where the annotation will be included");
        set.setRequired(true);
        options.addOption(set);
        
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
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		System.out.println("Please set the inputDirectoryPath ");
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
			System.out.println("App::main :: Gate Exception  ");
			e.printStackTrace();
			System.exit(1);
		}
 
	    if(workdirPath==null) {
	    	workdirPath="";
	    }
	    
		try {
			process(inputFilePath, outputFilePath,workdirPath, annotationSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    /**
	 * Preprocessing NLP Standardization 
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void process(String inputDirectoryPath, String outputDirectoryPath, String workdir,String annotationSet) throws IOException {
    	System.out.println("App::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("App::process :: processing file : " + file.getAbsolutePath());
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + file.getName());
						processDocument(file, outputGATEFile, annotationSet);
					} catch (ResourceInstantiationException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} catch (MalformedURLException e) {
						System.out.println("App::process :: error with document " + file.getAbsolutePath());
						e.printStackTrace();
					} catch (InvalidOffsetException e) {
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
	 * @param pipeline
	 * @param inputFile
	 * @param outputGATEFile
	 * @throws ResourceInstantiationException
	 * @throws MalformedURLException
	 * @throws InvalidOffsetException
	 */
	private static void processDocument(File inputFile, File outputGATEFile, String annotationSet) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
		gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		try {	
			AnnotationSet annSet = gateDocument.getAnnotations(annotationSet); 
			postprocessing(annSet, gateDocument, "FINDING");
			postprocessing(annSet, gateDocument, "SPECIMEN");
			postprocessing(annSet, gateDocument, "STUDY_TESTCD");
			java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
			out.write(gateDocument.toXml());
			out.close();
		} catch (IOException e) {
			System.out.println("App :: executeDocument :: IOException ");
			e.printStackTrace();
		}
	}


	/**
	 * Annotations post-processing.
	 * 
	 * Consensus of FINDING annotations
	 * 
	 * @param annSet
	 */
	private static void postprocessing(AnnotationSet annSet, gate.Document gateDocument, String fieldName) {
		AnnotationSet sentences = annSet.get(fieldName);
		for (Annotation annotation : sentences) {
			Boolean process = true;
			if(annotation.getFeatures().get("stage")==null) {
				AnnotationSet findings = annSet.get(fieldName, annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset());
				if(findings.size()==1) { //Nothing to do, there is no overlaping
					FeatureMap features = gate.Factory.newFeatureMap();
					features.put("text", gate.Utils.stringFor(gateDocument, annotation));
					addFeatures(annotation, features);
					features.put("inst", "BSC");
					annotation.setFeatures(features);
				}else if(findings.size()>1) { // overlapping, select the larger annotated finding. 
					Annotation selected = null;
					FeatureMap features = gate.Factory.newFeatureMap();
					for (Annotation fnd : findings) {
						if(fnd.getFeatures().get("stage")==null) {
							if(selected==null) {
								selected = fnd;
								features.put("text", gate.Utils.stringFor(gateDocument, fnd));
								addFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() > 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { // if there is a larger term then
									features = gate.Factory.newFeatureMap();
									features.put("text", gate.Utils.stringFor(gateDocument, fnd));
									//remove the shorter annotation
									annSet.remove(selected);
									selected = fnd;
									addFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() == 
									selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { //if the terms are equals, add sources and relevant keys to features
									addFeatures(fnd, features);
									//remove the annotation, the information is already in the featureMap
									annSet.remove(fnd);
							} else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() < 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) {
								//System.out.println("MENOR " + fnd); // Do not do nothing the annotation is shorter than the selected
								annSet.remove(fnd);
							}	
						}else {
							//if a annotation was processed then do not 
							process = false;
						}
					}
					//set the joint features to the annotation
					//set basic feature
					if(process) {
						features.put("inst", "BSC");
						features.put("stage", "PP");
						selected.setFeatures(features);
					}else {
						if(selected!=null) {
							annSet.remove(selected);
						}
					}
				}else {
					System.out.println("ERROR ANOTACION NO PRESENTE");
				}
			}
		}
	}

	/**
	 * Add important key features to annotation
	 * @param annotation
	 * @param features
	 */
	private static void addFeatures(Annotation annotation, FeatureMap features) {
		if(annotation.getFeatures().get("source")!=null) {
			if(annotation.getFeatures().get("source").equals("CDISC")) {
				features.put("CDISC_NCI_CODE", annotation.getFeatures().get("EXT_CODE_ID"));
				features.put("CDISC_SEND", annotation.getFeatures().get("OID"));
			}else if(annotation.getFeatures().get("source").equals("ETOX")) {
				features.put("ETOX_ILO_ID", annotation.getFeatures().get("TERM_ID"));
			}else if(annotation.getFeatures().get("source").equals("UMLS")) {
				features.put("UMLS_CUI", annotation.getFeatures().get("CUI"));
				features.put("UMLS_SOURCE", annotation.getFeatures().get("UMLS_SOURCE"));
				features.put("UMLS_SOURCE_CODE", annotation.getFeatures().get("UMLS_SOURCE_CODE"));
				features.put("UMLS_SEM_TYPE_STR", annotation.getFeatures().get("SEM_TYPE_STR"));
				features.put("UMLS_SEM_TYPE", annotation.getFeatures().get("SEM_TYPE"));
			}else if(annotation.getFeatures().get("source").equals("DNORM")) {
				Object mesh = annotation.getFeatures().get("MESH");
				if(mesh!=null) {
					features.put("DNORM_MESH", mesh.toString());
				}
			}else if(annotation.getFeatures().get("source").equals("LINNEAUS")) {
				Object ncbi = annotation.getFeatures().get("ncbi");
				if(ncbi!=null) {
					features.put("LINNAEUS_NCBI", ncbi.toString());
				}
			}else {
				System.out.println();
			}
		}else {
			System.out.println();
		}
	}
}
