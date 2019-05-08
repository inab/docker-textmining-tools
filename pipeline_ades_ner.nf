#!/usr/bin/env nextflow

log.info """
The input directory is: ${params.inputDir}, Contains the pdf to be processed.
Base directory to use: ${params.baseDir}, This directory is used to output all the pipeline results.
"""
.stripIndent()

//Configuration of the original pdf directory
params.original_pdf_folder = "${params.inputDir}"


//UMLS instalation path 
//params.umls_instalation_folder ="/home/jcorvi/umls-2018AB-full/2018AB-full/ADESVERSION/2018AB/META"
//umls_instalation_folder_ch = Channel.fromPath( params.umls_instalation_folder, type: 'dir' )


//Output directory for the linnaeus tagger step
params.nlp_standard_preprocessing_output_folder = "${params.baseDir}/nlp_standard_preprocessing_output"
//Output directory for the linnaeus tagger step
params.linnaeus_output_folder = "${params.baseDir}/linnaeus_output"
//Output directory for the dnorm tagger step
params.dnorm_output_folder = "${params.baseDir}/dnorm_output"
//Output directory for the umls tagger step
params.umls_output_folder = "${params.baseDir}/umls_output"
//Output directory for the ades tagger step
params.ades_output_folder = "${params.baseDir}/ades_output"

original_pdf_folder_ch = Channel.fromPath( params.original_pdf_folder, type: 'dir' )

nlp_standard_preprocessing_output_folder=file(params.nlp_standard_preprocessing_output_folder)
linnaeus_output_folder=file(params.linnaeus_output_folder)
dnorm_output_folder=file(params.dnorm_output_folder)
umls_output_folder=file(params.umls_output_folder)
ades_output_folder=file(params.ades_output_folder)


process nlp_standard_preprocessing {
    input:
    file input_nlp_standard_preprocessing from original_pdf_folder_ch
    
    output:
    val nlp_standard_preprocessing_output_folder into nlp_standard_preprocessing_output_folder_ch
    	
    """
    nlp-standard-preprocessing -i $input_nlp_standard_preprocessing -o $nlp_standard_preprocessing_output_folder
	
    """
}

process linnaeus_wrapper {
    input:
    file input_linnaeus from nlp_standard_preprocessing_output_folder_ch
    
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
    umls-tagger -u "/home/jcorvi/umls-2018AB-full/2018AB-full/ADESVERSION/2018AB/META" -i $input_umls -o $umls_output_folder
	
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





workflow.onComplete { 
	println ("Workflow Done !!! ")
}
