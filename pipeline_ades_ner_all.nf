#!/usr/bin/env nextflow


//./nextflow run /home/jcorvi/projects/pdf_preprocessing/docker-textmining-tools/pipeline_ades_ner_all_ocr_grob.nf  --inputDir /home/jcorvi/eTRANSAFE_DATA/evaluation/nextflow_test/grobid_error/ --baseDir /home/jcorvi/eTRANSAFE_DATA/evaluation/nextflow_test/
//./nextflow run /home/jcorvi/projects/pdf_preprocessing/docker-textmining-tools/pipeline_ades_ner_all.nf -name 17_06_2019_1 --inputDir /home/jcorvi/eTRANSAFE_DATA/evaluation/eTransafe_table_based_gold_standard_v2/ --baseDir /home/jcorvi/eTRANSAFE_DATA/evaluation/eTransafe_table_based_gold_standard_v2
//./nextflow run /home/jcorvi/projects/pdf_preprocessing/docker-textmining-tools/pipeline_ades_ner_all.nf -name 17_06_2019_1 --inputDir /home/jcorvi/eTRANSAFE_DATA/evaluation/nextflow_test/corpus_gate/ --baseDir /home/jcorvi/eTRANSAFE_DATA/evaluation/nextflow_test
//./nextflow run /home/jcorvi/projects/pdf_preprocessing/docker-textmining-tools/pipeline_ades_ner_all.nf -name 17_06_2019_1 --inputDir /home/jcorvi/eTRANSAFE_DATA/evaluation/bayer_curation_internal/test/ --baseDir /home/jcorvi/eTRANSAFE_DATA/evaluation/bayer_curation_internal/test/

log.info """
The input directory is: ${params.inputDir}, Contains the pdf to be processed.
Base directory to use: ${params.baseDir}, This directory is used together with the pipeline name (-name parameter) output the results.
The output will be located at ${params.baseDir}/${workflow.runName}
"""
.stripIndent()

//set default parameters
//pipeline process
params.pipeline = "PRE,LINN,DNORM,UMLS,ADES,ADES_P"  


//Configuration of the original pdf directory
params.original_pdf_folder = "${params.inputDir}"

params.general = [
    paramsout:          "${params.baseDir}/execution-results/params_${workflow.runName}.json",
    resultout:          "${params.baseDir}/execution-results/results_${workflow.runName}.txt",
    pipeline:			"${params.pipeline}"
]

pipeline = params.general.pipeline.split(',')

steps = [:]

params.umls_tagger = [
	instalation_folder: "/home/jcorvi/umls-2018AB-full/2018AB-full/ADESVERSION/2018AB/META"
]
//instalation_folder: "/home/jcorvi/umls-2018AB-full/2018AB-full/ADESVERSION/2018AB/META"
//V2 le faltan corpus 
//V3 tiene todo luego se usara este directamente y se filtrara por el componente.
params.folders = [
	//Output directory for the linnaeus tagger step
	nlp_standard_preprocessing_output_folder: "${params.baseDir}/nlp_standard_preprocessing_output",
	//Output directory for the linnaeus tagger step
	linnaeus_output_folder: "${params.baseDir}/linnaeus_output",
	//Output directory for the dnorm tagger step
	dnorm_output_folder: "${params.baseDir}/dnorm_output",
	//Output directory for the hepatotoxicity tagger step
	hepatotoxicity_output_folder: "${params.baseDir}/hepatotoxicity_output",
	//Output directory for the umls tagger step
	umls_output_folder: "${params.baseDir}/umls_output",
	//Output directory for the umls tagger step
	cdisc_etox_output_folder: "${params.baseDir}/cdisc_etox_output",
	//Output directory for the umls tagger step
	own_ades_terms_output_folder: "${params.baseDir}/own_ades_output",
	//Output directory for the ades tagger step
	ades_output_folder: "${params.baseDir}/ades_output",
	//Output directory for the post processing ades
	ades_post_output_folder: "${params.baseDir}/ades_ner_postprocessing_output",
	//Output directory for the post processing ades
	ades_relation_extraction_output_folder: "${params.baseDir}/ades_relation_extraction_output",
	//Output directory for the post processing ades
	ades_export_to_json_output_folder: "${params.baseDir}/ades_export_to_json_output"
]

