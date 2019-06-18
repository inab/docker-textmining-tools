package es.bsc.inb.umlstagger.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.creole.Plugin;
import gate.creole.SerialAnalyserController;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;

/**
 * UMLS Tagger.
 * Given the UMLS Terminology this tool annotate documents with a given configuration of sources and semantic types. 
 * 
 * @author jcorvi
 *
 */
public class App {
	
	static final Logger log = Logger.getLogger("log");
	
	static Map<String,String> semanticTypesMap = new HashMap<String,String>();
	
	static Map<String,String> semanticTypesMapExcluded = new HashMap<String,String>();
	 
	static List<String> sourceList = new ArrayList<String>();
	
    public static void main( String[] args ){
    	
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
        
        Option dictOutput = new Option("d", "dictOutput", true, "Optional destination folder of internal dictionary generated from the umls terminology, if not an internal path is used. This option is recommended if you want to have access to the gazetter generated with your configuration.");
        dictOutput.setRequired(false);
        options.addOption(dictOutput);
        
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
        String annotationSet = cmd.getOptionValue("annotation_set");
        String dictOutputPath = cmd.getOptionValue("dictOutput");
        if (!java.nio.file.Files.isDirectory(Paths.get(umlsDirectoryPath))) {
        	System.out.println("Please set the input_umls_directory");
			System.exit(1);
    	}
        
        if (annotationSet==null) {
        	System.out.println("Please set the annotation set where the annotation will be included");
			System.exit(1);
    	}
        
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
        	System.out.println("Please set the inputDirectoryPath ");
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
    		System.out.println("Exception ocurred see the log for more information");
    		e.printStackTrace();
    		System.exit(1);
    	}
	    
