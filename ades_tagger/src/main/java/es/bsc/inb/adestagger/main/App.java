package es.bsc.inb.adestagger.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.MentionsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokenBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokenEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.util.CoreMap;
import es.bsc.inb.adestagger.model.EntityInstance;
import es.bsc.inb.adestagger.model.ReferenceValue;
import es.bsc.inb.adestagger.util.AnnotationUtil;
import es.bsc.inb.adestagger.util.StopWords;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * Adverse Drug Events terminology tagger.
 * 
 * @author jcorvi
 *
 */
public class App {
	
	static final Logger log = Logger.getLogger("log");
	
	static Map<Integer, EntityInstance> cdiscDictionary = new HashMap<Integer, EntityInstance>();
	
	static Map<Integer, EntityInstance> etoxAnatomyDictionary = new HashMap<Integer, EntityInstance>();
	
	static Map<Integer, EntityInstance> etoxMOADictionary = new HashMap<Integer, EntityInstance>();
	
	static Map<Integer, EntityInstance> etoxILODictionary = new HashMap<Integer, EntityInstance>();
	
	static Map<Integer, EntityInstance> etoxSENDDictionary = new HashMap<Integer, EntityInstance>();
	
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
    		log.error("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
    	File outputDirectory = new File(outputFilePath);
	    if(!outputDirectory.exists())
	    	outputDirectory.mkdirs();
	    
	    try {
			Gate.init();
		} catch (GateException e) {
			log.error("Wrapper::generatePlainText :: Gate Exception  ", e);
			System.exit(1);
		}
 
	    
	    /*String propertiesParametersPath="";
	    Properties propertiesParameters = PropertiesUtil.loadPropertiesParameters(propertiesParametersPath);
	    */
	    Properties propertiesParameters = null;
	    try {
			generateNERList(propertiesParameters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			processTagger(inputFilePath, outputFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
    }
    
    
    /**
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void processTagger(String inputDirectoryPath, String outputDirectoryPath) throws IOException {
    	Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma,  ner, regexner, entitymentions ");
		props.put("regexner.mapping", "ner_list/cdisc_send_ner.txt, ner_list/ades_extended_terminology.txt, ner_list/etox_anatomy_ner.txt, ner_list/etox_moa_ner.txt, "
				+ "ner_list/etox_in_life_obs_ner.txt, ner_list/etox_send_codelist_ner.txt,ner_list/pkunit_ner.txt,ner_list/treatment_related_triggers.txt");
		props.put("regexner.posmatchtype", "MATCH_ALL_TOKENS");
		
		props.put("rulesFiles", "rules/ades_extended_terminology.rules");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		String[] rulesFiles = props.getProperty("rulesFiles").split(",");
	    // set up an environment with reasonable defaults
	    Env env = TokenSequencePattern.getNewEnv();
	    
	    // build the CoreMapExpressionExtractor
	    CoreMapExpressionExtractor extractor = CoreMapExpressionExtractor.createExtractorFromFiles(env, rulesFiles);
		//BufferedWriter filesProcessedWriter = new BufferedWriter(new FileWriter(outputDirectoryPath + File.separator + "list_files_processed.dat", true));
		
		log.info("Wrapper::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("Wrapper::processTagger :: processing file : " + file.getAbsolutePath());
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + file.getName());
						executeDocument(pipeline, extractor, file, outputGATEFile);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (InvalidOffsetException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} 
				}
			}
		}else {
			System.out.println("No directory :  " + inputDirectoryPath);
		}
		log.info("Wrapper::generatePlainText :: END ");
	}

	/**
		 * Findings of LTKB ChemicalCompunds
		 * 
		 * @param sourceId
		 * @param document_model
		 * @param first_finding_on_document
		 * @param section
		 * @param sentence_text
		 * @return
	 * @throws MalformedURLException 
	 * @throws ResourceInstantiationException 
	 * @throws InvalidOffsetException 
		 * @throws MoreThanOneEntityException
		 */
		private static void executeDocument(StanfordCoreNLP pipeline, CoreMapExpressionExtractor extractor, File inputFile, File outputGATEFile) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
			long startTime = System.currentTimeMillis();
			gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
			String plainText = gateDocument.getContent().getContent(0l, gate.Utils.lengthLong(gateDocument)).toString();
			Annotation document = new Annotation(plainText.toLowerCase());
			//String text = "3.7 ml/g/day pepepep 3.7 mg/m pepep 11 mg  pepepep 400-200 MG/KG, 400 - 200 MG/KG, 400 MG/KG, 01, 02, 03 MG/KG and  Microscopic findings, Altered Consistency, all decreasing amount recovered infinity observed normalized by surface area, severity four out of five, liver cell adenoma, mean ventricular rate by electrocardiogram "; 
			//String text = "therapeutic finding related effect associated pepepe peppepe therapy pepe shows oooo ramification on the rat";
			//Annotation document = new Annotation(text.toLowerCase());
			pipeline.annotate(document);
			long endTime = System.currentTimeMillis();
			log.info(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
	        try {	
	        	//List<CoreLabel> tokens= document.get(TokensAnnotation.class);
			    List<CoreMap> sentences= document.get(SentencesAnnotation.class);
			    for(CoreMap sentence: sentences) {
			    	List<CoreLabel> tokens= sentence.get(TokensAnnotation.class);
			    	//previousSentencences.add(sentence.toString());
			        Integer sentenceBegin = sentence.get(CharacterOffsetBeginAnnotation.class);
			        Integer sentenceEnd = sentence.get(CharacterOffsetEndAnnotation.class);
			        List<CoreMap> entityMentions = sentence.get(MentionsAnnotation.class);
			        for (CoreMap entityMention : entityMentions) {
			        	String term = entityMention.get(TextAnnotation.class).replaceAll("\n", " ");
			        	String label = entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
			        	if(!StopWords.stopWordsEn.contains(term) && !AnnotationUtil.entityMentionsToDelete.contains(label)) {
			        		Integer termBegin = entityMention.get(CharacterOffsetBeginAnnotation.class);
			        		Integer termEnd = entityMention.get(CharacterOffsetEndAnnotation.class);
				        	annotate(gateDocument, sentence, termBegin, termEnd, term, label, "dictionary", tokens, entityMention, null);
			        	}
			        }
			        List<MatchedExpression> matchedExpressionssentence = extractor.extractExpressions(sentence);
			        for (MatchedExpression me : matchedExpressionssentence) {
			        	String term = me.getText().replaceAll("\n", " ");
			        	if(!StopWords.stopWordsEn.contains(term)) {
			        		Integer termBegin = me.getAnnotation().get(CharacterOffsetBeginAnnotation.class);
					       	Integer termEnd = me.getAnnotation().get(CharacterOffsetEndAnnotation.class);
			        		String label = me.getValue().get().toString().toUpperCase();
			        		annotate(gateDocument , sentence, termBegin, termEnd, term, label, "rule", tokens, null, me);
			        	}
			        }
			    }
			    
			    java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
			    out.write(gateDocument.toXml());
			    out.close();
			    
			} catch (IOException e) {
				log.error("TaggerServiceImpl :: tagging2 :: IOException ", e);
			}
	    }

		
		/**
		 * Annotate the information retrieved from the NER and from the Rules.
		 * @param id
		 * @param bw
		 * @param sentence
		 * @param sentenceBegin
		 * @param sentenceEnd
		 * @param meBegin
		 * @param meEnd
		 * @param term
		 * @param label
		 * @throws IOException
		 */
		private static void annotate(Document gateDocument, CoreMap sentence, int meBegin, int meEnd,	String term, String label, 
				String annotationMethod, List<CoreLabel> tokens,CoreMap entityMention, MatchedExpression me) throws IOException {
			String source = "";
			try {
				FeatureMap features = Factory.newFeatureMap(); 
				if(label.contains(AnnotationUtil.SOURCE_ETOX_SUFFIX)) {
					source = AnnotationUtil.SOURCE_ETOX;
					label = label.replaceAll(AnnotationUtil.SOURCE_ETOX_SUFFIX, "");
				}else if(label.contains(AnnotationUtil.SOURCE_CDISC_SUFFIX)){
					String internal_code_str = label.substring(label.lastIndexOf("_")+1);
					Integer internal_code = new Integer(internal_code_str);
					label = label.substring(0, label.lastIndexOf("_"));
					source = AnnotationUtil.SOURCE_CDISC;
					label = label.replaceAll(AnnotationUtil.SOURCE_CDISC_SUFFIX, "");
					findFeatures(cdiscDictionary, features, internal_code);
				}else {
					source =  AnnotationUtil.SOURCE_MANUAL;
				}
				
				features.put("source", source);
	    		features.put("annotationMethod", annotationMethod);
	    		features.put("text", term);
	    		if(label.contains(AnnotationUtil.STUDY_DOMAIN_SUFFIX) || label.contains("SDOMAIN") || label.contains("CLCAT")){
	    			features.put("study_domain", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.STUDY_DOMAIN, features);
	    		}else if(label.endsWith(AnnotationUtil.MANIFESTATION_OF_FINDING)){
	    			features.put("manifestation_of_finding", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.MANIFESTATION_OF_FINDING, features);
	    		}else if(label.endsWith("SEVERITY")){
	    			features.put("severity", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.SEVERITY_FINDING, features);
	    		}else if(label.endsWith(AnnotationUtil.RISK_LEVEL)) {
	    			features.put("risk_level", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.RISK_LEVEL, features);
	    		}else if(label.contains("SEND SEVERITY")) {
	    			features.put("severity", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "SEVERITY", features);
	    		}else if(label.contains("SEND STUDY TYPE") || label.contains("STCAT")) {
	    			features.put("study_type", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "STUDY_TYPE", features);
	    		}else if(label.contains("LBTEST") || label.contains("PKPARMCD_") || label.contains("PKPARM_") || label.endsWith("TEST NAME") || label.endsWith("TEST CODE")) {
	    			features.put("study_domain_testcd", label);
	    			if(label.endsWith("TEST CODE")) {//por aca ingresa cdisc test codes
	    				String TESTCODE =  label.substring(0,label.indexOf("_"));//add keys features
	    				String aux = label.substring(label.indexOf("_")+1);
	    				String TESTCODEVALUE = 	aux.substring(0,aux.indexOf("_"));	
	    				String TESTCODEDESCRIPTION =  aux.substring(aux.indexOf("_")+1);
	    				String label_ = TESTCODE + "=" + TESTCODEVALUE + "("+ TESTCODEDESCRIPTION +")";
	    				//toxicolodyReportWitAnnotations.getAnnotations("STUDY TEST CODE(SRTSTCD) CDISC").add(startOff, endOff,  label_, features);
	    				gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.STUDY_DOMAIN_TESTCD, features);
	    			}else if(label.contains("LBTEST")){
	    				String label_ = "LBTEST"; // LBTEST FROM ETOX
	    				//toxicolodyReportWitAnnotations.getAnnotations("STUDY TEST CODE(LBTEST) ETOX").add(startOff, endOff,  label_, features);
	    				if(term.length()>3) {
	    					gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.STUDY_DOMAIN_TESTCD, features);
	    				}
	    			}else {
	    				//String label_ = "TEST_NAME"; //TEST NAME findings ... 
	    				//toxicolodyReportWitAnnotations.getAnnotations("STUDY TEST NAME(SRTST) CDISC").add(startOff, endOff,  label, features);
	    				gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.STUDY_DOMAIN_TESTCD, features);
	    			}
	    		}else if(label.equals("DURATION_DOSIS")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "DURATION_DOSIS", features);
	    		}else if(label.endsWith("_SEX")|| label.contains("SEXPOP")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.SEX, features);
				}else if(label.contains("ROUTE")) {
					gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.ROUTE_OF_ADMINISTRATION, features);
	    		}else if(label.contains("ANATOMY") || label.contains("ANATOMICAL LOCATION")) {
					gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.ANATOMY, features);
	    		}else if(label.contains("SPECIMEN") || label.startsWith("SPEC_")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.SPECIMEN, features);
	    		}else if(label.contains("SPECIES")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.SPECIES, features);
	    		}else if(label.contains("STRAIN")) {
	    			features.put("type", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "SPECIES", features);
	    		}else if(label.contains("STATICAL_")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.STATISTICAL_SIGNIFICANCE, features);
	    		}else if(label.equals("DOSE")) {
	    			List<CoreLabel> tokens_i = me.getAnnotation().get(TokensAnnotation.class);
	    			for (CoreLabel coreLabel : tokens_i) {
						if(coreLabel.get(NamedEntityTagAnnotation.class).contains("DOSE_UNIT")) {
							features.put("dose_unit", coreLabel.get(TextAnnotation.class));
						}
					}
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.DOSE, features);
	    		}else if(label.contains("PKUNIT")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "DOSE", features);
	    		}else if(label.contains("BODSYS")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.SPECIMEN, features);//BODYSYS SPECIMEN IN TEMPLATE
	    		}else if(label.contains("GROUP")) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), AnnotationUtil.GROUP, features);
	    		}else if(label.contains("FXFINDRS") || label.contains("NONNEO") || label.contains("NEOPLASM") || label.contains("NEOPLASTIC FINDING TYPE") 
	    				|| label.contains("CSTATE")) {
	    			features.put("finding_type", label);
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "FINDING", features);
	    		}else if(label.contains(AnnotationUtil.NO_TREATMENT_RELATED_EFFECT_DETECTED)) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "NO_TREATMENT_RELATED_TRIGGER", features);
	    		}else if(label.contains(AnnotationUtil.TREATMENT_RELATED_EFFECT_DETECTED)) {
	    			gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), "TREATMENT_RELATED_TRIGGER", features);
	    		}else if(label.contains("MOA")) {
	    			gateDocument.getAnnotations("BSC_OTHERS").add(new Long(meBegin), new Long(meEnd), "MOA", features);
	    		}else if(label.contains("PKPARM")) {
	    			gateDocument.getAnnotations("BSC_OTHERS").add(new Long(meBegin), new Long(meEnd), "PKPARM", features);
	    		}else {
	    			if(!(label.equals("DURATION") || label.equals("SET") || label.equals("NATIONALITY"))){
	    				System.out.print("");
	    			}
	    			log.debug("NOT ANNOTATED TERM : " +  term + " , TYPE : " + label);
	    			gateDocument.getAnnotations("BSC_OTHERS").add(new Long(meBegin), new Long(meEnd), label, features);
	    		}
	    			
	    		
	    		
			
			} catch (InvalidOffsetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
			
	/**
	 * Find features from dictionary.
	 * @param label
	 * @param text
	 * @return
	 */
	private static FeatureMap findFeatures(Map<Integer,EntityInstance> cdiscDictionary, FeatureMap features,Integer internal_code) {
		EntityInstance entity = cdiscDictionary.get(internal_code);
		if(entity!=null) {
			for (ReferenceValue reference : entity.getReferenceValues()) {
				features.put(reference.getName(), reference.getValue());
			}
		}
		return features;
	}


	/**
     * Generate NER list for Standford Pipeline 
     * @param propertiesParameters
	 * @throws IOException 
     */
	private static void generateNERList(Properties propertiesParameters) throws IOException {
		//Controlled terminology dictionaries
	    /*String etox_send_dict = propertiesParameters.getProperty("etox_send_dict");
		String etox_anatomy_dict = propertiesParameters.getProperty("etox_anatomy_dict");
		String etox_moa_dict = propertiesParameters.getProperty("etox_moa_dict");
		String etox_in_life_obs_dict = propertiesParameters.getProperty("etox_in_life_obs_dict");
		String cdi_send_terminology_dict = propertiesParameters.getProperty("cdis_send_terminology_dict");
	    */

		String etox_send_dict_path = "dict/etox_send_dict.txt";
		String etox_anatomy_dict_path = "dict/etox_anotomy_dict.txt";
		String etox_moa_dict_path = "dict/etox_moa_dict.txt";
		String etox_in_life_obs_dict_path = "dict/etox_in-life-observations_dict.txt";
		String cdi_send_terminology_dict_path = "dict/cdisc_send_dict.txt";
		
		String cdisc_send_ner = "ner_list/cdisc_send_ner.txt";
		generateNERGazzetterWithPriority(cdi_send_terminology_dict_path, cdiscDictionary, cdisc_send_ner, AnnotationUtil.SOURCE_CDISC_SUFFIX,  "MISC", "20.0");
		
		String pk_unit_ner = "ner_list/pkunit_ner.txt";
		generatePKUNITList(cdi_send_terminology_dict_path, cdiscDictionary, pk_unit_ner, AnnotationUtil.SOURCE_CDISC_SUFFIX,  "MISC", "25.0");
		
		String etox_send_codelist_ner = "ner_list/etox_send_codelist_ner.txt";
		generateNERGazzetterWithPriority(etox_send_dict_path, etoxSENDDictionary, etox_send_codelist_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX, "MISC", "2.0");
		    
		String etox_anatomy_ner = "ner_list/etox_anatomy_ner.txt";
		generateNERGazzetterWithPriority(etox_anatomy_dict_path, etoxAnatomyDictionary,etox_anatomy_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX, "MISC", "2.0");
		    
		String etox_moa_ner = "ner_list/etox_moa_ner.txt";
		generateNERGazzetterWithPriority(etox_moa_dict_path, etoxMOADictionary, etox_moa_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX, "MISC", "2.0");
		    
		String etox_in_life_obs_ner = "ner_list/etox_in_life_obs_ner.txt";
		generateNERGazzetterWithPriority(etox_in_life_obs_dict_path, etoxILODictionary, etox_in_life_obs_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX,  "MISC", "15.0");
	}
	
	/**
	 * PKUNIT List generation
	 * @param dictionaryPath
	 * @param entities
	 * @param outPutNerGazetterPath
	 * @param sourcePrefix
	 * @param tags_to_overwrite
	 * @param priority
	 * @throws IOException
	 */
	private static void generatePKUNITList(String dictionaryPath, Map<Integer,EntityInstance> entities,String outPutNerGazetterPath, String sourcePrefix, String tags_to_overwrite, String priority) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(outPutNerGazetterPath));
		List<String> terms = new ArrayList<String>();
		for (String line : ObjectBank.getLineIterator(dictionaryPath, "utf-8")) {
			String[] data = line.split("\t");
			if(data[2].toUpperCase().contains("PKUNIT")) { 
				terms.add(getScapedKeyWordNER(data[1].toLowerCase()) + "\tDOSE_UNIT\t" +  tags_to_overwrite + "\t" +  priority +"\n");
			}
			
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	private static void generateNERGazzetterWithPriority(String dictionaryPath, Map<Integer,EntityInstance> entities,String outPutNerGazetterPath, String sourcePrefix, String tags_to_overwrite, String priority) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(outPutNerGazetterPath));
		List<String> terms = new ArrayList<String>();
		String[] columnNames=null;
		boolean column=true;
		for (String line : ObjectBank.getLineIterator(dictionaryPath, "utf-8")) {
			if(column) {
				columnNames = line.split("\t");
				column=false;
			}else {
				String[] data = line.split("\t");
				if(data[1].toUpperCase().endsWith("TEST CODE")) { // The codes are more important that name.
					terms.add(getScapedKeyWordNER(data[1].toLowerCase()) + "\t" +  data[2].toUpperCase()+sourcePrefix+"_"+data[0]+ "\t" +  tags_to_overwrite + "\t" +  new Float(priority) * 2 +"\n");
					entities.put(new Integer(data[0]), retrieveEntity(data, columnNames));
				}else {
					terms.add(getScapedKeyWordNER(data[1].toLowerCase()) + "\t" +  data[2].toUpperCase()+sourcePrefix+"_"+data[0]+ "\t" +  tags_to_overwrite + "\t" +  priority +"\n");
					entities.put(new Integer(data[0]), retrieveEntity(data, columnNames));
				}
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	/**
	 * Retrieve tag information form tagger line
	 * @param data
	 * @param columnNames
	 * @return
	 */
	private static EntityInstance retrieveEntity(String[] data, String[] columnNames) {
		try {
			String id = data[0];
			List<ReferenceValue> referenceValues = new ArrayList<ReferenceValue>();
			for (int i = 0; i < columnNames.length; i++) {
				String name = columnNames[i];
				String value = data[i];
				if(value!=null && !value.trim().equals("null")) {
					ReferenceValue key_val = new ReferenceValue(name, value);
					referenceValues.add(key_val);
				}
				//no data for that column, do not forget to put null and complete the information in the tagger.
				
			}
			EntityInstance entityInstance = new EntityInstance(new Integer(id),referenceValues);
			return entityInstance;
		} catch(Exception e) {
			log.error("Error reading custom tag tagged line " + data ,e);
		}
		return null;
	}
	
	private static String getScapedKeyWordNER(String keyword) {
		String example ="submandib + % 1.1 - ( $ * [ ] ) { } lan x # ? | javi ";
		PTBEscapingProcessor esc = new PTBEscapingProcessor();
		String keyword_esc = esc.escapeString(keyword);
		/*String char_b = "/";
		String char_e = "/";
		String word_boundary = "\\b";*/
		keyword_esc = keyword_esc.replaceAll("\\/", "\\\\/").
		replaceAll("\\*", "\\\\*").
		replaceAll("\\?", "\\\\?").
		replaceAll("\\+", "\\\\+").
		//replaceAll("\\$", "\\\\$").
		replaceAll("\\|", "\\\\|");
		return keyword_esc;
	}
	
}
