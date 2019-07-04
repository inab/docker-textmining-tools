package es.bsc.inb.linnaeus.gate.wrapper;

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

import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import uk.ac.man.entitytagger.EntityTagger;
/**
 * Wrapper Linnaeus for Gate document and TEI standard format
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
        
        Option set = new Option("a", "annotation_set", true, "Annotation set where the annotation will be included");
        set.setRequired(true);
        options.addOption(set);
        
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
        String annotationSet = cmd.getOptionValue("annotation_set");
        if (!java.nio.file.Files.isDirectory(Paths.get(inputFilePath))) {
    		log.error("Please set the inputDirectoryPath ");
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
			log.error("Wrapper::generatePlainText :: Gate Exception  ", e);
			System.exit(1);
		}
    	try {
    		String tmpWorkDir = outputFilePath + File.separator + "linnaeus_tmp";
            generatePlainText(inputFilePath, tmpWorkDir);
            executeLinnaeusTagger(tmpWorkDir, tmpWorkDir + File.separator + "linneaus_result.txt");
            annotateGateDocuments(inputFilePath, outputFilePath ,tmpWorkDir + File.separator + "linneaus_result.txt", annotationSet);
            deleteDirectory(new File(tmpWorkDir));
    	}catch(Exception e) {
    		log.error("Exception ocurred see the log for more information", e);
    		System.exit(1);
    	}
    }   
    
   

	/**
     * Execute Linneaus Tagger
     * @param tmpWorkDir
     * @param outputFile
     */
    private static void executeLinnaeusTagger(String tmpWorkDir, String outputFile) {
		String[] args = {"--textDir", tmpWorkDir, "--out", outputFile};
		EntityTagger.main(args);
	}


	/**
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
	 */
	private static void annotateGateDocuments(String inputDirectoryPath,String outputDirectoryPath,String linnaeusOutput, String annotationSet) {
		log.info("Wrapper::annotateGateDocuments :: INIT ");
		File inputDirectory = new File(inputDirectoryPath);
		File[] files =  inputDirectory.listFiles();
		for (File file : files) {
			if(file.getName().endsWith(".xml")  || file.getName().endsWith(".txt")){
				log.info("Wrapper::annotateGateDocuments :: processing file : " + file.getAbsolutePath());
				String fileOutPutName = file.getName();
				if(fileOutPutName.endsWith(".txt")) {
					fileOutPutName = fileOutPutName.replace(".txt", ".xml");
				}
				File outputFile = new File(outputDirectoryPath + File.separator + fileOutPutName);
				annotateGateDocuemt(file, outputFile, linnaeusOutput, annotationSet);
			}
		}
		log.info("Wrapper::annotateGateDocuments :: END ");
	}
    
	/**
	 * Annotate Gate document	
	 * @param inputGATEFile
	 * @param linnaeusOutput
	 */
	 public static void annotateGateDocuemt(File inputGATEFile, File outputGATEFile, String linnaeusOutput, String annotationSet) {
		 String docIdGate = inputGATEFile.getName().substring(0, inputGATEFile.getName().lastIndexOf("."));
		 if (Files.isRegularFile(Paths.get(linnaeusOutput))) {
			gate.Document toxicolodyReportWitAnnotations;
			try {
				toxicolodyReportWitAnnotations = Factory.newDocument(inputGATEFile.toURI().toURL(), "UTF-8");
				BufferedReader br = new BufferedReader(new FileReader(linnaeusOutput));
			    String line;
			    while ((line = br.readLine()) != null) {
			    	String[] data = line.split("\t");
				    String ncbi_map = data[0];
					String docId =  data[1];
					if(docId.equals(docIdGate)) {
						String source = "LINNAEUS";
					   	Long startOff = new Long(data[2]);
			    		Long endOff =  new Long(data[3]);
					   	String text = data[4];
						FeatureMap features = gate.Factory.newFeatureMap();
					    features.put("SOURCE", source);
						features.put("text", text);
						features.put("LINNAEUS_NCBI", ncbi_map);
						features.put("LINNAEUS_ORIGINAL_LABEL", "SPECIES");
						toxicolodyReportWitAnnotations.getAnnotations(annotationSet).add(startOff, endOff, "SPECIMEN", features);
					}
				}
			    br.close();
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
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
	 */
	public static void generatePlainText(String inputDirectoryPath, String tmpWordDir ) {
		log.info("Wrapper::generatePlainText :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			//create tmp directory for linnaeus
			File workdir = new File(tmpWordDir);
		    if (!workdir.exists()) {
				boolean r = workdir.mkdir();
				if(!r) {
					log.error("Wrapper::generatePlainText :: cannot create tmp folder in  " + workdir.getAbsolutePath());
					System.exit(1);
				}
			}
		    File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")  || file.getName().endsWith(".txt")){
					try {
						log.info("Wrapper::generatePlainText :: processing file : " + file.getAbsolutePath());
						gate.Document toxicolodyReportWitAnnotations = Factory.newDocument(file.toURI().toURL(), "UTF-8");
						String plainText = toxicolodyReportWitAnnotations.getContent().getContent(0l, gate.Utils.lengthLong(toxicolodyReportWitAnnotations)).toString();
						String plainTextPath = tmpWordDir + File.separator + file.getName().replace(".xml", ".txt");
						//create tmp dir for tagger
						createTxtFile(plainTextPath, plainText);
					} catch (ResourceInstantiationException e) {
						log.error("Wrapper::generatePlainText :: error with document " + file.getAbsolutePath(), e);
					} catch (MalformedURLException e) {
						log.error("Wrapper::generatePlainText :: error with document " + file.getAbsolutePath(), e);
					} catch (InvalidOffsetException e) {
						log.error("Wrapper::generatePlainText :: error with document " + file.getAbsolutePath(), e);
					} catch (FileNotFoundException e) {
						log.error("Wrapper::generatePlainText :: error with document " + file.getAbsolutePath(), e);
					} catch (IOException e) {
						log.error("Wrapper::generatePlainText :: error with document " + file.getAbsolutePath(), e);
					}
				}
			}
		}
		log.info("Wrapper::generatePlainText :: END ");
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