params.folders_steps = [
	//Output directory for the linnaeus tagger step
	PRE: "${params.baseDir}/nlp_standard_preprocessing_output",
	//Output directory for the linnaeus tagger step
	LINN: "${params.baseDir}/linnaeus_output",
	//Output directory for the dnorm tagger step
	DNORM: "${params.baseDir}/dnorm_output",
	//Output directory for the hepatotoxicity step
	HEP: "${params.baseDir}/hepatotoxicity_output",
	//Output directory for the umls tagger step
	UMLS: "${params.baseDir}/umls_output",
	//Output directory for the cdisc_etox tagger step
	CDISC_ETOX: "${params.baseDir}/cdisc_etox_output",
	//Output directory for the cdisc_etox tagger step
	OWN_TERMS: "${params.baseDir}/own_ades_terms_output",
	//Output directory for the ades tagger step
	ADES: "${params.baseDir}/ades_output",
	//Output directory for the post processing ades
	ADES_P: "${params.baseDir}/ades_ner_postprocessing_output",
	//Output directory for the post processing ades
	ADES_REL_EXT: "${params.baseDir}/ades_relation_extraction_output",
	//Output directory for export json
	ADES_JSON: "${params.baseDir}/ades_export_to_json_output"
]

original_pdf_folder_ch = Channel.fromPath( params.original_pdf_folder, type: 'dir' )

nlp_standard_preprocessing_output_folder=file(params.folders.nlp_standard_preprocessing_output_folder)
linnaeus_output_folder=file(params.folders.linnaeus_output_folder)
dnorm_output_folder=file(params.folders.dnorm_output_folder)
hepatotoxicity_output_folder=file(params.folders.hepatotoxicity_output_folder)
umls_output_folder=file(params.folders.umls_output_folder)
cdisc_etox_output_folder=file(params.folders.cdisc_etox_output_folder)
own_ades_terms_output_folder=file(params.folders.own_ades_terms_output_folder)
ades_output_folder=file(params.folders.ades_output_folder)
ades_post_output_folder=file(params.folders.ades_post_output_folder)
ades_relation_extraction_output_folder=file(params.folders.ades_relation_extraction_output_folder)
ades_export_to_json_output_folder=file(params.folders.ades_export_to_json_output_folder)
ner_evaluation_output=file(params.general.resultout)

original_pdf_folder = params.original_pdf_folder

class StepPipeline {
   int id;
   String name;
   int order;
   String inputDir;
   String outputDir;	
   StepPipeline(id, name, inputDir, outputDir) {          
        this.id = id
        this.name = name
        this.inputDir = inputDir
        this.outputDir = outputDir
   }
   
   String toString(){
   		return "Step: \n	id: " + id + "\n	" + " name: " + name + "\n	" + " inputDir: " + inputDir + "\n	" + " outputDir: " + outputDir + "\n" 
   }
}


void ProcessUserInputArguments(){
    def toRemove = []
    // Detect user inputed arguments.
    // Param's values are all dictionaries, whose class is null
    // User inputed arguments always(?) have classes associated
    for (param in params) 
        if (param.value.class != null)
            toRemove.add(param.key)
	// Transform the user inputed arguments into params
    for (userArgument in toRemove) {
		// Split the name into a list of entries, 
        // which represent a hierarchy inside params
        splittedArgument = userArgument.split('\\.')
		// Traverse the hierarchy starting from params
        curDict = params
        partOfConfiguration = true
		if (splittedArgument.size() > 1) {
            int x = 0;
            // For each hierarchy level in the hierarchy obtained (but last element)
            for (; x < (splittedArgument.size() - 1); x++) {
				// Get the current hierarchy level
                hierarchy = splittedArgument[x]
				// Check if the current hierarchy dict contains the current level as key
                // This is useful to inform the user of misspelled arguments
                if (curDict.containsKey(hierarchy)){
                    if (curDict[hierarchy] != null && curDict[hierarchy].class == null){
                        // Move deeper inside the hierarchy
                        curDict = curDict[hierarchy]    
                        continue  
                    }
                }

                // If current hierarchy dict does not contain the current level, inform the user
                println "[Config Wrapper] Argument \"" + userArgument + "\" is not part of the configuration"

                // Reverse the flag params from containing wrong information
                partOfConfiguration = false
                break
            }

            if (x == splittedArgument.size() - 1) {
                // Check if we have to change a value in params
                if (partOfConfiguration && curDict.containsKey(splittedArgument[x])) {
                    println "[Config Wrapper] " + userArgumALLent + " new value: " + params[userArgument] 
                    curDict[splittedArgument[splittedArgument.size() - 1]] = params[userArgument]
                } else {
                    println "[Config Wrapper] Argument \"" + userArgument + "\" is not part of the configuration\n" +
                            "                 \"" + splittedArgument[x] + "\" is not an element of \"" + splittedArgument[x - 1] + "\"" 
                }
            }
        } 
        else 
            println "[Config Wrapper] Argument \"" + userArgument + "\" is not part of the configuration"
        
        params.remove(userArgument)
    }
}

