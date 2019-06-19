package es.bsc.inb.gnormplus.gate.wrapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import GNormPluslib.GNormPlus;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

/**
 * Wrapper GNormPlus for Gate document and TEI standard format
 * @author jcorvi
 *
 */
class Wrapper {
	
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

        Option workdir = new Option("workdir", "workdir", false, "workDir directory path");
        workdir.setRequired(false);
        options.addOption(workdir);
        
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
			System.out.println("Wrapper:: main :: Gate Exception");
			System.out.println(e);
			System.exit(1);
		}
 
    	try {
    		String tmpWorkDir = outputFilePath + File.separator + "gnormplus_tmp";
    		String tmpWorkDirDat = outputFilePath + File.separator + "gnormplus_tmp_dat";
    		createGNormFormat(inputFilePath, tmpWorkDir, workdirPath);
    		executeGNormPlusTagger(tmpWorkDir, tmpWorkDirDat, workdirPath);
    		annotateGateDocuments(inputFilePath, outputFilePath ,tmpWorkDirDat, annotationSet);
            deleteDirectory(new File(tmpWorkDir));
            deleteDirectory(new File(tmpWorkDirDat));
    	}catch(Exception e) {
    		System.out.println("Exception ocurred see the log for more information");
    		System.out.println(e);
    		e.printStackTrace();
    		System.exit(1);
    	}
    	
    }   
    
   

	/**
     * Execute GNormPlus Tagger
     * @param tmpWorkDir
     * @param outputFile
     */
    private static void executeGNormPlusTagger(String inputFile, String outputFile, String workdirPath) {
    	if(workdirPath==null) {
    		workdirPath = "";
		}
    	File workdir = new File(workdirPath + outputFile);
	    if (!workdir.exists()) {
			boolean r = workdir.mkdir();
			if(!r) {
				System.out.println("Wrapper::processTagger :: cannot create tmp folder in  " + workdir.getAbsolutePath());
				System.exit(1);
			}
		}
    	System.out.println("WorkDirectory : " + workdirPath);
    	String[] args = {inputFile , outputFile};
		try {
			GNormPlus.main(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Annotate the gates documents with the plain tagged information.
	 * @param properties_parameters_path
	 */
	private static void annotateGateDocuments(String inputDirectoryPath,String outputDirectoryPath,String taggedDirectory, String annotationSet) {
		System.out.println("Wrapper::annotateGateDocuments :: INIT ");
		File inputDirectory = new File(inputDirectoryPath);
		File[] files =  inputDirectory.listFiles();
		for (File file : files) {
			if(file.getName().endsWith(".xml")  || file.getName().endsWith(".txt")){
				System.out.println("Wrapper::annotateGateDocuments :: processing file : " + file.getAbsolutePath());
				String fileOutPutName = file.getName();
				if(fileOutPutName.endsWith(".txt")) {
					fileOutPutName = fileOutPutName.replace(".txt", ".xml");
				}
				File outputFile = new File(outputDirectoryPath + File.separator + fileOutPutName);
				annotateGateDocuemt(file, outputFile, taggedDirectory + File.separator + file.getName() + ".txt", annotationSet);
			}
		}
		System.out.println("Wrapper::annotateGateDocuments :: END ");
	}
    
	/**
	 * Annotate Gate document	
	 * @param inputGATEFile
	 * @param linnaeusOutput
	 */
	 public static void annotateGateDocuemt(File inputGATEFile, File outputGATEFile, String gnormPlusOutput, String annotationSet) {
		 if (Files.isRegularFile(Paths.get(gnormPlusOutput))) {
			gate.Document toxicolodyReportWitAnnotations;
			try {
				toxicolodyReportWitAnnotations = Factory.newDocument(inputGATEFile.toURI().toURL(), "UTF-8");
				BufferedReader br = new BufferedReader(new FileReader(gnormPlusOutput));
			    String line = br.readLine();
			    if(line!=null && line.length()>0) {
			    	while ((line = br.readLine()) != null && !line.trim().equals("")) {
				    	String[] data = line.split("\t");
				    	String source = "GNORMPLUS";
				    	//String id = data[0];
						Long startOff = new Long(data[1]);
						Long endOff =  new Long(data[2]);
						String text = data[3];
						String label = data[4];
						String source_id = data[5];
						FeatureMap features = gate.Factory.newFeatureMap();
						features.put("source", source);
						features.put("text", text);
						//features.put("inst", "BSC");
						if(source_id!=null && !source_id.trim().equals("")) {
							features.put("ncbi", source_id);
						}
						toxicolodyReportWitAnnotations.getAnnotations(annotationSet).add(startOff, endOff, label, features);
					}
			    	br.close();
			    	java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(outputGATEFile, false)));
					out.write(toxicolodyReportWitAnnotations.toXml());
					out.close();
			    }
			} catch (FileNotFoundException e) {
				System.out.println("Wrapper::annotateGateDocuemt ::  " + inputGATEFile);
				System.out.println(e);
			} catch (IOException e) {
				System.out.println("Wrapper::annotateGateDocuemt ::  " + inputGATEFile);
				System.out.println(e);
			} catch (ResourceInstantiationException e) {
				System.out.println("Wrapper::annotateGateDocuemt ::  " + inputGATEFile);
				System.out.println(e);
			} catch (InvalidOffsetException e) {
				System.out.println("Wrapper::annotateGateDocuemt ::  " + inputGATEFile);
				System.out.println(e);
			}
		}
	}
	
	
    /**
	 * Execute the Tagger
	 * @param properties_parameters_path
	 */
	public static void createGNormFormat(String inputDirectoryPath, String tmpWordDir, String workdirPath) {
		System.out.println("Wrapper::createGNormFormat :: INIT ");
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			//create tmp directory to work
			File workdir = new File(tmpWordDir);
		    if (!workdir.exists()) {
				boolean r = workdir.mkdir();
				if(!r) {
					System.out.println("Wrapper::createGNormFormat :: cannot create tmp folder in  " + workdir.getAbsolutePath());
					System.exit(1);
				}
			}
		    File[] files =  inputDirectory.listFiles();
			for (File file : files) {
				if(file.getName().endsWith(".xml")  || file.getName().endsWith(".txt")){
					try {
						System.out.println("Wrapper::createGNormFormat :: processing file : " + file.getAbsolutePath());
						gate.Document toxicolodyReportWitAnnotations = Factory.newDocument(file.toURI().toURL(), "UTF-8");
						String plainText = toxicolodyReportWitAnnotations.getContent().getContent(0l, gate.Utils.lengthLong(toxicolodyReportWitAnnotations)).toString();
						String plainTextPath = tmpWordDir + File.separator + file.getName()+".txt";
						plainText = plainText.replaceAll("\t", " ").replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\\|", " ").replaceAll("\\p{C}", "?");
						plainText = file.getName()+"|a|"+plainText+"\n\n";
						createTxtFile(plainTextPath, plainText);
					} catch (ResourceInstantiationException e) {
						System.out.println("Wrapper::createGNormFormat :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					} catch (MalformedURLException e) {
						System.out.println("Wrapper::createGNormFormat :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					} catch (InvalidOffsetException e) {
						System.out.println("Wrapper::createGNormFormat :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					} catch (FileNotFoundException e) {
						System.out.println("Wrapper::createGNormFormat :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					} catch (IOException e) {
						System.out.println("Wrapper::createGNormFormat :: error with document " + file.getAbsolutePath());
						System.out.println(e);
					}catch (Exception e) {
						System.out.println("Wrapper::createGNormFormat :: uncontrolled error with document " + file.getAbsolutePath());
						System.out.println(e);
					}
				}
			}
		}
		System.out.println("Wrapper::createGNormFormat :: END ");
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
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos,StandardCharsets.UTF_8));
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
