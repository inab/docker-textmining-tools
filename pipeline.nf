#!/usr/bin/env nextflow

log.info """
The input directory is: ${params.inputDir}. Contains the pdf to be processed.
Base directory to use: ${params.baseDir}. This directory is used to output all the pipeline results.
"""
.stripIndent()

//Configuration of the original pdf directory
params.original_pdf_folder = "${params.inputDir}"
//Configuration of the preprocessing ocrmypdf directory, store pdf files after pdf process
params.preprocessing_pdf_folder = "${params.baseDir}/preprocessed_pdf"
//Configuration of the grobid out directory, store the pdf annotated with the structure of the study report
params.grobid_output_folder = "${params.baseDir}/grobid_output"
//force ocr allways ?, if not set a validation will run to see if the pdf is readable or not.  
//It's strongly recomended that the scanned pdf and the readable pdf were put in different folders. 
//And for the already readable pdf this parameter set as False, if all the documents are in the same directory and hava a mix of scanned and readable pdf its recomended
//to set this parameter to True  
params.forceOCR = "True"
//Output directory for the linnaeus tagger step
params.linnaeus_output_folder = "${params.baseDir}/linnaeus_output"
//Output directory for the dnorm tagger step
params.dnorm_output_folder = "${params.baseDir}/dnorm_output"
//Output directory for the umls tagger step
params.umls_output_folder = "${params.baseDir}/umls_output"
//Output directory for the ades tagger step
params.ades_output_folder = "${params.baseDir}/ades_output"

original_pdf_folder_ch = Channel.fromPath( params.original_pdf_folder, type: 'dir' )
preprocessing_pdf_folder = file(params.preprocessing_pdf_folder)
grobid_output_folder=file(params.grobid_output_folder)
linnaeus_output_folder=file(params.linnaeus_output_folder)
dnorm_output_folder=file(params.dnorm_output_folder)
umls_output_folder=file(params.umls_output_folder)
ades_output_folder=file(params.ades_output_folder)

process ocrmypdf {
    input:
    file original_pdf_folder from original_pdf_folder_ch
    
    output:
    val preprocessing_pdf_folder into preprocessing_pdf_folder_ch
    
    """
    python3 /app/main.py -i $original_pdf_folder -o $preprocessing_pdf_folder -forceOCR params.forceOCR
	
    """
}

process grobid {
    input:
    file preprocessing_pdf_folder_2 from preprocessing_pdf_folder_ch
    
    output:
    val grobid_output_folder into grobid_output_folder_ch
    	
    """
    grobid-core -e JAVA_OPTS=-Xmx5G -exe processFullText -dIn $preprocessing_pdf_folder_2 -dOut $grobid_output_folder -Xmx1024m
	
    """
}

process linnaeus_wrapper {
    input:
    file input_linnaeus from grobid_output_folder_ch
    
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
    umls-tagger -i $input_umls -o $umls_output_folder
	
    """
}

process addes_tagger {
    input:
    file input_addes from umls_output_folder_ch
    
    output:
    val ades_output_folder into ades_output_folder_ch
    	
    """
    ades-tagger -i $input_ades -o $ades_output_folder
	
    """
}

workflow.onComplete { 
	println ("Workflow Done !!! ")
}