void ProcessPipelineParameters(){
	print(pipeline)
	if(pipeline!="ALL"){
	    int i = 1
		for (s in pipeline){
			if(steps.size()==0){
		 		stepPip = new StepPipeline(i,s, original_pdf_folder, params.folders_steps[s])
				print(stepPip)
			}else{
				stepPip = new StepPipeline(i,s,params.folders_steps[pipeline[i-2]], params.folders_steps[s])
				print(stepPip)
			}
			steps.putAt(stepPip.name, stepPip)	
			i=i+1
		}	
	}else{
		print(pipeline)
	}
}

void printSection(section, level = 1){
    println (("  " * level) + "↳ " + section.key)
    if (section.value.class == null)
    {
        for (element in section.value)
        {
            printSection(element, level + 1)
        }
    }
    else {
        if (section.value == "")
            println (("  " * (level + 1) ) + "↳ Empty String")
        else
            println (("  " * (level + 1) ) + "↳ " + section.value)
    }
}

void PrintConfiguration(){
    println ""
    println "=" * 34
    println "ADEs Text-mining pipeline Configuration"
    println "=" * 34

    for (configSection in params) {
        printSection(configSection)
        println "=" * 30
    }

    println "\n"
}

String parseElement(element){
    if (element instanceof String || element instanceof GString ) 
        return "\"" + element + "\""    

    if (element instanceof Integer)
        return element.toString()

    if (element.value.class == null)
    {
        StringBuilder toReturn = new StringBuilder()
        toReturn.append()
        toReturn.append("\"")
        toReturn.append(element.key)
        toReturn.append("\": {")

        for (child in element.value)
        {
            toReturn.append(parseElement(child))
            toReturn.append(',')
        }
        toReturn.delete(toReturn.size() - 1, toReturn.size() )
        
        toReturn.append('}')
        return toReturn.toString()
    } 
    else 
    {
        if (element.value instanceof String || element.value instanceof GString ) 
            return "\"" + element.key + "\": \"" + element.value +ades_post_output_folder +"\""            

        else if (element.value instanceof ArrayList)
        {
            // println "\tis a list"
            StringBuilder toReturn = new StringBuilder()
            toReturn.append("\"")
            toReturn.append(element.key)
            toReturn.append("\": [")
            for (child in element.value)
            {
                toReturn.append(parseElement(child)) 
                toReturn.append(",")                
            }
            toReturn.delete(toReturn.size() - 1, toReturn.size() )
            toReturn.append("]")
            return toReturn.toString()
        }

        return "\"" + element.key + "\": " + element.value
    }
}

def SaveParamsToFile() {
    // Check if we want to produce the params-file for this execution
    if (params.paramsout == "")
        return;

    // Replace the strings ${baseDir} and ${workflow.runName} with their values
    //params.general.paramsout = params.general.paramsout
    //    .replace("\${baseDir}".toString(), baseDir.toString())
    //    .replace("\${workflow.runName}".toString(), workflow.runName.toString())

    // Store the provided paramsout value in usedparamsout
    params.general.usedparamsout = params.general.paramsout

    // Compare if provided paramsout is the default value
    if ( params.general.paramsout == "${baseDir}/param-files/${workflow.runName}.json"){
        // And store the default value in paramsout
        params.general.paramsout = "\${baseDir}/param-files/\${workflow.runName}.json"
    }

    // Inform the user we are going to store the params-file and how to use it.
    println "[Config Wrapper] Saving current parameters to " + params.general.usedparamsout + "\n" +
            "                 This file can be used to input parameters providing \n" + 
            "                   '-params-file \"" + params.general.usedparamsout + "\"'\n" + 
            "                   to nextflow when running the workflow."


    // Manual JSONification of the params, to avoid using libraries.
    StringBuilder content = new StringBuilder();
    // Start the dictionary
    content.append("{")

    // As parseElement only accepts key-values or dictionaries,
    //      we iterate here for each 'big-category'
    for (element in params) 
    {
        // We parse the element
        content.append(parseElement(element))
        // And add a comma to separate elements of the list
        content.append(",")
    }

    // Remove the last comma
    content.delete(content.size() - 1, content.size() )
    // And add the final bracket
    content.append("}")

    // Create a file handler for the current usedparamsout
    configJSON = file(params.general.usedparamsout)
    // Make all the dirs of usedparamsout path
    configJSON.getParent().mkdirs()
    // Write the contents to file
    configJSON.write(content.toString())
}


//Execution Begin


ProcessUserInputArguments()
ProcessPipelineParameters()
PrintConfiguration()
SaveParamsToFile()


