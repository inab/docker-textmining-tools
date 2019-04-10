Grobid docker
========================

<b>Grobid docker, internal use.</b>   


========================

GROBID (or Grobid) means GeneRation Of BIbliographic Data.

GROBID is a machine learning library for extracting, parsing and re-structuring raw documents such as PDF into structured TEI-encoded documents with a particular focus on technical and scientific publications. First developments started in 2008 as a hobby. In 2011 the tool has been made available in open source. Work on GROBID has been steady as side project since the beginning and is expected to continue until at least 2020 :)

More on Grobid https://grobid.readthedocs.io/en/latest/

========================

Build and run the docker individually

# To build the docker, just go into the grobid folder and execute
docker build -t grobid .
#To run the docker, just set the input_folder and the output
mkdir ${PWD}/grobid_output; docker run --rm -u $UID -v ${PWD}/input_folder_pdf:/in:ro -v ${PWD}/grobid_output:/out:rw grobid grobid-core -e JAVA_OPTS=-Xmx5G -exe processFullText -dIn /in -dOut /out -Xmx1024m

To parameters:

-dIn input folder with the pdf to be parsed. The documents could be plain txt or xml gate documents.

-dOut output folder with the documents annotated in gate format.