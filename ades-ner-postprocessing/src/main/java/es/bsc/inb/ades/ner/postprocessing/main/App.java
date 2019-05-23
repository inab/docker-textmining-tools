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
import gate.Document;
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
	
	public static void main( String[] args ){
    	
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
	 * Preprocessing NLP Standardization 
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
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + file.getName());
						processDocument(file, outputGATEFile);
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
	private static void processDocument(File inputFile, File outputGATEFile) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
		gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
		try {	
			AnnotationSet annSet = gateDocument.getAnnotations("BSC"); 
			findingsPostprocessing(annSet, gateDocument);
			specimenPostprocessing(annSet, gateDocument);
			testCDPostprocessing(annSet, gateDocument);
			java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
			out.write(gateDocument.toXml());
			out.close();
		} catch (IOException e) {
			System.out.println("App :: executeDocument :: IOException ");
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param annSet
	 * @param gateDocument
	 */
	private static void specimenPostprocessing(AnnotationSet annSet, Document gateDocument) {
		AnnotationSet sentences = annSet.get("SPECIMEN");
		for (Annotation annotation : sentences) {
			Boolean process = true;
			if(annotation.getFeatures().get("stage")==null) {
				AnnotationSet findings = annSet.get("SPECIMEN", annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset());
				if(findings.size()==1) { //Nothing to do, there is no overlaping
					FeatureMap features = gate.Factory.newFeatureMap();
					features.put("text", gate.Utils.stringFor(gateDocument, annotation));
					addSpecimenFeatures(annotation, features);
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
								addSpecimenFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() > 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { // if there is a larger term then
									features = gate.Factory.newFeatureMap();
									features.put("text", gate.Utils.stringFor(gateDocument, fnd));
									//remove the shorter annotation
									annSet.remove(selected);
									selected = fnd;
									addSpecimenFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() == 
									selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { //if the terms are equals, add sources and relevant keys to features
									addSpecimenFeatures(fnd, features);
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
					}
				}else {
					System.out.println("ERROR ANOTACION NO PRESENTE ' ");
				}
//					if(str.length()>1000 && str.length()<50) {
//						//delete annotations inside, set as dirty sentence.
//					}else {
//						//
//					}
			}
		}
		
	}

	

	/**
	 * STUDY_TESTCD field pre-processing
	 * @param annSet
	 * @param gateDocument
	 */
	private static void testCDPostprocessing(AnnotationSet annSet, Document gateDocument) {
		AnnotationSet sentences = annSet.get("STUDY_TESTCD");
		for (Annotation annotation : sentences) {
			if(annotation.getFeatures().get("stage")==null) {
				AnnotationSet findings = annSet.get("STUDY_TESTCD", annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset());
				if(findings.size()==1) { //Nothing to do, there is no overlaping
					FeatureMap features = gate.Factory.newFeatureMap();
					features.put("text", gate.Utils.stringFor(gateDocument, annotation));
					addStudyTestFeatures(annotation, features);
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
								addStudyTestFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() > 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { // if there is a larger term then
									features = gate.Factory.newFeatureMap();
									features.put("text", gate.Utils.stringFor(gateDocument, fnd));
									//remove the shorter annotation
									annSet.remove(selected);
									selected = fnd;
									addStudyTestFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() == 
									selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { //if the terms are equals, add sources and relevant keys to features
								addStudyTestFeatures(fnd, features);
									//remove the annotation, the information is already in the featureMap
									annSet.remove(fnd);
							} else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() < 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) {
								//System.out.println("MENOR " + fnd); // Do not do nothing the annotation is shorter than the selected
								annSet.remove(fnd);
							}	
						}
					}
					//set the joint features to the annotation
					//set basic feature
					features.put("inst", "BSC");
					features.put("stage", "PP");
					selected.setFeatures(features);
				}else {
					System.out.println("ERROR ANOTACION NO PRESENTE ' ");
				}
//					if(str.length()>1000 && str.length()<50) {
//						//delete annotations inside, set as dirty sentence.
//					}else {
//						//
//					}
			}
		}
	}

	/**
	 * FINGINS Annotations post-processing.
	 * 
	 * Consensus of FINDING annotations
	 * 
	 * @param annSet
	 */
	private static void findingsPostprocessing(AnnotationSet annSet, gate.Document gateDocument) {
		AnnotationSet sentences = annSet.get("FINDING");
		for (Annotation annotation : sentences) {
			if(annotation.getFeatures().get("stage")==null) {
				AnnotationSet findings = annSet.get("FINDING", annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset());
				if(findings.size()==1) { //Nothing to do, there is no overlaping
					FeatureMap features = gate.Factory.newFeatureMap();
					features.put("text", gate.Utils.stringFor(gateDocument, annotation));
					addFindingFeatures(annotation, features);
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
								addFindingFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() > 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { // if there is a larger term then
									features = gate.Factory.newFeatureMap();
									features.put("text", gate.Utils.stringFor(gateDocument, fnd));
									//remove the shorter annotation
									annSet.remove(selected);
									selected = fnd;
									addFindingFeatures(fnd, features);
							}else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() == 
									selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) { //if the terms are equals, add sources and relevant keys to features
									addFindingFeatures(fnd, features);
									//remove the annotation, the information is already in the featureMap
									annSet.remove(fnd);
							} else if(fnd.getEndNode().getOffset()-fnd.getStartNode().getOffset() < 
								selected.getEndNode().getOffset()-selected.getStartNode().getOffset()) {
								//System.out.println("MENOR " + fnd); // Do not do nothing the annotation is shorter than the selected
								annSet.remove(fnd);
							}	
						}
					}
					//set the joint features to the annotation
					//set basic feature
					features.put("inst", "BSC");
					features.put("stage", "PP");
					selected.setFeatures(features);
				}else {
					System.out.println("ERROR ANOTACION NO PRESENTE ' ");
				}
