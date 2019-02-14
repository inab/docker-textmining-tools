docker build -t etransafe.bsc.es/tesseract-poppler tesseract-poppler
docker build -t etransafe.bsc.es/grobidi grobid
docker build -t etransafe.bsc.es/cermine cermine
docker build -t etransafe.bsc.es/ocrmypdf ocrmypdf

#To run the pipeline (ocrmypdf + grobid) with nextflow
./nextflow run pipeline.nf