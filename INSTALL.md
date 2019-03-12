# Rough instructions to build the docker images

docker build -t etransafe.bsc.es/tesseract-poppler tesseract-poppler
docker build -t etransafe.bsc.es/grobid grobid
docker build -t etransafe.bsc.es/cermine cermine
docker build -t etransafe.bsc.es/ocrmypdf ocrmypdf
docker build -t etransafe.bsc.es/linnaeus-gate-wrapper linnaeus-gate-wrapper
docker build -t etransafe.bsc.es/dnorm-gate-wrapper dnorm-gate-wrapper
docker build -t etransafe.bsc.es/dnorm-gate-wrapper dnorm-gate-wrapper

#To run the pipeline (ocrmypdf + grobid) with nextflow
./nextflow run pipeline.nf
