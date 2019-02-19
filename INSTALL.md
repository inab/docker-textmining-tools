# Rough instructions to build the docker images

docker build -t etransafe.bsc.es/tesseract-poppler tesseract-poppler
docker build -t etransafe.bsc.es/grobid grobid
docker build -t etransafe.bsc.es/cermine cermine
docker build -t etransafe.bsc.es/ocrmypdf ocrmypdf

#To run the pipeline (ocrmypdf + grobid) with nextflow
./nextflow run pipeline.nf
