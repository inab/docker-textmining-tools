// Script parameters

//Configuration of the original pdf directory
params.original_pdf_folder = "/home/jcorvi/eTRANSAFE_DATA/nextflow_example/original_pdf"
//Configuration of the preprocessing ocrmypdf directory, store pdf files after pdf process
params.preprocessing_pdf_folder = "/home/jcorvi/eTRANSAFE_DATA/nextflow_example/preprocessed_pdf"
//Configuration of the grobid out directory, store the pdf annotated with the structure of the study report
params.grobid_output_folder = "/home/jcorvi/eTRANSAFE_DATA/nextflow_example/grobid_output"
//force ocr allways ?, if not set a validation will run to see if the pdf is readable or not.  
//It's strongly recomended that the scanned pdf and the readable pdf were put in different folders. 
//And for the already readable pdf this parameter set as False, if all the documents are in the same directory and hava a mix of scanned and readable pdf its recomended
//to set this parameter to True  
params.forceOCR = "True"

original_pdf_folder_ch = Channel.fromPath( params.original_pdf_folder, type: 'dir' )
preprocessing_pdf_folder = file(params.preprocessing_pdf_folder)
grobid_output_folder=file(params.grobid_output_folder)

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
    grobid-core -exe processFullText -dIn $preprocessing_pdf_folder_2 -dOut $grobid_output_folder
	
    """
}

workflow.onComplete { 
	println ("Workflow Done !!! ")
}