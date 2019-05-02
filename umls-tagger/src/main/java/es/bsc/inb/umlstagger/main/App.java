package es.bsc.inb.umlstagger.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringJoiner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.MentionsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.util.CoreMap;
import es.bsc.inb.umlstagger.model.EntityInstance;
import es.bsc.inb.umlstagger.model.ReferenceValue;
import es.bsc.inb.umlstagger.util.AnnotationUtil;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * UMLS Tagger.
 * Given an UMSL Installation these tools execute a NER using the UMLS Terminology. 
 * 
 * @author jcorvi
 *
 */
public class App {
	
	static final Logger log = Logger.getLogger("log");
	
	static Map<String,String> semanticTypesMap = new HashMap<String,String>();
	
	static Map<String, EntityInstance> umlsDictionary = new HashMap<String, EntityInstance>();
	
	static List<String> sourceList = new ArrayList<String>();
	
    public static void main( String[] args ){
    	
    	Options options = new Options();
    	
        Option input = new Option("i", "input", true, "input directory path");
        input.setRequired(true);
        options.addOption(input);
        
        Option output = new Option("o", "output", true, "output directory path");
        output.setRequired(true);
        options.addOption(output);
        
        Option inputUMLSDirectory = new Option("u", "input_umls_directory", true, "input directory where the RRF files are located,  usually are in ... META folder");
        inputUMLSDirectory.setRequired(true);
        options.addOption(inputUMLSDirectory);
        
        Option configuration_file = new Option("c", "configuration_file", true, "it contains the semantic type mappings and the sources to be used during the mapping. "
        		+ " If no configuration file is provided, a default one will be used.  ");
        configuration_file.setRequired(false);
        options.addOption(configuration_file);
        
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
        String umlsDirectoryPath = cmd.getOptionValue("input_umls_directory");
        String configurationFilePath = cmd.getOptionValue("configuration_file");
        
        if (!java.nio.file.Files.isDirectory(Paths.get(umlsDirectoryPath))) {
    		log.error("Please set the input_umls_directory");
			System.exit(1);
    	}
        
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		log.error("Please set the inputDirectoryPath ");
			System.exit(1);
    	}
    	
    	File outputDirectory = new File(outputFilePath);
	    if(!outputDirectory.exists())
	    	outputDirectory.mkdirs();
	    
	    if(workdirPath==null) {
	    	workdirPath="";
	    }
	    
	    try {
    		loadConfigurationFile(workdirPath,  configurationFilePath);
    	}catch(Exception e) {
    		log.error("Exception ocurred see the log for more information", e);
    		System.exit(1);
    	}
	    
	    try {
	    	String internalDictPath = workdirPath + "dict" + File.separator + "umls_terminology_.txt";
		    generateInternalDic(umlsDirectoryPath, internalDictPath);
    	}catch(Exception e) {
    		log.error("Exception ocurred see the log for more information", e);
    		System.exit(1);
    	}
	    
	    try {
			Gate.init();
		} catch (GateException e) {
			log.error("Wrapper::generatePlainText :: Gate Exception  ", e);
			System.exit(1);
		}