//					if(str.length()>1000 && str.length()<50) {
//						//delete annotations inside, set as dirty sentence.
//					}else {
//						//
//					}
			}
		}
	}
	
	
	private static void addSpecimenFeatures(Annotation annotation, FeatureMap features) {
		if(annotation.getFeatures().get("source")!=null) {
			if(annotation.getFeatures().get("source").equals("CDISC")) {
				features.put("CDISC_NCI_CODE", annotation.getFeatures().get("EXT_CODE_ID"));
				features.put("CDISC_SEND", annotation.getFeatures().get("OID"));
			}else if(annotation.getFeatures().get("source").equals("ETOX")) {
				features.put("ETOX_ILO_ID", annotation.getFeatures().get("TERM_ID"));
			}else if(annotation.getFeatures().get("source").equals("UMLS")) {
				features.put("UMLS_CUI", annotation.getFeatures().get("CUI"));
				features.put("UMLS_SOURCE", annotation.getFeatures().get("SOURCE"));
				features.put("UMLS_SOURCE_CODE", annotation.getFeatures().get("SOURCE_CODE"));
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
	
	private static void addStudyTestFeatures(Annotation annotation, FeatureMap features) {
		if(annotation.getFeatures().get("source")!=null) {
			if(annotation.getFeatures().get("source").equals("CDISC")) {
				features.put("CDISC_NCI_CODE", annotation.getFeatures().get("EXT_CODE_ID"));
				features.put("CDISC_SEND", annotation.getFeatures().get("OID"));
			}else if(annotation.getFeatures().get("source").equals("ETOX")) {
				features.put("ETOX_ILO_ID", annotation.getFeatures().get("TERM_ID"));
			}else if(annotation.getFeatures().get("source").equals("UMLS")) {
				features.put("UMLS_CUI", annotation.getFeatures().get("CUI"));
				features.put("UMLS_SOURCE", annotation.getFeatures().get("SOURCE"));
				features.put("UMLS_SOURCE_CODE", annotation.getFeatures().get("SOURCE_CODE"));
			}else {
				System.out.println();
			}
		}else {
			System.out.println();
		}
	}
	
	/**
	 * Add finding features given the annotation
	 * @param selected
	 * @param features
	 */
	private static void addFindingFeatures(Annotation annotation, FeatureMap features) {
		if(annotation.getFeatures().get("source")!=null) {
			if(annotation.getFeatures().get("source").equals("CDISC")) {
				features.put("CDISC_NCI_CODE", annotation.getFeatures().get("EXT_CODE_ID"));
				features.put("CDISC_SEND", annotation.getFeatures().get("OID"));
			}else if(annotation.getFeatures().get("source").equals("ETOX")) {
				features.put("ETOX_ILO_ID", annotation.getFeatures().get("TERM_ID"));
			}else if(annotation.getFeatures().get("source").equals("UMLS")) {
				features.put("UMLS_CUI", annotation.getFeatures().get("CUI"));
				features.put("UMLS_SOURCE", annotation.getFeatures().get("SOURCE"));
				features.put("UMLS_SOURCE_CODE", annotation.getFeatures().get("SOURCE_CODE"));
			}else if(annotation.getFeatures().get("source").equals("DNORM")) {
				Object mesh = annotation.getFeatures().get("MESH");
				if(mesh!=null) {
					features.put("DNORM_MESH", mesh.toString());
				}
			}else {
				System.out.println();
			}
		}else {
			System.out.println();
		}
	}

	/**
	 * 
	 * @param findings
	 */
	private static void consensusFinding(AnnotationSet findings) {
		
		
	}
	
//	/**
//	 * Execute process in a document
//	 * @param pipeline
//	 * @param inputFile
//	 * @param outputGATEFile
//	 * @throws ResourceInstantiationException
//	 * @throws MalformedURLException
//	 * @throws InvalidOffsetException
//	 */
//	private static void processDocument(File inputFile, File outputGATEFile) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
//		gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
//		try {	
//			Set<String> annotationsSet = gateDocument.getAnnotationSetNames();
//			for (String annotationSet : annotationsSet) {
//				AnnotationSet annSet = gateDocument.getAnnotations(annotationSet); 
//				AnnotationSet findings = annSet.get("TREATMENT_RELATED_SENTENCE");
//				for (Annotation annotation : findings) {
//					AnnotationSet treatmentRelatedAnno = annSet.getContained(annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset());
//					System.out.println(annotation.getType() + "\t" + gate.Utils.stringFor(gateDocument, annotation));
//					for (Annotation internalAnnotation : treatmentRelatedAnno) {
//						System.out.println(internalAnnotation.getType() + "\t" + gate.Utils.stringFor(gateDocument, internalAnnotation));
//					}
//				}
//			}
//			java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
//			out.write(gateDocument.toXml());
//			out.close();
//		} catch (IOException e) {
//			System.out.println("App :: executeDocument :: IOException ");
//			e.printStackTrace();
//		}
//	}

	
}
