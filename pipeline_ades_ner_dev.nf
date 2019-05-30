#!/usr/bin/env nextflow

log.info """
The input directory is: ${params.inputDir}, Contains the pdf to be processed.
Base directory to use: ${params.baseDir}, This directory is used to output all the pipeline results.
The name of the workflow execution is ${workflow.runName}
"""
.stripIndent()

//set default parameters
//pipeline process
params.pipeline = "PRE,LINN,UMLS,ADES,ADES_P"  


//Configuration of the original pdf directory
params.original_pdf_folder = "${params.inputDir}"

params.general = [
    paramsout:          "${params.baseDir}/param-files/${workflow.runName}.json",
    resultout:          "${params.baseDir}/result-files/${workflow.runName}.json",
    pipeline:			"${params.pipeline}",
    input_files: 		"${params.inputDir}"
]

pipeline = params.general.pipeline.split(',')
//input_files = params.general.input_files

steps = [:]

params.umls_tagger = [
	instalation_folder: "/home/jcorvi/umls-2018AB-full/2018AB-full/ADESVERSION/2018AB/META"
]

params.folders = [
	//Output directory for the linnaeus tagger step
	nlp_standard_preprocessing_output_folder: "${params.baseDir}/nlp_standard_preprocessing_output",
	//Output directory for the linnaeus tagger step
	linnaeus_output_folder: "${params.baseDir}/linnaeus_output",
	//Output directory for the dnorm tagger step
	dnorm_output_folder: "${params.baseDir}/dnorm_output",
	//Output directory for the umls tagger step
	umls_output_folder: "${params.baseDir}/umls_output",
	//Output directory for the ades tagger step
	ades_output_folder: "${params.baseDir}/ades_output",
	//Output directory for the post processing ades
	ades_post_output_folder: "${params.baseDir}/ades_ner_postprocessing_output"
]

params.folders_steps = [
	//Output directory for the linnaeus tagger step
	PRE: "${params.baseDir}/nlp_standard_preprocessing_output",
	//Output directory for the linnaeus tagger step
	LINN: "${params.baseDir}/linnaeus_output",
	//Output directory for the dnorm tagger step
	DNORM: "${params.baseDir}/dnorm_output",
	//Output directory for the umls tagger step
	UMLS: "${params.baseDir}/umls_output",
	//Output directory for the ades tagger step
	ADES: "${params.baseDir}/ades_output",
	//Output directory for the post processing ades
	ADES_P: "${params.baseDir}/ades_ner_postprocessing_output",
	//Eval output file
	EVAL: "${params.baseDir}/performance_n.txt"
]

//original_pdf_folder_ch = Channel.fromPath(params.original_pdf_folder, type: 'dir' )

nlp_standard_preprocessing_output_folder=file(params.folders.nlp_standard_preprocessing_output_folder)
linnaeus_output_folder=file(params.folders.linnaeus_output_folder)
dnorm_output_folder=file(params.folders.dnorm_output_folder)
umls_output_folder=file(params.folders.umls_output_folder)
ades_output_folder=file(params.folders.ades_output_folder)
ades_post_output_folder=file(params.folders.ades_post_output_folder)
ner_evaluation_output=file("${params.baseDir}/performance_n.txt")


//nlp_standard_preprocessing_output_ch = Channel.fromPath(nlp_standard_preprocessing_output_folder)
steps_channel=[:]
steps_channel.putAt("PRE", nlp_standard_preprocessing_output_folder)	
//println("9999")
//println(steps_channel["PRE"])


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
        //this.inputChannel = Channel.fromPath( params.original_pdf_folder, type: 'dir' )
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
		 		stepPip = new StepPipeline(i,s, params.general.input_files, params.folders_steps[s])
				print(stepPip)
			}else{
				stepPip = new StepPipeline(i,s,params.folders_steps[pipeline[i-2]], params.folders_steps[s])
				print(stepPip)
			}
			steps.putAt(stepPip.name, stepPip)	
			i=i+1
		}
		stepPip = new StepPipeline(i,"EVAL",params.folders_steps[pipeline[i-2]], params.folders_steps["EVAL"])
		steps.putAt("EVAL", stepPip)	
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
            return "\"" + element.key + "\": \"" + element.value + "\""            

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
println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv")


println(steps_channel["PRE"])

println("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv")

ProcessUserInputArguments()
ProcessPipelineParameters()
PrintConfiguration()
SaveParamsToFile()

process nlp_standard_preprocessing {
    input:
    file input_nlp_standard_preprocessing from Channel.fromPath(params.general.input_files)
	
	output:
    val nlp_standard_preprocessing_output_folder into nlp_standard_preprocessing_output_ch
    
    //when:
    //pipeline[0]=="ALL" || pipeline.contains("PRE")
    
    script:
    println(input_nlp_standard_preprocessing)
    """
    nlp-standard-preprocessing -i $input_nlp_standard_preprocessing -o $nlp_standard_preprocessing_output_folder
	
    """
}

process linnaeus_wrapper {
    input:
    file input_linnaeus from steps_channel["PRE"]
    
    output:
    val linnaeus_output_folder into linnaeus_output_folder_ch
    	
    """
    linnaeus-gate-wrapper -i $input_linnaeus -o $linnaeus_output_folder
	
    """
}

process dnorm_wrapper {
    input:
    file input_dnorm from linnaeus_output_folder_ch
    
    output:
    val dnorm_output_folder into dnorm_output_folder_ch
    	
    """
    dnorm-gate-wrapper -i $input_dnorm -o $dnorm_output_folder
	
    """
}

process umls_tagger {
    input:
    file input_umls from dnorm_output_folder_ch
   
    output:
    val umls_output_folder into umls_output_folder_ch
    	
    """
    umls-tagger -u $params.umls_tagger.instalation_folder -i $input_umls -o $umls_output_folder
	
    """
}

process ades_tagger {
    input:
    file input_ades from umls_output_folder_ch
    
    output:
    val ades_output_folder into ades_output_folder_ch
    	
    """
    ades-tagger -i $input_ades -o $ades_output_folder
	
    """
}

process ades_ner_postprocessing {
    input:
    file input_ades_post from ades_output_folder_ch
    
    output:
    val ades_post_output_folder into ades_post_output_folder_ch
    	
    """
    ades-ner-postprocessing -i $input_ades_post -o $ades_post_output_folder
	
    """
}

process evaluation_ner {
    input:
    file input_ner_evaluation from ades_post_output_folder_ch
    
    output:
    val ner_evaluation_output into ner_validation_output_ch
    	
    """
    evaluation-ner -i $input_ner_evaluation -o $ner_evaluation_output -k EVALUATION -e BSC
	
    """
}

workflow.onComplete { 
	println ("Workflow Done !!! ")
}
