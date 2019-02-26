package es.bsc.inb.dnorm.gate.wrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import dnorm.RunDNorm;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * Wrapper Dnorm for Gate document and TEI standard format
 * @author jcorvi
 *
 */
class Wrapper {
	
	static final Logger log = Logger.getLogger("log");
	
	/**
	 * Entry method to execute the system
	 * @param args
	 */
    public static void main(String[] args) {
    	
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
        
        Option configfile = new Option("configfile", "configfile", true, "Configuration file, if none is provided a default is used");
        configfile.setRequired(false);
        options.addOption(configfile);
        
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
        String configFilePath = cmd.getOptionValue("configfile");
        
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
			log.error("Wrapper::main :: Gate Exception  ", e);
			System.exit(1);
		}
 
    	try {
    		String tmpWorkDir = outputFilePath + File.separator + "dnorm_tmp";
    		processTagger(inputFilePath, tmpWorkDir, workdirPath, configFilePath);
            annotateGateDocuments(inputFilePath, outputFilePath ,tmpWorkDir);
            deleteDirectory(new File(tmpWorkDir));
    	}catch(Exception e) {
    		log.error("Exception ocurred see the log for more information", e);
    		System.exit(1);
    	}
    	
    }   
    
   

	/**
     * Execute DNorm Tagger
     * @param tmpWorkDir
     * @param outputFile
     */
    private static void executeDnormTagger(String inputFile, String outputFile, String workdirPath, String configFile) {
    	if(workdirPath==null) {
    		workdirPath = "";
		}
    	if(configFile==null) {
    		configFile=workdirPath+"/config/banner_NCBIDisease_TEST.xml";
    	}
    	System.out.println("WorkDirectory : " + workdirPath);
    	String[] args = {configFile, workdirPath+"/data/CTD_diseases.tsv", workdirPath+"/output/simmatrix_NCBIDisease_e4.bin", inputFile , outputFile};
		RunDNorm.main(args);
	}


	/**
	 * Annotate the gates documents with the plain tagged information.
	 * @param properties_parameters_path
	 */
	private static void annotateGateDocuments(String inputDirectoryPath,String outputDirectoryPath,String taggedDirectory) {
		log.info("Wrapper::annotateGateDocuments :: INIT ");
		File inputDirectory = new File(inputDirectoryPath);
		File[] files =  inputDirectory.listFiles();
		for (File file : files) {
			if(file.getName().endsWith(".xml")){
				log.info("Wrapper::annotateGateDocuments :: processing file : " + file.getAbsolutePath());
				File outputFile = new File(outputDirectoryPath + File.separator + file.getName());
				annotateGateDocuemt(file, outputFile, taggedDirectory + File.separator + file.getName().replace(".xml", ".txt.dat"));
			}
		}
		log.info("Wrapper::annotateGateDocuments :: END ");
	}
    
	/**
	 * Annotate Gate document	
	 * @param inputGATEFile
	 * @param linnaeusOutput
	 */
	 public static void annotateGateDocuemt(File inputGATEFile, File outputGATEFile, String linnaeusOutput) {
		 if (Files.isRegularFile(Paths.get(linnaeusOutput))) {
			gate.Document toxicolodyReportWitAnnotations;
			try {
				toxicolodyReportWitAnnotations = Factory.newDocument(inputGATEFile.toURI().toURL(), "UTF-8");
				BufferedReader br = new BufferedReader(new FileReader(linnaeusOutput));
			    String line;
			    while ((line = br.readLine()) != null) {
			    	String[] data = line.split("\t");
			    	String source = "DNORM";
					Long startOff = new Long(data[1]);
					Long endOff =  new Long(data[2]);
					String text = data[3].toLowerCase();
					FeatureMap features = gate.Factory.newFeatureMap();
					features.put("source", source);
					features.put("text", text);
					if(data.length==5) {
						String crossRef= data[4];
						if(crossRef.startsWith("MESH")) {
							features.put("MESH", crossRef.substring(crossRef.indexOf("MESH:")));
						}else if(crossRef.startsWith("OMIM")) {
							features.put("OMIM", crossRef.substring(crossRef.indexOf("OMIM:")));
						}else {
							log.warn("Wrapper: annotateGateDocument :: cross referencen different to mesh and omim ");
						}
					}
					toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "DISEASE", features);
				}
			    java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
				out.write(toxicolodyReportWitAnnotations.toXml());
				out.close();
			} catch (FileNotFoundException e) {
				log.error("Wrapper::annotateGateDocuemt ::  " + inputGATEFile, e);
			} catch (IOException e) {
				log.error("Wrapper::annotateGateDocuemt ::  " + inputGATEFile, e);
			} catch (ResourceInstantiationException e) {
				log.error("Wrapper::annotateGateDocuemt ::  " + inputGATEFile, e);
			} catch (InvalidOffsetException e) {
				log.error("Wrapper::annotateGateDocuemt ::  " + inputGATEFile, e);
			}
		}
	}
	
	
    /**
	 * Execute the Tagger
	 * @param properties_parameters_path
	 */
	public static void processTagger(String inputDirectoryPath, String tmpWordDir, String workdirPath, String configFile) {
		log.info("Wrapper::processTagger :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			//create tmp directory to work
			File workdir = new File(tmpWordDir);
		    if (!workdir.exists()) {
				boolean r = workdir.mkdir();
				if(!r) {
					log.error("Wrapper::processTagger :: cannot create tmp folder in  " + workdir.getAbsolutePath());
					System.exit(1);
				}
			}
		    File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")){
					try {
						log.info("Wrapper::processTagger :: processing file : " + file.getAbsolutePath());
						gate.Document toxicolodyReportWitAnnotations = Factory.newDocument(file.toURI().toURL(), "UTF-8");
						String plainText = toxicolodyReportWitAnnotations.getContent().getContent(0l, gate.Utils.lengthLong(toxicolodyReportWitAnnotations)).toString();
						String plainTextPath = tmpWordDir + File.separator + file.getName().replace(".xml", ".txt");
						plainText = plainText.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ");
						plainText = file.getName()+"\t"+plainText;
						createTxtFile(plainTextPath, plainText);
						executeDnormTagger(plainTextPath, plainTextPath+".dat", workdirPath, configFile);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (InvalidOffsetException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (FileNotFoundException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					} catch (IOException e) {
						log.error("Wrapper::processTagger :: error with document " + file.getAbsolutePath(), e);
					}
				}
			}
		}
		log.info("Wrapper::processTagger :: END ");
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
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.write(plainText);
		bw.flush();
		bw.close();
	}
	
	/**
	 * Force deletion of directory
	 * @param path
	 * @return
	 */
	static public boolean deleteDirectory(File path) {
		if (path.exists()) {
	        File[] files = path.listFiles();
	        for (int i = 0; i < files.length; i++) {
	            if (files[i].isDirectory()) {
	                deleteDirectory(files[i]);
	            } else {
	                files[i].delete();
	            }
	        }
	    }
	    return (path.delete());
	}
}
