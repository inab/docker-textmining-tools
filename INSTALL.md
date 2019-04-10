# Rough instructions to build the docker images

docker build -t tesseract-poppler tesseract-poppler
docker build -t grobid grobid
docker build -t cermine cermine
docker build -t ocrmypdf ocrmypdf
docker build -t linnaeus-gate-wrapper linnaeus-gate-wrapper
docker build -t dnorm-gate-wrapper dnorm-gate-wrapper
docker build -t metamap-gate-wrapper metamap-gate-wrapper

#To run the pipeline (ocrmypdf + grobid) with nextflow
./nextflow run pipeline.nf