process nlp_standard_preprocessing {
    input:
    file input_nlp_standard_preprocessing from original_pdf_folder_ch
    
    output:
    val nlp_standard_preprocessing_output_folder into nlp_standard_preprocessing_output_folder_ch
    
    //when:
    //pipeline[0]=="ALL" || pipeline.contains("PRE")
    
    script:
    """
    nlp-standard-preprocessing -i $input_nlp_standard_preprocessing -o $nlp_standard_preprocessing_output_folder -a BSC
	
    """
}

process cdisc_etox_annotation {
    input:
    file input_cdisc_etox from nlp_standard_preprocessing_output_folder_ch
    
    output:
    val cdisc_etox_output_folder into cdisc_etox_output_folder_ch
    	
    """
    cdisc-etox-annotation -i $input_cdisc_etox -o $cdisc_etox_output_folder -a BSC -ia BSC
	
    """
}

process own_ades_terms_annotation {
    input:
    file input_own_ades_terms from cdisc_etox_output_folder_ch
    
    output:
    val own_ades_terms_output_folder into own_ades_terms_output_folder_ch
    	
    """
    own-ades-terminology-annotation -i $input_own_ades_terms -o $own_ades_terms_output_folder -a BSC -ia BSC
	
    """
}


process linnaeus_wrapper {
    input:
    file input_linnaeus from own_ades_terms_output_folder_ch
    
    output:
    val linnaeus_output_folder into linnaeus_output_folder_ch
    	
    """
    linnaeus-gate-wrapper -i $input_linnaeus -o $linnaeus_output_folder -a BSC
	
    """
}

process dnorm_wrapper {
    input:
    file input_dnorm from linnaeus_output_folder_ch
    
    output:
    val dnorm_output_folder into dnorm_output_folder_ch
    	
    """
    dnorm-gate-wrapper -i $input_dnorm -o $dnorm_output_folder -a BSC
	
    """
}

process hepatotoxicity_annotation {
    input:
    file input_hepatotoxicity from dnorm_output_folder_ch
    
    output:
    val hepatotoxicity_output_folder into hepatotoxicity_output_folder_ch
    	
    """
    hepatotoxicity-annotation -i $input_hepatotoxicity -o $hepatotoxicity_output_folder -a BSC
	
    """
}

process umls_tagger {
    input:
    file input_umls from hepatotoxicity_output_folder_ch
   
    output:
    val umls_output_folder into umls_output_folder_ch
    	
    """
    umls-tagger -u $params.umls_tagger.instalation_folder -i $input_umls -o $umls_output_folder -a BSC
	
    """
}



process ades_tagger {
    input:
    file input_ades from umls_output_folder_ch
    
    output:
    val ades_output_folder into ades_output_folder_ch
    	
    """
    ades-tagger -i $input_ades -o $ades_output_folder -a BSC
	
    """
}

process ades_ner_postprocessing {
    input:
    file input_ades_post from ades_output_folder_ch
    
    output:
    val ades_post_output_folder into ades_post_output_folder_ch
    	
    """
    ades-ner-postprocessing -i $input_ades_post -o $ades_post_output_folder -a BSC
	
    """
}

process ades_relation_extraction {
    input:
    file input_ades_relation_extraction from ades_post_output_folder_ch
    
    output:
    val ades_relation_extraction_output_folder into ades_relation_extraction_output_ch
    	
    """
    ades-relation-extraction -i $input_ades_relation_extraction -o $ades_relation_extraction_output_folder -a BSC -ar TREATMENT_RELATED_FINDINGS
	
    """
}

process ades_export_to_json {
    input:
    file input_ades_to_json from ades_relation_extraction_output_ch
    
    output:
    val ades_export_to_json_output_folder into ades_export_to_json_output_ch
    	
    """
    ades-export-to-json -i $input_ades_to_json -o $ades_export_to_json_output_folder -a BSC -ar TREATMENT_RELATED_FINDINGS
	
    """
}

process import_json_to_mongo {
    input:
    file input_import_json_to_mongo from ades_export_to_json_output_ch
    	
    """
    import-json-to-mongo -i $input_import_json_to_mongo
	
    """
}

//process evaluation_ner {
//    input:
//    file input_ner_evaluation from ades_post_output_folder_ch
    
//    output:
//    val ner_evaluation_output into ner_validation_output_ch
    	
//    """
//    evaluation-ner -i $input_ner_evaluation -oades_export_to_json_output_folder $ner_evaluation_output -k EVALUATION -e BSC
	
//    """
//}

workflow.onComplete { 
	println ("Workflow Done !!! ")
}