	    String listsDefinitionsPath = null;
	    try {
	    	String dictFolderPath = workdirPath + "dictionary";
		    if(dictOutputPath!=null) {
		    	if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
		        	System.out.println("The outputDictionaryFolder : " + dictOutputPath + " do not exist, no output of dictionary will be done ");
				}else {
					dictFolderPath = dictOutputPath;
				}
		    }
	    	File dictFolder = new File(dictFolderPath);
		    if(!dictFolder.exists())
		    	dictFolder.mkdirs();
		    String gazzeteer = dictFolder + File.separator + "terms.lst";
		    listsDefinitionsPath = dictFolder + File.separator + "lists.def";
		    generateDictionary(umlsDirectoryPath, gazzeteer, listsDefinitionsPath);
    	}catch(Exception e) {
    		System.out.println("Exception ocurred see the log for more information");
    		e.printStackTrace();
    		System.exit(1);
    	}
	    try {
			Gate.init();
		} catch (GateException e) {
			System.out.println("App::main :: Gate Exception  ");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String japeRules = workdirPath+"jape_rules/main.jape";
	    	process(inputFilePath, outputFilePath, listsDefinitionsPath, japeRules, annotationSet);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (GateException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
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
    private static void generateDictionary(String inputDirectoryFilePath , String outputFilePath, String listDefPath) {
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
					if(semanticType!=null && !excludeSemanticTypeBySource(data[11], semanticType[0])) {
						//bw.write(data[14] + "\tAUI=" + data[7] + "\tCUI=" + data[0] + "\tSUI=" + data[5] + "\tSOURCE=" + data[11] + "\tSOURCE_CODE=" + data[13] + "\tSEM_TYPE=" + semanticType[0] + "\tSEM_TYPE_STR=" + semanticType[1] + "\tLABEL=" + semanticTypesMap.get( semanticType[0]) + "\n");
						bw.write(data[14] + "\tCUI=" + data[0] + "\tUMLS_SOURCE=" + data[11] + "\tUMLS_SOURCE_CODE=" + data[13] + "\tSEM_TYPE=" + semanticType[0] + "\tSEM_TYPE_STR=" + semanticType[1] + "\tLABEL=" + semanticTypesMap.get( semanticType[0]) + "\n");
					}
				}
				bw.flush();
			 } 
			bw.close();
			fos.close();
			br.close();
			cui_semantyc_type=null;
			File listDef = new File(listDefPath);
			FileOutputStream foslistDef = new FileOutputStream(listDef);
			BufferedWriter bwfoslistDef = new BufferedWriter(new OutputStreamWriter(foslistDef,StandardCharsets.UTF_8));
			bwfoslistDef.write("terms.lst:UMLS:UMLS");
			bwfoslistDef.flush();
			bwfoslistDef.close();
			foslistDef.close(); 
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
     * Annotation Process
     * @param inputDirectory
     * @param outputDirectory
     * @param listsDefinitionsPath
     * @param japeRules
     * @param annotationSet
     * @throws GateException
     * @throws IOException
     */
    private static void process(String inputDirectory, String outputDirectory, String listsDefinitionsPath, String japeRules, String annotationSet) throws GateException, IOException {
    	try {
    		System.out.println("App :: main :: INIT PROCESS");
	    	Corpus corpus = Factory.newCorpus("My Files"); 
	    	File directory = new File(inputDirectory); 
	    	ExtensionFileFilter filter = new ExtensionFileFilter("Txt files", new String[]{"txt","xml"}); 
	    	URL url = directory.toURL(); 
	    	corpus.populate(url, filter, null, false);
	    	Plugin anniePlugin = new Plugin.Maven("uk.ac.gate.plugins", "annie", "8.5"); 
	    	Gate.getCreoleRegister().registerPlugin(anniePlugin); 
	    	// create a serial analyser controller to run ANNIE with 
	    	SerialAnalyserController annieController =  (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController",
	    			Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE"); 
	    	annieController.setCorpus(corpus); 
	    	 
	    	//Gazetter parameters
	    	FeatureMap params = Factory.newFeatureMap(); 
	    	params.put("listsURL", new File(listsDefinitionsPath).toURL());
	    	params.put("gazetteerFeatureSeparator", "\t");
	    	//params.put("longestMatchOnly", true);
	    	//params.put("wholeWordsOnly", false);
	    	ProcessingResource treatment_related_finding_gazetter = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params); 
	    	//treatment_related_finding_gazetter.setParameterValue("longestMatchOnly", true);
	    	//treatment_related_finding_gazetter.setParameterValue("wholeWordsOnly", false);
	    	annieController.add(treatment_related_finding_gazetter);
	    	
		    LanguageAnalyser jape = (LanguageAnalyser)gate.Factory.createResource("gate.creole.Transducer", gate.Utils.featureMap(
				              "grammarURL", new File(japeRules).toURI().toURL(),"encoding", "UTF-8"));
			jape.setParameterValue("outputASName", annotationSet);
			annieController.add(jape);
			// execute controller 
		    annieController.execute();
		    Factory.deleteResource(treatment_related_finding_gazetter);
		    Factory.deleteResource(jape);
		    Factory.deleteResource(annieController);	
		    Gate.removeKnownPlugin(anniePlugin);
		    //Save documents in different output
		    for (Document  document : corpus) {
		    	String nameOutput = "";
		    	if(document.getName().indexOf(".txt")!=-1) {
		    		nameOutput =  document.getName().substring(0, document.getName().indexOf(".txt")+4).replace(".txt", ".xml"); 
		    	}else {
		    		nameOutput = document.getName().substring(0, document.getName().indexOf(".xml")+4);
		    	}
		    	java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(new File(outputDirectory + File.separator + nameOutput), false)));
			    out.write(document.toXml());
			    out.close();
			}
    	}catch(Exception e) {
    		System.out.println("App :: main :: ERROR ");
    		e.printStackTrace();
    		System.exit(1);
	    }	 
	    System.out.println("App :: main :: END PROCESS");
    }
    
    /**
     * Exclude semantic type by source.
     * @param source 
     * @param semantiType
     * @return
     */
    private static boolean excludeSemanticTypeBySource(String source, String semantiType) {
		String sourceSemTypes = semanticTypesMapExcluded.get(source);
		if(sourceSemTypes!=null) {
			String[] data = sourceSemTypes.split("\\|");
			return Arrays.asList(data).contains(semantiType);
		}
		return false;
	}

	
	
	/**
     * Load the configuration: semantic types and sources to be used
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
		    		//semantic type mapping
		    		if(line.contains("[EXCLUDED_SEMANTIC_TYPES_BY_SOURCE]")) {
		    			System.out.println("Semantic maps to exclude by source:");
		    			semType = true;
		    			while ((line = br.readLine()) != null) {
		    				if(!line.startsWith("#") && !line.trim().equals("")) {
			    				if(!line.contains("[EXCLUDED_SEMANTIC_TYPES_BY_SOURCE_END]")) {
			    					String[] data = line.split("=");
							    	if(data.length==2) {
							    		System.out.println(line);
							    		semanticTypesMapExcluded.put(data[0], data[1]);
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