	    try {
			generateNERList(workdirPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
		try {
			processTagger(inputFilePath, outputFilePath, workdirPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    /**
     * Generate NER From UMLS Dictionary 
     * @param propertiesParameters
	 * @throws IOException 
     */
	private static void generateNERList(String workdirPath) throws IOException {
		String umls_terminology_path = workdirPath+"dict/umls_terminology_.txt";
		String umls_terminology_ner = workdirPath+"ner_list/umls_terminology_.txt";
		generateNERGazzetterWithPriority(umls_terminology_path, umlsDictionary, umls_terminology_ner, "MISC", "10.0");
	}
    
	/**
     * Load semantic type information from the MRSTY file and the given configuration
     * @param mrstyPath
     * @return
     * @throws IOException
     */
	private static Map<String, String[]> loadSemanticTypeData(String mrstyPath) throws IOException {
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		File mrstyFile = new File(mrstyPath);
	    BufferedReader brty = new BufferedReader(new FileReader(mrstyFile));
	    String lineType;
	    while ((lineType = brty.readLine()) != null) {
	    	String[] dataType = lineType.split("\\|");
			//CUI | TUI | STN | STY | ATUI | CVF
			//if is a semantic type to include
	    	//The preference is the same as in the umls subset definition.
	    	if(semanticTypesMap.get(dataType[1])!=null) {
	    		if(map.get(dataType[0])==null) {
	    			map.put(dataType[0], new String[]{dataType[1], dataType[3]});
	    		}
	    	}
	    }
	    brty.close();
	    return map;
	}
	
	/**
     * Generates an internal dictionary from the umls rrf files, this is for filer the information with the given configuration file,
     * also improves the performance.
     * @param propertiesParameters
     */
    private static void generateInternalDic(String inputDirectoryFilePath , String outputFilePath) {
    	System.out.println("Generating internal dictionary given the configuration");
    	String mrconsoPath =  inputDirectoryFilePath + File.separator + "MRCONSO.RRF";
    	String mrstyPath =  inputDirectoryFilePath + File.separator + "MRSTY.RRF";
    	if (!Files.isRegularFile(Paths.get(mrconsoPath))) {
    		System.out.println("MRCONSO.RRF not found:  " + mrconsoPath);
    		System.exit(1);
    	}
    	if (!Files.isRegularFile(Paths.get(mrstyPath))) {
    		System.out.println("MRSTY.RRF not found:  " + mrstyPath);
    		System.exit(1);
    	}
    	try {
    		//Load semantic type data in memory, for performance reasons.
    		//key CUI and data TUI
		    Map<String, String[]> cui_semantyc_type = loadSemanticTypeData(mrstyPath);
    		
	    	File fout = new File(outputFilePath);
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,StandardCharsets.UTF_8));
			bw.write("KEYWORD\tAUI\tCUI\tSUI\tSOURCE\tSOURCE_CODE\tSEM_TYPE\tSEM_TYPE_STR\n");
			bw.flush();
			
			File mrconsoFile = new File(mrconsoPath);
	    	BufferedReader br;
		    br = new BufferedReader(new FileReader(mrconsoFile));
			String line;
			Boolean all_sources = sourceList.isEmpty();
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\\|");
				if((all_sources | sourceList.contains(data[11])) && data[6].equals("Y")) {
					String[] semanticType =  cui_semantyc_type.get(data[0]);
					//if there is no info in semanticType it mean that the cui it is not in the semantic type given in the configuration.
					if(semanticType!=null) {
						bw.write(data[14] + "\t" + data[7] + "\t" + data[0] + "\t" + data[5] + "\t" + data[11] + "\t" + data[13] + "\t" + semanticType[0] + "\t" + semanticType[1] + "\n");
					}
				}
				 bw.flush();
			 }
			 bw.close();
			 br.close();
		} catch (FileNotFoundException e) {
			System.out.println("FileNotFoundException");
			e.printStackTrace();
			System.exit(1);
			
		} catch (IOException e) {
			System.out.println("IOException ");
			e.printStackTrace();
			System.exit(1);
		}
    	System.out.println("End dictionary generation");
	}
    
    /**
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
     * @throws IOException 
	 */
	public static void processTagger(String inputDirectoryPath, String outputDirectoryPath, String workdirPath) throws IOException {
    	Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma,  ner, regexner, entitymentions ");
		props.put("regexner.mapping", workdirPath+"ner_list/umls_terminology_.txt");
		props.put("regexner.posmatchtype", "MATCH_ALL_TOKENS");
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		log.info("App::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						System.out.println("Wrapper::processTagger :: processing file : " + file.getAbsolutePath());
						File outputGATEFile = new File (outputDirectoryPath +  File.separator + file.getName());
						executeDocument(pipeline, file, outputGATEFile);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (InvalidOffsetException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (GateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
		private static void executeDocument(StanfordCoreNLP pipeline, File inputFile, File outputGATEFile) throws ResourceInstantiationException, MalformedURLException, InvalidOffsetException {
			long startTime = System.currentTimeMillis();
			gate.Document gateDocument = Factory.newDocument(inputFile.toURI().toURL(), "UTF-8");
			String plainText = gateDocument.getContent().getContent(0l, gate.Utils.lengthLong(gateDocument)).toString();
			
//			String text = "Except for thinning of fur, all effects observed at the end of treatment were not observed at the end of "
//					+ "the recovery period indicating complete reversibility of these effects.";
			//String text2 = " RCC On completion of treatment THAS all animals were sacrificed under general KetavetÂ® anesthesia by exsanguination via the left and right femoral blood vessels .";
			
			Annotation document = new Annotation(plainText.toLowerCase());
			//Annotation document = new Annotation(text2.toLowerCase());
			pipeline.annotate(document);
			long endTime = System.currentTimeMillis();
			log.info(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
	        try {	
	        	List<CoreMap> sentences= document.get(SentencesAnnotation.class);
			    for(CoreMap sentence: sentences) {
			    	List<CoreLabel> tokens= sentence.get(TokensAnnotation.class);
			    	List<CoreMap> entityMentions = sentence.get(MentionsAnnotation.class);
			        for (CoreMap entityMention : entityMentions) {
			        	String term = entityMention.get(TextAnnotation.class).replaceAll("\n", " ");
			        	String label = entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
			        	Integer termBegin = entityMention.get(CharacterOffsetBeginAnnotation.class);
			        	Integer termEnd = entityMention.get(CharacterOffsetEndAnnotation.class);
				        if(!AnnotationUtil.stopWordsEn.contains(term) && !AnnotationUtil.entityMentionsToDelete.contains(label) && label.startsWith("T") && label.contains("_A") 
				        		&& term.length()>2) {
				        	annotate(gateDocument, sentence, termBegin, termEnd, term, label, "dictionary", tokens, entityMention, null);
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
		 * Annotate the information retrieved from the NER.
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
			FeatureMap features = Factory.newFeatureMap();
			try {
				String cui = label.substring(label.lastIndexOf("_")+1);
				label = label.substring(0, label.lastIndexOf("_"));
				findFeatures(umlsDictionary, features, cui);
				features.put("source", "UMLS");
				features.put("inst", "BSC");
				features.put("annotationMethod", annotationMethod);
				features.put("text", term);
				//TODO some hardcode filters
				if((features.get("SEM_TYPE").equals("T033") && features.get("SOURCE").equals("SNOMEDCT_US"))  //the finding semantic type of snomed_us 
						//| (features.get("SEM_TYPE").equals("T017") && features.get("SOURCE").equals("SNOMEDCT_VET")) //the anatomical structure semantic type of snomed_vet 
						| (features.get("SEM_TYPE").equals("T033") && features.get("SOURCE").equals("SNOMEDCT_VET")) //the finding semantic type of snomed_vt  
						| (StringUtils.isAllUpperCase(features.get("KEYWORD").toString()) & features.get("SOURCE").equals("OMIM")) //OMIM has to many ...  upper case abbreviations 
						)
				{
					
				} else {
					gateDocument.getAnnotations("BSC").add(new Long(meBegin), new Long(meEnd), semanticTypesMap.get(label)!=null?semanticTypesMap.get(label):features.get("SEM_TYPE_STR").toString(), features);
					//gateDocument.getAnnotations(features.get("SOURCE").toString()).add(new Long(meBegin), new Long(meEnd), semanticTypesMap.get(label)!=null?semanticTypesMap.get(label):features.get("SEM_TYPE_STR").toString(), features);
				}
			} catch (Exception e) {
				System.out.println("No controlled exception with label :  " + label + " text:  " + features.get("text"));
				System.out.println(e);
			}

		}
	/**
	 * Find features from dictionary.
	 * @param label
	 * @param text
	 * @return
	 */
	private static FeatureMap findFeatures(Map<String,EntityInstance> dictionary, FeatureMap features,String internal_code) {
		EntityInstance entity = dictionary.get(internal_code);
		if(entity!=null) {
			for (ReferenceValue reference : entity.getReferenceValues()) {
				features.put(reference.getName(), reference.getValue());
			}
		}
		return features;
	}


	/**
	 * Generate NER Gazzeter from the umls terminology taking into account the semantic types indicated
	 * @param dictionaryPath
	 * @param entities
	 * @param outPutNerGazetterPath
	 * @param tags_to_overwrite
	 * @param priority
	 * @throws IOException
	 */
	private static void generateNERGazzetterWithPriority(String dictionaryPath, Map<String,EntityInstance> entities,String outPutNerGazetterPath, String tags_to_overwrite, String priority) throws IOException {
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
				//for generate subset taking into account the semantic types indicated
				//if(semanticTypesMap.get(data[6])!=null) {
				terms.add(getScapedKeyWordNER(data[0].toLowerCase()) + "\t" +  data[6].toUpperCase()+"_"+data[1]+ "\t" +  tags_to_overwrite + "\t" +  priority +"\n");
				entities.put(data[1], retrieveEntity(data, columnNames));
				//}
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
			String id = data[1];
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
			EntityInstance entityInstance = new EntityInstance(id,referenceValues);
			return entityInstance;
		} catch(Exception e) {
			System.out.println("Error reading custom tag tagged line " + data);
			System.out.println(e);
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
	
	/**
     * Load semantic type to retrieve and the corresponding label mapping
     * @throws IOException 
     * 
     */
    private static void loadConfigurationFile(String workdirPath, String  configurationFilePath) throws IOException {
    	if(workdirPath==null) {
    		workdirPath = "";
		}
    	if(configurationFilePath==null) {
    		System.out.println("Configuration file not provided, the default one will be used.");
    		configurationFilePath = workdirPath + "config.properties"; 
    	}
    	if (Files.isRegularFile(Paths.get(configurationFilePath))) {
    		BufferedReader br = new BufferedReader(new FileReader(configurationFilePath));
		    String line;
		    Boolean found = false;
		    Boolean semType = false;
		    while ((line = br.readLine()) != null) {
		    	if(!line.startsWith("#") && !line.trim().equals("")) {
		    		//sources configuration
		    		if(line.contains("[SOURCES]")) {
		    			while (!found && (line = br.readLine()) != null) {
		    				if(line.startsWith("sources=")) {
		    					found=true;
		    					String sources = line.substring(line.indexOf('=')+1);
		    					if(!sources.equals("ALL_SOURCES")) {
		    						System.out.println("This sources will be use: " + sources);
		    						String[] data_source = sources.split("\\|");
		    						for (String string : data_source) {
		    							sourceList.add(string);
									}
		    					}else {
		    						System.out.println("All sources will be used");
		    					}
		    				}
		    			}
		    			if(!found) {
		    				System.out.println("Error reading sources configuration, please review the config.properties");
		    				System.exit(1);
		    			}
		    		}
		    		//semantic type mapping
		    		if(line.contains("[SEMANTIC_TYPES]")) {
		    			System.out.println("Semantic maps:");
		    			semType = true;
		    			while ((line = br.readLine()) != null) {
		    				if(!line.startsWith("#") && !line.trim().equals("")) {
			    				if(!line.contains("[SEMANTIC_TYPES_END]")) {
			    					String[] data = line.split("\\|");
							    	if(data.length==3) {
							    		System.out.println(line);
							    		semanticTypesMap.put(data[0], data[2]);
							    	}else {
							    		System.out.println("Error reading semantic type, line:  " + line);
							    		System.exit(1);
							    	}
			    				}else {
			    					break;
			    				}
		    					
		    				}	
		    			}
		    		}
		    	}
		    }
		    
		    if(!found) {
		    	System.out.println("Error no source configuration finded : "  + configurationFilePath);
				System.exit(1);
		    }
		    if(!semType) {
		    	System.out.println("Error no semantic mapping configuration found : "  + configurationFilePath);
				System.exit(1);
		    }
		    
		    br.close();
    	}else {
    		System.out.println("Cannot find configuration file: " + configurationFilePath);
    		System.exit(1);
    	}
	}
	
}
