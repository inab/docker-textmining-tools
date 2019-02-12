#!/usr/bin/env python3

import sys
import argparse
import os
import logging
from shutil import copyfile
from subprocess import check_call

logging.basicConfig(format='%(levelname)s:%(message)s', level=logging.DEBUG)

parser=argparse.ArgumentParser()
parser.add_argument('-i', help='Input Folder')
parser.add_argument('-o', help='Output Folder')
parser.add_argument('-forceOCR', help='Force OCR execution in the pdf files, if not a validation to verify if a pdf is readable is done')
args=parser.parse_args()
parameters={}
if __name__ == '__main__':
    import main
    parameters = main.ReadParameters(args)     
    main.Main(parameters)

def Main(parameters):
    inputDirectory=parameters['inputDirectory']
    outputDirectory= parameters['outputDirectory']
    forceOCR= parameters['forceOCR']
    execute(inputDirectory, outputDirectory,forceOCR)

    
def ReadParameters(args):
    """Read the parameters of the module, see --help"""
    missing_parameter=False
    if(args.i!=None):
        parameters['inputDirectory']=args.i
    else:
        missing_parameter=True
        logging.error("Please set the input folder parameter, for more information --help ")
    if(args.o!=None):
        parameters['outputDirectory']=args.o
    else:
        missing_parameter=True
        logging.error("Please set the output folder parameter, for more information --help ")
    
    if(missing_parameter):
        logging.error("Please set the correct parameters before continue --help ")
        sys.exit(1)
    
    if(args.forceOCR!=None and args.forceOCR=='True'):
        parameters['forceOCR']=True
        logging.info("Force OCR is set to TRUE no validation of pdf text size will be done, and OCR will be execute in all the pdf")
    else:
        parameters['forceOCR']=False
        logging.info("Force OCR is not set, by default a validation of readable pdf will be done")
    return parameters    

def execute(input_dir, output_dir,forceOCR):
    """Main execution method"""
    logging.info("OCRMYPDF  : Begin the execution ")     
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    ids_list=[]
    if(os.path.isfile(output_dir+"/list_files_processed.dat")):
        with open(output_dir+"/list_files_processed.dat",'r') as ids:
            for line in ids:
                ids_list.append(line.replace("\n",""))
    if os.path.exists(input_dir):
        onlyfiles_toprocess = [os.path.join(input_dir, f) for f in os.listdir(input_dir) if (os.path.isfile(os.path.join(input_dir, f)) & f.endswith('.pdf') & (os.path.basename(f) not in ids_list))]
    
    with open(output_dir+"/list_files_processed.dat",'a') as list_files:    
        for file in onlyfiles_toprocess:  
            output_file_readable_pdf =  output_dir + "/" + os.path.basename(file)[:os.path.basename(file).rfind('.')] + '.pdf'
            #out_file_txt =  output_dir + "/" + os.path.basename(file)[:os.path.basename(file).rfind('.')] + '.txt'
            logging.info("Processiong file : " +  file)     
            try:
                #ret = call_ocrmypdf(file, output_file_readable_pdf, out_file_txt)
                ret = call_ocrmypdf(file, output_file_readable_pdf, forceOCR)
                if(ret==0):
                    list_files.write(os.path.basename(file)+"\n")
                    list_files.flush()
            except Exception as inst:
                logging.error("call_ocrmypdf :  error with document: " +  file, " error :  " + str(inst))        
    logging.info("Pdf to Text End Process")   


   


def call_ocrmypdf(input_file_pdf, output_file_readable_pdf,forceOCR):
    """Call OCRMYPDF, if forceOCR is True always execute the OCR if not, previously validate if the PDF is readable (contain text)"""
    if(forceOCR!=True):
        if(isNotReadable(input_file_pdf)):
            resp = check_call("ocrmypdf  %s %s" % (input_file_pdf, output_file_readable_pdf),   shell=True)
            if(resp==1):
                logging.error("ocrmypdf error, on file  : " + input_file_pdf + ".  output file : "  + output_file_readable_pdf)
                return 1
            return 0
        else:
            #just copy the file to output
            copyfile(input_file_pdf,output_file_readable_pdf)
    else:
        resp = check_call("ocrmypdf  %s %s --force-ocr" % (input_file_pdf, output_file_readable_pdf),   shell=True)
        if(resp==1):
            logging.error("ocrmypdf error, on file  : " + input_file_pdf + ".  output file : "  + output_file_readable_pdf)
            return 1
        return 0
    
def isNotReadable(input_file_pdf):
    """ Verify if a PDF is readable or not, just using the file size of the pdftotext output"""
    logging.info("Verify if pdf need OCR")
    resp = check_call("pdftotext  %s %s " % (input_file_pdf, input_file_pdf+".txt"),   shell=True)
    if(resp==1):
        logging.error("pdftotext error, on file  : " + input_file_pdf )
        return True
    else:
        size = os.path.getsize(input_file_pdf+".txt")
        if(size==0):
            logging.info("The size is  0, so OCR will be executed")
            return True
        else: 
            logging.info("The size is different than 0, so no OCR will be executed")
            return False
    return True

